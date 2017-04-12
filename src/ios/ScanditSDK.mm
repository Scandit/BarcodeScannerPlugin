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
#import "SBSLegacySettingsParamParser.h"
#import "SBSLegacyUIParamParser.h"
#import "SBSUIParamParser.h"
#import "SBSPhonegapParamParser.h"
#import "SBSTypeConversion.h"
#import "SBSPickerStateMachine.h"
#import <ScanditBarcodeScanner/ScanditBarcodeScanner.h>
#import <ScanditBarcodeScanner/SBSTextRecognition.h>

@interface SBSLicense ()
+ (void)setFrameworkIdentifier:(NSString *)frameworkIdentifier;
@end

@interface ScanditSDK () <SBSScanDelegate, SBSOverlayControllerDidCancelDelegate, ScanditSDKSearchBarDelegate,
                          SBSPickerStateDelegate, SBSTextRecognitionDelegate>

@property (nonatomic, copy) NSString *callbackId;
@property (readwrite, assign) BOOL hasPendingOperation;
@property (nonatomic, assign) BOOL continuousMode;
@property (nonatomic, assign) BOOL modallyPresented;
@property (nonatomic, strong) SBSPickerStateMachine *pickerStateMachine;
@property (nonatomic, strong) dispatch_queue_t queue;
@property (nonatomic, assign) BOOL legacyMode;
@property (nonatomic, strong) NSArray *rejectedCodeIds;

@property (nonatomic, strong) NSCondition* didScanCondition;
@property (nonatomic, assign) int nextState;
@property (nonatomic, assign) BOOL immediatelySwitchToNextState;
@property (nonatomic, assign) BOOL didScanCallbackFinish;

@property (nonatomic,strong, readonly) ScanditSDKRotatingBarcodePicker* picker;
@end


@implementation ScanditSDK
@synthesize hasPendingOperation;

- (dispatch_queue_t)queue {
    if (!_queue) {
        self.queue = dispatch_queue_create("scandit_barcode_scanner_plugin", NULL);
        dispatch_queue_t high = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, NULL);
        dispatch_set_target_queue(_queue, high);
    }
    return _queue;
}

- (ScanditSDKRotatingBarcodePicker*)picker {
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
    if (self.hasPendingOperation) {
        return;
    }
    self.hasPendingOperation = YES;
    
    NSUInteger argc = [command.arguments count];
    if (argc < 2) {
        NSLog(@"The show call received too few arguments and has to return without starting.");
        return;
    }
    self.callbackId = command.callbackId;
    
    NSDictionary *settings = [command.arguments objectAtIndex:0];
    NSDictionary *options = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:1]];
    NSDictionary *overlayOptions = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:2]];
    
    self.legacyMode = NO;
    [self showPickerWithSettings:settings options:options overlayOptions:overlayOptions];
}

- (void)scan:(CDVInvokedUrlCommand *)command {
    if (self.hasPendingOperation) {
        return;
    }
    self.hasPendingOperation = YES;
    
    NSUInteger argc = [command.arguments count];
    if (argc < 2) {
        NSLog(@"The scan call received too few arguments and has to return without starting.");
        return;
    }
    self.callbackId = command.callbackId;
    
    NSString *appKey = [command.arguments objectAtIndex:0];
    [SBSLicense setFrameworkIdentifier:@"phonegap"];
    [SBSLicense setAppKey:appKey];
    
    NSDictionary *options = [self lowerCaseOptionsFromOptions:[command.arguments objectAtIndex:1]];
    
    self.legacyMode = YES;
    [self showPickerWithSettings:nil options:options overlayOptions:nil];
}

