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

import android.content.Context;
import android.graphics.Point;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.PropertyChangeListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ocr.RecognizedText;
import com.scandit.recognition.Barcode;
import com.scandit.recognition.Quadrilateral;
import com.scandit.recognition.TrackedBarcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ResultRelay {
    
    private static Callback mCallback;
    
    public static void setCallback(Callback callback) {
        mCallback = callback;
    }
    
    public static int relayResult(Bundle bundle) {
        if (mCallback != null) {
            return mCallback.onRelayedResult(bundle);
        }
        return 0;
    }

    public static JSONObject jsonForPropertyChange(int propertyName, int newState) {
        String name;
        switch (propertyName) {
            case PropertyChangeListener.TORCH:
                name = "torchOn";
                break;
            case PropertyChangeListener.SWITCH_CAMERA:
                name = "switchCamera";
                break;
            case PropertyChangeListener.RECOGNITION_MODE:
                name = "recognitionMode";
                break;
            case PropertyChangeListener.RELATIVE_ZOOM:
                name = "relativeZoom";
                break;
            default:
                name = "";
                break;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("newState", newState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject jsonForDidProcessFrame(String base64Data) {
        JSONObject json = new JSONObject();
        try {
            json.put("base64Data", base64Data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject jsonForLicenseValidationFail(String errorMessage) {
        JSONObject json = new JSONObject();
        try {
            json.put("errorMessage", errorMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject jsonForSession(ScanSession session, BarcodePicker picker) {
        JSONObject json = new JSONObject();
        try {
            json.put("newlyRecognizedCodes", jsonForCodes(session.getNewlyRecognizedCodes(), picker));
            json.put("newlyLocalizedCodes", jsonForCodes(session.getNewlyLocalizedCodes(), picker));
            json.put("allRecognizedCodes", jsonForCodes(session.getAllRecognizedCodes(), picker));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject jsonForTrackedCodes(List<TrackedBarcode> trackedCodes, BarcodePicker picker) {
        JSONObject json = new JSONObject();
        try {
            json.put("newlyTrackedCodes", jsonForCodes(trackedCodes, picker));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONArray jsonForCodes(List<? extends Barcode> codes, BarcodePicker picker) {
        JSONArray array = new JSONArray();

        for (Barcode code : codes) {
            JSONObject object = new JSONObject();
            try {
                object.put("symbology", code.getSymbologyName());
                object.put("gs1DataCarrier", code.isGs1DataCarrier());
                object.put("recognized", code.isRecognized());
                object.put("data", code.getData());
                object.put("location", jsonForQuadrilateral(code.getLocation()));
                // XXX The JS layer expects the value to be in dp instead of pixels, hence we need the extra
                // scaling for the converted locations.
                object.put("convertedLocation", jsonForQuadrilateral(convertQuadrilateral(picker, code.getLocation())));
                object.put("compositeFlag", code.getCompositeFlag());
                if (code instanceof TrackedBarcode) {
                    object.put("uniqueId", ((TrackedBarcode) code).getId());
                    object.put("predictedLocation", jsonForQuadrilateral(((TrackedBarcode) code).getPredictedLocation()));
                    // XXX The JS layer expects the value to be in dp instead of pixels, hence we need the extra
                    // scaling for the converted locations.
                    object.put("convertedPredictedLocation", jsonForQuadrilateral(convertQuadrilateral(picker, ((TrackedBarcode) code).getPredictedLocation())));
                } else {
                    object.put("uniqueId", code.getHandle());
                }
                if (code.isRecognized()) {
                    JSONArray bytes = new JSONArray();
                    byte[] rawData = code.getRawData();
                    for (byte theByte : rawData) {
                        bytes.put((int) theByte);
                    }
                    object.put("rawData", bytes);
                }
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    private static Quadrilateral convertQuadrilateral(BarcodePicker picker, Quadrilateral rect) {
        return new Quadrilateral(
                dpFromPx(picker.getContext(), picker.convertPointToPickerCoordinates(rect.top_left)),
                dpFromPx(picker.getContext(), picker.convertPointToPickerCoordinates(rect.top_right)),
                dpFromPx(picker.getContext(), picker.convertPointToPickerCoordinates(rect.bottom_left)),
                dpFromPx(picker.getContext(), picker.convertPointToPickerCoordinates(rect.bottom_right))
        );
    }

    private static Point dpFromPx(Context context, Point point) {
        float displayDensity = context.getResources().getDisplayMetrics().density;
        return new Point(
                (int) (point.x / displayDensity),
                (int) (point.y / displayDensity)
        );
    }

    private static JSONObject jsonForQuadrilateral(Quadrilateral quadrilateral) {
        JSONObject obj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            array.put(quadrilateral.top_left.x);
            array.put(quadrilateral.top_left.y);
            obj.put("topLeft", array);

            array = new JSONArray();
            array.put(quadrilateral.top_right.x);
            array.put(quadrilateral.top_right.y);
            obj.put("topRight", array);

            array = new JSONArray();
            array.put(quadrilateral.bottom_left.x);
            array.put(quadrilateral.bottom_left.y);
            obj.put("bottomLeft", array);

            array = new JSONArray();
            array.put(quadrilateral.bottom_right.x);
            array.put(quadrilateral.bottom_right.y);
            obj.put("bottomRight", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static JSONObject jsonForRecognizedText(RecognizedText recognizedText) {
        JSONObject json = new JSONObject();
        try {
            json.put("text", recognizedText.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public interface Callback {
        int onRelayedResult(Bundle bundle);
    }
}
