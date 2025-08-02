
// ...existing code...
// src/main/java/com/example/coursera_clone/controller/EnrollmentController.java
package com.example.coursera_clone.controller;

import com.example.coursera_clone.dto.MessageResponse;
import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.Enrollment;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.EnrollmentRepository;
import com.example.coursera_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database!"));
    }

    // Endpoint to check if a user is enrolled in a specific course (for frontend)
    @GetMapping("/is-enrolled/{courseId}")
    public ResponseEntity<Boolean> isEnrolledInCourse(@PathVariable String courseId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }
        boolean enrolled = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), courseId).isPresent();
        return ResponseEntity.ok(enrolled);
    }



    @GetMapping("/my-courses")
    public ResponseEntity<List<Course>> getMyCourses() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Enrollment> enrollments = enrollmentRepository.findByUserId(currentUser.getId());
        List<Course> courses = enrollments.stream()
                                        .map(enrollment -> courseRepository.findById(enrollment.getCourseId()).orElse(null))
                                        .filter(course -> course != null)
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(courses);
    }

    // NEW: Endpoint to check if a user is enrolled in a specific course
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollInCourse(@PathVariable String courseId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isEmpty()) {
            logger.warn("Enrollment failed for user {}: Course with ID {} not found.", currentUser.getUsername(), courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: Course not found."));
        }

        // Check if already enrolled
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), courseId);
        if (existingEnrollment.isPresent()) {
            logger.info("User {} is already enrolled in course {}.", currentUser.getUsername(), courseId);
            return ResponseEntity.ok(new MessageResponse("Already enrolled in this course."));
        }

        Enrollment enrollment = new Enrollment(currentUser.getId(), courseId);
        enrollmentRepository.save(enrollment);
        logger.info("User {} enrolled in course {} successfully.", currentUser.getUsername(), courseId);
        return ResponseEntity.ok(new MessageResponse("Enrolled in course successfully!"));
    }
}
