package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeRequest;
import io.github.maksluczak.ems.employee.dto.UpdateEmployeeRequest;
import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository, PasswordEncoder passwordEncoder,
                           S3Service s3Service,
                           S3Buckets s3Buckets) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        // TODO: check if profile image is empty or null
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

        // TODO: check if profile image is empty or null
        String profileImageId = employee.getProfileImageId();
        return s3Service.getObject(
                s3Buckets.getEmployee(),
                profileImageId
        );
    }

    public void insertEmployee(RegisterEmployeeRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .build();

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .position(request.getPosition())
                .build();

        user.setEmployee(employee);
        employee.setUser(user);

        userRepository.save(user);
    }

    public void uploadEmployeeImage(Integer id, MultipartFile file) {
        employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee not found"));

        String profileImageId = UUID.randomUUID().toString();
        try {
            s3Service.putObject(
                    s3Buckets.getEmployee(),
                    "profile-image/%s/%s".formatted(id, profileImageId),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateEmployee(Integer id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Employee not found"));

        employee.setPosition(request.getPosition());
        employeeRepository.save(employee);
    }

    public void deleteEmployee(Integer id) {
        employeeRepository.deleteById(id);
    }
}
