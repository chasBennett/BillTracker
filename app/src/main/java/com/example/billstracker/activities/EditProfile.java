package com.example.billstracker.activities;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.BottomDrawer;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditProfile extends BaseActivity {

    final Context mContext = this;
    TextInputEditText enterNewUsername, enterNewName, enterNewPassword, confirmPassword;
    TextInputLayout matchPassword, editPasswordLayout;
    TextView noUppercase, noLowercase, noNumber, err, passwordTooShort, usernameError;
    LinearLayout passRequirements;
    TextView submit;
    SwitchCompat biometricSwitch2;
    ImageView back;
    com.google.android.material.imageview.ShapeableImageView icon;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    StorageReference storageReference;
    boolean name;
    boolean uName;
    boolean pass;
    int differences;
    User thisUser;
    private Uri filePath;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_edit_profile);

        err = findViewById(R.id.showFrequency);
        icon = findViewById(R.id.editProfileIcon);
        submit = findViewById(R.id.btnSubmitUser);
        enterNewName = findViewById(R.id.etEditName);
        confirmPassword = findViewById(R.id.etEditPassword1);
        noNumber = findViewById(R.id.noNumber);
        matchPassword = findViewById(R.id.matchPasswordLayout);
        enterNewUsername = findViewById(R.id.etEditUsername);
        enterNewPassword = findViewById(R.id.etEditPassword);
        noUppercase = findViewById(R.id.noUppercase);
        noLowercase = findViewById(R.id.noLowercase);
        editPasswordLayout = findViewById(R.id.editPasswordLayout);
        passwordTooShort = findViewById(R.id.passwordTooShort);
        passRequirements = findViewById(R.id.passRequirements);
        biometricSwitch2 = findViewById(R.id.biometricSwitch2);
        back = findViewById(R.id.backEditProfile);
        usernameError = findViewById(R.id.usernameError);

        Tools.setupUI(EditProfile.this, findViewById(android.R.id.content));

        thisUser = repo.getUser(EditProfile.this);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::loadImage);
        storageReference = FirebaseStorage.getInstance().getReference("images");

        matchPassword.setVisibility(View.GONE);

        icon.setImageTintList(null);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
                Glide.with(icon).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).circleCrop().into(icon);
            } else {
                Glide.with(icon).load(ResourcesCompat.getDrawable(getResources(), R.drawable.profile_icon, getTheme())).into(icon);
                icon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blackAndWhite, getTheme())));
            }
        } else {
            Glide.with(icon).load(ResourcesCompat.getDrawable(getResources(), R.drawable.profile_icon, getTheme())).into(icon);
            icon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blackAndWhite, getTheme())));
        }

        icon.setOnClickListener(v -> {
            BottomDrawer bd = new BottomDrawer(EditProfile.this);
            bd.setDefaultButtonListener(v1 -> {
                if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(null).build();
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(request);
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()));
                    photoRef.delete().addOnSuccessListener(aVoid -> Log.d(TAG, "User profile photo deleted successfully")).addOnFailureListener(exception -> Log.d(TAG, "onFailure: did not delete file"));
                }
                Glide.with(icon).load(ResourcesCompat.getDrawable(getResources(), R.drawable.profile_icon, getTheme())).into(icon);
                icon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blackAndWhite, getTheme())));
                bd.dismissDialog();
            });
            bd.setSelectImageButtonListener(v12 -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                bd.dismissDialog();
            });
        });

        biometricSwitch2.setChecked(repo.getAllowBiometrics(EditProfile.this));

        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

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
                repo.setAllowBiometrics(true, EditProfile.this);
                repo.setShowBiometricPrompt(true, EditProfile.this);
            } else {
                repo.setAllowBiometrics(false, EditProfile.this);
                repo.setShowBiometricPrompt(false, EditProfile.this);
            }
            Notify.createPopup(EditProfile.this, getString(R.string.your_biometric_preferences_have_been_updated), null);
        });

        enterNewName.setText(thisUser.getName());
        enterNewUsername.setText(thisUser.getUserName());
        enterNewPassword.setText(thisUser.getPassword());
        uName = true;
        name = true;
        pass = true;
        TextWatcher watcher = new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                checkEntries();
            }
        };
        enterNewUsername.addTextChangedListener(watcher);
        enterNewName.addTextChangedListener(watcher);
        enterNewPassword.addTextChangedListener(watcher);
        confirmPassword.addTextChangedListener(watcher);

        if (thisUser.getPassword().equals(repo.getUid(EditProfile.this))) {
            TextView error = findViewById(R.id.googleSignInError);
            error.setVisibility(View.VISIBLE);
            editPasswordLayout.setVisibility(View.GONE);
            error.setText(getString(R.string.changes_to_email_are_not_allowed_when_signed_in_via_google));
            enterNewPassword.setEnabled(false);
            enterNewUsername.setEnabled(false);
        }

    }

    public void checkEntries() {

        submit.setVisibility(View.GONE);
        submit.setEnabled(false);
        submit.setOnClickListener(null);
        differences = 0;

        enterNewUsername.setBackground(AppCompatResources.getDrawable(EditProfile.this, R.drawable.border_stroke));
        enterNewName.setBackground(AppCompatResources.getDrawable(EditProfile.this, R.drawable.border_stroke));
        enterNewPassword.setBackground(AppCompatResources.getDrawable(EditProfile.this, R.drawable.border_stroke));

        if (enterNewUsername.getText() != null) {
            if (enterNewUsername.getText().toString().equalsIgnoreCase(thisUser.getUserName())) {
                usernameError.setVisibility(View.GONE);
                uName = true;
                TextTools.setValidBorder(enterNewUsername, true);
                checkNameAndPassword();
            } else {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(enterNewUsername.getText().toString()).matches() && enterNewUsername.getText().length() > 5) {
                    FirebaseTools.isRegisteredEmail(usernameError, enterNewUsername.getText().toString(), wasSuccessful -> {
                        uName = !wasSuccessful;
                        if (uName) {
                            ++differences;
                            TextTools.setValidBorder(enterNewUsername, true);
                            checkNameAndPassword();
                        }
                    });
                } else {
                    uName = false;
                    TextTools.setValidBorder(enterNewUsername, false);
                    usernameError.setVisibility(View.GONE);
                    checkNameAndPassword();
                }
            }
        } else {
            uName = false;
            TextTools.setValidBorder(enterNewUsername, false);
            checkNameAndPassword();
        }
    }

    public void checkNameAndPassword() {
        if (enterNewName.getText() != null) {
            if (enterNewName.getText().toString().equalsIgnoreCase(thisUser.getName())) {
                name = true;
                TextTools.setValidBorder(enterNewName, true);
            } else {
                if (enterNewName.getText().length() > 2) {
                    name = true;
                    ++differences;
                    TextTools.setValidBorder(enterNewName, true);
                } else {
                    TextTools.setValidBorder(enterNewName, false);
                    name = false;
                }
            }
        } else {
            name = false;
            if (enterNewName.hasFocus()) {
                TextTools.setValidBorder(enterNewName, false);
            }
        }
        if (enterNewPassword.getText() != null) {
            if (enterNewPassword.getText().toString().equals(thisUser.getPassword())) {
                passRequirements.setVisibility(View.GONE);
                pass = true;
                TextTools.setValidBorder(enterNewPassword, true);
            } else {
                passRequirements.setVisibility(View.VISIBLE);
                boolean upCase = false, loCase = false, isDigit = false, length = false;
                String password = enterNewPassword.getText().toString();
                for (int i = 0; i < password.length(); i++) {
                    if (Character.isUpperCase(password.charAt(i))) {
                        upCase = true;
                        noUppercase.setText(getString(R.string.uppercase_letter));
                        noUppercase.setTextColor(getColor(R.color.payBill));
                    }
                    if (Character.isLowerCase(password.charAt(i))) {
                        loCase = true;
                        noLowercase.setText(getString(R.string.lowercase_letter));
                        noLowercase.setTextColor(getColor(R.color.payBill));
                    }
                    if (Character.isDigit(password.charAt(i))) {
                        isDigit = true;
                        noNumber.setText(getString(R.string.number));
                        noNumber.setTextColor(getColor(R.color.payBill));
                    }
                }
                if (password.length() < 6) {
                    passwordTooShort.setText(getString(R.string.not6));
                    passwordTooShort.setTextColor(getColor(R.color.grey));
                } else {
                    length = true;
                    passwordTooShort.setText(getString(R.string.isSixCharacters));
                    passwordTooShort.setTextColor(getColor(R.color.payBill));
                }
                if (!upCase) {
                    noUppercase.setText(getString(R.string.noUpper));
                    noUppercase.setTextColor(getColor(R.color.grey));
                }
                if (!loCase) {
                    noLowercase.setText(getString(R.string.noLower));
                    noLowercase.setTextColor(getColor(R.color.grey));
                }
                if (!isDigit) {
                    noNumber.setText(getString(R.string.notNumber));
                    noNumber.setTextColor(getColor(R.color.grey));
                }
                if (upCase && loCase && isDigit && length) {
                    passRequirements.setVisibility(View.GONE);
                    matchPassword.setVisibility(View.VISIBLE);
                    if (confirmPassword.getText() != null) {
                        if (!confirmPassword.getText().toString().equals(enterNewPassword.getText().toString())) {
                            matchPassword.setError(getString(R.string.passwords_dont_match));
                            TextTools.setValidBorder(confirmPassword, false);
                            pass = false;
                        } else {
                            matchPassword.setError(null);
                            matchPassword.setVisibility(View.GONE);
                            pass = true;
                            TextTools.setValidBorder(enterNewPassword, true);
                            ++differences;
                        }
                    } else {
                        TextTools.setValidBorder(confirmPassword, false);
                        pass = false;
                    }
                } else {
                    pass = false;
                    matchPassword.setVisibility(View.GONE);
                    enterNewPassword.requestFocus();
                    TextTools.setValidBorder(enterNewPassword, false);
                }
            }
        } else {
            pass = false;
        }
        if (name && uName && pass && differences > 0) {
            submit.setVisibility(View.VISIBLE);
            submit.setEnabled(true);
            submit.setOnClickListener(v -> submit());
        }
    }

    public void submit() {

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(enterNewName.getWindowToken(), 0);

        if (enterNewName.getText() == null || enterNewName.getText().toString().isEmpty()) {
            Notify.createPopup(EditProfile.this, getString(R.string.name_can_t_be_blank), null);
        } else if (enterNewUsername.getText() == null || enterNewUsername.getText().toString().isEmpty()) {
            Notify.createPopup(EditProfile.this, getString(R.string.username_can_t_be_blank), null);
        } else if (enterNewPassword.getText() == null || enterNewPassword.getText().toString().isEmpty()) {
            Notify.createPopup(EditProfile.this, getString(R.string.password_can_t_be_blank), null);
        } else {
            String newName = enterNewName.getText().toString();
            String newPassword = enterNewPassword.getText().toString();
            String newUserName = enterNewUsername.getText().toString().toLowerCase();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                repo.loadLocalData(EditProfile.this, null);
                recreate();
            } else {
                FirebaseTools.updateUser(EditProfile.this, user, newUserName, newName, newPassword, isSuccessful -> {
                    if (isSuccessful) {
                        Notify.createPopup(EditProfile.this, getString(R.string.user_profile_updated_successfully), null);
                        User.Builder userBuilder = repo.editUser(EditProfile.this);
                        if (userBuilder != null) {
                            userBuilder.setName(newName)
                                    .setUserName(newUserName)
                                    .setPassword(newPassword)
                                    .save((wasSuccessful, message) -> {
                                        if (wasSuccessful) {
                                            recreate();
                                        }
                                    });
                        }
                        else {
                            Notify.createPopup(EditProfile.this, getString(R.string.anErrorHasOccurred), null);
                        }
                    } else {
                        Notify.createPopup(EditProfile.this, getString(R.string.anErrorHasOccurred), null);
                    }
                });
            }
        }
    }

    public void loadImage(Uri uri) {
        if (uri != null) {
            icon.setImageTintList(null);
            filePath = uri;
            Glide.with(icon).load(uri).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).circleCrop().into(icon);
            uploadImage();
            Log.d("PhotoPicker", "Selected URI: " + uri);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    }

    private String getFileExtension(Uri uri) {

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadImage() {
        if (filePath != null) {
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString() + "." + getFileExtension(filePath));

            fileReference.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                }, 1000);
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(uri1 -> {
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri1).build();
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                FirebaseAuth.getInstance().getCurrentUser().updateProfile(request);
                            }
                        });
                    }
                }
            });
        }
    }

}