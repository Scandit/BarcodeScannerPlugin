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

import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ocr.RecognizedText;
import com.scandit.recognition.Barcode;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains helpers for marshalling data to JS in the format understood by the JS portion of the
 * Scandit BarcodeScanner plugin.
 */
public class Marshal {

    public static JSONArray createEventArgs(String eventName, JSONObject arg) {
        JSONArray args = new JSONArray();
        args.put(eventName);
        args.put(arg);
        return args;
    }

    public static JSONArray createEventArgs(String eventName, int arg) {
        JSONArray args = new JSONArray();
        args.put(eventName);
        args.put(arg);
        return args;
    }

    public static JSONArray createEventArgs(String eventName, String arg) {
        JSONArray args = new JSONArray();
        args.put(eventName);
        args.put(arg);
        return args;
    }

    public static PluginResult createOkResult(JSONArray args) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, args);
        result.setKeepCallback(true);
        return result;
    }



    public static PluginResult createFailResult(String message) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
        result.setKeepCallback(true);
        return result;
    }

    public static PluginResult createCancel() {
        return createFailResult("Canceled");
    }


    public static void rejectCodes(ScanSession session, ArrayList<Long> rejectedCodeIds) {
        if (rejectedCodeIds == null) {
            return;
        }
        List<Barcode> newlyRecognized = session.getNewlyRecognizedCodes();
        for (Long id : rejectedCodeIds) {
            for (Barcode code : newlyRecognized) {
                if (code.getHandle() == id) {
                    session.rejectCode(code);
                }
            }
        }
    }

    public static void rejectRecognizedTexts(RecognizedText recognizedText, ArrayList<Long> rejectedCodeIds) {
        if (rejectedCodeIds != null && !rejectedCodeIds.isEmpty()) {
            recognizedText.setRejected(true);
        }
    }

}
