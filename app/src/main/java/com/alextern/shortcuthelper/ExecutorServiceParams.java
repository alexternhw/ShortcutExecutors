package com.alextern.shortcuthelper;

public interface ExecutorServiceParams {
    // Version 1.0
    String kServicePackageName = "com.alextern.shortcutexecutors";

    String kConnectAction = "192f2516-84ae-4928-950a-4b0b5a5e7439";
    int kServiceGetPermissionsFunc = 1;
    int kServiceGetAvailActions = 2;
    String kMethodsKey = "methods";

    // permissions flags
    int kCallPermission = 1;
    int kChangeVolumePermission = 2;
    int kWriteSettingPermission = 4;

    // Request permissions action
    String kRequestPermissionAction = "shortcuthelper.intent.action.REQUEST_PERMISSIONS";
    String kPermissionParam = "ex_perm";    // required permissions parameters in uri string form : string
    String kPermissionParamFlags = "f";     // permissions flags : long
    String kPermissionParamDialog = "d";    // true if need to show the information dialog that permissions is required : boolean

    //Main action for execute something
    String kExecuteAction = "shortcuthelper.intent.action.EXECUTE";

    // Confirmation parameters
    String kConfirmationParam = "ex_con";   // intent with confirmation parameters in uri string form : String
    String kConfirmationTypeParam = "type"; // type of the confirmation : int, currently only 1 (dialog) is possible
    int kConfirmationTypeDialog = 1;
    String kConfirmationParamTitle = "t";   // title : String
    String kConfirmationParamMessage = "m";  // Message : String
    String kConfirmationParamPositiveButton = "pb"; // Positive button : String
    String kConfirmationParamNegativeButton = "nb"; // Negative button : String

    // Notification parameters
    String kNotificationParam = "ex_not";   // intent with notification parameters in uri string form.
    String kNotificationParamType = "type"; // type of the notification : int, 0 - toast, 1 - dialog, 2 - status bar notification
    int kNotificationTypeToast = 0;
    int kNotificationTypeDialog = 1;
    int kNotificationTypeStatusNoti = 2;
    String kNotificationParamMessage = "m"; // Message : String

    // Action parameters
    String kActionParam = "ex_act";         // intent with action parameters in uri string form : String
    String kActionParamCode = "c";          // code of the action : int

    /** Originate call, requires intent with Intent.ACTION_CALL in kActionParamForwardIntent field */
    int kActionCodeCall = 1;
    String kActionParamForwardIntent = "fwi"; // intent in uri form : String

    /** Change ringer mode, require kActionParamInt field as sub-code: 1 - set silent, 2 - set vibrate, 3 - set normal, 4 - switch */
    int kActionCodeSetRinger = 10;
    String kActionParamInt = "i";             // int sub-code for the operation
    String kActionParamInt2 = "i2";
    String kActionParamInt3 = "i3";
    String kActionParamInt4 = "i4";
    String kActionParamString = "s";          // string sub-code for the operation.

    /** Change stream volume, kActionParamInt - sub code: 1 - adjust lower, 2 - adjust same, 3 - adjust higher,
         4 set particular volume (kActionParamInt3 for volume level in percent).
         Use kActionParamInt2 for required stream type, by default this is equal to 3 - music steam
         Use kActionParamInt4 for flags like AudioManager.FLAG_SHOW_UI, by default 0*/
    int kActionCodeChangeVolume = 11;

    /** Change brightness, kActionParamInt sub code: 1 - increase 10% up, 2 decrease 10% down, 3 - set to min, 4 - set to max,
     *  5 - set to some value (kActionInt2 from 0 to 100 in percent), 6 - enable automatic mode, 7 - disable automatic mode, 8 - toggle automatic mode. */
    int kActionCodeChangeBrightness = 20;

    /** Change Auto orientation, kActionParamInt sub code: 1 - enable, 2 - disable, 3 - toggle. */
    int kActionCodeSetAutoOrientation = 21;

    /** Change WiFi state, kActionParamInt sub code: 1 - enable, 2 - disable, 3 - toggle */
    int kActionCodeSetWifi = 1000;
    /** Work with bluetooth, kActionParamInt sub code: 1 - disable, 2 - simple enable, 3 - enable via dialog, 4 - enable with discoverable dialog, 5 - toggle  */
    int kActionCodeSetBluetooth = 1001;

    /** Special action for handle mode lists
     *  kActionParamInt is used for provide count of the modes, then we can use kActionParamListElement + <element index> for obtain
     *  intent with particular mode parameters in uri string form.
     *  Each mode parameters contains name in kActionParamString and count of actions in kActionParamInt, we can use
     *  kActionParamListElement + <element index> for obtain intent with particular action parameters */
    int kActionCodeHandleMode = 2000;
    String kActionParamListElement = "l";     // Used as partial name for elements of the lists

    String kCurModeKey = "{mode}";
    String kPrevModeKey = "{prev_mode}";
}
