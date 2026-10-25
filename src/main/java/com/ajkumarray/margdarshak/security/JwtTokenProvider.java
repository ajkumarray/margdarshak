package com.ajkumarray.margdarshak.security;

import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.enums.UserRoleEnums;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {

    public static final String ROLE_CLAIM = "role";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String userCode, UserRoleEnums role) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expiration * 1000);

        return Jwts.builder().setSubject(userCode).claim(ROLE_CLAIM, role.name()).setIssuedAt(now)
                .setExpiration(expiryDate).signWith(SignatureAlgorithm.HS256, secretKey.getBytes()).compact();
    }

    public String getUserCodeFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public UserRoleEnums getRoleFromToken(String token) {
        return UserRoleEnums.valueOf(parseClaims(token).get(ROLE_CLAIM, String.class));
    }

    /**
     * A token is valid only if we signed it, it hasn't expired, and it carries
     * everything we issue: subject (userCode) and a known role.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            UserRoleEnums.valueOf(claims.get(ROLE_CLAIM, String.class));
            return claims.getSubject() != null && !claims.getSubject().isBlank();
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }

}
