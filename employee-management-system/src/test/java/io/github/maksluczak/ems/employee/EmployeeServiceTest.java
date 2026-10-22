package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeRequest;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeResponse;
import io.github.maksluczak.ems.employee.generator.EmployeeStringGenerator;
import io.github.maksluczak.ems.exception.BadRequestException;
import io.github.maksluczak.ems.exception.ResourceNotFoundException;
import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmployeeStringGenerator stringGenerator;
    @Mock
    private S3Service s3Service;
    @Mock
    private S3Buckets s3Buckets;
    @InjectMocks
    private EmployeeService employeeService;

    public Employee employeeWithUser(int id, String username) {
        User user = User.builder()
                .id(id)
                .username(username)
                .role(Role.EMPLOYEE)
                .build();

        Employee employee = Employee.builder()
                .id(id)
                .firstName("John")
                .lastName("Smith")
                .position("Sr. Java Engineer")
                .user(user)
                .build();
        user.setEmployee(employee);
        return employee;
    }

    @Test
    void testInsertEmployeeGeneratesCredentialsAndEncodesPassword() {
        when(stringGenerator.generateEmployeeId()).thenReturn("AB12c");
        when(stringGenerator.generateEmployeeSecurePassword()).thenReturn("Secret#Pass12");
        when(passwordEncoder.encode("Secret#Pass12")).thenReturn("encoded");
        RegisterEmployeeRequest request =
                new RegisterEmployeeRequest("Julian", "Pike", Role.EMPLOYEE, "Tester");

        RegisterEmployeeResponse response = employeeService.insertEmployee(request);

        assertThat(response.getUsername()).isEqualTo("julianpike_AB12c");
        assertThat(response.getEmail()).isEqualTo("julianpike_AB12c@employee.company.com");
        assertThat(response.getPassword()).isEqualTo("Secret#Pass12");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(captor.getValue().getEmployee().getUser()).isSameAs(captor.getValue());
    }

    @Test
    void testGetEmployeeByIdThrowsWhenMissing() {
        when(employeeRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(10))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testUploadNewImageAndDeleteOldOne() {
        Employee employee = employeeWithUser(1, "john");
        employee.setProfileImageId("profile-image/1/old-uuid");
        byte[] content = "test-file".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", content);
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(s3Buckets.employee()).thenReturn("test-bucket");

        employeeService.uploadEmployeeImage(1, file);

        verify(s3Service).putObject(eq("test-bucket"), contains("profile-image/1/"), eq(content));
        verify(employeeRepository).updateProfileImageId(contains("profile-image/1/"), eq(1));
        verify(s3Service).deleteObject("test-bucket", "profile-image/1/old-uuid");
    }

    @Test
    void testUploadRejectsNonImageFile() {
        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", "pdf".getBytes());

        assertThatThrownBy(() -> employeeService.uploadEmployeeImage(1, file))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(s3Service);
    }

    @Test
    void testAdminCannotDeleteOwnAccount() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employeeWithUser(1, "admin")));

        assertThatThrownBy(() -> employeeService.deleteEmployee(1, "admin"))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).delete(any());
    }
}
