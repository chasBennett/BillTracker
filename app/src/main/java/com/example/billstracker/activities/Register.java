package com.example.billstracker.activities;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.example.billstracker.R;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.internal.zzaf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

public class Register extends AppCompatActivity {

    InputMethodManager mgr;
    TextView registerError;
    TextInputEditText registerName;
    TextInputEditText registerEmail;
    TextInputEditText registerPassword;
    LinearLayout passwordRequirements;
    TextView passwordLength;
    TextView uppercaseLetter;
    TextView lowercaseLetter;
    TextView number;
    TextView viewTerms;
    Button registerButton;
    TextView alreadyRegistered;
    LinearLayout registerBox;
    LinearLayout termsView;
    TextView printTerms;
    Button closeTerms;
    ConstraintLayout pb;
    ScrollView scroll;
    boolean nam, pas, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        registerError = findViewById(R.id.registerError);
        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        passwordRequirements = findViewById(R.id.passwordRequirements);
        passwordLength = findViewById(R.id.passwordLength);
        uppercaseLetter = findViewById(R.id.uppercaseLetter);
        lowercaseLetter = findViewById(R.id.lowercaseLetter);
        number = findViewById(R.id.number);
        scroll = findViewById(R.id.registerScroll);
        viewTerms = findViewById(R.id.viewTerms);
        registerButton = findViewById(R.id.registerButton);
        alreadyRegistered = findViewById(R.id.alreadyRegisteredButton);
        registerBox = findViewById(R.id.registerBox);
        termsView = findViewById(R.id.termsView);
        printTerms = findViewById(R.id.printTerms);
        closeTerms = findViewById(R.id.btnCloseTerms);
        pb = findViewById(R.id.progressBar);
        nam = false;
        pas = false;
        email = false;

        Tools.fixProgressBarLogo(pb);

