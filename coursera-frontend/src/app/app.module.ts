// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component'; // Keep the import for usage in main.ts

@NgModule({
  // REMOVED: declarations array is not needed for standalone components
  declarations: [],
  imports: [
    BrowserModule,
    HttpClientModule,
    // REMOVED: AppComponent from imports array here.
    // Standalone components are imported directly where they are used (e.g., in other components' imports array)
    // or bootstrapped directly in main.ts.
  ],
  providers: [],
  // REMOVED: bootstrap array is not used when bootstrapping with bootstrapApplication
  bootstrap: []
})
export class AppModule { }
