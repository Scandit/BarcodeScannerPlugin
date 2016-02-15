package com.mirasense.scanditsdk.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.ScanOverlay;

import java.io.Serializable;
import java.lang.NumberFormatException;
import java.util.List;

/**
 * Created by mo on 14/12/15.
 */
public class UIParamParser {

    public static final String paramBeep = "beep".toLowerCase();
    public static final String paramVibrate = "vibrate".toLowerCase();
    public static final String paramTorch = "torch".toLowerCase();

    public static final String paramTorchButtonPositionAndSize = "torchButtonPositionAndSize".toLowerCase();
    public static final String paramTorchButtonMarginsAndSize = "torchButtonMarginsAndSize".toLowerCase();
    public static final String paramTorchButtonOffAccessibilityLabel = "torchButtonOffAccessibilityLabel".toLowerCase();
    public static final String paramTorchButtonOffAccessibilityHint = "torchButtonOffAccessibilityLabel".toLowerCase();
    public static final String paramTorchButtonOnAccessibilityLabel = "torchButtonOffAccessibilityLabel".toLowerCase();
    public static final String paramTorchButtonOnAccessibilityHint = "torchButtonOffAccessibilityLabel".toLowerCase();

    public static final String paramCameraSwitchVisibility = "cameraSwitchVisibility".toLowerCase();
    public static final String paramCameraSwitchButtonPositionAndSize = "cameraSwitchButtonPositionAndSize".toLowerCase();
    public static final String paramCameraSwitchButtonMarginsAndSize = "cameraSwitchButtonMarginsAndSize".toLowerCase();
    public static final String paramCameraSwitchButtonBackAccessibilityLabel = "cameraSwitchButtonBackAccessibilityLabel".toLowerCase();
    public static final String paramCameraSwitchButtonBackAccessibilityHint = "cameraSwitchButtonBackAccessibilityHint".toLowerCase();
    public static final String paramCameraSwitchButtonFrontAccessibilityLabel = "cameraSwitchButtonFrontAccessibilityLabel".toLowerCase();
    public static final String paramCameraSwitchButtonFrontAccessibilityHint = "cameraSwitchButtonFrontAccessibilityHint".toLowerCase();

    public static final String paramViewfinderDimension = "viewfinderDimension".toLowerCase();
    public static final String paramViewfinderColor = "viewfinderColor".toLowerCase();
    public static final String paramViewfinderDecodedColor = "viewfinderDecodedColor".toLowerCase();

    public static final String paramGuiStyle = "guiStyle".toLowerCase();

    public static final String paramProperties = "properties".toLowerCase();


