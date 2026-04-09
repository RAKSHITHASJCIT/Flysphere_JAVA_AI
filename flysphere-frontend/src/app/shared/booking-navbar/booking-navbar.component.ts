import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-booking-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-navbar.component.html',
  styleUrls: ['./booking-navbar.component.css']
})
export class BookingNavbarComponent {

  currentStep: number = 1;

  constructor(private router: Router) {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateStep();
      });

    this.updateStep();
  }

  navigateTo(step: number) {
    if (step === 1) {
      this.router.navigate(['/booking']);
    } else if (step === 2) {
      this.router.navigate(['/review']);
    } else if (step === 3) {
      this.router.navigate(['/confirmation']);
    }
  }

  private updateStep() {
    const url = this.router.url;

    if (url.includes('review')) {
      this.currentStep = 2;
    } else if (url.includes('confirmation')) {
      this.currentStep = 3;
    } else {
      this.currentStep = 1;
    }
  }
}
