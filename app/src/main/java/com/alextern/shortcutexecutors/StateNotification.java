package com.alextern.shortcutexecutors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

class StateNotification extends StateGeneral implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {
    StateNotification() {
        paramsName = kNotificationParam;
    }

    @Override
    void execute() {
        if (params == null)
            activity.finish();
        else {
            int type = params.getIntExtra(kNotificationParamType, kNotificationTypeToast);
            String message = params.getStringExtra(kNotificationParamMessage);
            switch (type) {
                case kNotificationTypeToast:
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    activity.finish();
                    break;
                case kNotificationTypeDialog:
                    showDialog(message);
                    break;
                case kNotificationTypeStatusNoti:
                    // TODO:
                    break;
            }
        }
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        String label = activity.getString(android.R.string.ok);
        builder.setPositiveButton(label, this);
        builder.setOnCancelListener(this);
        builder.create().show();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        activity.finish();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        activity.finish();
    }
}
