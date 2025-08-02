// src/main/java/com/example/coursera_clone/repository/UserRepository.java
// This interface provides methods for interacting with the 'users' table.
package com.example.coursera_clone.repository; // Corrected package name

import com.example.coursera_clone.model.User; // Corrected import
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // We'll need these methods for authentication later
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
