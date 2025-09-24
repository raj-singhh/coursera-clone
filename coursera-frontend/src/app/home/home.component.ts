import { Component, OnInit, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService, Course } from '../course.service';
import { AuthService } from '../auth.service';
import { Chart, registerables } from 'chart.js';
import { Observable, Subject } from 'rxjs';
import { LoginComponent } from '../login/login.component';
import { RegisterComponent } from '../register/register.component';
import { Router, ActivatedRoute, NavigationEnd, RouterLink } from '@angular/router';
import { filter, takeUntil } from 'rxjs/operators';

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

  showLogin = false;
  showRegister = false;
  isLoggedIn$: Observable<boolean>;
  enrolledCourseIds = new Set<string>();

  showMyCourses = false;

  private readonly destroy$ = new Subject<void>();
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private courseService: CourseService,
    private authService: AuthService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;

    this.destroyRef.onDestroy(() => {
      this.destroy$.next();
      this.destroy$.complete();
    });

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: NavigationEnd) => {
      this.handleRouteChanges(event.urlAfterRedirects);
    });
  }

  ngOnInit(): void {
    this.handleRouteChanges(this.router.url);

    this.isLoggedIn$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(loggedIn => {
      if (loggedIn) {
        this.fetchMyEnrolledCourses(false);
      } else {
        this.enrolledCourseIds.clear();
      }
      this.updateCourseLists();
    });
  }

  ngAfterViewInit(): void {
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
        this.fetchMyEnrolledCourses(true);
      } else {
        this.router.navigate(['/login']);
      }
    } else {
      this.updateCourseLists();
    }
  }

  private updateCourseLists(): void {
    if (!this.showLogin && !this.showRegister) {
      if (this.showMyCourses) {
        this.fetchMyEnrolledCourses(true);
      } else {
        this.fetchCourses();
        if (this.authService.getToken()) {
          this.fetchMyEnrolledCourses(false);
        }
      }
    }
  }

  fetchCourses(): void {
    this.courseService.getAllCourses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
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
        }
      });
  }

  fetchMyEnrolledCourses(updateChart: boolean): void {
    if (!this.authService.getToken()) {
      this.enrolledCourses = [];
      this.enrolledCourseIds.clear();
      if (updateChart) {
        this.updatePriceChart([]);
      }
      return;
    }

    this.courseService.getMyEnrolledCourses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.enrolledCourses = data;
          this.enrolledCourseIds = new Set(data.map(course => course.id));
          console.log('Enrolled courses fetched successfully:', this.enrolledCourses);
          console.log('Enrolled course IDs:', Array.from(this.enrolledCourseIds));

          if (updateChart) {
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

  isCourseEnrolled(courseId: string): boolean {
    return this.enrolledCourseIds.has(courseId);
  }

  onLoginSuccess(): void {
    this.router.navigate(['/']).then(() => {
        this.fetchMyEnrolledCourses(false);
        this.updateCourseLists();
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
    this.fetchMyEnrolledCourses(true);
  }

  viewAllCoursesInternal(): void {
    this.showMyCourses = false;
    this.showLogin = false;
    this.showRegister = false;
    this.fetchCourses();
    if (this.authService.getToken()) {
      this.fetchMyEnrolledCourses(false);
    }
  }

  onPurchaseCourse(courseId: string): void {
    if (!this.authService.getToken()) {
      alert('Please log in to purchase a course.');
      this.router.navigate(['/login']);
      return;
    }

    this.router.navigate(['/payment', courseId]);
  }
}
