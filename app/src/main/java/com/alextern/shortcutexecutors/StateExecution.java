package com.alextern.shortcutexecutors;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;

class StateExecution extends StateGeneral {
    StateExecution() {
        paramsName = kActionParam;
    }

    @Override
    void execute() {
        setError(R.string.error_msg_incorrectParameters, null);
        if (params != null) {
            boolean success = invokeAction(params);
            if (success) {
                activity.switchToState(ActionActivity.kStateNotification, null);
            } else {
                Intent params = StateError.generateParams(errorMessage, exception);
                activity.switchToState(ActionActivity.kStateError, params);
            }
        } else {
            Intent params = StateError.generateParams(errorMessage, exception);
            activity.switchToState(ActionActivity.kStateError, params);
        }
    }

    private String errorMessage;
    private Exception exception;

    private void setError(int resId, Exception e) {
        exception = e;
        errorMessage = activity.getString(resId);
    }

    private boolean invokeAction(Intent params) {
        int actionCode = params.getIntExtra(kActionParamCode, 0);
        switch (actionCode) {
            case kActionCodeCall:
                return forwardIntent(params);
            case kActionCodeSetRinger:
                return setRingerMode(params);
            case kActionCodeChangeVolume:
                return changeVolume(params);
            case kActionCodeChangeBrightness:
                return changeBrightness(params);
            case kActionCodeChangeAdaptiveBrightness:
                return changeAdaptiveBrightness(params);
            case kActionCodeSetAutoOrientation:
                return changeAutoOrientation(params);
            case kActionCodeSetWifi:
                return setWifiState(params);
            case kActionCodeSetBluetooth:
                return setBluetooth(params);
            case kActionCodeSetMasterSync:
                return setMasterSync(params);
            case kActionCodeHandleMode:
                return handleModeSwitch();
        }
        return false;
    }

    private boolean forwardIntent(Intent params) {
        String intentToLaunch = params.getStringExtra(kActionParamForwardIntent);
        if (intentToLaunch != null) {
            try {
                Intent forwardIntent = Intent.parseUri(intentToLaunch, 0);
                activity.startActivity(forwardIntent);
                return true;
            } catch (Exception e) {
                setError(R.string.error_msg_exception, e);
            }
        }
        return false;
    }

    private boolean setRingerMode(Intent params) {
        int subMode = params.getIntExtra(kActionParamInt, 0);
        if (subMode > 0 && subMode < 5) {
            AudioManager manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (manager != null) {
                int ringerMode = subMode - 1;
                if (subMode == 4)
                    ringerMode = (manager.getRingerMode() + 1) % 3;
                manager.setRingerMode(ringerMode);
                return true;
            } else {
                errorMessage = "Could not obtain Audio manager";
            }
        }

        return false;
    }

