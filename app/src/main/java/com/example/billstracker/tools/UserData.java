package com.example.billstracker.tools;

import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.expenses;
import static com.example.billstracker.activities.Login.payments;
import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.Login.uid;
import static com.example.billstracker.tools.BillerManager.id;

import android.content.Context;
import android.content.Intent;

import com.example.billstracker.activities.Login;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.Serializable;
import java.util.ArrayList;

public class UserData implements Serializable {

    public static void save () {

        upload();
    }
    public static void load (Context context) {

        if (uid == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                uid = FirebaseAuth.getInstance().getUid();
            }
            else {
                context.startActivity(new Intent(context, Login.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
        if (thisUser == null || thisUser.getUserName() == null || thisUser.getName() == null || thisUser.getBudgets() == null || thisUser.getTrophies() == null || thisUser.getid() == null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    thisUser = task.getResult().toObject(User.class);
                }
                else {
                    context.startActivity(new Intent(context, Login.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });
        }
    }

    public static void upload() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (thisUser.getBills() != null || thisUser.getPhoneNumber() != null || thisUser.getTermsAcceptedOn() != null) {
            thisUser = new User(thisUser.getUserName(), thisUser.getPassword(), thisUser.getName(), thisUser.getAdmin(), thisUser.getLastLogin(), thisUser.getDateRegistered(), thisUser.getid(), thisUser.getTotalLogins(),
                    thisUser.getTicketNumber(), thisUser.getIncome(), thisUser.getPayFrequency(), thisUser.getTrophies(), thisUser.getBudgets(), thisUser.getPartners());
            db.collection("users").document(uid).set(thisUser);
        }
        else {
            if (uid != null) {
                db.collection("users").document(uid).set(thisUser, SetOptions.merge());
            }
        }

        if (uid != null) {
            ArrayList <Payment> save = new ArrayList<>();
            for (Payment payment : payments.getPayments()) {
                if (payment.isPaid() || payment.isDateChanged()) {
                    save.add(payment);
                }
            }
            if (!save.isEmpty()) {
                for (Payment payment : save) {
                    db.collection("users").document(payment.getOwner()).collection("payments").document(String.valueOf(payment.getPaymentId())).set(payment, SetOptions.merge());
                }
            }
        }
        if (bills == null) {
            bills = new Bills(new ArrayList<>());
        }
        if (bills.getBills() == null) {
            bills.setBills(new ArrayList<>());
        }
        if (bills != null && uid != null && bills.getBills() != null) {

            for (Bill bill: bills.getBills()) {
                db.collection("users").document(bill.getOwner()).collection("bills").document(bill.getBillerName()).set(bill, SetOptions.merge());
            }
        }
        if (expenses != null && expenses.getExpenses() != null && !expenses.getExpenses().isEmpty() && uid != null) {
            int id = id();
            for (Expense expense: expenses.getExpenses()) {
                if (expense.getId() == null) {
                    expense.setId(String.valueOf(id));
                    ++id;
                }
                db.collection("users").document(expense.getOwner()).collection("expenses").document(expense.getId()).set(expense, SetOptions.merge());
            }
        }
    }
}
