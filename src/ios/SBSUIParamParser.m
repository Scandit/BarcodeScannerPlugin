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
#import "SBSUIParamParser.h"

#import "SBSPhonegapParamParser.h"


@interface SBSUIParamParser ()
+ (BOOL)array:(NSArray *)array onlyContainObjectsOfClass:(Class)class;
@end

@implementation SBSUIParamParser

+ (NSString *)paramBeep { return [@"beep" lowercaseString]; }
+ (NSString *)paramVibrate { return [@"vibrate" lowercaseString]; }

#pragma mark - Torch Params
+ (NSString *)paramTorch { return [@"torch" lowercaseString]; }
+ (NSString *)paramTorchButtonMarginsAndSize { return [@"torchButtonMarginsAndSize" lowercaseString]; }
+ (NSString *)paramTorchButtonOffAccessibilityLabel { return [@"torchButtonOffAccessibilityLabel" lowercaseString]; }
+ (NSString *)paramTorchButtonOffAccessibilityHint { return [@"torchButtonOffAccessibilityLabel" lowercaseString]; }
+ (NSString *)paramTorchButtonOnAccessibilityLabel { return [@"torchButtonOffAccessibilityLabel" lowercaseString]; }
+ (NSString *)paramTorchButtonOnAccessibilityHint { return [@"torchButtonOffAccessibilityLabel" lowercaseString]; }

#pragma mark - Camera Switch Params
+ (NSString *)paramCameraSwitchVisibility { return [@"cameraSwitchVisibility" lowercaseString]; }
+ (NSString *)paramCameraSwitchButtonMarginsAndSize { return [@"cameraSwitchButtonMarginsAndSize" lowercaseString]; }
+ (NSString *)paramCameraSwitchButtonBackAccessibilityLabel {
    return [@"cameraSwitchButtonBackAccessibilityLabel" lowercaseString];
}
+ (NSString *)paramCameraSwitchButtonBackAccessibilityHint {
    return [@"cameraSwitchButtonBackAccessibilityHint" lowercaseString];
}
+ (NSString *)paramCameraSwitchButtonFrontAccessibilityLabel {
    return [@"cameraSwitchButtonFrontAccessibilityLabel" lowercaseString];
}
+ (NSString *)paramCameraSwitchButtonFrontAccessibilityHint {
    return [@"cameraSwitchButtonFrontAccessibilityHint" lowercaseString];
}

#pragma mark - Other Params
+ (NSString *)paramToolBarButtonCaption { return [@"toolBarButtonCaption" lowercaseString]; }
+ (NSString *)paramViewfinderDimension { return [@"viewfinderDimension" lowercaseString]; }
+ (NSString *)paramViewfinderColor { return [@"viewfinderColor" lowercaseString]; }
+ (NSString *)paramViewfinderDecodedColor { return [@"viewfinderDecodedColor" lowercaseString]; }
+ (NSString *)paramLogoOffsets { return [@"logoOffsets" lowercaseString]; }
+ (NSString *)paramZoom { return [@"zoom" lowercaseString]; }
+ (NSString *)paramGuiStyle { return [@"guiStyle" lowercaseString]; }
+ (NSString *)paramProperties { return [@"properties" lowercaseString]; }
+ (NSString *)paramTextRecognitionSwitchVisible { return [@"textRecognitionSwitchVisible" lowercaseString]; }
+ (NSString *)paramMissingCameraPermissionInfoText { return [@"missingCameraPermissionInfoText" lowercaseString]; }


#pragma mark - Picker Updates

