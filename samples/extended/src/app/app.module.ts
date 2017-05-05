import { NgModule, ErrorHandler } from '@angular/core';
import { IonicApp, IonicModule, IonicErrorHandler } from 'ionic-angular';
import { MyApp } from './app.component';

import { PickersPage } from '../pages/pickers/pickers';
import { SettingsPage } from '../pages/settings/settings';
import { ScanPage } from '../pages/scan/scan';

import { TabsPage } from '../pages/tabs/tabs';

import { Enums } from '../providers/enums';
import { Scanner } from '../providers/scanner';
import { ScannerSettings } from '../providers/scanner-settings';

@NgModule({
  declarations: [
    MyApp,
    ScanPage,
    SettingsPage,
    PickersPage,
    TabsPage
  ],
  imports: [
    IonicModule.forRoot(MyApp)
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    ScanPage,
    SettingsPage,
    PickersPage,
    TabsPage
  ],
  providers: [
    { provide: ErrorHandler, useClass: IonicErrorHandler },
    Scanner,
    ScannerSettings,
    Enums,
  ]
})
export class AppModule {}
