// src/main/java/com/example/coursera_clone/service/CertificateService.java
package com.example.coursera_clone.service;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.ProgressRepository;
import com.example.coursera_clone.repository.UserRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.svg.converter.SvgConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    @Value("${app.frontend.base-url}")
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
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Add logo
            try (InputStream logoStream = getClass().getResourceAsStream("/static/logo.svg")) {
                if (logoStream == null) {
                    throw new RuntimeException("Could not find logo resource");
                }
                Image logo = SvgConverter.convertToImage(logoStream, pdf);
                logo.setAutoScale(true);
                logo.setFixedPosition(50, 500);
                document.add(logo);
            }

            if (frontendBaseUrl == null || frontendBaseUrl.isEmpty()) {
                logger.error("ERROR (CertificateService): Frontend base URL is not configured. Cannot generate verification link.");
                throw new IllegalStateException("Frontend base URL is not configured. Certificate verification link cannot be added.");
            }


            // Fonts
            PdfFont titleFont = PdfFontFactory.createFont();
            PdfFont nameFont = PdfFontFactory.createFont();
            PdfFont textFont = PdfFontFactory.createFont();

            // Certificate content
            document.add(new Paragraph("CERTIFICATE OF COMPLETION")
                    .setFont(titleFont)
                    .setFontSize(36)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(100));

            document.add(new Paragraph("This certifies that")
                    .setFont(textFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50));

            document.add(new Paragraph(user.getUsername().toUpperCase())
                    .setFont(nameFont)
                    .setFontSize(48)
                    .setBold()
                    .setUnderline()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            document.add(new Paragraph("has successfully completed the course")
                    .setFont(textFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50));

            document.add(new Paragraph(course.getTitle())
                    .setFont(titleFont)
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            document.add(new Paragraph("on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
                    .setFont(textFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50));

            if (course.getInstructor() != null && !course.getInstructor().isEmpty()) {
                document.add(new Paragraph("Instructor: " + course.getInstructor())
                        .setFont(textFont)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20));
            }

            // Add border
            PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());
            canvas.setStrokeColor(ColorConstants.BLUE);
            canvas.setLineWidth(5);
            // Add verification link
            String verificationLink = String.format("%s/verify-certificate/%s/%s", frontendBaseUrl, user.getId(), course.getId());
            // Add verification link as a clickable element
            Link link = new Link("Verify Certificate", PdfAction.createURI(verificationLink));
            document.add(new Paragraph(link)
                    .setFont(textFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setUnderline()
                    .setFontColor(ColorConstants.BLUE));

            canvas.rectangle(20, 20, document.getPdfDocument().getDefaultPageSize().getWidth() - 40, document.getPdfDocument().getDefaultPageSize().getHeight() - 40);
            canvas.stroke();

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
