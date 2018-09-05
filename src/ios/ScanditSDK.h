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
#import "Cordova/CDVPlugin.h"
#import "SBSResizeScannerProtocol.h"
#import "SBSConstraints.h"

#define dispatch_main_sync_safe(block)\
if ([NSThread isMainThread]) {\
block();\
} else {\
dispatch_sync(dispatch_get_main_queue(), block);\
}


@interface ScanditSDK : CDVPlugin

- (void)initLicense:(CDVInvokedUrlCommand *)command;

- (void)show:(CDVInvokedUrlCommand *)command;

- (void)applySettings:(CDVInvokedUrlCommand *)command;

- (void)updateOverlay:(CDVInvokedUrlCommand *)command;

- (void)cancel:(CDVInvokedUrlCommand *)command;

- (void)pause:(CDVInvokedUrlCommand *)command;

- (void)resume:(CDVInvokedUrlCommand *)command;

- (void)stop:(CDVInvokedUrlCommand *)command;

- (void)start:(CDVInvokedUrlCommand *)command;

- (void)resize:(CDVInvokedUrlCommand *)command;

- (void)torch:(CDVInvokedUrlCommand *)command;

+ (void)add:(id <SBSResizeScannerProtocol>)listener to:(ScanditSDK *)scanditSdk;

@end
