package com.alextern.shortcutexecutors;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.ArrayList;

class StatePermissionsDispatch extends StateGeneral implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    StatePermissionsDispatch() {
        paramsName = kPermissionParam;
        permissionNames = new StringBuilder();
    }

    @Override
    public void OnResume() {
        super.OnResume();
        if (!checked)
            execute();
    }

    @Override
    public void OnPause() {
        super.OnPause();
        checked = false;
    }

    @Override
    void execute() {
        if (params != null) {
            checked = true;
            permissionNames.setLength(0);
            int requiredPermissions = params.getIntExtra(kPermissionParamFlags, 0);
            boolean askForPermission = params.getBooleanExtra(kPermissionParamDialog, false);
            boolean canExecute = true;
            manifestPermission = null;
            if ((requiredPermissions & kCallPermission) == kCallPermission)
                if (!checkAndAdd(Manifest.permission.CALL_PHONE, R.string.permission_call)) {
                    canExecute = false;
                    showGoToSettingsDialog();
                }
            if (manifestPermission != null) {
                canExecute = false;
                if (askForPermission) {
                    showDialog(kManifestFlow);
                } else
                    goToAskManifestPermissions();
            }

            if (canExecute && (requiredPermissions & kChangeVolumePermission) == kChangeVolumePermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !activity.getSystemService(NotificationManager.class).isNotificationPolicyAccessGranted()) {
                    canExecute = false;
                    permissionNames.append(activity.getString(R.string.permission_volume));
                    if (askForPermission) {
                        showDialog(kVolumeFlow);
                    } else
                        goToAskVolumePermissions();
                }
            }

            if (canExecute && (requiredPermissions & kWriteSettingPermission) == kWriteSettingPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(activity)) {
                    canExecute = false;
                    permissionNames.append(activity.getString(R.string.permission_settings));
                    if (askForPermission) {
                        showDialog(kWriteSettingsFlow);
                    } else {
                        goToAskWriteSettingsPermissions();
                    }
                }
            }

            if (canExecute) {
                if (activity.curMode == ActionActivity.kModeRequestParameters)
                    activity.finish();
                else {
                    activity.switchToState(ActionActivity.kStateExecution, null);
                }
            }
        } else {
            activity.finish();
        }
    }

    private static final int kManifestFlow = 1;
    private static final int kVolumeFlow = 2;
    private static final int kWriteSettingsFlow = 3;
    private static final int kAppSettingsFlow = 4;
    private StringBuilder permissionNames;
    private ArrayList<String> manifestPermission;
    private int flow;
    private boolean checked;

    private boolean checkAndAdd(String permission, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                if (manifestPermission == null)
                    manifestPermission = new ArrayList<>();
                manifestPermission.add(permission);
                if (permissionNames.length() > 0)
                    permissionNames.append(',');
                permissionNames.append(activity.getString(resId));
            } else {
                if (permissionNames.length() > 0)
                    permissionNames.append(',');
                permissionNames.append(activity.getString(resId));
                return false;
            }
        }
        return true;
    }

    private void showDialog(int continueFlow) {
        flow = continueFlow;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.permission_dialog_title);

        SpannableStringBuilder spBuilder = new SpannableStringBuilder(activity.getText(R.string.permission_dialog_message));
        SpannableString add = new SpannableString(permissionNames);
        add.setSpan(new StyleSpan(Typeface.BOLD), 0, add.length(), 0);
        spBuilder.append(add);
        builder.setMessage(spBuilder);

        builder.setNegativeButton(android.R.string.cancel, this);
        builder.setPositiveButton(R.string.permission_dialog_button, this);
        builder.setOnCancelListener(this);

        builder.create().show();
    }

    private void showGoToSettingsDialog() {
        flow = StatePermissionsDispatch.kAppSettingsFlow;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.permission_dialog_title);

        SpannableStringBuilder spBuilder = new SpannableStringBuilder(activity.getText(R.string.permission_dialog_disabledMessage));
        SpannableString add = new SpannableString(permissionNames);
        add.setSpan(new StyleSpan(Typeface.BOLD), 0, add.length(), 0);
        spBuilder.append(add);
        builder.setMessage(spBuilder);

        builder.setNegativeButton(android.R.string.cancel, this);
        builder.setPositiveButton(R.string.permission_dialog_buttonSettings, this);
        builder.setOnCancelListener(this);

        builder.create().show();
    }

    private void goToAskManifestPermissions() {
        Intent params = new Intent();
        params.putExtra(StatePermissionAsk.kPermissionsListParam, manifestPermission);
        params.putExtra(StatePermissionAsk.kPreviousParam, this.params);
        activity.switchToState(ActionActivity.kStatePermissionAsk, params);
    }

    private void goToAskVolumePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
    }

    private void goToAskWriteSettingsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).setData(Uri.parse("package:" + activity.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void goToAskAppSettingsPermissions() {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(i);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_NEGATIVE) {
            activity.finish();
        } else if (button == DialogInterface.BUTTON_POSITIVE) {
            switch (flow) {
                case kManifestFlow:
                    goToAskManifestPermissions();
                    break;
                case kVolumeFlow:
                    goToAskVolumePermissions();
                    break;
                case kWriteSettingsFlow:
                    goToAskWriteSettingsPermissions();
                    break;
                case kAppSettingsFlow:
                    goToAskAppSettingsPermissions();
                    break;
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        activity.finish();
    }
}
