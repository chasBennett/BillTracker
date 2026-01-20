package com.example.billstracker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.billstracker.tools.Repository;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseActivity extends AppCompatActivity {

    protected Repository repo = Repository.getInstance();
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Get UID from Repository or FirebaseAuth
        String uid = Repository.getInstance().retrieveUid(BaseActivity.this);

        // 2. If UID is null, check SharedPreferences for a "user_json" (the ID is usually the filename)
        if (uid == null) {
            // Retrieve from your persistent storage logic
            uid = Repository.getInstance().retrieveUid(BaseActivity.this);
        }

        // 3. GATEKEEPER LOGIC
        if (uid == null) {
            // CASE A: No user found. Redirect to Login.
            redirectToLogin();
        } else if (!Repository.getInstance().isDataLoaded()) {
            // CASE B: Process Death. UID exists, but static data is wiped.
            // Show a simple loading overlay or just initialize synchronously
            Repository.getInstance().initializeBackEnd(this, (success, message) -> {
                if (success) {
                    onDataReady(); // Trigger child activity UI setup
                } else {
                    redirectToLogin();
                }
            });
        } else {
            // CASE C: Everything is fine.
            onDataReady();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Override method for child activities.
     */
    protected abstract void onDataReady();
}
