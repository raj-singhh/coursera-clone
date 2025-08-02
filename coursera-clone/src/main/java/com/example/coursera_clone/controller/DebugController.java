// Utility endpoint to list all course IDs and titles for debugging
package com.example.coursera_clone.controller;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/course-ids")
    public ResponseEntity<List<Map<String, String>>> getAllCourseIds() {
        List<Map<String, String>> ids = courseRepository.findAll().stream()
            .map(course -> Map.of("id", course.getId(), "title", course.getTitle()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }
}
