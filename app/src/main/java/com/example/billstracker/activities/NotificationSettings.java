package com.example.billstracker.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;

import com.example.billstracker.R;
import com.example.billstracker.tools.Prefs;
import com.example.billstracker.tools.Tools;

public class NotificationSettings extends AppCompatActivity {

    Context mContext;
    LinearLayout notificationInstruction;
    ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mContext = this;
        update();
    }

    private void update() {

        boolean notificationPreference = Prefs.getNotificationPreference(NotificationSettings.this);
        LinearLayout aboutBack = findViewById(R.id.aboutBack);
        aboutBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        ConstraintLayout pb = findViewById(R.id.progressBar10);
        Tools.fixProgressBarLogo(pb);
        pb.setVisibility(View.GONE);
        TextView notificationsOff = findViewById(R.id.notificationsOff), notificationsOn = findViewById(R.id.notificationsOn);
        Button notificationsToggle = findViewById(R.id.notificationsToggle);
        notificationInstruction = findViewById(R.id.notificationInstruction1);
        scroll = findViewById(R.id.scrollView2);

        if (!notificationPreference) {
            notificationsOff.setVisibility(View.VISIBLE);
            notificationsOn.setVisibility(View.GONE);
            notificationsToggle.setText(R.string.NotificationsOn);
            notificationsToggle.setOnClickListener(view -> {
                if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()) {
                    requestPermissionLauncher();
                }
                else {
                    pb.setVisibility(View.VISIBLE);
                    Prefs.setNotificationPreference(NotificationSettings.this, true);
                    update();
                }
            });
        }
        else {
            notificationsOff.setVisibility(View.GONE);
            notificationsOn.setVisibility(View.VISIBLE);
            notificationsToggle.setText(R.string.notificationsOff);
            notificationsToggle.setOnClickListener(view -> {
                pb.setVisibility(View.VISIBLE);
                if (Prefs.getNotificationPreference(NotificationSettings.this)) {
                    Prefs.setNotificationPreference(NotificationSettings.this, false);
                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                }
                else {
                    Prefs.setNotificationPreference(NotificationSettings.this, true);
                }
                update();
            });
        }
    }

    private void requestPermissionLauncher() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                scroll.setVisibility(View.GONE);
                notificationInstruction.setVisibility(View.VISIBLE);
                Button btnNotification = findViewById(R.id.btnNotification1);
                btnNotification.setOnClickListener(view -> {
                    launchAppSettings();
                    notificationInstruction.setVisibility(View.GONE);
                    scroll.setVisibility(View.VISIBLE);
                });
            }

        }
    }

    public void setPreference () {

        Prefs.setNotificationPreference(NotificationSettings.this, NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled());
        update();
    }

    public void launchAppSettings() {

        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        i.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        setPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ConstraintLayout pb = findViewById(R.id.progressBar10);
        pb.setVisibility(View.GONE);
        setPreference();
        update();
    }

}