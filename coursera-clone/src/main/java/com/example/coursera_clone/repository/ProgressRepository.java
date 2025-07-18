// src/main/java/com/example/coursera_clone/repository/ProgressRepository.java
package com.example.coursera_clone.repository;

import com.example.coursera_clone.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Method used by ProgressService to find existing progress for a user and lesson
    Optional<Progress> findByUserIdAndLessonId(Long userId, Long lessonId);

    // Method used by ProgressService to get all progress for a user for specific lessons
    List<Progress> findByUserIdAndLessonIdIn(Long userId, List<Long> lessonIds);

    // Method used by ProgressService to count completed lessons for a user in a course
    List<Progress> findByUserIdAndLesson_CourseIdAndCompleted(Long userId, Long courseId, boolean completed);

    // Keep your existing methods if they are different or you have more
    // Example from your old ProgressController:
    // Optional<Progress> findByUserAndLesson(User user, Lesson lesson);
    // List<Progress> findByUserAndLessonIn(User user, List<Lesson> lessons);
}
