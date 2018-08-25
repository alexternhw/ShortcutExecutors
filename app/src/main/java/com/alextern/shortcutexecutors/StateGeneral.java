package com.alextern.shortcutexecutors;

import android.content.Intent;

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

    protected Intent getIntentFromUri(Intent parent, String key) {
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
}