    public static void updatePickerUI(BarcodePicker picker, Bundle bundle) {
        if (bundle.containsKey(paramBeep)) {
            picker.getOverlayView().setBeepEnabled(bundle.getBoolean(paramBeep));
        }

        if (bundle.containsKey(paramVibrate)) {
            picker.getOverlayView().setVibrateEnabled(bundle.getBoolean(paramVibrate));
        }

        if (bundle.containsKey(paramTorch)) {
            picker.getOverlayView().setTorchEnabled(bundle.getBoolean(paramTorch));
        }

        if (bundleContainsStringKey(bundle, paramTorchButtonOffAccessibilityLabel)) {
            String label = bundle.getString(paramTorchButtonOffAccessibilityLabel);
            picker.getOverlayView().setTorchOffContentDescription(label);
        }

        if (bundleContainsStringKey(bundle, paramTorchButtonOnAccessibilityLabel)) {
            String label = bundle.getString(paramTorchButtonOnAccessibilityLabel);
            picker.getOverlayView().setTorchOnContentDescription(label);
        }

        if (bundleContainsListKey(bundle, paramTorchButtonMarginsAndSize)) {
            List<Object> marginsAndSize = (List<Object>)bundle.getSerializable(paramTorchButtonMarginsAndSize);
            if ((checkClassOfListObjects(marginsAndSize, Integer.class) || checkClassOfListObjects(marginsAndSize, String.class))
                            && marginsAndSize.size() == 4) {
                picker.getOverlayView().setTorchButtonMarginsAndSize(
                        getDp(marginsAndSize.get(0)), getDp(marginsAndSize.get(1)),
                        getDp(marginsAndSize.get(2)), getDp(marginsAndSize.get(3)));
            } else {
                Log.e("ScanditSDK", "Failed to parse torch button margins and size - wrong type");
            }
        }

        if (bundle.containsKey(paramCameraSwitchVisibility)) {
            switch (bundle.getInt(paramCameraSwitchVisibility, -1)) {
                case 0:
                    picker.getOverlayView().setCameraSwitchVisibility(ScanOverlay.CAMERA_SWITCH_NEVER);
                    break;
                case 1:
                    picker.getOverlayView().setCameraSwitchVisibility(ScanOverlay.CAMERA_SWITCH_ON_TABLET);
                    break;
                case 2:
                    picker.getOverlayView().setCameraSwitchVisibility(ScanOverlay.CAMERA_SWITCH_ALWAYS);
                    break;
                default:
                    Log.e("ScanditSDK", "Failed to parse camera switch visibility - wrong type");
                    break;
            }
        }

        if (bundleContainsListKey(bundle, paramCameraSwitchButtonMarginsAndSize)) {
            List<Object> marginsAndSize = (List<Object>)bundle.getSerializable(paramCameraSwitchButtonMarginsAndSize);
            if ((checkClassOfListObjects(marginsAndSize, Integer.class) || checkClassOfListObjects(marginsAndSize, String.class))
                            && marginsAndSize.size() == 4) {
                picker.getOverlayView().setCameraSwitchButtonMarginsAndSize(
                        getDp(marginsAndSize.get(0)), getDp(marginsAndSize.get(1)),
                        getDp(marginsAndSize.get(2)), getDp(marginsAndSize.get(3)));
            } else {
                Log.e("ScanditSDK", "Failed to parse camera switch button margins and size - wrong type");
            }
        }

        if (bundleContainsStringKey(bundle, paramCameraSwitchButtonBackAccessibilityLabel)) {
            String label = bundle.getString(paramCameraSwitchButtonBackAccessibilityLabel);
            picker.getOverlayView().setTorchOffContentDescription(label);
        }

        if (bundleContainsStringKey(bundle, paramCameraSwitchButtonFrontAccessibilityLabel)) {
            String label = bundle.getString(paramCameraSwitchButtonFrontAccessibilityLabel);
            picker.getOverlayView().setTorchOnContentDescription(label);
        }

        if (bundleContainsListKey(bundle, paramViewfinderDimension)) {
            List<Object> viewfinderDimension = (List<Object>)bundle.getSerializable(paramViewfinderDimension);
            if (checkClassOfListObjects(viewfinderDimension, Float.class)
                    && viewfinderDimension.size() == 4) {
                picker.getOverlayView().setViewfinderDimension(
                        (Float)viewfinderDimension.get(0), (Float)viewfinderDimension.get(1),
                        (Float)viewfinderDimension.get(2), (Float)viewfinderDimension.get(3));
            } else {
                Log.e("ScanditSDK", "Failed to parse viewfinder dimension - wrong type");
            }
        }

        if (bundleContainsStringKey(bundle, paramViewfinderColor)) {
            String color = bundle.getString(paramViewfinderColor);
            if (color.length() == 6) {
                try {
                    String red = color.substring(0, 2);
                    String green = color.substring(2, 4);
                    String blue = color.substring(4, 6);
                    float r = ((float) Integer.parseInt(red, 16)) / 256.0f;
                    float g = ((float) Integer.parseInt(green, 16)) / 256.0f;
                    float b = ((float) Integer.parseInt(blue, 16)) / 256.0f;
                    picker.getOverlayView().setViewfinderColor(r, g, b);
                } catch (NumberFormatException e) {
                }
            }
        }
        if (bundleContainsStringKey(bundle, paramViewfinderDecodedColor)) {
            String color = bundle.getString(paramViewfinderDecodedColor);
            if (color.length() == 6) {
                try {
                    String red = color.substring(0, 2);
                    String green = color.substring(2, 4);
                    String blue = color.substring(4, 6);
                    float r = ((float) Integer.parseInt(red, 16)) / 256.0f;
                    float g = ((float) Integer.parseInt(green, 16)) / 256.0f;
                    float b = ((float) Integer.parseInt(blue, 16)) / 256.0f;
                    picker.getOverlayView().setViewfinderDecodedColor(r, g, b);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (bundle.containsKey(paramGuiStyle)) {
            switch (bundle.getInt(paramGuiStyle, -1)) {
                case 0:
                    picker.getOverlayView().setGuiStyle(ScanOverlay.GUI_STYLE_DEFAULT);
                    break;
                case 1:
                    picker.getOverlayView().setGuiStyle(ScanOverlay.GUI_STYLE_LASER);
                    break;
                case 2:
                    picker.getOverlayView().setGuiStyle(ScanOverlay.GUI_STYLE_NONE);
                    break;
                default:
                    Log.e("ScanditSDK", "Failed to parse gui style - wrong type");
                    break;
            }
        }

        if (bundleContainsBundleKey(bundle, paramProperties)) {
            Bundle properties = bundle.getBundle(paramProperties);
            for (String key : properties.keySet()) {
                //picker.getOverlayView().setProperty(key, properties.get);
            }
        }
    }

    public static boolean bundleContainsStringKey(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            if (bundle.getString(key) != null) {
                return true;
            } else {
                Log.e("ScanditSDK", "Failed to parse " + key + " - needs to be string");
            }
        }
        return false;
    }

    public static boolean bundleContainsListKey(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            Serializable serial = bundle.getSerializable(key);
            if (serial != null && serial instanceof List) {
                return true;
            } else {
                Log.e("ScanditSDK", "Failed to parse " + key + " - needs to be array");
            }
        }
        return false;
    }

    public static boolean bundleContainsBundleKey(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            if (bundle.getBundle(key) != null) {
                return true;
            } else {
                Log.e("ScanditSDK", "Failed to parse " + key + " - needs to be bundle");
            }
        }
        return false;
    }

    public static boolean checkClassOfListObjects(List<Object> list, Class<?> aClass) {
        for (Object obj : list) {
            if (!aClass.isInstance(obj)) {
                return false;
            }
        }
        return true;
    }

    // Converts px to dp if String ends with 'px'
    public static Integer getDp(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj.getClass().equals(String.class)) {
            String str = (String) obj;
            if (str.substring(Math.max(str.length() - 2, 0)).equals("px")) {
                try {
                    int px = Integer.parseInt(str.substring(0, str.length() - 2));
                    return (int) ( (int)px * 1f / (ScanditSDK.DISPLAY.densityDpi / 160f));
                } catch (NumberFormatException e) {
                    Log.e("ScanditSDK", "Can not recognize dp value of String " + str + " - returning 0");
                    return 0;
                }
            } else {
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    Log.e("ScanditSDK", "Can not recognize dp value of String " + str + " - returning 0");
                    return 0;
                }
            }
        } else {
            Log.e("ScanditSDK", "Can not recognize dp value - returning 0");
            return 0;
        }
    }
}
