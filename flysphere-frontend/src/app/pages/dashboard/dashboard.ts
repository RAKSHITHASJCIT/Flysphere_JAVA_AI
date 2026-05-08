import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  user: any;
  role: string | null = null;

  constructor(private auth: AuthService) {}

  ngOnInit() {
    // ✅ LocalStorage-based auth (no JWT, no profile API)
    const storedUser = localStorage.getItem('user');

    if (!storedUser) {
      window.location.href = '/login';
      return;
    }

    this.user = JSON.parse(storedUser);
    this.role = this.user.role;
  }

  logout() {
    this.auth.logout();
    window.location.href = '/login';
  }
}
