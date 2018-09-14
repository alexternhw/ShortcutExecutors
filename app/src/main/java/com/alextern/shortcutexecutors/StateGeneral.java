package com.alextern.shortcutexecutors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.alextern.shortcuthelper.ExecutorServiceParams;

import java.net.URISyntaxException;

abstract class StateGeneral implements ExecutorServiceParams {
    public Intent params;
    public ActionActivity activity;


    /** Lifecycle method, empty in general, can be overridden by sub-classes */
    public void OnResume() {
        //
    }

    /** Lifecycle method, empty in general, can be overridden by sub-classes */
    public void OnPause() {

    }

    void generateParameters() {
        params = getIntentFromUri(activity.getIntent(), paramsName);
    }


    abstract void execute();

    String paramsName;

    Intent getIntentFromUri(Intent parent, String key) {
        if (key != null) {
            String paramString =  parent.getStringExtra(key);
            if (paramString != null) {
                try {
                    return Intent.parseUri(paramString, 0);
                } catch (URISyntaxException e) {
                    // nothing
                }
            }
        }
        return null;
    }

    /** Obtain action preferences which can be used to keep some parameters for particular shortcuts */
    SharedPreferences getActionPreferences() {
        if (activity != null) {
            Intent intent = activity.getIntent();
            String fragment = null;
            if (intent != null) {
                Uri uri = intent.getData();
                if (uri != null)
                    fragment = uri.getEncodedFragment();
            }

            if (fragment != null) {
                return activity.getSharedPreferences(fragment, Context.MODE_PRIVATE);
            }
        }

        return null;
    }

    ModelSwitchMode getModelSwitchMode() {
        Object model = activity.getObjectFromCache(kModelSwitchModeName);
        if (model == null) {
            Intent actionData = getIntentFromUri(activity.getIntent(), kActionParam);
            if (actionData != null) {
                int code = actionData.getIntExtra(kActionParamCode, 0);
                if (code == kActionCodeHandleMode) {
                   ModelSwitchMode switchModel = new ModelSwitchMode(actionData, getActionPreferences());
                   if (switchModel.errorMessage == null) {
                       switchModel.fillTemplateKeys(activity);
                   }
                   model = switchModel;
                   activity.keepObjectToCache(kModelSwitchModeName, model);
                }
            }
        }

        return (ModelSwitchMode) model;
    }

    /**
     * Fill templates by real values, we get values from the activity cache.
     * For example "Switch to {mode}?" will be changed to "Switch to <current mode name>?"
     * @param text Text with templates which should be handled.
     * @return Transformed string.
     */
    String fillTemplates(String text) {
        int startIndex = 0;
        String transformedString = text;
        do {
            startIndex = text.indexOf('{', startIndex);
            if (startIndex != -1) {
                int endIndex = text.indexOf('}', startIndex);
                if (endIndex != -1) {
                    String key = text.substring(startIndex, endIndex + 1);
                    Object value = activity.getObjectFromCache(key);
                    if (value != null && value instanceof String) {
                        transformedString = transformedString.replace(key, (String) value);
                    }
                }
                startIndex++;
            }
        } while (startIndex != -1);

        return transformedString;
    }


    //region Private implementation

    private static final String kModelSwitchModeName = "ModelSwitchMode";

    //endregion
}
