import { Injectable } from '@angular/core';
import { Events } from 'ionic-angular';

import { ScannerSettings } from './scanner-settings';

import { Enums } from './enums';

@Injectable()
export class Scanner {
  public event: {
    scan: string,
    stateChange: string,
  };

  public state;
  public picker: BarcodePicker;

  public portraitConstraints: Constraints;
  public landscapeConstraints: Constraints;

  private ScannerState;

  constructor(
    private events: Events,
    private settings: ScannerSettings,
    private enums: Enums,
  ) {
    this.ScannerState = this.enums.ScannerState;
    this.event = {
      scan: 'scanner:scan',
      stateChange: 'scanner:stateChange',
    };

    this.events.subscribe(this.settings.event.settingsChanged, (newScanSettings: ScanSettings, newUiSettings: UiSettings) => {
      if (this.picker) {
        this.picker.applyScanSettings(newScanSettings);
        this.applyUiSettings(newUiSettings, this.picker.getOverlayView());
      }
    });

    this.setAppKey();

    this.createNewPicker();
  }

  public start(): void {
    if (this.isFullscreen()) {
      this.createNewPicker();
    }

    if (this.isStopped()) {
      this.show();
    }

    this.startScanning();
  }

  public pause(): void {
    this.picker.pauseScanning();
  }

  public resume(): void {
    this.picker.resumeScanning();
  }

  public stop(): void {
    if (!this.isStopped()) {
      this.picker.cancel();
    }
  }

  public startScanning(): void {
    this.picker.startScanning();
  }

  public isStopped(): boolean {
    return this.state === this.ScannerState.stopped;
  }

  public clampActiveScanningArea(): void {
    this.settings.clampActiveScanningArea(this.portraitConstraints, this.landscapeConstraints);
  }

  public setConstraints(top: Constraint, right: Constraint, bottom: Constraint, left: Constraint, animationDuration: number = 0): void {
    this.setPortraitConstraints(top, right, bottom, left, animationDuration);
    this.setLandscapeConstraints(top, right, bottom, left, animationDuration);
  }

  public setPortraitConstraints(top: Constraint, right: Constraint, bottom: Constraint, left: Constraint, animationDuration: number = 0): void {
    const newConstraints = new Scandit.Constraints();
    newConstraints.topMargin = top;
    newConstraints.rightMargin = right;
    newConstraints.bottomMargin = bottom;
    newConstraints.leftMargin = left;

    if (
      !this.portraitConstraints ||
      newConstraints.topMargin !== this.portraitConstraints.topMargin ||
      newConstraints.rightMargin !== this.portraitConstraints.rightMargin ||
      newConstraints.bottomMargin !== this.portraitConstraints.bottomMargin ||
      newConstraints.leftMargin !== this.portraitConstraints.leftMargin
    ) {
      this.portraitConstraints = newConstraints;
      this.applyConstraints(animationDuration);
    }
  }

  public setLandscapeConstraints(top: Constraint, right: Constraint, bottom: Constraint, left: Constraint, animationDuration: number = 0): void {
    const newConstraints = new Scandit.Constraints();
    newConstraints.topMargin = top;
    newConstraints.rightMargin = right;
    newConstraints.bottomMargin = bottom;
    newConstraints.leftMargin = left;

    if (
      !this.landscapeConstraints ||
      newConstraints.topMargin !== this.landscapeConstraints.topMargin ||
      newConstraints.rightMargin !== this.landscapeConstraints.rightMargin ||
      newConstraints.bottomMargin !== this.landscapeConstraints.bottomMargin ||
      newConstraints.leftMargin !== this.landscapeConstraints.leftMargin
    ) {
      this.landscapeConstraints = newConstraints;
      this.applyConstraints(animationDuration);
    }
  }

  private createNewPicker(): void {
    this.picker = new Scandit.BarcodePicker(this.settings.getScanSettings());
    this.picker.continuousMode = true;
    this.state = this.ScannerState.stopped;
  }

  private show(): void {
    this.applyUiSettings(this.settings.getUiSettings(), this.picker.getOverlayView());
    this.picker.show({
      didScan: this.onScan.bind(this),
      didChangeState: this.onStateChange.bind(this),
      didCancel: this.onCancel.bind(this),
      didManualSearch: this.onManualInput.bind(this),
    });
  }

  private applyUiSettings(uiSettings: UiSettings, overlay): void {
    overlay.setBeepEnabled(uiSettings.feedback.beep);
    overlay.setVibrateEnabled(uiSettings.feedback.vibrate);

    overlay.showSearchBar(uiSettings.searchBar);
    if (uiSettings.searchBar) {
      overlay.setSearchBarPlaceholderText('Manual barcode entry');
    }

    overlay.setTorchEnabled(uiSettings.torch.enabled);
    overlay.setTorchButtonMarginsAndSize(
      uiSettings.torch.offset.left,
      uiSettings.torch.offset.top,
      40,
      40,
    );

    overlay.setCameraSwitchVisibility(uiSettings.cameraSwitch.visibility);
    overlay.setCameraSwitchButtonMarginsAndSize(
      uiSettings.cameraSwitch.offset.right,
      uiSettings.cameraSwitch.offset.top,
      40,
      40,
    );

    overlay.setGuiStyle(uiSettings.viewfinder.style);
    overlay.setViewfinderDimension(
      uiSettings.viewfinder.portrait.width,
      uiSettings.viewfinder.portrait.height,
      uiSettings.viewfinder.landscape.width,
      uiSettings.viewfinder.landscape.height,
    );
  }

  private onScan(session): void {
    this.events.publish(this.event.scan, session);
  }

  private onManualInput(content): void {
    console.log(content);
  }

  private onCancel(error): void {
    // console.log(error);
  }

  private onStateChange(state): void {
    this.changeState(state);
  }

  private changeState(state): void {
    this.state = state;
    this.events.publish(this.event.stateChange, state);
  }

  private applyConstraints(animationDuration: number = 0): void {
    this.picker.setConstraints(this.portraitConstraints, this.landscapeConstraints, animationDuration);
  }

  private isFullscreen(): boolean {
    return (this.portraitConstraints.topMargin === 0 || this.portraitConstraints.topMargin === '0%')
      && (this.portraitConstraints.rightMargin === 0 || this.portraitConstraints.rightMargin === '0%')
      && (this.portraitConstraints.leftMargin === 0 || this.portraitConstraints.leftMargin === '0%')
      && (this.portraitConstraints.bottomMargin === 0 || this.portraitConstraints.bottomMargin === '0%')
      && (this.landscapeConstraints.topMargin === 0 || this.landscapeConstraints.topMargin === '0%')
      && (this.landscapeConstraints.rightMargin === 0 || this.landscapeConstraints.rightMargin === '0%')
      && (this.landscapeConstraints.leftMargin === 0 || this.landscapeConstraints.leftMargin === '0%')
      && (this.landscapeConstraints.bottomMargin === 0 || this.landscapeConstraints.bottomMargin === '0%');
  }

  private setAppKey(): void {
    Scandit.License.setAppKey("--- ENTER YOUR SCANDIT APP KEY HERE ---");
  }
}