    private boolean changeVolume(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        int streamType = params.getIntExtra(kActionParamInt2, AudioManager.STREAM_MUSIC);
        int volume = params.getIntExtra(kActionParamInt3, 0);
        int flags = params.getIntExtra(kActionParamInt4, 0);
        // validate parameters
        if (op > 0 && op < 5 && streamType >= 0 && streamType <= 10 && volume >= 0 && volume <= 100) {
            AudioManager manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (manager != null) {
                switch (op) {
                    case 1:
                        manager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, flags);
                        break;
                    case 2:
                        manager.adjustStreamVolume(streamType, AudioManager.ADJUST_SAME, flags);
                        break;
                    case 3:
                        manager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, flags);
                        break;
                    case 4: {
                        int maxVolume = manager.getStreamMaxVolume(streamType);
                        manager.setStreamVolume(streamType, maxVolume * volume / 100,flags);
                    }
                        break;
                }

                return true;
            } else {
                errorMessage = "Could not obtain Audio manager";
            }
        }
        return false;
    }

    private boolean changeBrightness(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        int percents = params.getIntExtra(kActionParamInt2,0);
        // validate parameters
        if (op > 0 && op < 4 && percents >= 0 && percents <= 100) {
            ContentResolver resolver = activity.getContentResolver();
            try {
                int curBrightnessLevel = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
                int requiredLevel;
                switch(op) {
                    case 1:
                        requiredLevel = Math.min(255, curBrightnessLevel + 25);
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, requiredLevel);
                        break;
                    case 2:
                        requiredLevel = Math.max(0, curBrightnessLevel - 25);
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, requiredLevel);
                        break;
                    case 3:
                        requiredLevel = 255 * percents / 100;
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, requiredLevel);
                        break;
                }
                return true;
            } catch (Settings.SettingNotFoundException e) {
                setError(R.string.error_msg_exception, e);
                return false;
            }
        }

        return false;
    }

    private boolean changeAdaptiveBrightness(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        // validate parameters
        if (op > 0 && op < 4) {
            ContentResolver resolver = activity.getContentResolver();
            try {
                switch(op) {
                    case 1:
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        break;
                    case 2:
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        break;
                    case 3: {
                        int curMode = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
                        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                                curMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        break;
                    }
                }
                return true;
            } catch (Settings.SettingNotFoundException e) {
                setError(R.string.error_msg_exception, e);
                return false;
            }
        }

        return false;
    }

    private boolean setWifiState(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && op > 0 && op < 4) {
            errorMessage = "Wifi manager returns false on operations";
            switch (op) {
                case 1:
                    return wifiManager.setWifiEnabled(true);
                case 2:
                    return wifiManager.setWifiEnabled(false);
                case 3: {
                    boolean enabled = wifiManager.isWifiEnabled();
                    return  wifiManager.setWifiEnabled(!enabled);
                }
            }
        } else if (wifiManager == null) {
            errorMessage = "Could not obtain Wifi manager";
        }
        return false;
    }

    private boolean changeAutoOrientation(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        if (op > 0 && op < 4) {
            ContentResolver resolver = activity.getContentResolver();
            try {
                switch(op) {
                    case 1:
                        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 1);
                        break;
                    case 2:
                        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 0);
                        break;
                    case 3: {
                        int curValue = Settings.System.getInt(resolver, Settings.System.ACCELEROMETER_ROTATION);
                        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, curValue == 0 ? 1 : 0);
                        break;
                    }
                }
                return true;
            } catch (Settings.SettingNotFoundException e) {
                setError(R.string.error_msg_exception, e);
            }
        }
        return false;
    }

    private boolean setBluetooth(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (op > 0 && op < 6) {
                errorMessage = "Bluetooth adapter returns false while execution";
                switch (op) {
                    case 1:
                        return adapter.disable();
                    case 2:
                        return adapter.enable();
                    case 3:
                        activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                        break;
                    case 4:
                        activity.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
                        break;
                    case 5:
                        return adapter.isEnabled() ? adapter.disable() : adapter.enable();
                }
                return true;
            }
        } else {
            errorMessage = "Could not obtain bluetooth adapter";
        }
        return false;
    }

    private boolean setMasterSync(Intent params) {
        int op = params.getIntExtra(kActionParamInt, 0);
        if (op > 0 && op < 4) {
            switch (op) {
                case 1:
                    ContentResolver.setMasterSyncAutomatically(true);
                    break;
                case 2:
                    ContentResolver.setMasterSyncAutomatically(false);
                    break;
                case 3:
                    ContentResolver.setMasterSyncAutomatically(!ContentResolver.getMasterSyncAutomatically());
                    break;
            }
            return true;
        }

        return false;
    }

    private boolean handleModeSwitch() {
        ModelSwitchMode model = getModelSwitchMode();
        if (model != null && model.errorMessage == null) {
            ModelSwitchMode.Mode currentMode = model.getCurrentMode();
            for (Intent action : currentMode.actions) {
                if (!invokeAction(action))
                    return false;
            }

            model.goToNextMode(getActionPreferences());
            return true;
        }
        return false;
    }
}
