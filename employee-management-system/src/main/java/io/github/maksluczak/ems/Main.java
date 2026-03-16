package io.github.maksluczak.ems;

import io.github.maksluczak.ems.employee.Employee;
import io.github.maksluczak.ems.s3.S3Buckets;
import io.github.maksluczak.ems.s3.S3Service;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             S3Service s3Service,
                             S3Buckets s3Buckets) {
        return args -> {
            createRandomAdminAndEmployee(userRepository, passwordEncoder);
            // testBucketUploadAndDownload(s3Service, s3Buckets);
        };
    }

    private static void createRandomAdminAndEmployee(UserRepository userRepository,
                                                     PasswordEncoder passwordEncoder) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@company.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            Employee adminEmployee = Employee.builder()
                    .firstName("Adam")
                    .lastName("Nowak")
                    .email("admin@company.com")
                    .position("Chief Administrator")
                    .profileImageUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Admin")
                    .user(adminUser)
                    .build();

            adminUser.setEmployee(adminEmployee);
            userRepository.save(adminUser);
            System.out.println("Dodano konto administratora.");
        }

        String randomUsername = "user" + System.currentTimeMillis() % 1000;
        User employeeUser = User.builder()
                .username(randomUsername)
                .email(randomUsername + "@company.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.EMPLOYEE)
                .build();

        Employee employeeDetails = Employee.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email(randomUsername + "@company.com")
                .position("Software Engineer")
                .profileImageUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + randomUsername)
                .user(employeeUser)
                .build();

        employeeUser.setEmployee(employeeDetails);
        userRepository.save(employeeUser);
        System.out.println("Dodano losowego pracownika: " + randomUsername);
    }

    private static void testBucketUploadAndDownload(S3Service s3Service,
                                                    S3Buckets s3Buckets) {
        s3Service.putObject(
                s3Buckets.getEmployee(),
                "foo-2/test",
                "Hello! Welcome to my app.".getBytes()
        );

        byte[] bytes = s3Service.getObject(
                s3Buckets.getEmployee(),
                "foo-2/test"
        );
        System.out.println("Hooray " + new String(bytes));
    }
}
