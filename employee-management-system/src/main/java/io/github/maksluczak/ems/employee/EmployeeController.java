package io.github.maksluczak.ems.employee;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> getEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("{id}")
    public Employee getEmployeeById(@PathVariable Integer id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public void addNewEmployee(@RequestBody Employee employee) {
        employeeService.insertEmployee(employee);
    }

    @PutMapping("{id}")
    public void updateEmployee(@PathVariable Integer id, @RequestBody Employee newEmployee) {
        employeeService.updateEmployee(id, newEmployee);
    }

    @DeleteMapping("{id}")
    public void deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
    }
}
