package com.example.billstracker.activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.Google;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;

public class Settings extends BaseActivity {

    final Context mContext = this;
    LinearLayout back;
    ConstraintLayout pb;
    User thisUser;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_settings);

        back = findViewById(R.id.backButton);
        pb = findViewById(R.id.progress2);
        back.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            getOnBackPressedDispatcher().onBackPressed();
            pb.setVisibility(View.GONE);
        });
        LinearLayout edit = findViewById(R.id.llUserProfileEdit);
        LinearLayout delete = findViewById(R.id.llDeleteAccount);
        LinearLayout about = findViewById(R.id.llAbout);
        LinearLayout logout = findViewById(R.id.llLogout);
        LinearLayout shareAccount = findViewById(R.id.llShareAccount);
        TextView uName = findViewById(R.id.replaceWithUsername);

        thisUser = repo.getUser(Settings.this);
        uName.setText(thisUser.getName());

        Tools.fixProgressBarLogo(pb);

        edit.setOnClickListener(this::profileEdit);

        delete.setOnClickListener(this::deleteUser);
        about.setOnClickListener(view -> {
            Intent getAbout = new Intent(mContext, NotificationSettings.class);
            startActivity(getAbout);
        });
        shareAccount.setOnClickListener(v -> startActivity(new Intent(Settings.this, ShareAccount.class)));
        logout.setOnClickListener(view -> {
            CustomDialog cd = new CustomDialog(Settings.this, getString(R.string.logout), getString(R.string.are_you_sure_you_would_like_to_logout), getString(R.string.logout), getString(R.string.cancel), null);
            cd.setPositiveButtonListener(v -> {
                cd.dismissDialog();
                pb.setVisibility(View.VISIBLE);
                repo.logout(Settings.this);
            });
        });
    }

    public void profileEdit(View view) {
        pb.setVisibility(View.VISIBLE);
        Intent edit = new Intent(mContext, EditProfile.class);
        edit.putExtra("Name", thisUser.getName());
        edit.putExtra("UserName", thisUser.getUserName());
        startActivity(edit);
        pb.setVisibility(View.GONE);
    }

    public void deleteUser(View view) {
        if (thisUser.getRegisteredWithGoogle()) {
            CustomDialog cd = new CustomDialog(Settings.this, "Delete Account",
                    "Please re-authenticate in order to delete your account.",
                    "Re-authenticate", "Cancel", null);

            cd.setPositiveButtonListener(v -> {
                cd.dismissDialog();
                pb.setVisibility(View.VISIBLE);
                Google.launchGoogleSignIn(Settings.this, (wasSuccessful, user, idToken) -> {
                    if (wasSuccessful && user != null) {
                        if (idToken != null) {
                            AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
                            executeDeletion(cred);
                        } else {
                            pb.setVisibility(View.GONE);
                            Notify.createPopup(Settings.this, "Google authentication failed.", null);
                        }
                    }
                });
            });
        } else {
            CustomDialog cd = new CustomDialog(Settings.this, "Delete Account",
                    "Enter password to confirm deletion.", "Confirm", "Cancel", null);
            cd.setEditText("Password", null, ResourcesCompat.getDrawable(getResources(), R.drawable.padlock, getTheme()));

            cd.setPositiveButtonListener(v -> {
                String pwd = cd.getEditText().getText().toString();
                if (!pwd.isEmpty()) {
                    cd.dismissDialog();
                    pb.setVisibility(View.VISIBLE);
                    AuthCredential cred = EmailAuthProvider.getCredential(thisUser.getUserName(), pwd);
                    executeDeletion(cred);
                }
            });
        }
    }

    private void executeDeletion(AuthCredential credential) {
        repo.deleteUserAccount(Settings.this, credential, (success, msg) -> {
            pb.setVisibility(View.GONE);
            if (success) {
                Intent intent = new Intent(mContext, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("Deleted", true);
                startActivity(intent);
                finish();
            } else {
                Notify.createPopup(Settings.this, msg, null);
            }
        });
    }

}