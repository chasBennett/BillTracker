package com.example.billstracker.tools;

import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;

public class InMemoryCache {

    private User thisUser;
    private Bills bills;
    private Payments payments;
    private Expenses expenses;
    private String uid;

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

    public User getThisUser() {
        return thisUser;
    }

    public void setThisUser(User thisUser) {
        this.thisUser = thisUser;
    }

    public Bills getBills() {
        return bills;
    }

    public void setBills(Bills bills) {
        this.bills = bills;
    }

    public Payments getPayments() {
        return payments;
    }

    public void setPayments(Payments payments) {
        this.payments = payments;
    }

    public Expenses getExpenses() {
        return expenses;
    }

    public void setExpenses(Expenses expenses) {
        this.expenses = expenses;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

