package com.example.billstracker.tools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.billstracker.R;
import com.example.billstracker.activities.Login;
import com.example.billstracker.custom_objects.Payment;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return Result.failure();
            }
        }

        ArrayList<Payment> payments = Repo.getInstance().getSavedPayments(context);
        String channelId = Repo.getInstance().getSavedChannelId(context);
        LocalDate today = LocalDate.from(LocalDate.now().atStartOfDay());

        for (Payment payment : payments) {
            if (payment.isPaid()) {
                continue;
            }

            LocalDate dueDate = LocalDate.from(DateFormat.makeLocalDate(payment.getDueDate()).atStartOfDay());
            int daysRemaining = (int) ChronoUnit.DAYS.between(today, dueDate);

            if (daysRemaining < 4) {
                String title = payment.getBillerName();
                String message = getString(daysRemaining, title);
                triggerNotification(context, title, message, channelId, payment.getPaymentId());
            }
        }
        return Result.success();
    }

    @NonNull
    private static String getString(int daysRemaining, String title) {
        String message;
        if (daysRemaining == -1) {
            message = "Your bill for " + title + " was due yesterday";
        }
        else if (daysRemaining < 0) {
            message = "Your bill for " + title + " was due " + (daysRemaining * -1) + " days ago.";
        }
        else if (daysRemaining == 0) {
            message = "Your bill for " + title + " is due today.";
        }
        else if (daysRemaining == 1) {
            message = "Your bill for " + title + " is due tomorrow.";
        }
        else {
            message = "Your bill for " + title + " is due in " + daysRemaining + " days.";
        }
        return message;
    }

    @SuppressLint("MissingPermission")
    private void triggerNotification(Context context, String title, String msg, String channelId, int id) {
        Intent intent = new Intent(context, Login.class);
        PendingIntent pi = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.new_icon)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(id, builder.build());
    }
}