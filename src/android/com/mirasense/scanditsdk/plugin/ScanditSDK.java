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

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.barcodepicker.internal.ScanditSDKGlobals;
import com.scandit.base.util.JSONParseException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;


public class ScanditSDK extends CordovaPlugin {

    public static final String DID_SCAN_EVENT = "didScan";
    public static final String DID_MANUAL_SEARCH_EVENT = "didManualSearch";
    public static final String DID_RECOGNIZE_TEXT_EVENT = "didRecognizeText";
    public static final String DID_CHANGE_STATE_EVENT = "didChangeState";
    public static final String DID_RECOGNIZE_NEW_CODES = "didRecognizeNewCodes";
    public static final String DID_CHANGE_PROPERTY = "didChangeProperty";
    public static final String DID_FAIL_TO_VALIDATE_LICENSE = "didFailToValidateLicense";
    public static final String DID_PROCESS_FRAME = "didProcessFrame";


    private static final String INIT_LICENSE_COMMAND = "initLicense";
    private static final String SHOW_COMMAND = "show";
    private static final String APPLY_SETTINGS_COMMAND = "applySettings";
    private static final String CANCEL_COMMAND = "cancel";
    private static final String PAUSE_COMMAND = "pause";
    private static final String RESUME_COMMAND = "resume";
    private static final String START_COMMAND = "start";
    private static final String STOP_COMMAND = "stop";
    private static final String RESIZE_COMMAND = "resize";
    private static final String UPDATE_OVERLAY_COMMAND = "updateOverlay";
    private static final String ENABLE_TORCH_COMMAND = "torch";
    private static final String FINISH_DID_SCAN_COMMAND = "finishDidScanCallback";
    private static final String FINISH_DID_RECOGNIZE_NEW_CODES_COMMAND = "finishDidRecognizeNewCodesCallback";
    private static final int REQUEST_CAMERA_PERMISSION = 505;

    private CallbackContext mCallbackContext;

    private ScanditWorker mWorker = null;
    private boolean mRequestingCameraPermission = false;
    private IPickerController mPickerController;

    static class Command {
        Command(String action, JSONArray args, CallbackContext callbackContext) {
            this.action = action;
            this.args = args;
            this.callbackContext = callbackContext;
        }
        JSONArray args;
        String action;
        CallbackContext callbackContext;
    }

