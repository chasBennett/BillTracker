package com.example.billstracker;

import static android.content.ContentValues.TAG;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.core.widget.TextViewCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {

    String name;
    String username;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Context mContext = this;
    boolean firstScan, darkMode, biometricPreference;
    EditText newUserName, newName, newPassword, confirm;
    TextView usernameTooShort, passwordTooShort, noUppercase, noLowercase, noNumber, nameTooShort, match, err;
    int nightModeFlags;
    LinearLayout hideError, passRequirements, pb;
    TextView submit;
    SwitchCompat biometricSwitch2;
    ImageView backEditProfile;
    com.google.android.material.imageview.ShapeableImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        darkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        pb = findViewById(R.id.pb6);
        err = findViewById(R.id.showFrequency);
        icon = findViewById(R.id.editProfileIcon);
        match = findViewById(R.id.textView25);
        submit = findViewById(R.id.btnSubmitUser);
        newName = findViewById(R.id.etEditName);
        confirm = findViewById(R.id.etEditPassword1);
        noNumber = findViewById(R.id.noNumber);
        hideError = findViewById(R.id.hideError);
        newUserName = findViewById(R.id.etEditUsername);
        newPassword = findViewById(R.id.etEditPassword);
        noUppercase = findViewById(R.id.noUppercase);
        noLowercase = findViewById(R.id.noLowercase);
        nameTooShort = findViewById(R.id.nameTooShort);
        usernameTooShort = findViewById(R.id.userNameTooShort);
        passwordTooShort = findViewById(R.id.passwordTooShort);
        passRequirements = findViewById(R.id.passRequirements);
        biometricSwitch2 = findViewById(R.id.biometricSwitch2);
        backEditProfile = findViewById(R.id.backEditProfile);

        sp = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        biometricPreference = sp.getBoolean("biometricPreference", false);
        editor = sp.edit();

        SaveUserData load = new SaveUserData();
        load.loadUserData(EditProfile.this);

        Glide.with(EditProfile.this).load(R.drawable.daco_3157628).optionalCircleCrop().optionalFitCenter().into(icon);

        biometricSwitch2.setChecked(biometricPreference);
        firstScan = false;

        backEditProfile.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            onBackPressed();
        });

        BiometricManager biometricManager = BiometricManager.from(mContext);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            biometricSwitch2.setClickable(false);
            biometricSwitch2.setChecked(false);
        } else {
            biometricSwitch2.setClickable(true);
            err.setVisibility(View.GONE);
        }

        biometricSwitch2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                editor.putBoolean("biometricPreference", true);
                editor.putBoolean("allowBiometricPrompt", true);
                editor.putString("email", thisUser.getUserName());
                editor.putString("password", thisUser.getPassword());
            } else {
                editor.putBoolean("biometricPreference", false);
                editor.putBoolean("allowBiometricPrompt", false);
                editor.putString("email", "");
                editor.putString("password", "");
            }
            editor.apply();
        });

        personalize();

        newName.setText(thisUser.getName());
        newUserName.setText(thisUser.getUserName());
        newPassword.setText(thisUser.getPassword());

        final boolean[] userName = {true}, firstPassword = {true}, name = {true}, password = {true};

        newUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                submit.setEnabled(false);
                userName[0] = false;
                    if (newUserName.getText().toString().length() < 1 && newUserName.getText().toString().length() > 0) {
                        submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                        usernameTooShort.setVisibility(View.VISIBLE);
                        usernameTooShort.setText(R.string.tooShort);
                    } else if (newUserName.getText().toString().length() == 0) {
                        submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                        usernameTooShort.setVisibility(View.GONE);
                    } else if (!newUserName.getText().toString().contains("@") || !newUserName.getText().toString().contains(".com") && !newUserName.getText().toString().contains(".edu") &&
                            !newUserName.getText().toString().contains(".net") && !newUserName.getText().toString().contains(".gov") && !newUserName.getText().toString().contains(".org")) {
                        submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                        usernameTooShort.setVisibility(View.VISIBLE);
                        usernameTooShort.setText(R.string.enterAValidEmailAddress);
                    } else {
                        usernameTooShort.setVisibility(View.GONE);
                        userName[0] = true;
                        if (firstPassword[0] && name[0] && password[0]) {
                            submit.setTextColor(getResources().getColor(R.color.white, getTheme()));
                            submit.setEnabled(true);
                            submit.setOnClickListener(v -> submit());
                        }
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                submit.setEnabled(false);
                firstPassword[0] = false;
                if (!firstScan) {
                    firstScan = true;
                }
                else {
                    passRequirements.setVisibility(View.VISIBLE);
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                    hideError.setVisibility(View.VISIBLE);
                }

                char ch;
                boolean length1 = false;
                boolean capital = false;
                boolean lowercase1 = false;
                boolean number1 = false;

                for (int n = 0; n < newPassword.getText().toString().length(); n++) {
                    ch = newPassword.getText().toString().charAt(n);
                    if (Character.isUpperCase(ch)) {
                        capital = true;
                    } else if (Character.isLowerCase(ch)) {
                        lowercase1 = true;
                    } else if (Character.isDigit(ch)) {
                        number1 = true;
                    }
                }

                if (newPassword.getText().toString().length() < 6) {
                    passwordTooShort.setVisibility(View.VISIBLE);
                    passwordTooShort.setText(R.string.notSixCharacters);
                    passwordTooShort.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                } else {
                    passwordTooShort.setText(R.string.isSixCharacters);
                    length1 = true;
                    passwordTooShort.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall,0,0,0);
                    TextViewCompat.setCompoundDrawableTintList(passwordTooShort, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (!capital) {
                    noUppercase.setVisibility(View.VISIBLE);
                    noUppercase.setText(R.string.noUppercaseLetter);
                    noUppercase.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                } else {
                    noUppercase.setText(R.string.isUppercaseLetter);
                    noUppercase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall,0,0,0);
                    TextViewCompat.setCompoundDrawableTintList(noUppercase, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (!lowercase1) {
                    noLowercase.setVisibility(View.VISIBLE);
                    noLowercase.setText(R.string.notOneLowercase);
                    noLowercase.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                } else {
                    noLowercase.setText(R.string.isOneLowercase);
                    noLowercase.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall,0,0,0);
                    TextViewCompat.setCompoundDrawableTintList(noLowercase, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (!number1) {
                    noNumber.setVisibility(View.VISIBLE);
                    noNumber.setText(R.string.notOneNumber);
                    noNumber.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                } else {
                    noNumber.setText(R.string.isOneNumber);
                    noNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall,0,0,0);
                    TextViewCompat.setCompoundDrawableTintList(noNumber, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                }
                if (length1 && capital && lowercase1 && number1) {
                    firstPassword[0] = true;
                    passRequirements.setVisibility(View.GONE);
                    if (userName[0] && name[0] && password[0]) {

                        submit.setTextColor(getResources().getColor(R.color.white, getTheme()));
                        submit.setEnabled(true);
                        submit.setOnClickListener(v -> submit());
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                submit.setEnabled(false);
                password[0] = false;
                if (!confirm.getText().toString().equals(newPassword.getText().toString())) {
                    password[0] = false;
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                    match.setVisibility(View.VISIBLE);
                    match.setTextColor(getTheme().getResources().getColor(R.color.grey, getTheme()));
                }
                else {
                    password[0] = true;
                    match.setVisibility(View.GONE);
                    hideError.setVisibility(View.GONE);
                    confirm.setText("");
                    newPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall,0,0,0);
                    TextViewCompat.setCompoundDrawableTintList(newPassword, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                    if (name[0] && userName[0] && firstPassword[0]) {
                        submit.setTextColor(getResources().getColor(R.color.white, getTheme()));
                        submit.setEnabled(true);
                        submit.setOnClickListener(v -> submit());

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int n, int i1, int i2) {
                submit.setEnabled(false);
                name[0] = false;
                if (newName.getText().toString().length() < 1 && newName.getText().toString().length() > 0) {
                    submit.setTextColor(getResources().getColor(R.color.grey, getTheme()));
                    nameTooShort.setVisibility(View.VISIBLE);
                }
                else if (newName.getText().toString().length() == 0) {
                    nameTooShort.setVisibility(View.GONE);
                }
                else {
                    nameTooShort.setVisibility(View.GONE);
                    name[0] = true;
                    if (userName[0] && firstPassword[0] && password[0]) {
                        submit.setTextColor(getResources().getColor(R.color.white, getTheme()));
                        submit.setEnabled(true);
                        submit.setOnClickListener(view -> submit());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void personalize() {

        name = sp.getString("KEY_NAME", "");
        username = sp.getString("KEY_USERNAME", "");
    }

    public void submit() {

        SaveUserData load = new SaveUserData();
        if (thisUser == null) {
            load.loadUserData(EditProfile.this);
        }
        pb.setVisibility(View.VISIBLE);
        EditText newName = findViewById(R.id.etEditName);
        EditText newUserName = findViewById(R.id.etEditUsername);
        EditText newPassword = findViewById(R.id.etEditPassword);
        String nName = newName.getText().toString();
        String nPassword = newPassword.getText().toString();
        String nUserName = newUserName.getText().toString().toLowerCase();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentEmail = null;
        if (user != null) {
            currentEmail = user.getEmail();
        }
        AuthCredential credential = EmailAuthProvider.getCredential(thisUser.getUserName(), thisUser.getPassword());
        String finalCurrentEmail = currentEmail;
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (!nUserName.equals(finalCurrentEmail)) {
                user.updateEmail(nUserName).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        thisUser.setUserName(nUserName);
                        thisUser.setPassword(nPassword);
                        thisUser.setName(nName);
                        Log.d(TAG, "User email address updated.");
                        editor.putString("email", nUserName);
                        editor.apply();
                    } else {
                        Log.d(TAG, "User email address update failed.");

                    }
                });
            }
            user.updatePassword(nPassword).addOnCompleteListener(task12 -> {
                if (task12.isSuccessful()) {
                    thisUser.setUserName(nUserName);
                    thisUser.setPassword(nPassword);
                    thisUser.setName(nName);
                    Log.d(TAG, "User password was updated.");
                    editor.putString("password", nPassword);
                    editor.apply();
                } else {
                    Log.d(TAG, "User password update failed.");
                }
            });
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nName).build());
        });
        if (!nUserName.equalsIgnoreCase(thisUser.getUserName())) {
            FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();

            if (user1 != null) {
                user1.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // email sent
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setMessage(getString(R.string.emailAddressUpdated)).setTitle(getString(R.string.emailAddressUpdatedSuccessfully))
                                        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {

                                            FirebaseAuth.getInstance().signOut();
                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                            mAuth.signInWithEmailAndPassword(nUserName, nPassword)
                                                    .addOnCompleteListener(task13 -> {

                                                        if (task13.isSuccessful()) {
                                                            Log.d(TAG, "signInWithEmail:success");
                                                        } else {
                                                            Log.w(TAG, "signInWithEmail:failure", task13.getException());
                                                        }

                                                    });
                                            thisUser.setUserName(nUserName);
                                            thisUser.setPassword(nPassword);
                                            thisUser.setName(nName);
                                            editor.putString("email", thisUser.getUserName());
                                            editor.putString("password", thisUser.getPassword());
                                            editor.apply();
                                            db.collection("users").document(uid).set(thisUser);
                                            load.saveUserData(EditProfile.this);
                                            Intent restart = new Intent(mContext, Logon.class);
                                            restart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(restart);
                                        });
                                pb.setVisibility(View.INVISIBLE);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.anErrorHasOccurred),
                                        Toast.LENGTH_SHORT).show();
                                overridePendingTransition(0, 0);

                            }
                        });
            }
        } else {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(nUserName, nPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    });
            thisUser.setUserName(nUserName);
            thisUser.setPassword(nPassword);
            thisUser.setName(nName);
            editor.putString("email", thisUser.getUserName());
            editor.putString("password", thisUser.getPassword());
            editor.apply();
            if (thisUser != null) {
                SaveUserData save = new SaveUserData();
                save.saveUserData(EditProfile.this);
            }
            Toast.makeText(this, getString(R.string.informationUpdated), Toast.LENGTH_SHORT).show();
            Context mContext = this;
            Intent home = new Intent(mContext, MainActivity2.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            home.putExtra("Welcome", true);
            startActivity(home);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}