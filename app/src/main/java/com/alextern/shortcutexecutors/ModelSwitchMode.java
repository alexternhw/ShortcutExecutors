package com.alextern.shortcutexecutors;

import android.content.Intent;
import android.content.SharedPreferences;

import com.alextern.shortcuthelper.ExecutorServiceParams;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Class for manage mode switch flow data.
 */
class ModelSwitchMode implements ExecutorServiceParams {
    String errorMessage;        // If not null then there is an error during loading
    Exception errorException;   // Optional exception for clarify the error

    ModelSwitchMode(Intent params, SharedPreferences prefs) {
        if (prefs == null) {
            errorMessage = "Switch mode - shared preferences is null";
            return;
        }

        int code = params.getIntExtra(kActionParamCode, 0);
        if (code != kActionCodeHandleMode) {
            errorMessage = "Switch mode - invalid code in the parameters";
            return;
        }

        int modeCount = params.getIntExtra(kActionParamInt, -1);
        if (modeCount <= 0) {
            errorMessage = "Switch mode - invalid count of modes = " + modeCount;
            return;
        }

        modes = new ArrayList<>(modeCount);
        for (int i = 0; i < modeCount; i++) {
            Intent decodedMode = loadData(params, i);
            if (decodedMode == null)
                return;

            Mode mode = new Mode();
            errorMessage = mode.loadMode(decodedMode);

            if (errorMessage == null) {
                modes.add(mode);
            } else {
                errorMessage += "; mode index = " + i;
                break;
            }
        }

        if (errorMessage == null) {
            curModeIndex = prefs.getInt(kCurModeIndexKey, 0);
        }
    }

    void fillTemplateKeys(ActionActivity activity) {
        Mode curMode = modes.get(curModeIndex);
        Mode prevMode = modes.get(getModeIndex(-1));
        activity.keepObjectToCache(kCurModeKey, curMode.modeName);
        activity.keepObjectToCache(kPrevModeKey, prevMode.modeName);
    }

    Mode getCurrentMode() {
        return modes.get(curModeIndex);
    }

    void goToNextMode(SharedPreferences actionPreferences) {
        int nextMode = getModeIndex(1);
        actionPreferences.edit().putInt(kCurModeIndexKey, nextMode).apply();
    }

    class Mode {
        String modeName;
        ArrayList<Intent> actions;

        String loadMode(Intent params) {
            // no check on params == null, this is done in the constructor
            modeName = params.getStringExtra(kActionParamString);
            if (modeName == null) {
                return "Switch mode - mode name is null";
            }

            int actionCount = params.getIntExtra(kActionParamInt, -1);
            if (actionCount <= 0) {
                return "Switch mode - incorrect count of actions = " + actionCount;
            }

            actions = new ArrayList<>(actionCount);
            for (int i = 0; i< actionCount; i++) {
                Intent actionData = loadData(params, i);
                if (actionData != null) {
                    actions.add(actionData);
                } else {
                    return errorMessage;
                }
            }

            return null;
        }
    }

    private static final String kCurModeIndexKey = "cur_mode_index";
    private ArrayList<Mode> modes;
    private int curModeIndex;

    private Intent loadData(Intent params, int index) {
        String elementName = kActionParamListElement + index;
        String encodedData = params.getStringExtra(elementName);
        if (encodedData == null) {
            errorMessage = "Switch mode - param for element " + index + " not found";
            return null;
        }

        Intent decodedData;
        try {
            decodedData = Intent.parseUri(encodedData, 0);
        } catch (URISyntaxException e) {
            errorMessage = "Switch mode - fail to decode param for element = " + index;
            errorException = e;
            return null;
        }

        return decodedData;
    }

    // Calculate mode index witch is switched on subst from cur mode index
    private int getModeIndex(int subst) {
        return (curModeIndex + modes.size() + subst) % modes.size();
    }
}
