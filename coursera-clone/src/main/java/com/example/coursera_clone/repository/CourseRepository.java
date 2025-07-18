// src/main/java/com/example/coursera_clone/repository/CourseRepository.java
// This interface provides methods for interacting with the 'courses' table.
package com.example.coursera_clone.repository; // Corrected package name

import com.example.coursera_clone.model.Course; // Corrected import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // You can add custom query methods here if needed later
}
