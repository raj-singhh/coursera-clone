// src/main/java/com/example/coursera_clone/service/ProgressService.java
package com.example.coursera_clone.service;

import com.example.coursera_clone.model.Course;
import com.example.coursera_clone.model.Lesson;
import com.example.coursera_clone.model.Progress;
import com.example.coursera_clone.model.User;
import com.example.coursera_clone.repository.CourseRepository;
import com.example.coursera_clone.repository.LessonRepository;
import com.example.coursera_clone.repository.ProgressRepository;
import com.example.coursera_clone.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Updates the progress for a specific lesson for a given user.
     * If progress exists, it updates; otherwise, it creates a new entry.
     *
     * @param userId The ID of the user.
     * @param lessonId The ID of the lesson.
     * @param watchedPercentage The percentage of the lesson watched.
     * @param isCompleted Boolean indicating if the lesson is completed.
     * @return The updated or newly created Progress object.
     */
    public Progress updateLessonProgress(Long userId, Long lessonId, double watchedPercentage, boolean isCompleted) {
        logger.info("DEBUG (ProgressService): Updating progress for user {} lesson {}: {}% completed: {}", userId, lessonId, watchedPercentage, isCompleted);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

        // Check if user is enrolled in the course
        // Note: This logic might already be in EnrollmentService if you have one.
        // For now, we'll assume it's handled at the controller or implicitly by data integrity.
        // If you have an EnrollmentService, you might want to inject it here and add a check.

        Progress progress = progressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    logger.info("Creating new progress entry for user '{}' and lesson '{}' (ID: {}).",
                            user.getUsername(), lesson.getTitle(), lessonId);
                    Progress newProgress = new Progress();
                    newProgress.setUser(user);
                    newProgress.setLesson(lesson);
                    return newProgress;
                });

        progress.setWatchedPercentage(watchedPercentage);
        progress.setCompleted(isCompleted);
        progress.setLastUpdated(java.time.LocalDateTime.now());

        Progress savedProgress = progressRepository.save(progress);
        logger.info("DEBUG (ProgressService): Progress saved for user {} lesson {}. Completed: {}", userId, lessonId, savedProgress.isCompleted());
        return savedProgress;
    }

    /**
     * Retrieves all progress entries for a specific course for a given user,
     * mapped by lesson ID.
     *
     * @param userId The ID of the user.
     * @param courseId The ID of the course.
     * @return A map where keys are lesson IDs and values are Progress objects.
     */
    public Map<Long, Progress> getUserProgressForCourse(Long userId, Long courseId) {
        logger.info("DEBUG (ProgressService): Getting user progress for user {} in course {}", userId, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        List<Lesson> lessonsInCourse = lessonRepository.findByCourseIdOrderByLessonOrder(courseId);
        List<Long> lessonIds = lessonsInCourse.stream().map(Lesson::getId).collect(Collectors.toList());

        List<Progress> progressList = progressRepository.findByUserIdAndLessonIdIn(userId, lessonIds);

        Map<Long, Progress> progressMap = progressList.stream()
                .collect(Collectors.toMap(progress -> progress.getLesson().getId(), progress -> progress));

        logger.info("DEBUG (ProgressService): Retrieved {} progress entries for user {} in course {}", progressMap.size(), userId, courseId);
        return progressMap;
    }

    /**
     * Checks if a user has completed all lessons in a given course.
     *
     * @param userId The ID of the user.
     * @param courseId The ID of the course.
     * @return True if all lessons are completed, false otherwise.
     */
    public boolean getCourseCompletionStatus(Long userId, Long courseId) {
        logger.info("DEBUG (ProgressService): Checking course completion status for user {} and course {}", userId, courseId);

        List<Lesson> lessonsInCourse = lessonRepository.findByCourseIdOrderByLessonOrder(courseId);
        if (lessonsInCourse.isEmpty()) {
            logger.warn("WARN (ProgressService): No lessons found for course ID {}. Considering it not completed.", courseId);
            return false; // A course with no lessons cannot be completed
        }

        // Use the existing repository method that directly checks completion status
        long completedLessonsCount = progressRepository.findByUserIdAndLesson_CourseIdAndCompleted(userId, courseId, true).size();
        boolean isCompleted = completedLessonsCount == lessonsInCourse.size();
        logger.info("DEBUG (ProgressService): User {} has completed {} out of {} lessons for course {}. Course completion status: {}",
                userId, completedLessonsCount, lessonsInCourse.size(), courseId, isCompleted);
        return isCompleted;
    }
}
