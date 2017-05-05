import { Component } from '@angular/core';

import { PickersPage } from '../pickers/pickers';
import { SettingsPage } from '../settings/settings';
import { ScanPage } from '../scan/scan';

@Component({
  templateUrl: 'tabs.html'
})
export class TabsPage {
  tab1Root: any = ScanPage;
  tab2Root: any = SettingsPage;
  tab3Root: any = PickersPage;

  constructor() { }
}
