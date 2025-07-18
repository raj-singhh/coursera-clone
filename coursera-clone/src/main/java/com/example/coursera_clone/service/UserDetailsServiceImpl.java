// src/main/java/com/example/coursera_clone/service/UserDetailsServiceImpl.java
package com.example.coursera_clone.service;

import com.example.coursera_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User; // Spring Security User class

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Explicitly use your model.User class to avoid ambiguity
        com.example.coursera_clone.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Correct way to build Spring Security UserDetails object
        // Use org.springframework.security.core.userdetails.User.builder()
        return User.builder()
                .username(user.getUsername()) // Lombok should generate getUsername()
                .password(user.getPassword()) // Lombok should generate getPassword()
                .roles("USER") // Assign a default role for now
                .build();
    }
}