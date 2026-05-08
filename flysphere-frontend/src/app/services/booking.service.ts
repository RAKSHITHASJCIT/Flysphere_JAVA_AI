import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BookingService {

  private apiUrl = 'http://localhost:5000/api/bookings';

  constructor(private http: HttpClient) {}

  getUserBookings(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/user/${userId}`);
  }

  cancelBooking(bookingCode: string): Observable<any> {
    // ✅ Backend returns 200 with empty body, so expect text response
    return this.http.put(
      `${this.apiUrl}/${bookingCode}/cancel`,
      {},
      { responseType: 'text' as 'json' }
    );
  }
}
