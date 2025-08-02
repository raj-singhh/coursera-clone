// src/main/java/com/example/coursera_clone/controller/LessonController.java
package com.example.coursera_clone.controller;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.Lesson;
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Endpoint to get all lessons for a specific course, ordered by lessonOrder

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable String courseId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isEmpty()) {
            logger.warn("Lessons not found: Course with ID {} does not exist.", courseId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrder(courseId);
        logger.info("Retrieved {} lessons for course ID {}.", lessons.size(), courseId);
        return ResponseEntity.ok(lessons);
    }

    // Endpoint to get a single lesson by ID
    @GetMapping("/{lessonId}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable String lessonId) {
        Optional<Lesson> lesson = lessonRepository.findById(lessonId);
        if (lesson.isPresent()) {
            return ResponseEntity.ok(lesson.get());
        } else {
            logger.warn("Lesson with ID {} not found.", lessonId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
