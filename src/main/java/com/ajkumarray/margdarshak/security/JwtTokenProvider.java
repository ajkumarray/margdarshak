package com.ajkumarray.margdarshak.security;

import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.enums.UserRoleEnums;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Access and refresh tokens use different secrets and a typ claim, so neither
 * is accepted in place of the other.
 */
@Component
public class JwtTokenProvider {

    public static final String ROLE_CLAIM = "role";
    public static final String TYPE_CLAIM = "typ";
    public static final String ACCESS_TYPE = "access";
    public static final String REFRESH_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-secret}")
    private String refreshSecretKey;

    @Value("${jwt.refresh-expiration:604800}")
    private long refreshExpiration;

    public String generateToken(String userCode, UserRoleEnums role) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expiration * 1000);

        return Jwts.builder().setSubject(userCode).claim(TYPE_CLAIM, ACCESS_TYPE).claim(ROLE_CLAIM, role.name())
                .setIssuedAt(now).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes()).compact();
    }

    public String getUserCodeFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        return getAccessClaims(token) != null;
    }

    /**
     * @return the claims of a valid access token (signed by us, not expired,
     *         typ=access, a userCode and a known role), otherwise null
     */
    public Claims getAccessClaims(String token) {
        try {
            Claims claims = parseClaims(token);
            UserRoleEnums.valueOf(claims.get(ROLE_CLAIM, String.class));
            boolean complete = ACCESS_TYPE.equals(claims.get(TYPE_CLAIM, String.class)) && hasText(claims.getSubject());
            return complete ? claims : null;
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    public String generateRefreshToken(String userCode, String jti, Date expiresAt) {
        return Jwts.builder().setSubject(userCode).claim(TYPE_CLAIM, REFRESH_TYPE).setId(jti).setIssuedAt(new Date())
                .setExpiration(expiresAt).signWith(SignatureAlgorithm.HS256, refreshSecretKey.getBytes()).compact();
    }

    /**
     * @return the claims of a valid refresh token (signed with the refresh
     *         secret, not expired, typ=refresh, a userCode and a jti), otherwise
     *         null; whether its session is still active is checked in the DB
     */
    public Claims parseRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(refreshSecretKey.getBytes()).parseClaimsJws(token).getBody();
            boolean complete = REFRESH_TYPE.equals(claims.get(TYPE_CLAIM, String.class))
                    && hasText(claims.getSubject()) && hasText(claims.getId());
            return complete ? claims : null;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
