package com.user_auth_org.hng_internship;

import com.user_auth_org.hng_internship.repository.UserRepository;
import com.user_auth_org.hng_internship.security.SecurityConstants;
import com.user_auth_org.hng_internship.security.TokenGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenGeneratorTest {

    private TokenGenerator tokenGenerator;
    private final String SECRET_KEY = SecurityConstants.JWT_SECRET; // Replace with your actual secret key

    @BeforeEach
    public void setUp() {
        tokenGenerator = new TokenGenerator();
    }

    @Test
    public void testGenerateToken_ExpiryAndUserDetails() {
        // Mocking the authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");

        // Generate the token
        String token = tokenGenerator.generateToken(authentication);

        // Assert token is not null
        assertNotNull(token);

        // Parsing the token to verify its claims
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        // Check if the subject (username) is correctly set
        assertEquals("testuser", claims.getSubject());

        // Check if the expiration date is set correctly
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        // Calculate expected expiration date
        Date currentDate = new Date();
        Date expectedExpiration = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPIRATION);

        // Allow for slight variations in time (e.g., a few seconds difference)
        long tolerance = 5000; // 5 seconds
        assertTrue(Math.abs(expectedExpiration.getTime() - expiration.getTime()) < tolerance,
                "Token expiration time is not as expected!");

        // Ensure the token is not expired
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

        // Generate the token
        String token = tokenGenerator.generateToken(authentication);

        // Assert token is not null
        assertNotNull(token);

        // Wait for the token to expire (assuming very short expiration time for the test)
        try {
            Thread.sleep(SecurityConstants.JWT_EXPIRATION + 1000); // Add 1 second delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Validate the token should now be expired
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            tokenGenerator.validateToken(token);
        }, "Expected token to be expired, but it was not");
    }
}