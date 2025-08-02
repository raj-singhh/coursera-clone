// src/main/java/com/example/coursera_clone/service/CertificateService.java
package com.example.coursera_clone.service;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.ProgressRepository;
import com.example.coursera_clone.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.ColorConstants; // NEW: For link color
import com.itextpdf.layout.element.Link; // NEW: For creating clickable links
import com.itextpdf.kernel.pdf.action.PdfAction; // NEW: For link actions

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // NEW: Import for @Value
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private ProgressService progressService;

    @Value("${app.frontend.base-url}") // NEW: Inject the frontend base URL from application.properties
    private String frontendBaseUrl;

    public ByteArrayInputStream generateCertificate(String username, String courseId) {
        logger.info("DEBUG (CertificateService): Attempting to generate certificate for user '{}' and course ID '{}'", username, courseId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("ERROR (CertificateService): User not found: {}", username);
                    return new RuntimeException("User not found: " + username);
                });

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    logger.error("ERROR (CertificateService): Course not found for ID: {}", courseId);
                    return new RuntimeException("Course not found for ID: " + courseId);
                });

        logger.info("DEBUG (CertificateService): Checking course completion status for user '{}' (ID: {}) and course ID '{}'", username, user.getId(), courseId);
        boolean isCourseCompleted = progressService.getCourseCompletionStatus(user.getId(), courseId);

        if (!isCourseCompleted) {
            logger.warn("WARN (CertificateService): Course ID '{}' not completed by user '{}'. Certificate cannot be generated.", courseId, username);
            throw new IllegalStateException("Course not completed by user. Certificate not available.");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Certificate content
            document.add(new Paragraph("CERTIFICATE OF COMPLETION")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold());
            document.add(new Paragraph("\n")); // Spacer

            document.add(new Paragraph("This certifies that")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14));
            document.add(new Paragraph(user.getUsername().toUpperCase())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(28)
                    .setBold()
                    .setUnderline());
            document.add(new Paragraph("\n")); // Spacer

            document.add(new Paragraph("has successfully completed the course")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14));
            document.add(new Paragraph(course.getTitle())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold());
            document.add(new Paragraph("\n")); // Spacer

            document.add(new Paragraph("on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12));
            document.add(new Paragraph("\n")); // Spacer

            if (course.getInstructor() != null && !course.getInstructor().isEmpty()) {
                document.add(new Paragraph("Instructor: " + course.getInstructor())
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12));
            }

            // --- NEW: Add Verification Link ---
            document.add(new Paragraph("\n\n")); // Add more space before the link

            // Construct the verification URL
            // The frontend route will be something like /verify-certificate/:userId/:courseId
            String verificationUrl = String.format("%s/verify-certificate/%s/%s", frontendBaseUrl, user.getId(), course.getId());
            logger.info("DEBUG (CertificateService): Generated verification URL: {}", verificationUrl);

            // Create a clickable link
            Link verificationLink = new Link("Verify this certificate", PdfAction.createURI(verificationUrl));
            verificationLink.setFontColor(ColorConstants.BLUE); // Make it blue like a hyperlink
            verificationLink.setUnderline(); // Underline it to indicate it's a link

            document.add(new Paragraph()
                    .add(verificationLink)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)); // Smaller font for the verification text
            // --- END NEW ---

            document.close();
            logger.info("DEBUG (CertificateService): Certificate PDF generated successfully for user '{}' and course ID '{}'", username, courseId);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            logger.error("ERROR (CertificateService): Error generating PDF for user '{}' and course ID '{}': {}", username, courseId, e.getMessage(), e);
            throw new RuntimeException("Error generating certificate PDF", e);
        }
    }

    public Map<String, Object> verifyCertificate(String userId, String courseId) {
        logger.info("DEBUG (CertificateService): Verifying certificate for user ID: {} and course ID: {}", userId, courseId);

        Map<String, Object> result = new HashMap<>();

        try {
            // Check if user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("ERROR (CertificateService): User not found for ID: {}", userId);
                        return new RuntimeException("User not found for ID: " + userId);
                    });

            // Check if course exists
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> {
                        logger.error("ERROR (CertificateService): Course not found for ID: {}", courseId);
                        return new RuntimeException("Course not found for ID: " + courseId);
                    });

            // Check if the user has completed the course
            boolean isCourseCompleted = progressService.getCourseCompletionStatus(userId, courseId);

            if (isCourseCompleted) {
                result.put("valid", true);
                result.put("message", "Certificate is valid");
                result.put("studentName", user.getUsername());
                result.put("courseName", course.getTitle());
                result.put("instructor", course.getInstructor());
                result.put("completionDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
                logger.info("DEBUG (CertificateService): Certificate verification successful for user ID: {} and course ID: {}", userId, courseId);
            } else {
                result.put("valid", false);
                result.put("message", "Certificate is not valid - course not completed by this user");
                logger.warn("WARN (CertificateService): Certificate verification failed - course not completed by user ID: {} for course ID: {}", userId, courseId);
            }

        } catch (Exception e) {
            logger.error("ERROR (CertificateService): Error during certificate verification for user ID: {} and course ID: {}: {}", userId, courseId, e.getMessage(), e);
            result.put("valid", false);
            result.put("message", "Certificate verification failed: " + e.getMessage());
        }

        return result;
    }
}
