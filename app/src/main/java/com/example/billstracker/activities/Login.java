package com.example.billstracker.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
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
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.databinding.ActivityLoginBinding;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Google;
import com.example.billstracker.tools.Repo;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private boolean starting;
    private boolean biometricEligible;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private BiometricManager biometricManager;
    private boolean googleLogin;
    private final ActivityResultLauncher<String> launcher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
            }
    );
    private ActivityLoginBinding binding;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar15.progressBar.setVisibility(View.GONE);
        biometricManager = BiometricManager.from(Login.this);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometricAuthentication))
                .setNegativeButtonText(getString(R.string.cancel))
                .setConfirmationRequired(false)
                .build();

        Tools.fixProgressBarLogo(binding.progressBar15.progressBar);
        googleLogin = false;
        starting = false;
        biometricEligible = true;

        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()) Tools.requestPermissionLauncher(Login.this, launcher);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("Open Email", false)) {
                Tools.openEmailApp(Login.this);
            }
            else if (extras.getBoolean("Deleted")) {
                Notify.createPopup(Login.this, getString(R.string.profileDeletedSuccessfully), null);
                binding.loginUsername.setText("");
                Repo.getInstance().clearLoginCredentials(Login.this);
            }
            getIntent().getExtras().clear();
        }

        Repo.getInstance().initialize(Login.this);

        binding.staySignedIn.setChecked(Repo.getInstance().getStaySignedIn(Login.this));

        Tools.setupUI(Login.this, findViewById(android.R.id.content));

        addListeners();

        biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(Login.this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (Repo.getInstance().getSavedEmail(Login.this) != null && Repo.getInstance().getSavedPassword(Login.this) != null) {
                    signInWithEmailAndPassword(Repo.getInstance().getSavedEmail(Login.this), Repo.getInstance().getSavedPassword(Login.this));
                }
                else {
                    Notify.createPopup(Login.this, "Biometric login failed", null);
                }
            }

        });

        binding.googleButton.setOnClickListener(view -> Google.launchGoogleSignIn(Login.this, (wasSuccessful, user, token) -> {
            if (wasSuccessful && user != null) {
                Repo.getInstance().setUid(user.getUid(), Login.this);
                googleLogin = true;
                load();
            }
        }));

    }

    protected void addListeners() {

        binding.loginError.setText("");

        binding.forgotPassword.setOnClickListener(view -> startActivity(new Intent(Login.this, ForgotPassword.class)));

        binding.createAccount.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));

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

        binding.loginButton.setOnClickListener(view -> {
            googleLogin = false;
            checkLogin();
            TextTools.closeSoftInput(binding.loginPassword);
        });

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            biometricEligible = false;
            TextViewCompat.setCompoundDrawableTintList(binding.biometricButton, ColorStateList.valueOf(getResources().getColor(R.color.neutralGray, getTheme())));
            binding.biometricButton.setTextColor(getResources().getColor(R.color.neutralGray, getTheme()));
        }

        binding.biometricButton.setOnClickListener(view -> {

            biometricManager = BiometricManager.from(Login.this);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
                Notify.createPopup(Login.this, getString(R.string.biometricsNotSupported), null);
                return;
            }

            if (!Repo.getInstance().getAllowBiometrics(Login.this)) {
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
        if (!Tools.isValidString(binding.loginPassword, 4) || binding.loginPassword.getText() == null) {
            Notify.createPopup(Login.this, getString(R.string.password_is_invalid), null);
        }
        else if (!Tools.isValidEmail(binding.loginUsername) || binding.loginUsername.getText() == null) {
            Notify.createPopup(Login.this, getString(R.string.username_is_invalid), null);
        }
        else {
            binding.loginError.setVisibility(View.GONE);
            signInWithEmailAndPassword(binding.loginUsername.getText().toString(), binding.loginPassword.getText().toString());
        }
    }

    protected void signInWithEmailAndPassword(String username, String password) {

        binding.progressBar15.progressBar.setVisibility(View.VISIBLE);
        Repo.getInstance().loadLocalData(Login.this);
        FirebaseTools.signInWithEmailAndPassword(Login.this, username, password, wasSuccessful -> {
            if (wasSuccessful) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                boolean isGoogleUser = false;

                if (firebaseUser != null) {
                    for (UserInfo info : firebaseUser.getProviderData()) {
                        if (info.getProviderId().equals("google.com")) {
                            isGoogleUser = true;
                            break;
                        }
                    }
                }
                if (isGoogleUser || firebaseUser != null) {
                    if (isGoogleUser || firebaseUser.isEmailVerified()) {
                        Repo.getInstance().saveCredentials(Login.this, username, password);
                        Repo.getInstance().setStaySignedIn(binding.staySignedIn.isChecked(), Login.this);
                        load();
                    } else {
                        binding.progressBar15.progressBar.setVisibility(View.GONE);
                        Notify.createPopup(Login.this, "Please verify your email.", null);
                    }
                }
                else {
                    binding.progressBar15.progressBar.setVisibility(View.GONE);
                    Notify.createPopup(Login.this, "An error occurred", null);
                }
            }
        });
    }
    protected void load() {
        if (isLoading) return; // Prevent multiple clicks/calls
        isLoading = true;

        binding.progressBar15.progressBar.setVisibility(View.VISIBLE);
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            Repo.getInstance().setUid(currentUid, this);
            Repo.getInstance().fetchCloudData(currentUid, (success, msg) -> {
                if (success) {
                    loadPartnerData();
                } else {
                    isLoading = false;
                    binding.progressBar15.progressBar.setVisibility(View.GONE);
                    Notify.createPopup(this, "Login failed. Please check connection.", null);
                }
            });
        }
    }
    protected void loadPartnerData() {
        User thisUser = Repo.getInstance().getUser(Login.this);

        if (thisUser == null || thisUser.getName() == null) {
            binding.progressBar15.progressBar.setVisibility(View.GONE);
            Notify.createPopup(Login.this, "Error loading user profile.", null);
            return;
        }

        thisUser.setRegisteredWithGoogle(googleLogin);

        if (thisUser.getPartners() == null) {
            thisUser.setPartners(new ArrayList<>());
        }

        if (!thisUser.getPartners().isEmpty()) {
            final int totalPartners = thisUser.getPartners().size();
            final int[] loadedPartners = {0};

            for (Partner partner : thisUser.getPartners()) {
                FirebaseTools.getPartner(partner, wasSuccessful -> {
                    if (wasSuccessful) {
                        Repo.getInstance().loadPartnerData(partner.getPartnerUid(), (wasSuccessful1, message) -> {
                            loadedPartners[0]++;
                            if (loadedPartners[0] == totalPartners) {
                                finalizeDataAndLaunch();
                            }
                        });
                    } else {
                        loadedPartners[0]++;
                        if (loadedPartners[0] == totalPartners) {
                            finalizeDataAndLaunch();
                        }
                    }
                });
            }
        } else {
            finalizeDataAndLaunch();
        }
    }

    private void finalizeDataAndLaunch() {
        setOwnership();
        Repo.getInstance().saveLocalData(Login.this);
        checkBiometricPreference();
    }

    public void setOwnership () {
        if (Repo.getInstance().getUid() != null) {
            if (Repo.getInstance().getBills() != null && !Repo.getInstance().getBills().isEmpty()) {
                for (Bill bill : Repo.getInstance().getBills()) {
                    if (bill.getOwner() == null) {
                        bill.setOwner(Repo.getInstance().getUid());
                    }
                }
            }
            if (Repo.getInstance().getPayments() != null && !Repo.getInstance().getPayments().isEmpty()) {
                for (Payment payment: Repo.getInstance().getPayments()) {
                    if (payment.getOwner() == null) {
                        payment.setOwner(Repo.getInstance().getUid());
                    }
                }
            }
            if (Repo.getInstance().getExpenses() != null && !Repo.getInstance().getExpenses().isEmpty()) {
                for (Expense expense: Repo.getInstance().getExpenses()) {
                    if (expense.getOwner() == null) {
                        expense.setOwner(Repo.getInstance().getUid());
                    }
                }
            }
        }
    }

    protected void checkBiometricPreference() {

        Repo.getInstance().setStaySignedIn(binding.staySignedIn.isChecked(), Login.this);

        if (!Repo.getInstance().getAllowBiometrics(Login.this) && biometricEligible && Repo.getInstance().getShowBiometricPrompt(Login.this)) {
            CustomDialog cd = new CustomDialog(Login.this, getString(R.string.enableBiometrics1), getString(R.string.willYouEnableBiometrics), getString(R.string.yes), getString(R.string.notRightNow),
                    getString(R.string.dontAskAgain));
            cd.setPositiveButtonListener(v -> {
                Repo.getInstance().setAllowBiometrics(true, Login.this);
                User user = Repo.getInstance().getUser(Login.this);

                if (user.getRegisteredWithGoogle()) {
                    String email = user.getUserName();
                    String password = user.getId();

                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (firebaseUser != null) {
                        firebaseUser.linkWithCredential(credential).addOnCompleteListener(task -> {
                            Repo.getInstance().saveCredentials(Login.this, email, password);
                            finishBiometricSetup(cd);
                        });
                    }
                } else {
                    Repo.getInstance().saveCredentials(Login.this, user.getUserName(), user.getPassword());
                    finishBiometricSetup(cd);
                }
            });
            cd.setNeutralButtonListener(v -> {
                Repo.getInstance().setAllowBiometrics(false, Login.this);
                Repo.getInstance().setShowBiometricPrompt(false, Login.this);
                Notify.createPopup(Login.this, getString(R.string.biometricsAreDisabled), null);
                launchMainActivity();
            });
            cd.setNegativeButtonListener(v -> {
                Repo.getInstance().setAllowBiometrics(false, Login.this);
                Repo.getInstance().setShowBiometricPrompt(true, Login.this);
                cd.dismissDialog();
                launchMainActivity();
            });
        } else {
            launchMainActivity();
        }
    }

    private void finishBiometricSetup(CustomDialog cd) {
        Repo.getInstance().setShowBiometricPrompt(false, Login.this);
        cd.dismissDialog();
        launchMainActivity();
    }

    protected void launchMainActivity() {
        if (!starting) {
            String pendingPaymentId = getIntent().getStringExtra("paymentId");
            starting = true;
            if (pendingPaymentId != null) {
                Intent payIntent = new Intent(this, PayBill.class);
                payIntent.putExtra("paymentId", pendingPaymentId);
                startActivity(payIntent);
            }
            else {
                Intent home = new Intent(Login.this, MainActivity2.class);
                // Clear flags ensure we don't come back to Login on "Back" press
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
            }
            finish(); // Explicitly finish Login
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && Repo.getInstance().getStaySignedIn(this)) {
            if (!isLoading && !starting) {
                load();
            }
        }
    }
}