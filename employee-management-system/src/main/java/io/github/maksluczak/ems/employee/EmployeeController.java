package io.github.maksluczak.ems.employee;

import io.github.maksluczak.ems.employee.dto.EmployeeResponse;
import io.github.maksluczak.ems.employee.dto.RegisterEmployeeRequest;
import io.github.maksluczak.ems.employee.dto.UpdateEmployeeRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("{id}/profile-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getEmployeeProfileImage(@PathVariable Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeProfileImageById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public ResponseEntity<EmployeeResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(employeeService.getEmployeeByUsername(username));
    }

    @GetMapping("/me/profile-image")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<byte[]> getMyProfileImage(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(employeeService.getEmployeeProfileImageByUsername(username));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createEmployee(@Valid @RequestBody RegisterEmployeeRequest request) {
        employeeService.insertEmployee(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(
            value = "{id}/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> uploadProfilePicture(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        employeeService.uploadEmployeeImage(id, file);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateEmployee(@PathVariable Integer id, @Valid @RequestBody UpdateEmployeeRequest request) {
        employeeService.updateEmployee(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }
}
