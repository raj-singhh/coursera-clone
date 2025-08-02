// src/main/java/com/example/coursera_clone/model/Course.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "courses")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course {

    @Id
    private String id;

    private String title;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;

    private String instructor;
    private Integer duration;
    private Double rating;

    // REMOVED: private String videoUrl;

    // For MongoDB, you can store lesson IDs if you want to reference lessons
    // private Set<String> lessonIds = new HashSet<>();
}
