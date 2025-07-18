// src/main/java/com/example/coursera_clone/dto/LoginRequest.java
package com.example.coursera_clone.dto;

import lombok.Data;

@Data // Lombok to generate getters, setters, equals, hashCode, toString
public class LoginRequest {
    private String username;
    private String password;
}
