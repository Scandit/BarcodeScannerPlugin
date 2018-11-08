Use the Scandit Scan Case in your app      {#cordova-scan-case}
=========================================

This guide show how to scan barcodes with the Scandit scan case using the ScanCase API.

Using the ScanCase API is relatively easy: all you need to do is to create a ScanCase object and implement
the necessary callbacks.

## Creating an instance of ScanCase

In order to create an instance of ScanCase, you first need to
set the license key (if you haven't already), create a scan settings object, and
enable the symbologies you need. At this point you can create a ScanCase.

~~~~~~~~~~~~~~~~{.java}
Scandit.License.setAppKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --");
var scanSettings = {
    symbologies: [Scandit.Barcode.Symbology.EAN13]
};
scanCase = Scandit.ScanCase.acquire(scanSettings, callbacks);
~~~~~~~~~~~~~~~~

Scanning with ScanCase is driven by it's state property.
ScanCase initially starts in STANDBY and can be set to one of the following three states:
- OFF: camera is off, torch is off.
- STANDBY: camera is on but with throttled frame-rate, scanner is off, torch is off.
- ACTIVE: camera is on, scanner is on, torch is on.

In order to start scanning, you just need to change the state to ACTIVE:

~~~~~~~~~~~~~~~~{.java}
scanCase.setState(Scandit.ScanCase.State.ACTIVE);
~~~~~~~~~~~~~~~~

## Define the callbacks

Let's now implement the callbacks. The first callback is called when ScanCase finished the initialization process and it's ready to be used.

~~~~~~~~~~~~~~~~{.java}
var callbacks = {
    didInitialize: function () {},
    didScan: function (session) {
        return Scandit.ScanCase.State.STANDBY;
    },
    didChangeState: function (data) {}
};
~~~~~~~~~~~~~~~~

Whenever a new code is scanned the following method is called.
If you want to keep scanning new codes, return ACTIVE, if you want to
temporary pause the scanner return STANDBY.
You can also return OFF if you don't plan to scan new codes relatively soon.

Please note that changing from OFF to ACTIVE takes more time
than switching from STANDBY to ACTIVE.

~~~~~~~~~~~~~~~~{.java}
function () {
    // ScanCase is ready, and the state is STANDBY
}
~~~~~~~~~~~~~~~~

Whenever the state of ScanCase changes the following method is called.
There are multiple reasons for which the state can be changed:
- MANUAL: the state has been changed programmatically by changing the State property
  or by returning a different state from the didScan callback.
- TIMEOUT: the state has been changed because of a timeout (check the timeout section).
- VOLUME_BUTTON: the state has been changed by the volume button (check the volume button section).

~~~~~~~~~~~~~~~~{.java}
function (session) {
    return Scandit.ScanCase.State.STANDBY;
}
~~~~~~~~~~~~~~~~

## Enabling the volume button to start scanning

It is possible to use the volume button to scan.
Keeping the volume button pressed will keep the scan case in ACTIVE state,
while releasing the button will change the state to STANDBY.
To enable this feature, all you need to do is:

~~~~~~~~~~~~~~~~{.m}
scanCase.volumeButtonToScanEnabled(true);
~~~~~~~~~~~~~~~~

You know when the state changes, because of the volume button, in the didChangeState callback,
the reason will be VOLUME_BUTTON.

<!-- ## Using timeouts to switch state

It is possible to switch from one state to another one automatically after a specific timeout.
This could be useful, for instance, to switch the scanner off after a long time of inactivity in order to save power.

The following code changes the state from STANDBY to OFF after approximately 60 seconds.

*Swift*
~~~~~~~~~~~~~~~~{.m}
scanCase.setTimeout(60, from: .standby, to: .off)
~~~~~~~~~~~~~~~~
You know when the state changes because of a timeout in SBSScanCaseDelegate::scanCase:didChangeState:reason:,
the reason will be SBSScanCaseStateChangeReasonTimeout.

You could, for instance, display an alert to inform the user that the scanned has been switched off:

*Swift*
~~~~~~~~~~~~~~~~{.m}
func scanCase(_ scanCase: SBSScanCase, didChange state: SBSScanCaseState, reason: SBSScanCaseStateChangeReason) {
    switch state {
    case .standby: break
    case .active: break
    case .off:
        DispatchQueue.main.async {
            if reason == SBSScanCaseStateChangeReason.timeout {
                let alertTitle = "State changed to SBSScanCaseStateOff to save power"
                let alertController = UIAlertController(title: alertTitle, message: nil, preferredStyle: .alert)
                let okAction = UIAlertAction(title: "OK", style: .default)
                alertController.addAction(okAction)
                self.present(alertController, animated: true)
            }
        }
    }
}
~~~~~~~~~~~~~~~~ -->

## Restricting scanning area

It is possible to restrict the area in which the scanner will perform actual scanning. To do this you need to use the following settings while creating scan settings:
- `scanningAreaHeight`: for 1D symbologies
- `scanningAreaHeight2d`: for 2D symbologies

Example configuration could look like this:

~~~~~~~~~~~~~~~~{.java}
Scandit.License.setAppKey("-- ENTER YOUR SCANDIT LICENSE KEY HERE --");
var scanSettings = {
    symbologies: [Scandit.Barcode.Symbology.EAN13],
    scanningAreaHeight: 0.1,
    scanningAreaHeight2d: 0.2
};
scanCase = Scandit.ScanCase.acquire(scanSettings, callbacks);
~~~~~~~~~~~~~~~~
