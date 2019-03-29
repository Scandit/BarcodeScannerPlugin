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
#import "SBSTypeConversion.h"
#import <ScanditBarcodeScanner/ScanditBarcodeScanner.h>

@interface SBSCode (Handle)

// private property to get the underlying data handle. Used for generating unique Ids.
@property (readonly, nonatomic) void *handle;

@end

@implementation SBSCode (UniqueId)

- (long)uniqueId {
    return (long)self.handle;
}

@end

static NSMutableDictionary *SBSJSObjectsFromCode(SBSCode *code) {
    NSInteger identifier = code.uniqueId;
    if ([code isKindOfClass:[SBSTrackedCode class]]) {
        identifier = ((SBSTrackedCode *)code).identifier.integerValue;
    }
    NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                 [code symbologyName], @"symbology",
                                 [NSNumber numberWithLong:identifier], @"uniqueId",
                                 [NSNumber numberWithBool:[code isGs1DataCarrier]], @"gs1DataCarrier",
                                 [NSNumber numberWithBool:[code isRecognized]], @"recognized", nil];
    [dict setObject:@(code.compositeFlag) forKey:@"compositeFlag"];
    if ([code isRecognized]) {
        if (code.data == nil) {
            [dict setObject:@"" forKey:@"data"];
        } else {
            [dict setObject:code.data forKey:@"data"];
        }
        // convert raw data to array of integers
        NSData *rawData = code.rawData;
        NSMutableArray *rawDataAsIntArray = [NSMutableArray arrayWithCapacity:rawData.length];
        const uint8_t *bytes = (const uint8_t*)[rawData bytes];
        for (NSUInteger i = 0; i < rawData.length; ++i) {
            int byte = bytes[i];
            [rawDataAsIntArray addObject:@(byte)];
        }
        [dict setObject:rawDataAsIntArray forKey:@"rawData"];
    }
    return dict;
}

//static NSDictionary *SBSJsonObjectFromPoint(CGPoint point) {
//    return @{
//             @"x": @(point.x),
//             @"y": @(point.y),
//             };
//}
//
//static NSDictionary *SBSJsonObjectFromQuadrilateral(SBSQuadrilateral quadrilateral) {
//    return @{
//             @"topLeft": SBSJsonObjectFromPoint(quadrilateral.topLeft),
//             @"topRight": SBSJsonObjectFromPoint(quadrilateral.topRight),
//             @"bottomLeft": SBSJsonObjectFromPoint(quadrilateral.bottomLeft),
//             @"bottomRight": SBSJsonObjectFromPoint(quadrilateral.bottomRight)
//             };
//}

NSArray *SBSJSObjectsFromCodeArray(NSArray *codes) {
    NSMutableArray *finalArray = [[NSMutableArray alloc] initWithCapacity:codes.count];
    for (SBSCode *code in codes) {
        [finalArray addObject:SBSJSObjectsFromCode(code)];
    }
    return finalArray;
}

NSString *SBSScanStateToString(SBSScanCaseState state) {
    switch (state) {
        case SBSScanCaseStateActive:
            return @"active";
        case SBSScanCaseStateOff:
            return @"off";
        case SBSScanCaseStateStandby:
            return @"standby";
    }
    return @"unknown";
}

SBSScanCaseState SBSScanStateFromString(NSString *state) {
    if ([state isEqualToString:@"active"])
        return SBSScanCaseStateActive;
    if ([state isEqualToString:@"standby"])
        return SBSScanCaseStateStandby;
    if ([state isEqualToString:@"off"])
        return SBSScanCaseStateOff;
    return SBSScanCaseStateOff;
}

NSString *SBSScanStateChangeReasonToString(SBSScanCaseStateChangeReason reason) {
    switch (reason) {
        case SBSScanCaseStateChangeReasonManual:
            return @"manual";
        case SBSScanCaseStateChangeReasonTimeout:
            return @"timeout";
        case SBSScanCaseStateChangeReasonVolumeButton:
            return @"volumeButton";
    }
    return @"unknown";
}
