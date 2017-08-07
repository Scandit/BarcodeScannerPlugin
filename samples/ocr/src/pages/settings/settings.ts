import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';

import { ScannerSettings } from '../../providers/scanner-settings';
import { Enums } from '../../providers/enums';

@Component({
  selector: 'page-settings',
  templateUrl: 'settings.html',
})
export class SettingsPage {
  public scanPosition: 'top' | 'center' | 'bottom' = 'center';
  public scanMode: string; // Enums.ScanningMode

  private yPositions = {
    top:    0.25,
    center: 0.5,
    bottom: 0.75,
    0.25:   'top',
    0.5:    'center',
    0.75:   'bottom',
  }

  constructor(
    public nav: NavController,
    private settings: ScannerSettings,
    private Enums: Enums,
  ) {
    this.scanPosition = this.yPositions[this.settings.getScanSettings().scanningHotSpot.y] || 'center';
    this.scanMode = this.Enums.ScanningMode[this.settings.scanningMode];
  }

  public ionViewWillLeave(): void {
    this.setScanningMode();
    this.setScanningPosition();
  }

  private setScanningMode(): void {
    this.settings.setScanningMode(this.Enums.ScanningMode[this.scanMode]);
  }

  private setScanningPosition(): void {
    let s = this.settings.getScanSettings();
    s.scanningHotSpot = new Scandit.Point(
      0.5,                                     // x
      this.yPositions[this.scanPosition],      // y
    );
    s.activeScanningAreaPortrait.y = s.scanningHotSpot.y - (s.activeScanningAreaPortrait.height / 2)
    s.activeScanningAreaLandscape = s.activeScanningAreaPortrait;

    console.log(s.scanningHotSpot, s.activeScanningAreaPortrait, s.activeScanningAreaLandscape)
    this.settings.updateScanSettings(s);
    console.log('set scanning hotspot to ' + this.scanPosition, s.scanningHotSpot);
  }

}
