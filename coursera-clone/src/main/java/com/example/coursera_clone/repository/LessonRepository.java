// src/main/java/com/example/coursera_clone/repository/LessonRepository.java
package com.example.coursera_clone.repository;

import com.example.coursera_clone.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends MongoRepository<Lesson, String> {

    // Method used by ProgressService to get lessons for a course, ordered by lessonOrder
    List<Lesson> findByCourseIdOrderByLessonOrder(String courseId);

    // Keep your existing methods if they are different or you have more
    // Example from your old ProgressController:
    // List<Lesson> findByCourseOrderByLessonOrder(Course course);
}
