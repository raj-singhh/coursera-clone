import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.component.css'
})
export class App {
  protected readonly title = signal('coursera-frontend');
}
