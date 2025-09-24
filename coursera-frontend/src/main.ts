import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';
import './styles.css';

bootstrapApplication(AppComponent, appConfig)
  .then(ref => {
    // For debugging
    console.log('Application bootstrapped successfully');
  })
  .catch(err => console.error('Error bootstrapping app:', err));
