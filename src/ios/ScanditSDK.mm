//  Copyright 2016 Scandit AG
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software distributed under the
//  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
//  express or implied. See the License for the specific language governing permissions and
//  limitations under the License.

#import "ScanditSDK.h"
#import "ScanditSDKRotatingBarcodePicker.h"
#import "ScanditSDKSearchBar.h"
#import "SBSUIParamParser.h"
#import "SBSPhonegapParamParser.h"
#import "SBSTypeConversion.h"
#import "SBSPickerStateMachine.h"
#import "SBSSampleBufferConverter.h"
#import "SBSResizeScannerProtocol.h"
#import <ScanditBarcodeScanner/ScanditBarcodeScanner.h>
#import <ScanditBarcodeScanner/SBSTextRecognition.h>

@interface SBSLicense ()
+ (void)setFrameworkIdentifier:(NSString *)frameworkIdentifier;
@end

@interface ScanditSDK () <SBSScanDelegate, SBSOverlayControllerDidCancelDelegate, ScanditSDKSearchBarDelegate,
SBSPickerStateDelegate, SBSTextRecognitionDelegate, SBSProcessFrameDelegate, SBSPropertyObserver,
SBSLicenseValidationDelegate>

@property (nonatomic, copy) NSString *callbackId;
@property (readwrite, assign) BOOL hasPendingOperation;
@property (nonatomic, assign) BOOL continuousMode;
@property (nonatomic, assign) BOOL modallyPresented;
@property (nonatomic, strong) SBSPickerStateMachine *pickerStateMachine;
@property (nonatomic, readonly) dispatch_queue_t queue;
@property (nonatomic, strong) NSArray *rejectedCodeIds;
@property (nonatomic, strong) NSArray *visuallyRejectedCodeIds;
@property (nonatomic, strong) NSMutableSet<NSNumber *> *recognizedCodeIdentifiers;

@property (nonatomic, strong) NSCondition *didScanCondition;
@property (nonatomic, strong) NSCondition *didRecognizeNewCodesCondition;
@property (nonatomic, assign) int nextState;
@property (nonatomic, assign) BOOL immediatelySwitchToNextState;
@property (nonatomic, assign) BOOL didScanCallbackFinish;
@property (nonatomic, assign) BOOL didRecognizeNewCodesCallbackFinish;

@property (nonatomic, assign) BOOL matrixScanEnabled;
@property (nonatomic, assign) BOOL isDidScanDefined;
@property (nonatomic, assign) BOOL shouldPassBarcodeFrame;

@property (nonatomic, strong, readonly) ScanditSDKRotatingBarcodePicker *picker;

@property (nonatomic, weak) id<SBSResizeScannerProtocol> delegate;

@end

@implementation ScanditSDK

@synthesize hasPendingOperation;

- (dispatch_queue_t)queue {
    static dispatch_queue_t queue;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        queue = dispatch_queue_create("scandit_barcode_scanner_plugin", NULL);
        dispatch_queue_t high = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, NULL);
        dispatch_set_target_queue(queue, high);
    });
    return queue;
}

- (ScanditSDKRotatingBarcodePicker *)picker {
    return self.pickerStateMachine.picker;
}

- (void)initLicense:(CDVInvokedUrlCommand *)command {
    NSUInteger argc = [command.arguments count];
    if (argc < 1) {
        NSLog(@"The initLicense call received too few arguments and has to return without starting.");
        return;
    }
    NSString *appKey = [command.arguments objectAtIndex:0];
    [SBSLicense setFrameworkIdentifier:@"phonegap"];
    [SBSLicense setAppKey:appKey];
}

- (void)show:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;

    if (self.hasPendingOperation) {
        return;
    }
    self.hasPendingOperation = YES;

    NSUInteger argc = [command.arguments count];
    if (argc < 2) {
        NSLog(@"The show call received too few arguments and has to return without starting.");
        return;
    }

    NSDictionary *settings = [command.arguments objectAtIndex:0];
    NSDictionary *options = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:1]];
    NSDictionary *overlayOptions = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:2]];
    [self checkConstraints:options];
    [self showPickerWithSettings:settings options:options overlayOptions:overlayOptions];
}

