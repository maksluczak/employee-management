package io.github.maksluczak.ems.auth;

import io.github.maksluczak.ems.auth.dto.AuthenticationRequest;
import io.github.maksluczak.ems.auth.dto.AuthenticationResponse;
import io.github.maksluczak.ems.config.JwtService;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final JwtService service;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, JwtService service, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.service = service;
        this.authenticationManager = authenticationManager;
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
                .role(user.getRole())
                .build();
    }

    public AuthenticationResponse refresh(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Missing or invalid Authorization header");
        }

        String jwtToken = authHeader.substring(7);

        String username = service.extractUsername(jwtToken);
        User user = repository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found"));

        String newToken = service.generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .role(user.getRole())
                .build();
    }
}
