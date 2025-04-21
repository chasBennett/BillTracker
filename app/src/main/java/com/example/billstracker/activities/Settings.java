package com.example.billstracker.activities;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.payments;
import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.Login.uid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.example.billstracker.R;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.Prefs;
import com.example.billstracker.tools.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;

public class Settings extends AppCompatActivity {

    final Context mContext = this;
    LinearLayout back;
    ConstraintLayout pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                Tools.signOut(Settings.this);
            });
        });
    }

    public void profileEdit(View view){
        pb.setVisibility(View.VISIBLE);
        Intent edit = new Intent(mContext, EditProfile.class);
        edit.putExtra("Name", thisUser.getName());
        edit.putExtra("UserName", thisUser.getUserName());
        startActivity(edit);
        pb.setVisibility(View.GONE);
    }

    public void deleteUser(View view) {

        if (!thisUser.getPassword().equals(uid)) {
            CustomDialog cd = new CustomDialog(Settings.this, getString(R.string.delete_account), getString(R.string.if_you_are_sure_you_would_like_to_delete_your_account_please_enter_your_password_to_confirm_this_action_cannot_be_undone),
                    getString(R.string.confirm), getString(R.string.cancel), null);
            cd.setEditText(getString(R.string.password1), null, ResourcesCompat.getDrawable(getResources(), R.drawable.padlock, getTheme()));
            cd.setPositiveButtonListener(v -> {
                Tools.hideKeyboard(Settings.this);
                pb.setVisibility(View.VISIBLE);
                if (cd.getEditText().getText().toString().equals(thisUser.getPassword())) {
                    cd.dismissDialog();
                    FirebaseFirestore.getInstance().collection("users").document(uid).delete().addOnCompleteListener(task1 -> {
                        if (task1.isComplete() && task1.isSuccessful()) {
                            Prefs.clearPrefs(Settings.this);
                            uid = null;
                            thisUser = null;
                            payments = null;
                            bills = null;
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> {
                                    if (task.isComplete()) {
                                        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
                                        CredentialManager manager = CredentialManager.create(Settings.this);
                                        manager.clearCredentialStateAsync(clearRequest, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {
                                            @Override
                                            public void onResult(Void unused) {
                                            }

                                            @Override
                                            public void onError(@NonNull ClearCredentialException e) {
                                                Log.e(TAG, "Couldn't clear user credentials: " + e);
                                            }
                                        });
                                        startActivity(new Intent(mContext, Login.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP).putExtra("Deleted", true));
                                    } else {
                                        pb.setVisibility(View.GONE);
                                        Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                                    }
                                });
                            } else {
                                pb.setVisibility(View.GONE);
                                Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                            }
                        } else {
                            pb.setVisibility(View.GONE);
                            Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                        }
                    });
                } else {
                    pb.setVisibility(View.GONE);
                    Notify.createPopup(Settings.this, getString(R.string.password_is_invalid), null);
                }
            });
        }
        else {
            CustomDialog cd = new CustomDialog(Settings.this, getString(R.string.delete_account), getString(R.string.if_you_are_sure_you_would_like_to_delete_your_account_please_type) + getString(R.string.delete)
                    + getString(R.string.in_the_box_below_this_action_cannot_be_undone), getString(R.string.confirm), getString(R.string.cancel), null);
            cd.setEditText(getString(R.string.password1), null, ResourcesCompat.getDrawable(getResources(), R.drawable.padlock, getTheme()));
            cd.setPositiveButtonListener(v -> {
                Tools.hideKeyboard(Settings.this);
                pb.setVisibility(View.VISIBLE);
                if (cd.getEditText().getText().toString().equals("Delete")) {
                    cd.dismissDialog();
                    FirebaseFirestore.getInstance().collection("users").document(uid).delete().addOnCompleteListener(task1 -> {
                        if (task1.isComplete() && task1.isSuccessful()) {
                            Prefs.clearPrefs(Settings.this);
                            uid = null;
                            thisUser = null;
                            payments = null;
                            bills = null;
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> {
                                    if (task.isComplete()) {
                                        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
                                        CredentialManager manager = CredentialManager.create(Settings.this);
                                        manager.clearCredentialStateAsync(clearRequest, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {
                                            @Override
                                            public void onResult(Void unused) {
                                                startActivity(new Intent(mContext, Login.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP).putExtra("Deleted", true));
                                            }

                                            @Override
                                            public void onError(@NonNull ClearCredentialException e) {
                                                pb.setVisibility(View.GONE);
                                                Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                                            }
                                        });
                                    } else {
                                        pb.setVisibility(View.GONE);
                                        Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                                    }
                                });
                            } else {
                                pb.setVisibility(View.GONE);
                                Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                            }
                        } else {
                            pb.setVisibility(View.GONE);
                            Notify.createPopup(Settings.this, getString(R.string.anErrorHasOccurred), null);
                        }
                    });
                } else {
                    pb.setVisibility(View.GONE);
                    Notify.createPopup(Settings.this, getString(R.string.please_enter) + getString(R.string.delete) + getString(R.string.to_proceed), null);
                }
            });
        }
    }

}