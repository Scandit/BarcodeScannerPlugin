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

import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.internal.Code;
import com.scandit.barcodepicker.ocr.RecognizedText;
import com.scandit.barcodepicker.ocr.TextRecognitionListener;
import com.scandit.base.util.JSONParseException;
import com.scandit.recognition.Barcode;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controls a subview picker, e.g. a picker shown in the actual plugin activity, displayed on
 * top of the webview.
 */
public class SubViewPickerController
        extends PickerControllerBase
        implements BarcodePickerWithSearchBar.SearchBarListener, OnScanListener,
                   PickerStateMachine.Callback, TextRecognitionListener {

    private RelativeLayout mLayout;

    private PickerStateMachine mPickerStateMachine = null;

    private SubViewPickerOrientationHandler mOrientationHandler = null;
    private boolean mCloseWhenDidScanCallbackFinishes = false;
    private AtomicBoolean mPendingClose = new AtomicBoolean(false);
    // Can't use Size, because the class is not available in all the releases we support.
    // chosen such that dim.x <= dim.y
    private Point mScreenDimensions = null;
    private ArrayList<Long> mRejectedCodeIds;

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
                     boolean legacyMode) {
        mPendingClose.set(false);
        mLegacyMode = legacyMode;
        mContinuousMode = PhonegapParamParser.shouldRunInContinuousMode(options);
        mOrientationHandler = new SubViewPickerOrientationHandler(Looper.getMainLooper(), mPlugin,
                                                                  null);
        mCloseWhenDidScanCallbackFinishes = false;
        mOrientationHandler.start(true);
        final Activity pluginActivity = mPlugin.cordova.getActivity();
        DisplayMetrics display =  pluginActivity.getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (display.widthPixels * 160.f / display.densityDpi);
        int height = (int) (display.heightPixels * 160.f / display.densityDpi);
        mScreenDimensions = new Point(Math.min(width, height), Math.max(width, height));


        // initialization must be performed on main thread.
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScanSettings scanSettings;
                if (settings == null) {
                    scanSettings = LegacySettingsParamParser.getSettings(options);
                } else {
                    try {
                        scanSettings = ScanSettings.createWithJson(settings);
                    } catch (JSONParseException e) {
                        Log.e("ScanditSDK", "Exception while creating settings");
                        e.printStackTrace();
                        sendRuntimeError("Exception while creating settings: " + e.getMessage() +
                                         ". Falling back to default scan settings.");
                        scanSettings = ScanSettings.create();
                    }
                }
                BarcodePickerWithSearchBar picker = new BarcodePickerWithSearchBar(pluginActivity,
                                                                                   scanSettings);
                picker.setOnScanListener(SubViewPickerController.this);
                picker.setTextRecognitionListener(SubViewPickerController.this);
                mPickerStateMachine = new PickerStateMachine(picker, SubViewPickerController.this);
                mOrientationHandler.setScreenDimensions(mScreenDimensions);
                mOrientationHandler.setPicker(mPickerStateMachine.getPicker());
                // Set all the UI options.
                PhonegapParamParser.updatePicker(picker, options, SubViewPickerController.this);
                internalUpdateUI(overlayOptions, options);
                // Create the layout to add the picker to and add it on top of the web view.
                mLayout = new RelativeLayout(pluginActivity);
                ViewGroup viewGroup = getPickerParent();
                if (viewGroup == null)
                    return; // couldn't determine view group, nothing to be done.
                viewGroup.addView(mLayout);
                RelativeLayout.LayoutParams rLayoutParams =
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT);
                mLayout.addView(mPickerStateMachine.getPicker(), rLayoutParams);
                PhonegapParamParser.updateLayout(pluginActivity, mPickerStateMachine.getPicker(),
                        options, mScreenDimensions);


                if (mPendingClose.compareAndSet(true, false)) {
                    // picker was closed(canceled) in the meantime. close it now.
                    SubViewPickerController.this.close();
                }
                if (!mLegacyMode) return;

                // In legacy mode, start scanning when show is called.
                int state = PhonegapParamParser.shouldStartInPausedState(options)
                                ? PickerStateMachine.PAUSED
                                : PickerStateMachine.ACTIVE;
                mPickerStateMachine.setState(state);

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
    protected void setRejectedCodeIds(ArrayList<Long> rejectedCodeIds) {
        mRejectedCodeIds = rejectedCodeIds;
    }

    @Override
    public void startScanning() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPickerStateMachine == null) {
                    return;
                }
                mPickerStateMachine.startScanning();
            }
        });
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
            }
        });
    }

    private void runOnUiThread(Runnable r) {
        mPlugin.cordova.getActivity().runOnUiThread(r);
    }

    private void internalUpdateUI(Bundle overlayOptions, Bundle options) {
        BarcodePickerWithSearchBar picker = mPickerStateMachine.getPicker();
        if (mLegacyMode) {
            if (options == null) return;
            LegacyUIParamParser.updatePickerUI(mPlugin.cordova.getActivity(), picker, options);
        } else {
            UIParamParser.updatePickerUI(picker, overlayOptions);
            PhonegapParamParser.updatePicker(picker, overlayOptions, this);
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
        PluginResult result;
        if (mLegacyMode) {
            JSONArray args = new JSONArray();
            args.put(entry);
            args.put("UNKNOWN");
            result = Marshal.createOkResult(args);
        } else {
            JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_MANUAL_SEARCH_EVENT, entry);
            result = Marshal.createOkResult(args);
        }
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
        if (mInFlightDidScanCallbackId.get() != 0) {
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
            mPickerStateMachine.setState(PickerStateMachine.STOPPED);
        }
    }

    @Override
    public void onActivityResume() {
        if (mOrientationHandler != null) {
            mOrientationHandler.start(false);
        }
        if (mPickerStateMachine != null) {
            mPickerStateMachine.setState(PickerStateMachine.ACTIVE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // this should never be called
    }

    private void internalRemoveSubviewPicker() {
        if (Looper.myLooper() == null || Looper.myLooper().getThread() != Thread.currentThread()) {
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
            return (ViewGroup)webView;
        } else {
            try {
                java.lang.reflect.Method getViewMethod = webView.getClass().getMethod("getView");
                Object viewObject = getViewMethod.invoke(webView);
                if (viewObject instanceof View) {
                    View view = (View)viewObject;
                    ViewParent parentView = view.getParent();
                    if (parentView instanceof ViewGroup) {
                        return (ViewGroup) parentView;
                    }
                }
            } catch (Exception e) {
                String message = "Unable to fetch the ViewGroup through webView.getView().getParent().";
                Log.e("ScanditSDK", message);
                e.printStackTrace();
                sendRuntimeError(message);
            }
        }
        return null;
    }

    @Override
    public void didScan(ScanSession session) {
        // don't do anything if there is a pending close operation. otherwise we will deadlock
        if (mPendingClose.get()) {
            return;
        }
        PluginResult result;
        if (mLegacyMode) {
            JSONArray args = new JSONArray();
            Barcode code = session.getNewlyRecognizedCodes().get(0);
            args.put(code.getData());
            args.put(Code.symbologyToString(code.getSymbology(), code.isGs1DataCarrier()));
            args.put(code.getSymbologyName());
            result = Marshal.createOkResult(args);
        } else {
            JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_SCAN_EVENT,
                    ResultRelay.jsonForSession(session));
            result = Marshal.createOkResult(eventArgs);
        }

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
    public int didRecognizeText(RecognizedText recognizedText) {
        if (mPendingClose.get()) {
            // return if there is a pending close. Otherwise we might deadlock
            return TextRecognitionListener.PICKER_STATE_STOPPED;
        }
        PluginResult result;
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_RECOGNIZE_TEXT_EVENT,
                ResultRelay.jsonForRecognizedText(recognizedText));
        result = Marshal.createOkResult(eventArgs);

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
        // don't produce events in legacy mode. They would be interpreted as scan events.
        if (mLegacyMode) return;

        JSONArray didChangeStateArgs = Marshal.createEventArgs(ScanditSDK.DID_CHANGE_STATE_EVENT, newState);
        mCallbackContext.sendPluginResult(Marshal.createOkResult(didChangeStateArgs));
    }
}