- (void)checkConstraints:(NSDictionary *) options {
    NSDictionary *portraitConstraintsDict = [options objectForKey:[SBSPhonegapParamParser paramPortraitConstraints]];
    NSDictionary *landscapeConstraintsDict = [options objectForKey:[SBSPhonegapParamParser paramLandscapeConstraints]];
    if (self.delegate != nil && portraitConstraintsDict != nil && landscapeConstraintsDict != nil) {
        SBSConstraints *portraitConstraints = [[SBSConstraints alloc] init];
        portraitConstraints.topMargin = portraitConstraintsDict[@"topmargin"];
        portraitConstraints.rightMargin = portraitConstraintsDict[@"rightmargin"];
        portraitConstraints.bottomMargin = portraitConstraintsDict[@"bottommargin"];
        portraitConstraints.leftMargin = portraitConstraintsDict[@"leftmargin"];
        portraitConstraints.width = portraitConstraintsDict[@"width"];
        portraitConstraints.height = portraitConstraintsDict[@"height"];
        SBSConstraints *landscapeConstraints = [[SBSConstraints alloc] init];
        landscapeConstraints.topMargin = landscapeConstraintsDict[@"topmargin"];
        landscapeConstraints.rightMargin = landscapeConstraintsDict[@"rightmargin"];
        landscapeConstraints.bottomMargin = landscapeConstraintsDict[@"bottommargin"];
        landscapeConstraints.leftMargin = landscapeConstraintsDict[@"leftmargin"];
        landscapeConstraints.width = landscapeConstraintsDict[@"width"];
        landscapeConstraints.height = landscapeConstraintsDict[@"height"];
        [self.delegate scannerResized:portraitConstraints landscapeConstraints:landscapeConstraints with:0];
    }
}

