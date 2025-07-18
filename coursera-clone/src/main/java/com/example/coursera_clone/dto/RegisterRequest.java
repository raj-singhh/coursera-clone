// src/main/java/com/example/coursera_clone/dto/RegisterRequest.java
package com.example.coursera_clone.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
}
