// src/main/java/com/example/coursera_clone/controller/CourseController.java
package com.example.coursera_clone.controller;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.Lesson; // NEW: Import Lesson
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.LessonRepository; // NEW: Import LessonRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired // NEW: Inject LessonRepository
    private LessonRepository lessonRepository;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isPresent()) {
            return ResponseEntity.ok(course.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostConstruct
    public void addSampleData() {
        if (courseRepository.count() == 0) {
            // Course 1: Introduction to Spring Boot
            Course course1 = new Course();
            course1.setTitle("Introduction to Spring Boot");
            course1.setDescription("Learn the basics of building applications with Spring Boot. Covers REST APIs, JPA, and more. This course is perfect for beginners looking to get started with modern Java development. You will build a complete RESTful API from scratch, integrate with a database, and understand core Spring Boot concepts like dependency injection and auto-configuration. Practical exercises and a final project will solidify your learning.");
            course1.setThumbnailUrl("https://placehold.co/600x400/5e81ac/ffffff?text=Spring+Boot");
            course1.setPrice(new BigDecimal("49.99"));
            course1.setInstructor("Dr. Alice Smith");
            course1.setDuration(20);
            course1.setRating(4.5);
            // REMOVED: course1.setVideoUrl(...)
            courseRepository.save(course1); // Save course first to get its ID

            // Add Lessons for Course 1
            lessonRepository.save(new Lesson("Course Introduction", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Overview of Spring Boot and course structure.", 1, course1));
            lessonRepository.save(new Lesson("Setting up Development Environment", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Tools and setup for Spring Boot development.", 2, course1));
            lessonRepository.save(new Lesson("Building Your First REST API", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Hands-on coding for a simple REST endpoint.", 3, course1));
            lessonRepository.save(new Lesson("Database Integration with JPA", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Connecting to H2 database and using JPA.", 4, course1));


            // Course 2: Mastering Angular (Modern)
            Course course2 = new Course();
            course2.setTitle("Mastering Angular (Modern)");
            course2.setDescription("A deep dive into the modern Angular framework (Angular 2+). Build dynamic single-page applications with TypeScript, components, and services. Learn advanced topics like routing, state management, and testing. This course emphasizes best practices, performance optimization, and building scalable Angular applications. Includes hands-on projects to apply your knowledge.");
            course2.setThumbnailUrl("https://placehold.co/600x400/bf616a/ffffff?text=Angular");
            course2.setPrice(new BigDecimal("39.99"));
            course2.setInstructor("Prof. Bob Johnson");
            course2.setDuration(25);
            course2.setRating(4.8);
            courseRepository.save(course2);

            // Add Lessons for Course 2
            lessonRepository.save(new Lesson("Angular Project Setup", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Setting up Angular CLI and workspace.", 1, course2));
            lessonRepository.save(new Lesson("Components and Templates", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Building UI with Angular components.", 2, course2));
            lessonRepository.save(new Lesson("Services and Dependency Injection", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Managing data and logic with services.", 3, course2));


            // Course 3: Database Fundamentals with SQL
            Course course3 = new Course();
            course3.setTitle("Database Fundamentals with SQL");
            course3.setDescription("Understand relational databases, SQL, and database design principles. Essential for data management and backend development. Covers topics like normalization, joins, and indexing. You will learn to write complex SQL queries, design efficient database schemas, and manage data effectively. Practical labs with MySQL and PostgreSQL are included.");
            course3.setThumbnailUrl("https://placehold.co/600x400/8fbcbb/ffffff?text=Databases");
            course3.setPrice(new BigDecimal("29.99"));
            course3.setInstructor("Ms. Carol White");
            course3.setDuration(15);
            course3.setRating(4.2);
            courseRepository.save(course3);

            // Add Lessons for Course 3
            lessonRepository.save(new Lesson("Introduction to Relational Databases", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Understanding tables, rows, and columns.", 1, course3));
            lessonRepository.save(new Lesson("Basic SQL Queries (SELECT, FROM, WHERE)", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Retrieving data from databases.", 2, course3));


            // Course 4: Advanced Java Programming & Concurrency
            Course course4 = new Course();
            course4.setTitle("Advanced Java Programming & Concurrency");
            course4.setDescription("Explore advanced Java concepts like concurrency, functional programming, and design patterns. This course is for experienced Java developers looking to deepen their understanding of the language and build high-performance applications. Topics include multithreading, Java Streams API, Lambdas, and common design patterns like Singleton, Factory, and Observer. Extensive coding exercises are provided.");
            course4.setThumbnailUrl("https://placehold.co/600x400/d08770/ffffff?text=Java+Adv");
            course4.setPrice(new BigDecimal("59.99"));
            course4.setInstructor("Dr. David Green");
            course4.setDuration(30);
            course4.setRating(4.9);
            courseRepository.save(course4);

            // Add Lessons for Course 4
            lessonRepository.save(new Lesson("Multithreading Basics", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Threads, Runnables, and Executors.", 1, course4));
            lessonRepository.save(new Lesson("Concurrency Utilities", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Locks, Semaphores, and Atomic Variables.", 2, course4));
            lessonRepository.save(new Lesson("Java Streams API", "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", "Functional programming with collections.", 3, course4));
        }
    }
}
