import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BookingNavbarComponent } from '../../shared/booking-navbar/booking-navbar.component';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [CommonModule, BookingNavbarComponent],
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.css']
})
export class ConfirmationComponent implements OnInit {

  bookingId: string = '';
  bookingData: any;
  passengers: any[] = [];
  totals: any;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    console.log('✅ Confirmation component loaded');
    const bookingIdParam = this.route.snapshot.paramMap.get('bookingId');

    if (!bookingIdParam) {
      this.router.navigate(['/search']);
      return;
    }

    this.bookingId = bookingIdParam;

    // ✅ Fetch booking from backend (production safe)
    this.http.get<any>(`http://localhost:5000/api/bookings/${this.bookingId}`)
      .subscribe({
        next: (response) => {
          if (!response?.success) {
            this.router.navigate(['/search']);
            return;
          }

          console.log('✅ Booking API response:', response);
          console.log('✅ Booking object:', response.booking);

          this.bookingData = response.booking;
          this.passengers = response.passengers || [];
          this.totals = {
            grandTotal: response.booking.total_amount
          };
        },
        error: () => {
          this.router.navigate(['/search']);
        }
      });
  }

  printTicket() {
    window.print();
  }

  downloadTicket() {
    if (!this.bookingData?.id) return;

    window.open(
      `http://localhost:5000/api/bookings/${this.bookingData.id}/ticket`,
      '_blank'
    );
  }

  goHome() {
    this.router.navigate(['/search']);
  }
}
