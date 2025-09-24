import { Component, DestroyRef, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService, Course } from '../course.service';
import { AuthService } from '../auth.service';
import { PaymentService } from '../payment.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

declare var Razorpay: any;

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit, OnDestroy {
  selectedPaymentMethod = 'razorpay';
  course: Course | null = null;

  private readonly destroy$ = new Subject<void>();
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private authService: AuthService,
    private paymentService: PaymentService
  ) {
    this.destroyRef.onDestroy(() => {
      this.destroy$.next();
      this.destroy$.complete();
    });
  }

  ngOnInit(): void {
    const courseId = this.route.snapshot.paramMap.get('id');
    if (courseId) {
      this.courseService.getCourseById(courseId)
        .pipe(takeUntil(this.destroy$))
        .subscribe(course => {
          this.course = course;
        });
    }
  }

  ngOnDestroy(): void {
    // Cleanup handled by destroyRef
  }

  selectPaymentMethod(method: string): void {
    this.selectedPaymentMethod = method;
  }

  proceedToPayment(): void {
    if (this.selectedPaymentMethod === 'razorpay' && this.course) {
      this.paymentService.createOrder(this.course.price).subscribe(
        (response) => {
          const options = {
            key: 'rzp_test_6pcInM1gebf7pX', // Add your Razorpay Key ID
            amount: response.amount,
            currency: response.currency,
            name: 'Coursera Clone',
            description: 'Course Payment',
            order_id: response.id,
            handler: (res: any) => {
              this.enrollInCourse();
            },
            prefill: {
              name: this.authService.getUsername(),
              email: '',
              contact: ''
            },
            notes: {
              address: 'Coursera Clone'
            },
            theme: {
              color: '#3399cc'
            }
          };
          const rzp = new Razorpay(options);
          rzp.open();
        },
        (error) => {
          console.error('Error creating order:', error);
        }
      );
    }
  }

  enrollInCourse(): void {
    if (this.course) {
      this.courseService.enrollInCourse(this.course.id).subscribe({
        next: (response) => {
          alert(response.message);
          this.router.navigate(['/my-courses']);
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
}
