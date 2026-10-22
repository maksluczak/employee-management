package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeRequest;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeResponse;
import io.github.maksluczak.ems.employee.dto.UpdateEmployeeRequest;
import io.github.maksluczak.ems.employee.generator.EmployeeStringGenerator;
import io.github.maksluczak.ems.exception.BadRequestException;
import io.github.maksluczak.ems.exception.ResourceNotFoundException;
import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
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

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Integer id) {
        return toResponse(findEmployee(id));
    }

    @Transactional(readOnly = true)
    public byte[] getEmployeeProfileImageById(Integer id) {
        return downloadProfileImage(findEmployee(id));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUsername(String username) {
        return toResponse(findEmployeeByUsername(username));
    }

    @Transactional(readOnly = true)
    public byte[] getEmployeeProfileImageByUsername(String username) {
        return downloadProfileImage(findEmployeeByUsername(username));
    }

    @Transactional
    public RegisterEmployeeResponse insertEmployee(RegisterEmployeeRequest request) {
        String username = normalize(request.getFirstName()) +
                normalize(request.getLastName()) + "_" +
                stringGenerator.generateEmployeeId();
        String email;
        if (request.getRole() == Role.ADMIN) { email = username + "@admin.company.com"; }
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
                .id(employee.getId())
                .username(username)
                .email(email)
                .password(password)
                .build();
    }

    @Transactional
    public void uploadEmployeeImage(Integer id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        Employee employee = findEmployee(id);
        String previousImageId = employee.getProfileImageId();

        String key = "profile-image/%s/%s".formatted(id, UUID.randomUUID());
        try {
            s3Service.putObject(s3Buckets.employee(), key, file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        employeeRepository.updateProfileImageId(key, id);

        if (previousImageId != null && !previousImageId.isBlank()) {
            s3Service.deleteObject(s3Buckets.employee(), previousImageId);
        }
    }

    @Transactional
    public void updateEmployee(Integer id, UpdateEmployeeRequest request) {
        Employee employee = findEmployee(id);
        employee.setPosition(request.getPosition());
    }

    @Transactional
    public void deleteEmployee(Integer id, String currentUsername) {
        Employee employee = findEmployee(id);

        if (employee.getUser().getUsername().equals(currentUsername)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        String imageId = employee.getProfileImageId();
        userRepository.delete(employee.getUser());

        if (imageId != null && !imageId.isBlank()) {
            s3Service.deleteObject(s3Buckets.employee(), imageId);
        }
    }

    private Employee findEmployee(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id %s not found".formatted(id)));
    }

    private Employee findEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getEmployee() == null) {
            throw new ResourceNotFoundException("User has no employee profile");
        }
        return user.getEmployee();
    }

    private byte[] downloadProfileImage(Employee employee) {
        String profileImageId = employee.getProfileImageId();
        if (profileImageId == null || profileImageId.isBlank()) {
            throw new ResourceNotFoundException("Employee has no profile image");
        }
        return s3Service.getObject(s3Buckets.employee(), profileImageId);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .position(employee.getPosition())
                .build();
    }

    static String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value.replace("ł", "l").replace("Ł", "L"), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
