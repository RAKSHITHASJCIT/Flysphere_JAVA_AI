import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../services/auth';
import { BookingNavbarComponent } from '../../shared/booking-navbar/booking-navbar.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, FormsModule, BookingNavbarComponent],
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css']
})
export class MyBookingsComponent implements OnInit {

  expandedBookingId: string | null = null;

  togglePreview(bookingId: string): void {
    this.expandedBookingId =
      this.expandedBookingId === bookingId ? null : bookingId;
  }

  bookings: any[] = [];
  userId!: number;
  loading = true;

  // ✅ Filters
  searchText: string = '';
  selectedStatus: string = '';

  // ✅ Pagination
  currentPage: number = 1;
  pageSize: number = 5;

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const user = JSON.parse(storedUser);

      // ✅ Handle both possible key names
      this.userId = user?.id ?? user?.userId;

      console.log('Logged in user object:', user);
      console.log('Resolved userId:', this.userId);
    }

    if (this.userId) {
      this.loadBookings();
    } else {
      this.loading = false;
    }
  }

  loadBookings(): void {
    this.bookingService.getUserBookings(this.userId)
      .subscribe({
        next: (data) => {
          console.log('Bookings fetched from API:', data);

          // ✅ Sort latest bookings first (by createdAt descending)
          this.bookings = (data || []).sort((a: any, b: any) => {
            return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
          });

          // ✅ When landing on My Bookings page show ALL bookings
          this.currentPage = 1;
          this.pageSize = this.bookings.length || 5;

          this.loading = false;
        },
        error: (err) => {
          console.error('Error fetching bookings:', err);
          this.loading = false;
        }
      });
  }

  viewDetails(booking: any): void {

    // ✅ Persist navigation source
    sessionStorage.setItem('fromMyBookings', 'true');

    this.router.navigate(
      ['/confirmation', booking.bookingId]
    );
  }

  // ✅ Download ticket PDF (force browser navigation)
  downloadTicket(bookingId: string): void {
    const url = `http://localhost:5000/api/tickets/${bookingId}/pdf`;

    // Force browser to navigate to file (guaranteed request)
    window.location.href = url;
  }

  // ✅ Display full route including outbound + return (round trip support)
  getRoute(booking: any): string {

    if (booking.segments && booking.segments.length > 0) {

      const routes: string[] = booking.segments
        .filter((seg: any) => seg.flight)
        .map((seg: any) => {
          const dep = seg.flight.departureAirport;
          const arr = seg.flight.arrivalAirport;
          return dep && arr ? `${dep} → ${arr}` : '';
        })
        .filter((r: string) => r !== '');

      if (routes.length > 0) {
        return routes.join('  |  ');
      }
    }

    return '—';
  }

  // ✅ Get cabin types with proper round trip handling
  getCabinTypes(booking: any): string {

    if (!booking.segments || booking.segments.length === 0) {
      return 'Economy';
    }

    const cabins = booking.segments
      .map((seg: any) => seg.cabinType || 'Economy')
      .filter((c: string) => c);

    if (cabins.length === 1) {
      // One way
      return cabins[0];
    }

    if (cabins.length >= 2) {

      const [departureCabin, returnCabin] = cabins;

      // ✅ If both same → show only one
      if (departureCabin === returnCabin) {
        return departureCabin;
      }

      // ✅ If different → show both clearly
      return `${departureCabin} (Departure) / ${returnCabin} (Return)`;
    }

    return 'Economy';
  }

  // ✅ Filtered + Paginated bookings logic
  filteredBookings(): any[] {

    const filtered = this.bookings.filter(b => {

      const matchesSearch =
        !this.searchText ||
        b.bookingId?.toLowerCase().includes(this.searchText.toLowerCase());

      const matchesStatus =
        !this.selectedStatus ||
        b.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });

    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;

    return filtered.slice(start, end);
  }

  totalPages(): number {
    const filteredCount = this.bookings.filter(b =>
      (!this.searchText || b.bookingId?.toLowerCase().includes(this.searchText.toLowerCase())) &&
      (!this.selectedStatus || b.status === this.selectedStatus)
    ).length;

    return Math.ceil(filteredCount / this.pageSize);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage = page;
    }
  }

  resetFilters(): void {
    this.searchText = '';
    this.selectedStatus = '';

    // ✅ Reset pagination back to first page
    this.currentPage = 1;
  }

  cancelBooking(bookingCode: string): void {
    const confirmCancel = confirm('Are you sure you want to cancel this booking?');
    if (!confirmCancel) return;

    // ✅ Optimistic UI update (no double click required)
    this.bookings = this.bookings.map(b =>
      b.bookingId === bookingCode
        ? { ...b, status: 'CANCELLED' }
        : b
    );

    this.bookingService.cancelBooking(bookingCode)
      .subscribe({
        next: () => {
          // ✅ No need to reload entire list
          console.log('Booking cancelled successfully');
        },
        error: (err) => {
          console.error('Cancel failed, reverting UI', err);

          // revert back if API fails
          this.bookings = this.bookings.map(b =>
            b.bookingId === bookingCode
              ? { ...b, status: 'CONFIRMED' }
              : b
          );
        }
      });
  }
}
