// src/main/java/com/example/coursera_clone/controller/CertificateController.java
package com.example.coursera_clone.controller;

import com.example.coursera_clone.service.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    @Autowired
    private CertificateService certificateService;

    @GetMapping("/download/{courseId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Ensure this matches your user roles
    public ResponseEntity<InputStreamResource> downloadCertificate(@PathVariable Long courseId) {
        logger.info("DEBUG (CertificateController): Received request to download certificate for course ID: {}", courseId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
            logger.info("DEBUG (CertificateController): Authenticated user for certificate download: {}", username);
        } else {
            logger.warn("DEBUG (CertificateController): No authenticated user found for certificate download. This should not happen if security is configured correctly.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Should be caught by AuthTokenFilter first
        }

        try {
            logger.info("DEBUG (CertificateController): Calling CertificateService to generate certificate for user {} and course {}", username, courseId);
            ByteArrayInputStream bis = certificateService.generateCertificate(username, courseId);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=certificate_course_" + courseId + "_" + username + ".pdf"); // Dynamic filename
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            logger.info("DEBUG (CertificateController): Successfully generated certificate for user {} and course {}. Returning PDF.", username, courseId);
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));

        } catch (IllegalStateException e) {
            // This exception is thrown by CertificateService if the course is not completed
            logger.warn("WARN (CertificateController): Certificate generation failed for user {} and course {}: {}", username, courseId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // 403 Forbidden
        } catch (RuntimeException e) {
            // Catch other runtime exceptions (e.g., user/course not found, PDF generation error)
            logger.error("ERROR (CertificateController): Runtime error during certificate generation for user {} and course {}: {}", username, courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or 404 Not Found, or 500 Internal Server Error
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            logger.error("ERROR (CertificateController): Unexpected error during certificate generation for user {} and course {}: {}", username, courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
