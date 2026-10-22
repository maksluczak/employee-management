package io.github.maksluczak.ems.auth;

import io.github.maksluczak.ems.auth.dto.AuthenticationRequest;
import io.github.maksluczak.ems.auth.dto.AuthenticationResponse;
import io.github.maksluczak.ems.config.JwtService;
import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.github.maksluczak.ems.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;

    private final User user = User.builder()
            .username("john")
            .password("encoded")
            .role(Role.EMPLOYEE)
            .build();

    @Test
    void testAuthenticateReturnsTokenAndRole() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(
                new AuthenticationRequest("john", "password123"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("john", "password123"));
    }

    @Test
    void authenticateWithWrongPasswordThrows() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticate(
                new AuthenticationRequest("john", "wrongpassword")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtService);
    }
}
