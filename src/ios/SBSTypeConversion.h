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
#import <ScanditBarcodeScanner/ScanditBarcodeScanner.h>

#ifdef __cplusplus
extern "C" {
#endif

@interface SBSTrackedCode (Identifier)

@property (nonatomic, readonly) NSNumber *identifier;

@end

@interface SBSCode (UniqueId)

@property (readonly, nonatomic) long uniqueId;

@end
    
NSArray *SBSJSObjectsFromCodeArray(NSArray *codes);
NSDictionary *SBSJSObjectsFromTrackedCodes(NSDictionary<NSNumber *, SBSTrackedCode *> *trackedCodes);
NSString *SBSScanStateToString(SBSScanCaseState state);
SBSScanCaseState SBSScanStateFromString(NSString *state);
NSString *SBSScanStateChangeReasonToString(SBSScanCaseStateChangeReason reason);

#ifdef __cplusplus
}
#endif
