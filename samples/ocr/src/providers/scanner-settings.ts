import { Injectable } from '@angular/core';
import { Events } from 'ionic-angular';

import { Enums } from './enums';

@Injectable()
export class ScannerSettings {
  public event: {
    settingsChanged: string
  };

  public scanningMode;

  private settings: ScanSettings;
  private uiSettings: UiSettings;

  private scanningModeRegex = {};

  constructor(
    private events: Events,
    private Enums: Enums,
  ) {
    this.event = {
      settingsChanged: 'settings:changed',
    };

    this.scanningModeRegex[this.Enums.ScanningMode[this.Enums.ScanningMode.iban]] =
      '([A-Z]{2}[A-Z0-9]{2}\\s([A-Z0-9]{4}\\s){4,}([A-Z0-9]{1,4}))';
    this.scanningModeRegex[this.Enums.ScanningMode[this.Enums.ScanningMode.gs1]] =
      '((\\(01\\)[0-9]{13,14})(\\s*(\\(10\\)[0-9a-zA-Z]{1,20})|(\\(11\\)[0-9]{6})|(\\(17\\)[0-9]{6})|(\\(21\\)[0-9a-zA-Z]{1,20}))+)';
    this.scanningModeRegex[this.Enums.ScanningMode[this.Enums.ScanningMode.price]] =
      '((^|\\s+)[0-9]{1,}\\.[0-9]{1,}(\\s+|$))';
    console.log(this.scanningModeRegex);

    this.scanningMode = this.Enums.ScanningMode.iban;

    this.settings = this.getDefaultScanSettings();
    this.uiSettings = this.getDefaultUiSettings();

  }

  public getScanSettings(): ScanSettings {
    return this.settings;
  }

  public updateScanSettings(newSettings: ScanSettings): void {
    this.settings = newSettings;
    this.emitSettingsChanged();
  }

  public getUiSettings(): UiSettings {
    return this.uiSettings;
  }

  public updateUiSettings(newSettings: UiSettings): void {
    this.uiSettings = newSettings;
    this.emitSettingsChanged();
  }

  public setScanningMode(newScanningMode): void {
    console.log(newScanningMode);
    this.scanningMode = newScanningMode;

    this.settings.textRecognition.regex = this.scanningModeRegex[this.Enums.ScanningMode[this.scanningMode]];

    console.log(this.settings.textRecognition);
    this.updateScanSettings(this.settings);
  }

  private getDefaultScanSettings(): ScanSettings {
    let settings = new Scandit.ScanSettings();
    settings.recognitionMode = Scandit.ScanSettings.RecognitionMode.TEXT;
    settings.textRecognition = {}; // new Scandit.TextRecognitionSettings()

    settings.activeScanningAreaPortrait = new Scandit.Rect(0, 0.4, 0.9, 0.1); // default active scanning area
    settings.scanningHotSpot = new Scandit.Point(
      0.5, // x
      0.5, // y
    );
    settings.activeScanningAreaPortrait.y = settings.scanningHotSpot.y - (settings.activeScanningAreaPortrait.height / 2)
    settings.activeScanningAreaLandscape = settings.activeScanningAreaPortrait;
    console.log(settings.scanningHotSpot, settings.activeScanningAreaPortrait, settings.activeScanningAreaLandscape);

    settings.textRecognition.areaPortrait = settings.activeScanningAreaPortrait;
    settings.textRecognition.areaLandscape = settings.activeScanningAreaLandscape;

    settings.textRecognition.regex = this.scanningModeRegex[this.Enums.ScanningMode[this.scanningMode]];

    console.log(settings.textRecognition);

    return settings;
  }

  private getDefaultUiSettings(): UiSettings {
    return {
      viewfinder: {
        style: this.Enums.GuiStyle.default,
        portrait: {
          width: 0.9,
          height: 0.1,
        },
        landscape: {
          width: 0.9,
          height: 0.1,
        },
      },
      searchBar: false,
      feedback: {
        beep: true,
        vibrate: true,
      },
      torch: {
        enabled: false,
        offset: {
          left: 15,
          top: 15,
        }
      },
      cameraSwitch: {
        visibility: this.Enums.CameraSwitchVisibility.never,
        offset: {
          right: 15,
          top: 15,
        },
      },
    };
  }

  private emitSettingsChanged(): void {
    this.events.publish(this.event.settingsChanged, this.settings, this.uiSettings);
  }
}
