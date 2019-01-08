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

#import "ScanditSDK.h"


@interface SBSBarcodePicker (extended)
- (id)initWithSettings:(SBSScanSettings *)settings
    runningOnFramework:(NSString *)usedFramework;
@end

@interface ScanditSDKRotatingBarcodePicker() <UISearchBarDelegate>
@property (nonatomic, strong) ScanditSDKSearchBar *manualSearchBar;
@property (nonatomic, strong) UIView *statusBarBackground;
@property (nonatomic, assign) BOOL didSetSize;
@property (nonatomic, strong) NSLayoutConstraint *leftConstraint;
@property (nonatomic, strong) NSLayoutConstraint *topConstraint;
@property (nonatomic, strong) NSLayoutConstraint *rightConstraint;
@property (nonatomic, strong) NSLayoutConstraint *bottomConstraint;
@property (nonatomic, strong) NSLayoutConstraint *widthConstraint;
@property (nonatomic, strong) NSLayoutConstraint *heightConstraint;
@property (nonatomic, strong) UITapGestureRecognizer *tapRecognizer;
@end


@implementation ScanditSDKRotatingBarcodePicker

- (instancetype)initWithSettings:(SBSScanSettings *)settings {
    self = [super initWithSettings:settings];
    if (self) {
        _portraitConstraints = [[SBSConstraints alloc] init];
        _landscapeConstraints = [[SBSConstraints alloc] init];
    }
    return self;
}

#pragma mark - Orientation Changes & Margin Adjustment

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator {
    [super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];
    UIInterfaceOrientation orientation;
    if (size.width > size.height) {
        orientation = UIInterfaceOrientationLandscapeLeft;
    } else {
        orientation = UIInterfaceOrientationPortrait;
    }
    [self adjustSize:0 newOrientation:orientation];
}

- (void)adjustSize:(CGFloat)animationDuration {
    [self adjustSize:animationDuration newOrientation:UIInterfaceOrientationUnknown];
}

