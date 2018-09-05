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

#import "ScanditSDKRotatingBarcodePicker.h"


/**
 * Parameter parser for parameters that are specific to the phonegap implementation.
 */
@interface SBSPhonegapParamParser : NSObject

+ (NSString *)paramContinuousMode;
+ (NSString *)paramIsDidScanDefined;
+ (NSString *)paramShouldPassBarcodeFrame;
+ (NSString *)paramPortraitMargins;
+ (NSString *)paramLandscapeMargins;
+ (NSString *)paramPortraitConstraints;
+ (NSString *)paramLandscapeConstraints;
+ (NSString *)paramAnimationDuration;

+ (NSString *)paramPaused;


+ (void)updatePicker:(ScanditSDKRotatingBarcodePicker *)picker
         fromOptions:(NSDictionary *)options
  withSearchDelegate:(id<ScanditSDKSearchBarDelegate>)searchDelegate;

+ (NSDictionary *)updateLayoutOfPicker:(ScanditSDKRotatingBarcodePicker *)picker
                 withOptions:(NSDictionary *)options;

+ (BOOL)isPausedSpecifiedInOptions:(NSDictionary *)options;

+ (CGRect)rectFromParameter:(NSObject *)parameter;

@end
