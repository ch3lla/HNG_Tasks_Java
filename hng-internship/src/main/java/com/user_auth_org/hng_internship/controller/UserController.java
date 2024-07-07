package com.user_auth_org.hng_internship.controller;

import com.user_auth_org.hng_internship.dto.AuthResponseDto;
import com.user_auth_org.hng_internship.dto.LoginDto;
import com.user_auth_org.hng_internship.dto.UserDto;
import com.user_auth_org.hng_internship.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getSingleUserData(@PathVariable int userId) {
        return userService.getSingleUserData(userId);
    }
}
