package com.example.billstracker.activities;

import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.billstracker.R;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    LinearLayout pb;

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

        submit.setOnClickListener(view -> {
            Tools.hideKeyboard(ForgotPassword.this);
            if (fpUsername.getText() != null) {
                if (Patterns.EMAIL_ADDRESS.matcher(fpUsername.getText().toString()).matches()) {
                    pb.setVisibility(View.VISIBLE);
                    FirebaseAuth.getInstance().sendPasswordResetEmail(fpUsername.getText().toString()).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    CustomDialog cd = new CustomDialog(ForgotPassword.this, getString(R.string.emailSent), getString(R.string.passwordResetLinkSent), getString(R.string.ok), null, null);
                                    cd.setPositiveButtonListener(v -> {
                                        cd.dismissDialog();
                                        pb.setVisibility(View.GONE);
                                        FirebaseAuth.getInstance().signOut();
                                        getOnBackPressedDispatcher().onBackPressed();
                                    });
                                    cd.setPerimeterListener(view1 -> {
                                        cd.dismissDialog();
                                        FirebaseAuth.getInstance().signOut();
                                        getOnBackPressedDispatcher().onBackPressed();
                                    });
                                } else {
                                    CustomDialog cd = new CustomDialog(ForgotPassword.this, getString(R.string.emailNotFound), getString(R.string.emailNotRegistered), getString(R.string.ok), null, null);
                                    cd.setPositiveButtonListener(v -> {
                                        cd.dismissDialog();
                                        pb.setVisibility(View.GONE);
                                        FirebaseAuth.getInstance().signOut();
                                    });
                                    cd.setPerimeterListener(view1 -> {
                                        cd.dismissDialog();
                                        FirebaseAuth.getInstance().signOut();
                                        getOnBackPressedDispatcher().onBackPressed();
                                    });
                                }
                            });
                } else {
                    pb.setVisibility(View.GONE);
                    fpError.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }

}