- (void)picker:(ScanditSDKRotatingBarcodePicker *)picker didChangeState:(SBSPickerState)newState {
    CDVPluginResult *result = [self createResultForEvent:@"didChangeState" value:@(newState)];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

- (void)showPickerWithSettings:(NSDictionary *)settings
                       options:(NSDictionary *)options
                overlayOptions:(NSDictionary *)overlayOptions {
    dispatch_async(self.queue, ^{
        // Continuous mode support.
        self.continuousMode = NO;
        NSNumber *continuousMode = [options objectForKey:[SBSPhonegapParamParser paramContinuousMode]];
        if (continuousMode && [continuousMode isKindOfClass:[NSNumber class]]) {
            self.continuousMode = [continuousMode boolValue];
        }

        // check if didScan callback is defined
        NSNumber *isDidScanDefined = [options objectForKey:[SBSPhonegapParamParser paramIsDidScanDefined]];
        if (isDidScanDefined && [isDidScanDefined isKindOfClass:[NSNumber class]]) {
            self.isDidScanDefined = [isDidScanDefined boolValue];
        }

        // check if didProcessFrame callback is defined
        NSNumber *shouldPassBarcodeFrame = [options objectForKey:[SBSPhonegapParamParser paramShouldPassBarcodeFrame]];
        if (shouldPassBarcodeFrame && [shouldPassBarcodeFrame isKindOfClass:[NSNumber class]]) {
            self.shouldPassBarcodeFrame = [shouldPassBarcodeFrame boolValue];
        }

        dispatch_main_sync_safe(^{
            // Create the picker.
            NSError *error;
            auto scanSettings = [SBSScanSettings settingsWithDictionary:settings error:&error];
            scanSettings = [self updateScanSettings:scanSettings withSettings:settings];
            if (error) {
                NSLog(@"Error when creating settings: %@", [error localizedDescription]);
            }
            auto picker = [[ScanditSDKRotatingBarcodePicker alloc] initWithSettings:scanSettings];
            self.pickerStateMachine = [[SBSPickerStateMachine alloc] initWithPicker:picker delegate:self];
            // Show the toolbar if we start modally. Need to do this here already such that other
            // toolbar options can be set afterwards.
            if (![options objectForKey:[SBSPhonegapParamParser paramPortraitMargins]]
                && ![options objectForKey:[SBSPhonegapParamParser paramLandscapeMargins]]
                && ![options objectForKey:[SBSPhonegapParamParser paramPortraitConstraints]]
                && ![options objectForKey:[SBSPhonegapParamParser paramLandscapeConstraints]]) {
                [picker.overlayController showToolBar:YES];
            }

            // Set all the UI options.
            [SBSPhonegapParamParser updatePicker:picker
                                     fromOptions:options
                              withSearchDelegate:self];
            [SBSUIParamParser updatePickerUI:picker fromOptions:overlayOptions];
            [SBSPhonegapParamParser updatePicker:self.picker
                                     fromOptions:overlayOptions
                              withSearchDelegate:self];

            // Set this class as the delegate for the overlay controller. It will now receive events when
            // a barcode was successfully scanned, manually entered or the cancel button was pressed.
            self.picker.scanDelegate = self;
            [self.picker addPropertyObserver:self];
            self.picker.processFrameDelegate = self;
            self.picker.licenseValidationDelegate = self;
            if ([self.picker respondsToSelector:@selector(setTextRecognitionDelegate:)]) {
                self.picker.textRecognitionDelegate = self;
            }
            self.picker.overlayController.cancelDelegate = self;

            BOOL showAsSubView =
            [options objectForKey:[SBSPhonegapParamParser paramPortraitMargins]] ||
            [options objectForKey:[SBSPhonegapParamParser paramLandscapeMargins]] ||
            [options objectForKey:[SBSPhonegapParamParser paramPortraitConstraints]] ||
            [options objectForKey:[SBSPhonegapParamParser paramLandscapeConstraints]]
            ;
            if (showAsSubView) {
                self.modallyPresented = NO;
                [self.viewController addChildViewController:picker];
                [self.viewController.view addSubview:self.picker.view];
                [picker didMoveToParentViewController:self.viewController];

                NSDictionary *constraints = [SBSPhonegapParamParser updateLayoutOfPicker:self.picker withOptions:options];
                if (self.delegate != nil && constraints != nil) {
                    SBSConstraints *portraitMargins = [constraints objectForKey:@"portraitConstraints"];
                    SBSConstraints *landscapeMargins = [constraints objectForKey:@"landscapeConstraints"];
                    NSNumber *animationDuration = [constraints objectForKey:@"animationDuration"];
                    CGFloat duration = [animationDuration doubleValue];
                    [self.delegate scannerResized:portraitMargins landscapeConstraints:landscapeMargins with:duration];
                }


            } else {
                self.modallyPresented = YES;

                // Present the barcode picker in a new window.
                // We don't use presentViewController:animated:completion: because that would hang the webview
                // if the cordova-plugin-wkwebview-engine plugin is used.
                const auto frame = [[UIScreen mainScreen] bounds];
                const auto scanditWindow = [[UIWindow alloc] initWithFrame:frame];
                const auto scanditController = [[UIViewController alloc] init];
                scanditController.view.backgroundColor = [UIColor clearColor];
                [scanditWindow setRootViewController:scanditController];
                [scanditWindow setWindowLevel:UIWindowLevelNormal];
                [scanditWindow makeKeyAndVisible];
                [scanditController presentViewController:self.picker animated:YES completion:nil];
            }
        });
    });
}

- (void)startScanning:(NSNumber *)startPaused {
    [self.pickerStateMachine startScanningInPausedState:[startPaused boolValue]];
}

- (void)applySettings:(CDVInvokedUrlCommand *)command {
    NSUInteger argc = [command.arguments count];
    if (argc < 1) {
        NSLog(@"The applySettings call received too few arguments and has to return without starting.");
        return;
    }

    dispatch_async(self.queue, ^{
        if (self.picker) {
            NSDictionary *settings = [command.arguments objectAtIndex:0];
            NSError *error;
            SBSScanSettings *scanSettings = [SBSScanSettings settingsWithDictionary:settings error:&error];
            scanSettings = [self updateScanSettings:scanSettings withSettings:settings];
            if (error) {
                NSLog(@"Error when creating settings: %@", [error localizedDescription]);
            } else {
                [self.picker applyScanSettings:scanSettings completionHandler:nil];
            }
        }
    });
}

- (void)updateOverlay:(CDVInvokedUrlCommand *)command {
    NSUInteger argc = [command.arguments count];
    if (argc > 0) {
        NSDictionary *overlayOptions = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:0]];
        [SBSUIParamParser updatePickerUI:self.picker fromOptions:overlayOptions];
        [SBSPhonegapParamParser updatePicker:self.picker
                                 fromOptions:overlayOptions
                          withSearchDelegate:self];
    }
}

