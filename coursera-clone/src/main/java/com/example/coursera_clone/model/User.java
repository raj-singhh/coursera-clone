// src/main/java/com/example/coursera_clone/model/User.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // NEW: Import JsonIgnoreProperties

@Document(collection = "users")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "enrollments"}) // NEW: Ignore Hibernate internal fields and the 'enrollments' collection
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String password;
    // For MongoDB, you should store enrollment IDs or use DBRef if needed.
    // private Set<String> enrollmentIds = new HashSet<>();
}
