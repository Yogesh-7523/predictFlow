package org.authservice;

import org.authservice.entity.User;
import org.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner initUsers(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                if (userRepo.findByEmail("test@example.com").isEmpty()) {
                    User u = new User();
                    u.setEmail("test@example.com");
                    u.setName("Test User");
                    u.setRole("USER");
                    u.setPassword(passwordEncoder.encode("secret"));
                    userRepo.save(u);
                }
                if (userRepo.findByEmail("user2@example.com").isEmpty()) {
                    User u2 = new User();
                    u2.setEmail("user2@example.com");
                    u2.setName("User 2");
                    u2.setRole("USER");
                    u2.setPassword(passwordEncoder.encode("secret2"));
                    userRepo.save(u2);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not seed users: " + e.getMessage());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
