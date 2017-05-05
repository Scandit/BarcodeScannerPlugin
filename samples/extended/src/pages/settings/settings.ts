import { Component } from '@angular/core';

import { ScannerSettings } from '../../providers/scanner-settings';
import { Scanner } from '../../providers/scanner';

import { Enums } from '../../providers/enums';

@Component({
  selector: 'page-settings',
  templateUrl: 'settings.html'
})
export class SettingsPage {
  public scanSettings: ScanSettings;
  public uiSettings: UiSettings;

  public scanningArea;

  public viewfinderStyle: string;
  public cameraSwitchVisibility: string;

  private GuiStyle;
  private CameraSwitchVisibility;

  private shouldBeStopped: boolean;

  constructor(
    private settingsProvider: ScannerSettings,
    private scannerProvider: Scanner,
    private enums: Enums,
  ) {
    this.GuiStyle = this.enums.GuiStyle;
    this.CameraSwitchVisibility = this.enums.CameraSwitchVisibility;
    this.fetchSettings();

    this.setupActiveScanningArea();
  }

  public ionViewWillEnter(): void {
    this.stopScanner();
    this.fetchSettings();
  }

  public ionViewDidEnter(): void {
    this.shouldBeStopped = true;
    this.stopScanner();
  }

  public ionViewWillLeave(): void {
    this.shouldBeStopped = false;
    this.normalizeSettings();
    this.updateSettings();
  }

  public normalizeSettings(): void {
    this.normalizeSymbologies();
    this.updateScanningArea();
  }

  private updateSettings(): void {
    this.settingsProvider.updateScanSettings(this.scanSettings);
    this.settingsProvider.updateUiSettings(this.uiSettings);
    this.scanSettings.restricted = this.scanningArea.restricted;
  }

  private stopScanner(): void {
    const checkScannerIsStopped = () => {
      setTimeout(() => {
        if (this.shouldBeStopped && !this.scannerProvider.isStopped()) {
          this.stopScanner();
          console.warn('expected scanner state to be stopped, stopping scanner again...');
        }
      }, 500);
    };

    this.scannerProvider.stop();
    checkScannerIsStopped();
  }

  private fetchSettings(): void {
    this.scanSettings = this.settingsProvider.getScanSettings();
    this.uiSettings = this.settingsProvider.getUiSettings();

    this.viewfinderStyle = this.GuiStyle[this.uiSettings.viewfinder.style]
    this.cameraSwitchVisibility = this.CameraSwitchVisibility[this.uiSettings.cameraSwitch.visibility];
  }

  private setupActiveScanningArea(): void {
    this.scanningArea = {
      width: 0.5,
      height: 0.5,
      x: 0.5,
      y: 0.5,
      restricted: false,
    };
  }

  private normalizeSymbologies(): void {
    // If checksum is empty string, remove it.
    this.scanSettings.symbologies['msi-plessey'].checksums = this.scanSettings.symbologies['msi-plessey'].checksums.filter(v => v !== "");

    // If addons are enabled, the maxNumberOfCodes should be set to 2, to include the scanned addon code
    if (this.scanSettings.symbologies['two-digit-add-on'].enabled || this.scanSettings.symbologies['five-digit-add-on'].enabled) {
      this.scanSettings.maxNumberOfCodesPerFrame = 2;
    }

    // Convert the string value of the enum to it's integer value
    this.uiSettings.viewfinder.style = (this.GuiStyle[this.viewfinderStyle] as number);
    this.uiSettings.cameraSwitch.visibility = (this.CameraSwitchVisibility[this.cameraSwitchVisibility] as number);
  }

  private updateScanningArea(): void {
    this.scanSettings.restricted = this.scanningArea.restricted;

    if (this.scanningArea.restricted) {
      const newArea = new Scandit.Rect(
        this.scanningArea.x - this.scanningArea.width * this.scanningArea.x,
        this.scanningArea.y - this.scanningArea.x * this.scanningArea.height,
        this.scanningArea.width,
        this.scanningArea.height,
      );
      this.scanSettings.scanningHotSpot = new Scandit.Point(this.scanningArea.x, this.scanningArea.y);
      this.scanSettings.activeScanningAreaPortrait = newArea;
      this.scanSettings.activeScanningAreaLandscape = newArea;
    } else {
      const newArea = new Scandit.Rect(0, 0, 1, 1);
      this.scanSettings.scanningHotSpot = new Scandit.Point(0.5, 0.5);
      this.scanSettings.activeScanningAreaPortrait = newArea;
      this.scanSettings.activeScanningAreaLandscape = newArea;
      this.settingsProvider.clampActiveScanningArea(this.scannerProvider.portraitConstraints, this.scannerProvider.landscapeConstraints);
    }
  }
}