- (void)picker:(ScanditSDKRotatingBarcodePicker *)picker didChangeState:(SBSPickerState)newState {
    if (self.legacyMode) return;
    
    CDVPluginResult * result = [self createResultForEvent:@"didChangeState" value:@(newState)];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

- (void)showPickerWithSettings:(NSDictionary *)settings
                       options:(NSDictionary *)options
                overlayOptions:(NSDictionary *)overlayOptions {
    dispatch_async(self.queue, ^{
        // Continuous mode support.
        self.continuousMode = NO;
        NSObject *continuousMode = [options objectForKey:[SBSPhonegapParamParser paramContinuousMode]];
        if (continuousMode && [continuousMode isKindOfClass:[NSNumber class]]) {
            self.continuousMode = [((NSNumber *)continuousMode) boolValue];
        }
        
        dispatch_main_sync_safe(^{
            // Create the picker.
            SBSScanSettings *scanSettings;
            if (!settings) {
                scanSettings = [SBSLegacySettingsParamParser settingsForOptions:options];
            } else {
                NSError *error;
                scanSettings = [SBSScanSettings settingsWithDictionary:settings error:&error];
                if (error) {
                    NSLog(@"Error when creating settings: %@", [error localizedDescription]);
                }
            }
            ScanditSDKRotatingBarcodePicker* picker = [[ScanditSDKRotatingBarcodePicker alloc]
                                         initWithSettings:scanSettings];
            self.pickerStateMachine =
                [[SBSPickerStateMachine alloc] initWithPicker:picker delegate:self];
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
            
            if (self.legacyMode) {
                [SBSLegacyUIParamParser updatePickerUI:picker fromOptions:options];
            } else {
                [SBSUIParamParser updatePickerUI:picker fromOptions:overlayOptions];
                [SBSPhonegapParamParser updatePicker:self.picker
                                         fromOptions:overlayOptions
                                  withSearchDelegate:self];
            }
            
            // Set this class as the delegate for the overlay controller. It will now receive events when
            // a barcode was successfully scanned, manually entered or the cancel button was pressed.
            self.picker.scanDelegate = self;
            self.picker.textRecognitionDelegate = self;
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
                
                [SBSPhonegapParamParser updateLayoutOfPicker:self.picker
                                                 withOptions:options];
                
            } else {
                self.modallyPresented = YES;
                
                // Present the barcode picker modally and start scanning.
                [self.viewController presentViewController:self.picker animated:YES completion:nil];
            }
            
            // Only already start in legacy mode.
            if (self.legacyMode) {
                [self performSelector:@selector(startScanning:)
                           withObject:@([SBSPhonegapParamParser isPausedSpecifiedInOptions:options])
                           afterDelay:0.1];
            }
        });
    });
}

- (void)startScanning:(NSNumber*)startPaused {
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
                if (self.legacyMode) {
                    [SBSLegacyUIParamParser updatePickerUI:self.picker fromOptions:options];
                }
                
                [SBSPhonegapParamParser updateLayoutOfPicker:self.picker withOptions:options];
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

- (void)finishDidScanCallback:(CDVInvokedUrlCommand*)command {
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


#pragma mark - SBSScanDelegate methods

- (void)barcodePicker:(SBSBarcodePicker *)picker didScan:(SBSScanSession *)session {
    CDVPluginResult *pluginResult = [self resultForSession:session];

    int nextState = [self sendPluginResultBlocking:pluginResult];
    if (!self.continuousMode) {
        nextState = SBSPickerStateStopped;
    }
    [self switchToNextScanState:nextState withSession:session];
    NSArray* newlyRecognized = session.newlyRecognizedCodes;
    for (NSNumber* codeId in self.rejectedCodeIds) {
        long value = [codeId longValue];
        for (SBSCode* code in newlyRecognized) {
            if (code.uniqueId == value) {
                [session rejectCode:code];
                break;
            }
        }
    }
    if (!self.continuousMode) {
        dispatch_main_sync_safe(^{
            if (self.modallyPresented) {
                [self.viewController dismissViewControllerAnimated:YES completion:nil];
            } else {
                [self.picker removeFromParentViewController];
                [self.picker.view removeFromSuperview];
                [self.picker didMoveToParentViewController:nil];
            }
            self.pickerStateMachine = nil;
            self.hasPendingOperation = NO;
        });
    }
}

- (CDVPluginResult*)createResultForEvent:(NSString*)name value:(NSObject*)value {
    NSArray* args = @[name, value];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                 messageAsArray:args];
    [result setKeepCallback:@YES];
    return result;
}

- (CDVPluginResult *)resultForSession:(SBSScanSession *)session {
    if (self.legacyMode) {
        SBSCode *newCode = [session.newlyRecognizedCodes objectAtIndex:0];
        NSArray *args = [[NSArray alloc] initWithObjects:[newCode data], [newCode symbologyString], nil];
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                     messageAsArray:args];
        [result setKeepCallback:@YES];
        return result;
    }
    NSDictionary *result = @{
        @"newlyRecognizedCodes": SBSJSObjectsFromCodeArray(session.newlyRecognizedCodes),
        @"newlyLocalizedCodes" : SBSJSObjectsFromCodeArray(session.newlyLocalizedCodes),
        @"allRecognizedCodes" : SBSJSObjectsFromCodeArray(session.allRecognizedCodes)
    };
    return [self createResultForEvent:@"didScan" value:result];
}

