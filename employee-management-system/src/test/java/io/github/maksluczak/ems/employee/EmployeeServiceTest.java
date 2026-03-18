package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private S3Service s3Service;
    @Mock
    private S3Buckets s3Buckets;
    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void testCreateUserAndEmployeeAndGetHimById() {
        User user = User.builder()
                .username("johnsmith1")
                .email("johnsmith1@company.com")
                .password(passwordEncoder.encode("p@ssword123"))
                .role(Role.EMPLOYEE)
                .build();

        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Smith")
                .email("johnsmith1@company.com")
                .position("Senior Software Java Developer")
                .build();

        user.setEmployee(employee);
        employee.setUser(user);

        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        EmployeeResponse result = employeeService.getEmployeeById(1);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("profileImageId", "password", "user")
                .isEqualTo(employee);
    }

    @Test
    public void testUploadNewImageAndDeleteOldOne() {
        int employeeId = 1;
        String previousImageKey = "profile-image/1/old-uuid";
        Employee employee = Employee.builder()
                .id(employeeId)
                .profileImageId(previousImageKey)
                .build();

        byte[] testFileContent = "test-file".getBytes();
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                testFileContent);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(s3Buckets.getEmployee()).thenReturn("fs-maksluczak-client-test");

        employeeService.uploadEmployeeImage(employeeId, file);

        verify(s3Service).deleteObject("fs-maksluczak-client-test", previousImageKey);
        verify(s3Service).putObject(
                eq("fs-maksluczak-client-test"),
                contains("profile-image/" + employeeId),
                eq(testFileContent));
        verify(employeeRepository).updateProfileImageId(anyString(), eq(employeeId));
    }
}
