package org.authservice.controller;

import org.authservice.entity.User;
import org.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        try {
            String result = authService.register(user);
            logger.info("User registration successful for email: {}", user.getEmail());
            return result;
        } catch (Exception e) {
            logger.error("User registration failed for email: {}. Reason: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            String token = authService.login(payload.get("email"), payload.get("password"));
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}. Reason: {}", payload.get("email"), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/validate-token")
    public boolean validate(@RequestParam("token") String token) {
        return authService.validateToken(token);
    }
}