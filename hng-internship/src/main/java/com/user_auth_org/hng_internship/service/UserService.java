package com.user_auth_org.hng_internship.service;

import com.user_auth_org.hng_internship.dto.AuthResponseDto;
import com.user_auth_org.hng_internship.dto.LoginDto;
import com.user_auth_org.hng_internship.dto.UserDto;
import com.user_auth_org.hng_internship.model.Organization;
import com.user_auth_org.hng_internship.model.User;
import com.user_auth_org.hng_internship.repository.OrganizationRepository;
import com.user_auth_org.hng_internship.repository.UserRepository;
import com.user_auth_org.hng_internship.security.TokenGenerator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Transactional
    public ResponseEntity<?> registerUser(UserDto userDto) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.existsByEmail(userDto.getEmail())) {
            response.put("status", "Bad request");
            response.put("message", "Email is taken!");
            response.put("statusCode", 400);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstname(userDto.getFirstName());
        user.setLastname(userDto.getLastName());
        user.setPhone(userDto.getPhone());

        user = userRepository.save(user);

        Organization organization = new Organization(user.getFirstname());
        organization.addUser(user);
        organization = organizationRepository.save(organization);

        user.addOrganization(organization);
        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
        );

        String token = tokenGenerator.generateToken(authentication);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("firstName", user.getFirstname());
        userData.put("lastName", user.getLastname());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", token);
        data.put("user", userData);

        response.put("status", "success");
        response.put("message", "Registration successful");
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public ResponseEntity<?> loginUser(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenGenerator.generateToken(authentication);

            User userDetails = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userDetails.getUserId());
            userData.put("firstName", userDetails.getFirstname());
            userData.put("lastName", userDetails.getLastname());
            userData.put("email", userDetails.getEmail());
            userData.put("phone", userDetails.getPhone());

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", token);
            data.put("user", userData);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("data", data);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Bad Request");
            response.put("message", "Authentication failed");
            response.put("statusCode", 401);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<?> getSingleUserData(int userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUsername = authentication.getName();

            // Retrieve user details by userId from the database
            User userDetails = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Check if the authenticated user has permission to access this userId
            if (!userDetails.getEmail().equals(authenticatedUsername)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Unauthorized access");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }

            // Construct the success response
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userDetails.getUserId());
            userData.put("firstName", userDetails.getFirstname());
            userData.put("lastName", userDetails.getLastname());
            userData.put("email", userDetails.getEmail());
            userData.put("phone", userDetails.getPhone());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User details retrieved successfully");
            response.put("data", userData);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An unexpected error occurred");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
