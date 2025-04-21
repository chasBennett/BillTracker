package com.example.billstracker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Google;
import com.example.billstracker.tools.Prefs;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.UserData;
import com.example.billstracker.tools.Watcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Login extends AppCompatActivity {

    public static final ArrayList<User> userList = new ArrayList<>();
    public static User thisUser;
    public static Payments payments = new Payments();
    public static Expenses expenses = new Expenses();
    public static Bills bills = new Bills();
    public static String uid;
    TextView loginError;
    TextView forgotPassword;
    TextView biometricButton;
    TextView register;
    TextInputEditText loginUsername;
    TextInputEditText loginPassword;
    Button loginButton;
    boolean starting;
    boolean biometricPreference;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    BiometricManager biometricManager;
    Context mContext;
    androidx.appcompat.widget.SwitchCompat staySignedIn;
    ConstraintLayout pb;
    boolean credentialLogin;
    final ActivityResultLauncher<String> launcher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginError = findViewById(R.id.loginError);
        forgotPassword = findViewById(R.id.forgotPassword);
        biometricButton = findViewById(R.id.biometricButton);
        register = findViewById(R.id.createAccount);
        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        biometricManager = BiometricManager.from(Login.this);
        mContext = Login.this;
        pb = findViewById(R.id.progressBar15);
        Tools.fixProgressBarLogo(pb);
        staySignedIn = findViewById(R.id.staySignedIn);
        thisUser = new User();
        credentialLogin = false;
        starting = false;

        FirebaseApp.initializeApp(Login.this);
        FirebaseAuth.getInstance().useAppLanguage();

        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()) {
            Tools.requestPermissionLauncher(Login.this, launcher);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean openEmail = extras.getBoolean("Open Email", false);
            if (openEmail) {
                Tools.openEmailApp(Login.this);
            }
            else if (extras.getBoolean("Deleted")) {
                Notify.createPopup(Login.this, getString(R.string.profileDeletedSuccessfully), null);
                loginUsername.setText("");
                uid = null;
            }
            getIntent().getExtras().clear();
        }

        pb.setVisibility(View.GONE);

        payments = new Payments(new ArrayList<>());
        expenses = new Expenses(new ArrayList<>());
        bills = new Bills(new ArrayList<>());
        payments = new Payments(new ArrayList<>());
        thisUser = new User();

        if (Prefs.getUserUid(Login.this) != null) {
            uid = Prefs.getUserUid(Login.this);
        }
        else {
            uid = null;
        }

        staySignedIn.setChecked(Prefs.getStaySignedIn(Login.this));
        biometricPreference = Prefs.getBiometricPreferences(Login.this);

        Tools.setupUI(Login.this, findViewById(android.R.id.content));

        addListeners();

        if (Prefs.getUserName(Login.this) != null && !Prefs.getUserName(Login.this).isEmpty()) {
            loginUsername.setText(Prefs.getUserName(Login.this));
        }

        if (Prefs.getUserUid(Login.this) != null && Prefs.getStaySignedIn(Login.this)) {
            uid = Prefs.getUserUid(Login.this);
            load();
        }

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.biometricAuthentication)).setNegativeButtonText(getString(R.string.cancel)).setConfirmationRequired(false).build();
        biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(Login.this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (Prefs.getUserName(Login.this) != null && !Prefs.getUserName(Login.this).isEmpty() && Prefs.getPassword(Login.this) != null && !Prefs.getPassword(Login.this).isEmpty()) {
                    signInWithEmailAndPassword(Prefs.getUserName(Login.this), Prefs.getPassword(Login.this));
                }
                else {
                    Notify.createPopup(Login.this, getString(R.string.enableAfterLoggingIn), null);
                }
            }

        });

        findViewById(R.id.google_button).setOnClickListener(view -> Google.launchGoogleSignIn(Login.this, (wasSuccessful, user) -> {
            if (wasSuccessful && user != null) {
                uid = user.getUid();
                credentialLogin = true;
                load();
            }
        }));

    }

    protected void addListeners() {

        loginError.setText("");

        forgotPassword.setOnClickListener(view -> startActivity(new Intent(Login.this, ForgotPassword.class)));

        register.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));

        Tools.addValidEmailListener(loginUsername);

        loginPassword.addTextChangedListener(new Watcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Tools.isValidString(loginPassword, 5);
            }
        });

        TextTools.onEnterSelected(loginPassword, isEnter -> {
            if (isEnter) {
                credentialLogin = false;
                checkLogin();
            }
        });

        loginButton.setOnClickListener(view -> {
            credentialLogin = false;
            checkLogin();
            TextTools.closeSoftInput(loginPassword);
        });

        biometricButton.setOnClickListener(view -> {

            biometricManager = BiometricManager.from(Login.this);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
                Notify.createPopup(Login.this, getString(R.string.biometricsNotSupported), null);
                return;
            }

            if (!Prefs.getBiometricPreferences(this)) {
                Notify.createPopup(Login.this, getString(R.string.enableAfterLoggingIn), null);
            }
            else {
                biometricManager = BiometricManager.from(Login.this);
                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
                    Notify.createPopup(Login.this, getString(R.string.biometricsNotSupported), null);
                } else {
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        });
    }

    protected void checkLogin() {
        if (!Tools.isValidString(loginPassword, 4) || loginPassword == null || loginPassword.getText() == null) {
            Notify.createPopup(Login.this, getString(R.string.password_is_invalid), null);
        }
        else if (!Tools.isValidEmail(loginUsername) || loginUsername == null || loginUsername.getText() == null) {
            Notify.createPopup(Login.this, getString(R.string.username_is_invalid), null);
        }
        else {
            loginError.setVisibility(View.GONE);
            signInWithEmailAndPassword(loginUsername.getText().toString(), loginPassword.getText().toString());
        }
    }

    protected void signInWithEmailAndPassword(String username, String password) {

        pb.setVisibility(View.VISIBLE);
        FirebaseTools.signInWithEmailAndPassword(Login.this, username, password, wasSuccessful -> {
            if (wasSuccessful) {
                FirebaseTools.checkIfEmailVerified(Login.this, pb, wasSuccessful1 -> {
                    if (wasSuccessful1) {
                        Prefs.setUserName(Login.this, username);
                        Prefs.setPassword(Login.this, password);

                        if (staySignedIn.isChecked() && !Prefs.getStaySignedIn(Login.this)) {
                            Prefs.setSignedIn(Login.this, true);
                        }

                        load();
                    }
                    else {
                        pb.setVisibility(View.GONE);
                        Notify.createPopup(Login.this, getString(R.string.sign_in_failed_please_try_again), null);
                        Prefs.setSignedIn(Login.this, false);
                    }
                });
            }
            else {
                FirebaseTools.findEmail(username, isSuccessful -> {
                    if (!isSuccessful) {
                        Notify.createPopup(Login.this, getString(R.string.email_is_not_yet_registered_please_click_create_an_account_to_get_started),  null);
                    }
                    if (!credentialLogin) {
                        pb.setVisibility(View.GONE);
                        Prefs.setSignedIn(Login.this, false);
                        Notify.createPopup(Login.this, getString(R.string.login_failed_please_try_again), null);
                    }
                });
            }
        });
    }
    protected void load () {
        pb.setVisibility(View.VISIBLE);
        if (uid != null) {
            payments = new Payments(new ArrayList<>());
            expenses = new Expenses(new ArrayList<>());
            bills = new Bills(new ArrayList<>());

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseTools.loadUser(Login.this, pb, wasSuccessful -> {
                    if (wasSuccessful) {
                        loadPartnerData();
                    } else {
                        Notify.createPopup(Login.this, getString(R.string.login_failed_please_try_again), null);
                        pb.setVisibility(View.GONE);
                    }
                });
            }
        }
        else {
            Notify.createPopup(Login.this, getString(R.string.login_failed_please_try_again), null);
            pb.setVisibility(View.GONE);
        }
    }
    protected void loadPartnerData () {

        if (thisUser.getName() == null) {
            thisUser.setName("User");
            UserData.save();
        }
        if (thisUser.getPartners() == null) {
            thisUser.setPartners(new ArrayList<>());
        }
        if (thisUser.getPartners() != null && !thisUser.getPartners().isEmpty()) {
            for (Partner partner : thisUser.getPartners()) {
                FirebaseTools.getPartner(partner, wasSuccessful -> {
                    if (wasSuccessful) {
                        FirebaseTools.loadPartnerData(partner.getPartnerUid(), isSuccessful -> {});
                    } else {
                        loadUserData();
                    }
                });
            }
            if (thisUser == null) {
                pb.setVisibility(View.GONE);
                Notify.createPopup(Login.this, getString(R.string.anErrorHasOccurred), null);
            } else {
                loadUserData();
            }
        }
        else {
            loadUserData();
        }
    }
    protected void loadUserData () {

        FirebaseTools.getPayments(uid, isSuccessful -> FirebaseTools.getBills(uid, isSuccessful1 -> FirebaseTools.getExpenses(uid, isSuccessful2 -> setOwnership())));
    }
    protected void setOwnership () {
        if (uid != null) {
            if (bills != null && !bills.getBills().isEmpty()) {
                for (Bill bill : bills.getBills()) {
                    if (bill.getOwner() == null) {
                        bill.setOwner(uid);
                    }
                }
            }
            if (payments != null && !payments.getPayments().isEmpty()) {
                for (Payment payment: payments.getPayments()) {
                    if (payment.getOwner() == null) {
                        payment.setOwner(uid);
                    }
                }
            }
            if (expenses != null && !expenses.getExpenses().isEmpty()) {
                for (Expense expense: expenses.getExpenses()) {
                    if (expense.getOwner() == null) {
                        expense.setOwner(uid);
                    }
                }
            }
            checkBiometricPreference();
        }
        else {
            Notify.createPopup(Login.this, getString(R.string.anErrorHasOccurred), null);
        }
    }

    protected void checkBiometricPreference() {

        boolean allowBiometrics = Prefs.getAllowBiometrics(this);
        boolean biometricEligible = true;
        biometricPreference = Prefs.getBiometricPreferences(this);

        Prefs.setSignedIn(Login.this, staySignedIn.isChecked());

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            biometricEligible = false;
            TextViewCompat.setCompoundDrawableTintList(biometricButton, ColorStateList.valueOf(getResources().getColor(R.color.neutralGray, getTheme())));
            biometricButton.setTextColor(getResources().getColor(R.color.neutralGray, getTheme()));
        }

        if (allowBiometrics && biometricEligible) {

            if (!biometricPreference) {
                CustomDialog cd = new CustomDialog(Login.this, getString(R.string.enableBiometrics1), getString(R.string.willYouEnableBiometrics), getString(R.string.yes), getString(R.string.notRightNow),
                        getString(R.string.dontAskAgain));
                cd.setPositiveButtonListener(v -> {
                    Prefs.setBiometricPreferences(Login.this, true);
                    Prefs.setAllowBiometrics(Login.this, true);
                    cd.dismissDialog();
                    launchMainActivity();
                });
                cd.setNeutralButtonListener(v -> {
                    Prefs.setBiometricPreferences(Login.this, false);
                    Prefs.setAllowBiometrics(Login.this, false);
                    Prefs.setUserName(Login.this, "");
                    Prefs.setPassword(Login.this, "");
                    Notify.createPopup(Login.this, getString(R.string.biometricsAreDisabled), null);
                    launchMainActivity();
                });
                cd.setNegativeButtonListener(v -> launchMainActivity());
            }
            else {
                launchMainActivity();
            }

        } else {
            launchMainActivity();
        }
    }

    protected void launchMainActivity() {
        if (!starting) {
            Intent home = new Intent(Login.this, MainActivity2.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            starting = true;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            if (Prefs.getStaySignedIn(Login.this)) {
                load();
            }
        }
    }
}