// src/main/java/com/example/coursera_clone/model/Course.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String thumbnailUrl;

    @Column(nullable = false)
    private BigDecimal price;

    private String instructor;
    private Integer duration;
    private Double rating;

    // REMOVED: private String videoUrl;

    // NEW: One-to-Many relationship with Lesson
    // mappedBy refers to the 'course' field in the Lesson entity
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Lesson> lessons = new HashSet<>(); // Initialize to prevent NullPointerException

    // Helper method to add a lesson
    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
        lesson.setCourse(this);
    }

    // Helper method to remove a lesson
    public void removeLesson(Lesson lesson) {
        this.lessons.remove(lesson);
        lesson.setCourse(null);
    }
}
