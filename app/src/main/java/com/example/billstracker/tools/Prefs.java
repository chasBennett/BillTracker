package com.example.billstracker.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public interface Prefs {

    static SharedPreferences getPrefs (Activity activity) {
        return activity.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
    }

    static SharedPreferences.Editor getEditor (Activity activity) {
        return getPrefs(activity).edit();
    }
    static boolean isTrainingDone (Activity activity) {
        return getPrefs(activity).getBoolean("Training done", false);
    }
    static void setTrainingDone (Activity activity, boolean value) {
        getEditor(activity).putBoolean("Training done", value).apply();
    }
    static void setNotificationPreference (Activity activity, boolean value) {
        getEditor(activity).putBoolean("NotificationPreference", value).apply();
    }
    static void setSignedInWithGoogle (Activity activity, boolean value) {
        getEditor(activity).putBoolean("SignedInWithGoogle", value).apply();
    }
    static boolean getNotificationPreference (Activity activity) {
        return getPrefs(activity).getBoolean("NotificationPreference", false);
    }
    static void clearPrefs (Activity activity) {
        getEditor(activity).clear().apply();
    }
}