        termsView.setVisibility(View.GONE);
        registerBox.setVisibility(View.VISIBLE);
        passwordRequirements.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);

        Tools.setupUI(Register.this, findViewById(android.R.id.content));

        viewTerms.setOnClickListener(view -> {
            mgr.hideSoftInputFromWindow(registerName.getApplicationWindowToken(), 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, () -> {
                    termsView.setVisibility(View.GONE);
                    registerBox.setVisibility(View.VISIBLE);
                });
            }
            termsView.setVisibility(View.VISIBLE);
            registerBox.setVisibility(View.GONE);
            String sb = getString(R.string.tac1) + getString(R.string.tac2) + getString(R.string.tac3) + getString(R.string.tac4) + getString(R.string.tac5) + getString(R.string.tac6) +
                    getString(R.string.tac7) + getString(R.string.tac8) + getString(R.string.tac9) + getString(R.string.tac10) + getString(R.string.tac11) + getString(R.string.tac12) +
                    getString(R.string.tac13) + getString(R.string.tac14) + getString(R.string.tac15) + getString(R.string.tac16) + getString(R.string.tac17) + getString(R.string.tac18) +
                    getString(R.string.tac19) + getString(R.string.tac20) + getString(R.string.tac21) + getString(R.string.tac22) + getString(R.string.tac23) +
                    getString(R.string.tac24) + getString(R.string.tac25);
            printTerms.setText(sb);
        });

        closeTerms.setOnClickListener(v -> {
            termsView.setVisibility(View.GONE);
            registerBox.setVisibility(View.VISIBLE);
        });
        Watcher watcher = new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                checkEntries();
            }
        };

        registerName.addTextChangedListener(watcher);
        registerEmail.addTextChangedListener(watcher);
        registerPassword.addTextChangedListener(watcher);

        registerButton.setOnClickListener(v -> {
            if (nam && email && pas && registerName.getText() != null && registerEmail.getText() != null && registerPassword.getText() != null) {
                pb.setVisibility(View.VISIBLE);
                mgr.hideSoftInputFromWindow(registerName.getApplicationWindowToken(), 0);
                registerUser(registerName.getText().toString(), registerEmail.getText().toString(), registerPassword.getText().toString());
            } else {
                pb.setVisibility(View.GONE);
                String errorMessage;
                if (!nam || registerName.getText() == null) {
                    errorMessage = getString(R.string.name_must_be_at_least_3_characters_long);
                } else if (!email || registerEmail.getText() == null) {
                    errorMessage = getString(R.string.enterAValidEmailAddress);
                } else {
                    errorMessage = getString(R.string.password_is_invalid);
                }
                Notify.createPopup(Register.this, errorMessage, null);
            }
        });

        alreadyRegistered.setOnClickListener(v -> {
            Tools.onBackSelected(Register.this);
            mgr.hideSoftInputFromWindow(registerName.getApplicationWindowToken(), 0);
        });

    }

    protected void checkEntries() {
        ////////Name Validation/////////
        if (getCurrentFocus() == registerName) {
            if (registerName.getText() != null) {
                nam = registerName.getText().toString().length() >= 3;
            } else {
                nam = false;
            }
        }

        ////////Email Validation/////////
        if (getCurrentFocus() == registerEmail) {
            if (registerEmail.getText() != null && !registerEmail.getText().toString().isEmpty()) {
                registerError.setVisibility(View.VISIBLE);
                if (registerEmail.getText().toString().length() > 6 && android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail.getText().toString()).matches()) {
                    FirebaseTools.isRegisteredEmail(registerError, registerEmail.getText().toString(), isSuccessful -> {
                        email = !isSuccessful;
                        Tools.setEditTextChecked(registerEmail, email);
                    });
                } else {
                    email = false;
                    registerError.setText(getString(R.string.enterAValidEmailAddress));
                }
            } else {
                email = false;
                registerError.setVisibility(View.GONE);
            }
        }

        ////////Password Validation////////
        if (getCurrentFocus() == registerPassword) {
            pas = TextTools.isAcceptablePassword(registerPassword, passwordLength, uppercaseLetter, lowercaseLetter, number);
            if (!pas && registerPassword.getText() != null && !registerPassword.getText().toString().isEmpty()) {
                passwordRequirements.setVisibility(View.VISIBLE);
                passwordRequirements.post(() -> scroll.smoothScrollTo(0, passwordRequirements.getBottom()));
            } else {
                passwordRequirements.setVisibility(View.GONE);
            }
        }

        Tools.setEditTextChecked(registerName, nam);
        Tools.setEditTextChecked(registerPassword, pas);
        Tools.setEditTextChecked(registerEmail, email);
    }

    protected void registerUser(String name, String email, String password) {
        if (Tools.isValidString(registerName, 3) && Tools.isValidEmail(registerEmail) && TextTools.isAcceptablePassword(registerPassword, passwordLength, uppercaseLetter, lowercaseLetter, number)) {
            mgr.hideSoftInputFromWindow(registerName.getApplicationWindowToken(), 0);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    pb.setVisibility(View.GONE);
                    Notify.createPopup(Register.this, getString(R.string.user_registration_failed), null);
                } else {
                    String userId = mAuth.getUid();

                    // Create the date string for registration
                    String dateRegistered = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));

                    // IMPLEMENTATION: Use the new addUser builder flow
                    Repository.getInstance().addUser(email, password, name, userId, this)
                            .setDateRegistered(dateRegistered)
                            .setLastLogin("01-01-2022") // Default starting point
                            .setIncome(0.0)
                            .setPayFrequency(2) // Bi-weekly default
                            .save((success, message) -> {
                                if (success) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        UserProfileChangeRequest update = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name).build();
                                        user.updateProfile(update);
                                        FirebaseTools.sendVerificationEmail(user, (wasSuccessful, message1) -> {
                                            if (wasSuccessful) {
                                                pb.setVisibility(View.GONE);
                                                CustomDialog cd = new CustomDialog(Register.this, getString(R.string.account_created_successfully), getString(R.string.accountCreatedSuccessfullyMessage), getString(R.string.ok), null, null);
                                                cd.setPositiveButtonListener(v -> {
                                                    cd.dismissDialog();
                                                    pb.setVisibility(View.VISIBLE);
                                                    FirebaseAuth.getInstance().signOut();

                                                    // Clear Credential Manager State
                                                    ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
                                                    CredentialManager manager = CredentialManager.create(Register.this);
                                                    manager.clearCredentialStateAsync(clearRequest, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {
                                                        @Override
                                                        public void onResult(Void unused) {
                                                        }

                                                        @Override
                                                        public void onError(@NonNull ClearCredentialException e) {
                                                            Log.e(TAG, "Couldn't clear user credentials: " + e);
                                                        }
                                                    });

                                                    pb.setVisibility(View.GONE);
                                                    Repository.getInstance().setStaySignedIn(false, Register.this);
                                                    Register.this.startActivity(new Intent(Register.this, Login.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK).putExtra("Welcome", true));
                                                });
                                                cd.show();
                                                pb.setVisibility(View.GONE);
                                            } else {
                                                pb.setVisibility(View.GONE);
                                                Notify.createPopup(Register.this, getString(R.string.anErrorHasOccurred), null);
                                                finish();
                                                ActivityOptionsCompat.makeCustomAnimation(Register.this, 0, 0);
                                                Intent logon = new Intent(Register.this, Login.class);
                                                logon.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(logon);
                                            }
                                        });
                                    }
                                } else {
                                    pb.setVisibility(View.GONE);
                                    Notify.createPopup(Register.this, "Sync Error: " + message, null);
                                }
                            });
                }
            });
        }
    }
}