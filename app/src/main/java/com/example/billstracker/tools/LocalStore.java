package com.example.billstracker.tools;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expenses;
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
    private static final String KEY_NEEDS_DOWNLOAD = "needsDownload";

    private final Gson gson = new Gson();

    private SharedPreferences prefs(Context context, String uid) {
        return context.getSharedPreferences(uid, MODE_PRIVATE);
    }

    // ---------- WRITE ----------

    public void writeAll(Context context, String uid,
                         User user,
                         Bills bills,
                         Payments payments,
                         Expenses expenses) {

        prefs(context, uid).edit()
                .putString(KEY_USER, gson.toJson(user))
                .putString(KEY_BILLS, gson.toJson(bills))
                .putString(KEY_PAYMENTS, gson.toJson(payments))
                .putString(KEY_EXPENSES, gson.toJson(expenses))
                .putBoolean(KEY_DISK_COMPLETE, true)
                .apply();
    }

    // ---------- READ ----------

    public User readUser(Context context, String uid) {
        String json = prefs(context, uid).getString(KEY_USER, null);
        return json == null ? null : gson.fromJson(json, User.class);
    }

    public Bills readBills(Context context, String uid) {
        String json = prefs(context, uid).getString(KEY_BILLS, null);
        Bills bills = json == null ? null : gson.fromJson(json, Bills.class);
        return bills != null ? bills : new Bills(new ArrayList<>());
    }

    public Payments readPayments(Context context, String uid) {
        String json = prefs(context, uid).getString(KEY_PAYMENTS, null);
        Payments payments = json == null ? null : gson.fromJson(json, Payments.class);
        return payments != null ? payments : new Payments(new ArrayList<>());
    }

    public Expenses readExpenses(Context context, String uid) {
        String json = prefs(context, uid).getString(KEY_EXPENSES, null);
        Expenses expenses = json == null ? null : gson.fromJson(json, Expenses.class);
        return expenses != null ? expenses : new Expenses(new ArrayList<>());
    }

    // ---------- FLAGS ----------

    public boolean isDiskComplete(Context context, String uid) {
        return prefs(context, uid).getBoolean(KEY_DISK_COMPLETE, false);
    }

    public void setNeedsDownload(Context context, String uid, boolean value) {
        prefs(context, uid).edit().putBoolean(KEY_NEEDS_DOWNLOAD, value).apply();
    }

    public boolean needsDownload(Context context, String uid) {
        return prefs(context, uid).getBoolean(KEY_NEEDS_DOWNLOAD, false);
    }

    // ---------- CLEAR ----------

    public void clear(Context context, String uid) {
        prefs(context, uid).edit().clear().apply();
    }
}
