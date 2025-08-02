// src/main/java/com/example/coursera_clone/repository/ProgressRepository.java
package com.example.coursera_clone.repository;

import com.example.coursera_clone.model.Progress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends MongoRepository<Progress, String> {

    // Method used by ProgressService to find existing progress for a user and lesson
    Optional<Progress> findByUserIdAndLessonId(String userId, String lessonId);

    // Method used by ProgressService to get all progress for a user for specific lessons
    List<Progress> findByUserIdAndLessonIdIn(String userId, List<String> lessonIds);

    // Method used by ProgressService to count completed lessons for a user in a course
    List<Progress> findByUserIdAndLessonIdInAndCompleted(String userId, List<String> lessonIds, boolean completed);

    // Keep your existing methods if they are different or you have more
    // Example from your old ProgressController:
    // Optional<Progress> findByUserAndLesson(User user, Lesson lesson);
    // List<Progress> findByUserAndLessonIn(User user, List<Lesson> lessons);
}
