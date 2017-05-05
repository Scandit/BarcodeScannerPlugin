import { Component } from '@angular/core';
import { Platform } from 'ionic-angular';

import { TabsPage } from '../pages/tabs/tabs';

@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  public rootPage: any = TabsPage;

  constructor(
    private platform: Platform,
  ) {
    this.platform.ready().then(() => {
      // console.log('Platform is ready.');
    });
  }
}