    private ArrayList<Command> mQueuedCommands = new ArrayList<Command>();
    private WeakReference<ResizeScannerInterface> resizeListener = new WeakReference<ResizeScannerInterface>(null);

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        return this.executeCommand(action, args, callbackContext, false);
    }

    private boolean executeCommand(String action, JSONArray args, CallbackContext callbackContext,
                                   boolean isQueuedCommand) {
        if (mWorker == null) {
            mWorker = new ScanditWorker();
            mWorker.start();
        }

        // check if we have to request camera permission before executing commands.
        if (!mRequestingCameraPermission && !isQueuedCommand && action.equals(SHOW_COMMAND)) {
            mRequestingCameraPermission =
                    !PermissionHelper.hasPermission(this, Manifest.permission.CAMERA);
            if (mRequestingCameraPermission) {
                // request permission
                PermissionHelper.requestPermission(this, REQUEST_CAMERA_PERMISSION,
                        Manifest.permission.CAMERA);
            }
        }

        if (mRequestingCameraPermission) {
            // queue all the commands until we have finished asking for permission. We will then
            // either have the permission, or display a message in the picker that says that we
            // don't have permission to access the camera.
            mQueuedCommands.add(new Command(action, args, callbackContext));
            return true;
        }

        if (action.equals(INIT_LICENSE_COMMAND)) {
            initLicense(args);
        } else if (action.equals(SHOW_COMMAND)) {
            mCallbackContext = callbackContext;
            show(args);
        } else if (action.equals(APPLY_SETTINGS_COMMAND)) {
            applySettings(args);
        } else if (action.equals(CANCEL_COMMAND)) {
            cancel(args);
        } else if (action.equals(PAUSE_COMMAND)) {
            setPickerState(PickerStateMachine.PAUSED);
        } else if (action.equals(RESUME_COMMAND)) {
            setPickerState(PickerStateMachine.ACTIVE);
        } else if (action.equals(STOP_COMMAND)) {
            setPickerState(PickerStateMachine.STOPPED);
        } else if (action.equals(START_COMMAND)) {
            // can't use setPickerState(PickerStateMachine.ACTIVE), because we need to call
            // startScanning, even if the picker was in paused state.
            startScanning(args);
        } else if (action.equals(RESIZE_COMMAND)) {
            resize(args);
        } else if (action.equals(UPDATE_OVERLAY_COMMAND)) {
            updateOverlay(args);
        } else if (action.equals(ENABLE_TORCH_COMMAND)) {
            torch(args);
        } else if (action.equals(FINISH_DID_SCAN_COMMAND)) {
            finishDidScanCallback(args);
        } else if (action.equals(FINISH_DID_RECOGNIZE_NEW_CODES_COMMAND)) {
            finishDidRecognizeNewCodesCallback(args);
        } else {
            callbackContext.error("Invalid Action: " + action);
            return false;
        }
        return true;
    }

    private void initLicense(JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The initLicense call received too few arguments and has to return without starting.");
            return;
        }

        try {
            String appKey = data.getString(0);
            ScanditSDKGlobals.usedFramework = "phonegap";
            ScanditLicense.setAppKey(appKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            return;
        }
        mRequestingCameraPermission = false;
        // execute all the queued commands and clear.
        for (Command queuedCommand : mQueuedCommands) {
            this.executeCommand(queuedCommand.action, queuedCommand.args,
                    queuedCommand.callbackContext, true);
        }
        mQueuedCommands.clear();
    }

    private void show(JSONArray data) {
        if (data.length() > 2) {
            // We extract all options and add them to the intent extra bundle.
            try {
                final JSONObject settings = unifyRecognitionMode(data.getJSONObject(0));
                final Bundle options = new Bundle();
                setOptionsOnBundle(data.getJSONObject(1), options);
                final Bundle overlayOptions = new Bundle();
                setOptionsOnBundle(data.getJSONObject(2), overlayOptions);
                showPicker(settings, options, overlayOptions, false);

            } catch (JSONException e) {
                Log.e("ScanditSDK", "The show call received too few arguments and has to return without starting.");
                e.printStackTrace();
            }
        }
    }

    private void showPicker(final JSONObject settings, final Bundle options,
                            final Bundle overlayOptions, final boolean legacyMode) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean showPickerAsSubView =
                        options.containsKey(PhonegapParamParser.paramPortraitMargins) ||
                                options.containsKey(PhonegapParamParser.paramLandscapeMargins) ||
                                options.containsKey(PhonegapParamParser.paramPortraitConstraints) ||
                                options.containsKey(PhonegapParamParser.paramLandscapeConstraints);
                if (showPickerAsSubView) {
                    if (mPickerController == null || !(mPickerController instanceof SubViewPickerController)) {
                        mPickerController = new SubViewPickerController(ScanditSDK.this, mCallbackContext);
                        ((SubViewPickerController) mPickerController).setResizeListener(resizeListener.get());
                    }
                } else {
                    mPickerController = new FullscreenPickerController(ScanditSDK.this, mCallbackContext);
                }

                mPickerController.show(settings, options, overlayOptions, legacyMode, mCallbackContext);
            }
        });
    }

    private void applySettings(JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The applySettings call received too few arguments and has to return without starting.");
            return;
        }
        try {
            final JSONObject settings = unifyRecognitionMode(data.getJSONObject(0));

            mWorker.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (mPickerController == null)
                                return;
                            try {
                                ScanSettings scanSettings = ScanSettings.createWithJson(settings);
                                mPickerController.applyScanSettings(scanSettings);
                            } catch (JSONParseException e) {
                                Log.e("ScanditSDK", "Exception when creating settings");
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private JSONObject unifyRecognitionMode(JSONObject settings) throws JSONException {
        if (settings.has("recognitionMode")) {
            int mode = settings.optInt("recognitionMode", -1);
            if (mode == 1) {
                settings.put("recognitionMode", "text");
            } else if (mode == 2) {
                settings.put("recognitionMode", "code");
            }
        }
        
        return settings;
    }

    private void updateOverlay(final JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The updateOverlay call received too few arguments and has to return without starting.");
            return;
        }

        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mPickerController != null) {
                    final Bundle bundle = new Bundle();
                    try {
                        setOptionsOnBundle(data.getJSONObject(0), bundle);
                        mPickerController.updateUI(bundle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void cancel(final JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mPickerController == null) return;
                mPickerController.close();
            }
        });
    }

    private void startScanning(final JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mPickerController == null) return;
                boolean startPaused = false;
                if (data.length() > 0) {
                    try {
                        JSONObject options = data.getJSONObject(0);
                        if (options.has("paused")) {
                            startPaused = options.getBoolean("paused");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mPickerController.startScanning(startPaused);
            }
        });
    }

    private void setPickerState(final int state) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mPickerController == null) return;
                mPickerController.setState(state);
            }
        });
    }

    private void resize(final JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The resize call received too few arguments and has to return without starting.");
            return;
        }
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mPickerController != null) {
                    final Bundle bundle = new Bundle();
                    try {
                        setOptionsOnBundle(data.getJSONObject(0), bundle);
                        mPickerController.updateLayout(bundle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void torch(final JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The torch call received too few arguments and has to return without starting.");
            return;
        }
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean enabled = data.getBoolean(0);
                    mPickerController.setTorchEnabled(enabled);
                } catch (JSONException e) {
                    // FIXME: error handling?
                    e.printStackTrace();
                }
            }
        });
    }

    private void finishDidScanCallback(final JSONArray data) {
        mPickerController.finishDidScanCallback(data);
    }

    private void finishDidRecognizeNewCodesCallback(final JSONArray data) {
        mPickerController.finishDidRecognizeNewCodesCallback(data);
    }

    private void setOptionsOnBundle(JSONObject options, Bundle bundle) {
        @SuppressWarnings("unchecked")
        Iterator<String> iter = options.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            Object obj = options.opt(key);
            if (obj != null) {
                if (obj instanceof Float) {
                    bundle.putFloat(key.toLowerCase(), (Float) obj);
                } else if (obj instanceof Double) {
                    bundle.putFloat(key.toLowerCase(), new Float((Double) obj));
                } else if (obj instanceof Integer) {
                    bundle.putInt(key.toLowerCase(), (Integer) obj);
                } else if (obj instanceof Boolean) {
                    bundle.putBoolean(key.toLowerCase(), (Boolean) obj);
                } else if (obj instanceof String) {
                    bundle.putString(key.toLowerCase(), (String) obj);
                } else if (obj instanceof JSONArray) {
                    ArrayList<Object> list = new ArrayList<Object>();
                    JSONArray array = (JSONArray) obj;
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            Object item = array.get(i);
                            if (item instanceof Double) {
                                list.add(new Float((Double) item));
                            } else {
                                list.add(array.get(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    bundle.putSerializable(key.toLowerCase(), list);
                } else if (obj instanceof JSONObject) {
                    Bundle dictionary = new Bundle();
                    setOptionsOnBundle((JSONObject) obj, dictionary);
                    bundle.putBundle(key.toLowerCase(), dictionary);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPickerController == null) return;
        mPickerController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        if (mPickerController == null) return;
        mPickerController.onActivityPause();
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (mPickerController == null) return;
        mPickerController.onActivityResume();
    }

    public void setResizeListener(ResizeScannerInterface listener) {
        resizeListener = new WeakReference<ResizeScannerInterface>(listener);
        if (mPickerController instanceof SubViewPickerController) {
            ((SubViewPickerController) mPickerController).setResizeListener(listener);
        }
    }
}
