package com.alextern.shortcutexecutors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

class StateConfirmation extends StateGeneral implements  DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    StateConfirmation() {
        paramsName = kConfirmationParam;
    }

    @Override
    void execute() {
        ModelSwitchMode model = getModelSwitchMode();

        if (model != null && model.errorMessage != null) {
            // check on errors while handle model switch in confirmation state, other states avoid this
            Intent params = StateError.generateParams(model.errorMessage, model.errorException);
            activity.switchToState(ActionActivity.kStateError, params);
        } else if (params == null) {
            // No confirmation - get action and permission and go to permission dialog
            goToPermissionState();
        } else {
            // show confirmation
            int type = params.getIntExtra(kConfirmationTypeParam, kConfirmationTypeDialog);
            if (type == kConfirmationTypeDialog) {
                showDialog();
            }
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.mipmap.ic_notification);
        String label = params.getStringExtra(kConfirmationParamTitle);
        if (label != null)
            builder.setTitle(fillTemplates(label));
        label = params.getStringExtra(kConfirmationParamMessage);
        if (label != null)
            builder.setMessage(fillTemplates(label));
        label = params.getStringExtra(kConfirmationParamPositiveButton);
        if (label == null)
            label = activity.getString(android.R.string.ok);
        builder.setPositiveButton(label, this);
        label = params.getStringExtra(kConfirmationParamNegativeButton);
        if (label == null)
            label = activity.getString(android.R.string.cancel);
        builder.setNegativeButton(label, this);
        builder.setOnCancelListener(this);
        builder.create().show();
    }

    private void goToPermissionState() {
        Intent actionParams = getIntentFromUri(activity.getIntent(), kActionParam);
        if (actionParams != null) {
            int actionCode = actionParams.getIntExtra(kActionParamCode, 0);
            int permissions = getPermissionsForAction(actionCode);
            Intent params = new Intent();
            params.putExtra(kPermissionParamFlags, permissions);
            params.putExtra(kPermissionParamDialog, true);
            activity.switchToState(ActionActivity.kStatePermissionDispatch, params);
        } else {
            activity.finish();
        }
    }

    private int getPermissionsForAction(int actionCode) {
        if (actionCode < 10)
            return kCallPermission;
        else if (actionCode < 20)
            return kChangeVolumePermission;
        else if (actionCode < 30)
            return kWriteSettingPermission;
        else if (actionCode == kActionCodeHandleMode) {
            // we should obtain permissions for all actions in the current mode
            ModelSwitchMode model = getModelSwitchMode();
            if (model != null && model.errorMessage == null) {
                ModelSwitchMode.Mode curMode = model.getCurrentMode();
                int resultPermissions = 0;
                for (Intent action : curMode.actions) {
                    int code = action.getIntExtra(kActionParamCode, 0);
                    resultPermissions |= getPermissionsForAction(code);
                }
                return resultPermissions;
            }
        }
        return 0;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            goToPermissionState();
        } else if (button == DialogInterface.BUTTON_NEGATIVE) {
            activity.finish();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        activity.finish();
    }
}
