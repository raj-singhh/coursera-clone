// src/app/course-detail/course-detail.component.ts
import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService, Course, Lesson, LessonProgressMap, Progress } from '../course.service';
import { AuthService } from '../auth.service';
import { Observable, Subscription, forkJoin } from 'rxjs';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.css',
  changeDetection: ChangeDetectionStrategy.Default
})
export class CourseDetailComponent implements OnInit, OnDestroy {
  @ViewChild('videoPlayer') videoPlayerRef!: ElementRef<HTMLVideoElement>;

  courseDetails: Course | null = null;
  currentCourseId: number | null = null;
  isLoggedIn$: Observable<boolean>;
  currentCourseIsEnrolled: boolean = false;
  lessons: Lesson[] = [];
  selectedLesson: Lesson | null = null;
  lessonProgress: LessonProgressMap = {};
  isCourseCompleted: boolean = false;

  private isLoggedInSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private courseService: CourseService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.currentCourseId = +id;
        this.fetchCourseDetails(this.currentCourseId);
      } else {
        console.error('Course ID not found in route parameters.');
        this.courseDetails = null;
      }
    });

    this.isLoggedInSubscription = this.isLoggedIn$.subscribe(loggedIn => {
      if (loggedIn && this.currentCourseId) {
        this.checkEnrollmentAndProgress();
      } else {
        this.currentCourseIsEnrolled = false;
        this.lessonProgress = {};
        this.isCourseCompleted = false;
      }
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    if (this.isLoggedInSubscription) {
      this.isLoggedInSubscription.unsubscribe();
    }
    if (this.videoPlayerRef && this.videoPlayerRef.nativeElement) {
      this.videoPlayerRef.nativeElement.pause();
      this.videoPlayerRef.nativeElement.removeAttribute('src');
      this.videoPlayerRef.nativeElement.load();
      console.log('Video player paused and unloaded on component destroy.');
    }
  }

  fetchCourseDetails(id: number): void {
    this.courseService.getCourseById(id).subscribe({
      next: (data) => {
        this.courseDetails = data;
        console.log('Course details fetched:', this.courseDetails);
        this.fetchLessonsForCourse();
        if (this.authService.getToken() && this.courseDetails) {
          this.checkEnrollmentAndProgress();
        } else {
          this.currentCourseIsEnrolled = false;
          this.lessonProgress = {};
          this.isCourseCompleted = false;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching course details:', err);
        this.courseDetails = null;
        alert('Course not found or an error occurred.');
        this.router.navigate(['/']);
      }
    });
  }

  private checkEnrollmentAndProgress(): void {
    if (!this.currentCourseId || !this.authService.getToken()) {
      this.currentCourseIsEnrolled = false;
      this.lessonProgress = {};
      this.isCourseCompleted = false;
      this.cdr.detectChanges();
      return;
    }

    this.courseService.isEnrolledInCourse(this.currentCourseId).pipe(take(1)).subscribe({
      next: (enrolled) => {
        this.currentCourseIsEnrolled = enrolled;
        console.log(`Course ${this.currentCourseId} enrollment status: ${this.currentCourseIsEnrolled}`);

        if (this.currentCourseIsEnrolled) {
          forkJoin([
            this.courseService.getUserProgressForCourse(this.currentCourseId!).pipe(take(1)),
            this.courseService.getCourseCompletionStatus(this.currentCourseId!).pipe(take(1))
          ]).subscribe({
            next: ([progressData, completedStatus]) => {
              this.lessonProgress = JSON.parse(JSON.stringify(progressData));
              console.log('User progress for course (map) AFTER FETCH:', this.lessonProgress);
              console.dir(this.lessonProgress);

              this.isCourseCompleted = completedStatus;
              console.log(`Course completion status AFTER FETCH: ${this.isCourseCompleted}`);

              this.cdr.detectChanges();
            },
            error: (err) => {
              console.error('Error fetching progress or completion status:', err);
              this.lessonProgress = {};
              this.isCourseCompleted = false;
              this.cdr.detectChanges();
            }
          });
        } else {
          this.lessonProgress = {};
          this.isCourseCompleted = false;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error checking enrollment status:', err);
        this.currentCourseIsEnrolled = false;
        this.lessonProgress = {};
        this.isCourseCompleted = false;
        this.cdr.detectChanges();
      }
    });
  }

  fetchLessonsForCourse(): void {
    if (!this.currentCourseId) return;
    this.courseService.getLessonsByCourse(this.currentCourseId).subscribe({
      next: (data) => {
        this.lessons = data;
        console.log('Lessons fetched:', this.lessons);
        if (this.lessons.length > 0 && !this.selectedLesson) {
          this.selectLesson(this.lessons[0]);
        }
        if (this.authService.getToken() && this.currentCourseIsEnrolled) {
            this.checkEnrollmentAndProgress();
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching lessons:', err);
        this.lessons = [];
        this.cdr.detectChanges();
      }
    });
  }

  selectLesson(lesson: Lesson): void {
    this.selectedLesson = lesson;
    if (this.videoPlayerRef && this.videoPlayerRef.nativeElement) {
      this.videoPlayerRef.nativeElement.pause();
      this.videoPlayerRef.nativeElement.currentTime = 0;
      this.videoPlayerRef.nativeElement.src = lesson.videoUrl;
      this.videoPlayerRef.nativeElement.load();
      this.videoPlayerRef.nativeElement.loop = false;

      if (this.currentCourseIsEnrolled && !this.isLessonCompleted(lesson.id)) {
        this.videoPlayerRef.nativeElement.play();
      } else if (this.currentCourseIsEnrolled && this.isLessonCompleted(lesson.id)) {
        this.videoPlayerRef.nativeElement.currentTime = 0;
      }
      console.log(`Selected lesson: ${lesson.title}`);
    }
    this.cdr.detectChanges();
  }

  onVideoTimeUpdate(): void {
    if (!this.selectedLesson || !this.authService.getToken() || !this.currentCourseIsEnrolled) return;

    const video = this.videoPlayerRef.nativeElement;
    const percentage = (video.currentTime / video.duration) * 100;
    const completed = video.ended || percentage >= 99.5;

    const currentProgress = this.lessonProgress[this.selectedLesson.id];
    const oldCompleted = currentProgress ? currentProgress.completed : false;

    if (completed || (percentage - (currentProgress?.watchedPercentage || 0) >= 5) || (currentProgress?.watchedPercentage === 0 && percentage > 0 && !oldCompleted)) {
      if (video.ended) {
        video.pause();
        video.loop = false;
        console.log(`Video for lesson ${this.selectedLesson.id} ended. Paused.`);
      }

      this.courseService.updateLessonProgress(this.selectedLesson.id, percentage, completed).subscribe({
        next: (response) => {
          console.log(`Progress updated for lesson ${this.selectedLesson?.id}: ${percentage.toFixed(2)}% completed: ${completed}.`);
          const updatedLessonProgress: Progress = {
            ...currentProgress,
            lesson: this.selectedLesson!,
            watchedPercentage: percentage,
            completed: completed,
            lastUpdated: new Date().toISOString()
          };
          this.lessonProgress = {
            ...this.lessonProgress,
            [this.selectedLesson!.id]: JSON.parse(JSON.stringify(updatedLessonProgress))
          };

          this.checkEnrollmentAndProgress();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to update lesson progress:', err);
        }
      });
    }
  }

  trackByLessonId(index: number, lesson: Lesson): number {
    return lesson.id;
  }

  isLessonCompleted(lessonId: number): boolean {
    const progress = this.lessonProgress[lessonId];
    console.log(`DEBUG: isLessonCompleted for Lesson ID ${lessonId}. Progress object:`, progress, `Is completed:`, progress?.completed);
    return progress?.completed || false;
  }

  enrollInCourse(): void {
    if (!this.currentCourseId || !this.authService.getToken()) {
      alert('Please log in to enroll in this course.');
      this.router.navigate(['/login']);
      return;
    }

    this.courseService.enrollInCourse(this.currentCourseId).subscribe({
      next: (response) => {
        alert(response.message);
        console.log('Enrollment successful:', response.message);
        this.currentCourseIsEnrolled = true;
        this.fetchLessonsForCourse();
        this.checkEnrollmentAndProgress();
        this.courseService.getMyEnrolledCourses().subscribe({
          next: () => console.log('My enrolled courses list refreshed after enrollment.'),
          error: (err) => console.error('Error refreshing my enrolled courses:', err)
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Enrollment failed:', err);
        let errorMessage = 'Failed to enroll in course.';
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        }
        alert(errorMessage);
      }
    });
  }

  // Method to handle certificate download
  downloadCertificate(): void {
    if (!this.currentCourseId || !this.authService.getToken()) {
      alert('Please log in to download the certificate.');
      this.router.navigate(['/login']);
      return;
    }

    if (!this.isCourseCompleted) {
      alert('You must complete all lessons in the course to download the certificate.');
      return;
    }

    // --- NEW DEBUGGING LINE ---
    const certificateDownloadUrl = `${this.courseService['certificateApiUrl']}/download/${this.currentCourseId}`;
    console.log('DEBUG: Attempting to download certificate from URL:', certificateDownloadUrl);
    // --- END NEW DEBUGGING LINE ---

    this.courseService.downloadCertificate(this.currentCourseId).subscribe({
      next: (data: Blob) => {
        const blob = new Blob([data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const courseTitle = this.courseDetails?.title.replace(/[^a-zA-Z0-9]/g, '_') || 'Course';
        const userName = this.authService.getUsername() || 'User';
        a.download = `Certificate_of_Completion_${courseTitle}_${userName}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();

        console.log('Certificate download initiated.');
      },
      error: (err) => {
        console.error('Error downloading certificate:', err);
        let errorMessage = 'Failed to download certificate.';
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        } else if (err.status === 403) {
            errorMessage = 'You must complete all lessons to download the certificate.';
        } else if (err.status === 404) {
            errorMessage = 'Certificate not found or course data missing.';
        }
        alert(errorMessage);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