- (void)adjustSize:(CGFloat)animationDuration newOrientation:(UIInterfaceOrientation)newOrientation {
    if (self.parentViewController && self.view.superview) {
        
        [UIView animateWithDuration:animationDuration animations:^{
            SBSConstraints *constraints = self.portraitConstraints;
            UIInterfaceOrientation current = [[UIApplication sharedApplication] statusBarOrientation];
            if ((newOrientation == UIInterfaceOrientationUnknown
                && UIInterfaceOrientationIsLandscape(current))
                || UIInterfaceOrientationIsLandscape(newOrientation)) {
                constraints = self.landscapeConstraints;
            }
            
            [self.view setTranslatesAutoresizingMaskIntoConstraints:NO];
            
            // Remove all no longer needed constraints.
            if (!constraints.leftMargin && self.leftConstraint) {
                [self.view.superview removeConstraint:self.leftConstraint];
            }
            if (!constraints.topMargin && self.topConstraint) {
                [self.view.superview removeConstraint:self.topConstraint];
            }
            if (!constraints.rightMargin && self.rightConstraint) {
                [self.view.superview removeConstraint:self.rightConstraint];
            }
            if (!constraints.bottomMargin && self.bottomConstraint) {
                [self.view.superview removeConstraint:self.bottomConstraint];
            }
            if (!constraints.width && self.widthConstraint) {
                [self.view.superview removeConstraint:self.widthConstraint];
            }
            if (!constraints.height && self.heightConstraint) {
                [self.view.superview removeConstraint:self.heightConstraint];
            }
            
            if (constraints.leftMargin) {
                if (!self.leftConstraint) {
                    self.leftConstraint = [NSLayoutConstraint
                                           constraintWithItem:self.view
                                           attribute:NSLayoutAttributeLeading
                                           relatedBy:NSLayoutRelationEqual
                                           toItem:self.view.superview
                                           attribute:NSLayoutAttributeLeading
                                           multiplier:1.0
                                           constant:[constraints.leftMargin floatValue]];
                    [self.view.superview addConstraint:self.leftConstraint];
                } else {
                    self.leftConstraint.constant = [constraints.leftMargin floatValue];
                }
            }
            
            if (constraints.topMargin) {
                if (!self.topConstraint) {
                    self.topConstraint = [NSLayoutConstraint
                                          constraintWithItem:self.view
                                          attribute:NSLayoutAttributeTop
                                          relatedBy:NSLayoutRelationEqual
                                          toItem:self.view.superview
                                          attribute:NSLayoutAttributeTop
                                          multiplier:1.0
                                          constant:[constraints.topMargin floatValue]];
                    [self.view.superview addConstraint:self.topConstraint];
                } else {
                    self.topConstraint.constant = [constraints.topMargin floatValue];
                }
            }
            
            if (constraints.rightMargin) {
                if (!self.rightConstraint) {
                    self.rightConstraint = [NSLayoutConstraint
                                            constraintWithItem:self.view
                                            attribute:NSLayoutAttributeTrailing
                                            relatedBy:NSLayoutRelationEqual
                                            toItem:self.view.superview
                                            attribute:NSLayoutAttributeTrailing
                                            multiplier:1.0
                                            constant:-[constraints.rightMargin floatValue]];
                    [self.view.superview addConstraint:self.rightConstraint];
                } else {
                    self.rightConstraint.constant = -[constraints.rightMargin floatValue];
                }
            }
            
            if (constraints.bottomMargin) {
                if (!self.bottomConstraint) {
                    self.bottomConstraint = [NSLayoutConstraint
                                             constraintWithItem:self.view
                                             attribute:NSLayoutAttributeBottom
                                             relatedBy:NSLayoutRelationEqual
                                             toItem:self.view.superview
                                             attribute:NSLayoutAttributeBottom
                                             multiplier:1.0
                                             constant:-[constraints.bottomMargin floatValue]];
                    [self.view.superview addConstraint:self.bottomConstraint];
                } else {
                    self.bottomConstraint.constant = -[constraints.bottomMargin floatValue];
                }
            }
            
            if (constraints.width) {
                if (!self.widthConstraint) {
                    self.widthConstraint = [NSLayoutConstraint
                                            constraintWithItem:self.view
                                            attribute:NSLayoutAttributeWidth
                                            relatedBy:NSLayoutRelationEqual
                                            toItem:nil
                                            attribute:NSLayoutAttributeNotAnAttribute
                                            multiplier:1.0
                                            constant:[constraints.width floatValue]];
                    [self.view.superview addConstraint:self.widthConstraint];
                } else {
                    self.widthConstraint.constant = [constraints.width floatValue];
                }
            }
            
            if (constraints.height) {
                if (!self.heightConstraint) {
                    self.heightConstraint = [NSLayoutConstraint
                                             constraintWithItem:self.view
                                             attribute:NSLayoutAttributeHeight
                                             relatedBy:NSLayoutRelationEqual
                                             toItem:nil
                                             attribute:NSLayoutAttributeNotAnAttribute
                                             multiplier:1.0
                                             constant:[constraints.height floatValue]];
                    [self.view.superview addConstraint:self.heightConstraint];
                } else {
                    self.heightConstraint.constant = [constraints.height floatValue];
                }
            }
            
            [self.view layoutIfNeeded];
        }];
    }
}

- (void)setTrackedCodeStates:(NSDictionary *)trackedCodeStates {
    // Extend to draw custom views.
}

#pragma mark - Search Bar

- (void)showSearchBar:(BOOL)show {
    void (^block)(void) = ^void() {
        if (!show && self.manualSearchBar) {
            [self.manualSearchBar removeFromSuperview];
            self.manualSearchBar = nil;
            if (self.statusBarBackground) {
                [self.statusBarBackground removeFromSuperview];
                self.statusBarBackground = nil;
            }
            
            [self.overlayController setTorchButtonLeftMargin:15 topMargin:15 width:40 height:40];
            [self.overlayController setCameraSwitchButtonRightMargin:15 topMargin:15 width:40 height:40];
            
        } else if (show && !self.manualSearchBar) {
            [self createManualSearchBar];
            
            [self.overlayController setTorchButtonLeftMargin:15 topMargin:15 + 44 width:40 height:40];
            [self.overlayController setCameraSwitchButtonRightMargin:15 topMargin:15 + 44 width:40 height:40];
        }
    };
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}