- (void)cancel:(CDVInvokedUrlCommand *)command {
    dispatch_async(self.queue, ^{
        if (self.picker) {
            [self overlayController:self.picker.overlayController didCancelWithStatus:nil];
        }
    });
}

- (void)pause:(CDVInvokedUrlCommand *)command {
    dispatch_async(self.queue, ^{
        [self.pickerStateMachine setDesiredState:SBSPickerStatePaused];
    });
}

- (void)resume:(CDVInvokedUrlCommand *)command {
    dispatch_async(self.queue, ^{
        [self.pickerStateMachine setDesiredState:SBSPickerStateActive];
    });
}

- (void)start:(CDVInvokedUrlCommand *)command {
    dispatch_async(self.queue, ^{
        NSUInteger argc = [command.arguments count];
        NSDictionary *options = [NSDictionary dictionary];
        if (argc >= 1) {
            options = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:0]];
        }
        [self.recognizedCodeIdentifiers removeAllObjects];
        [self.pickerStateMachine startScanningInPausedState:[SBSPhonegapParamParser isPausedSpecifiedInOptions:options]];
    });
}

- (void)stop:(CDVInvokedUrlCommand *)command {
    dispatch_async(self.queue, ^{
        [self.pickerStateMachine setDesiredState:SBSPickerStateStopped];
    });
}

- (void)resize:(CDVInvokedUrlCommand *)command {
    if (self.picker && !self.modallyPresented) {
        NSUInteger argc = [command.arguments count];
        if (argc < 1) {
            NSLog(@"The resize call received too few arguments and has to return without starting.");
            return;
        }
        dispatch_async(self.queue, ^{
            dispatch_main_sync_safe(^{
                NSDictionary *options = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:0]];
                NSDictionary *constraints = [SBSPhonegapParamParser updateLayoutOfPicker:self.picker withOptions:options];
                if (self.delegate != nil && constraints != nil) {
                    SBSConstraints *portraitMargins = [constraints objectForKey:@"portraitConstraints"];
                    SBSConstraints *landscapeMargins = [constraints objectForKey:@"landscapeConstraints"];
                    NSNumber *animationDuration = [constraints objectForKey:@"animationDuration"];
                    CGFloat duration = [animationDuration doubleValue];
                    [self.delegate scannerResized:portraitMargins landscapeConstraints:landscapeMargins with:duration];
                }

            });
        });
    }
}

- (void)torch:(CDVInvokedUrlCommand *)command {
    NSUInteger argc = [command.arguments count];
    if (argc < 1) {
        NSLog(@"The torch call received too few arguments and has to return without starting.");
        return;
    }
    dispatch_async(self.queue, ^{
        NSNumber *enabled = [command.arguments objectAtIndex:0];
        [self.picker switchTorchOn:[enabled boolValue]];
    });
}

