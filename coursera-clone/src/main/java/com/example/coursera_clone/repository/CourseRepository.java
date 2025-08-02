// src/main/java/com/example/coursera_clone/repository/CourseRepository.java
// This interface provides methods for interacting with the 'courses' table.
package com.example.coursera_clone.repository; // Corrected package name

import com.example.coursera_clone.model.Course; // Corrected import
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    // You can add custom query methods here if needed later
}
