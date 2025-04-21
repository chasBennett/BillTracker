package com.example.billstracker.tools;

import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.os.BundleCompat;

import com.example.billstracker.R;
import com.example.billstracker.activities.Login;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.Payment;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Context mContext = context.getApplicationContext();
        Bundle extras = intent.getExtras();
        Payments list = new Payments(new ArrayList<>());
        String channelId = null;
        long currentDat = 0;
        if (extras != null && !extras.isEmpty()) {
            channelId = extras.getString("channelId");
            if (BundleCompat.getSerializable(extras, "payments", Payments.class) != null) {
                list = BundleCompat.getSerializable(extras, "payments", Payments.class);
            }
            currentDat = extras.getLong("notificationDate");
        }
        String title = mContext.getString(R.string.billDue);
        LocalDate currentDate = null;
        if (currentDat != 0) {
            currentDate = DateFormat.makeLocalDate(currentDat);
        }
        int code;
        String message = "";
        NotificationManagerCompat manager = NotificationManagerCompat.from(mContext);
        manager.cancelAll();

        if (list != null && list.getPayments() != null && !list.getPayments().isEmpty() && channelId != null && currentDate != null) {
            for (Payment payment: list.getPayments()) {
                if (!payment.isPaid()) {
                    LocalDate dueDate = DateFormat.makeLocalDate(payment.getDueDate());
                    code = 1;
                    if (dueDate != null) {
                        if (dueDate.isAfter(currentDate)) {
                            if (ChronoUnit.DAYS.between(currentDate, dueDate) == 3) {
                                message = mContext.getString(R.string.yourBillFor) + " " + FixNumber.addSymbol(payment.getPaymentAmount()) + " " + mContext.getString(R.string.at) + " " + payment.getBillerName() + " " +
                                        mContext.getString(R.string.isDueInThreeDays);
                                code = payment.getPaymentId();
                            } else if (ChronoUnit.DAYS.between(currentDate, dueDate) == 2) {
                                message = mContext.getString(R.string.yourBillFor) + " " + FixNumber.addSymbol(payment.getPaymentAmount()) + " " + mContext.getString(R.string.at) + " " + payment.getBillerName() + " " +
                                        mContext.getString(R.string.isDueInTwoDays);
                                code = payment.getPaymentId();
                            } else if (ChronoUnit.DAYS.between(currentDate, dueDate) == 1) {
                                message = mContext.getString(R.string.yourBillFor) + " " + FixNumber.addSymbol(payment.getPaymentAmount()) + " " + mContext.getString(R.string.at) + " " + payment.getBillerName() + " " +
                                        mContext.getString(R.string.isDueTomorrow);
                                code = payment.getPaymentId();
                            }
                        } else if (currentDate == dueDate) {
                            message = mContext.getString(R.string.yourBillFor) + " " + FixNumber.addSymbol(payment.getPaymentAmount()) + " " + mContext.getString(R.string.at) + " " + payment.getBillerName() + " " +
                                    mContext.getString(R.string.isDueToday);
                            code = payment.getPaymentId();
                        } else if (dueDate.isBefore(currentDate)) {
                            if (ChronoUnit.DAYS.between(dueDate, currentDate) == 1) {
                                message = mContext.getString(R.string.yourBillFor) + " " + FixNumber.addSymbol(payment.getPaymentAmount()) + " " + mContext.getString(R.string.at) + " " + payment.getBillerName() + " " +
                                        mContext.getString(R.string.wasDue) + " " + mContext.getString(R.string.yesterday);
                                code = payment.getPaymentId();
                            } else if (ChronoUnit.DAYS.between(dueDate, currentDate) >= 2) {
                                long days = ChronoUnit.DAYS.between(dueDate, currentDate);
                                message = mContext.getString(R.string.yourBillFor) + " " + FixNumber.addSymbol(payment.getPaymentAmount()) + " " + mContext.getString(R.string.at) + " " + payment.getBillerName() + " " +
                                        mContext.getString(R.string.wasDue) + " " + days + " " + mContext.getString(R.string.daysAgo);
                                code = payment.getPaymentId();
                            }
                        }
                    }

                    NotificationCompat.Builder notification = null;
                    if (code != 1) {
                        Intent intent1 = new Intent(mContext, Login.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pi = PendingIntent.getActivity(mContext, code, intent1, PendingIntent.FLAG_IMMUTABLE);
                        notification = new NotificationCompat.Builder(mContext, channelId)
                                .setSmallIcon(R.drawable.new_icon)
                                .setColor(context.getColor(R.color.primary))
                                .setContentTitle(title)
                                .setContentText(message)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pi)
                                .setAutoCancel(true);
                    }
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (notification != null) {
                        manager.notify(code, notification.build());
                    }
                }
            }
            long today = DateFormat.makeTimedLong(DateFormat.plusDays(DateFormat.makeLong(currentDate), 1), 8, 0);
            Intent intent1 = new Intent();
            intent1.putExtra("payments", list);
            intent1.putExtra("channelId", channelId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            intent1.putExtra("pendingIntent", pendingIntent);
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, today, pendingIntent);
        }
    }
}
