package com.alextern.shortcutexecutors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.alextern.shortcuthelper.ExecutorServiceParams;

import java.util.HashMap;

public class ActionActivity extends Activity implements ExecutorServiceParams {
    static final int kStateConfirmation = 1;
    static final int kStatePermissionDispatch = 2;
    static final int kStatePermissionAsk = 3;
    static final int kStateExecution = 4;
    static final int kStateNotification = 5;
    static final int kStateError = 6;

    //region Activity override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || getIntent().getAction() == null)
            finish();
        else {
            switch (getIntent().getAction()) {
                case kExecuteAction:            curMode = kModeAction; break;
                case kRequestPermissionAction:  curMode = kModeRequestParameters; break;
                default:                        curMode = kModeUnknown;
            }
            if (curMode != kModeUnknown) {
                if (savedInstanceState == null) {
                    switchToState(curMode == kModeAction ? kStateConfirmation : kStatePermissionDispatch, null);
                } else  {
                    int savedState = savedInstanceState.getInt(kStateKey);
                    Intent savedParams = savedInstanceState.getParcelable(kStateParamsKey);
                    switchToState(savedState, savedParams);
                }
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(kStateKey, curStateType);
        if (curState.params != null) {
            outState.putParcelable(kStateParamsKey, curState.params);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == StatePermissionAsk.kRequestPermissionCode && curState instanceof StatePermissionAsk) {
            ((StatePermissionAsk) curState).onPermissionResult(permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (curState != null)
            curState.OnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (curState != null) {
            curState.OnPause();
        }
    }

    //endregion
    //region private implementation
    //region Package private

    static final int kModeUnknown = 0;
    static final int kModeAction = 1;
    static final int kModeRequestParameters = 2;
    int curMode;

    /** States can use two following cache functions for store some local objects and transfer theirs between the states.
     * The object are NOT automatically recreated after activity recreation, so you should restore it manually.
     */
    void keepObjectToCache(String name, Object object) {
        cachedObjects.put(name, object);
    }

    Object getObjectFromCache(String name) {
        return cachedObjects.get(name);
    }

    //endregion

    private static final String kStateKey = "stateType";
    private static final String kStateParamsKey = "stateParams";

    private HashMap<String, Object> cachedObjects = new HashMap<>();
    private StateGeneral curState;
    private int curStateType;

    void switchToState(int stateType, Intent params) {
        switch (stateType) {
            case kStateConfirmation:
                curState = new StateConfirmation();
                break;
            case kStatePermissionDispatch:
                curState = new StatePermissionsDispatch();
                break;
            case kStatePermissionAsk:
                curState = new StatePermissionAsk();
                break;
            case kStateExecution:
                curState = new StateExecution();
                break;
            case kStateNotification:
                curState = new StateNotification();
                break;
            case kStateError:
                curState = new StateError();
                break;
        }
        curStateType = stateType;
        curState.activity = this;
        if (params != null)
            curState.params = params;
        else {
            curState.generateParameters();
        }
        curState.execute();
    }

    //endregion

}
