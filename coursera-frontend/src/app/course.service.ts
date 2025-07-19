// src/app/course.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { MessageResponse } from './auth.service';

export interface Course {
  id: number;
  title: string;
  description: string;
  thumbnailUrl: string;
  price: number;
  instructor?: string;
  duration?: number;
  rating?: number;
  lessons?: Lesson[];
}

export interface Lesson {
  id: number;
  title: string;
  videoUrl: string;
  description?: string;
  lessonOrder: number;
}

export interface Progress {
  id: number;
  lesson: Lesson;
  completed: boolean;
  watchedPercentage: number;
  lastUpdated: string;
}

export interface LessonProgressMap {
  [lessonId: number]: Progress;
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private apiUrl = `${environment.backendUrl}/api/courses`;
  private enrollmentApiUrl = `${environment.backendUrl}/api/enrollments`;
  private lessonApiUrl = `${environment.backendUrl}/api/lessons`;
  private progressApiUrl = `${environment.backendUrl}/api/progress`;
  private certificateApiUrl = `${environment.backendUrl}/api/certificates`; 

  constructor(private http: HttpClient) { }

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl);
  }

  getCourseById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/${id}`);
  }

  enrollInCourse(courseId: number): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.enrollmentApiUrl}/enroll/${courseId}`, {});
  }

  getMyEnrolledCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.enrollmentApiUrl}/my-courses`);
  }

  isEnrolledInCourse(courseId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.enrollmentApiUrl}/is-enrolled/${courseId}`);
  }

  getLessonsByCourse(courseId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.lessonApiUrl}/course/${courseId}`);
  }

  updateLessonProgress(lessonId: number, watchedPercentage: number, completed: boolean): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.progressApiUrl}/lesson/${lessonId}`, { watchedPercentage, isCompleted: completed });
  }

  getUserProgressForCourse(courseId: number): Observable<LessonProgressMap> {
    return this.http.get<LessonProgressMap>(`${this.progressApiUrl}/course/${courseId}/user-progress`);
  }

  getCourseCompletionStatus(courseId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.progressApiUrl}/course/${courseId}/completion-status`);
  }

  // NEW: Method to download certificate
  downloadCertificate(courseId: number): Observable<Blob> {
    // responseType: 'blob' is crucial for downloading binary data like PDFs
    return this.http.get(`${this.certificateApiUrl}/download/${courseId}`, { responseType: 'blob' });
  }
}
