// src/main/java/com/example/coursera_clone/controller/ProgressController.java
package com.example.coursera_clone.controller;

import com.example.coursera_clone.dto.MessageResponse;
import com.example.coursera_clone.model.Progress;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.service.ProgressService;
// CORRECTED IMPORT: Assuming UserDetailsImpl is in the 'service' package
// If UserDetailsImpl is actually UserDetailsServiceImpl, you might need to adjust logic slightly
import com.example.coursera_clone.service.UserDetailsServiceImpl; // Assuming UserDetailsImpl is your UserDetailsServiceImpl
import com.example.coursera_clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    private static final Logger logger = LoggerFactory.getLogger(ProgressController.class);

    @Autowired
    private ProgressService progressService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        // Use UserDetails from Spring Security directly, then find User from repository
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database!"));
    }

    static class ProgressUpdateRequest {
        public Double watchedPercentage;
        public Boolean isCompleted;
    }

    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateLessonProgress(@PathVariable String lessonId, @RequestBody ProgressUpdateRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        logger.info("DEBUG (ProgressController): Received progress update request for user '{}' (ID: {}) lesson {}: {}% completed: {}",
                currentUser.getUsername(), currentUser.getId(), lessonId, request.watchedPercentage, request.isCompleted);

        try {
            progressService.updateLessonProgress(currentUser.getId(), lessonId, request.watchedPercentage, request.isCompleted);
            return ResponseEntity.ok(new MessageResponse("Progress updated successfully!"));
        } catch (RuntimeException e) {
            logger.error("ERROR (ProgressController): Failed to update progress for user {} lesson {}: {}", currentUser.getId(), lessonId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}/user-progress")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProgressForCourse(@PathVariable String courseId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        logger.info("DEBUG (ProgressController): User '{}' (ID: {}) requesting progress for course ID {}", currentUser.getUsername(), currentUser.getId(), courseId);

        try {
            Map<String, Progress> progressMap = progressService.getUserProgressForCourse(currentUser.getId(), courseId);
            logger.info("DEBUG (ProgressController): User '{}' retrieved progress for {} lessons in course ID {}.", currentUser.getUsername(), progressMap.size(), courseId);
            return ResponseEntity.ok(progressMap);
        } catch (RuntimeException e) {
            logger.error("ERROR (ProgressController): Failed to retrieve user progress for user {} course {}: {}", currentUser.getId(), courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/course/{courseId}/completion-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCourseCompletionStatus(@PathVariable String courseId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        logger.info("DEBUG (ProgressController): User '{}' (ID: {}) requesting completion status for course ID {}", currentUser.getUsername(), currentUser.getId(), courseId);

        try {
            boolean isCompleted = progressService.getCourseCompletionStatus(currentUser.getId(), courseId);
            logger.info("DEBUG (ProgressController): User '{}' course ID {} completion status: {}.", currentUser.getUsername(), courseId, isCompleted);
            return ResponseEntity.ok(isCompleted);
        } catch (RuntimeException e) {
            logger.error("ERROR (ProgressController): Failed to retrieve completion status for user {} course {}: {}", currentUser.getId(), courseId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
