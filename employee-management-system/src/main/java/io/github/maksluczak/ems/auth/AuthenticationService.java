package io.github.maksluczak.ems.auth;

import io.github.maksluczak.ems.config.JwtService;
import io.github.maksluczak.ems.employee.Employee;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
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

    public AuthenticationResponse register(RegisterRequest request) {
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

        repository.save(user);

        var jwtToken = service.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
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

        var jwtToken = service.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
