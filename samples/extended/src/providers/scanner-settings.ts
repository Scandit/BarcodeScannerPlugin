import { Injectable } from '@angular/core';
import { Events } from 'ionic-angular';

import { Enums } from './enums';

@Injectable()
export class ScannerSettings {
  public event: {
    settingsChanged: string
  };

  public maxScanningArea;
  public restricted: boolean;

  private settings: ScanSettings;
  private uiSettings: UiSettings;

  constructor(
    private events: Events,
    private enums: Enums,
  ) {
    this.maxScanningArea = { portrait: { width: 1, height: 1 }, landscape: { width: 1, height: 1 } };
    this.restricted = false;

    this.event = {
      settingsChanged: 'settings:changed',
    };

    this.settings = this.getDefaultScanSettings();
    this.uiSettings = this.getDefaultUiSettings();
  }

  public getScanSettings(): ScanSettings {
    return this.settings;
  }

  public updateScanSettings(settings: ScanSettings): void {
    this.settings = settings;
    this.emitSettingsChanged();
  }

  public getUiSettings(): UiSettings {
    return this.uiSettings;
  }

  public updateUiSettings(settings: UiSettings): void {
    this.uiSettings = settings;
    this.emitSettingsChanged();
  }

  public clampActiveScanningArea(portraitConstraints: Constraints, landscapeConstraints: Constraints): void {
    const isString = x => typeof x === 'string';

    const getScreenDimensions = () => {
      return {
        portrait: {
          height: Math.max(window.innerHeight, window.innerWidth),
          width: Math.min(window.innerHeight, window.innerWidth),
        },
        landscape: {
          height: Math.min(window.innerHeight, window.innerWidth),
          width: Math.max(window.innerHeight, window.innerWidth),
        }
      };
    }

    const getMargins: () => {portrait: Margins, landscape: Margins} = () => {
      const screen = getScreenDimensions();
      return {
        portrait: {
          top:    isString(portraitConstraints.topMargin)    ? parseInt((portraitConstraints.topMargin as string), 10) / 100 * screen.portrait.height    : (portraitConstraints.topMargin as number),
          right:  isString(portraitConstraints.rightMargin)  ? parseInt((portraitConstraints.rightMargin as string), 10) / 100 * screen.portrait.width   : (portraitConstraints.rightMargin as number),
          bottom: isString(portraitConstraints.bottomMargin) ? parseInt((portraitConstraints.bottomMargin as string), 10) / 100 * screen.portrait.height : (portraitConstraints.bottomMargin as number),
          left:   isString(portraitConstraints.leftMargin)   ? parseInt((portraitConstraints.leftMargin as string), 10) / 100 * screen.portrait.width    : (portraitConstraints.leftMargin as number),
        },
        landscape: {
          top:    isString(landscapeConstraints.topMargin)    ? parseInt((landscapeConstraints.topMargin as string), 10) / 100 * screen.landscape.height    : (landscapeConstraints.topMargin as number),
          right:  isString(landscapeConstraints.rightMargin)  ? parseInt((landscapeConstraints.rightMargin as string), 10) / 100 * screen.landscape.width   : (landscapeConstraints.rightMargin as number),
          bottom: isString(landscapeConstraints.bottomMargin) ? parseInt((landscapeConstraints.bottomMargin as string), 10) / 100 * screen.landscape.height : (landscapeConstraints.bottomMargin as number),
          left:   isString(landscapeConstraints.leftMargin)   ? parseInt((landscapeConstraints.leftMargin as string), 10) / 100 * screen.landscape.width    : (landscapeConstraints.leftMargin as number),
        }
      };
    }

    if (!this.restricted) {
      const screen = getScreenDimensions();
      const margins = getMargins();

      // Portrait calculations
      const portrait = {
        x: 0,
        y: 0,
        width: (screen.portrait.width - margins.portrait.left - margins.portrait.right) / screen.portrait.width,
        height: (screen.portrait.height - margins.portrait.top - margins.portrait.bottom) / screen.portrait.height,
      };
      portrait.x = (1 - portrait.width) / 2; // coordinates go from 0-1
      portrait.y = (1 - portrait.height) / 2; // coordinates go from 0-1
      let portraitArea = new Scandit.Rect(portrait.x, portrait.y, portrait.width, portrait.height);

      // Landscape calculations
      const landscape = {
        x: 0,
        y: 0,
        width: (screen.landscape.width - margins.landscape.left - margins.landscape.right) / screen.landscape.width,
        height: (screen.landscape.height - margins.landscape.top - margins.landscape.bottom) / screen.landscape.height,
      };
      landscape.x = (1 - landscape.width) / 2; // coordinates go from 0-1
      landscape.y = (1 - landscape.height) / 2; // coordinates go from 0-1
      let landscapeArea = new Scandit.Rect(landscape.x, landscape.y, landscape.width, landscape.height);

      // Edge-case (T/B & L/R cropped picker)
      let forceSet: boolean = false;
      if (
        (margins.portrait.top > 0 && margins.portrait.right > 0 && margins.portrait.bottom > 0 && margins.portrait.right > 0) ||
        (margins.landscape.top > 0 && margins.landscape.right > 0 && margins.landscape.bottom > 0 && margins.landscape.right > 0)
      ) {
        portraitArea = new Scandit.Rect(0, 0, 1, 1);
        landscapeArea = new Scandit.Rect(0, 0, 1, 1);
        forceSet = true;
      }

      // Update active scanning area
      if (
        forceSet ||
        this.settings.activeScanningAreaPortrait.width >= portraitArea.width ||
        this.settings.activeScanningAreaPortrait.height >= portraitArea.height ||
        this.settings.activeScanningAreaLandscape.width >= landscapeArea.width ||
        this.settings.activeScanningAreaLandscape.height >= landscapeArea.height
      ) {
        this.settings.activeScanningAreaPortrait = portraitArea;
        this.settings.activeScanningAreaLandscape = landscapeArea;
        this.updateScanSettings(this.settings);
      } else {
        this.maxScanningArea.portrait = portraitArea;
        this.maxScanningArea.landscape = landscapeArea;
      }
    }
  }

  private getDefaultScanSettings(): ScanSettings {
    let settings = new Scandit.ScanSettings();
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN13, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPCA, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.EAN8, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.UPCE, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.CODE39, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.CODE128, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.ITF, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.QR, true);
    settings.setSymbologyEnabled(Scandit.Barcode.Symbology.DATA_MATRIX, true);

    settings.symbologies['msi-plessey'].checksums = ['mod10'];

    return settings;
  }

  private getDefaultUiSettings(): UiSettings {
    return {
      viewfinder: {
        style: this.enums.GuiStyle.default,
        portrait: {
          width: 0.8,
          height: 0.4,
        },
        landscape: {
          width: 0.6,
          height: 0.4,
        },
      },
      searchBar: false,
      feedback: {
        beep: true,
        vibrate: true,
      },
      torch: {
        enabled: true,
        offset: {
          left: 15,
          top: 15,
        }
      },
      cameraSwitch: {
        visibility: this.enums.CameraSwitchVisibility.never,
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
