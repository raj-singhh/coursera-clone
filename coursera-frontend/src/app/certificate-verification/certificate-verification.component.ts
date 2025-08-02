import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

interface VerificationResult {
  valid: boolean;
  message: string;
  studentName?: string;
  courseName?: string;
  instructor?: string;
  completionDate?: string;
}

@Component({
  selector: 'app-certificate-verification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './certificate-verification.component.html',
  styleUrls: ['./certificate-verification.component.css']
})
export class CertificateVerificationComponent implements OnInit {
  verificationResult: VerificationResult | null = null;
  loading = true;
  error = false;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const userId = this.route.snapshot.paramMap.get('userId');
    const courseId = this.route.snapshot.paramMap.get('courseId');

    if (userId && courseId) {
      this.verifyCertificate(userId, courseId);
    } else {
      this.error = true;
      this.loading = false;
    }
  }

  verifyCertificate(userId: string, courseId: string): void {
    const apiUrl = `http://localhost:8080/api/certificates/verify/${userId}/${courseId}`;
    
    this.http.get<VerificationResult>(apiUrl).subscribe({
      next: (result) => {
        this.verificationResult = result;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error verifying certificate:', error);
        this.error = true;
        this.loading = false;
      }
    });
  }

  getCurrentDate(): string {
    return new Date().toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}
