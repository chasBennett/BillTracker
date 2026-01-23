package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;
import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.Notify;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public interface FirebaseTools {

    String Tag = "Firebase Tools Message";

    /**
     * Checks partner authorization status in Firestore
     */
    static void getPartner(Context context, Partner partner, FirebaseCallback callback) {
        if (partner.getPartnerUid() != null) {
            FirebaseFirestore.getInstance().collection("users").document(partner.getPartnerUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Log.i(Tag, "User data retrieved for: " + partner.getPartnerUid());
                    User partnerUser = task.getResult().toObject(User.class);
                    if (partnerUser != null && partnerUser.getPartners() != null && !partnerUser.getPartners().isEmpty()) {
                        boolean found = false;
                        for (Partner part : partnerUser.getPartners()) {
                            // Logic updated: Use the ID from the current active user in Repository
                            if (part.getPartnerUid().equals(Repository.getInstance().getUser(context).getId())) {
                                if (part.getSharingAuthorized()) {
                                    callback.isSuccessful(partner.getSharingAuthorized());
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) callback.isSuccessful(false);
                    } else {
                        callback.isSuccessful(false);
                    }
                } else {
                    callback.isSuccessful(false);
                }
            });
        } else {
            Log.i(Tag, "Partner Uid is null");
            callback.isSuccessful(false);
        }
    }

    static void signInWithEmailAndPassword(Activity activity, String username, String password, FirebaseCallback callback) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail:success");
                callback.isSuccessful(true);
            } else {
                callback.isSuccessful(false);
                Log.w(TAG, "signInWithEmail:failure", task.getException());
            }
        });
    }

    static void sendVerificationEmail(FirebaseUser user, OnCompleteCallback callback) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true, "Verification email sent.");
                    } else {
                        callback.onComplete(false, "Failed to send verification email.");
                    }
                });
    }

    public interface OnCompleteCallback {
        void onComplete(boolean wasSuccessful, String message);

        default void log(boolean success, String message) {
            String tag = FirebaseTools.class.getSimpleName();
            if (success) {
                Log.i(tag, "[Operation Success] " + message);
            } else {
                Log.e(tag, "[Operation Failed] " + message);
            }
            onComplete(success, message);
        }
    }

    static void isRegisteredEmail(TextView errorTextView, String email, FirebaseCallback callback) {
        Context activity = errorTextView.getContext();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query queryByEmail = db.collection("users").whereEqualTo("userName", email);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(activity.getString(R.string.searching_database));
        queryByEmail.get().addOnCompleteListener(task -> {
            boolean found = false;
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    errorTextView.setText(activity.getString(R.string.usernameUnavailable));
                    callback.isSuccessful(true);
                } else {
                    errorTextView.setText(activity.getString(R.string.usernameAvailable));
                    callback.isSuccessful(false);
                }
            } else {
                Log.d(TAG, requireNonNull(requireNonNull(task.getException()).getMessage()));
            }
        });
    }

    static void reAuthenticateWithEmail(Activity activity, String username, String password, FirebaseCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(username, password);
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.isSuccessful(true);
                    Log.d(ContentValues.TAG, "User re-authenticated.");
                } else {
                    callback.isSuccessful(false);
                    Notify.createPopup(activity, "User authentication failed", null);
                }
            });
        } else {
            callback.isSuccessful(false);
            Notify.createPopup(activity, "User authentication failed", null);
        }
    }

    static void changeName(Context context, FirebaseUser user, String newName, FirebaseCallback callback) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(newName).build();
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Repository profile locally
                User.Builder userBuilder = Repository.getInstance().editUser(context);
                if (userBuilder != null) {
                    userBuilder.setName(newName)
                            .save((wasSuccessful, message) -> {});
                }
                callback.isSuccessful(true);
            } else {
                callback.isSuccessful(false);
            }
        });
    }

    static void changeUsername(FirebaseUser user, String newUsername, FirebaseCallback callback) {
        user.verifyBeforeUpdateEmail(newUsername).addOnCompleteListener(task -> {
            callback.isSuccessful(task.isSuccessful());
            if (task.isSuccessful()) {
                Log.d(ContentValues.TAG, "User email address updated.");
            }
        });
    }

    static void changePassword(Context context, FirebaseUser user, String newPassword, FirebaseCallback callback) {
        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Repository profile locally
                User.Builder userBuilder = Repository.getInstance().editUser(context);
                if (userBuilder != null) {
                    userBuilder.setPassword(newPassword)
                            .save((wasSuccessful, message) -> {});
                }
                else {
                    Notify.createPopup((Activity) context, context.getString(R.string.anErrorHasOccurred), null);
                }
                Repository.getInstance().editUser(context).setPassword(newPassword).save(null);
                callback.isSuccessful(true);
            } else {
                callback.isSuccessful(false);
            }
        });
    }

    /**
     * Sequential update logic for User profile
     */
    static void updateUser(Activity activity, FirebaseUser user, String username, String name, String password, FirebaseCallback callback) {
        User thisUser = Repository.getInstance().getUser(activity);
        FirebaseTools.reAuthenticateWithEmail(activity, thisUser.getUserName(), thisUser.getPassword(), wasSuccessful -> {
            if (wasSuccessful) {
                // Nested checks to ensure all changes are attempted
                if (username != null && !username.equals(thisUser.getUserName())) {
                    FirebaseTools.changeUsername(user, username, isSuccessful -> {
                        if (isSuccessful) {
                            User.Builder userBuilder = Repository.getInstance().editUser(activity);
                            if (userBuilder != null) {
                                userBuilder.setUserName(username)
                                        .save((wasSuccessful1, message) -> {});
                            }
                            else {
                                Notify.createPopup(activity, activity.getString(R.string.anErrorHasOccurred), null);
                            }
                            updateNameAndPassword(activity, user, name, password, callback);
                        } else {
                            callback.isSuccessful(false);
                        }
                    });
                } else {
                    updateNameAndPassword(activity, user, name, password, callback);
                }
            } else {
                callback.isSuccessful(false);
            }
        });
    }

    // Helper for sequential updates to avoid deep nesting in updateUser
    private static void updateNameAndPassword(Activity activity, FirebaseUser user, String name, String password, FirebaseCallback callback) {
        User thisUser = Repository.getInstance().getUser(activity);
        if (name != null && !name.equals(thisUser.getName())) {
            FirebaseTools.changeName(activity, user, name, isSuccessful1 -> {
                if (isSuccessful1) {
                    if (password != null && !password.equals(thisUser.getPassword())) {
                        FirebaseTools.changePassword(activity, user, password, callback);
                    } else {
                        callback.isSuccessful(true);
                    }
                } else {
                    callback.isSuccessful(false);
                }
            });
        } else if (password != null && !password.equals(thisUser.getPassword())) {
            FirebaseTools.changePassword(activity, user, password, callback);
        } else {
            callback.isSuccessful(true);
        }
    }

    static void getPayments(String userId, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("payments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Repository.getInstance().getPayments().addAll(task.getResult().toObjects(Payment.class));
            }
            callback.isSuccessful(true);
        });
    }

    static void getBills(Context context, String userId, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("bills").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Repository.getInstance().getBills().addAll(task.getResult().toObjects(Bill.class));
            }
            callback.isSuccessful(true);
        });
    }

    static void getExpenses(String userId, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("expenses").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Repository.getInstance().getExpenses().addAll(task.getResult().toObjects(Expense.class));
            }
            callback.isSuccessful(true);
        });
    }

    interface FirebaseCallback {
        void isSuccessful(boolean isSuccessful);
    }

    interface FirebaseSignInResult {
        void onComplete(boolean wasSuccessful, FirebaseUser user);
    }
}