// src/main/java/com/example/coursera_clone/repository/LessonRepository.java
package com.example.coursera_clone.repository;

import com.example.coursera_clone.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Method used by ProgressService to get lessons for a course, ordered by lessonOrder
    List<Lesson> findByCourseIdOrderByLessonOrder(Long courseId);

    // Keep your existing methods if they are different or you have more
    // Example from your old ProgressController:
    // List<Lesson> findByCourseOrderByLessonOrder(Course course);
}
