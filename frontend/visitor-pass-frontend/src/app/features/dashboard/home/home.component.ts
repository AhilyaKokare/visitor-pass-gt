import { Component, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProfileService } from '../../../core/services/profile.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, TitleCasePipe],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  user: User | null = null;
  currentTime = new Date();

  constructor(private profileService: ProfileService) {}

  ngOnInit(): void {
    this.profileService.getMyProfile().subscribe(profile => {
      this.user = profile;
    });
    setInterval(() => this.currentTime = new Date(), 1000);
  }
}
