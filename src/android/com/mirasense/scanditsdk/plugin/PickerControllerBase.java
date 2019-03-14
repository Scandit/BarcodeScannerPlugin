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


import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for the picker controllers.
 */
abstract class PickerControllerBase implements IPickerController {

    final CordovaPlugin mPlugin;
    CallbackContext mCallbackContext;

    // Indicates whether a result callback (any of didScan, didRecognizeNewCodes, didRecognizeText)
    // is in process.
    private AtomicInteger mInFlightResultCallbackId = new AtomicInteger(0);
    // The id of the last result callback (any of didScan, didRecognizeNewCodes, didRecognizeText)
    private AtomicInteger mLastResultCallbackId = new AtomicInteger(0);
    private AtomicBoolean mShouldBlockForDidScan = new AtomicBoolean(false);
    private int mNextState = 0;
    private final Object mSync = new Object();


    PickerControllerBase(CordovaPlugin plugin, CallbackContext callbacks) {
        mPlugin = plugin;
        mCallbackContext = callbacks;
    }

    boolean isResultCallbackInFlight() {
        return mInFlightResultCallbackId.get() != 0;
    }

    private void clearInFlightResultCallbackAndNotify() {
        synchronized (mSync) {
            mInFlightResultCallbackId.set(0);
            mSync.notifyAll();
        }
    }

    @Override
    public void setState(int state) {
        mShouldBlockForDidScan.set(state == PickerStateMachine.ACTIVE);
        // stop any in-flight callback when there is a state change.
        clearInFlightResultCallbackAndNotify();
    }

    @Override
    public void updateLayout(Bundle layoutOptions) {
        // Do nothing by default.
    }

    @Override
    public void finishDidScanCallback(JSONArray data) {
        mNextState = 0;
        if (data != null && data.length() > 0) {
            try {
                mNextState = data.getInt(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setRejectedCodeIds(determineRejectedCodes(data, 1));
        clearInFlightResultCallbackAndNotify();
    }

    @Override
    public void finishDidRecognizeNewCodesCallback(JSONArray data) {
        setRejectedTrackedCodeIds(determineRejectedCodes(data, 0));
        setTrackedCodeStates(determineStateObjects(data, 1));
        clearInFlightResultCallbackAndNotify();
    }

    public static Map<Long, JSONObject> determineStateObjects(JSONArray data, int dataIndex) {
        Map<Long, JSONObject> stateObjects = new HashMap<Long, JSONObject>();
        if (data != null && data.length() > dataIndex) {
            try {
                JSONObject jsonObject = data.getJSONObject(dataIndex);
                Iterator<String> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String id = iterator.next();
                    stateObjects.put(Long.parseLong(id), jsonObject.getJSONObject(id));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return stateObjects;
    }

    protected abstract void setTrackedCodeStates(Map<Long, JSONObject> trackedCodeStates);

    /**
     * Reads the rejected code ids out of the specified position in the json array. Handles arrays
     * that are null or too short.
     *
     * @param data The json array.
     * @param dataIndex The position at which to read the array.
     * @return The rejected code ids.
     */
    private List<Long> determineRejectedCodes(JSONArray data, int dataIndex) {
        List<Long> rejectedCodeIds = new ArrayList<Long>();
        if (data != null && data.length() > dataIndex) {
            try {
                JSONArray jsonData = data.getJSONArray(dataIndex);
                for (int i = 0; i < jsonData.length(); ++i) {
                    rejectedCodeIds.add(jsonData.getLong(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rejectedCodeIds;
    }

    protected abstract void setRejectedCodeIds(List<Long> rejectedCodeIds);

    protected abstract void setRejectedTrackedCodeIds(List<Long> rejectedCodeIds);

    int sendPluginResultBlocking(PluginResult result) {
        int currentId = mLastResultCallbackId.incrementAndGet();
        mInFlightResultCallbackId.set(currentId);
        mNextState = 0;

        try {
            mCallbackContext.sendPluginResult(result);
            synchronized (mSync) {
                while (mInFlightResultCallbackId.get() == currentId &&
                        mShouldBlockForDidScan.get()) {
                    mSync.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mNextState;
    }
}
