package com.ajkumarray.margdarshak.security;

import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String userCode) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expiration);

        return Jwts.builder().setSubject(userCode).setIssuedAt(now).setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes()).compact();
    }

    public String getUserCodeFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token.replace("Bearer ", "")).getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token.replace("Bearer ", ""));
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
