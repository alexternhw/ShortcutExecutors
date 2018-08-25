package com.alextern.shortcutexecutors;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;

class StatePermissionAsk extends StateGeneral {
    static final String kPermissionsListParam = "perm_list";
    static final String kPreviousParam = "previous";
    static final int kRequestPermissionCode = 100;

    @Override
    void generateParameters() {
        // for this state we should always provides the parameters
        activity.finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    void execute() {
        if (params != null) {
            ArrayList<String> permissions = params.getStringArrayListExtra(kPermissionsListParam);
            activity.requestPermissions(permissions.toArray(new String[]{}), kRequestPermissionCode);
        } else {
            activity.finish();
        }
    }

    public void onPermissionResult(String[] permissions, int[] grantResults) {
        if (permissions.length == 0)
            return;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                activity.finish();
                return;
            }
        }

        Intent prevParam = params.getParcelableExtra(kPreviousParam);
        activity.switchToState(ActionActivity.kStatePermissionDispatch, prevParam);
    }
}
