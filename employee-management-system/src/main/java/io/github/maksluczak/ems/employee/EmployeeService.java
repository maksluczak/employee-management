package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeRequest;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeResponse;
import io.github.maksluczak.ems.employee.dto.UpdateEmployeeRequest;
import io.github.maksluczak.ems.employee.generator.EmployeeStringGenerator;
import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeStringGenerator stringGenerator;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmployeeStringGenerator stringGenerator,
                           S3Service s3Service,
                           S3Buckets s3Buckets) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.stringGenerator = stringGenerator;
        this.s3Service = s3Service;
        this.s3Buckets = s3Buckets;
    }

    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(employee -> EmployeeResponse.builder()
                        .id(employee.getId())
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .email(employee.getEmail())
                        .position(employee.getPosition())
                        .build())
                .toList();
    }

    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .position(employee.getPosition())
                .build();
    }

    public byte[] getEmployeeProfileImageById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (employee.getProfileImageId() == null || employee.getProfileImageId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee has no profile image");
        }

        String profileImageId = employee.getProfileImageId();
        return s3Service.getObject(
                s3Buckets.getEmployee(),
                profileImageId
        );
    }

    public EmployeeResponse getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Employee employee = user.getEmployee();

        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .position(employee.getPosition())
                .build();
    }

    public byte[] getEmployeeProfileImageByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Employee employee = user.getEmployee();

        if (employee.getProfileImageId() == null || employee.getProfileImageId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee has no profile image");
        }

        String profileImageId = employee.getProfileImageId();
        return s3Service.getObject(
                s3Buckets.getEmployee(),
                profileImageId
        );
    }

    public RegisterEmployeeResponse insertEmployee(RegisterEmployeeRequest request) {
        String username = request.getFirstName().toLowerCase() +
                request.getLastName().toLowerCase() + "_" +
                stringGenerator.generateEmployeeId();
        String email;
        if (request.getRole().equals(Role.ADMIN)) { email = username + "@admin.company.com"; }
        else { email = username + "@employee.company.com"; }
        String password = stringGenerator.generateEmployeeSecurePassword();
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(request.getRole())
                .build();

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .position(request.getPosition())
                .build();

        user.setEmployee(employee);
        employee.setUser(user);

        userRepository.save(user);

        return RegisterEmployeeResponse.builder()
                .username(username)
                .email(email)
                .password(password)
                .build();
    }

    public void uploadEmployeeImage(Integer id, MultipartFile file) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee not found"));

        String previousImageId = employee.getProfileImageId();
        if (previousImageId != null) {
            s3Service.deleteObject(s3Buckets.getEmployee(), previousImageId);
        }

        String profileImageId = UUID.randomUUID().toString();
        String key = "profile-image/%s/%s".formatted(id, profileImageId);
        try {
            s3Service.putObject(
                    s3Buckets.getEmployee(),
                    key,
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        employeeRepository.updateProfileImageId(key, id);
    }

    public void updateEmployee(Integer id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee not found"));

        employee.setPosition(request.getPosition());
        employeeRepository.save(employee);
    }

    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee not found"));

        if (employee.getProfileImageId() != null && !employee.getProfileImageId().isBlank()) {
            s3Service.deleteObject(s3Buckets.getEmployee(), employee.getProfileImageId());
        }
        userRepository.delete(employee.getUser());
    }
}
