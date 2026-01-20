package com.example.billstracker.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;

public class MarkAsPaidReceiver extends BroadcastReceiver {

    private static final String TAG = "MarkAsPaidReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int paymentId = intent.getIntExtra("paymentId", -1);

        if (paymentId == -1) {
            Log.e(TAG, "No paymentId found in intent");
            return;
        }

        Repository repo = Repository.getInstance();

        // 1. Ensure data is loaded into memory first
        repo.loadLocalData(context, (success, message) -> {
            if (success) {
                processPayment(context, repo, paymentId);
            }
        });

        // 2. Dismiss the notification immediately
        NotificationManagerCompat.from(context).cancel(paymentId);
    }

    private void processPayment(Context context, Repository repo, int paymentId) {
        // Find the specific payment
        Payment targetPayment = null;
        for (Payment p : repo.getPayments()) {
            if (p.getPaymentId() == paymentId) {
                targetPayment = p;
                break;
            }
        }

        if (targetPayment != null && !targetPayment.isPaid()) {
            // Update Payment Status
            targetPayment.setPaid(true);
            targetPayment.setNeedsSync(true);

            // Update associated Bill
            for (Bill bill : repo.getBills()) {
                if (bill.getBillerName().equals(targetPayment.getBillerName())) {
                    bill.setPaymentsRemaining(bill.getPaymentsRemaining() - 1);
                    double amountToSubtract = targetPayment.getPaymentAmount() - targetPayment.getPartialPayment();
                    bill.setBalance(bill.getBalance() - amountToSubtract);

                    targetPayment.setPartialPayment(0);
                    bill.setNeedsSync(true);
                    break;
                }
            }

            // 3. Save everything to Cloud and Local Storage
            repo.saveData(context, (wasSuccessful, msg) -> {
                if (wasSuccessful) {
                    Log.d(TAG, "Payment marked as paid via notification successfully.");
                } else {
                    Log.e(TAG, "Failed to sync payment update: " + msg);
                }
            });
        }
    }
}