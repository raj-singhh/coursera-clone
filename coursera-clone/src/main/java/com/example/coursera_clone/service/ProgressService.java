// src/main/java/com/example/coursera_clone/service/ProgressService.java
package com.example.coursera_clone.service;

import com.example.coursera_clone.model.Lesson;
import com.example.coursera_clone.model.Progress;
import com.example.coursera_clone.repository.LessonRepository;
import com.example.coursera_clone.repository.ProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private LessonRepository lessonRepository;



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
    public Progress updateLessonProgress(String userId, String lessonId, Double watchedPercentage, Boolean isCompleted) {
        logger.info("DEBUG (ProgressService): Updating progress for user {} lesson {}: {}% completed: {}", userId, lessonId, watchedPercentage, isCompleted);

        // No need to fetch User or Lesson objects for MongoDB, just use IDs
        Progress progress = progressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> new Progress(userId, lessonId));
        progress.updateProgress(watchedPercentage, isCompleted);
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
    public Map<String, Progress> getUserProgressForCourse(String userId, String courseId) {
        logger.info("DEBUG (ProgressService): Getting user progress for user {} in course {}", userId, courseId);
        // No need to fetch Course object for MongoDB, just use courseId

        List<Lesson> lessonsInCourse = lessonRepository.findByCourseIdOrderByLessonOrder(courseId);
        List<String> lessonIds = lessonsInCourse.stream().map(Lesson::getId).collect(Collectors.toList());

        List<Progress> progressList = progressRepository.findByUserIdAndLessonIdIn(userId, lessonIds);

        Map<String, Progress> progressMap = progressList.stream()
                .collect(Collectors.toMap(Progress::getLessonId, progress -> progress));

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
    public boolean getCourseCompletionStatus(String userId, String courseId) {
        logger.info("DEBUG (ProgressService): Checking course completion status for user {} and course {}", userId, courseId);

        List<Lesson> lessonsInCourse = lessonRepository.findByCourseIdOrderByLessonOrder(courseId);
        if (lessonsInCourse.isEmpty()) {
            logger.warn("WARN (ProgressService): No lessons found for course ID {}. Considering it not completed.", courseId);
            return false; // A course with no lessons cannot be completed
        }

        List<String> lessonIds = lessonsInCourse.stream().map(Lesson::getId).collect(Collectors.toList());
        long completedLessonsCount = progressRepository.findByUserIdAndLessonIdInAndCompleted(userId, lessonIds, true).size();
        boolean isCompleted = completedLessonsCount == lessonsInCourse.size();
        logger.info("DEBUG (ProgressService): User {} has completed {} out of {} lessons for course {}. Course completion status: {}",
                userId, completedLessonsCount, lessonsInCourse.size(), courseId, isCompleted);
        return isCompleted;
    }
}
