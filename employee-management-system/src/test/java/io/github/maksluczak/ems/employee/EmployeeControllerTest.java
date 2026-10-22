package io.github.maksluczak.ems.employee;

import com.jayway.jsonpath.JsonPath;
import io.github.maksluczak.ems.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Service s3Service;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin", "admin123");
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String createEmployee() throws Exception {
        return mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Jan", "lastName": "Kowalski", "role": "EMPLOYEE", "position": "Developer"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void testRequestWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreatedEmployeeCanLogInAndSeeOwnProfile() throws Exception {
        String created = createEmployee();
        String employeeToken = login(JsonPath.read(created, "$.username"), JsonPath.read(created, "$.password"));

        mockMvc.perform(get("/api/v1/employees/me").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"));
    }

    @Test
    void testEmployeeCannotAccessAdminEndpoints() throws Exception {
        String created = createEmployee();
        String employeeToken = login(JsonPath.read(created, "$.username"), JsonPath.read(created, "$.password"));

        mockMvc.perform(get("/api/v1/employees").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEmployeeWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "", "lastName": "Kowalski", "position": "Developer"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    void testGetMissingEmployeeReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/employees/9999").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testAdminCanUpdateAndDeleteEmployee() throws Exception {
        Integer id = JsonPath.read(createEmployee(), "$.id");

        mockMvc.perform(put("/api/v1/employees/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position": "Team Leader"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/employees/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/employees/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUploadAndDownloadProfileImage() throws Exception {
        Integer id = JsonPath.read(createEmployee(), "$.id");
        byte[] image = {1, 2, 3};
        when(s3Service.getObject(anyString(), anyString())).thenReturn(image);

        mockMvc.perform(multipart("/api/v1/employees/" + id + "/profile-image")
                        .file(new MockMultipartFile("file", "avatar.png", "image/png", image))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/employees/" + id + "/profile-image")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().bytes(image));
    }
}