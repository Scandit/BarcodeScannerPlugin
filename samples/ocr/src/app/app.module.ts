import { NgModule, ErrorHandler } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';

import { MyApp } from './app.component';
import { ScanPage } from '../pages/scan/scan';
import { SettingsPage } from '../pages/settings/settings';

import { Enums } from '../providers/enums';
import { Scanner } from '../providers/scanner';
import { ScannerSettings } from '../providers/scanner-settings';

@NgModule({
  declarations: [
    MyApp,
    ScanPage,
    SettingsPage,
  ],
  imports: [
    BrowserModule,
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    ScanPage,
    SettingsPage,
  ],
  providers: [
    { provide: ErrorHandler, useClass: IonicErrorHandler },
    Enums,
    Scanner,
    ScannerSettings,
  ]
})
export class AppModule {}