- (void)finishDidScanCallback:(CDVInvokedUrlCommand *)command {
    NSArray *args = command.arguments;
    self.nextState = 0;
    if ([args count] > 1) {
        int nextState = [args[0] intValue];
        self.rejectedCodeIds = args[1];
        if (self.immediatelySwitchToNextState) {
            [self switchToNextScanState:nextState withSession:nil];
            self.immediatelySwitchToNextState = NO;
        } else {
            self.nextState = nextState;
        }
    }
    self.didScanCallbackFinish = YES;
    [self.didScanCondition signal];
}

- (void)finishDidRecognizeNewCodesCallback:(CDVInvokedUrlCommand *)command {
    NSArray *args = command.arguments;
    if ([args count] >= 1 && [args[0] isKindOfClass:[NSArray class]]) {
        self.visuallyRejectedCodeIds = args[0];
    }
    if ([args count] >= 2 && [args[1] isKindOfClass:[NSDictionary class]]) {
        [self.picker setTrackedCodeStates:args[1]];
    }
    self.didRecognizeNewCodesCallbackFinish = YES;
    [self.didRecognizeNewCodesCondition signal];
}

#pragma mark - Utilities

- (NSDictionary *)lowerCaseOptionsFromOptions:(NSDictionary *)options {
    NSMutableDictionary *result = [NSMutableDictionary dictionary];
    for (NSString *key in options) {
        NSObject *object = [options objectForKey:key];
        if ([object isKindOfClass:[NSDictionary class]]) {
            object = [self lowerCaseOptionsFromOptions:(NSDictionary *)object];
        }
        [result setObject:object forKey:[key lowercaseString]];
    }
    return result;
}

- (SBSScanSettings *)updateScanSettings:(SBSScanSettings *)scanSettings withSettings:(NSDictionary *)settings {
    if ([scanSettings respondsToSelector:@selector(setRecognitionMode:)]) {
        id recognitionMode = settings[@"recognitionMode"];
        if (recognitionMode != nil && [recognitionMode isKindOfClass:[NSString class]] && [recognitionMode isEqualToString:@"text"]) {
            scanSettings.recognitionMode = SBSRecognitionModeText;
        } else if (recognitionMode != nil && [recognitionMode isKindOfClass:[NSString class]]) {
            scanSettings.recognitionMode = SBSRecognitionModeCode;
        } else if (recognitionMode != nil && [recognitionMode isKindOfClass:[NSNumber class]] && [recognitionMode isEqualToNumber:[NSNumber numberWithInt:1]]) {
            scanSettings.recognitionMode = SBSRecognitionModeText;
        } else if (recognitionMode != nil && [recognitionMode isKindOfClass:[NSNumber class]]) {
            scanSettings.recognitionMode = SBSRecognitionModeCode;
        }
    }

    NSNumber *matrixScanEnabled = settings[@"matrixScanEnabled"];
    if (matrixScanEnabled != nil && [matrixScanEnabled isKindOfClass:[NSNumber class]]) {
        if ([matrixScanEnabled boolValue]) {
            self.recognizedCodeIdentifiers = [[NSMutableSet alloc] init];
            scanSettings.matrixScanEnabled = YES;
            self.matrixScanEnabled = YES;
        } else {
            self.recognizedCodeIdentifiers = nil;
            scanSettings.matrixScanEnabled = NO;
            self.matrixScanEnabled = NO;
        }
    }

    return scanSettings;
}

- (void)add:(id <SBSResizeScannerProtocol>)listener {
    self.delegate = listener;
}

+ (void)add:(id <SBSResizeScannerProtocol>)listener to:(ScanditSDK *)scanditSdk {
    [scanditSdk add:listener];
}

- (void)removeListener {
    self.delegate = nil;
}

+ (void)removeListener:(ScanditSDK *)scanditSdk {
    [scanditSdk removeListener];
}

#pragma mark - SBSScanDelegate methods

