package io.github.maksluczak.ems.config;

import io.github.maksluczak.ems.employee.Employee;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.username}") String adminUsername,
                            @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        User adminUser = User.builder()
                .username(adminUsername)
                .email("admin@company.com")
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        Employee adminEmployee = Employee.builder()
                .firstName("System")
                .lastName("Admin")
                .email("admin@company.com")
                .position("Admin")
                .user(adminUser)
                .build();

        adminUser.setEmployee(adminEmployee);
        userRepository.save(adminUser);
        log.info("Created first administrator account: {}", adminUsername);
    }
}
