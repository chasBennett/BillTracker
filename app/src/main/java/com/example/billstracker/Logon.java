package com.example.billstracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

public class Logon extends AppCompatActivity {

    public static ArrayList<User> userList = new ArrayList<>();
    public static ArrayList<Bill> billList = new ArrayList<>();
    public static User thisUser;
    public static PaymentInfo paymentInfo;
    public static ExpenseInfo expenseInfo;
    public static boolean checkTrophies;
    static ArrayList<Biller> billers = new ArrayList<>();
    public static String uid;
    boolean loggedIn;
    boolean biometricPreference;
    boolean alreadySignedIn;
    TextView forgotPassword, loginMessage, emailError, length, matchError, uppercase, lowercase, number, notificationsOff;
    Button expandLogin, firstRegister, login, btnRegister, ok, openEmail;
    LinearLayout titleBar, match, error, loginBox, registerBox;
    Executor executor;
    AlertDialog dialog;
    SharedPreferences sp;
    TextView errorMessage, openLogin, openRegister;
    ConstraintLayout main, pb;
    Context mContext = this;
    String TAG = MainActivity2.class.getSimpleName();
    ImageView biometric, firstLogo;
    EditText password1, username, name, email, password, pass2;
    CheckBox staySignedIn, check;
    String setName;
    BiometricManager biometricManager;
    VideoView video;
    ScrollView hiddenLogin;
    ConstraintLayout verificationSent;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkTrophies = true;
        setContentView(R.layout.activity_logon);
        login = findViewById(R.id.loginButton);
        biometric = findViewById(R.id.biometricButton);
        password1 = findViewById(R.id.etPassword);
        username = findViewById(R.id.etUserName);
        firstLogo = findViewById(R.id.firstLogo);
        name = findViewById(R.id.etWelcomeName);
        errorMessage = findViewById(R.id.errorMessage);
        video = findViewById(R.id.videoView);
        loginBox = findViewById(R.id.loginBox);
        registerBox = findViewById(R.id.registerBox);
        main = findViewById(R.id.logonLayout);
        btnRegister = findViewById(R.id.btnRegister);
        emailError = findViewById(R.id.emailError);
        verificationSent = findViewById(R.id.verificationSent);
        ok = findViewById(R.id.btnOk);
        openEmail = findViewById(R.id.btnOpenEmail);
        email = findViewById(R.id.etWelcomeEmail);
        openLogin = findViewById(R.id.openLogin);
        openRegister = findViewById(R.id.openRegister);
        notificationsOff = findViewById(R.id.notificationsTurnedOff);
        password = findViewById(R.id.etWelcomePassword);
        pass2 = findViewById(R.id.etWelcomePassword2);
        error = findViewById(R.id.passError);
        match = findViewById(R.id.match);
        length = findViewById(R.id.length);
        matchError = findViewById(R.id.matchError);
        uppercase = findViewById(R.id.uppercase);
        lowercase = findViewById(R.id.lowercase);
        number = findViewById(R.id.number);
        mContext = this.getBaseContext();
        expandLogin = findViewById(R.id.expandLogin);
        firstRegister = findViewById(R.id.firstRegister);
        hiddenLogin = findViewById(R.id.hiddenLogin);
        staySignedIn = findViewById(R.id.staySignedIn);
        loginMessage = findViewById(R.id.loginMessage);
        pb = findViewById(R.id.pb8);
        loggedIn = false;
        forgotPassword = findViewById(R.id.forgotPassword);
        check = findViewById(R.id.checkBox);
        titleBar = findViewById(R.id.titleBar);

    }

    public void loadout() {

        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()) {
            notificationsOff.setVisibility(View.VISIBLE);
        }
        else {
            notificationsOff.setVisibility(View.GONE);
        }

        getValuesFromSharedPreferences();

        addListeners();
        paymentInfo = new PaymentInfo(new ArrayList<>());

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle(getString(R.string.biometricAuthentication)).setNegativeButtonText(getString(R.string.cancel))
                .setConfirmationRequired(false).build();

        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                sp.getString("email", "");
                if (!sp.getString("email", "").equals("") && !sp.getString("password", "").equals("")) {
                    String email = sp.getString("email", "");
                    String pass = sp.getString("password", "");
                    pb.setVisibility(View.VISIBLE);
                    loginMessage.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.logging_you_in_as), email));
                    expandLogin.setVisibility(View.GONE);
                    firstRegister.setVisibility(View.GONE);
                    hiddenLogin.setVisibility(View.GONE);
                    signIn(email, pass);
                } else {
                    Toast.makeText(mContext, getString(R.string.enableAfterLoggingIn), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        if (sp.contains("biometricPreference") && biometricPreference) {
            if (BiometricManager.from(mContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS && !alreadySignedIn) {
                biometricPrompt.authenticate(promptInfo);
            }
        }
    }

    public void startVideo() {
        if (Logon.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beach2);
            video.setVideoURI(uri);
        } else {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beach4);
            video.setVideoURI(uri);
        }
        video.start();
        MediaController mediaController = new MediaController(Logon.this);
        mediaController.setAnchorView(video);
        mediaController.hide();
        video.setOnCompletionListener(mp -> video.start());
    }

    public void getValuesFromSharedPreferences() {

        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        biometricPreference = sp.getBoolean("biometricPreference", false);
        if (sp.contains("UserName") && !sp.getString("UserName", "").equals("")) {
            setName = sp.getString("name", "");
            check.setChecked(true);
            username.setText(sp.getString("UserName", ""));
        } else {
            check.setChecked(false);
        }

        if (sp.contains("Stay Signed In") && sp.getBoolean("Stay Signed In", false)) {
            alreadySignedIn = true;
            expandLogin.setVisibility(View.GONE);
            firstRegister.setVisibility(View.GONE);
            video.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);
            main.setAlpha(.3f);
            loginMessage.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.logging_you_in_as), sp.getString("Username", "")));
            signIn(sp.getString("Username", ""), sp.getString("Password", ""));
        } else {
            startVideo();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username.setText(extras.getString("UserName"));
            password.setText(extras.getString("Password"));
        }
    }

    public void addListeners() {

        final boolean[] nam = {false};
        final boolean[] pass = {false};
        final boolean[] mail = {false};

        btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.grey, getTheme())));
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                nam[0] = false;
                name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                if (name.getText().toString().length() > 0 && name.getText().toString().length() < 3) {
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.grey, getTheme())));
                } else {
                    nam[0] = true;
                    name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall, 0);
                    if (nam[0] && pass[0] && mail[0]) {
                        btnRegister.setEnabled(true);
                        btnRegister.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button, getTheme())));
                        btnRegister.setOnClickListener(v -> registerUser());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                btnRegister.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                btnRegister.setEnabled(false);
                mail[0] = false;
                email.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                if (email.getText().toString().length() == 0) {
                    emailError.setVisibility(View.INVISIBLE);
                } else if (email.getText().toString().length() < 6) {
                    emailError.setVisibility(View.VISIBLE);
                    emailError.setText(R.string.emailTooShort);
                } else if (!email.getText().toString().contains("@")
                        || !email.getText().toString().endsWith(".com") && !email.getText().toString().endsWith(".net") &&
                        !email.getText().toString().endsWith(".edu") && !email.getText().toString().endsWith(".gov")) {
                    emailError.setText(R.string.enterValidEmail);
                    emailError.setVisibility(View.VISIBLE);
                } else {
                    mAuth.fetchSignInMethodsForEmail(email.getText().toString()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean check = !Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();
                            emailError.setVisibility(View.VISIBLE);
                            if (!check) {
                                mail[0] = true;
                                emailError.setText(R.string.usernameAvailable);
                                email.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall, 0);
                                TextViewCompat.setCompoundDrawableTintList(emailError, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                                if (nam[0] && mail[0] && pass[0]) {
                                    btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.button, getTheme())));
                                    btnRegister.setEnabled(true);
                                    btnRegister.setOnClickListener(v -> registerUser());
                                }
                            } else {
                                emailError.setText(R.string.usernameUnavailable);
                                mail[0] = false;
                            }
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pass[0] = false;
                error.setVisibility(View.VISIBLE);
                btnRegister.setEnabled(false);
                btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.lightblue, getTheme())));
                password.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                if (password.getText().toString().length() == 0) {
                    error.setVisibility(View.GONE);
                    match.setVisibility(View.GONE);
                    btnRegister.setEnabled(false);
                    return;
                } else {
                    length.setText(R.string.notSixCharacters);
                }

                char ch;
                boolean length1 = false;
                boolean capital = false;
                boolean lowercase1 = false;
                boolean number1 = false;

                for (int n = 0; n < password.getText().toString().length(); n++) {
                    ch = password.getText().toString().charAt(n);
                    if (Character.isUpperCase(ch)) {
                        capital = true;
                    } else if (Character.isLowerCase(ch)) {
                        lowercase1 = true;
                    } else if (Character.isDigit(ch)) {
                        number1 = true;
                    }
                }

                if (password.getText().toString().length() < 6) {
                    length.setText(R.string.notSixCharacters);
                    length.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                    length.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    length.setText(R.string.isSixCharacters);
                    length1 = true;
                    length.setTextColor(getResources().getColor(R.color.green, getTheme()));
                    length.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    TextViewCompat.setCompoundDrawableTintList(length, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                btnRegister.setEnabled(false);
                btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.lightblue, getTheme())));
                if (!capital) {
                    uppercase.setText(R.string.noCapitalLetter);
                    uppercase.setTextColor(getTheme().getResources().getColor(R.color.blackAndWhite, getTheme()));
                    uppercase.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    uppercase.setText(R.string.isCapitalLetter);
                    uppercase.setTextColor(getResources().getColor(R.color.green, getTheme()));
                    uppercase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    TextViewCompat.setCompoundDrawableTintList(uppercase, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (!lowercase1) {
                    lowercase.setText(R.string.noLowercaseLetter);
                    lowercase.setTextColor(getTheme().getResources().getColor(R.color.blackAndWhite, getTheme()));
                    lowercase.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    lowercase.setText(R.string.isLowercaseLetter);
                    lowercase.setTextColor(getResources().getColor(R.color.green, getTheme()));
                    lowercase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    TextViewCompat.setCompoundDrawableTintList(lowercase, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (!number1) {
                    number.setText(R.string.noNumber);
                    number.setTextColor(getTheme().getResources().getColor(R.color.blackAndWhite, getTheme()));
                    number.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    number.setText(R.string.isNumber);
                    number.setTextColor(getResources().getColor(R.color.green, getTheme()));
                    number.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    TextViewCompat.setCompoundDrawableTintList(number, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (length1 && capital && lowercase1 && number1) {
                    error.setVisibility(View.GONE);
                    match.setVisibility(View.VISIBLE);
                } else {
                    error.setVisibility(View.VISIBLE);
                    pass2.setText("");
                    match.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        pass2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btnRegister.setEnabled(false);
                btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.lightblue, getTheme())));
                if (pass2.getText().length() == 0) {
                    matchError.setVisibility(View.GONE);
                } else {
                    matchError.setVisibility(View.VISIBLE);
                    matchError.setText(R.string.passwords_dont_match);
                    matchError.setTextColor(getTheme().getResources().getColor(R.color.blackAndWhite, getTheme()));
                    matchError.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (pass2.getText().toString().equals(password.getText().toString())) {
                    pass[0] = true;
                    matchError.setText(R.string.passwordsMatch);
                    matchError.setTextColor(getResources().getColor(R.color.green, getTheme()));
                    matchError.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    TextViewCompat.setCompoundDrawableTintList(matchError, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                    match.setVisibility(View.GONE);
                    password.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall, 0);
                    password.setPadding(20, 0, 20, 0);
                    TextViewCompat.setCompoundDrawableTintList(password, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(pass2.getWindowToken(), 0);

                    if (nam[0] && pass[0] && mail[0]) {
                        btnRegister.setEnabled(true);
                        btnRegister.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button, getTheme())));
                        btnRegister.requestFocus();
                        btnRegister.setOnClickListener(v -> registerUser());
                    }
                } else {
                    pass[0] = false;
                    btnRegister.setEnabled(false);
                    btnRegister.setBackgroundTintList(ColorStateList.valueOf(getTheme().getResources().getColor(R.color.lightblue, getTheme())));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /////////Login View/////////\/\/\/////

        forgotPassword.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            main.setAlpha(.3f);
            loginMessage.setText(R.string.forgot_password);
            Intent fp = new Intent(mContext, ForgotPassword.class);
            fp.putExtra("UserName", username.getText());
            startActivity(fp);
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorMessage.setText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        expandLogin.setOnClickListener(view -> {
            hiddenLogin.setVisibility(View.VISIBLE);
            expandLogin.setVisibility(View.GONE);
            firstRegister.setVisibility(View.GONE);
            video.setVisibility(View.GONE);
            registerBox.setVisibility(View.GONE);
            loginBox.setVisibility(View.VISIBLE);
            openLogin.setBackground(null);
            openRegister.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
        });

        openRegister.setOnClickListener(v -> {
            loginBox.setVisibility(View.GONE);
            registerBox.setVisibility(View.VISIBLE);
            openRegister.setBackground(null);
            openLogin.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
        });

        openLogin.setOnClickListener(v -> {
            registerBox.setVisibility(View.GONE);
            loginBox.setVisibility(View.VISIBLE);
            openLogin.setBackground(null);
            openRegister.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorMessage.setText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        firstRegister.setOnClickListener(v -> {
            hiddenLogin.setVisibility(View.VISIBLE);
            expandLogin.setVisibility(View.GONE);
            firstRegister.setVisibility(View.GONE);
            video.setVisibility(View.GONE);
            loginBox.setVisibility(View.GONE);
            registerBox.setVisibility(View.VISIBLE);
            openRegister.setBackground(null);
            openLogin.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
        });

        username.setOnKeyListener((view, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(username.getWindowToken(), 0);
                password.requestFocus();
            }
            return false;
        });

        password1.setOnKeyListener((view, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(password1.getWindowToken(), 0);
                if (password1.getText().length() > 6 && username.getText().length() > 6) {
                    SharedPreferences.Editor editor = sp.edit();
                    if (check.isChecked()) {
                        editor.putString("UserName", username.getText().toString());
                    } else {
                        editor.putString("UserName", "");
                    }
                    if (staySignedIn.isChecked()) {
                        editor.putBoolean("Stay Signed In", true);
                        editor.putString("Username", username.getText().toString());
                        editor.putString("Password", password1.getText().toString());
                    } else {
                        editor.putBoolean("Stay Signed In", false);
                    }
                    editor.apply();
                    pb.setVisibility(View.VISIBLE);
                    main.setAlpha(.3f);
                    loginMessage.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.logging_you_in_as), username.getText().toString()));
                    signIn(username.getText().toString(), password1.getText().toString());
                } else {
                    pb.setVisibility(View.GONE);
                    main.setAlpha(1f);
                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText(R.string.username_or_password_too_short);
                    errorMessage.setTextColor(getResources().getColor(R.color.fingerprint, getTheme()));
                }
            }
            return false;
        });

        login.setOnClickListener(view -> {

            if (password1.getText().length() > 2 && username.getText().length() > 2) {
                pb.setVisibility(View.VISIBLE);
                main.setAlpha(.3f);
                loginMessage.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.logging_you_in_as), username.getText().toString()));

                SharedPreferences.Editor editor = sp.edit();
                if (check.isChecked()) {
                    editor.putString("UserName", username.getText().toString());
                } else {
                    editor.putString("UserName", "");
                }
                if (staySignedIn.isChecked()) {
                    editor.putBoolean("Stay Signed In", true);
                    editor.putString("Username", username.getText().toString());
                    editor.putString("Password", password1.getText().toString());
                } else {
                    editor.putBoolean("Stay Signed In", false);
                }
                editor.apply();
                signIn(username.getText().toString(), password1.getText().toString());
            } else {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(R.string.username_or_password_too_short);
                errorMessage.setTextColor(getResources().getColor(R.color.fingerprint, getTheme()));
                pb.setVisibility(View.GONE);
                main.setAlpha(1f);
            }
        });

        biometric.setOnClickListener(view -> {

            biometricManager = BiometricManager.from(mContext);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
                Toast.makeText(mContext, getString(R.string.biometricsNotSupported), Toast.LENGTH_SHORT).show();
                return;
            }

            if (sp.contains("biometricPreference")) {
                if (!biometricPreference) {
                    Toast.makeText(mContext, getString(R.string.enableAfterLoggingIn), Toast.LENGTH_LONG).show();
                } else {
                    biometricManager = BiometricManager.from(mContext);
                    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
                        Toast.makeText(mContext, getString(R.string.biometricsNotSupported), Toast.LENGTH_SHORT).show();
                    } else {
                        biometricPrompt.authenticate(promptInfo);
                    }
                }
            } else {
                Toast.makeText(mContext, getString(R.string.enableAfterLoggingIn), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerUser() {

        pb.setVisibility(View.VISIBLE);
        loginMessage.setText(R.string.creating_user_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String lastLogin = "01-01-2022";

        String email1 = email.getText().toString().toLowerCase(Locale.getDefault());
        String password1 = password.getText().toString();
        String name1 = name.getText().toString();

        LocalDateTime loginTime = LocalDateTime.now();
        DateTimeFormatter formattedLoginTime = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

        String dateRegistered = loginTime.format(formattedLoginTime);
        mAuth.createUserWithEmailAndPassword(email1, password1).addOnCompleteListener(task -> {
            Log.d("TAG", "createUserWithEmail:onComplete:" + task.isSuccessful());
            if (!task.isSuccessful()) {
                pb.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.emailAlreadyRegistered),
                        Toast.LENGTH_SHORT).show();
            } else {
                String userId = mAuth.getUid();
                ArrayList<Bill> bills = new ArrayList<>();
                ArrayList<Trophy> trophies = new ArrayList<>();
                ArrayList<Budgets> budgets = new ArrayList<>();
                User user1 = new User(email1, password1, name1, false, lastLogin, dateRegistered, userId, bills, 0, "0", 0, "2", false, "01-01-2022", trophies, budgets);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                if (userId != null) {
                    db.collection("users").document(userId).set(user1);
                }
                else {
                    Toast.makeText(Logon.this, getString(R.string.anErrorHasOccurred), Toast.LENGTH_SHORT).show();
                    recreate();
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                UserProfileChangeRequest update = new UserProfileChangeRequest.Builder().setDisplayName(name1).build();
                assert user != null;
                SharedPreferences sp = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("registered", true);
                editor.putString("email", email1);
                editor.putString("password", password1);
                editor.apply();
                user.updateProfile(update);
                Logon.userList.add(user1);
                sendVerificationEmail(user);
            }
        });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pb.setVisibility(View.GONE);
                        verificationSent.setVisibility(View.VISIBLE);
                        ok.setOnClickListener(v -> {
                            pb.setVisibility(View.VISIBLE);
                            loginMessage.setText(R.string.finishing_up);

                            SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("First Logon", true);
                            editor.apply();
                            FirebaseAuth.getInstance().signOut();
                            verificationSent.setVisibility(View.GONE);
                            hiddenLogin.setVisibility(View.VISIBLE);
                            openRegister.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
                            openLogin.setBackground(null);
                            registerBox.setVisibility(View.GONE);
                            loginBox.setVisibility(View.VISIBLE);
                            pb.setVisibility(View.GONE);
                        });
                        openEmail.setOnClickListener(v -> {
                            FirebaseAuth.getInstance().signOut();
                            SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("First Logon", true);
                            editor.apply();
                            Intent email = new Intent(Intent.ACTION_MAIN);
                            email.addCategory(Intent.CATEGORY_APP_EMAIL);
                            email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(email);
                            editor.putBoolean("First Logon", true);
                            editor.apply();
                            verificationSent.setVisibility(View.GONE);
                            hiddenLogin.setVisibility(View.VISIBLE);
                            openRegister.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
                            openLogin.setBackground(null);
                            registerBox.setVisibility(View.GONE);
                            loginBox.setVisibility(View.VISIBLE);

                        });
                    } else {
                        pb.setVisibility(View.GONE);
                        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("First Logon", true);
                        editor.apply();
                        Toast.makeText(getApplicationContext(), getString(R.string.anErrorHasOccurred),
                                Toast.LENGTH_SHORT).show();
                        overridePendingTransition(0, 0);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());

                    }
                });
    }

    public void checkBiometricPreference(String user) {

        SharedPreferences.Editor editor = sp.edit();
        boolean allowBiometricPrompt = true;
        boolean match = true;
        boolean biometricEligible = true;
        if (sp.contains("allowBiometricPrompt")) {
            allowBiometricPrompt = sp.getBoolean("allowBiometricPrompt", true);
        }

        biometricManager = BiometricManager.from(mContext);

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            biometricEligible = false;
            biometric.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.neutralGray, getTheme())));
        }

        if (allowBiometricPrompt && biometricEligible) {

            if (!biometricPreference || !match) {
                pb.setVisibility(View.GONE);
                main.setAlpha(1f);
                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(Logon.this);
                builder.setMessage(getString(R.string.willYouEnableBiometrics)).setTitle(getString(R.string.enableBiometrics1)).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    editor.putBoolean("biometricPreference", true);
                    editor.putBoolean("allowBiometricPrompt", true);
                    editor.putString("email", user);
                    editor.apply();
                    launchMainActivity();

                });
                builder.setNegativeButton(getString(R.string.notRightNow), (dialogInterface, i) -> {
                    dialog.dismiss();
                    launchMainActivity();
                });
                builder.setNeutralButton(getString(R.string.dontAskAgain), (dialogInterface, i) -> {
                    editor.putBoolean("biometricPreference", false);
                    editor.putBoolean("allowBiometricPrompt", false);
                    editor.putString("email", "");
                    editor.putString("password", "");
                    editor.apply();
                    androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                    builder1.setMessage(getString(R.string.biometricsAreDisabled))
                            .setTitle(getString(R.string.biometricsDisabled)).setPositiveButton(getString(R.string.ok), (dialogInterface1, i1) -> launchMainActivity());
                    androidx.appcompat.app.AlertDialog alert = builder1.create();
                    alert.show();
                });
                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
            } else {
                launchMainActivity();
            }

        } else {
            launchMainActivity();
        }
    }

    public void signIn(String username, String password) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        checkIfEmailVerified(username);
                        uid = mAuth.getUid();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());

                        hiddenLogin.setVisibility(View.VISIBLE);
                        errorMessage.setVisibility(View.VISIBLE);
                        errorMessage.setText(R.string.invalidLogin);
                        errorMessage.setTextColor(getResources().getColor(R.color.fingerprint, getTheme()));
                        pb.setVisibility(View.GONE);
                        main.setAlpha(1f);
                    }
                });
    }

    public boolean checkIfEmailRegistered(String email) {
        final boolean[] found = {false};
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                found[0] = !Objects.requireNonNull(task.getResult().getSignInMethods()).isEmpty();
            }
        });
        return found[0];
    }

    private void checkIfEmailVerified(String username) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        if (user.isEmailVerified()) {
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("registered", true);
            edit.apply();
            load(username);
        } else {
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("Stay Signed In", false);
            edit.apply();
            pb.setVisibility(View.GONE);
            main.setAlpha(1f);
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Logon.this);
            builder.setMessage(getString(R.string.verifyEmail)).setTitle(getString(R.string.emailNotVerified)).setPositiveButton(getString
                    (R.string.resendEmail), (dialogInterface, i) ->
                    user.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(mContext, getString(R.string.verificationEmailSent),
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(mContext, getString(R.string.anErrorHasOccurred),
                                            Toast.LENGTH_SHORT).show();
                                    recreate();

                                }
                                pb.setVisibility(View.GONE);
                            }));

            builder.setNegativeButton(getString(R.string.ok), (dialogInterface, i) -> FirebaseAuth.getInstance().signOut());
            androidx.appcompat.app.AlertDialog alert = builder.create();
            alert.show();

        }
    }

    public void load(String userName) {

        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (thisUser == null) {
            thisUser = new User();
        }
        if (paymentInfo == null) {
            paymentInfo = new PaymentInfo();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userName.toLowerCase()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    Login oldUser = doc.toObject(Login.class);
                    ArrayList <Bill> billsList = new ArrayList<>();
                    if (oldUser != null && oldUser.getBills() != null) {
                        if (oldUser.getBills().size() > 0) {
                            for (Bills bills : oldUser.getBills()) {
                                Bill bill = new Bill(bills.getBillerName(), bills.getAmountDue(), bills.getDayDue(), bills.getDateLastPaid(), bills.getBillsId(), bills.isRecurring(), bills.getFrequency(), bills.getWebsite(), bills.getCategory(),
                                        bills.getIcon(), bills.getPaymentsRemaining(), bills.getBalance(), bills.getEscrow());
                                billsList.add(bill);
                            }
                        }
                        if (oldUser.getExpenses() != null) {
                            if (oldUser.getExpenses().size() > 0) {
                                ExpenseInfo expenses = new ExpenseInfo(oldUser.getExpenses());
                                db.collection("expenses").document(uid).set(expenses);
                            }
                        }
                    }
                    else {
                        assert oldUser != null;
                        oldUser.setBills(new ArrayList<>());
                    }
                    User newUser = new User(oldUser.getUserName(), oldUser.getPassword(), oldUser.getName(), oldUser.getAdmin(), oldUser.getLastLogin(), oldUser.getDateRegistered(), oldUser.getid(), billsList, oldUser.getTotalLogins(),
                            oldUser.getTicketNumber(), oldUser.getIncome(), oldUser.getPayFrequency(), oldUser.isTermsAccepted(), oldUser.getTermsAcceptedOn(), oldUser.getTrophies(), oldUser.getBudgets());
                    db.collection("users").document(uid).set(newUser);
                    db.collection("users").document(userName.toLowerCase()).delete();
                }
                FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                db1.collection("users").document(uid).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot doc1 = task1.getResult();
                        if (doc1.exists()) {
                            thisUser = doc1.toObject(User.class);
                        }
                        db1.collection("payments").document(uid).get().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {
                                DocumentSnapshot doc11 = task11.getResult();
                                if (doc11.exists()) {
                                    paymentInfo = doc11.toObject(PaymentInfo.class);
                                } else {
                                    paymentInfo = new PaymentInfo(new ArrayList<>());
                                }
                            }
                            db1.collection("expenses").document(uid).get().addOnCompleteListener(task111 -> {
                                if (task111.isSuccessful()) {
                                    DocumentSnapshot doc11 = task111.getResult();
                                    if (doc11.exists()) {
                                        expenseInfo = doc11.toObject(ExpenseInfo.class);
                                    } else {
                                        expenseInfo = new ExpenseInfo(new ArrayList<>());
                                    }
                                    if (paymentInfo == null) {
                                        paymentInfo = new PaymentInfo(new ArrayList<>());
                                    }
                                    if (paymentInfo.getPayments() == null) {
                                        paymentInfo.setPayments(new ArrayList<>());
                                    }
                                    if (expenseInfo == null) {
                                        expenseInfo = new ExpenseInfo(new ArrayList<>());
                                    }
                                    if (expenseInfo.getExpenses() == null) {
                                        expenseInfo.setExpenses(new ArrayList<>());
                                    }
                                    updateUI();
                                }
                            });
                        });
                    }
                });
            }
        });
    }

    private void updateUI() {

        DateFormatter df = new DateFormatter();
        String loginTime = df.createCurrentDateStringWithTime();
        if (thisUser == null) {
            Toast.makeText(mContext, getString(R.string.anErrorHasOccurred), Toast.LENGTH_SHORT).show();
            recreate();
            finish();
        }
        thisUser.setLastLogin(loginTime);
        if (thisUser.getBills() == null) {
            thisUser.setBills(new ArrayList<>());
        }
        if (thisUser.getTrophies() == null) {
            thisUser.setTrophies(new ArrayList<>());
        }
        if (thisUser.getBudgets() == null) {
            thisUser.setBudgets(new ArrayList<>());
        }
        for (Bill bills : thisUser.getBills()) {
            switch (bills.getCategory()) {
                case "Auto Loan":
                    bills.setCategory("0");
                    break;
                case "Credit Card":
                    bills.setCategory("1");
                    break;
                case "Entertainment":
                    bills.setCategory("2");
                    break;
                case "Insurance":
                    bills.setCategory("3");
                    break;
                case "Miscellaneous":
                    bills.setCategory("4");
                    break;
                case "Mortgage":
                    bills.setCategory("5");
                    break;
                case "Personal Loan":
                case "Personal Loans":
                    bills.setCategory("6");
                    break;
                case "Utilities":
                    bills.setCategory("7");
                    break;
            }
            switch (bills.getFrequency()) {
                case "Daily":
                    bills.setFrequency("0");
                    break;
                case "Weekly":
                    bills.setFrequency("1");
                    break;
                case "Bi-Weekly":
                    bills.setFrequency("2");
                    break;
                case "Monthly":
                    bills.setFrequency("3");
                    break;
                case "Quarterly":
                    bills.setFrequency("4");
                    break;
                case "Yearly":
                    bills.setFrequency("5");
                    break;
            }
            if (bills.getAmountDue().contains("$")) {
                bills.setAmountDue(bills.getAmountDue().replaceAll("\\$", ""));
            }
            if (bills.getIcon() == null) {
                bills.setIcon("fixMe");
            }
            for (Payments payment : paymentInfo.getPayments()) {
                if (payment.getPaymentAmount().contains("$")) {
                    payment.setPaymentAmount(payment.getPaymentAmount().replaceAll("\\$", ""));
                }
            }
            if (bills.getPaymentsRemaining() == null) {
                bills.setPaymentsRemaining("1000");
            }
        }
        if (thisUser.getPayFrequency() != null) {
            switch (thisUser.getPayFrequency()) {
                case "Weekly":
                    thisUser.setPayFrequency("0");
                    break;
                case "Bi-Weekly":
                    thisUser.setPayFrequency("1");
                    break;
                case "Monthly":
                    thisUser.setPayFrequency("2");
                    break;
            }
        }
        else {
            thisUser.setPayFrequency("2");
        }
        if (Integer.parseInt(thisUser.getPayFrequency()) > 2) {
            thisUser.setPayFrequency(String.valueOf(2));
        }
        thisUser.setTotalLogins(thisUser.getTotalLogins() + 1);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("email", thisUser.getUserName());
        editor.putString("name", thisUser.getName());
        editor.putString("password", thisUser.getPassword());
        editor.apply();
        checkBiometricPreference(thisUser.getUserName());

    }

    private void launchMainActivity() {

        pb.setVisibility(View.GONE);
        main.setAlpha(1f);
        hiddenLogin.setVisibility(View.GONE);
        titleBar.setVisibility(View.GONE);
        firstRegister.setVisibility(View.GONE);
        expandLogin.setVisibility(View.GONE);
        video.setVisibility(View.GONE);
        if (!thisUser.isTermsAccepted()) {
            Intent terms = new Intent(Logon.this, TermsAndConditions.class);
            terms.putExtra("Type", "Existing User");
            startActivity(terms);
        } else {
            Intent home = new Intent(mContext, MainActivity2.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            home.putExtra("Name", thisUser.getName());
            home.putExtra("UserName", thisUser.getUserName());
            home.putExtra("Logged In", true);
            startActivity(home);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        startVideo();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadout();
        hiddenLogin.setVisibility(View.GONE);
        expandLogin.setVisibility(View.VISIBLE);
        firstRegister.setVisibility(View.VISIBLE);
        video.setVisibility(View.VISIBLE);
        startVideo();
    }
}