- (void)barcodePicker:(SBSBarcodePicker *)picker didScan:(SBSScanSession *)session {
    // Don't serialize the result if didScan is not defined.
    if (!self.isDidScanDefined) {
        return;
    }

    CDVPluginResult *pluginResult = [self resultForSession:session];

    int nextState = [self sendPluginResultBlocking:pluginResult];
    if (!self.continuousMode) {
        nextState = SBSPickerStateStopped;
    }
    [self switchToNextScanState:nextState withSession:session];
    NSArray *newlyRecognized = session.newlyRecognizedCodes;
    for (NSNumber *codeId in self.rejectedCodeIds) {
        long value = [codeId longValue];
        for (SBSCode *code in newlyRecognized) {
            if (code.uniqueId == value) {
                [session rejectCode:code];
                break;
            }
        }
    }
}

#pragma mark - SBSProcessFrameDelegate methods

- (void)barcodePicker:(SBSBarcodePicker *)barcodePicker
      didProcessFrame:(CMSampleBufferRef)frame
              session:(SBSScanSession *)session {
    // Call `didProcessFrame` only when new codes have been recognized and when didProcessFrame has been set.
    // If MatrixScan is enabled we will send the frame later.
    if (!self.matrixScanEnabled && session.newlyRecognizedCodes.count > 0) {
        [self sendFrameIfNeeded:frame];
    }

    // If at least a code has been recognized and continuous mode is off, then we should dismiss/remove the picker
    // and set hasPendingOperation = NO.
    // This is done here and not in barcodePicker:didScan: because barcodePicker:didProcessFrame:session:
    // is executed after barcodePicker:didScan: and we might need to send the `didProcessFrame` event.
    if (session.newlyRecognizedCodes.count > 0 && !self.continuousMode) {
        dispatch_main_sync_safe(^{
            if (self.modallyPresented) {
                [self.picker dismissViewControllerAnimated:YES completion:nil];
            } else {
                [self.picker removeFromParentViewController];
                [self.picker.view removeFromSuperview];
                [self.picker didMoveToParentViewController:nil];
            }
            if (self.delegate != nil) {
                [self.delegate scannerDismissed];
            }
            self.pickerStateMachine = nil;
            self.hasPendingOperation = NO;
        });
    }

    // Proceed only if matrix scan is enabled
    if (!self.matrixScanEnabled || session.trackedCodes == nil) {
        return;
    }

    NSDictionary<NSNumber *, SBSTrackedCode *> *trackedCodes = session.trackedCodes;
    NSSet<NSNumber *> *trackedCodeIdentifiers = [NSSet setWithArray:[trackedCodes allKeys]];
    NSMutableArray<SBSTrackedCode *> *newlyTrackedCodes = [[NSMutableArray alloc] init];
    BOOL atLeastOneNewCode = NO;
    for (NSNumber *identifier in trackedCodeIdentifiers) {
        // Check if it's a new identifier.
        if (trackedCodes[identifier].isRecognized && ![self.recognizedCodeIdentifiers containsObject:identifier]) {
            atLeastOneNewCode = YES;
            // Add the new identifier.
            [self.recognizedCodeIdentifiers addObject:identifier];
            [newlyTrackedCodes addObject:trackedCodes[identifier]];
        }
    }
    // Remove all identifiers that disappeared.
    [self.recognizedCodeIdentifiers intersectSet:trackedCodeIdentifiers];

    // We want to block the thread and wait for the JS callback only if at least one new code was found.
    if (atLeastOneNewCode) {
        // Call JS callback to send the tracked codes (blocking the thread).
        CDVPluginResult *pluginResult = [self trackingResultWithTrackedCodes:newlyTrackedCodes];
        [self sendNewlyTrackedCodesBlocking:pluginResult];

        // Visually reject codes
        for (NSNumber *codeId in self.visuallyRejectedCodeIds) {
            SBSTrackedCode *code = trackedCodes[codeId];
            [session rejectTrackedCode:code];
        }

        // Send the frame
        [self sendFrameIfNeeded:frame];
    }
}

#pragma mark - Create/send result

