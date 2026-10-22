package io.github.maksluczak.ems.config;

import io.github.maksluczak.ems.user.Role;
import io.github.maksluczak.ems.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtServiceTest {

    private static final String SECRET =
            "665da784b13a00f11318bc79853e6f282f113965233e66d9a3cab8da410b36e75245fb9af26ebafe130fbeeafba80e9eb462696f501498042e6115a4ef8a4532";

    private final User user = User.builder()
            .username("john")
            .role(Role.EMPLOYEE)
            .build();

    private JwtService serviceWith(Duration expiration, Duration refreshWindow) {
        return new JwtService(new JwtProperties(SECRET, expiration, refreshWindow));
    }

    @Test
    void testGeneratedTokenShouldContainsUsernameAndIsValid() {
        JwtService jwtService = serviceWith(Duration.ofHours(1), Duration.ofDays(7));

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void testTokenIsNotValidForAnotherUser() {
        JwtService jwtService = serviceWith(Duration.ofHours(1), Duration.ofDays(7));
        User otherUser = User.builder().username("jane").build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void testExpiredTokenThrowsOnParsing() {
        JwtService jwtService = serviceWith(Duration.ofHours(-1), Duration.ofDays(7));

        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void testExpiredTokenCanBeRefreshedInsideRefreshWindow() {
        JwtService jwtService = serviceWith(Duration.ofHours(-1), Duration.ofDays(7));

        String token = jwtService.generateToken(user);
        String extractedUsername = jwtService.extractUsernameForRefresh(token);

        assertThat(extractedUsername).isEqualTo(user.getUsername());
    }

    @Test
    void testTokenOlderThanRefreshWindowCannotBeRefreshed() {
        JwtService jwtService = serviceWith(Duration.ofDays(-8), Duration.ofDays(7));

        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.extractUsernameForRefresh(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void testTokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = "a".repeat(86) + "==";
        JwtService otherService = new JwtService(new JwtProperties(otherSecret, Duration.ofHours(1), Duration.ofDays(7)));
        JwtService jwtService = serviceWith(Duration.ofHours(1), Duration.ofDays(7));

        String foreignToken = otherService.generateToken(user);

        assertThatThrownBy(() -> jwtService.extractUsername(foreignToken))
                .isInstanceOf(JwtException.class);
    }
}
