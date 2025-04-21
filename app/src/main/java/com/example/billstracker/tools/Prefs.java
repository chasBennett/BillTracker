package com.example.billstracker.tools;

import static com.example.billstracker.activities.Login.thisUser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.billstracker.custom_objects.Biller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public interface Prefs {

    static boolean getStaySignedIn (Activity activity) {
        return getPrefs(activity).getBoolean("Stay Signed In", false);
    }
    static SharedPreferences getPrefs (Activity activity) {
        return activity.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
    }
    static void saveBillers (Activity activity, ArrayList<Biller> billers) {

        Set<Biller> billerSet = new LinkedHashSet<>(billers);
        SharedPreferences prefs = getPrefs(activity);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(billerSet);
        editor.putString("billers", json);
        editor.apply();
    }
    static ArrayList <Biller> getBillers (Activity activity) {
        SharedPreferences prefs = getPrefs(activity);
        Gson gson = new Gson();
        String json = prefs.getString("billers", null);
        Type type = new TypeToken<ArrayList<Biller>>() {}.getType();
        return gson.fromJson(json, type);
    }
    static void setSignedIn (Activity activity, boolean value) {
        getEditor(activity).putBoolean("Stay Signed In", value).apply();
        if (thisUser != null && thisUser.getUserName() != null && thisUser.getPassword() != null) {
            setUserName(activity, thisUser.getUserName());
            setPassword(activity, thisUser.getPassword());
        }
    }
    static SharedPreferences.Editor getEditor (Activity activity) {
        return getPrefs(activity).edit();
    }
    static String getUserUid (Activity activity) {
        return getPrefs(activity).getString("userUid", null);
    }
    static void setUserUid (Activity activity, String uid) {
        getEditor(activity).putString("userUid", uid).apply();
    }
    static String getUserName (Activity activity) {
        return getPrefs(activity).getString("UserName", null);
    }
    static boolean isTrainingDone (Activity activity) {
        return getPrefs(activity).getBoolean("Training done", false);
    }
    static void setTrainingDone (Activity activity, boolean value) {
        getEditor(activity).putBoolean("Training done", value).apply();
    }
    static void setUserName (Activity activity, String string) {
        getEditor(activity).putString("UserName", string).apply();
    }
    static String getPassword (Activity activity) {
        return getPrefs(activity).getString("Password", null);
    }
    static void setPassword (Activity activity, String string) {
        getEditor(activity).putString("Password", string).apply();
    }
    static boolean getBiometricPreferences (Activity activity) {
        return getPrefs(activity).getBoolean("biometricPreference", false);
    }
    static void setBiometricPreferences (Activity activity, boolean value) {
        getEditor(activity).putBoolean("biometricPreference", value).apply();
    }
    static boolean getAllowBiometrics (Activity activity) {
        return getPrefs(activity).getBoolean("allowBiometrics", false);
    }
    static void setAllowBiometrics (Activity activity, boolean value) {
        getEditor(activity).putBoolean("allowBiometrics", value).apply();
    }
    static void setNotificationPreference (Activity activity, boolean value) {
        getEditor(activity).putBoolean("NotificationPreference", value).apply();
    }
    static void setSignedInWithGoogle (Activity activity, boolean value) {
        getEditor(activity).putBoolean("SignedInWithGoogle", value).apply();
    }
    static boolean getSignedInWithGoogle (Activity activity) {
        return getPrefs(activity).getBoolean("SignedInWithGoogle", false);
    }
    static void setGoogleIdToken (Activity activity, String value) {
        getEditor(activity).putString("GoogleIdToken", value).apply();
    }
    static String getGoogleIdToken (Activity activity) {
        return getPrefs(activity).getString("GoogleIdToken", null);
    }
    static boolean getNotificationPreference (Activity activity) {
        return getPrefs(activity).getBoolean("NotificationPreference", false);
    }
    static void clearPrefs (Activity activity) {
        getEditor(activity).clear().apply();
    }
}