- (void)createManualSearchBar {
    self.manualSearchBar = [[ScanditSDKSearchBar alloc] init];
    [self.manualSearchBar setDelegate:self];
    [self.manualSearchBar setTranslucent:YES];
    
    self.statusBarBackground = [[UIView alloc] init];
    self.statusBarBackground.backgroundColor = [UIColor whiteColor];

    [self.view addSubview:self.manualSearchBar];
    
    [self setConstraintsForView:self.manualSearchBar toHorizontallyMatch:self.view];
    [self.view addConstraint:[self topGuideConstraintForView:self.manualSearchBar
                                                     toMatch:self
                                                withConstant:0.0f]];
    
    if (self.statusBarBackground) {
        [self.view addSubview:self.statusBarBackground];
        [self setConstraintsForView:self.statusBarBackground toHorizontallyMatch:self.view];
        
        [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.statusBarBackground
                                                              attribute:NSLayoutAttributeTop
                                                              relatedBy:NSLayoutRelationEqual
                                                                 toItem:self.view
                                                              attribute:NSLayoutAttributeTop
                                                             multiplier:1.0
                                                               constant:0.0]];
        [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.statusBarBackground
                                                              attribute:NSLayoutAttributeBottom
                                                              relatedBy:NSLayoutRelationEqual
                                                                 toItem:self.manualSearchBar
                                                              attribute:NSLayoutAttributeTop
                                                             multiplier:1.0
                                                               constant:0.0]];
    }
}

- (void)setSearchBarKeyboardType:(UIKeyboardType)keyboardType {
    void (^block)(void) = ^void() {
        if (self.manualSearchBar) {
            [self.manualSearchBar setKeyboardType:keyboardType];
        }
    };
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}

- (NSLayoutConstraint *)topGuideConstraintForView:(UIView *)view
                                          toMatch:(UIViewController *)controller
                                     withConstant:(CGFloat)constant {
    return [NSLayoutConstraint constraintWithItem:view
                                        attribute:NSLayoutAttributeTop
                                        relatedBy:NSLayoutRelationEqual
                                           toItem:controller.topLayoutGuide
                                        attribute:NSLayoutAttributeBottom
                                       multiplier:1.0
                                         constant:constant];
}

- (void)setConstraintsForView:(UIView *)view
          toHorizontallyMatch:(UIView *)refView {
    view.translatesAutoresizingMaskIntoConstraints = NO;
    NSLayoutConstraint *constraint;
    constraint = [NSLayoutConstraint constraintWithItem:view
                                              attribute:NSLayoutAttributeLeading
                                              relatedBy:NSLayoutRelationEqual
                                                 toItem:refView
                                              attribute:NSLayoutAttributeLeading
                                             multiplier:1.0
                                               constant:0];
    [refView addConstraint:constraint];
    constraint = [NSLayoutConstraint constraintWithItem:view
                                              attribute:NSLayoutAttributeTrailing
                                              relatedBy:NSLayoutRelationEqual
                                                 toItem:refView
                                              attribute:NSLayoutAttributeTrailing
                                             multiplier:1.0
                                               constant:0];
    [refView addConstraint:constraint];
}


#pragma mark - UISearchBarDelegate methods

/*
 * The search bar feature has been deprecated and is now only available when using the "old"
 * ScanditSDK interface. That's why these methods are defined here rather than in the base
 * class.
 */
- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchbar {
    [searchbar setShowsCancelButton:YES animated:YES];
    [((ScanditSDKSearchBar *)self.manualSearchBar) updateCancelButton];
    
    self.tapRecognizer = [[UITapGestureRecognizer alloc]
                          initWithTarget:self
                          action:@selector(cancelSearch)];
    [self.view addGestureRecognizer:self.tapRecognizer];
}

- (void)searchBarTextDidEndEditing:(UISearchBar *)searchbar {
    [searchbar setShowsCancelButton:NO animated:YES];
    [searchbar resignFirstResponder];
    [self.view removeGestureRecognizer:self.tapRecognizer];
    self.tapRecognizer = nil;
}

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchbar {
    [searchbar resignFirstResponder];
    searchbar.text = @"";
}

- (void)searchBar:(UISearchBar *)searchbar textDidChange:(NSString *)searchText {
    [((ScanditSDKSearchBar *)self.manualSearchBar) updateCancelButton];
}

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchbar {
    [searchbar resignFirstResponder];
    [searchbar setShowsCancelButton:NO animated:YES];
    [self.searchDelegate searchExecutedWithContent:searchbar.text];
}

- (void)cancelSearch {
    [self.manualSearchBar resignFirstResponder];
}

@end
