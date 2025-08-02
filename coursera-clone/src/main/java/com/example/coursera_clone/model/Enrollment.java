// src/main/java/com/example/coursera_clone/model/Enrollment.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // NEW: Import JsonIgnoreProperties

@Document(collection = "enrollments")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "course"}) // NEW: Ignore Hibernate internal fields and related entities to prevent recursion
public class Enrollment {

    @Id
    private String id;

    private String userId;
    private String courseId;

    private LocalDateTime enrollmentDate;
    public Enrollment(String userId, String courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.enrollmentDate = LocalDateTime.now();
    }
}
