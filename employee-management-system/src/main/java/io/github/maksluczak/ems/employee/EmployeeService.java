package io.github.maksluczak.ems.employee;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(id + " does not found"));
    }

    public void insertEmployee(Employee employee) {
        employeeRepository.save(employee);
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
