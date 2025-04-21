package com.example.billstracker.tools;

import static com.example.billstracker.activities.Login.payments;
import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.MainActivity2.channelId;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.billstracker.R;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotificationManager {

    public static String createNotificationChannel (Activity activity) {

        if (thisUser == null) {
            UserData.load(activity);
        }
        if (thisUser != null && thisUser.getid() != null && activity != null) {
            CharSequence name = (activity.getString(R.string.billTracker));
            String description = (activity.getString(R.string.billTrackerNotificationChannel));
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            channelId = thisUser.getid();
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager manager = (android.app.NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
            manager.createNotificationChannel(channel);
            return thisUser.getid();
        }
        else {
            return null;
        }
    }

    public static void scheduleNotifications (Activity activity) {

        if (payments != null && payments.getPayments() != null && !payments.getPayments().isEmpty() && channelId != null) {
            long numDate = DateFormat.currentDateAsLong();
            numDate = DateFormat.makeTimedLong(numDate, 8, 57);
            if (LocalDateTime.now(ZoneId.systemDefault()).getHour() > 7) {
                numDate = DateFormat.makeTimedLong(DateFormat.plusDays(numDate, 1), 8, 0);
            } else {
                numDate = DateFormat.makeTimedLong(numDate, 8, 0);
            }
            Intent intent = new Intent(activity, NotificationReceiver.class);
            intent.putExtra("payments", payments);
            intent.putExtra("channelId", channelId);
            intent.putExtra("notificationDate", numDate);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            intent.putExtra("pendingIntent", pendingIntent);
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, numDate, pendingIntent);
        }
    }
}
