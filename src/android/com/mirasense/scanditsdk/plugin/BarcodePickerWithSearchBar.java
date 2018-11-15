//  Copyright 2016 Scandit AG
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License. You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software distributed under the
//  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
//  express or implied. See the License for the specific language governing permissions and
//  limitations under the License.
package com.mirasense.scanditsdk.plugin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.base.system.SbSystemUtils;

/**
 * A small wrapper around the barcode picker to add support for a search bar into which barcode
 * data can be entered manually.
 */
public class BarcodePickerWithSearchBar extends BaseBarcodePicker {

    private SearchBar mSearchBar;
    private SearchBarListener mListener;

    public static Constraints portraitConstraints = new Constraints();
    public static Constraints landscapeConstraints = new Constraints();


    public BarcodePickerWithSearchBar(Context context) {
        super(context);
    }

    public BarcodePickerWithSearchBar(Context context, ScanSettings settings) {
        super(context, settings);
    }

    public void adjustSize(Activity activity, Constraints newPortraitConstraints,
                           Constraints newLandscapeConstraints, double animationDuration) {
        final RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Display display = activity.getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        
        final Constraints oldConstraints;
        final Constraints newConstraints;
        if (screenHeight > screenWidth) {
            oldConstraints = portraitConstraints;
            newConstraints = newPortraitConstraints;
        } else {
            oldConstraints = landscapeConstraints;
            newConstraints = newLandscapeConstraints;
        }

        if (animationDuration == 0) {
            updateLayoutParams(rLayoutParams, oldConstraints, newConstraints, 1);
        } else {
            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    updateLayoutParams(rLayoutParams, oldConstraints, newConstraints, interpolatedTime);
                }
            };
            anim.setDuration((int) (animationDuration * 1000));
            startAnimation(anim);
        }

        portraitConstraints = newPortraitConstraints;
        landscapeConstraints = newLandscapeConstraints;
    }

    private void updateLayoutParams(RelativeLayout.LayoutParams rLayoutParams,
                                    Constraints oldConstraints, Constraints newConstraints,
                                    float interpolatedTime) {
        if (newConstraints.getLeftMargin() == null) {
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        } else {
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        }
        if (newConstraints.getTopMargin() == null) {
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else {
            rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        }

        if (newConstraints.getLeftMargin() != null) {
            rLayoutParams.leftMargin = calculateInterpolatedValue(
                    oldConstraints.getLeftMargin(), newConstraints.getLeftMargin(), interpolatedTime);
        }
        if (newConstraints.getTopMargin() != null) {
            rLayoutParams.topMargin = calculateInterpolatedValue(
                    oldConstraints.getTopMargin(), newConstraints.getTopMargin(), interpolatedTime);
        }
        if (newConstraints.getRightMargin() != null) {
            rLayoutParams.rightMargin = calculateInterpolatedValue(
                    oldConstraints.getRightMargin(), newConstraints.getRightMargin(), interpolatedTime);
        }
        if (newConstraints.getBottomMargin() != null) {
            rLayoutParams.bottomMargin = calculateInterpolatedValue(
                    oldConstraints.getBottomMargin(), newConstraints.getBottomMargin(), interpolatedTime);
        }
        if (newConstraints.getWidth() != null) {
            rLayoutParams.width = calculateInterpolatedValue(
                    oldConstraints.getWidth(), newConstraints.getWidth(), interpolatedTime);
        }
        if (newConstraints.getHeight() != null) {
            rLayoutParams.height = calculateInterpolatedValue(
                    oldConstraints.getHeight(), newConstraints.getHeight(), interpolatedTime);
        }
        setLayoutParams(rLayoutParams);
    }

    public void setOnSearchBarListener(SearchBarListener listener) {
        mListener = listener;
    }

    public void showSearchBar(boolean show) {
        if (show && mSearchBar == null) {
            mSearchBar = new SearchBar(getContext(), new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSearchClicked();
                }
            });
            RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            rParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            addView(mSearchBar, rParams);

            getOverlayView().setTorchButtonMarginsAndSize(15, 55, 40, 40);
            getOverlayView().setCameraSwitchButtonMarginsAndSize(15, 55, 40, 40);

            requestChildFocus(null, null);
        } else if (!show && mSearchBar != null) {
            removeView(mSearchBar);
            mSearchBar = null;
            invalidate();
        }
    }

    public void setSearchBarInputType(int inputType) {
        if (mSearchBar != null) {
            mSearchBar.setInputType(inputType);
        }
    }

    protected void setSearchBarPlaceholderText(String text) {
        mSearchBar.setHint(text);
    }

    private void onSearchClicked() {
        mListener.didEnter(mSearchBar.getText());
    }

    private int calculateInterpolatedValue(Integer oldValue, Integer newValue, float interpolatedTime) {
        if (oldValue != null) {
            return SbSystemUtils.pxFromDp(getContext(),
                    (int) (oldValue + (newValue - oldValue) * interpolatedTime));
        } else {
            return SbSystemUtils.pxFromDp(getContext(), newValue);
        }
    }


    public interface SearchBarListener {
        /**
         *  Called whenever a string was entered in the search bar and the button to search was pressed.
         *
         *  @param entry the text that has been entered by the user.
         */
        void didEnter(String entry);
    }


    /**
     * Constraints for the barcode picker consisting of margins, width and height. The getters
     * do not necessarily return the properties previously set but may return 0 for a margin if too
     * few margins were set or return null for width/height if too many margins were set.
     */
    public static class Constraints {
        private Integer mLeftMargin = null;
        private Integer mTopMargin = null;
        private Integer mRightMargin = null;
        private Integer mBottomMargin = null;
        private Integer mWidth = null;
        private Integer mHeight = null;

        public Constraints() {}

        public Constraints(Rect margins) {
            if (margins != null) {
                mLeftMargin = margins.left;
                mTopMargin = margins.top;
                mRightMargin = margins.right;
                mBottomMargin = margins.bottom;
            }
        }

        public Integer getLeftMargin() {
            if (mLeftMargin == null && (mRightMargin == null || mWidth == null)) return 0;
            return mLeftMargin;
        }
        public void setLeftMargin(Integer value) { mLeftMargin = value; }

        public Integer getTopMargin() {
            if (mTopMargin == null && (mBottomMargin == null || mHeight == null)) return 0;
            return mTopMargin;
        }
        public void setTopMargin(Integer value) { mTopMargin = value; }

        public Integer getRightMargin() {
            return mRightMargin;
        }
        public void setRightMargin(Integer value) { mRightMargin = value; }

        public Integer getBottomMargin() {
            return mBottomMargin;
        }
        public void setBottomMargin(Integer value) { mBottomMargin = value; }

        public Integer getWidth() {
            if (mLeftMargin != null && mRightMargin != null) return null;
            return mWidth;
        }
        public void setWidth(Integer value) { mWidth = value; }

        public Integer getHeight() {
            if (mTopMargin != null && mBottomMargin != null) return null;
            return mHeight;
        }
        public void setHeight(Integer value) { mHeight = value; }
    }
}
