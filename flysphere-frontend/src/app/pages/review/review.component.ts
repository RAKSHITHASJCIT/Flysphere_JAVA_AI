import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BookingNavbarComponent } from '../../shared/booking-navbar/booking-navbar.component';

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [CommonModule, BookingNavbarComponent],
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.css']
})
export class ReviewComponent implements OnInit {

  bookingData: any;
  passengers: any[] = [];
  contact: any;
  totals: any;

  // ✅ Travel Protection flags (from Booking page)
  departureInsurance: boolean = false;
  returnInsurance: boolean = false;

  constructor(private router: Router, private http: HttpClient) {}

  ngOnInit(): void {
    const state = history.state;

    if (!state || !state.bookingData) {
      this.router.navigate(['/search']);
      return;
    }

    this.bookingData = state.bookingData;
    this.passengers = state.passengers || [];
    this.contact = state.contact || {};
    this.totals = state.totals || {};

    // ✅ Restore Travel Protection selections
    this.departureInsurance = state.departureInsurance || false;
    this.returnInsurance = state.returnInsurance || false;
  }

 confirmBooking() {
  const isRound = this.bookingData?.tripType === 'round';

  const outboundFlightId = isRound
    ? this.bookingData?.departure?.flight?.id
    : this.bookingData?.flight?.id;

  const returnFlightId = isRound
    ? this.bookingData?.return?.flight?.id
    : null;

  // ✅ Get cabin classes separately for departure and return
  const outboundCabinClass =
    isRound
      ? this.bookingData?.departure?.selectedCabinClass
      : this.bookingData?.selectedCabinClass;

  const returnCabinClass =
    isRound
      ? this.bookingData?.return?.selectedCabinClass
      : null;

  // ✅ Get logged-in user from localStorage
  const storedUser = localStorage.getItem('user');
  let loggedInUserId: number | null = null;

  if (storedUser) {
    const user = JSON.parse(storedUser);

    // ✅ Force correct DB column mapping (users table uses 'id')
    loggedInUserId = user?.id;

    console.log('Booking will be saved for userId:', loggedInUserId);
  }

  // ✅ Build segment-level add-ons from passenger + insurance selections
  const firstPassenger = this.passengers?.length ? this.passengers[0] : null;

  const outboundAddOns = {
    seatPreference: firstPassenger?.outboundSeat || null,
    mealPreference: firstPassenger?.outboundMeal || null,
    extraBaggage: firstPassenger?.baggage || false,
    travelProtection: this.departureInsurance || false
  };

  // ✅ Use return-specific fields (not outbound ones)
  const returnAddOns = isRound ? {
    seatPreference: firstPassenger?.returnSeat || null,
    mealPreference: firstPassenger?.returnMeal || null,
    extraBaggage: firstPassenger?.baggage || false,
    travelProtection: this.returnInsurance || false
  } : null;

  const payload: any = {
    userId: loggedInUserId,
    outboundFlightId: outboundFlightId,
    returnFlightId: returnFlightId,
    passengers: this.passengers,
    totalAmount: this.totals?.grandTotal,
    outboundCabinClass: outboundCabinClass || 'Economy',
    returnCabinClass: returnCabinClass,

    // ✅ Contact Information
    contactEmail: this.contact?.email,
    contactPhone: this.contact?.phone,

    // ✅ Segment-level add-ons
    outboundAddOns: outboundAddOns,
    returnAddOns: returnAddOns
  };

  console.log('🚀 Sending booking payload:', payload);

  this.http.post('http://localhost:5000/api/bookings', payload)
    .subscribe({
      next: (response: any) => {
        console.log('✅ Booking API response:', response);

        if (!response || !response.bookingId) {
          alert('Booking succeeded but response format is unexpected.');
          return;
        }

        this.router.navigate(
          ['/confirmation', response.bookingId],
          {
            state: {
              bookingData: response,
              passengers: response.passengers || [],
              totals: { grandTotal: response.totalAmount }
            }
          }
        );
      },
      error: (error) => {
        console.error('❌ Booking API failed:', error);
        alert('Booking failed. Please check backend server or try again.');
      }
    });
}


  goBackToBooking() {
    this.router.navigate(['/booking'], {
      state: {
        bookingData: this.bookingData,
        passengers: this.passengers,
        contact: this.contact,
        totals: this.totals
      }
    });
  }
}
