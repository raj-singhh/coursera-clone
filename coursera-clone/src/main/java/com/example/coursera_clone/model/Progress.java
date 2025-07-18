// src/main/java/com/example/coursera_clone/model/Progress.java
package com.example.coursera_clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
})
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "lesson"})
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // FIXED: Renamed from 'isCompleted' to 'completed' to match JSON output
    @Column(nullable = false)
    private boolean completed; // Renamed from isCompleted

    @Column(nullable = false)
    private Double watchedPercentage;

    private LocalDateTime lastUpdated;

    public Progress(User user, Lesson lesson) {
        this.user = user;
        this.lesson = lesson;
        this.completed = false; // Initialize with new name
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
