package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " does not found"));
    }

    public Employee getEmployeeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getEmployee();
    }

    public void updateEmployee(Integer id, Employee newEmployee) {
        employeeRepository.findById(id)
                .map(employee -> {
                    employee.setPosition(newEmployee.getPosition());
                    return employeeRepository.save(employee);
                })
                .orElseGet(() -> {
                    return employeeRepository.save(newEmployee);
                });
    }

    public void deleteEmployee(Integer id) {
        employeeRepository.deleteById(id);
    }
}
