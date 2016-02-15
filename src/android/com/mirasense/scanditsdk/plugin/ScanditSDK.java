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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.barcodepicker.internal.ScanditSDKGlobals;
import com.mirasense.scanditsdk.plugin.ScanditSDKResultRelay.ScanditSDKResultRelayCallback;
import com.scandit.base.system.SbSystemUtils;
import com.scandit.base.util.JSONParseException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;


public class ScanditSDK extends CordovaPlugin implements ScanditSDKResultRelayCallback,
        OnScanListener, SearchBarBarcodePicker.ScanditSDKSearchBarListener {

    public static final String INIT_LICENSE = "initLicense";
    public static final String SHOW = "show";
    public static final String SCAN = "scan";
    public static final String APPLY_SETTINGS = "applySettings";
    public static final String CANCEL = "cancel";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String RESIZE = "resize";
    public static final String TORCH = "torch";
    
    public static DisplayMetrics DISPLAY;

    private CallbackContext mCallbackContext;
    private boolean mContinuousMode = false;
    private boolean mPendingOperation = false;

    private final OrientationHandler mHandler = new OrientationHandler(Looper.getMainLooper());

    private SearchBarBarcodePicker mBarcodePicker;
    private RelativeLayout mLayout;

    private ScanditWorker mWorker = null;
    private boolean mLegacyMode = false;

    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (mWorker == null) {
            mWorker = new ScanditWorker();
            mWorker.start();
        }
        
        if (action.equals(INIT_LICENSE)) {
            initLicense(args);
            return true;
        } else if (action.equals(SHOW)) {
            mCallbackContext = callbackContext;
            show(args);
            return true;
        } else if (action.equals(SCAN)) {
            mCallbackContext = callbackContext;
            scan(args);
            return true;
        } else if (action.equals(APPLY_SETTINGS)) {
            applySettings(args);
            return true;
        } else if (action.equals(CANCEL)) {
            cancel(args);
            return true;
        } else if (action.equals(PAUSE)) {
            pause(args);
            return true;
        } else if (action.equals(RESUME)) {
            resume(args);
            return true;
        } else if (action.equals(STOP)) {
            stop(args);
            return true;
        } else if (action.equals(START)) {
            start(args);
            return true;
        } else if (action.equals(RESIZE)) {
            resize(args);
            return true;
        } else if (action.equals(TORCH)) {
            torch(args);
            return true;
        } else {
            callbackContext.error("Invalid Action: " + action);
            return false;
        }
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

    private void show(JSONArray data) {
        if (mPendingOperation) {
            return;
        }
        mPendingOperation = true;
        mHandler.start();

        if (data.length() > 2) {
            // We extract all options and add them to the intent extra bundle.
            try {
                final JSONObject settings = data.getJSONObject(0);
                final Bundle options = new Bundle();
                setOptionsOnBundle(data.getJSONObject(1), options);
                final Bundle overlayOptions = new Bundle();
                setOptionsOnBundle(data.getJSONObject(2), overlayOptions);

                mLegacyMode = false;

                showPicker(settings, options, overlayOptions);

            } catch (JSONException e) {
                Log.e("ScanditSDK", "The show call received too few arguments and has to return without starting.");
                e.printStackTrace();
            }
        }
    }

    private void scan(JSONArray data) {
        if (mPendingOperation) {
            return;
        }
        mPendingOperation = true;
        mHandler.start();

        final Bundle options = new Bundle();
        try {
            ScanditSDKGlobals.usedFramework = "phonegap";
            ScanditLicense.setAppKey(data.getString(0));
        } catch (JSONException e) {
            Log.e("ScanditSDK", "Function called through Java Script contained illegal objects.");
            e.printStackTrace();
            return;
        }

        if (data.length() > 1) {
            // We extract all options and add them to the intent extra bundle.
            try {
                setOptionsOnBundle(data.getJSONObject(1), options);
                mLegacyMode = true;
                showPicker(null, options, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPicker(final JSONObject settings, final Bundle options, final Bundle overlayOptions) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                mContinuousMode = false;
                if (options.containsKey(PhonegapParamParser.paramContinuousMode)) {
                    mContinuousMode = options.getBoolean(PhonegapParamParser.paramContinuousMode);
                }

                if (options.containsKey(PhonegapParamParser.paramPortraitMargins)
                        || options.containsKey(PhonegapParamParser.paramLandscapeMargins)) {
                    final AtomicBoolean done = new AtomicBoolean(false);
                    Runnable task = new Runnable() {
                        public void run() {
                            synchronized (this) {
                                ScanSettings scanSettings;
                                if (settings == null) {
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
                                mBarcodePicker = new SearchBarBarcodePicker(cordova.getActivity(), scanSettings);
                                mBarcodePicker.setOnScanListener(ScanditSDK.this);

                                // Set all the UI options.
                                PhonegapParamParser.updatePicker(mBarcodePicker, options, ScanditSDK.this);

                                if (mLegacyMode) {
                                    LegacyUIParamParser.updatePickerUI(cordova.getActivity(), mBarcodePicker, options);
                                } else {
                                    UIParamParser.updatePickerUI(mBarcodePicker, overlayOptions);
                                    PhonegapParamParser.updatePicker(mBarcodePicker, overlayOptions, ScanditSDK.this);
                                }

                                // Create the layout to add the picker to and add it on top of the web view.
                                mLayout = new RelativeLayout(cordova.getActivity());

                                ViewGroup viewGroup = getViewGroupToAddTo();
                                if (viewGroup != null) {
                                    viewGroup.addView(mLayout);
                                }

                                RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                                mLayout.addView(mBarcodePicker, rLayoutParams);
                                PhonegapParamParser.updateLayout(cordova.getActivity(), mBarcodePicker, options);

                                // Only already start in legacy mode.
                                if (mLegacyMode) {
                                    if (options.containsKey(PhonegapParamParser.paramPaused)
                                            && options.getBoolean(PhonegapParamParser.paramPaused)) {
                                        mBarcodePicker.startScanning(true);
                                    } else {
                                        mBarcodePicker.startScanning();
                                    }
                                }
                                done.set(true);
                                notify();
                            }
                        }
                    };
                    cordova.getActivity().runOnUiThread(task);
                    synchronized (task) {
                        while (!done.get()) {
                            try {
                                task.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                } else {
                    ScanditSDKResultRelay.setCallback(ScanditSDK.this);
                    Intent intent = new Intent(cordova.getActivity(), ScanditSDKActivity.class);
                    if (settings != null) {
                        intent.putExtra("settings", settings.toString());
                    }
                    intent.putExtra("options", options);
                    if (overlayOptions != null) {
                        intent.putExtra("overlayOptions", overlayOptions);
                    }
                    cordova.startActivityForResult(ScanditSDK.this, intent, 1);
                }
            }
        });
    }

    private void applySettings(JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The applySettings call received too few arguments and has to return without starting.");
            return;
        }
        try {
            final Bundle bundle = new Bundle();
            setOptionsOnBundle(data.getJSONObject(0), bundle);

            mWorker.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mBarcodePicker != null) {
                        final AtomicBoolean done = new AtomicBoolean(false);
                        Runnable task = new Runnable() {
                            public void run() {
                                synchronized (this) {
                                    if (mLegacyMode) {
                                        LegacyUIParamParser.updatePickerUI(cordova.getActivity(), mBarcodePicker, bundle);
                                    }
                                    PhonegapParamParser.updateLayout(cordova.getActivity(), mBarcodePicker, bundle);
                                }
                            }
                        };
                        cordova.getActivity().runOnUiThread(task);
                        synchronized (task) {
                            while (!done.get()) {
                                try {
                                    task.wait();
                                } catch (InterruptedException e) {}
                            }
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cancel(JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mBarcodePicker != null) {
                    removeSubviewPicker();
                    mPendingOperation = false;
                    mHandler.stop();
                    mCallbackContext.error("Canceled");
                } else {
                    ScanditSDKActivity.cancel();
                }
            }
        });
    }

    private void start(JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mBarcodePicker != null) {
                    mBarcodePicker.startScanning();
                } else {
                    ScanditSDKActivity.start();
                }
            }
        });
    }

    private void pause(JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mBarcodePicker != null) {
                    mBarcodePicker.pauseScanning();
                } else {
                    ScanditSDKActivity.pause();
                }
            }
        });
    }

    private void resume(JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mBarcodePicker != null) {
                    mBarcodePicker.resumeScanning();
                } else {
                    ScanditSDKActivity.resume();
                }
            }
        });
    }

    private void stop(JSONArray data) {
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mBarcodePicker != null) {
                    mBarcodePicker.stopScanning();
                } else {
                    ScanditSDKActivity.stop();
                }
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
                if (mBarcodePicker != null) {
                    final Bundle bundle = new Bundle();
                    try {
                        setOptionsOnBundle(data.getJSONObject(0), bundle);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (mLegacyMode) {
                                LegacyUIParamParser.updatePickerUI(cordova.getActivity(), mBarcodePicker, bundle);
                            }
                            PhonegapParamParser.updateLayout(cordova.getActivity(), mBarcodePicker, bundle);
                        }
                    });
                }
            }
        });
    }

    /**
     * Switches the torch on or off. Pass true to turn it on, false to turn it off.
     * You call this the following way from java script:
     *
     * cordova.exec(null, null, "ScanditSDK", "torch", [true]);
     */
    private void torch(final JSONArray data) {
        if (data.length() < 1) {
            Log.e("ScanditSDK", "The torch call received too few arguments and has to return without starting.");
            return;
        }
        mWorker.getHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean enabled = false;
                try {
                    enabled = data.getBoolean(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final boolean innerEnabled = enabled;
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (mBarcodePicker != null) {
                            mBarcodePicker.switchTorchOn(innerEnabled);
                        } else {
                            ScanditSDKActivity.torch(innerEnabled);
                        }
                    }
                });
            }
        });
    }
    
    private void removeSubviewPicker() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mBarcodePicker.stopScanning();

                ViewGroup viewGroup = getViewGroupToAddTo();
                if (viewGroup != null) {
                    viewGroup.removeView(mLayout);
                }
                mLayout = null;
                mBarcodePicker = null;
            }
        });
    }

    private void setOptionsOnBundle(JSONObject options, Bundle bundle) {
        @SuppressWarnings("unchecked")
        Iterator<String> iter = (Iterator<String>) options.keys();
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
                    JSONArray array = (JSONArray)obj;
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
                    setOptionsOnBundle((JSONObject)obj, dictionary);
                    bundle.putBundle(key, dictionary);
                }
            }
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ScanditSDKResultRelay.setCallback(null);
        if (resultCode == ScanditSDKActivity.SCAN || resultCode == ScanditSDKActivity.MANUAL) {
            PluginResult result = resultForBundle(data.getExtras());
            mPendingOperation = false;
            mHandler.stop();
            mCallbackContext.sendPluginResult(result);

        } else if (resultCode == ScanditSDKActivity.CANCEL) {
            mPendingOperation = false;
            mHandler.stop();
            mCallbackContext.error("Canceled");
        }
    }

    @Override
    public void onResultByRelay(Bundle bundle) {
        PluginResult result = resultForBundle(bundle);
        if (mContinuousMode) {
            result.setKeepCallback(true);
        } else {
            mPendingOperation = false;
            mHandler.stop();
        }
        mCallbackContext.sendPluginResult(result);
    }

    private PluginResult resultForBundle(Bundle bundle) {
        PluginResult result;
        if (bundle.containsKey("jsonString")) {
            String jsonString = bundle.getString("jsonString");
            try {
                JSONObject json = new JSONObject(jsonString);
                result = new PluginResult(Status.OK, json);
            } catch (JSONException e) {
                e.printStackTrace();
                result = new PluginResult(Status.OK, "error");
            }
        } else if (bundle.containsKey("string")) {
            String string = bundle.getString("string");
            result = new PluginResult(Status.OK, string);
        } else {
            // Legacy
            String barcode = bundle.getString("barcode");
            String symbology = bundle.getString("symbology");
            JSONArray args = new JSONArray();
            args.put(barcode);
            args.put(symbology);
            result = new PluginResult(Status.OK, args);
        }
        return result;
    }

    @Override
    public void didScan(ScanSession session) {
        if (session.getNewlyRecognizedCodes().size() > 0) {
            PluginResult result;
            if (mLegacyMode) {
                JSONArray args = new JSONArray();
                args.put(session.getNewlyRecognizedCodes().get(0).getData());
                args.put(session.getNewlyRecognizedCodes().get(0).getSymbologyName());
                result = new PluginResult(Status.OK, args);
            } else {
                result = new PluginResult(Status.OK, ScanditSDKResultRelay.jsonForSession(session));
            }
            if (mContinuousMode) {
                result.setKeepCallback(true);
            } else {
                removeSubviewPicker();
                mPendingOperation = false;
                mHandler.stop();
            }
            mCallbackContext.sendPluginResult(result);
        }
    }

    @Override
    public void didEnter(String entry) {
        PluginResult result;
        if (mLegacyMode) {
            JSONArray args = new JSONArray();
            args.put(entry);
            args.put("UNKNOWN");
            result = new PluginResult(Status.OK, args);
        } else {
            result = new PluginResult(Status.OK, entry);
        }
        if (mContinuousMode) {
            result.setKeepCallback(true);
        } else {
            removeSubviewPicker();
            mPendingOperation = false;
            mHandler.stop();
        }
        mCallbackContext.sendPluginResult(result);
    }

    private ViewGroup getViewGroupToAddTo() {
        if (webView instanceof WebView) {
            return (ViewGroup) webView;
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
                Log.e("ScanditSDK", "Unable to fetch the ViewGroup through webView.getView().getParent().");
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        DISPLAY = this.cordova.getActivity().getApplicationContext().getResources().getDisplayMetrics();
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);

        if (mBarcodePicker != null) {
            mBarcodePicker.stopScanning();
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (mBarcodePicker != null) {
            mBarcodePicker.startScanning();
        }
    }

    /**
     * Handler to check the orientation of the phone and adjust the margins based on it.
     */
    private final class OrientationHandler extends Handler {

        final static int CHECK_ORIENTATION = 1;
        private int mLastRotation = 0;
        private boolean mRunning = false;

        public OrientationHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CHECK_ORIENTATION:
                    if (mRunning) {
                        checkOrientation();
                    }
                    break;
            }
        }

        private void start() {
            if (!mRunning) {
                mRunning = true;
                mLastRotation = SbSystemUtils.getDisplayRotation(cordova.getActivity());
                mHandler.sendEmptyMessageDelayed(OrientationHandler.CHECK_ORIENTATION, 20);
            }
        }

        private void stop() {
            if (mRunning) {
                mRunning = false;
            }
        }

        private void checkOrientation() {
            Context context = cordova.getActivity();
            if (context != null) {
                int displayRotation = SbSystemUtils.getDisplayRotation(context);
                if (displayRotation != mLastRotation) {

                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = new Bundle();
                            ArrayList<Object> list = new ArrayList<Object>();
                            list.add((Integer) mBarcodePicker.portraitMargins.left);
                            list.add((Integer) mBarcodePicker.portraitMargins.top);
                            list.add((Integer) mBarcodePicker.portraitMargins.right);
                            list.add((Integer) mBarcodePicker.portraitMargins.bottom);
                            bundle.putSerializable(PhonegapParamParser.paramPortraitMargins, list);

                            list = new ArrayList<Object>();
                            list.add((Integer) mBarcodePicker.landscapeMargins.left);
                            list.add((Integer) mBarcodePicker.landscapeMargins.top);
                            list.add((Integer) mBarcodePicker.landscapeMargins.right);
                            list.add((Integer) mBarcodePicker.landscapeMargins.bottom);
                            bundle.putSerializable(PhonegapParamParser.paramLandscapeMargins, list);

                            PhonegapParamParser.updateLayout(cordova.getActivity(), mBarcodePicker, bundle);
                        }
                    });
                    mLastRotation = displayRotation;
                }
                mHandler.sendEmptyMessageDelayed(OrientationHandler.CHECK_ORIENTATION, 20);
            }
        }
    }
}
