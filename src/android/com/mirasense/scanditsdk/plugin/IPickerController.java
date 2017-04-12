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

import android.content.Intent;
import android.os.Bundle;

import com.scandit.barcodepicker.ScanSettings;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Defines the interface for classes that control a barcode picker instance. This interface is
 * implemented by a "full-screen" picker and a "subview" picker controller. The former controls a
 * picker instance running in a separate activity, the latter controls a picker displayed as a
 * subview in the actual plugin activity.
 */
public interface IPickerController {
    /**
     * Set the desired state of the picker.
     *
     * @param state The new state
     */
    void setState(int state);

    void show(JSONObject settings, Bundle options, Bundle overlayOptions, boolean legacyMode);

    /**
     * @brief Apply new scan settings to the picker
     *
     * @param scanSettings
     */
    void applyScanSettings(ScanSettings scanSettings);

    /**
     * @brief Update the UI
     *
     * @param overlayOptions The overlay options. Currently this is only supported when using the
     *                       subview picker. The paramters of the full-screen picker can not be
     *                       changed for the full-screen picker while the picker is running.
     */
    void updateUI(Bundle overlayOptions);

    /**
     * @brief Update the Layout
     *
     * @param layoutOptions The layout options. Currently this is only supported when using the
     *                      subview picker. The paramters of the full-screen picker can not be
     *                      changed for the full-screen picker while the picker is running.
     */
    void updateLayout(Bundle layoutOptions);

    /**
     * Enable/disable the torch
     *
     * @param enabled Whether the torch should be enabled.
     */
    void setTorchEnabled(boolean enabled);

    /**
     * Close (cancel) then picker
     */
    void close();

    /**
     * Called when the main plugin activity gets paused
     */
    void onActivityPause();

    /**
     * Called when the main plugin activity gets resumed
     */
    void onActivityResume();

    /**
     * Called when a result becomes available from a launched intent. Only used for the full-screen
     * picker controller.
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data data containing the result.
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);

    void finishDidScanCallback(JSONArray data);

    void startScanning();
}
