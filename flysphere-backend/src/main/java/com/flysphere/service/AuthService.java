package com.flysphere.service;

import com.flysphere.entity.User;
import com.flysphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return user;
    }

    // ✅ Return user by email (used by /profile endpoint)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // Temporary hash generator (will remove after use)
    @PostConstruct
    public void generateTempHash() {
        String rawPassword = "Temp@123";
        String hash = passwordEncoder.encode(rawPassword);
        System.out.println("=================================");
        System.out.println("BCrypt Hash for Temp@123:");
        System.out.println(hash);
        System.out.println("=================================");
    }
}
