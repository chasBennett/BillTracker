package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;
import static com.example.billstracker.tools.FirebaseTools.checkForExistingUser;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;

import com.example.billstracker.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public interface Google {

    @SuppressLint("CredentialManagerMisuse")
    static void launchGoogleSignIn(Activity activity, GoogleLoginCallback callback) {

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(activity.getString(R.string.default_web_client_id)).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();
        CredentialManager.create(activity).getCredentialAsync(activity, request, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {

            @Override
            public void onResult(GetCredentialResponse getCredentialResponse) {
                handleSignIn(activity, getCredentialResponse.getCredential(), callback);
            }

            @Override
            public void onError(@NonNull androidx.credentials.exceptions.GetCredentialException e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                }
                Log.e(TAG, "Credential retrieval failed");
                callback.onComplete(false, null, null);
            }
        });
    }

    static void handleSignIn(Activity activity, Credential credential, GoogleLoginCallback callback) {

        if (credential != null) {
            if (credential instanceof PublicKeyCredential) {
                String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
                callback.onComplete(false, null, null);
            } else if (credential instanceof PasswordCredential) {
                String username = ((PasswordCredential) credential).getId();
                String password = ((PasswordCredential) credential).getPassword();
                callback.onComplete(false, null, null);
            } else if (credential instanceof CustomCredential && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                firebaseAuthWithGoogle(activity, GoogleIdTokenCredential.createFrom(credential.getData()).getIdToken(), callback);
            } else {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential");
                callback.onComplete(false, null, null);
            }
        } else {
            callback.onComplete(false, null, null);
        }
    }

    static void firebaseAuthWithGoogle(Activity activity, String idToken, GoogleLoginCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                checkForExistingUser(activity, (wasSuccessful, user) -> {
                    if (wasSuccessful) {
                        Log.d(TAG, "signInWithCredential:success");
                        callback.onComplete(true, user, idToken);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        callback.onComplete(false, null, null);
                    }
                });
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                callback.onComplete(false, null, null);
            }
        });
    }

    interface GoogleLoginCallback {
        void onComplete(boolean wasSuccessful, FirebaseUser user, String idToken);
    }
}
