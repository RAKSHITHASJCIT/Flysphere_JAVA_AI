import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { BookingNavbarComponent } from '../../shared/booking-navbar/booking-navbar.component';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [CommonModule, HttpClientModule, BookingNavbarComponent],
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.css']
})
export class ConfirmationComponent implements OnInit {

  // ✅ Dropdown state (closed by default)
  showOutboundAddOns = false;
  showReturnAddOns = false;

  toggleOutbound() {
    this.showOutboundAddOns = !this.showOutboundAddOns;
  }

  toggleReturn() {
    this.showReturnAddOns = !this.showReturnAddOns;
  }

  bookingData: any;
  bookingId: string = '';
  fromMyBookings: boolean = false;
  isCancelled: boolean = false;

  passengers: any[] = [];
  totals: any;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    // ✅ Detect navigation source
    this.fromMyBookings = sessionStorage.getItem('fromMyBookings') === 'true';
    
    // ✅ Clear flag immediately so it doesn't affect future navigations
    sessionStorage.removeItem('fromMyBookings');

    const bookingIdParam = this.route.snapshot.paramMap.get('bookingId');

    if (!bookingIdParam) {
      this.router.navigate(['/search']);
      return;
    }

    this.bookingId = bookingIdParam;

    this.http.get(`http://localhost:5000/api/bookings/${this.bookingId}`)
      .subscribe({
        next: (response: any) => {

          this.bookingData = response;
          this.passengers = response.passengers || [];
          this.totals = { grandTotal: response.totalAmount };

          // ✅ Detect cancelled status
          this.isCancelled = response.status === 'CANCELLED';

          // Force change detection to avoid blank render
          this.cdr.detectChanges();

        },
        error: (err) => {
          console.error('Failed to load booking', err);
          // Temporarily prevent redirect to debug the issue
          alert('Failed to load booking. Check backend response.');
        }
      });
  }

  downloadTicket() {
    if (!this.bookingData?.bookingId) return;

    this.http.get(
      `http://localhost:5000/api/tickets/${this.bookingData.bookingId}/pdf`,
      { responseType: 'blob' }
    ).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.bookingData.bookingId}_Eticket.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('PDF download failed', err);
        alert('Failed to download E‑Ticket.');
      }
    });
  }

  goHome() {
    this.router.navigate(['/search']);
  }

  goBackToMyBookings() {
    this.router.navigate(['/my-bookings']);
  }

  // ✅ Copy to clipboard utility
  copyToClipboard(value: string | undefined) {
    if (!value) return;

    navigator.clipboard.writeText(value).then(() => {
      console.log('Copied to clipboard');
    });
  }
}
