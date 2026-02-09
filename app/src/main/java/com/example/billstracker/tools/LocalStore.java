package com.example.billstracker.tools;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;
import com.google.gson.Gson;

import java.util.ArrayList;

public class LocalStore {
    private static final String KEY_USER = "user_json";
    private static final String KEY_BILLS = "bills_json";
    private static final String KEY_PAYMENTS = "payments_json";
    private static final String KEY_EXPENSES = "expenses_json";
    private static final String KEY_DISK_COMPLETE = "disk_data_complete";
    private static final String KEY_NEEDS_DOWNLOAD = "needs_download";
    private static final String KEY_LAST_UID = "last_uid";
    private static final String KEY_UID = "uid";
    private static final String KEY_STAY_SIGNED_IN = "stay_signed_in";
    private static final String KEY_ALLOW_BIOMETRICS = "allow_biometrics";
    private static final String KEY_GLOBAL_PREFS = "Global_Preferences";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_SIGNED_IN_WITH_GOOGLE = "signed_in_with_google";
    private static final String KEY_SECRET_GLOBAL_PREFS = "Secret_Global_Prefs";
    private static final String KEY_CHANNEL_ID = "channel_id";
    private static final String KEY_WORKER_CHANNEL_ID = "worker_channel_id";
    private static final String KEY_PIN = "user_pin";
    private final SharedPreferences globalPrefs;
    private final SharedPreferences encryptedPrefs;
    private final Gson gson;
    private final InMemoryCache cache;
    private final Context context;

