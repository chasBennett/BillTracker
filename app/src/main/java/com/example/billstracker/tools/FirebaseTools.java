package com.example.billstracker.tools;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;
import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Biller;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public interface FirebaseTools {

    String Tag = "Firebase Tools Message";

    static void loadBillers (Activity activity, ArrayList <Biller> billers, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("billers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                billers.addAll(task.getResult().toObjects(Biller.class));
                Set<Biller> set = new LinkedHashSet<>(billers);
                billers.clear();
                billers.addAll(set);
                Repo.getInstance().saveBillers(activity, billers);
                callback.isSuccessful(true);
                Log.i(Tag, "Biller data loaded successfully");
            } else {
                callback.isSuccessful(false);
                Log.i(Tag, requireNonNull(task.getException()).toString());
            }
        });
    }
    static void getPartner (Partner partner, FirebaseCallback callback) {
        if (partner.getPartnerUid() != null) {
            FirebaseFirestore.getInstance().collection("users").document(partner.getPartnerUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Log.i (Tag, "User data retrieved for: " + partner.getPartnerUid());
                    User partnerUser = task.getResult().toObject(User.class);
                    if (partnerUser != null && partnerUser.getPartners() != null && !partnerUser.getPartners().isEmpty()) {
                        boolean found = false;
                        for (Partner part : partnerUser.getPartners()) {
                            if (part.getPartnerUid().equals(Repo.getInstance().getUid())) {
                                if (part.getSharingAuthorized()) {
                                    callback.isSuccessful(partner.getSharingAuthorized());
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            callback.isSuccessful(false);
                        }
                    }
                    else {
                        callback.isSuccessful(false);
                    }
                }
                else {
                    callback.isSuccessful(false);
                }
            });
        }
        else {
            Log.i (Tag, "Partner Uid is null");
            callback.isSuccessful(false);
        }
    }
    static void signInWithEmailAndPassword (Activity activity, String username, String password, FirebaseCallback callback) {
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
    static void checkIfEmailVerified (Activity activity, ConstraintLayout pb, FirebaseCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                String uid = user.getUid();
                if (!uid.isEmpty()) {
                    Repo.getInstance().setUid(uid, activity);
                }
                callback.isSuccessful(true);
            } else {
                pb.setVisibility(View.GONE);
                Repo.getInstance().setStaySignedIn(false, activity);
                CustomDialog cd = new CustomDialog(activity, activity.getString(R.string.emailNotVerified), activity.getString(R.string.verifyEmail), activity.getString(R.string.resendEmail),
                        activity.getString(R.string.ok), null);
                cd.setPositiveButtonListener(v -> user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Notify.createPopup(activity, activity.getString(R.string.verificationEmailSent), null);
                        cd.dismissDialog();
                    } else {
                        Notify.createPopup(activity, activity.getString(R.string.anErrorHasOccurred), null);
                        activity.recreate();
                    }
                    FirebaseAuth.getInstance().signOut();
                    callback.isSuccessful(false);
                }));
                cd.setPerimeterListener(view -> {
                    FirebaseAuth.getInstance().signOut();
                    callback.isSuccessful(false);
                });
            }
        }
        else {
            callback.isSuccessful(false);
        }
    }
    static void findEmail (String email, FirebaseCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query queryByEmail = db.collection("users").whereEqualTo("userName", email);
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
                    Log.i(TAG, "User already exists.");
                    callback.isSuccessful(true);
                }
                else {
                    Log.i(TAG, "User doesn't exist.");
                    callback.isSuccessful(false);
                }
            } else {
                Log.d(TAG, requireNonNull(requireNonNull(task.getException()).getMessage())); //Never ignore potential errors!
            }
        });
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
                    Log.d(TAG, "User already exists.");
                    callback.isSuccessful(true);
                }
                else {
                    errorTextView.setText(activity.getString(R.string.usernameAvailable));
                    Log.d(TAG, "User doesn't exist.");
                    callback.isSuccessful(false);
                }
            } else {
                Log.d(TAG, requireNonNull(requireNonNull(task.getException()).getMessage())); //Never ignore potential errors!
            }
        });
    }
    static void reAuthenticateWithEmail (Activity activity, String username, String password, FirebaseCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(username, password);
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.isSuccessful(true);
                    Log.d(ContentValues.TAG, "\n\n\nUser re-authenticated.\n\n\n");
                }
                else {
                    callback.isSuccessful(false);
                    Notify.createPopup(activity, "User authentication failed", null);
                }
            });
        }
        else {
            callback.isSuccessful(false);
            Notify.createPopup(activity, "User authentication failed", null);
        }
    }
    static void changeName (Context context, FirebaseUser user, String newName, FirebaseCallback callback) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(newName).build();
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(ContentValues.TAG, "\n\n\nUser name updated.\n\n\n");
                Repo.getInstance().updateUser(context, user1 -> user1.setUserName(newName));
                callback.isSuccessful(true);
            } else {
                callback.isSuccessful(false);
            }
        });
    }
    static void changeUsername (FirebaseUser user, String newUsername, FirebaseCallback callback) {
        user.verifyBeforeUpdateEmail(newUsername).addOnCompleteListener(task -> {
            callback.isSuccessful(task.isSuccessful());
            if (task.isSuccessful()) {
                Log.d(ContentValues.TAG, "User email address updated.");
            }
        });
    }
    static void changePassword (Context context, FirebaseUser user, String newPassword, FirebaseCallback callback) {
        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.isSuccessful(true);
                Log.d(ContentValues.TAG, "User password updated.");
                Repo.getInstance().updateUser(context, user1 -> user1.setPassword(newPassword));
            } else {
                callback.isSuccessful(false);
            }
        });
    }
    static void updateUser (Activity activity, FirebaseUser user, String username, String name, String password, FirebaseCallback callback) {

        User thisUser = Repo.getInstance().getUser(activity);
        FirebaseTools.reAuthenticateWithEmail(activity, thisUser.getUserName(), thisUser.getPassword(), wasSuccessful -> {
            if (wasSuccessful) {
                if (username != null && !username.equals(thisUser.getUserName())) {
                    FirebaseTools.changeUsername(user, username, isSuccessful -> {
                        if (isSuccessful) {
                            thisUser.setUserName(username);
                            if (name != null && !name.equals(thisUser.getName())) {
                                FirebaseTools.changeName(activity, user, name, isSuccessful1 -> {
                                    if (isSuccessful1) {
                                        thisUser.setName(name);
                                        if (password != null && !password.equals(thisUser.getPassword())) {
                                            FirebaseTools.changePassword(activity, user, password, isSuccessful11 -> {
                                                if (isSuccessful11) {
                                                    thisUser.setPassword(password);
                                                    callback.isSuccessful(true);
                                                } else {
                                                    callback.isSuccessful(false);
                                                }
                                            });
                                        }
                                        else {
                                            callback.isSuccessful(true);
                                        }
                                    } else {
                                        callback.isSuccessful(false);
                                    }
                                });
                            }
                            else {
                                callback.isSuccessful(true);
                            }
                        } else {
                            callback.isSuccessful(false);
                        }
                    });
                } else if (name != null && !name.equals(thisUser.getName())) {
                    FirebaseTools.changeName(activity, user, name, isSuccessful1 -> {
                        if (isSuccessful1) {
                            thisUser.setName(name);
                            if (password != null && !password.equals(thisUser.getPassword())) {
                                FirebaseTools.changePassword(activity, user, password, isSuccessful11 -> {
                                    if (isSuccessful11) {
                                        thisUser.setPassword(password);
                                        callback.isSuccessful(true);
                                    } else {
                                        callback.isSuccessful(false);
                                    }
                                });
                            }
                            else {
                                callback.isSuccessful(true);
                            }
                        } else {
                            callback.isSuccessful(false);
                        }
                    });
                }
                else if (password != null && !password.equals(thisUser.getPassword())) {
                    FirebaseTools.changePassword(activity, user, password, isSuccessful11 -> {
                        if (isSuccessful11) {
                            thisUser.setPassword(password);
                            callback.isSuccessful(true);
                        } else {
                            callback.isSuccessful(false);
                        }
                    });
                }
                else {
                    callback.isSuccessful(false);
                }
            }
            else {
                callback.isSuccessful(false);
            }
        });
    }

    static void getPayments (String userId, FirebaseCallback callback) {

        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        db1.collection("users").document(userId).collection("payments").get().addOnCompleteListener(task11 -> {
            if (task11.isSuccessful() && task11.getResult() != null && !task11.getResult().isEmpty()) {
                Repo.getInstance().getPayments().addAll(task11.getResult().toObjects(Payment.class));
                callback.isSuccessful(true);
            } else {
                callback.isSuccessful(true);
            }
        });
    }
    static void getBills (Context context, String userId, FirebaseCallback callback) {
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();

        db1.collection("users").document(userId).collection("bills").get().addOnCompleteListener(task111 -> {
            if (task111.isSuccessful() && task111.getResult() != null && !task111.getResult().isEmpty()) {
                Repo.getInstance().getBills().addAll(task111.getResult().toObjects(Bill.class));
                Repo.getInstance().removeDuplicateBills();
                callback.isSuccessful(true);
            } else {
                if (Repo.getInstance().getUser(context).getBills() != null && !Repo.getInstance().getUser(context).getBills().isEmpty()) {
                    Repo.getInstance().getBills().addAll(Repo.getInstance().getUser(context).getBills());
                    Repo.getInstance().setUser(new User(Repo.getInstance().getUser(context).getUserName(), Repo.getInstance().getUser(context).getPassword(), Repo.getInstance().getUser(context).getName(), Repo.getInstance().getUser(context).isAdmin(),
                            Repo.getInstance().getUser(context).getLastLogin(), Repo.getInstance().getUser(context).getDateRegistered(), Repo.getInstance().getUser(context).getId(), Repo.getInstance().getUser(context).getTotalLogins(),
                            Repo.getInstance().getUser(context).getTicketNumber(), Repo.getInstance().getUser(context).getIncome(), Repo.getInstance().getUser(context).getPayFrequency(), Repo.getInstance().getUser(context).getTrophies(),
                            Repo.getInstance().getUser(context).getBudgets(), Repo.getInstance().getUser(context).getPartners(), Repo.getInstance().getUser(context).getVersionNumber()));
                }
                callback.isSuccessful(true);
            }
        });
    }
    static void getExpenses (String userId, FirebaseCallback callback) {
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();

        db1.collection("users").document(userId).collection("expenses").get().addOnCompleteListener(task1111 -> {
            if (task1111.isSuccessful() && task1111.getResult() != null && !task1111.getResult().isEmpty()) {
                Repo.getInstance().getExpenses().addAll(task1111.getResult().toObjects(Expense.class));
            }
            callback.isSuccessful(true);
        });
    }

    static void checkForExistingUser (Activity activity, FirebaseSignInResult callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Query queryByEmail = FirebaseFirestore.getInstance().collection("users").whereEqualTo("userName", user.getEmail());
            queryByEmail.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User thisUser = Repo.getInstance().getUser(activity);
                    boolean found = false;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            found = true;
                            thisUser = document.toObject(User.class);
                            break;
                        }
                    }
                    if (found && thisUser.getUserName() != null && thisUser.getPassword() != null) {
                        Repo.getInstance().saveCredentials(activity, thisUser.getUserName(), thisUser.getPassword());
                        Repo.getInstance().setUid(thisUser.getId(), activity);
                        AuthCredential credential = EmailAuthProvider.getCredential(thisUser.getUserName(), thisUser.getPassword());
                        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential).addOnCompleteListener(activity, task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d(ContentValues.TAG, "linkWithCredential:success");
                                FirebaseUser user1 = task1.getResult().getUser();
                                if (user1 != null) {
                                    Repo.getInstance().updateUser(activity, user2 -> {
                                        user2.setAdmin(false);
                                        user2.setId(user1.getUid());
                                    });
                                }
                                callback.onComplete(true, user1);
                            }
                            else {
                                callback.onComplete(true, user);
                                Log.w(ContentValues.TAG, "linkWithCredential:failure", task1.getException());
                            }
                        });
                    }
                    else {
                        callback.onComplete(true, user);
                    }
                }
                else {
                    callback.onComplete(true, user);
                }
            });
        }
        else {
            callback.onComplete(false, null);
        }
    }

    interface FirebaseCallback {
        void isSuccessful(boolean isSuccessful);
    }
    interface FirebaseSignInResult {
        void onComplete(boolean wasSuccessful, FirebaseUser user);
    }

}
