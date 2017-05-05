import { Component, NgZone, ViewChild } from '@angular/core';
import { Events, Content, NavController, NavParams } from 'ionic-angular';

import { Scanner } from '../../providers/scanner';

import { Enums } from '../../providers/enums';

@Component({
  selector: 'page-pickers',
  templateUrl: 'pickers.html'
})
export class PickersPage {
  @ViewChild(Content) content: Content;

  public scannedCodes;

  private onScanHandler: Function;

  constructor(
    private zone: NgZone,
    private navCtrl: NavController,
    private navParams: NavParams,
    private events: Events,
    private scanner: Scanner,
    private enums: Enums,
  ) {
    this.onScanHandler = (session) => {
      this.handleScan(session);
    }
  }

  public ionViewWillEnter(): void {
    this.subscribe();
  }

  public ionViewDidEnter(): void {
    this.stopScanning();
    this.setScannedCodes(undefined);
  }

  public ionViewWillLeave(): void {
    this.unsubscribe();
  }

  public startFullscreenScanner($event: MouseEvent): void {
    $event.stopPropagation();
    this.startScanner(0, 0, 0, 0);
  }

  public startCroppedScanner($event: MouseEvent): void {
    $event.stopPropagation();
    this.startScanner(
      this.content.contentHeight * 0.2,
      this.content.contentWidth * 0.2,
      this.content.contentHeight * 0.2,
      this.content.contentWidth * 0.2,
    );
  }

  public startLRCroppedScanner($event: MouseEvent): void {
    $event.stopPropagation();
    this.startScanner(
      0,
      this.content.contentWidth * 0.2,
      0,
      this.content.contentWidth * 0.2,
    );
  }

  public startTBCroppedScanner($event: MouseEvent): void {
    $event.stopPropagation();
    this.startScanner(
      this.content.contentHeight * 0.2,
      0,
      this.content.contentHeight * 0.2,
      0,
    );
  }

  public stopScanning(): void {
    this.scanner.stop();
  }

  private subscribe(): void {
    this.events.subscribe(this.scanner.event.scan, this.onScanHandler);
  }

  private unsubscribe(): void {
    this.events.unsubscribe(this.scanner.event.scan, this.onScanHandler);
  }

  private startScanner(top: Margin, right: Margin, bottom: Margin, left: Margin): void {
    this.scanner.setConstraints(top, right, bottom, left, 0.1);
    this.scanner.clampActiveScanningArea();
    this.scanner.start();
  }

  private setScannedCodes(codes: any[]): void {
    this.zone.run(() => {
      this.scannedCodes = codes;
    });
  }

  private handleScan(session): void {
    this.setScannedCodes(session.newlyRecognizedCodes);
    this.scanner.stop();
  }
}
