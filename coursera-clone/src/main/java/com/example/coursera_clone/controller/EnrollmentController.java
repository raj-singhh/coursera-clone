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

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isEmpty()) {
            logger.warn("Enrollment failed for user {}: Course with ID {} not found.", currentUser.getUsername(), courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: Course not found."));
        }
        Course course = courseOptional.get();

        if (enrollmentRepository.findByUserAndCourse(currentUser, course).isPresent()) {
            logger.warn("Enrollment failed for user {}: Already enrolled in course ID {}.", currentUser.getUsername(), courseId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Error: Already enrolled in this course."));
        }

        Enrollment enrollment = new Enrollment(currentUser, course);
        enrollmentRepository.save(enrollment);

        logger.info("User '{}' successfully enrolled in course '{}' (ID: {}).", currentUser.getUsername(), course.getTitle(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Successfully enrolled in course!"));
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyEnrolledCourses() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        List<Enrollment> enrollments = enrollmentRepository.findByUser(currentUser);
        List<Course> enrolledCourses = enrollments.stream()
                                                .map(Enrollment::getCourse)
                                                .collect(Collectors.toList());

        logger.info("User '{}' retrieved {} enrolled courses.", currentUser.getUsername(), enrolledCourses.size());
        return ResponseEntity.ok(enrolledCourses);
    }

    // NEW: Endpoint to check if a user is enrolled in a specific course
    @GetMapping("/is-enrolled/{courseId}")
    public ResponseEntity<Boolean> isEnrolledInCourse(@PathVariable Long courseId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            // If not authenticated, they cannot be enrolled
            return ResponseEntity.ok(false);
        }

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isEmpty()) {
            // If course doesn't exist, they can't be enrolled in it
            return ResponseEntity.ok(false);
        }
        Course course = courseOptional.get();

        boolean isEnrolled = enrollmentRepository.findByUserAndCourse(currentUser, course).isPresent();
        logger.debug("User '{}' enrollment status for course ID {}: {}", currentUser.getUsername(), courseId, isEnrolled);
        return ResponseEntity.ok(isEnrolled);
    }
}
