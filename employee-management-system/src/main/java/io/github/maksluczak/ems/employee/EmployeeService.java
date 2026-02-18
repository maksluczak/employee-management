package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeRequest;
import io.github.maksluczak.ems.employee.dto.UpdateEmployeeRequest;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                        .profileImageUrl(employee.getProfileImageUrl())
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
                .profileImageUrl(employee.getProfileImageUrl())
                .build();
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
                .profileImageUrl(employee.getProfileImageUrl())
                .build();
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
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        user.setEmployee(employee);
        employee.setUser(user);

        userRepository.save(user);
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
