// src/app/home/home.component.ts
import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService, Course } from '../course.service';
import { AuthService } from '../auth.service';
import { Chart, registerables } from 'chart.js';
import { Observable } from 'rxjs';
import { LoginComponent } from '../login/login.component';
import { RegisterComponent } from '../register/register.component';
import { Router, ActivatedRoute, NavigationEnd, RouterLink } from '@angular/router';
import { filter } from 'rxjs/operators';

Chart.register(...registerables);

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    LoginComponent,
    RegisterComponent,
    RouterLink
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
  allCourses: Course[] = [];
  enrolledCourses: Course[] = [];
  private priceChart: Chart | null = null;

  showLogin: boolean = false;
  showRegister: boolean = false;
  isLoggedIn$: Observable<boolean>;
  enrolledCourseIds: Set<number> = new Set<number>(); // NEW: To store IDs of enrolled courses

  showMyCourses: boolean = false;

  constructor(
    private courseService: CourseService,
    private authService: AuthService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.handleRouteChanges(event.urlAfterRedirects);
    });
  }

  ngOnInit(): void {
    this.handleRouteChanges(this.router.url);

    this.isLoggedIn$.subscribe(loggedIn => {
      if (loggedIn) {
        this.fetchMyEnrolledCourses(false); // Fetch enrolled courses silently on login status change
      } else {
        this.enrolledCourseIds.clear(); // Clear enrolled IDs on logout
      }
      this.updateCourseLists(); // Re-fetch all courses or my courses based on current view
    });
  }

  ngAfterViewInit(): void {
    // Chart rendering happens after courses are fetched
  }

  ngOnDestroy(): void {
    if (this.priceChart) {
      this.priceChart.destroy();
    }
  }

  private handleRouteChanges(url: string): void {
    this.showLogin = false;
    this.showRegister = false;
    this.showMyCourses = false;

    if (url === '/login') {
      this.showLogin = true;
    } else if (url === '/register') {
      this.showRegister = true;
    } else if (url === '/my-courses') {
      if (this.authService.getToken()) {
        this.showMyCourses = true;
        this.fetchMyEnrolledCourses(true); // Explicitly fetch and show chart for My Courses
      } else {
        this.router.navigate(['/login']);
      }
    } else { // Default home route '/'
      this.updateCourseLists();
    }
  }

  private updateCourseLists(): void {
    // Only fetch courses if not showing login/register forms
    if (!this.showLogin && !this.showRegister) {
      if (this.showMyCourses) {
        this.fetchMyEnrolledCourses(true); // Pass true to update chart
      } else {
        this.fetchCourses(); // Always fetch all courses
        if (this.authService.getToken()) {
          this.fetchMyEnrolledCourses(false); // Fetch enrolled silently to update purchase buttons
        }
      }
    }
  }

  fetchCourses(): void {
    this.courseService.getAllCourses().subscribe({
      next: (data) => {
        this.allCourses = data;
        console.log('All courses fetched successfully:', this.allCourses);
        if (!this.showMyCourses && !this.showLogin && !this.showRegister) {
          this.updatePriceChart(this.allCourses);
        }
      },
      error: (error) => {
        console.error('Error fetching all courses:', error);
        this.allCourses = [];
        // No alert here, as it might be due to no token
      }
    });
  }

  // Modified to take a parameter to decide if chart should be updated
  fetchMyEnrolledCourses(updateChart: boolean): void {
    if (!this.authService.getToken()) {
      this.enrolledCourses = [];
      this.enrolledCourseIds.clear();
      if (updateChart) { // If we were expecting to update chart for My Courses but no token
        this.updatePriceChart([]);
      }
      return;
    }

    this.courseService.getMyEnrolledCourses().subscribe({
      next: (data) => {
        this.enrolledCourses = data;
        this.enrolledCourseIds = new Set(data.map(course => course.id)); // Populate the Set
        console.log('Enrolled courses fetched successfully:', this.enrolledCourses);
        console.log('Enrolled course IDs:', Array.from(this.enrolledCourseIds));

        if (updateChart) { // Only update chart if explicitly requested (i.e., on My Courses tab)
          this.updatePriceChart(this.enrolledCourses);
        }
      },
      error: (error) => {
        console.error('Error fetching enrolled courses:', error);
        this.enrolledCourses = [];
        this.enrolledCourseIds.clear();
        if (updateChart) {
          this.updatePriceChart([]);
        }
      }
    });
  }

  updatePriceChart(coursesToChart: Course[]): void {
    if (coursesToChart.length === 0) {
      if (this.priceChart) {
        this.priceChart.destroy();
        this.priceChart = null;
      }
      return;
    }

    const priceRanges = {
      '<₹30': 0,
      '₹30-₹50': 0,
      '>₹50': 0
    };

    coursesToChart.forEach(course => {
      const price = parseFloat(course.price.toString());
      if (price < 30) {
        priceRanges['<₹30']++;
      } else if (price >= 30 && price <= 50) {
        priceRanges['₹30-₹50']++;
      } else {
        priceRanges['>₹50']++;
      }
    });

    const ctx = document.getElementById('priceDistributionChart') as HTMLCanvasElement;
    if (!ctx) {
      console.error('Canvas element not found for chart.');
      return;
    }
    const context = ctx.getContext('2d');
    if (!context) {
      console.error('2D context not available for canvas.');
      return;
    }

    if (this.priceChart) {
      this.priceChart.data.labels = Object.keys(priceRanges);
      this.priceChart.data.datasets[0].data = Object.values(priceRanges);
      this.priceChart.update();
    } else {
      this.priceChart = new Chart(context, {
        type: 'bar',
        data: {
          labels: Object.keys(priceRanges),
          datasets: [{
            label: 'Number of Courses',
            data: Object.values(priceRanges),
            backgroundColor: [
              'rgba(94, 129, 172, 0.7)',
              'rgba(191, 97, 106, 0.7)',
              'rgba(143, 188, 187, 0.7)'
            ],
            borderColor: [
              'rgba(94, 129, 172, 1)',
              'rgba(191, 97, 106, 1)',
              'rgba(143, 188, 187, 1)'
            ],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  let label = context.dataset.label || '';
                  if (label) {
                    label += ': ';
                  }
                  label += context.parsed.y + ' courses';
                  return label;
                }
              }
            },
          },
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: 'Number of Courses'
              },
              ticks: {
                stepSize: 1,
                callback: function(tickValue: string | number) {
                    const value = typeof tickValue === 'string' ? parseFloat(tickValue) : tickValue;
                    if (value % 1 === 0) {
                        return value;
                    }
                    return null;
                }
              }
            },
            x: {
              title: {
                display: true,
                text: 'Price Range'
              }
            }
          }
        }
      });
    }
  }

  // NEW: Helper method to check if a course is enrolled
  isCourseEnrolled(courseId: number): boolean {
    return this.enrolledCourseIds.has(courseId);
  }

  onLoginSuccess(): void {
    this.router.navigate(['/']).then(() => {
        this.fetchMyEnrolledCourses(false); // Refresh enrolled courses after login, but don't show chart
        this.updateCourseLists(); // Re-evaluate which courses to show and update
    });
  }

  onRegisterSuccess(): void {
    this.router.navigate(['/login']);
  }

  showLoginFormInternal(): void {
    this.showLogin = true;
    this.showRegister = false;
    this.showMyCourses = false;
  }

  showRegisterFormInternal(): void {
    this.showRegister = true;
    this.showLogin = false;
    this.showMyCourses = false;
  }

  viewMyCoursesInternal(): void {
    if (!this.authService.getToken()) {
      alert('Please log in to view your courses.');
      this.router.navigate(['/login']);
      return;
    }
    this.showMyCourses = true;
    this.showLogin = false;
    this.showRegister = false;
    this.fetchMyEnrolledCourses(true); // Explicitly fetch and show chart for My Courses
  }

  viewAllCoursesInternal(): void {
    this.showMyCourses = false;
    this.showLogin = false;
    this.showRegister = false;
    this.fetchCourses();
    if (this.authService.getToken()) {
      this.fetchMyEnrolledCourses(false); // Update enrolled IDs in background for button states
    }
  }

  onPurchaseCourse(courseId: number): void {
    if (!this.authService.getToken()) {
      alert('Please log in to purchase a course.');
      this.router.navigate(['/login']);
      return;
    }

    this.courseService.enrollInCourse(courseId).subscribe({
      next: (response) => {
        alert(response.message);
        console.log('Enrollment successful:', response.message);
        // NEW: Always refresh enrolled courses after a purchase
        this.fetchMyEnrolledCourses(this.showMyCourses); // Pass current showMyCourses state to update chart if on that tab
        // Also, update all courses in case a purchase happened and user switches back
        this.fetchCourses(); // This will refresh the 'allCourses' list which includes the price chart if needed
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
}