+ (void)updatePickerUI:(SBSBarcodePicker *)picker fromOptions:(NSDictionary *)options {

    NSObject *beep = [options objectForKey:[self paramBeep]];
    if (beep) {
        if ([beep isKindOfClass:[NSNumber class]]) {
            [picker.overlayController setBeepEnabled:[((NSNumber *)beep) boolValue]];
        } else {
            NSLog(@"SBS Plugin: failed to parse beep - wrong type");
        }
    }

    NSObject *vibrate = [options objectForKey:[self paramVibrate]];
    if (vibrate) {
        if ([vibrate isKindOfClass:[NSNumber class]]) {
            [picker.overlayController setVibrateEnabled:[((NSNumber *)vibrate) boolValue]];
        } else {
            NSLog(@"SBS Plugin: failed to parse vibrate - wrong type");
        }
    }

    NSObject *torch = [options objectForKey:[self paramTorch]];
    if (torch) {
        if ([torch isKindOfClass:[NSNumber class]]) {
            [picker.overlayController setTorchEnabled:[((NSNumber *)torch) boolValue]];
        } else {
            NSLog(@"SBS Plugin: failed to parse torch - wrong type");
        }
    }

    NSObject *torchButtonMarginsAndSize = [options objectForKey:[self paramTorchButtonMarginsAndSize]];
    if (torchButtonMarginsAndSize) {
        if ([torchButtonMarginsAndSize isKindOfClass:[NSArray class]]) {
            NSArray *marginsAndSizeArray = (NSArray *)torchButtonMarginsAndSize;
            if ([marginsAndSizeArray count] == 4
                && ([self array:marginsAndSizeArray onlyContainObjectsOfClass:[NSNumber class]]
                    || [self array:marginsAndSizeArray onlyContainObjectsOfClass:[NSString class]])) {
                    [picker.overlayController setTorchButtonLeftMargin:[self getSize:marginsAndSizeArray[0] relativeTo:0]
                                                             topMargin:[self getSize:marginsAndSizeArray[1] relativeTo:0]
                                                                 width:[self getSize:marginsAndSizeArray[2] relativeTo:0]
                                                                height:[self getSize:marginsAndSizeArray[3] relativeTo:0]];
                }
        } else {
            NSLog(@"SBS Plugin: failed to parse torch button margins and size - wrong type");
        }
    }

    NSObject *torchOffAccLabel = [options objectForKey:[self paramTorchButtonOffAccessibilityLabel]];
    NSObject *torchOffAccHint = [options objectForKey:[self paramTorchButtonOffAccessibilityHint]];
    if (torchOffAccLabel || torchOffAccHint) {
        NSString *label = @"";
        if (torchOffAccLabel && [torchOffAccLabel isKindOfClass:[NSString class]]) {
            label = (NSString *) torchOffAccLabel;
        }
        NSString *hint = @"";
        if (torchOffAccHint && [torchOffAccHint isKindOfClass:[NSString class]]) {
            hint = (NSString *) torchOffAccHint;
        }
        [picker.overlayController setTorchOffButtonAccessibilityLabel:label hint:hint];
    }

    NSObject *torchOnAccLabel = [options objectForKey:[self paramTorchButtonOnAccessibilityLabel]];
    NSObject *torchOnAccHint = [options objectForKey:[self paramTorchButtonOnAccessibilityHint]];
    if (torchOnAccLabel || torchOnAccHint) {
        NSString *label = @"";
        if (torchOnAccLabel && [torchOnAccLabel isKindOfClass:[NSString class]]) {
            label = (NSString *) torchOnAccLabel;
        }
        NSString *hint = @"";
        if (torchOnAccHint && [torchOnAccHint isKindOfClass:[NSString class]]) {
            hint = (NSString *) torchOnAccHint;
        }
        [picker.overlayController setTorchOnButtonAccessibilityLabel:label hint:hint];
    }

    NSObject *cameraSwitchVisibility = [options objectForKey:[self paramCameraSwitchVisibility]];
    if (cameraSwitchVisibility) {
        if ([cameraSwitchVisibility isKindOfClass:[NSNumber class]]) {
            switch ([(NSNumber *)cameraSwitchVisibility integerValue]) {
                case 0:
                    [picker.overlayController setCameraSwitchVisibility:SBSCameraSwitchVisibilityNever];
                    break;

                case 1:
                    [picker.overlayController setCameraSwitchVisibility:SBSCameraSwitchVisibilityOnTablet];
                    break;

                case 2:
                    [picker.overlayController setCameraSwitchVisibility:SBSCameraSwitchVisibilityAlways];
                    break;
            }
        } else {
            NSLog(@"SBS Plugin: failed to parse camera switch visibility - wrong type");
        }
    }

    NSObject *cameraSwitchButtonMarginsAndSize = [options objectForKey:
                                                  [self paramCameraSwitchButtonMarginsAndSize]];
    if (cameraSwitchButtonMarginsAndSize) {
        if ([cameraSwitchButtonMarginsAndSize isKindOfClass:[NSArray class]]) {
            NSArray *marginsAndSizeArray = (NSArray *)cameraSwitchButtonMarginsAndSize;
            if ([marginsAndSizeArray count] == 4
                && ([self array:marginsAndSizeArray onlyContainObjectsOfClass:[NSNumber class]]
                    || [self array:marginsAndSizeArray onlyContainObjectsOfClass:[NSString class]])) {
                    [picker.overlayController setCameraSwitchButtonRightMargin:[self getSize:marginsAndSizeArray[0] relativeTo:0]
                                                                     topMargin:[self getSize:marginsAndSizeArray[1] relativeTo:0]
                                                                         width:[self getSize:marginsAndSizeArray[2] relativeTo:0]
                                                                        height:[self getSize:marginsAndSizeArray[3] relativeTo:0]];
                }
        } else {
            NSLog(@"SBS Plugin: failed to parse camera switch button margins and size - wrong type");
        }
    }

    NSObject *cameraBackAccLabel = [options objectForKey:[self paramCameraSwitchButtonBackAccessibilityLabel]];
    NSObject *cameraBackAccHint = [options objectForKey:[self paramCameraSwitchButtonBackAccessibilityHint]];
    if (cameraBackAccLabel || cameraBackAccHint) {
        NSString *label = @"";
        if (cameraBackAccLabel && [cameraBackAccLabel isKindOfClass:[NSString class]]) {
            label = (NSString *) cameraBackAccLabel;
        }
        NSString *hint = @"";
        if (cameraBackAccHint && [cameraBackAccHint isKindOfClass:[NSString class]]) {
            hint = (NSString *) cameraBackAccHint;
        }
        [picker.overlayController setCameraSwitchButtonBackAccessibilityLabel:label hint:hint];
    }

    NSObject *cameraFrontAccLabel = [options objectForKey:[self paramCameraSwitchButtonFrontAccessibilityLabel]];
    NSObject *cameraFrontAccHint = [options objectForKey:[self paramCameraSwitchButtonFrontAccessibilityHint]];
    if (cameraFrontAccLabel || cameraFrontAccHint) {
        NSString *label = @"";
        if (cameraFrontAccLabel && [cameraFrontAccLabel isKindOfClass:[NSString class]]) {
            label = (NSString *) cameraFrontAccLabel;
        }
        NSString *hint = @"";
        if (cameraFrontAccHint && [cameraFrontAccHint isKindOfClass:[NSString class]]) {
            hint = (NSString *) cameraFrontAccHint;
        }
        [picker.overlayController setCameraSwitchButtonFrontAccessibilityLabel:label hint:hint];
    }

    NSObject *guiStyle = [options objectForKey:[self paramGuiStyle]];
    if (guiStyle) {
        if ([guiStyle isKindOfClass:[NSNumber class]]) {
            switch ([(NSNumber *)guiStyle integerValue]) {
                case 0:
                    picker.overlayController.guiStyle = SBSGuiStyleDefault;
                    break;
                case 1:
                    picker.overlayController.guiStyle = SBSGuiStyleLaser;
                    break;
                case 2:
                    picker.overlayController.guiStyle = SBSGuiStyleNone;
                    break;
                case 3:
                    picker.overlayController.guiStyle = SBSGuiStyleMatrixScan;
                    break;
                case 4:
                    picker.overlayController.guiStyle = SBSGuiStyleLocationsOnly;
                    break;
                default:
                    picker.overlayController.guiStyle = SBSGuiStyleDefault;
                    break;
            }
        } else {
            NSLog(@"SBS Plugin: failed to parse gui style - wrong type");
        }
    }

    NSObject *viewfinderSize = [options objectForKey:[self paramViewfinderDimension]];
    if (viewfinderSize) {
        if ([viewfinderSize isKindOfClass:[NSArray class]]) {
            NSArray *vfSizeArray = (NSArray *)viewfinderSize;
            if ([vfSizeArray count] == 4 && [self array:vfSizeArray
                              onlyContainObjectsOfClass:[NSNumber class]]) {
                [picker.overlayController setViewfinderWidth:[vfSizeArray[0] floatValue]
                                                      height:[vfSizeArray[1] floatValue]
                                              landscapeWidth:[vfSizeArray[2] floatValue]
                                             landscapeHeight:[vfSizeArray[3] floatValue]];
            }
        } else {
            NSLog(@"SBS Plugin: failed to parse viewfinder size - wrong type");
        }
    }

    NSObject *viewfinderColor = [options objectForKey:[self paramViewfinderColor]];
    if (viewfinderColor) {
        if ([viewfinderColor isKindOfClass:[NSString class]]) {
            NSString *viewfinderColorString = (NSString *)viewfinderColor;
            if ([viewfinderColorString length] == 6) {
                unsigned int redInt;
                NSScanner *redScanner = [NSScanner scannerWithString:
                                         [viewfinderColorString substringToIndex:2]];
                [redScanner scanHexInt:&redInt];
                float red = ((float) redInt) / 256.0;

                unsigned int greenInt;
                NSScanner *greenScanner = [NSScanner scannerWithString:
                                           [[viewfinderColorString substringFromIndex:2] substringToIndex:2]];
                [greenScanner scanHexInt:&greenInt];
                float green = ((float) greenInt) / 256.0;

                unsigned int blueInt;
                NSScanner *blueScanner = [NSScanner scannerWithString:
                                          [viewfinderColorString substringFromIndex:4]];
                [blueScanner scanHexInt:&blueInt];
                float blue = ((float) blueInt) / 256.0;

                [picker.overlayController setViewfinderColor:red green:green blue:blue];
            }
        } else {
            NSLog(@"SBS Plugin: failed to parse viewfinder color - wrong type");
        }
    }

    NSObject *decodedColor = [options objectForKey:[self paramViewfinderDecodedColor]];
    if (decodedColor) {
        if ([decodedColor isKindOfClass:[NSString class]]) {
            NSString *decodedColorString = (NSString *)decodedColor;
            if ([decodedColorString length] == 6) {
                unsigned int redInt;
                NSScanner *redScanner = [NSScanner scannerWithString:
                                         [decodedColorString substringToIndex:2]];
                [redScanner scanHexInt:&redInt];
                float red = ((float) redInt) / 256.0;

                unsigned int greenInt;
                NSScanner *greenScanner = [NSScanner scannerWithString:
                                           [[decodedColorString substringFromIndex:2] substringToIndex:2]];
                [greenScanner scanHexInt:&greenInt];
                float green = ((float) greenInt) / 256.0;

                unsigned int blueInt;
                NSScanner *blueScanner = [NSScanner scannerWithString:
                                          [decodedColorString substringFromIndex:4]];
                [blueScanner scanHexInt:&blueInt];
                float blue = ((float) blueInt) / 256.0;

                [picker.overlayController setViewfinderDecodedColor:red green:green blue:blue];
            }
        } else {
            NSLog(@"SBS Plugin: failed to parse viewfinder decoded color - wrong type");
        }
    }

    NSObject *toolbarCaption = [options objectForKey:[self paramToolBarButtonCaption]];
    if (toolbarCaption) {
        if([toolbarCaption isKindOfClass:[NSString class]]) {
            [picker.overlayController setToolBarButtonCaption:((NSString *) toolbarCaption)];
        } else {
            NSLog(@"SBS Plugin: failed to parse toolbar caption - wrong type");
        }
    }
    
    NSObject *textRecognitionSwitchVisible = [options objectForKey:[self paramTextRecognitionSwitchVisible]];
    if (textRecognitionSwitchVisible) {
        if ([textRecognitionSwitchVisible isKindOfClass:[NSNumber class]]) {
            [picker.overlayController setTextRecognitionSwitchVisible:[((NSNumber *)textRecognitionSwitchVisible) boolValue]];
        } else {
            NSLog(@"SBS Plugin: failed to parse text recognition switch visibility - wrong type");
        }
    }

    NSObject *missingCameraPermissionInfoText = options[[self paramMissingCameraPermissionInfoText]];
    if (missingCameraPermissionInfoText) {
        if ([missingCameraPermissionInfoText isKindOfClass:[NSString class]]) {
            [picker.overlayController setMissingCameraPermissionInfoText:((NSString *)missingCameraPermissionInfoText)];
        } else {
            NSLog(@"SBS Plugin: failed to parse missing camera permission info text - wrong type");
        }
    }
}

+ (BOOL)array:(NSArray *)array onlyContainObjectsOfClass:(Class)aClass {
    for (NSObject *obj in array) {
        if (![obj isKindOfClass:aClass]) {
            return NO;
        }
    }
    return YES;
}

+ (NSNumber *)getSizeOrNull:(NSObject *)obj relativeTo:(int)max {
    if (obj) {
        return [NSNumber numberWithFloat:[SBSUIParamParser getSize:obj relativeTo:max]];
    } else {
        return nil;
    }
}

+ (float)getSize:(NSObject *)obj relativeTo:(int)max {
    if ([obj isKindOfClass:[NSNumber class]]) {
        return [(NSNumber *) obj floatValue];
    } else if ([obj isKindOfClass:[NSString class]]) {
        NSString* str = (NSString *) obj;
        if ([[str substringFromIndex: [str length] - 1] isEqualToString:@"%"]) {
            return [[str substringToIndex: [str length] - 1] floatValue] * max / 100.;
        } else {
            return [str floatValue];
        }
    } else {
        return 0.;
    }
}

@end
