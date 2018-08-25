package com.alextern.shortcutexecutors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

class StateError extends StateGeneral implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    static Intent generateParams(String message, Exception e) {
        Intent result = new Intent();
        result.putExtra("msg", message);
        result.putExtra("exception", e);
        return result;
    }

    @Override
    void generateParameters() {
        activity.finish();
    }

    @Override
    void execute() {
        if (params != null) {
            String message = params.getStringExtra("msg");
            Exception e = (Exception) params.getSerializableExtra("exception");
            if (e != null) {
                message += "\nException:\n" + e;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.error_dialog_title);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.error_dialog_button, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            builder.setOnCancelListener(this);
            builder.create().show();
        } else {
            activity.finish();
        }

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"alex.tern.homework@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Error in ShortcutExecutors");
            intent.putExtra(Intent.EXTRA_TEXT, getErrorText());

            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.error_dialog_button)));
        }
        activity.finish();
    }

    private String getErrorText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Message:\n").append(params.getStringExtra("msg")).append('\n');
        Exception e = (Exception) params.getSerializableExtra("exception");
        if (e != null) {
            builder.append("Exception:\n").append(e.toString()).append('\n');
            StackTraceElement[] elements = e.getStackTrace();
            for (StackTraceElement aStack : elements) {
                builder.append("at ");
                builder.append(aStack.toString());
                builder.append('\n');
            }
        }
        builder.append("Original Intent:\n").append(activity.getIntent().toUri(0)).append('\n');
        builder.append("Device Info:\n");
        builder.append("  brand:").append(Build.BRAND);
        builder.append("\n  manufacturer:").append(Build.MANUFACTURER);
        builder.append("\n  model:").append(Build.MODEL);
        builder.append("\n  tags:").append(Build.TAGS);
        builder.append("\n  type:").append(Build.TYPE);
        builder.append("\n  soft release:").append(Build.VERSION.RELEASE);
        builder.append("\n  soft version:").append(Build.VERSION.SDK_INT);

        PackageManager man = activity.getPackageManager();
        builder.append("\n  has wifi:").append(man.hasSystemFeature(PackageManager.FEATURE_WIFI));
        builder.append("\n  has bluetooth:").append(man.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH));
        try {
            builder.append("\n  app version:").append(man.getPackageInfo(kServicePackageName, 0).versionName);
            builder.append("\n  creator app version:").append(man.getPackageInfo("com.alextern.shortcuthelper", 0).versionName);
        } catch (PackageManager.NameNotFoundException e1) {
            // nothing
        }
        return builder.toString();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        activity.finish();
    }
}
