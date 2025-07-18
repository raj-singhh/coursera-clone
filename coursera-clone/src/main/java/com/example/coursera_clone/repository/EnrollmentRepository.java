// src/main/java/com/example/coursera_clone/repository/EnrollmentRepository.java
package com.example.coursera_clone.repository;

import com.example.coursera_clone.model.Enrollment;
import com.example.coursera_clone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    // Custom method to find all enrollments by a specific user
    List<Enrollment> findByUser(User user);

    // Custom method to check if a user is already enrolled in a specific course
    Optional<Enrollment> findByUserAndCourse(User user, com.example.coursera_clone.model.Course course);
}
