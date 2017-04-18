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

#import "ScanditSDKRotatingBarcodePicker.h"

#import "SBSPickerStateMachine.h"

@interface SBSPickerStateMachine ()

@end

@implementation SBSPickerStateMachine

- (void)setState:(SBSPickerState)state {
    if (state != _state) {
        _state = state;
        [self.delegate picker:self.picker didChangeState:_state];
    }
}

- (instancetype)initWithPicker:(ScanditSDKRotatingBarcodePicker*)picker
                      delegate:(id<SBSPickerStateDelegate>)delegate {
    if (self = [super init]) {
        _picker = picker;
        _delegate = delegate;
        _state = SBSPickerStateStopped;
        _desiredState = SBSPickerStateStopped;
    }
    return self;
}

- (void)setDesiredState:(SBSPickerState)state {
    if (_desiredState == state)
        return;
    _desiredState = state;
    switch (state) {
        case SBSPickerStateActive:
            [self transitionToActiveState];
            break;
        case SBSPickerStatePaused:
            [self transitionToPausedState];
            break;
        case SBSPickerStateStopped:
            [self transitionToStoppedState];
            break;
    }
}

- (void)transitionToActiveState {
    if (self.state == SBSPickerStatePaused) {
        [self.picker resumeScanning];
        self.state = SBSPickerStateActive;
    } else if (self.state == SBSPickerStateStopped) {
        [self.picker startScanningInPausedState:NO completionHandler:^{
            self.state = SBSPickerStateActive;
        }];
    }
}

- (void)transitionToPausedState {
    if (self.state == SBSPickerStateStopped) {
        [self.picker startScanningInPausedState:YES completionHandler:^{
            self.state = SBSPickerStatePaused;
        }];
    } else if (self.state == SBSPickerStateActive) {
        [self.picker pauseScanning];
        self.state = SBSPickerStatePaused;
    }
}

- (void)transitionToStoppedState {
    if (self.state == SBSPickerStateActive || self.state == SBSPickerStatePaused) {
        [self.picker stopScanningWithCompletionHandler:^{
            self.state = SBSPickerStateStopped;
        }];
    }
}

- (void)startScanningInPausedState:(BOOL)paused; {
    /*
     We don't use transitionToActiveState, because we wants to make sure to call startScanning instead of resumeScanning.
     In contrast to resumeScanning, startScanning clears the current barcode scanner session.
     */
    SBSPickerState state = paused ? SBSPickerStatePaused : SBSPickerStateActive;
    _desiredState = state;
    [self.picker startScanningInPausedState:paused completionHandler:^{
        self.state = state;
    }];
}

@end
