// src/main/java/com/example/coursera_clone/repository/EnrollmentRepository.java
package com.example.coursera_clone.repository;

import com.example.coursera_clone.model.Enrollment;
import com.example.coursera_clone.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    // Custom method to find all enrollments by a specific user
    List<Enrollment> findByUserId(String userId);

    // Custom method to check if a user is already enrolled in a specific course
    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);
}
