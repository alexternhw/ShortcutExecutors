package com.alextern.shortcutexecutors;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;

import com.alextern.shortcuthelper.ExecutorServiceParams;

import java.util.HashSet;

public class MainService extends Service implements ExecutorServiceParams {
    @Override
    public IBinder onBind(Intent intent) {
        return incomingMessenger.getBinder();
    }

    public int getPermissions() {
        int result = 0;
        if (getPackageManager().checkPermission(Manifest.permission.CALL_PHONE, kServicePackageName) == PackageManager.PERMISSION_GRANTED)
            result |= kCallPermission;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this)) {
            result |= kWriteSettingPermission;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || getSystemService(NotificationManager.class).isNotificationPolicyAccessGranted()) {
            result |= kChangeVolumePermission;
        }
        return result;
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case kServiceGetPermissionsFunc:
                    returnIntResult(msg, getPermissions());
                    break;
                case kServiceGetAvailActions:
                    returnAvailableActions(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void returnIntResult(Message msg, int result) {
        Messenger messenger = msg.replyTo;
        if (messenger != null) {
            Message reply = Message.obtain();
            reply.what = msg.what;
            reply.arg1 = result;
            try {
                messenger.send(reply);
            } catch (RemoteException e) {
                // nothing
            }
        }
    }

    private void returnAvailableActions(Message msg) {
        Messenger replyMessenger = msg.replyTo;
        if (replyMessenger != null) {
            HashSet<Integer> result = new HashSet<>();
            // Check call ability
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:5551231234"));
            if (getPackageManager().queryIntentActivities(callIntent, 0).size() > 0) {
                result.add(kActionCodeCall);
            }
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !audioManager.isVolumeFixed())) {
                result.add(kActionCodeSetRinger);
                result.add(kActionCodeChangeVolume);
            }
            result.add(kActionCodeChangeBrightness);
            result.add(kActionCodeChangeAdaptiveBrightness);
            result.add(kActionCodeSetAutoOrientation);
            if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                result.add(kActionCodeSetWifi);
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                result.add(kActionCodeSetBluetooth);
            }

            Message reply = Message.obtain();
            reply.what = msg.what;
            Bundle data = new Bundle();
            data.putSerializable(kMethodsKey, result);
            reply.setData(data);
            try {
                replyMessenger.send(reply);
            } catch (RemoteException e) {
                // nothing
            }
        }
    }

    final private Messenger incomingMessenger = new Messenger(new IncomingHandler());
}
