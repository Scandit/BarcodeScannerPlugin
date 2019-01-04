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

#import "ScanditSDKSearchBar.h"

@interface ScanditSDKSearchBar ()
@property (nonatomic, strong) UIToolbar *keyboardToolbar;
@end

@implementation ScanditSDKSearchBar

- (instancetype)init {
    if (self = [super init]) {
        [self internalInit];
    }
    return self;
}

- (void)restoreDefaults {
    self.goButtonCaption = @"Go";
    self.cancelButtonCaption = @"Cancel";
    self.minTextLengthForSearch = 2;
    self.maxTextLengthForSearch = 100;
}

- (void)internalInit {
    [self restoreDefaults];
    
    NSString *placeholder = @"Scan barcode or enter it here";
    UIKeyboardType keyboardType = UIKeyboardTypeNumberPad;
    
    self.text = @"";
    [self setPlaceholder:placeholder];
    [self setKeyboardType:keyboardType];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self
                                                     attribute:NSLayoutAttributeHeight
                                                     relatedBy:NSLayoutRelationEqual
                                                        toItem:nil
                                                     attribute:NSLayoutAttributeNotAnAttribute
                                                    multiplier:1.0
                                                      constant:44.0]];
}

- (void)setKeyboardType:(UIKeyboardType)keyboardType {
    [super setKeyboardType:keyboardType];
    [self updateInputAccessoryView];
}

- (void)setGoButtonCaption:(NSString *)goButtonCaption {
    _goButtonCaption = goButtonCaption;
    [self updateInputAccessoryView];
}

- (void)setCancelButtonCaption:(NSString *)cancelButtonCaption {
    _cancelButtonCaption = cancelButtonCaption;
    [self updateCancelButton];
}

- (void)updateInputAccessoryView {
    if (self.keyboardType == UIKeyboardTypeNumberPad
            || self.keyboardType == UIKeyboardTypeDecimalPad) {
        self.keyboardToolbar = [[UIToolbar alloc]initWithFrame:CGRectMake(0, 0, 320, 50)];
        self.keyboardToolbar.barStyle = UIBarStyleBlack;
        self.keyboardToolbar.items = @[[[UIBarButtonItem alloc]
                                        initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace
                                        target:nil
                                        action:nil],
                                       [[UIBarButtonItem alloc]
                                        initWithTitle:self.goButtonCaption
                                        style:UIBarButtonItemStyleDone
                                        target:self
                                        action:@selector(doneWithNumberPad)]];
        [self.keyboardToolbar sizeToFit];
        [self setInputAccessoryView:self.keyboardToolbar];
    } else {
        [self setInputAccessoryView:nil];
    }
}

- (void)doneWithNumberPad {
    [self resignFirstResponder];
    [self setShowsCancelButton:NO animated:YES];
    [self.delegate searchBarSearchButtonClicked:self];
}

/**
 * Changes the button to the right of the manual entry field between 'Cancel' and 'USE' depending on
 * whether the length of the entered text is long enough for a valid barcode.
 */
- (void)updateCancelButton {
    id barButtonAppearanceInSearchBar;
    if (@available(iOS 9.0, *)) {
        barButtonAppearanceInSearchBar = [UIBarButtonItem appearanceWhenContainedInInstancesOfClasses:@[[ScanditSDKSearchBar class]]];
    } else {
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
        barButtonAppearanceInSearchBar = [UIBarButtonItem appearanceWhenContainedIn:[ScanditSDKSearchBar class], nil];
#pragma GCC diagnostic pop
    }
    
    // Adjust whether the "Go" button can be clicked.
    if (barButtonAppearanceInSearchBar != nil) {
        if (self.text.length >= self.minTextLengthForSearch
                && self.text.length <= self.maxTextLengthForSearch) {
            [[[self.keyboardToolbar items] objectAtIndex:1] setEnabled:YES];
        } else {
            [[[self.keyboardToolbar items] objectAtIndex:1] setEnabled:NO];
        }
    }
}

@end
