package com.alextern.shortcutexecutors;

class StateNotification extends StateGeneral  {
    StateNotification() {
        paramsName = kNotificationParam;
    }

    @Override
    void execute() {
        if (params == null)
            activity.finish();
    }
}
