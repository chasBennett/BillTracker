package com.example.billstracker.activities;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.BottomDrawer;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.Google;
import com.example.billstracker.tools.LocalStore;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.MessageFormat;
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
    User thisUser;
    private Uri filePath;
    private static boolean googleUser = false;
    ErrorHelper error;

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

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blueAndBlack));

        WindowInsetsController controller = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            controller = window.getInsetsController();
        }

        if (controller != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                controller.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        }

        error = new ErrorHelper(EditProfile.this, usernameError);

        thisUser = repo.getUser();

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
                if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
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

        LocalStore store = repo.getStore();
        biometricSwitch2.setChecked(store.getAllowBiometrics());

        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        BiometricManager biometricManager = BiometricManager.from(mContext);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            biometricSwitch2.setClickable(false);
            biometricSwitch2.setChecked(false);
        } else {
            biometricSwitch2.setClickable(true);
            err.setVisibility(View.GONE);
        }

        biometricSwitch2.setOnClickListener(v -> {
            boolean isChecked = biometricSwitch2.isChecked();
            store.setAllowBiometrics(isChecked);
            Notify.createPopup(EditProfile.this, getString(R.string.your_biometric_preferences_have_been_updated), null);
        });

        enterNewName.setText(thisUser.getName());
        enterNewUsername.setText(thisUser.getUserName());
        enterNewPassword.setText(thisUser.getPassword());

        TextWatcher watcher = new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                error.hide();
                checkEntries();
            }
        };
        enterNewUsername.addTextChangedListener(watcher);
        enterNewName.addTextChangedListener(watcher);
        enterNewPassword.addTextChangedListener(watcher);
        confirmPassword.addTextChangedListener(watcher);

        if (thisUser.getPassword().equals(thisUser.getId())) {
            TextView error = findViewById(R.id.googleSignInError);
            error.setVisibility(View.VISIBLE);
            editPasswordLayout.setVisibility(View.GONE);
            error.setText(getString(R.string.changes_to_email_are_not_allowed_when_signed_in_via_google));
            enterNewPassword.setEnabled(false);
            enterNewUsername.setEnabled(false);
            biometricSwitch2.setVisibility(View.GONE);
            err.setVisibility(View.GONE);
            googleUser = true;
        }
        submit.setOnClickListener(view -> submit());
        disableSubmitButton();
        checkEntries();
    }

    public void checkEntries() {

        submit.setEnabled(false);
        submit.setAlpha(0.5f);

        TaskCompletionSource<Boolean> userTcs = new TaskCompletionSource<>();
        TaskCompletionSource<Boolean> passTcs = new TaskCompletionSource<>();
        TaskCompletionSource<Boolean> nameTcs = new TaskCompletionSource<>();

        validateUsername(userTcs::setResult);
        validatePassword(passTcs::setResult);
        validateName(nameTcs::setResult);

        Tasks.whenAllSuccess(userTcs.getTask(), passTcs.getTask(), nameTcs.getTask())
                .addOnSuccessListener(results -> {
                    boolean isUserValid = (Boolean) results.get(0);
                    boolean isPassValid = (Boolean) results.get(1);
                    boolean isNameValid = (Boolean) results.get(2);

                    if (isUserValid && isPassValid && isNameValid) {
                        enableSubmitButton();
                    } else {
                        disableSubmitButton();
                    }
                })
                .addOnFailureListener(e -> {
                    disableSubmitButton();
                    Log.e("Validation", "Check failed", e);
                });
    }

    private void enableSubmitButton() {
        submit.setVisibility(View.VISIBLE);
        submit.setEnabled(true);
        submit.setAlpha(1.0f);
    }

    private void disableSubmitButton() {
        submit.setEnabled(false);
        submit.setAlpha(0.5f);
        submit.setVisibility(View.GONE);
    }

    private void validateUsername(Callback callback) {
        if (googleUser) {
            callback.isSuccessful(true);
            addCheckMark(enterNewUsername);
            return;
        }
        if (enterNewUsername.getText() == null) {
            callback.isSuccessful(false);
            return;
        }
        boolean status = enterNewUsername.getCompoundDrawablesRelative()[2] != null;
        if (enterNewUsername.getText().toString().equals(thisUser.getUserName())) {
            status = true;
        }

        String username = enterNewUsername.getText().toString();

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            error.showProcessing();
            if (enterNewUsername.hasFocus() && !enterNewUsername.getText().toString().equals(thisUser.getUserName())) {
                FirebaseTools.isRegisteredEmail(username, wasSuccessful -> {
                    if (!wasSuccessful) {
                        error.hide();
                        addCheckMark(enterNewUsername);
                        callback.isSuccessful(true);
                    } else {
                        callback.isSuccessful(false);
                        enterNewUsername.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    }
                });
            }
            else {
                addCheckMark(enterNewUsername);
                error.hide();
                callback.isSuccessful(status);
            }
        } else {
            enterNewUsername.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            callback.isSuccessful(false);
        }
    }

    private void validateName(Callback callback) {

        if (enterNewName.getText() == null) {
            callback.isSuccessful(false);
            return;
        }
        String name = enterNewName.getText().toString();
        if (name.length() >= 3) {
            addCheckMark(enterNewName);
            callback.isSuccessful(true);
        } else {
            enterNewName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            callback.isSuccessful(false);
        }
    }

    private void validatePassword(Callback callback) {

        if (googleUser) {
            callback.isSuccessful(true);
            return;
        }

        if (enterNewPassword.getText() == null || confirmPassword.getText() == null) {
            callback.isSuccessful(false);
            return;
        }
        String password = enterNewPassword.getText().toString();
        String confirmedPassword = confirmPassword.getText().toString();

        if (password.isEmpty()) {
            matchPassword.setVisibility(View.GONE);
            passRequirements.setVisibility(View.GONE);
            enterNewPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            callback.isSuccessful(false);
        }
        else {
            if (!password.equals(thisUser.getPassword())) {
                if (matchPassword.getVisibility() == View.GONE) {
                    matchPassword.setVisibility(View.VISIBLE);
                    passRequirements.setVisibility(View.VISIBLE);
                }
                boolean length = reqMet(passwordTooShort, password.length() >= 6);
                boolean capitalLetter = reqMet(noUppercase, TextTools.hasCapitalLetter(password));
                boolean lowercase = reqMet(noLowercase, TextTools.hasLowercase(password));
                boolean number = reqMet(noNumber, TextTools.hasNumber(password));

                setHintTextColor(passwordTooShort, length);
                setHintTextColor(noUppercase, capitalLetter);
                setHintTextColor(noLowercase, lowercase);
                setHintTextColor(noNumber, number);

                if (length && capitalLetter && lowercase && number) {
                    if (password.equals(confirmedPassword)) {
                        addCheckMark(enterNewPassword);
                        matchPassword.setVisibility(View.GONE);
                        passRequirements.setVisibility(View.GONE);
                        callback.isSuccessful(true);
                    } else {
                        enterNewPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        callback.isSuccessful(false);
                    }
                } else {
                    enterNewPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    callback.isSuccessful(false);
                }
            }
            else {
                matchPassword.setVisibility(View.GONE);
                passRequirements.setVisibility(View.GONE);
                addCheckMark(enterNewPassword);
                callback.isSuccessful(true);
            }
        }
    }

    public void setHintTextColor(TextView tv, boolean valid) {
        if (tv != null) {
            if (valid) {
                tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.payBill, getTheme()));
                if (tv.getText().toString().startsWith("x ")) {
                    tv.setText(MessageFormat.format("  {0}", tv.getText().toString().substring(2)));
                }
                tv.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
            } else {
                tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.lightGrey, getTheme()));
                if (!tv.getText().toString().startsWith("x ")) {
                    tv.setText(MessageFormat.format("x {0}", tv.getText().toString()));
                }
            }
        }
    }

    public void addCheckMark (TextInputEditText edit) {
        if (edit != null) {
            edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall, 0);
            TextViewCompat.setCompoundDrawableTintList(edit, ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.payBill, getTheme())));
        }
    }

    public boolean reqMet (TextView textview, boolean met) {
        if (met) {
            textview.setTextColor(ResourcesCompat.getColor(getResources(), R.color.neutralGray, getTheme()));
            return true;
        }
        else {
            textview.setTextColor(ResourcesCompat.getColor(getResources(), R.color.lightGrey, getTheme()));
            return false;
        }
    }

    public void submit() {

        if (enterNewUsername.getText() == null || enterNewName.getText() == null || enterNewPassword.getText() == null) return;
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        String name = enterNewName.getText().toString();
        String email = enterNewUsername.getText().toString();
        String password = googleUser ? thisUser.getPassword() : enterNewPassword.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (googleUser) {
            if (currentUser == null) {
                error.showProcessing();
                error.showError(getString(R.string.re_authenticating_with_google));

                Google.launchGoogleSignIn(EditProfile.this, (wasSuccessful, user, result) -> {
                    if (wasSuccessful && user != null) {
                        executeProfileUpdate(email, name, password);
                    } else {
                        error.showError("Google Sign-In failed. Please try again.");
                        Log.e("EditProfile", "Google Auth failed: " + (user == null ? "user is null" : user) + ", " + (result == null ? "result is null" : result));
                    }
                });
            } else {
                executeProfileUpdate(email, name, password);
            }
        } else {
            executeProfileUpdate(email, name, password);
        }
    }

    private void executeProfileUpdate(String email, String name, String password) {
        error.showProcessing();

        FirebaseTools.updateUserProfile(EditProfile.this, email, name, password, googleUser, isSuccessful -> {
            if (isSuccessful) {
                error.showSuccess();
                thisUser.setName(name);
                thisUser.setUserName(email);
                if (!googleUser) thisUser.setPassword(password);
            } else {
                error.showError("Update failed. Check your connection.");
            }
        });
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
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
                    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (fUser != null) {
                        fUser.updateProfile(request);
                    }
                    syncPhotoToFirestore(uri.toString());
                });
            });
        }
    }

    private void syncPhotoToFirestore(String url) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        error.showProcessing();
        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        batch.update(FirebaseFirestore.getInstance().collection("userPhotos").document(uid), "photoUrl", url);
        batch.update(FirebaseFirestore.getInstance().collection("users").document(uid), "photoUrl", url);

        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Photo URL synced to all collections.");
            error.showSuccess();
        }).addOnFailureListener(e -> {
            error.showError("Photo synced failed.");
        });
    }

    interface Callback {
        void isSuccessful(boolean isSuccessful);
    }

    public static class ErrorHelper {

        private final TextView tv;
        private final Context context;

        public ErrorHelper (Context context, TextView textView) {
            this.context = context;
            this.tv = textView;
            hide();
        }
        public void showProcessing() {
            tv.setVisibility(View.VISIBLE);
            tv.setText(R.string.processing);
            tv.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.lightGrey, context.getTheme()));
        }

        public void showError(String error) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(error);
            tv.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.lightGrey, context.getTheme()));
        }

        public void showSuccess() {
            tv.setVisibility(View.VISIBLE);
            tv.setText(R.string.profile_was_updated_successfully);
            tv.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.primary, context.getTheme()));
        }

        public void hide() {
            tv.setText("");
        }
    }

}