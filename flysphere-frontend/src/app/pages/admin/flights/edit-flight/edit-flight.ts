import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-edit-flight',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './edit-flight.html',
  styleUrls: ['./edit-flight.css']
})
export class EditFlight implements OnInit {

  flightId: number = 0;
  flight: any = {};
  // ✅ No longer needed (removed delay/reschedule logic)

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.flightId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadFlight();
  }

  loadFlight() {
    this.http.get<any>(`http://localhost:5000/api/flights/${this.flightId}`)
      .subscribe(response => {

        console.log('Edit flight API response:', response);

        // ✅ Handle different backend response shapes
        let data = response;

        if (Array.isArray(response)) {
          data = response[0];
        } else if (response?.rows) {
          data = response.rows[0];
        }

        if (!data) {
          console.error('No flight data found');
          return;
        }

    // ✅ Normalize backend camelCase response to lowercase keys used by form
    this.flight = {
      ...data,
      flightid: data.flightid ?? data.flightId ?? data.id,
      flightno: data.flightno ?? data.flightNo,
      airlinename: data.airlinename ?? data.airlineName,
      departureairport: data.departureairport ?? data.departureAirport,
      arrivalairport: data.arrivalairport ?? data.arrivalAirport,
      departuredate: data.departuredate ?? data.departureDate,
      arrivaldate: data.arrivaldate ?? data.arrivalDate,
      departuretime: data.departuretime ?? data.departureTime,
      arrivaltime: data.arrivaltime ?? data.arrivalTime,
      flighttype: data.flighttype ?? data.flightType,
      totaleconomyseats: data.totaleconomyseats ?? data.totalEconomySeats,
      totalbusinessseats: data.totalbusinessseats ?? data.totalBusinessSeats,
      totalfirstclassseats: data.totalfirstclassseats ?? data.totalFirstClassSeats,
      economyadultfare: data.economyadultfare ?? data.economyAdultFare,
      economychildfare: data.economychildfare ?? data.economyChildFare,
      businessadultfare: data.businessadultfare ?? data.businessAdultFare,
      businesschildfare: data.businesschildfare ?? data.businessChildFare,
      firstadultfare: data.firstadultfare ?? data.firstAdultFare,
      firstchildfare: data.firstchildfare ?? data.firstChildFare
    };
        // ✅ No original comparison needed

        // ✅ Normalize BOTH objects for accurate comparison
        if (this.flight.departuredate) {
          this.flight.departuredate = this.flight.departuredate.substring(0, 10);
        }

        if (this.flight.arrivaldate) {
          this.flight.arrivaldate = this.flight.arrivaldate.substring(0, 10);
        }

        if (this.flight.departuretime) {
          this.flight.departuretime = this.flight.departuretime.substring(0, 5);
        }

        if (this.flight.arrivaltime) {
          this.flight.arrivaltime = this.flight.arrivaltime.substring(0, 5);
        }

        // ✅ Force change detection in case router reuse prevents update
        this.cdr.detectChanges();

      });
  }

  // ✅ Minimum flight duration (in minutes)
  minDurationMinutes = 120; // 2 hours (adjust as needed)

  // ✅ Intelligent auto-calculation
  onDepartureChange() {
    if (!this.flight.departuredate || !this.flight.departuretime) {
      return;
    }

    const depDateTime = new Date(
      `${this.flight.departuredate}T${this.flight.departuretime}`
    );

    // Add minimum duration
    const arrivalDateTime = new Date(
      depDateTime.getTime() + this.minDurationMinutes * 60000
    );

    // Format date YYYY-MM-DD
    const yyyy = arrivalDateTime.getFullYear();
    const mm = String(arrivalDateTime.getMonth() + 1).padStart(2, '0');
    const dd = String(arrivalDateTime.getDate()).padStart(2, '0');

    this.flight.arrivaldate = `${yyyy}-${mm}-${dd}`;

    // Format time HH:mm
    const hh = String(arrivalDateTime.getHours()).padStart(2, '0');
    const min = String(arrivalDateTime.getMinutes()).padStart(2, '0');

    this.flight.arrivaltime = `${hh}:${min}`;
  }

  updateFlight() {

    // ✅ Always reset status to Scheduled on edit
    const payload = {
        airlineName: this.flight.airlinename,
        flightType: this.flight.flighttype,
        flightNo: this.flight.flightno,
        departureAirport: this.flight.departureairport,
        arrivalAirport: this.flight.arrivalairport,
        departureDate: this.flight.departuredate,
        arrivalDate: this.flight.arrivaldate,
        departureTime: this.flight.departuretime,
        arrivalTime: this.flight.arrivaltime,
        totalEconomySeats: this.flight.totaleconomyseats,
        totalBusinessSeats: this.flight.totalbusinessseats,
        totalFirstClassSeats: this.flight.totalfirstclassseats,
        economyAdultFare: this.flight.economyadultfare,
        economyChildFare: this.flight.economychildfare,
        businessAdultFare: this.flight.businessadultfare,
        businessChildFare: this.flight.businesschildfare,
        firstAdultFare: this.flight.firstadultfare,
        firstChildFare: this.flight.firstchildfare,
        aircraftType: this.flight.aircrafttype ?? this.flight.aircraftType,
        flightStatus: 'Scheduled'
      };

    this.http.put(`http://localhost:5000/api/flights/${this.flightId}`, payload)
      .subscribe({
        next: () => {
          alert('Flight updated successfully');
          this.router.navigate(['/admin/flights']);
        },
        error: (err) => {
          console.error('Update failed:', err);
          alert('Update failed. Check console for details.');
        }
      });
  }
}
