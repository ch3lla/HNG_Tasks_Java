package com.user_auth_org.hng_internship;

import com.user_auth_org.hng_internship.controller.UserController;
import com.user_auth_org.hng_internship.dto.LoginDto;
import com.user_auth_org.hng_internship.dto.UserDto;
import com.user_auth_org.hng_internship.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDto userDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setEmail("john@example.com");
        userDto.setPassword("password123");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPhone("1234567890");

        loginDto = new LoginDto();
        loginDto.setEmail("john@example.com");
        loginDto.setPassword("password123");
    }

    @Test
    public void shouldRegisterUserSuccessfully() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", "mocked_token");
        data.put("user", userDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Registration successful");
        response.put("data", data);

        ResponseEntity<?> responseEntity = new ResponseEntity<>(response, HttpStatus.CREATED);

        when(userService.registerUser(ArgumentMatchers.any(UserDto.class)))
                .thenReturn((ResponseEntity) responseEntity);

        String userJson = "{\"email\":\"john@example.com\",\"password\":\"password123\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.user.email").value("john@example.com"));
    }

    @Test
    public void shouldFailIfRequiredFieldsAreMissing() throws Exception {
        String userJson = "{\"email\":\"john@example.com\",\"password\":\"password123\",\"firstName\":\"\",\"lastName\":\"\",\"phone\":\"\"}";

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Bad request");
        response.put("message", "Required fields are missing!");
        response.put("statusCode", 422);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);

        when(userService.registerUser(ArgumentMatchers.any(UserDto.class)))
                .thenReturn((ResponseEntity) responseEntity);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.firstName").value("First name is mandatory"))
                .andExpect(jsonPath("$.lastName").value("Last name is mandatory"));
    }

    @Test
    public void shouldFailIfDuplicateEmail() throws Exception {
        String userJson = "{\"email\":\"john@example.com\",\"password\":\"password123\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"}";

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Bad request");
        response.put("message", "Email is taken!");
        response.put("statusCode", 400);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        when(userService.registerUser(ArgumentMatchers.any(UserDto.class)))
                .thenReturn((ResponseEntity) responseEntity);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("Bad request"))
                .andExpect(jsonPath("$.message").value("Email is taken!"));
    }

    @Test
    public void shouldLoginUserSuccessfully() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", "mocked_token");
        data.put("user", userDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("data", data);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

        when(userService.loginUser(ArgumentMatchers.any(LoginDto.class)))
                .thenReturn((ResponseEntity) responseEntity);

        String loginJson = "{\"email\":\"john@example.com\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.user.email").value("john@example.com"));
    }

    @Test
    public void shouldFailLoginWithInvalidCredentials() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Bad Request");
        response.put("message", "Authentication failed");
        response.put("statusCode", 401);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        when(userService.loginUser(ArgumentMatchers.any(LoginDto.class)))
                .thenReturn((ResponseEntity) responseEntity);


        String loginJson = "{\"email\":\"john@example.com\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Authentication failed"));
    }
}
