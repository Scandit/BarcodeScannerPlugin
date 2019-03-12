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
import com.scandit.barcodepicker.ProcessFrameListener;
import com.scandit.barcodepicker.LicenseValidationListener;
import com.scandit.barcodepicker.PropertyChangeListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ocr.RecognizedText;
import com.scandit.barcodepicker.ocr.TextRecognitionListener;
import com.scandit.base.util.JSONParseException;
import com.scandit.recognition.TrackedBarcode;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Activity implementing the full-screen picker support. This activity is launched by the
 * FullScreenPickerController
 */
public class FullScreenPickerActivity extends Activity implements OnScanListener,
        BarcodePickerWithSearchBar.SearchBarListener, ProcessFrameListener, TextRecognitionListener,
        PickerStateMachine.Callback, PropertyChangeListener, LicenseValidationListener {

    public static final int CANCEL = 0;
    public static final int SCAN = 1;
    public static final int MANUAL = 2;
    public static final int TEXT = 3;

    private static FullScreenPickerActivity sActiveActivity = null;
    private static AtomicBoolean sPendingClose = new AtomicBoolean(false);
    private static AtomicBoolean sBufferedTorchEnabled = new AtomicBoolean(false);

    private PickerStateMachine mPickerStateMachine = null;
    private boolean mContinuousMode = false;
    private boolean mIsDidScanDefined = false;
    private boolean mShouldPassBarcodeFrame = false;

    private int mStateBeforeSuspend = PickerStateMachine.STOPPED;
    private List<Long> mRejectedCodeIds;

    private Set<Long> mLastFrameTrackedCodeIds = new HashSet<Long>();
    private List<Long> mRejectedTrackedCodeIds;


    public static void setState(int state) {
        if (sActiveActivity == null) return;
        sActiveActivity.mPickerStateMachine.setState(state);
    }

    public static void applyScanSettings(ScanSettings scanSettings) {
        if (sActiveActivity == null || sActiveActivity.mPickerStateMachine == null) return;
        sActiveActivity.mPickerStateMachine.applyScanSettings(scanSettings);
    }

    public static void startScanning(boolean paused) {
        if (sActiveActivity == null || sActiveActivity.mPickerStateMachine == null) return;
        sActiveActivity.mPickerStateMachine.startScanning(paused);
    }

    public static void setRejectedCodeIds(List<Long> rejectedCodeIds) {
        if (sActiveActivity == null) return;
        sActiveActivity.mRejectedCodeIds = rejectedCodeIds;
    }

    public static void setRejectedTrackedCodeIds(List<Long> rejectedCodeIds) {
        if (sActiveActivity == null) return;
        sActiveActivity.mRejectedTrackedCodeIds = rejectedCodeIds;
    }

    public static void setTrackedCodeStates(Map<Long, JSONObject> trackedCodeStates) {
        if (sActiveActivity == null) return;
        sActiveActivity.mPickerStateMachine.setTrackedCodeStates(trackedCodeStates);
    }

    public static void updateUI(Bundle overlayOptions) {
        if (sActiveActivity == null || sActiveActivity.mPickerStateMachine == null) return;
        UIParamParser.updatePickerUI(sActiveActivity.mPickerStateMachine.getPicker(), overlayOptions);
    }

    public static void setTorchEnabled(boolean enabled) {
        if (sActiveActivity != null) {
            sActiveActivity.switchTorchOn(enabled);
        } else {
            sBufferedTorchEnabled.set(enabled);
        }
    }

    public static void close() {
        sPendingClose.set(true);
        if (sActiveActivity != null) {
            sActiveActivity.didCancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JSONObject settings = new JSONObject();
        Bundle options = new Bundle();
        Bundle overlayOptions = new Bundle();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String settingsString = extras.getString("settings");
            try {
                if (settingsString != null) {
                    settings = new JSONObject(settingsString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Bundle optionsBundle = extras.getBundle("options");
            if (optionsBundle != null) {
                options = optionsBundle;
            }

            Bundle overlayOptionsBundle = extras.getBundle("overlayOptions");
            if (overlayOptionsBundle != null) {
                overlayOptions = overlayOptionsBundle;
            }
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
        try {
            scanSettings = ScanSettings.createWithJson(settings);
        } catch (JSONParseException e) {
            Log.e("ScanditSDK", "Exception when creating settings");
            e.printStackTrace();
            scanSettings = ScanSettings.create();
        }
        BarcodePickerWithSearchBar picker = new BarcodePickerWithSearchBar(this, scanSettings);
        picker.setOnScanListener(this);
        picker.setProcessFrameListener(this);
        picker.setLicenseValidationListener(this);
        picker.setTextRecognitionListener(this);
        picker.setPropertyChangeListener(this);

        this.setContentView(picker);
        mPickerStateMachine = new PickerStateMachine(picker, scanSettings, this);

        // Set all the UI options.
        PhonegapParamParser.updatePicker(picker, options, this);

        // Check buffered torch state and apply if needed.
        if (sBufferedTorchEnabled.compareAndSet(true, false)) {
            picker.switchTorchOn(true);
        }

        UIParamParser.updatePickerUI(picker, overlayOptions);
        PhonegapParamParser.updatePicker(picker, overlayOptions, this);

        mContinuousMode = PhonegapParamParser.shouldRunInContinuousMode(options);
        mIsDidScanDefined = PhonegapParamParser.isDidScanDefined(options);
        mShouldPassBarcodeFrame = PhonegapParamParser.shouldPassBarcodeFrame(options);

        mStateBeforeSuspend = PhonegapParamParser.shouldStartInPausedState(options)
                ? PickerStateMachine.PAUSED : PickerStateMachine.ACTIVE;
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
            didCancel();
        }
        // Once the activity is in the foreground again, restore previous picker state.
        mPickerStateMachine.setState(mStateBeforeSuspend);
    }

    public void switchTorchOn(boolean enabled) {
        mPickerStateMachine.getPicker().switchTorchOn(enabled);
    }

    public void didCancel() {
        mPickerStateMachine.getPicker().setOnScanListener(null);
        mPickerStateMachine.getPicker().setProcessFrameListener(null);
        mPickerStateMachine.getPicker().setLicenseValidationListener(null);
        mPickerStateMachine.getPicker().setTextRecognitionListener(null);
        mPickerStateMachine.getPicker().setPropertyChangeListener(null);

        mPickerStateMachine.setState(PickerStateMachine.STOPPED);
        setResult(CANCEL);
        finish();
        sPendingClose.set(false);
    }

    @Override
    public void didScan(ScanSession session) {
        // don't do anything if:
        // there is a pending close operation (otherwise we will deadlock)
        // or the didScan callback is undefined
        if (sPendingClose.get() || !mIsDidScanDefined) {
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

    private Bundle bundleForScanResult(ScanSession session) {
        Bundle bundle = new Bundle();
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_SCAN_EVENT,
                ResultRelay.jsonForSession(session));
        bundle.putString("jsonString", eventArgs.toString());
        return bundle;
    }

    @Override
    public void didProcess(byte[] bytes, int width, int height, ScanSession session) {
        if (sPendingClose.get()) {
            // return if there is a pending close. Otherwise we might deadlock
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
            Bundle bundle = bundleForProcessResult(newlyTrackedCodes);
            ResultRelay.relayResult(bundle);
            Marshal.rejectTrackedCodes(session, mRejectedTrackedCodeIds);

            // Call didProcessFrame only when new codes have started to be tracked.
            returnFrameBufferIfWanted(bytes, width, height);
        }
    }

    private void returnFrameBufferIfWanted(byte[] bytes, int width, int height) {
        if (mShouldPassBarcodeFrame) {
            String base64Data = SampleBufferConverter.base64StringFromFrame(bytes, width, height);
            ResultRelay.relayResult(bundleForDidProcessFrame(base64Data));
        }
    }

    private Bundle bundleForDidProcessFrame(String base64Data) {
        Bundle bundle = new Bundle();
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_PROCESS_FRAME,
                ResultRelay.jsonForDidProcessFrame(base64Data));
        bundle.putString("jsonString", args.toString());
        bundle.putBoolean("waitForResult", false);
        return bundle;
    }

    private Bundle bundleForProcessResult(List<TrackedBarcode> newylTrackedCodes) {
        Bundle bundle = new Bundle();
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_RECOGNIZE_NEW_CODES,
                ResultRelay.jsonForTrackedCodes(newylTrackedCodes));
        bundle.putString("jsonString", eventArgs.toString());
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

    private Bundle manualSearchResultsToBundle(String entry) {
        Bundle bundle = new Bundle();
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_MANUAL_SEARCH_EVENT, entry);
        bundle.putString("jsonString", args.toString());

        // no need to wait for result
        bundle.putBoolean("waitForResult", false);
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

    private Bundle bundleForTextRecognitionResult(RecognizedText recognizedText) {
        Bundle bundle = new Bundle();
        JSONArray eventArgs = Marshal.createEventArgs(ScanditSDK.DID_RECOGNIZE_TEXT_EVENT,
                ResultRelay.jsonForRecognizedText(recognizedText));
        bundle.putString("jsonString", eventArgs.toString());
        return bundle;
    }

    @Override
    public void onBackPressed() {
        sPendingClose.set(true);
        didCancel();
    }

    @Override
    public void pickerEnteredState(BarcodePickerWithSearchBar picker, int newState) {
        Bundle resultBundle = new Bundle();
        JSONArray didStopArgs = Marshal.createEventArgs(ScanditSDK.DID_CHANGE_STATE_EVENT, newState);
        resultBundle.putString("jsonString", didStopArgs.toString());
        resultBundle.putBoolean("waitForResult", false);
        ResultRelay.relayResult(resultBundle);
    }

    @Override
    public void pickerSwitchedMatrixScanState(BarcodePickerWithSearchBar picker, boolean matrixScan) {
        mLastFrameTrackedCodeIds.clear();
    }

    @Override
    public void onPropertyChange(int name, int newState) {
        ResultRelay.relayResult(bundleForPropertyChange(name, newState));
    }

    private Bundle bundleForPropertyChange(int name, int newState) {
        Bundle bundle = new Bundle();
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_CHANGE_PROPERTY,
                ResultRelay.jsonForPropertyChange(name, newState));
        bundle.putString("jsonString", args.toString());
        bundle.putBoolean("waitForResult", false);
        return bundle;
    }

    @Override
    public void failedToValidateLicense(String errorMessage) {
        ResultRelay.relayResult(bundleForLicenseValidationFail(errorMessage));
    }

    private Bundle bundleForLicenseValidationFail(String errorMessage) {
        Bundle bundle = new Bundle();
        JSONArray args = Marshal.createEventArgs(ScanditSDK.DID_FAIL_TO_VALIDATE_LICENSE,
                ResultRelay.jsonForLicenseValidationFail(errorMessage));
        bundle.putString("jsonString", args.toString());
        bundle.putBoolean("waitForResult", false);
        return bundle;
    }
}