- (int)sendPluginResultBlocking:(CDVPluginResult *)result {
    if (self.legacyMode) {
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        return 0;
    }

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
    NSString* command = @"cordova.callbacks['%@'].success(%@);";
    NSString* commandSubst =
        [NSString stringWithFormat:command, self.callbackId, result.argumentsAsJSON];
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

#pragma mark - SBSTextRecognitionDelegate

- (SBSBarcodePickerState)barcodePicker:(SBSBarcodePicker *)picker
                      didRecognizeText:(SBSRecognizedText *)text {
    NSDictionary *dict = @{ @"jsonString": text.text };
    CDVPluginResult *pluginResult = [self createResultForEvent:@"didRecognizeText" value:dict];
    int nextState = [self sendPluginResultBlocking:pluginResult];
    if (!self.continuousMode) {
        nextState = SBSPickerStateStopped;
    }
    SBSBarcodePickerState state = [self switchRecognitionTextToNextState:nextState];

    NSArray* newlyRecognized = session.newlyRecognizedCodes;
    if (self.rejectedCodeIds.count > 0) {
        text.rejected = YES;
    }

    if (!self.continuousMode) {
        dispatch_main_sync_safe(^{
            if (self.modallyPresented) {
                [self.viewController dismissViewControllerAnimated:YES completion:nil];
            } else {
                [self.picker removeFromParentViewController];
                [self.picker.view removeFromSuperview];
                [self.picker didMoveToParentViewController:nil];
            }
            self.pickerStateMachine = nil;
            self.hasPendingOperation = NO;
        });
    }

    return SBSBarcodePickerStateStopped;
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
            [self.viewController dismissViewControllerAnimated:YES completion:nil];
        } else {
            [self.picker removeFromParentViewController];
            [self.picker.view removeFromSuperview];
            [self.picker didMoveToParentViewController:nil];
        }
    });
    self.pickerStateMachine = nil;
    [self sendCancelEvent];
    self.hasPendingOperation = NO;
}


#pragma mark - ScanditSDKSearchBarDelegate

- (void)searchExecutedWithContent:(NSString *)content {
    CDVPluginResult *pluginResult;
    if (self.legacyMode) {
        NSArray *result = [[NSArray alloc] initWithObjects:content, "UNKNOWN", nil];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                          messageAsArray:result];
        [pluginResult setKeepCallback:@YES];
    } else {
        pluginResult = [self createResultForEvent:@"didManualSearch" value:content];
    }

    if (!self.continuousMode) {
        [self.pickerStateMachine setDesiredState:SBSPickerStateStopped];
        if (self.modallyPresented) {
            [self.viewController dismissViewControllerAnimated:YES completion:nil];
        } else {
            [self.picker removeFromParentViewController];
            [self.picker.view removeFromSuperview];
            [self.picker didMoveToParentViewController:nil];
        }
        self.pickerStateMachine = nil;
        self.hasPendingOperation = NO;
    } else {
        [self.picker.overlayController resetUI];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

@end
