package com.example.billstracker.activities;

import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPassword extends AppCompatActivity {

    ConstraintLayout pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        TextInputEditText fpUsername = findViewById(R.id.fpUserName);
        Button submit = findViewById(R.id.btnSubmitBiller);
        TextView fpError = findViewById(R.id.fpError);
        LinearLayout back = findViewById(R.id.backButton);
        pb = findViewById(R.id.pb7);

        fpError.setVisibility(View.GONE);

        fpUsername.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                fpError.setVisibility(View.GONE);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fpUsername.setText(extras.getString("UserName"));
        }

        Tools.setupUI(ForgotPassword.this, findViewById(android.R.id.content));

        back.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            getOnBackPressedDispatcher().onBackPressed();
        });

        submit.setOnClickListener(v -> {
            if (fpUsername.getText() != null) {
                String email = fpUsername.getText().toString().trim();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Tools.hideKeyboard(ForgotPassword.this);
                    pb.setVisibility(View.VISIBLE);

                    FirebaseFirestore.getInstance().collection("users")
                            .whereEqualTo("userName", email)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                    User user = task.getResult().toObjects(User.class).get(0);
                                    if (user.getRegisteredWithGoogle()) {
                                        pb.setVisibility(View.GONE);
                                        Notify.createPopup(ForgotPassword.this,
                                                "This account is linked with Google. Please use 'Sign in with Google' on the login screen.", null);
                                    } else {
                                        sendResetPasswordEmail(email);
                                    }
                                } else {
                                    pb.setVisibility(View.GONE);
                                    Notify.createPopup(ForgotPassword.this, getString(R.string.emailNotFound), null);
                                }
                            });
                } else {
                    fpError.setVisibility(View.VISIBLE);
                }
            } else {
                Notify.createPopup(ForgotPassword.this, "The email address field cannot be blank.", null);
            }
        });

    }

    private void sendResetPasswordEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    pb.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        CustomDialog cd = new CustomDialog(ForgotPassword.this,
                                getString(R.string.emailSent),
                                getString(R.string.passwordResetLinkSent),
                                getString(R.string.ok), null, null);

                        cd.setPositiveButtonListener(v -> {
                            cd.dismissDialog();
                            getOnBackPressedDispatcher().onBackPressed();
                        });
                        cd.show();
                    } else {
                        Notify.createPopup(ForgotPassword.this, "Failed to send reset email. Please try again later.", null);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }

}