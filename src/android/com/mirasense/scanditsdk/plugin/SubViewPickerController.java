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
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.mirasense.scanditsdk.plugin.BarcodePickerWithSearchBar.Constraints;

import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ProcessFrameListener;
import com.scandit.barcodepicker.LicenseValidationListener;
import com.scandit.barcodepicker.PropertyChangeListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ocr.RecognizedText;
import com.scandit.barcodepicker.ocr.TextRecognitionListener;
import com.scandit.base.util.JSONParseException;
import com.scandit.recognition.TrackedBarcode;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controls a subview picker, e.g. a picker shown in the actual plugin activity, displayed on
 * top of the webview.
 */
public class SubViewPickerController extends PickerControllerBase implements
        BarcodePickerWithSearchBar.SearchBarListener, OnScanListener, ProcessFrameListener,
        PickerStateMachine.Callback, TextRecognitionListener, PropertyChangeListener,
        LicenseValidationListener {

    private RelativeLayout mLayout;

    private PickerStateMachine mPickerStateMachine = null;
    private int mStateBeforeSuspend = PickerStateMachine.ACTIVE;

    private SubViewPickerOrientationHandler mOrientationHandler = null;
    private boolean mContinuousMode = false;
    private boolean mIsDidScanDefined = false;
    private boolean mShouldPassBarcodeFrame = false;
    private boolean mCloseWhenDidScanCallbackFinishes = false;
    private AtomicBoolean mPendingClose = new AtomicBoolean(false);
    // Can't use Size, because the class is not available in all the releases we support.
    // chosen such that dim.x <= dim.y
    private Point mScreenDimensions = null;
    private List<Long> mRejectedCodeIds;

    private Set<Long> mLastFrameTrackedCodeIds = new HashSet<Long>();
    private List<Long> mRejectedTrackedCodeIds;
    private WeakReference<ResizeScannerInterface> resizeListener = new WeakReference<ResizeScannerInterface>(null);

    SubViewPickerController(CordovaPlugin plugin, CallbackContext callbacks) {
        super(plugin, callbacks);
    }

    @Override
    public void setState(final int state) {
        super.setState(state);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPickerStateMachine == null) {
                    return;
                }
                mPickerStateMachine.setState(state);
            }
        });
    }

    @Override
    public void show(final JSONObject settings, final Bundle options, final Bundle overlayOptions,
                     boolean legacyMode, CallbackContext callbacks) {
        mCallbackContext = callbacks;
        mPendingClose.set(false);
        mContinuousMode = PhonegapParamParser.shouldRunInContinuousMode(options);
        mIsDidScanDefined = PhonegapParamParser.isDidScanDefined(options);
        mShouldPassBarcodeFrame = PhonegapParamParser.shouldPassBarcodeFrame(options);
        mOrientationHandler = new SubViewPickerOrientationHandler(Looper.getMainLooper(), mPlugin, null);
        mCloseWhenDidScanCallbackFinishes = false;
        mOrientationHandler.start(true);
        final Activity pluginActivity = mPlugin.cordova.getActivity();
        DisplayMetrics display = pluginActivity.getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (display.widthPixels * 160.f / display.densityDpi);
        int height = (int) (display.heightPixels * 160.f / display.densityDpi);
        mScreenDimensions = new Point(Math.min(width, height), Math.max(width, height));

        // initialization must be performed on main thread.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScanSettings scanSettings;
                try {
                    scanSettings = ScanSettings.createWithJson(settings);
                } catch (JSONParseException e) {
                    Log.e("ScanditSDK", "Exception while creating settings");
                    e.printStackTrace();
                    sendRuntimeError("Exception while creating settings: " + e.getMessage() +
                            ". Falling back to default scan settings.");
                    scanSettings = ScanSettings.create();
                }
                BarcodePickerWithSearchBar picker =
                        new BarcodePickerWithSearchBar(pluginActivity, scanSettings);
                picker.setOnScanListener(SubViewPickerController.this);
                picker.setProcessFrameListener(SubViewPickerController.this);
                picker.setLicenseValidationListener(SubViewPickerController.this);
                picker.setTextRecognitionListener(SubViewPickerController.this);
                picker.setPropertyChangeListener(SubViewPickerController.this);
                mPickerStateMachine = new PickerStateMachine(
                        picker, scanSettings, SubViewPickerController.this);
                mOrientationHandler.setScreenDimensions(mScreenDimensions);
                mOrientationHandler.setPicker(mPickerStateMachine.getPicker());
                // Set all the UI options.
                PhonegapParamParser.updatePicker(picker, options, SubViewPickerController.this);
                internalUpdateUI(overlayOptions, options);
                // Create the layout to add the picker to and add it on top of the web view.
                mLayout = new RelativeLayout(pluginActivity);
                ViewGroup viewGroup = getPickerParent();
                if (viewGroup == null) return; // couldn't determine view group, nothing to be done.
                viewGroup.addView(mLayout);
                RelativeLayout.LayoutParams rLayoutParams =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT);
                mLayout.addView(mPickerStateMachine.getPicker(), rLayoutParams);
                PhonegapParamParser.updateLayout(pluginActivity, mPickerStateMachine.getPicker(),
                        options, mScreenDimensions);
                callPickerShownListener(options);

                if (mPendingClose.compareAndSet(true, false)) {
                    // picker was closed(canceled) in the meantime. close it now.
                    SubViewPickerController.this.close();
                }
            }
        });
    }

    @Override
    public void finishDidScanCallback(JSONArray data) {
        super.finishDidScanCallback(data);
        // deal with calls to cancel in the didScan callback.
        if (mCloseWhenDidScanCallbackFinishes) {
            mCloseWhenDidScanCallbackFinishes = false;
            this.close();
        }
    }

    @Override
    protected void setRejectedCodeIds(List<Long> rejectedCodeIds) {
        mRejectedCodeIds = rejectedCodeIds;
    }

    @Override
    protected void setRejectedTrackedCodeIds(List<Long> rejectedCodeIds) {
        mRejectedTrackedCodeIds = rejectedCodeIds;
    }

    @Override
    protected void setTrackedCodeStates(Map<Long, JSONObject> trackedCodeStates) {
        PickerStateMachine stateMachine = mPickerStateMachine;
        if (stateMachine != null) {
            stateMachine.setTrackedCodeStates(trackedCodeStates);
        }
    }

    @Override
    public void startScanning(final boolean paused) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPickerStateMachine == null) {
                    return;
                }
                mPickerStateMachine.startScanning(paused);
                setStateOnParent(PickerStateMachine.ACTIVE);
            }
        });
    }

    private void setStateOnParent(int state) {
        super.setState(state);
    }

    @Override
    public void applyScanSettings(final ScanSettings scanSettings) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPickerStateMachine == null) return;
                mPickerStateMachine.applyScanSettings(scanSettings);
            }
        });
    }

    @Override
    public void updateUI(final Bundle overlayOptions) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                internalUpdateUI(overlayOptions, null);
            }
        });
    }

    @Override
    public void updateLayout(final Bundle layoutOptions) {
        final Activity pluginActivity = mPlugin.cordova.getActivity();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PhonegapParamParser.updateLayout(pluginActivity, mPickerStateMachine.getPicker(),
                        layoutOptions, mScreenDimensions);
                callPickerResizedListener(layoutOptions);
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        mPlugin.cordova.getActivity().runOnUiThread(r);
    }

    private void internalUpdateUI(Bundle overlayOptions, Bundle options) {
        BarcodePickerWithSearchBar picker = mPickerStateMachine.getPicker();
        UIParamParser.updatePickerUI(picker, overlayOptions);
        PhonegapParamParser.updatePicker(picker, overlayOptions, this);
    }
    
    private void callPickerResizedListener(Bundle bundle) {
        ResizeScannerInterface listener = resizeListener.get();
        if (listener != null) {
            Constraints portrait = PhonegapParamParser.extractConstraints(bundle,
                    PhonegapParamParser.paramPortraitConstraints, mScreenDimensions.x, mScreenDimensions.y);
            Constraints landscape = PhonegapParamParser.extractConstraints(bundle,
                    PhonegapParamParser.paramLandscapeConstraints, mScreenDimensions.y, mScreenDimensions.x);
            listener.scannerResized(portrait, landscape, bundle.getInt(PhonegapParamParser.paramAnimationDuration));
        }
    }

    private void callPickerShownListener(Bundle bundle) {
        ResizeScannerInterface listener = resizeListener.get();
        if (listener != null) {
            Constraints portrait = PhonegapParamParser.extractConstraints(bundle,
                    PhonegapParamParser.paramPortraitConstraints, mScreenDimensions.x, mScreenDimensions.y);
            Constraints landscape = PhonegapParamParser.extractConstraints(bundle,
                    PhonegapParamParser.paramLandscapeConstraints, mScreenDimensions.y, mScreenDimensions.x);
            listener.scannerShown(portrait, landscape);
        }
    }

    @Override
    public void setTorchEnabled(final boolean enabled) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPickerStateMachine == null) return;
                mPickerStateMachine.getPicker().switchTorchOn(enabled);
            }
        });
    }

    @Override
    public void didEnter(String entry) {
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_MANUAL_SEARCH_EVENT, entry);
        PluginResult result = Marshal.createOkResult(args);
        mCallbackContext.sendPluginResult(result);
        if (!mContinuousMode) {
            this.close();
        }
    }

    @Override
    public void close() {
        if (mPickerStateMachine == null) {
            // we don't have a picker yet. must be closed when it is created.
            mPendingClose.set(true);
            return;
        }

        mPickerStateMachine.getPicker().setOnScanListener(null);
        mPickerStateMachine.getPicker().setProcessFrameListener(null);
        mPickerStateMachine.getPicker().setLicenseValidationListener(null);
        mPickerStateMachine.getPicker().setTextRecognitionListener(null);
        mPickerStateMachine.getPicker().setPropertyChangeListener(null);

        if (isResultCallbackInFlight()) {
            // we get here if the didScan callback is still in progress. We need to delay
            // processing the cancel call to avoid a dead-lock. The picker will be closed
            // (removed) when finishDidScanCallback is called.
            mCloseWhenDidScanCallbackFinishes = true;
            return;
        }
        // say that there is a pending close.
        mPendingClose.set(true);
        mCloseWhenDidScanCallbackFinishes = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOrientationHandler.stop();
                internalRemoveSubviewPicker();
                mCallbackContext.sendPluginResult(Marshal.createCancel());
                mPendingClose.set(false);
            }
        });
    }

    @Override
    public void onActivityPause() {
        if (mOrientationHandler != null) {
            mOrientationHandler.stop();
        }
        if (mPickerStateMachine != null) {
            mStateBeforeSuspend = mPickerStateMachine.getState();
            mPickerStateMachine.setState(PickerStateMachine.STOPPED);
        }
    }

    @Override
    public void onActivityResume() {
        if (mOrientationHandler != null) {
            mOrientationHandler.start(false);
        }
        if (mPickerStateMachine != null) {
            mPickerStateMachine.setState(mStateBeforeSuspend);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // this should never be called
    }

    private void internalRemoveSubviewPicker() {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("must be called on main thread");
        }
        if (mPickerStateMachine == null) {
            return;
        }
        mPickerStateMachine.setState(PickerStateMachine.STOPPED);
        ViewGroup viewGroup = getPickerParent();
        if (viewGroup != null) {
            viewGroup.removeView(mLayout);
        }
        mLayout = null;
        mPickerStateMachine = null;
        ResizeScannerInterface listener = resizeListener.get();
        if (listener != null) {
            listener.scannerDismissed();
        }
    }

    private void removeSubviewPicker() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                internalRemoveSubviewPicker();
            }
        });
    }

    private void sendRuntimeError(String error) {
        mCallbackContext.sendPluginResult(Marshal.createFailResult(error));
    }

    private ViewGroup getPickerParent() {
        CordovaWebView webView = mPlugin.webView;
        if (webView instanceof WebView) {
            return (ViewGroup) webView;
        } else {
            try {
                java.lang.reflect.Method getViewMethod = webView.getClass().getMethod("getView");
                Object viewObject = getViewMethod.invoke(webView);
                if (viewObject instanceof View) {
                    ViewParent parentView = ((View) viewObject).getParent();
                    ViewGroup viewGroup = null;

                    // Continue going up if there is a parent, the parent is a view group and the
                    // current view group is not our custom view that can be used to force the
                    // picker to be added at a specific point instead of going all the way up
                    // to the root.
                    while (parentView != null && parentView instanceof ViewGroup
                            && !(viewGroup instanceof ScanditCordovaTopView)) {
                        viewGroup = (ViewGroup) parentView;
                        parentView = viewGroup.getParent();
                    }
                    return viewGroup;
                }
            } catch (Exception e) {
                String message = "Unable to fetch the ViewGroup through webView.getView().getParent()";
                Log.e("ScanditSDK", message);
                e.printStackTrace();
                sendRuntimeError(message);
            }
        }
        return null;
    }

    @Override
    public void didScan(ScanSession session) {
        // don't do anything if:
        // there is a pending close operation (otherwise we will deadlock)
        // or the didScan callback is undefined
        if (mPendingClose.get() || !mIsDidScanDefined) {
            return;
        }
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_SCAN_EVENT,
                ResultRelay.jsonForSession(session));
        PluginResult result = Marshal.createOkResult(eventArgs);

        int nextState = sendPluginResultBlocking(result);
        if (!mContinuousMode) {
            nextState = PickerStateMachine.PAUSED;
        }
        mPickerStateMachine.switchToNextScanState(nextState, session);
        Marshal.rejectCodes(session, mRejectedCodeIds);
        if (!mContinuousMode) {
            removeSubviewPicker();
        }
    }

    @Override
    public void didProcess(byte[] bytes, int width, int height, ScanSession session) {
        // don't do anything if there is a pending close operation. otherwise we will deadlock
        if (mPendingClose.get()) {
            return;
        }

        if ((mPickerStateMachine != null && !mPickerStateMachine.isMatrixScanEnabled())) {
            // Call didProcessFrame only when new codes have been recognized.
            if (session.getNewlyRecognizedCodes().size() > 0) {
                returnFrameBufferIfWanted(bytes, width, height);
            }
            return;
        }

        // If tracked codes are null for whatever reason, there is nothing more to do.
        if (session.getTrackedCodes() == null) {
            return;
        }

        Map<Long, TrackedBarcode> trackedCodes = session.getTrackedCodes();
        List<TrackedBarcode> newlyTrackedCodes = new ArrayList<TrackedBarcode>();
        Set<Long> recognizedCodeIds = new HashSet<Long>();

        for (Map.Entry<Long, TrackedBarcode> entry : trackedCodes.entrySet()) {
            // Check if it's a new identifier.
            if (entry.getValue().isRecognized()) {
                recognizedCodeIds.add(entry.getKey());
                if (!mLastFrameTrackedCodeIds.contains(entry.getKey())) {
                    // Add the new identifier.
                    mLastFrameTrackedCodeIds.add(entry.getKey());
                    newlyTrackedCodes.add(entry.getValue());
                }
            }
        }
        // Update the recognized code ids for next frame.
        mLastFrameTrackedCodeIds = recognizedCodeIds;

        if (newlyTrackedCodes.size() > 0) {
            JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_RECOGNIZE_NEW_CODES,
                    ResultRelay.jsonForTrackedCodes(newlyTrackedCodes));
            PluginResult result = Marshal.createOkResult(eventArgs);
            sendPluginResultBlocking(result);
            Marshal.rejectTrackedCodes(session, mRejectedTrackedCodeIds);

            // Call didProcessFrame only when new codes have started to be tracked.
            returnFrameBufferIfWanted(bytes, width, height);
        }
    }

    private void returnFrameBufferIfWanted(byte[] bytes, int width, int height) {
        if (mShouldPassBarcodeFrame) {
            String base64Data = SampleBufferConverter.base64StringFromFrame(bytes, width, height);

            JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_PROCESS_FRAME,
                    ResultRelay.jsonForDidProcessFrame(base64Data));
            PluginResult result = Marshal.createOkResult(args);
            mCallbackContext.sendPluginResult(result);
        }
    }

    @Override
    public int didRecognizeText(RecognizedText recognizedText) {
        if (mPendingClose.get()) {
            // return if there is a pending close. Otherwise we might deadlock
            return TextRecognitionListener.PICKER_STATE_STOPPED;
        }
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_RECOGNIZE_TEXT_EVENT,
                ResultRelay.jsonForRecognizedText(recognizedText));
        PluginResult result = Marshal.createOkResult(eventArgs);

        int nextState = sendPluginResultBlocking(result);
        if (!mContinuousMode) {
            nextState = PickerStateMachine.PAUSED;
        }
        if (!mContinuousMode) {
            removeSubviewPicker();
        }

        mPickerStateMachine.setState(nextState);
        Marshal.rejectRecognizedTexts(recognizedText, mRejectedCodeIds);
        if (nextState == PickerStateMachine.STOPPED) {
            return TextRecognitionListener.PICKER_STATE_STOPPED;
        } else if (nextState == PickerStateMachine.PAUSED) {
            return TextRecognitionListener.PICKER_STATE_PAUSED;
        } else {
            return TextRecognitionListener.PICKER_STATE_ACTIVE;
        }
    }

    @Override
    public void pickerEnteredState(BarcodePickerWithSearchBar picker, int newState) {
        JSONArray didChangeStateArgs = Marshal.createEventArgs(ScanditSDK.DID_CHANGE_STATE_EVENT, newState);
        mCallbackContext.sendPluginResult(Marshal.createOkResult(didChangeStateArgs));
    }

    @Override
    public void pickerSwitchedMatrixScanState(BarcodePickerWithSearchBar picker, boolean matrixScan) {
        mLastFrameTrackedCodeIds.clear();
    }

    @Override
    public void onPropertyChange(int name, int newState) {
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_CHANGE_PROPERTY,
                ResultRelay.jsonForPropertyChange(name, newState));
        PluginResult result = Marshal.createOkResult(args);
        mCallbackContext.sendPluginResult(result);
    }

    @Override
    public void failedToValidateLicense(String errorMessage) {
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_FAIL_TO_VALIDATE_LICENSE,
                ResultRelay.jsonForLicenseValidationFail(errorMessage));
        PluginResult result = Marshal.createOkResult(args);
        mCallbackContext.sendPluginResult(result);
    }
    
    public void setResizeListener(ResizeScannerInterface listener) {
        resizeListener = new WeakReference<ResizeScannerInterface>(listener);
    }
}
