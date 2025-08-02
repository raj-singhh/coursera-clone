// src/main/java/com/example/coursera_clone/model/Lesson.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "lessons")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "course"}) // Ignore course to prevent circular reference
public class Lesson {

    @Id
    private String id;

    private String title;
    private String videoUrl; // URL for the lesson video
    private String description; // Optional lesson description
    private Integer lessonOrder; // To maintain order of lessons in a course
    // Store courseId as a string reference
    private String courseId;

    public Lesson(String title, String videoUrl, String description, Integer lessonOrder, String courseId) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.description = description;
        this.lessonOrder = lessonOrder;
        this.courseId = courseId;
    }
}
