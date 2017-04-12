//
//  Copyright 2010 Mirasense AG
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//

package com.mirasense.scanditsdk.plugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.internal.Code;
import com.scandit.barcodepicker.ocr.RecognizedText;
import com.scandit.barcodepicker.ocr.TextRecognitionListener;
import com.scandit.base.util.JSONParseException;
import com.scandit.recognition.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Activity implementing the full-screen picker support. This activity is launched by the
 * FullScreenPickerController
 *
 */
public class FullScreenPickerActivity
        extends Activity
        implements OnScanListener, BarcodePickerWithSearchBar.SearchBarListener,
                   TextRecognitionListener, PickerStateMachine.Callback {
    
    public static final int CANCEL = 0;
    public static final int SCAN = 1;
    public static final int MANUAL = 2;
    public static final int TEXT = 1;

    private static FullScreenPickerActivity sActiveActivity = null;
    private static AtomicBoolean sPendingClose = new AtomicBoolean(false);
    private static AtomicBoolean sBufferedTorchEnabled = new AtomicBoolean(false);
    private boolean mContinuousMode = false;

    private boolean mLegacyMode = false;
    
    private PickerStateMachine mPickerStateMachine = null;
    private int mStateBeforeSuspend = PickerStateMachine.STOPPED;
    private static ArrayList<Long> rejectedCodeIds;
    private ArrayList<Long> mRejectedCodeIds;

    public static void setState(int state) {
        if (sActiveActivity == null)
            return;
        sActiveActivity.mPickerStateMachine.setState(state);
    }

    public static void setRejectedCodeIds(ArrayList<Long> rejectedCodeIds) {
        if (sActiveActivity == null)
            return;
        sActiveActivity.mRejectedCodeIds = rejectedCodeIds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JSONObject settings = null;
        Bundle options = null;
        Bundle overlayOptions = null;

        if (getIntent().getExtras().containsKey("settings")) {
            try {
                settings = new JSONObject(getIntent().getExtras().getString("settings"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mLegacyMode = false;
        } else {
            mLegacyMode = true;
        }
        if (getIntent().getExtras().containsKey("options")) {
            options = getIntent().getExtras().getBundle("options");
        }
        if (getIntent().getExtras().containsKey("overlayOptions")) {
            overlayOptions = getIntent().getExtras().getBundle("overlayOptions");
        }

        initializeAndStartBarcodeRecognition(settings, options, overlayOptions);
    }
    
    @SuppressWarnings("deprecation")
    private void initializeAndStartBarcodeRecognition(
            JSONObject settings, Bundle options, Bundle overlayOptions) {
        // Switch to full screen.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (options.getBoolean("secure")) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        ScanSettings scanSettings;
        if (mLegacyMode) {
            scanSettings = LegacySettingsParamParser.getSettings(options);
        } else {
            try {
                scanSettings = ScanSettings.createWithJson(settings);
            } catch (JSONParseException e) {
                Log.e("ScanditSDK", "Exception when creating settings");
                e.printStackTrace();
                scanSettings = ScanSettings.create();
            }
        }
        BarcodePickerWithSearchBar picker = new BarcodePickerWithSearchBar(this, scanSettings);
        picker.setOnScanListener(this);
        picker.setTextRecognitionListener(this);

        this.setContentView(picker);
        mPickerStateMachine = new PickerStateMachine(picker, this);

        // Set all the UI options.
        PhonegapParamParser.updatePicker(picker, options, this);

        // Check buffered torch state and apply if needed.
        if (sBufferedTorchEnabled.compareAndSet(true, false)) {
            picker.switchTorchOn(true);
        }

        if (mLegacyMode) {
            assert settings == null;
            LegacyUIParamParser.updatePickerUI(this, picker, options);
        } else {
            UIParamParser.updatePickerUI(picker, overlayOptions);
            PhonegapParamParser.updatePicker(picker, overlayOptions, this);
        }

        mContinuousMode = PhonegapParamParser.shouldRunInContinuousMode(options);


        mStateBeforeSuspend = PhonegapParamParser.shouldStartInPausedState(options)
                ? PickerStateMachine.PAUSED
                : PickerStateMachine.ACTIVE;
    }
    
    @Override
    protected void onPause() {
        sActiveActivity = null;
        // When the activity is in the background immediately stop the scanning to save resources
        // and free the camera. Remember the last state, so we can put the picker into the same
        // state in onResume.
        mStateBeforeSuspend = mPickerStateMachine.getState();
        mPickerStateMachine.setState(PickerStateMachine.STOPPED);
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        sActiveActivity = this;
        if (sPendingClose.compareAndSet(true, false)) {
            // close has been issued before we had the chance to start the picker.
            FullScreenPickerActivity.close();
        }
        // Once the activity is in the foreground again, restore previous picker state.
        mPickerStateMachine.setState(mStateBeforeSuspend);

    }

    public void switchTorchOn(boolean enabled) {
        mPickerStateMachine.getPicker().switchTorchOn(enabled);
    }

    public void didCancel() {
        mPickerStateMachine.setState(PickerStateMachine.STOPPED);
        setResult(CANCEL);
        finish();
        sPendingClose.set(false);
    }

    private Bundle bundleForScanResult(ScanSession session) {
        Bundle bundle = new Bundle();
        if (mLegacyMode) {
            Barcode code = session.getNewlyRecognizedCodes().get(0);
            bundle.putString("barcode", code.getData());
            bundle.putString("symbology", Code.symbologyToString(code.getSymbology(),
                    code.isGs1DataCarrier()));
            return bundle;
        }
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_SCAN_EVENT,
                ResultRelay.jsonForSession(session));
        bundle.putString("jsonString", eventArgs.toString());
        return bundle;
    }

    @Override
    public void didScan(ScanSession session) {
        if (sPendingClose.get()) {
            // return if there is a pending close. Otherwise we might deadlock
            return;
        }
        Bundle bundle = bundleForScanResult(session);
        if (!mContinuousMode) {
            mPickerStateMachine.switchToNextScanState(PickerStateMachine.PAUSED, session);
            final Intent intent = new Intent();
            bundle.putBoolean("waitForResult", false);
            intent.putExtras(bundle);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(SCAN, intent);
                    finish();
                }
            });
            return;
        }
        int nextState = ResultRelay.relayResult(bundle);
        mPickerStateMachine.switchToNextScanState(nextState, session);
        Marshal.rejectCodes(session, mRejectedCodeIds);
    }

    private Bundle manualSearchResultsToBundle(String entry) {
        Bundle bundle = new Bundle();
        if (mLegacyMode) {
            bundle.putString("barcode", entry.trim());
            bundle.putString("symbology", "UNKNOWN");
        } else {
            JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_MANUAL_SEARCH_EVENT, entry);
            bundle.putString("jsonString", args.toString());
        }
        // no need to wait for result
        bundle.putBoolean("waitForResult", false);
        return bundle;
    }
    
    @Override
    public void didEnter(String entry) {
        if (!mContinuousMode) {
            Intent intent = new Intent();
            intent.putExtras(manualSearchResultsToBundle(entry.trim()));
            setResult(MANUAL, intent);
            finish();
            return;
        }

        Bundle bundle = manualSearchResultsToBundle(entry.trim());
        ResultRelay.relayResult(bundle);
    }

    private Bundle bundleForTextRecognitionResult(RecognizedText recognizedText) {
        Bundle bundle = new Bundle();
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_RECOGNIZE_TEXT_EVENT,
                ResultRelay.jsonForRecognizedText(recognizedText));
        bundle.putString("jsonString", eventArgs.toString());
        return bundle;
    }

    @Override
    public int didRecognizeText(RecognizedText recognizedText) {
        if (sPendingClose.get()) {
            // return if there is a pending close. Otherwise we might deadlock
            return TextRecognitionListener.PICKER_STATE_STOPPED;
        }
        Bundle bundle = bundleForTextRecognitionResult(recognizedText);
        if (!mContinuousMode) {
            final Intent intent = new Intent();
            bundle.putBoolean("waitForResult", false);
            intent.putExtras(bundle);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(TEXT, intent);
                    finish();
                }
            });
            mPickerStateMachine.setState(PickerStateMachine.PAUSED);
            return TextRecognitionListener.PICKER_STATE_PAUSED;
        }
        int nextState = ResultRelay.relayResult(bundle);

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
    public void onBackPressed() {
        didCancel();
    }

    
    public static void close() {
        if (sActiveActivity != null) {
            sActiveActivity.didCancel();
        } else {
            sPendingClose.set(true);
        }
    }

    public static void setTorchEnabled(boolean enabled) {
        if (sActiveActivity != null) {
            sActiveActivity.switchTorchOn(enabled);
        } else {
            sBufferedTorchEnabled.set(enabled);
        }
    }

    @Override
    public void pickerEnteredState(BarcodePickerWithSearchBar picker, int newState) {
        // don't produce events in legacy mode. They would be interpreted as scan events.
        if (mLegacyMode) return;
        
        Bundle resultBundle = new Bundle();
        JSONArray didStopArgs = Marshal.createEventArgs(ScanditSDK.DID_CHANGE_STATE_EVENT, newState);
        resultBundle.putString("jsonString", didStopArgs.toString());
        resultBundle.putBoolean("waitForResult", false);
        ResultRelay.relayResult(resultBundle);
    }

    public static void applyScanSettings(ScanSettings scanSettings) {
        if (sActiveActivity == null || sActiveActivity.mPickerStateMachine == null) return;

        sActiveActivity.mPickerStateMachine.applyScanSettings(scanSettings);
    }

    public static void updateUI(Bundle overlayOptions) {
        if (sActiveActivity == null || sActiveActivity.mPickerStateMachine == null) return;
        UIParamParser.updatePickerUI(sActiveActivity.mPickerStateMachine.getPicker(), overlayOptions);
    }

    public static void startScanning() {
        if (sActiveActivity == null || sActiveActivity.mPickerStateMachine == null) return;
        sActiveActivity.mPickerStateMachine.startScanning();
    }
}
