package com.sadop.energymanagement.security;

import com.sadop.energymanagement.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "sadop-secret-key-sadop-secret-key-sadop-secret-key"; // At least 256-bit
    private final long EXPIRATION_TIME = 86400000; // 1 day in ms

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user) {
        String normalizedRole = user.getRole().name().toUpperCase();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", normalizedRole)
                .claim("fullName", user.getFullName())
                .claim("branchId", user.getBranch() != null ? user.getBranch().getId() : null)   // ✅ CEO safe
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractBranchId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("branchId", Long.class);
    }

    public String extractFullName(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("fullName", String.class);
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class)
                .toUpperCase();
    }

    public boolean isTokenValid(String token, User user) {
        String email = extractEmail(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}
