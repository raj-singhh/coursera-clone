// src/main/java/com/example/coursera_clone/dto/JwtResponse.java
package com.example.coursera_clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // ADDED: Add NoArgsConstructor for deserialization

@Data
@AllArgsConstructor // Lombok to generate a constructor with all fields
@NoArgsConstructor // ADDED: Required for JSON deserialization (e.g., by Jackson)
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    // In a real app, you might include roles here
}
