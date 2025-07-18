// src/main/java/com/example/coursera_clone/model/Enrollment.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // NEW: Import JsonIgnoreProperties

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "course"}) // NEW: Ignore Hibernate internal fields and related entities to prevent recursion
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    public Enrollment(User user, Course course) {
        this.user = user;
        this.course = course;
        this.enrollmentDate = LocalDateTime.now();
    }
}
