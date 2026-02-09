package com.example.billstracker.activities;

import static com.example.billstracker.tools.Google.clearCredentials;
import static com.example.billstracker.tools.Keys.KEY_CANCELED_BY_USER;
import static com.example.billstracker.tools.Keys.KEY_DELETED;
import static com.example.billstracker.tools.Keys.KEY_PREVENT_AUTO_LOGIN;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.databinding.ActivityLoginBinding;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Google;
import com.example.billstracker.tools.LocalStore;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {
    private final ActivityResultLauncher<String> launcher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
            }
    );

    private Repository repo;
    private LocalStore store;
    private boolean biometricEligible = false;
    private boolean googleLogin = false;
    private boolean starting = false;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private ActivityLoginBinding binding;
    private boolean preventAutoLogin = false;
    private final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repo = Repository.getInstance(this);
        store = repo.getStore();
        preventAutoLogin = !store.getStaySignedIn();
        googleLogin = repo.checkIfGoogleUser();

        binding.progressBar15.progressBar.setVisibility(View.GONE);
        binding.loginError.setText("");
        binding.staySignedIn.setChecked(!preventAutoLogin);

        setupBiometricPrompt();

        repo.setUid(store.getLastUid());

        Tools.setupUI(this, findViewById(android.R.id.content));
        addListeners();

        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled())
            Tools.requestPermissionLauncher(this, launcher);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean(KEY_DELETED, false)) {
                Notify.createPopup(this, getString(R.string.profileDeletedSuccessfully), null);
                binding.loginUsername.setText("");
            }
            else if (extras.getBoolean(KEY_PREVENT_AUTO_LOGIN, false)) {
                preventAutoLogin = true;
            }
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        biometricEligible = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;

        if (!biometricEligible) {
            TextViewCompat.setCompoundDrawableTintList(binding.biometricButton, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.neutralGray)));
            binding.biometricButton.setTextColor(ContextCompat.getColor(this, R.color.neutralGray));
        }

        if (googleLogin) {
            binding.biometricButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.keypad_icon, 0);
            binding.biometricButton.setText("Pin Login");
        }

        repo.initializeBackEnd((success, msg) -> {
            if (isFinishing() || isDestroyed()) return;
            if (success) {
                checkForAutoSignIn();
            }
        });
    }

    protected void addListeners() {

        binding.forgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPassword.class)));
        binding.createAccount.setOnClickListener(v -> startActivity(new Intent(this, Register.class)));
        Tools.addValidEmailListener(binding.loginUsername);
        TextTools.addTextChangedListener(binding.loginPassword, () -> Tools.isValidString(binding.loginPassword, 5));
        TextTools.onEnterSelected(binding.loginPassword, isEnter -> {
            if (isEnter) {
                checkLogin();
            }
        });

        binding.loginButton.setOnClickListener(v -> {
            checkLogin();
        });

        binding.biometricButton.setOnClickListener(v -> {

            // Case 1: Firebase EmailAuth User
            if (!googleLogin && store.getEmail() != null && !store.getEmail().isEmpty() && store.getPassword() != null && !store.getPassword().isEmpty() && store.getAllowBiometrics() && biometricEligible) {
                biometricPrompt.authenticate(promptInfo);
            }
            // Case 2: GoogleAuth User
            else if (googleLogin) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && store.getUid() != null && !store.getUid().isEmpty()) {
                    promptPinLogin();
                }
                else {
                    Google.attemptSilentSignIn(this, (wasSuccessful, user1, message) -> {
                        if (wasSuccessful) {
                            promptPinLogin();
                        }
                        else {
                            Notify.createPopup(Login.this, "Session expired. Please tap the Google button to sign in.", null);
                        }
                    });
                }
            }
            // Case 3: User must enable biometrics first
            else {
                Notify.createPopup(this, getString(R.string.enableAfterLoggingIn), null);
            }
        });

        binding.googleButton.setOnClickListener(v -> Google.launchGoogleSignIn(this, (wasSuccessful, user, token) -> {
            if (isFinishing() || isDestroyed()) return;
            if (wasSuccessful && user != null) {
                repo.loginUser(user.getEmail(), user.getUid(), user.getUid(), true, true, (success, message) -> {
                    if (success) {
                        googleLogin = true;
                        if (store.getPin() == null || store.getPin().isEmpty()) {
                            launchPinSetup((wasSuccessful1, message1) -> {
                                if (isFinishing() || isDestroyed()) return;
                                loadWithStoredCredentials();
                            });
                        }
                        else {
                            loadWithStoredCredentials();
                        }
                    } else {
                        Notify.createPopup(this, "Login failed: " + message + ".", null);
                    }
                });
            }
            else {
                if (token != null && (token.contains(KEY_CANCELED_BY_USER))) {
                    Log.d(TAG, "User cancelled Google Sign-In prompt.");
                } else {
                    Notify.createPopup(this, "Google sign in failed. Please try again.", null);
                }
            }
        }));
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (isFinishing() || isDestroyed()) return;
                if (store.getEmail() != null && !store.getEmail().isEmpty() && store.getEmail() != null && !store.getPassword().isEmpty()) {
                    signInWithEmailAndPassword(store.getEmail(), store.getPassword());
                }
                else {
                    Notify.createPopup(Login.this, "Credentials not found.", null);
                }
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.biometricAuthentication)).setNegativeButtonText(getString(R.string.cancel)).setConfirmationRequired(false).build();
    }

    protected void checkForAutoSignIn() {

        if (!preventAutoLogin) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (googleLogin) {
                if (user != null) {
                    promptPinLogin();
                } else {
                    Log.e(TAG, "Auto sign-in failed: User not found.");
                }
            }
            else {
                if (store.getEmail() != null && !store.getEmail().isEmpty() && store.getPassword() != null && !store.getPassword().isEmpty()) {
                    signInWithEmailAndPassword(store.getEmail(), store.getPassword());
                }
                else {
                    Log.e(TAG, "Auto sign-in failed: Email or password not found.");
                }
            }
        }
        else {
            Log.d(TAG, "Auto sign-in attempt blocked.");
        }
    }

    private void checkLogin() {
        if (binding.loginPassword.getText() != null && binding.loginUsername.getText() != null) {
            TextTools.closeSoftInput(binding.loginPassword);
            if (!Tools.isValidString(binding.loginPassword, 4) || binding.loginPassword.getText() == null || binding.loginPassword.getText().toString().isEmpty()) {
                Notify.createPopup(this, getString(R.string.password_is_invalid), null);
            } else if (!Tools.isValidEmail(binding.loginUsername) || binding.loginUsername.getText() == null) {
                Notify.createPopup(this, getString(R.string.username_is_invalid), null);
            } else {
                binding.loginError.setVisibility(View.GONE);
                signInWithEmailAndPassword(binding.loginUsername.getText().toString(), binding.loginPassword.getText().toString());
            }
        }
    }

    private void signInWithEmailAndPassword(String username, String password) {
        binding.progressBar15.progressBar.setVisibility(View.VISIBLE);
        FirebaseTools.signInWithEmailAndPassword(this, username, password, wasSuccessful -> {
            if (isFinishing() || isDestroyed()) return;
            if (wasSuccessful) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                    boolean needsDownload = !repo.isStoreDataComplete();
                    repo.loginUser(username, password, firebaseUser.getUid(), false, needsDownload, (success, message) -> {
                        binding.progressBar15.progressBar.setVisibility(View.GONE);
                        if (success) {
                            googleLogin = false;
                            loadWithStoredCredentials();
                        } else {
                            Notify.createPopup(this, "Login failed: " + message + ".", null);
                        }
                    });
                }
                else {
                    if (firebaseUser != null) {
                        CustomDialog cd = new CustomDialog(this, "Verify Email", "You need to verify your email by clicking the link in the email that was sent to you.",
                                getString(R.string.ok), getString(R.string.resendEmail), "Open Email App");
                        cd.setPositiveButtonListener(v -> {
                            cd.dismissDialog();
                            FirebaseAuth.getInstance().signOut();
                            clearCredentials(this, (wasSuccessful2, message) -> store.setStaySignedIn(false));
                        });
                        cd.setNegativeButtonListener(view -> FirebaseTools.sendVerificationEmail(firebaseUser, (wasSuccessful1, message1) -> {
                            if (isFinishing() || isDestroyed()) return;
                            if (wasSuccessful1) {
                                Notify.createPopup(this, getString(R.string.verificationEmailSent), null);
                            } else {
                                Notify.createPopup(this, getString(R.string.anErrorHasOccurred), null);
                            }
                        }));
                        cd.setNeutralButtonListener(view -> Tools.openEmailApp(this));
                        cd.show();
                    }
                    else {
                        Notify.createPopup(this, "Account lookup failed. Please try again.", null);
                    }
                }
            } else {
                Notify.createPopup(this, "Login failed. Please try again.", null);
            }
        });
    }

    private void promptPinLogin() {

        if (repo.getUid() == null || repo.getUid().isEmpty()) {
            Log.e(TAG, "User ID not found.");
            return;
        }

        String pin = store.getPin();
        if (pin == null || pin.isEmpty()) {
            Log.i(TAG, "Pin has not been set. Launching pin setup");
            launchPinSetup((wasSuccessful, message) -> {
                if  (isFinishing() || isDestroyed()) return;
                if (wasSuccessful) {
                    promptPinLogin();
                }
                else {
                    Log.e(TAG, "Pin setup failed: " + message + ".");
                }
            });
        }

        if (googleLogin) {
            CustomDialog cd = new CustomDialog(this, "", getString(R.string.enter_your_pin),
                    getString(R.string.login), getString(R.string.cancel), null);
            cd.enablePinKeypad();
            cd.setPositiveButtonListener(v -> {
                String enteredPin = cd.getInput();
                if (enteredPin.equals(pin)) {
                    cd.dismissDialog();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        repo.loginUser(user.getEmail(), user.getUid(), user.getUid(), true, false, (success, message) -> {
                            if (success) {
                                loadWithStoredCredentials();
                            } else {
                                Log.e(TAG, "Auto sign-in failed: " + message + ".");
                            }
                        });
                    }
                    else {
                        Log.e(TAG, "User not found.");
                        cd.dismissDialog();
                        Notify.createPopup(this, "Session expired. Please sign in with Google again.", null);
                    }
                }
                else {
                    Notify.createDialogPopup(cd, "Incorrect PIN", null);
                }
            });
            cd.setNegativeButtonListener(v -> cd.dismissDialog());
            cd.show();
        } else {
            Log.e(TAG, "Google login not enabled. Pin login cancelled.");
        }
    }

    private void launchPinSetup (Repository.OnCompleteCallback onComplete) {

        CustomDialog cd = new CustomDialog(this, getString(R.string.set_quick_login_pin), getString(R.string.enter_a_4_6_digit_pin_for_faster_future_login), getString(R.string.save), getString(R.string.skip), null);
        cd.enablePinKeypad();
        cd.setPositiveButtonListener(v -> {
            String pin = cd.getInput();
            if (pin.length() >= 4 && pin.length() <= 6) {
                store.setPin(pin);
                cd.dismissDialog();
                onComplete.onComplete(true, null);
            } else {
                Notify.createDialogPopup(cd, getString(R.string.pin_must_be_4_6_digits), null);
            }
        });
        cd.setNegativeButtonListener(v -> {
            cd.dismissDialog();
            onComplete.onComplete(false, null);
        });
        cd.show();
    }

    private void loadWithStoredCredentials() {
        binding.progressBar15.progressBar.setVisibility(View.VISIBLE);

        String currentUid = repo.getUid();

        if (currentUid == null || currentUid.isEmpty() || FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.progressBar15.progressBar.setVisibility(View.GONE);
            Notify.createPopup(this, "No user logged in.", null);
            return;
        }

        FirebaseTools.setPhotoData();

        if (store.getNeedsDownload(repo.getUid()) || repo.getUser() == null || repo.getBills() == null || repo.getPayments() == null || repo.getExpenses() == null) {
            repo.clearDisk();
            repo.fetchCloudData(repo.getUid(), (success, msg) -> {
                if (isFinishing() || isDestroyed()) return;
                if (success) loadPartnerData();
                else {
                    binding.progressBar15.progressBar.setVisibility(View.GONE);
                    Notify.createPopup(this, "Login failed. Please check connection.", null);
                }
            });
        }
        else {
            loadPartnerData();
        }
    }

    private void loadPartnerData() {
        if (isFinishing() || isDestroyed()) return;
        User thisUser = repo.getUser();
        if (thisUser == null) {
            binding.progressBar15.progressBar.setVisibility(View.GONE);
            Notify.createPopup(this, "Error loading user profile.", null);
            return;
        }
        repo.loadPartnerData((success, message) -> finalizeDataAndLaunch());
    }

    private void finalizeDataAndLaunch() {
        store.setSignedInWithGoogle(googleLogin);
        setOwnership();
        store.setStaySignedIn(binding.staySignedIn.isChecked());
    }

    private void setOwnership() {
        String uid = repo.getUid();
        if (uid != null) {
            store.setLastUid(uid);
            if (repo.getBills() != null) for (Bill bill : repo.getBills()) if (bill.getOwner() == null) bill.setOwner(uid);
            if (repo.getPayments() != null) for (Payment payment : repo.getPayments()) if (payment.getOwner() == null) payment.setOwner(uid);
            if (repo.getExpenses() != null) for (Expense exp : repo.getExpenses()) if (exp.getOwner() == null) exp.setOwner(uid);
            launchMainActivity();
        }
        else {
            Notify.createPopup(this, "Error loading user profile.", null);
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
}
