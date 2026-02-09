package com.example.billstracker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.billstracker.tools.Repository;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseActivity extends AppCompatActivity {

    protected Repository repo;
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = Repository.getInstance(this);
        String uid = repo.getUid();
        if (uid == null) {
            redirectToLogin();
            return;
        }

        if (!repo.isStoreDataComplete()) {
            repo.initializeBackEnd((success, message) -> {
                if (success) {
                    onDataReady();
                } else {
                    redirectToLogin();
                }
            });
        } else {
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
