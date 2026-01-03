package com.example.billstracker.tools;

import android.app.NotificationChannel;
import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationManager {

    public static void scheduleNotifications(Context context) {
        NotificationChannel channel = new NotificationChannel(Repo.getInstance().getUid(), "Bills", android.app.NotificationManager.IMPORTANCE_HIGH);
        context.getSystemService(android.app.NotificationManager.class).createNotificationChannel(channel);
        Repo.getInstance().saveDataForWorker(context, Repo.getInstance().getPayments(), Repo.getInstance().getSavedChannelId(context));

        long delay = calculateInitialDelay();

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("bill_notif_tag")
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyBillCheck",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

    private static long calculateInitialDelay() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return calendar.getTimeInMillis() - now;
    }
}