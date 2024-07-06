package com.user_auth_org.hng_internship;

import com.user_auth_org.hng_internship.security.SecurityConstants;
import com.user_auth_org.hng_internship.security.TokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenGeneratorTest {

    private TokenGenerator tokenGenerator;
    private final String SECRET_KEY = SecurityConstants.JWT_SECRET;

    @BeforeEach
    public void setUp() {
        tokenGenerator = new TokenGenerator();
    }

    @Test
    public void testGenerateToken_ExpiryAndUserDetails() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        String token = tokenGenerator.generateToken(authentication);

        assertNotNull(token);

        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());

        Date expiration = claims.getExpiration();
        Date currentDate = new Date();
        Date expectedExpiration = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPIRATION);

        long tolerance = 5000;
        assertTrue(Math.abs(expectedExpiration.getTime() - expiration.getTime()) < tolerance,
                "Token expiration time is not as expected!");

        assertTrue(expiration.after(new Date()), "Token has expired!");
    }

    @Test
    public void testGetUsernameFromJWT() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 10 minutes
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        String username = tokenGenerator.getUsernameFromJWT(token);

        assertEquals("testuser", username);
    }

    @Test
    public void testValidateToken_ValidToken() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 10 minutes
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();

        boolean isValid = tokenGenerator.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_InvalidToken() {
        String token = "invalidToken";

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            tokenGenerator.validateToken(token);
        }, "Expected validateToken to throw an exception, but it didn't");
    }

    @Test
    public void testTokenExpiration() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        String token = tokenGenerator.generateToken(authentication);

        assertNotNull(token);

        try {
            Thread.sleep(SecurityConstants.JWT_EXPIRATION + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            tokenGenerator.validateToken(token);
        }, "Expected token to be expired, but it was not");
    }
}