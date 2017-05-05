import { Component, NgZone, ViewChild } from '@angular/core';
import { Content, Events, NavController, Platform, ViewController } from 'ionic-angular';

import { Scanner } from '../../providers/scanner';

import { Enums } from '../../providers/enums';

@Component({
  selector: 'page-scan',
  templateUrl: 'scan.html'
})
export class ScanPage {
  @ViewChild(Content) content: Content;

  public isScanning: boolean;
  public scannedCodes;

  public scannerPlaceholderHeight = '200px';

  private onScanHandler: Function;
  private onStateChangeHandler: Function;

  private shouldBeScanning: boolean;

  private ScannerState;

  constructor(
    private zone: NgZone,
    private platform: Platform,
    private events: Events,
    private nav: NavController,
    private view: ViewController,
    private scanner: Scanner,
    private enums: Enums,
  ) {
    this.ScannerState = this.enums.ScannerState;
    this.onScanHandler = (session) => {
      this.handleScan(session);
    }

    this.onStateChangeHandler = (state) => {
      this.handleStateChange(state);
    }

    this.isScanning = false;
  }

  public ionViewWillEnter(): void {
    this.subscribe();
  }

  public ionViewDidEnter(): void {
    this.shouldBeScanning = true;
    this.startScanner();
    this.setScannerConstraints();
  }

  public ionViewWillLeave(): void {
    this.shouldBeScanning = false;
    this.unsubscribe();
  }

  public continueScanning(): void {
    this.setScannedCodes(undefined);
    this.scanner.resume();
  }

  private subscribe(): void {
    this.events.subscribe(this.scanner.event.scan, this.onScanHandler);
    this.events.subscribe(this.scanner.event.stateChange, this.onStateChangeHandler);
  }

  private unsubscribe(): void {
    this.events.unsubscribe(this.scanner.event.scan, this.onScanHandler);
    this.events.unsubscribe(this.scanner.event.stateChange, this.onStateChangeHandler);
  }

  private startScanner(): void {
    const checkScannerIsActive = () => {
      setTimeout(() => {
        if (this.shouldBeScanning && this.scanner.isStopped()) {
          this.startScanner();
          console.warn('expected scanner state to be active, starting scanner again...');
          // this could happen e.g. when when stop/start is called quickly after each other, such as when changing tabs quickly
        }
      }, 1000);
    };

    this.setScannedCodes(undefined);
    this.setScannerConstraints();

    this.scanner.start();

    checkScannerIsActive();
  }

  private setScannerConstraints(): void {
    const top = this.content.contentTop;
    if (top === undefined) {
      setTimeout(this.setScannerConstraints.bind(this), 500);
    }

    const topConstraint = top || 0;
    const rightConstraint = 0;
    const bottomConstraint = '50%';
    const leftConstraint = 0;

    this.scannerPlaceholderHeight = `calc(50vh - ${top}px)`;

    this.scanner.setConstraints(topConstraint, rightConstraint, bottomConstraint, leftConstraint);
    this.scanner.clampActiveScanningArea();
  }

  private handleScan(session): void {
    this.setScannedCodes(session.newlyRecognizedCodes);
    this.scanner.pause();
  }

  private setScannedCodes(codes: any[]): void {
    this.zone.run(() => {
      this.scannedCodes = codes;
    });
  }

  private handleStateChange(state): void {
    this.zone.run(() => {
      this.isScanning = state === this.ScannerState.active;
    });
  }
}
