package com.lalit.authservice.utils;



import com.lalit.authservice.entity.AuthUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {


    private final String jwtSecret;
    private final long jwtExpirationInMs;
    private final Key key;


    public JwtTokenProvider(
            @Value("${jwt.secret:63832bd0234728374283742837428374283742837428374283742837428374}") String jwtSecret,
            @Value("${jwt.expiration:3600000}") long jwtExpirationInMs) {

        this.jwtSecret = jwtSecret;
        this.jwtExpirationInMs = jwtExpirationInMs;

        // Manual initialization of the key right here
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }



    public String generateAccessToken(AuthUser user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Add custom claims (Role is important for the Student Management System)
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log the error (ExpiredJwtException, MalformedJwtException, etc.)
            return false;
        }
    }


    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}