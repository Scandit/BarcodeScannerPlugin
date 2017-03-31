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
#import <Foundation/Foundation.h>

@class ScanditSDKRotatingBarcodePicker;

typedef enum {
    // NOTE: enum values must match values defined in the JS portion of the plugin
    // (see barcodepicker.js)
    SBSPickerStatePaused = 1,
    SBSPickerStateStopped = 2,
    SBSPickerStateActive = 3
} SBSPickerState;


@protocol SBSPickerStateDelegate
-(void)picker:(ScanditSDKRotatingBarcodePicker*)picker didChangeState:(SBSPickerState)newState;
@end

/**
 * Implements a state machine for the states of the barcode picker
 */
@interface SBSPickerStateMachine : NSObject

- (instancetype)initWithPicker:(ScanditSDKRotatingBarcodePicker*)picker
                      delegate:(id<SBSPickerStateDelegate>)delegate;


/**
 * Set the desired state to start scanning but always use startScanning, even if the picker state
 * was paused. This is different from setting the desiredState property to SBSPickerStateActive.
 */
- (void)startScanningInPausedState:(BOOL)paused;


/**
 * The current state of the picker. May be different from the desired state if a state transition 
 * is in progress.
 */
@property (nonatomic, readonly) SBSPickerState state;

/**
 * The current state of the picker.
 *
 * When changing this property, the picker state change happens asynchronously. Once the picker has
 * changed its state to the desired state, picker:didChangeState: is invoked on the delegate. If
 * the picker is already in the desired state, settings this property has no effect.
 */
@property (nonatomic, assign) SBSPickerState desiredState;



@property (readonly, weak) id<SBSPickerStateDelegate> delegate;

@property (readonly, strong) ScanditSDKRotatingBarcodePicker* picker;

@end
