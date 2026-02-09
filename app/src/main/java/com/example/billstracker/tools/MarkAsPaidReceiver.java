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

        Repository repo = Repository.getInstance(context);
        repo.loadLocalData((success, message) -> {
            if (success) {
                processPayment(context, repo, paymentId);
            }
        });
        NotificationManagerCompat.from(context).cancel(paymentId);
    }

    private void processPayment(Context context, Repository repo, int paymentId) {
        Payment targetPayment = repo.getPayment(paymentId);
        for (Payment p : repo.getPayments()) {
            if (p.getPaymentId() == paymentId) {
                targetPayment = p;
                break;
            }
        }

        if (targetPayment != null && !targetPayment.isPaid()) {
            targetPayment.setPaid(true);
            targetPayment.setDatePaid(DateFormat.currentDateAsLong());
            targetPayment.setNeedsSync(true);

            Bill bill = repo.getBill(targetPayment.getBillerName());
            if (bill != null) {
                bill.setBalance(bill.getBalance() - targetPayment.getPaymentAmount());
                bill.setNeedsSync(true);
                bill.setPaymentsRemaining(bill.getPaymentsRemaining() - 1);
                targetPayment.setPartialPayment(0);
            }

            // 3. Save everything to Cloud and Local Storage
            FirebaseTools.saveData(context, (wasSuccessful, msg) -> {
                if (wasSuccessful) {
                    Log.d(TAG, "Payment marked as paid via notification successfully.");
                } else {
                    Log.e(TAG, "Failed to sync payment update: " + msg);
                }
            });
        }
    }
}