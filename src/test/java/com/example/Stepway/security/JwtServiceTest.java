package com.example.Stepway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test void roundTripsSignedTokenAndRejectsWrongKey() {
        JwtService service = new JwtService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
        org.springframework.security.core.userdetails.UserDetails user =
                User.withUsername("demo@example.com").password("unused").roles("ADMIN").build();
        String token = service.generateToken(user);
        assertTrue(service.validateToken(token, user));
        JwtService other = new JwtService("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=");
        assertThrows(io.jsonwebtoken.JwtException.class, () -> other.extractUsername(token));
    }

    @Test void refusesWeakSigningKey() {
        assertThrows(io.jsonwebtoken.security.WeakKeyException.class, () -> new JwtService("YWJj"));
    }
}
