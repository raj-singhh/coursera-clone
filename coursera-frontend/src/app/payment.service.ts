import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private baseUrl = 'http://localhost:8080/api/payment';

  constructor(private http: HttpClient) { }

  createOrder(amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/create-order`, { amount });
  }
}
