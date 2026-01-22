package com.example.billstracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.databinding.ActivityLoginBinding;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Google;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {

    private final ActivityResultLauncher<String> launcher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
            }
    );

    private final Repository repo = Repository.getInstance();
    private boolean starting = false;
    private boolean biometricEligible = false;
    private boolean googleLogin = false;
    private boolean isLoading = false;

    private BiometricManager biometricManager;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private ActivityLoginBinding binding;

    private static final String PREFS_NAME = "biometric_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_GOOGLE = "is_google";
    private static final String KEY_PIN = "user_pin";
    private boolean welcome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar15.progressBar.setVisibility(View.GONE);

        biometricManager = BiometricManager.from(this);
        setupBiometricPrompt();

        googleLogin = false;

        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled())
            Tools.requestPermissionLauncher(Login.this, launcher);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("Open Email", false)) {
                Tools.openEmailApp(Login.this);
            } else if (extras.getBoolean("Deleted")) {
                Notify.createPopup(Login.this, getString(R.string.profileDeletedSuccessfully), null);
                binding.loginUsername.setText("");
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putString(KEY_EMAIL, null).apply();
                prefs.edit().putString(KEY_PASSWORD, null).apply();
                prefs.edit().putString(KEY_PIN, null).apply();
                prefs.edit().putBoolean(KEY_IS_GOOGLE, false).apply();
            }
            else if (extras.getBoolean("Welcome")) {
                welcome = true;
            }
        }

        repo.initializeBackEnd(this, (wasSuccessful, message) -> {});

        binding.staySignedIn.setChecked(repo.getStaySignedIn(this));
        Tools.setupUI(Login.this, findViewById(android.R.id.content));

        addListeners();

        checkForAutoSignIn();
    }

    private void checkForAutoSignIn () {
        if (repo.getStaySignedIn(this)) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String email = prefs.getString(KEY_EMAIL, null);
            String password = prefs.getString(KEY_PASSWORD, null);
            String uid = repo.getUid(this);

            if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
                if (!prefs.getBoolean(KEY_IS_GOOGLE, false)) {
                    signInWithEmailAndPassword(email, password);
                } else {
                    if (uid != null && !uid.isEmpty()) {
                        loadWithStoredCredentials();
                    }
                }
            }
        }
        else {
            autoQuickLoginPrompt();
        }
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(Login.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        String email = prefs.getString(KEY_EMAIL, null);
                        String password = prefs.getString(KEY_PASSWORD, null);
                        signInWithEmailAndPassword(email, password);
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometricAuthentication))
                .setNegativeButtonText(getString(R.string.cancel))
                .setConfirmationRequired(false)
                .build();
    }

    private void addListeners() {
        binding.loginError.setText("");

        binding.forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(Login.this, ForgotPassword.class)));

        binding.createAccount.setOnClickListener(v ->
                startActivity(new Intent(Login.this, Register.class)));

        Tools.addValidEmailListener(binding.loginUsername);

        binding.loginPassword.addTextChangedListener(new Watcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Tools.isValidString(binding.loginPassword, 5);
            }
        });

        TextTools.onEnterSelected(binding.loginPassword, isEnter -> {
            if (isEnter) {
                googleLogin = false;
                checkLogin();
            }
        });

        binding.loginButton.setOnClickListener(v -> {
            googleLogin = false;
            checkLogin();
            TextTools.closeSoftInput(binding.loginPassword);
        });

        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        biometricEligible = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;

        if (!biometricEligible) {
            TextViewCompat.setCompoundDrawableTintList(binding.biometricButton,
                    ColorStateList.valueOf(getResources().getColor(R.color.neutralGray, getTheme())));
            binding.biometricButton.setTextColor(getResources().getColor(R.color.neutralGray, getTheme()));
        }

        binding.biometricButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String email = prefs.getString(KEY_EMAIL, null);
            String password = prefs.getString(KEY_PASSWORD, null);
            boolean isGoogleUser = prefs.getBoolean(KEY_IS_GOOGLE, false);

            if (!biometricEligible || email == null || password == null || email.isEmpty() || password.isEmpty()) {
                Notify.createPopup(this, "Please login to enable this feature", null);
                return;
            }

            if (isGoogleUser) {
                promptPinLogin();
            } else {
                biometricPrompt.authenticate(promptInfo);
            }
        });

        binding.googleButton.setOnClickListener(v -> Google.launchGoogleSignIn(Login.this, (wasSuccessful, user, token) -> {
            if (wasSuccessful && user != null) {
                repo.setUid(user.getUid(), Login.this);
                googleLogin = true;
                saveCredentials(user.getEmail(), "GOOGLE_USER", true);
                promptPinSetupIfNeeded(this::loadWithStoredCredentials);
            }
        }));
    }

    private void checkLogin() {
        if (!Tools.isValidString(binding.loginPassword, 4) || binding.loginPassword.getText() == null || binding.loginPassword.getText().toString().isEmpty()) {
            Notify.createPopup(Login.this, getString(R.string.password_is_invalid), null);
        } else if (!Tools.isValidEmail(binding.loginUsername) || binding.loginUsername.getText() == null) {
            Notify.createPopup(Login.this, getString(R.string.username_is_invalid), null);
        } else {
            binding.loginError.setVisibility(View.GONE);
            signInWithEmailAndPassword(binding.loginUsername.getText().toString(), binding.loginPassword.getText().toString());
        }
    }

    private void signInWithEmailAndPassword(String username, String password) {
        binding.progressBar15.progressBar.setVisibility(View.VISIBLE);
        FirebaseTools.signInWithEmailAndPassword(this, username, password, wasSuccessful -> {
            binding.progressBar15.progressBar.setVisibility(View.GONE);
            if (wasSuccessful) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                    saveCredentials(username, password, false);
                    repo.setUid(firebaseUser.getUid(), Login.this);
                    repo.setStaySignedIn(binding.staySignedIn.isChecked(), Login.this);
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_IS_GOOGLE, false).apply();
                    loadWithStoredCredentials();
                } else {
                    Notify.createPopup(Login.this, "Please verify your email before logging in.", null);
                }
            } else {
                Notify.createPopup(Login.this, "Login failed. Please try again.", null);
            }
        });
    }

    /* ==================== Quick Login / PIN Handling ==================== */

    private void autoQuickLoginPrompt() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);
        String password = prefs.getString(KEY_PASSWORD, null);
        String uid = repo.getUid(this);

        if (prefs.getBoolean(KEY_IS_GOOGLE, false)) {
            binding.biometricButton.setText(getString(R.string.pin_login));
            binding.biometricButton.setCompoundDrawablesWithIntrinsicBounds(null, null, AppCompatResources.getDrawable(Login.this, R.drawable.keypad_icon), null);
        }

        if (!welcome && email != null && !email.isEmpty() && password != null && !password.isEmpty() && !prefs.getBoolean(KEY_IS_GOOGLE, false)) {
            if (biometricEligible) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                if (uid != null && !uid.isEmpty()) {
                    promptPinLogin();
                }
            }
        }
        else {
            if (!welcome && uid != null && !uid.isEmpty() && prefs.getBoolean(KEY_IS_GOOGLE, false)) {
                promptPinLogin();
            }
        }
    }

    private void promptPinSetupIfNeeded(Runnable onComplete) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getString(KEY_PIN, null) != null) {
            onComplete.run();
            return;
        }

        CustomDialog cd = new CustomDialog(this,
                getString(R.string.set_quick_login_pin),
                getString(R.string.enter_a_4_6_digit_pin_for_faster_future_login),
                getString(R.string.save), getString(R.string.skip), null);
        cd.enablePinKeypad();
        cd.setPositiveButtonListener(v -> {
            String pin = cd.getInput();
            if (pin.length() >= 4 && pin.length() <= 6) {
                prefs.edit().putString(KEY_PIN, pin).apply();
                cd.dismissDialog();
                onComplete.run();
            } else {
                Notify.createDialogPopup(cd, getString(R.string.pin_must_be_4_6_digits), null);
            }
        });
        cd.setNegativeButtonListener(v -> {
            cd.dismissDialog();
            onComplete.run();
        });
        cd.show();
    }

    private void promptPinLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String storedPin = prefs.getString(KEY_PIN, null);
        String uid = repo.getUid(this);

        if (storedPin == null || uid == null || uid.isEmpty()) {
            Notify.createPopup(this, getString(R.string.pin_login_not_available), null);
            return;
        }

        CustomDialog cd = new CustomDialog(this,
                "",
                getString(R.string.enter_your_pin),
                getString(R.string.login), getString(R.string.cancel), null);
        cd.enablePinKeypad();
        cd.setPositiveButtonListener(v -> {
            String enteredPin = cd.getInput();
            if (enteredPin.equals(storedPin)) {
                cd.dismissDialog();
                loadWithStoredCredentials();
            } else {
                Notify.createDialogPopup(cd, "Incorrect PIN", null);
            }
        });
        cd.setNegativeButtonListener(v -> cd.dismissDialog());
        cd.show();
    }

    private void loadWithStoredCredentials() {
        if (isLoading) return;
        isLoading = true;
        binding.progressBar15.progressBar.setVisibility(View.VISIBLE);

        String uid = repo.getUid(this);
        if (uid == null || uid.isEmpty()) {
            binding.progressBar15.progressBar.setVisibility(View.GONE);
            Notify.createPopup(this, "Failed to retrieve user data", null);
            return;
        }

        if (repo.getLastUid(this) != null) {
            if (!uid.equals(repo.getLastUid(this))) {
                repo.clearDisk(this);
            }
        }

        repo.fetchCloudData(uid, this, (success, msg) -> {
            if (success) loadPartnerData();
            else {
                isLoading = false;
                binding.progressBar15.progressBar.setVisibility(View.GONE);
                Notify.createPopup(this, "Login failed. Please check connection.", null);
            }
        });
    }

    private void loadPartnerData() {
        User thisUser = repo.getUser(this);
        if (thisUser == null || thisUser.getName() == null) {
            binding.progressBar15.progressBar.setVisibility(View.GONE);
            Notify.createPopup(this, "Error loading user profile.", null);
            return;
        }

        thisUser.setRegisteredWithGoogle(googleLogin);
        if (thisUser.getPartners() == null) thisUser.setPartners(new ArrayList<>());

        if (!thisUser.getPartners().isEmpty()) {
            final int totalPartners = thisUser.getPartners().size();
            final int[] loadedPartners = {0};

            for (Partner partner : thisUser.getPartners()) {
                FirebaseTools.getPartner(this, partner, wasSuccessful -> {
                    loadedPartners[0]++;
                    if (loadedPartners[0] == totalPartners) finalizeDataAndLaunch();
                });
            }
        } else finalizeDataAndLaunch();
    }

    private void finalizeDataAndLaunch() {
        setOwnership();
        repo.setStaySignedIn(binding.staySignedIn.isChecked(), Login.this);
        launchMainActivity();
    }

    private void setOwnership() {
        String uid = repo.getUid(this);
        if (uid != null) {
            repo.setLastUid(this, uid);
            if (repo.getBills() != null) for (Bill bill : repo.getBills()) if (bill.getOwner() == null) bill.setOwner(uid);
            if (repo.getPayments() != null) for (Payment payment : repo.getPayments()) if (payment.getOwner() == null) payment.setOwner(uid);
            if (repo.getExpenses() != null) for (Expense exp : repo.getExpenses()) if (exp.getOwner() == null) exp.setOwner(uid);
        }
    }

    private void launchMainActivity() {
        if (!starting) {
            starting = true;
            Intent home = new Intent(this, MainActivity2.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        }
    }

    private void saveCredentials(String email, String password, boolean isGoogle) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .putBoolean(KEY_IS_GOOGLE, isGoogle)
                .apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && repo.getStaySignedIn(this)) {
            checkForAutoSignIn();
        }
    }
}
