package com.alextern.shortcutexecutors;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

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
            message = fillTemplates(message);

            switch (type) {
                case kNotificationTypeToast:
                    showToastMessage(message);
                    activity.finish();
                    break;
                case kNotificationTypeDialog:
                    showDialog(message);
                    break;
                case kNotificationTypeStatusNoti:
                    createNotification(message);
                    activity.finish();
                    break;
            }
        }
    }

    private static final String kChannelId = "action";

    private void showToastMessage(String message) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.notify_toast, null);

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void createNotification(String message) {
        createNotificationChannel();
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            Notification.Builder builder = new Notification.Builder(activity);
            builder.setSmallIcon(R.mipmap.ic_notification);
            builder.setWhen(System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(kChannelId);
            }
            builder.setContentTitle(activity.getString(R.string.notification_title));
            builder.setContentText(message);
            notificationManager.notify(UUID.randomUUID().toString(), 0, builder.getNotification());
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = activity.getString(R.string.channel_name);
            String description = activity.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(kChannelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.mipmap.ic_notification);
        builder.setTitle(R.string.notification_title);
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
