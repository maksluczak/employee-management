package io.github.maksluczak.ems.auth;

import io.github.maksluczak.ems.auth.dto.AuthenticationRequest;
import io.github.maksluczak.ems.auth.dto.AuthenticationResponse;
import io.github.maksluczak.ems.auth.dto.RegisterRequest;
import io.github.maksluczak.ems.config.JwtService;
import io.github.maksluczak.ems.employee.Employee;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService service;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService service, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.service = service;
        this.authenticationManager = authenticationManager;
    }

    public void registerAdmin(RegisterRequest request) {
        if(repository.existsByRole(Role.ADMIN)) {
            throw new IllegalStateException("Admin already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .position(request.getPosition())
                .build();

        user.setEmployee(employee);
        employee.setUser(user);

        repository.save(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
        );

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();

        String jwtToken = service.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse refresh(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String jwtToken = authHeader.substring(7);

        String username = service.extractUsername(jwtToken);
        User user = repository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found"));

        String newToken = service.generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .build();
    }
}
