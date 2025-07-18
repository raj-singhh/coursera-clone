// src/main/java/com/example/coursera_clone/service/CertificateService.java
package com.example.coursera_clone.service;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.ProgressRepository; // Assuming this exists
import com.example.coursera_clone.repository.UserRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgressRepository progressRepository; // Inject ProgressRepository

    @Autowired
    private ProgressService progressService; // Inject ProgressService to use its completion logic

    public ByteArrayInputStream generateCertificate(String username, Long courseId) {
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

        // CRITICAL CHECK: Verify if the user has completed the course using ProgressService
        logger.info("DEBUG (CertificateService): Checking course completion status for user '{}' (ID: {}) and course ID '{}'", username, user.getId(), courseId);
        boolean isCourseCompleted = progressService.getCourseCompletionStatus(user.getId(), courseId);

        if (!isCourseCompleted) {
            logger.warn("WARN (CertificateService): Course ID '{}' not completed by user '{}'. Certificate cannot be generated.", courseId, username);
            // Throw an exception that the controller can catch and map to a 403 Forbidden
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

            document.close();
            logger.info("DEBUG (CertificateService): Certificate PDF generated successfully for user '{}' and course ID '{}'", username, courseId);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            logger.error("ERROR (CertificateService): Error generating PDF for user '{}' and course ID '{}': {}", username, courseId, e.getMessage(), e);
            throw new RuntimeException("Error generating certificate PDF", e);
        }
    }
}
