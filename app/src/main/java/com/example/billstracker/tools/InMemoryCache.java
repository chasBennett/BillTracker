package com.example.billstracker.tools;

import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;

public class InMemoryCache {

    public User thisUser;
    public Bills bills;
    public Payments payments;
    public Expenses expenses;
    public String uid;

    public boolean isLoaded() {
        return thisUser != null && bills != null && payments != null && expenses != null;
    }

    public void clear() {
        thisUser = null;
        bills = null;
        payments = null;
        expenses = null;
        uid = null;
    }
}

