// src/main/java/com/example/coursera_clone/model/Progress.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "progress")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "lesson"})
public class Progress {

    @Id
    private String id;

    private String userId;
    private String lessonId;

    // FIXED: Renamed from 'isCompleted' to 'completed' to match JSON output
    private boolean completed; // Renamed from isCompleted
    private Double watchedPercentage;

    private LocalDateTime lastUpdated;

    public Progress(String userId, String lessonId) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.completed = false;
        this.watchedPercentage = 0.0;
        this.lastUpdated = LocalDateTime.now();
    }

    // FIXED: Update method to use 'completed'
    public void updateProgress(Double watchedPercentage, boolean completed) {
        this.watchedPercentage = watchedPercentage;
        this.completed = completed; // Use new name
        this.lastUpdated = LocalDateTime.now();
    }

    // You might also need a getter for 'isCompleted' if other parts of your backend
    // or frontend (via DTOs) still expect it, but for direct JSON serialization,
    // 'completed' will be used. For simplicity, we'll align everything to 'completed'.
    // If you need both, you'd use @JsonProperty("isCompleted") on a getter for 'completed'.
    // For now, let's stick to one name.
}