- (CDVPluginResult *)createResultForEvent:(NSString *)name value:(NSObject *)value {
    NSArray *args = @[name, value];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                 messageAsArray:args];
    [result setKeepCallback:@YES];
    return result;
}

- (CDVPluginResult *)resultForSession:(SBSScanSession *)session {
    NSDictionary *result = @{
                             @"newlyRecognizedCodes": SBSJSObjectsFromCodeArray(session.newlyRecognizedCodes),
                             @"newlyLocalizedCodes" : SBSJSObjectsFromCodeArray(session.newlyLocalizedCodes),
                             @"allRecognizedCodes" : SBSJSObjectsFromCodeArray(session.allRecognizedCodes)
                             };
    return [self createResultForEvent:@"didScan" value:result];
}

- (CDVPluginResult *)trackingResultWithTrackedCodes:(NSArray<SBSTrackedCode *> *)newlyTrackedCodes {
    NSDictionary *result = @{@"newlyTrackedCodes": SBSJSObjectsFromCodeArray(newlyTrackedCodes)};
    return [self createResultForEvent:@"didRecognizeNewCodes" value:result];
}

- (int)sendPluginResultBlocking:(CDVPluginResult *)result {
    if (![NSThread isMainThread]) {
        [self.didScanCondition lock];
        self.didScanCallbackFinish = NO;
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        while (!self.didScanCallbackFinish) {
            [self.didScanCondition wait];
        }
        return self.nextState;
    }
    // We are on the main thread where the callback will be invoked on as well, we
    // have to manually assemble the command to be executed.
    NSString *command = @"cordova.callbacks['%@'].success(%@);";
    NSString *commandSubst = [NSString stringWithFormat:command, self.callbackId, result.argumentsAsJSON];
    [self.commandDelegate evalJs:commandSubst scheduledOnRunLoop:NO];
    return self.nextState;
}

- (void)switchToNextScanState:(int)nextState withSession:(SBSScanSession *)session {
    if (nextState == 2) {
        if (session) {
            // pause immediately, but use picker state machine so we get proper events.
            [session pauseScanning];
        }
        [self.pickerStateMachine setDesiredState:SBSPickerStateStopped];
    } else if (nextState == 1) {
        if (session) {
            [session pauseScanning];
        }
        [self.pickerStateMachine setDesiredState:SBSPickerStatePaused];
    }
}

- (void)sendNewlyTrackedCodesBlocking:(CDVPluginResult *)result {
    if (![NSThread isMainThread]) {
        [self.didRecognizeNewCodesCondition lock];
        self.didRecognizeNewCodesCallbackFinish = NO;
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        while (!self.didRecognizeNewCodesCallbackFinish) {
            [self.didRecognizeNewCodesCondition wait];
        }
        return;
    }
    // We are on the main thread where the callback will be invoked on as well, we
    // have to manually assemble the command to be executed.
    NSString *command = @"cordova.callbacks['%@'].success(%@);";
    NSString *commandSubst = [NSString stringWithFormat:command, self.callbackId, result.argumentsAsJSON];
    [self.commandDelegate evalJs:commandSubst scheduledOnRunLoop:NO];
}

- (void)sendFrameIfNeeded:(CMSampleBufferRef)sampleBuffer {
    if (self.shouldPassBarcodeFrame) {
        const auto base64frame = [SBSSampleBufferConverter base64StringFromFrame:sampleBuffer];
        const auto imageObject = @{@"base64Data": base64frame};
        const auto pluginResult = [self createResultForEvent:@"didProcessFrame" value:imageObject];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }
}

#pragma mark - SBSTextRecognitionDelegate

