package com.example.Stepway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;


//    we have made this classs for:
//   generating tokens
//   validate
//   isExp
//   util class for jwt

@Service
public class JwtService {
    private final java.security.Key signingKey;

    public JwtService(@org.springframework.beans.factory.annotation.Value("${app.jwt.secret}") String secret) {
        this.signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                io.jsonwebtoken.io.Decoders.BASE64.decode(secret));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    }
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Extract roles and permissions into separate lists
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();
        List<Long> courses = new ArrayList<>();

        userDetails.getAuthorities().forEach(authority -> {
            String authorityName = authority.getAuthority();
            if (authorityName.startsWith("ROLE_")) {
                roles.add(authorityName.substring(5));
            } else {
                permissions.add(authorityName);
            }
        });
        claims.put("ROLES", roles);
        claims.put("PERMISSIONS", permissions);
        return createToken(claims, userDetails.getUsername());
    }
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+10L * 60 * 60 * 1000))
                .signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }


    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
