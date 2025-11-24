package org.authservice.service;


import org.authservice.entity.User;

public interface AuthService {
    String register(User user);
    String login(String email, String rawPassword);
    boolean validateToken(String token);
}
