// src/main/java/com/example/coursera_clone/model/Lesson.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "course"}) // Ignore course to prevent circular reference
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String videoUrl; // URL for the lesson video

    @Column(length = 500)
    private String description; // Optional lesson description

    @Column(nullable = false)
    private Integer lessonOrder; // To maintain order of lessons in a course

    // ManyToOne relationship with Course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public Lesson(String title, String videoUrl, String description, Integer lessonOrder, Course course) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.description = description;
        this.lessonOrder = lessonOrder;
        this.course = course;
    }
}