- (SBSBarcodePickerState)barcodePicker:(SBSBarcodePicker *)picker
                      didRecognizeText:(SBSRecognizedText *)text {
    NSDictionary *dict = @{ @"text": text.text };
    CDVPluginResult *pluginResult = [self createResultForEvent:@"didRecognizeText" value:dict];
    int nextState = [self sendPluginResultBlocking:pluginResult];
    if (!self.continuousMode) {
        nextState = SBSPickerStateStopped;
    }
    SBSBarcodePickerState barcodePickerState = [self switchRecognitionTextToNextState:nextState];
    if (![self.rejectedCodeIds isEqual:[NSNull null]] && self.rejectedCodeIds.count > 0) {
        text.rejected = YES;
    }

    if (!self.continuousMode) {
        dispatch_main_sync_safe(^{
            if (self.modallyPresented) {
                [self.picker dismissViewControllerAnimated:YES completion:nil];
            } else {
                [self.picker removeFromParentViewController];
                [self.picker.view removeFromSuperview];
                [self.picker didMoveToParentViewController:nil];
            }
            if (self.delegate != nil) {
                [self.delegate scannerDismissed];
            }
            self.pickerStateMachine = nil;
            self.hasPendingOperation = NO;
        });
    }

    return barcodePickerState;
}

- (SBSBarcodePickerState)switchRecognitionTextToNextState:(int)nextState {
    SBSBarcodePickerState state = SBSBarcodePickerStateActive;
    if (nextState == 2) {
        state = SBSBarcodePickerStateStopped;
        [self.pickerStateMachine setDesiredState:SBSPickerStateStopped];
    } else if (nextState == 1) {
        state = SBSBarcodePickerStatePaused;
        [self.pickerStateMachine setDesiredState:SBSPickerStatePaused];
    }
    return state;
}

#pragma mark - SBSOverlayControllerDidCancelDelegate

- (void)sendCancelEvent {
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:@"Canceled"];
    [pluginResult setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void)overlayController:(SBSOverlayController *)overlayController
      didCancelWithStatus:(NSDictionary *)status {
    [self.pickerStateMachine setDesiredState:SBSPickerStateStopped];
    dispatch_main_sync_safe(^{
        if (self.modallyPresented) {
            [self.picker dismissViewControllerAnimated:YES completion:nil];
        } else {
            [self.picker removeFromParentViewController];
            [self.picker.view removeFromSuperview];
            [self.picker didMoveToParentViewController:nil];
        }
        if (self.delegate != nil) {
            [self.delegate scannerDismissed];
        }
    });
    self.pickerStateMachine = nil;
    [self sendCancelEvent];
    self.hasPendingOperation = NO;
}

#pragma mark - ScanditSDKSearchBarDelegate

- (void)searchExecutedWithContent:(NSString *)content {
    CDVPluginResult *pluginResult = [self createResultForEvent:@"didManualSearch" value:content];

    if (!self.continuousMode) {
        [self.pickerStateMachine setDesiredState:SBSPickerStateStopped];
        if (self.modallyPresented) {
            [self.picker dismissViewControllerAnimated:YES completion:nil];
        } else {
            [self.picker removeFromParentViewController];
            [self.picker.view removeFromSuperview];
            [self.picker didMoveToParentViewController:nil];
        }
        if (self.delegate != nil) {
            [self.delegate scannerDismissed];
        }
        self.pickerStateMachine = nil;
        self.hasPendingOperation = NO;
    } else {
        [self.picker.overlayController resetUI];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

#pragma mark - SBSPropertyObserver

- (void)barcodePicker:(SBSBarcodePicker *)barcodePicker property:(NSString *)property changedToValue:(NSObject *)value {
    if (![value isKindOfClass:[NSString class]] && ![value isKindOfClass:[NSNumber class]]) {
        return;
    }
    const auto dictionary = @{@"name": property, @"newState": value};
    const auto pluginResult = [self createResultForEvent:@"didChangeProperty" value:dictionary];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

#pragma mark - SBSLicenseValidationDelegate

- (void)barcodePicker:(SBSBarcodePicker *)picker failedToValidateLicense:(NSString *)errorMessage {
    const auto error = @{@"message": errorMessage};
    const auto pluginResult = [self createResultForEvent:@"didFailToValidateLicense" value:error];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

@end
