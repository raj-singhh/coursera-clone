// src/main/java/com/example/coursera_clone/controller/AuthController.java
package com.example.coursera_clone.controller;

import com.example.coursera_clone.dto.JwtResponse;
import com.example.coursera_clone.dto.LoginRequest;
import com.example.coursera_clone.dto.RegisterRequest;
import com.example.coursera_clone.dto.MessageResponse;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.repository.UserRepository;
import com.example.coursera_clone.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // Import BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Fetch the user from the repository to get their ID and email
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() -> new RuntimeException("User not found after authentication."));

            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getEmail()));
        } catch (UsernameNotFoundException e) {
            logger.error("Login failed for user {}: User not found.", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not found."));
        } catch (BadCredentialsException e) {
            logger.error("Login failed for user {}: Invalid credentials.", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: Invalid username or password."));
        } catch (Exception e) {
            logger.error("An unexpected error occurred during login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: An unexpected error occurred during login."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        logger.info("Attempting registration for username: {}, email: {}", registerRequest.getUsername(), registerRequest.getEmail());

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            logger.warn("Registration failed: Username '{}' is already taken.", registerRequest.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email '{}' is already in use.", registerRequest.getEmail());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));

        userRepository.save(user);
        logger.info("User '{}' registered successfully!", user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully!"));
    }
}
