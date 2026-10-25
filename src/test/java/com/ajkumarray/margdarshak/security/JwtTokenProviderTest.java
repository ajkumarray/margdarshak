package com.ajkumarray.margdarshak.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.ajkumarray.margdarshak.enums.UserRoleEnums;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class JwtTokenProviderTest {

    static final String SECRET = "unit-test-secret-0123456789abcdef0123456789abcdef";

    private JwtTokenProvider jwtTokenProvider;

    static JwtTokenProvider provider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secretKey", SECRET);
        ReflectionTestUtils.setField(provider, "expiration", 3600L);
        return provider;
    }

    @BeforeEach
    void setUp() {
        jwtTokenProvider = provider();
    }

    @Test
    void issuedTokenIsValidAndCarriesUserCodeAndRole() {
        String token = "Bearer " + jwtTokenProvider.generateToken("MDadmin01", UserRoleEnums.ADMIN);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserCodeFromToken(token)).isEqualTo("MDadmin01");
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(UserRoleEnums.ADMIN);
    }

    @Test
    void tokenWithoutRoleIsInvalid() {
        String token = Jwts.builder().setSubject("MDuser001").setExpiration(inOneHour())
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes()).compact();

        assertThat(jwtTokenProvider.validateToken("Bearer " + token)).isFalse();
    }

    @Test
    void tokenWithUnknownRoleIsInvalid() {
        String token = Jwts.builder().setSubject("MDuser001").claim(JwtTokenProvider.ROLE_CLAIM, "SUPERUSER")
                .setExpiration(inOneHour()).signWith(SignatureAlgorithm.HS256, SECRET.getBytes()).compact();

        assertThat(jwtTokenProvider.validateToken("Bearer " + token)).isFalse();
    }

    @Test
    void tokenWithoutUserCodeIsInvalid() {
        String token = Jwts.builder().claim(JwtTokenProvider.ROLE_CLAIM, "USER").setExpiration(inOneHour())
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes()).compact();

        assertThat(jwtTokenProvider.validateToken("Bearer " + token)).isFalse();
    }

    @Test
    void tokenSignedWithOtherSecretIsInvalid() {
        String token = Jwts.builder().setSubject("MDadmin01").claim(JwtTokenProvider.ROLE_CLAIM, "ADMIN")
                .signWith(SignatureAlgorithm.HS256, "some-other-secret-0123456789abcdef".getBytes()).compact();

        assertThat(jwtTokenProvider.validateToken("Bearer " + token)).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        String token = Jwts.builder().setSubject("MDuser001").claim(JwtTokenProvider.ROLE_CLAIM, "USER")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes()).compact();

        assertThat(jwtTokenProvider.validateToken("Bearer " + token)).isFalse();
    }

    @Test
    void emptyTokenIsInvalid() {
        assertThat(jwtTokenProvider.validateToken("Bearer ")).isFalse();
    }

    private static Date inOneHour() {
        return new Date(System.currentTimeMillis() + 3_600_000);
    }
}
