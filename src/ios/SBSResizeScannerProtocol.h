//
//  SBSResizeScannerProtocol.h
//  EnterpriseBrowser
//
//  Created by Franciszek Gonciarz on 04.09.2018.
//

#import <Foundation/Foundation.h>
#import "SBSConstraints.h"

@protocol SBSResizeScannerProtocol <NSObject>
- (void)scannerResized:(SBSConstraints *)portraitConstraints landscapeConstraints:(SBSConstraints *)landscapeConstraints with:(CGFloat)animationDuration;
- (void)scannerDismissed;
@end
