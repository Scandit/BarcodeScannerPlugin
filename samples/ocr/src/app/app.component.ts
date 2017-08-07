import { Component } from '@angular/core';
import { Platform } from 'ionic-angular';

import { ScanPage } from '../pages/scan/scan';

@Component({
  template: '<ion-nav [root]="rootPage"></ion-nav>',
})
export class MyApp {
  public rootPage: any = ScanPage;

  constructor(
    private platform: Platform,
  ) {
    this.platform.ready().then(() => {
      console.log('Platform is ready.');
    });
  }
}