    public LocalStore(Context context, InMemoryCache cache) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.cache = cache;
        this.globalPrefs = context.getSharedPreferences(KEY_GLOBAL_PREFS, MODE_PRIVATE);
        SharedPreferences tempEncrypted;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tempEncrypted = EncryptedSharedPreferences.create(
                    KEY_SECRET_GLOBAL_PREFS,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e("LocalStore", "Encryption failed, falling back to standard", e);
            tempEncrypted = globalPrefs;
        }
        this.encryptedPrefs = tempEncrypted;
    }

    private SharedPreferences userPrefs (String uid) {
        return context.getSharedPreferences(uid, MODE_PRIVATE);
    }

    // ---------- WRITE ----------

    public void writeToDisk () {
        String uid = cache.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.e("LocalStore", "Cannot write to disk: UID is null");
            return;
        }

        SharedPreferences.Editor editor = userPrefs(uid).edit();

        if (cache.getThisUser() != null) editor.putString(KEY_USER, gson.toJson(cache.getThisUser()));
        if (cache.getBills() != null) editor.putString(KEY_BILLS, gson.toJson(cache.getBills()));
        if (cache.getPayments() != null) editor.putString(KEY_PAYMENTS, gson.toJson(cache.getPayments()));
        if (cache.getExpenses() != null) editor.putString(KEY_EXPENSES, gson.toJson(cache.getExpenses()));
        editor.putBoolean(KEY_DISK_COMPLETE, true).apply();
    }

    // ---------- READ ----------

    public User getUser(String uid) {
        String json = userPrefs(uid).getString(KEY_USER, null);

        // 1. Check if we actually have saved data
        if (json == null) {
            return new User(); // Return empty user if nothing is saved
        }

        try {
            // 2. Correct the logic to parse the JSON when it exists
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            Log.e("LocalStore", "Error parsing user JSON", e);
            return new User();
        }
    }

    public Bills getBills(String uid) {
        String json = userPrefs(uid).getString(KEY_BILLS, null);
        Bills bills = json == null ? null : gson.fromJson(json, Bills.class);
        return bills != null ? bills : new Bills(new ArrayList<>());
    }

    public Payments getPayments(String uid) {
        String json = userPrefs(uid).getString(KEY_PAYMENTS, null);
        if (json == null) return new Payments(new ArrayList<>());

        try {
            return gson.fromJson(json, Payments.class);
        } catch (Exception e) {
            Log.e("LocalStore", "Payments JSON structure mismatch, resetting data", e);
            return new Payments(new ArrayList<>());
        }
    }

    public Expenses getExpenses(String uid) {
        String json = userPrefs(uid).getString(KEY_EXPENSES, null);
        Expenses expenses = json == null ? null : gson.fromJson(json, Expenses.class);
        return expenses != null ? expenses : new Expenses(new ArrayList<>());
    }

    // ---------- FLAGS ----------

    public void setNeedsDownload (String uid, boolean value) {
        userPrefs(uid).edit().putBoolean(KEY_NEEDS_DOWNLOAD, value).apply();
    }

    public boolean getNeedsDownload (String uid) {
        return userPrefs(uid).getBoolean(KEY_NEEDS_DOWNLOAD, false);
    }

    // ---------- CLEAR ----------

    public void clearUserPrefs (String uid) {
        userPrefs(uid).edit().clear().apply();
    }

    public void clearGlobalPrefs () {
        context.getSharedPreferences(KEY_GLOBAL_PREFS, MODE_PRIVATE).edit().clear().apply();
        globalPrefs.edit().clear().apply();
    }

    public void clearEncryptedPrefs () {
        encryptedPrefs.edit().clear().apply();
    }

    // ---------- SIGN IN / OUT ----------

    // --- STAY SIGNED IN ---

    public boolean getStaySignedIn() {
        return globalPrefs.getBoolean(KEY_STAY_SIGNED_IN, false);
    }

    public void setStaySignedIn(boolean value) {
        globalPrefs.edit().putBoolean(KEY_STAY_SIGNED_IN, value).apply();
        if (!value && !getAllowBiometrics()) {
            encryptedPrefs.edit().remove(KEY_SAVED_EMAIL).remove(KEY_SAVED_PASSWORD).apply();
        }
    }

    public void saveCredentials(String email, String password, boolean isGoogleUser) {
        encryptedPrefs.edit().putString(KEY_SAVED_EMAIL, email).putString(KEY_SAVED_PASSWORD, password).putBoolean(KEY_SIGNED_IN_WITH_GOOGLE, isGoogleUser).apply();
    }

    public String getEmail() {
        return encryptedPrefs.getString(KEY_SAVED_EMAIL, null);
    }

    public String getPassword() {
        return encryptedPrefs.getString(KEY_SAVED_PASSWORD, null);
    }

    public boolean getAllowBiometrics() {
        return globalPrefs.getBoolean(KEY_ALLOW_BIOMETRICS, false);
    }

    public void setAllowBiometrics(boolean value) {
        globalPrefs.edit().putBoolean(KEY_ALLOW_BIOMETRICS, value).apply();
    }

    public String getPin() {
        return globalPrefs.getString(KEY_PIN, null);
    }

    public void setPin(String pin) {
        globalPrefs.edit().putString(KEY_PIN, pin).apply();
    }

    // ----------- UID -----------

    public String getLastUid () {
        return globalPrefs.getString(KEY_LAST_UID, null);
    }

    public void setLastUid(String uid) {
        globalPrefs.edit().putString(KEY_LAST_UID, uid).apply();
    }

    public String getUid() {
        return globalPrefs.getString(KEY_UID, null) != null ? globalPrefs.getString(KEY_UID, null) : cache.getUid();
    }

    public void setUid(String uid) {
        globalPrefs.edit().putString(KEY_UID, uid).apply();
    }

    // --------- GOOGLE ----------

    public boolean getSignedInWithGoogle() {
        return globalPrefs.getBoolean(KEY_SIGNED_IN_WITH_GOOGLE, false);
    }

    public void setSignedInWithGoogle(boolean value) {
        globalPrefs.edit().putBoolean(KEY_SIGNED_IN_WITH_GOOGLE, value).apply();
    }

    public void saveDataForWorker(ArrayList<Payment> paymentsList, String channelId) {
        String uid = getUid() != null ? getUid() : cache.getUid();
        SharedPreferences.Editor editor = userPrefs(uid).edit();

        Payments paymentsWrapper = new Payments(paymentsList);

        editor.putString(KEY_PAYMENTS, gson.toJson(paymentsWrapper));
        editor.putString(KEY_WORKER_CHANNEL_ID, channelId);
        editor.apply();
    }

    public String getSavedChannelId() {
        return globalPrefs.getString(KEY_CHANNEL_ID, null) != null ? globalPrefs.getString(KEY_CHANNEL_ID, null) : cache.getUid();
    }

    public void setSavedChannelId(String channelId) {
        String channel = channelId != null ? channelId : cache.getUid();
        globalPrefs.edit().putString(KEY_CHANNEL_ID, channel).apply();
    }
}
