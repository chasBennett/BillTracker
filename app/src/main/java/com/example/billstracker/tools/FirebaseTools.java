package com.example.billstracker.tools;

import static com.example.billstracker.tools.Keys.KEY_PREVENT_AUTO_LOGIN;
import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.util.Log;

import com.example.billstracker.activities.Login;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Message;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.SupportTicket;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.Notify;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface FirebaseTools {

    String TAG = "Firebase Tools Message";
    int TIMEOUT_SECONDS = 15;

    /**
     * Checks partner authorization status in Firestore
     */
    static void getPartner(Context context, Partner partner, FirebaseCallback callback) {
        if (partner.getPartnerUid() != null) {
            FirebaseFirestore.getInstance().collection("users").document(partner.getPartnerUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Log.i(TAG, "User data retrieved for: " + partner.getPartnerUid());
                    User partnerUser = task.getResult().toObject(User.class);
                    if (partnerUser != null && partnerUser.getPartners() != null && !partnerUser.getPartners().isEmpty()) {
                        boolean found = false;
                        for (Partner part : partnerUser.getPartners()) {
                            if (part.getPartnerUid().equals(Repository.getInstance(context).getUser().getId())) {
                                if (part.getSharingAuthorized()) {
                                    callback.isSuccessful(partner.getSharingAuthorized());
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) callback.isSuccessful(false);
                    } else {
                        Log.i(TAG, "Partner has no partners");
                        callback.isSuccessful(false);
                    }
                } else {
                    callback.isSuccessful(false);
                }
            });
        } else {
            Log.i(TAG, "Partner Uid is null");
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
            String TAG = FirebaseTools.class.getSimpleName();
            if (success) {
                Log.i(TAG, "[Operation Success] " + message);
            } else {
                Log.e(TAG, "[Operation Failed] " + message);
            }
            onComplete(success, message);
        }
    }

    static void isRegisteredEmail(String email, FirebaseCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("userName", email).get().addOnCompleteListener(task -> {
            boolean found = false;
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        found = true;
                        break;
                    }
                }
                callback.isSuccessful(found);
            } else {
                Log.d(TAG, requireNonNull(requireNonNull(task.getException()).getMessage()));
                callback.isSuccessful(false);
            }
        });
    }

    static void updateUserProfile(Activity activity, String newEmail, String newName, String newPass, boolean isGoogleUser, FirebaseCallback callback) {
        User localUser = Repository.getInstance(activity).getUser();
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        if (fUser == null || localUser == null) {
            if (callback != null) callback.isSuccessful(false);
            return;
        }

        List<Task<?>> authTasks = new ArrayList<>();

        if (isGoogleUser) {

            if (!newName.equals(localUser.getName())) {
                authTasks.add(fUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newName).build()));
            }

            Task<Void> combinedAuthTask = authTasks.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(authTasks);

            combinedAuthTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("FirebaseTools", "Auth Update Failed", task.getException());
                            if (task.getException() != null) {
                                throw task.getException();
                            }
                        }
                        // Proceed to Firestore update
                        return Tasks.withTimeout(updateFirestoreTickets(localUser, newName, localUser.getUserName(), newPass), 15, TimeUnit.SECONDS);
                    })
                    .addOnSuccessListener(v -> updateLocalCache(activity, localUser.getUserName(), newName, localUser.getPassword(), callback))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseTools", "Final chain failure: " + e.getMessage());
                        if (callback != null) callback.isSuccessful(false);
                    });
        } else {

            AuthCredential credential = EmailAuthProvider.getCredential(localUser.getUserName(), localUser.getPassword());
            fUser.reauthenticate(credential).addOnSuccessListener(aVoid -> {

                if (!newName.equals(localUser.getName())) {
                    authTasks.add(fUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newName).build()));
                }
                if (!newEmail.equals(localUser.getUserName())) {
                    authTasks.add(fUser.verifyBeforeUpdateEmail(newEmail));
                }
                if (!newPass.isEmpty() && !newPass.equals(localUser.getPassword())) {
                    authTasks.add(fUser.updatePassword(newPass));
                }

                Tasks.whenAll(authTasks)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful() && task.getException() != null)
                                throw task.getException();
                            return Tasks.withTimeout(updateFirestoreTickets(localUser, newName, newEmail, newPass), TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        })
                        .addOnSuccessListener(v -> updateLocalCache(activity, newEmail, newName, newPass, callback))
                        .addOnFailureListener(e -> {
                            handleFirebaseError(activity, e);
                            if (callback != null) callback.isSuccessful(false);
                        });

            }).addOnFailureListener(e -> {
                Notify.createPopup(activity, "Authentication failed. Verify current password.", null);
                if (callback != null) callback.isSuccessful(false);
            });
        }
    }

    private static Task<Void> updateFirestoreTickets(User localUser, String newName, String newEmail, String newPass) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return Tasks.forException(new Exception("User not authenticated"));

        WriteBatch batch = db.batch();

        // 1. Fetch the owner document and any documents where user is the agent
        Task<DocumentSnapshot> ownerTask = db.collection("tickets").document(currentUid).get();
        Task<QuerySnapshot> agentTask = db.collection("tickets").whereEqualTo("agentUid", currentUid).get();

        return Tasks.whenAllComplete(ownerTask, agentTask).continueWithTask(allTasks -> {

            // Process the Owner Document (where ID == UID)
            if (ownerTask.isSuccessful() && ownerTask.getResult().exists()) {
                DocumentSnapshot doc = ownerTask.getResult();
                SupportTicket ticket = doc.toObject(SupportTicket.class);
                if (ticket != null) {
                    // Update top-level fields
                    batch.update(doc.getReference(), "name", newName);
                    batch.update(doc.getReference(), "userEmail", newEmail);

                    // Update nested messages
                    processNestedMessages(doc, ticket, currentUid, newName, batch);
                }
            }

            // Process Agent Documents (where agentUid == UID)
            if (agentTask.isSuccessful() && agentTask.getResult() != null) {
                for (QueryDocumentSnapshot doc : agentTask.getResult()) {
                    SupportTicket ticket = doc.toObject(SupportTicket.class);

                    // Update nested messages
                    processNestedMessages(doc, ticket, currentUid, newName, batch);
                }
            }

            // 3. Update the global 'users' collection
            batch.update(db.collection("users").document(currentUid),
                    "name", newName,
                    "userName", newEmail,
                    "password", newPass);

            return batch.commit();
        });
    }

    /**
     * Helper to iterate through the messages array and update names based on IDs
     */
    private static void processNestedMessages(DocumentSnapshot doc, SupportTicket ticket, String uid, String newName, WriteBatch batch) {
        if (ticket.getMessages() == null || ticket.getMessages().isEmpty()) return;

        boolean wasModified = false;
        for (Message msg : ticket.getMessages()) {
            // Update author name if ID matches
            if (uid.equals(msg.getAuthorId())) {
                msg.setName(newName);
                wasModified = true;
            }
            // Update repliedTo name if ID matches
            if (uid.equals(msg.getRepliedToId())) {
                msg.setRepliedToName(newName);
                wasModified = true;
            }
        }

        // Only add to batch if we actually changed a message field
        if (wasModified) {
            batch.update(doc.getReference(), "messages", ticket.getMessages());
        }
    }

    private static void handleFirebaseError(Activity activity, Exception e) {
        String msg = "Update failed.";
        if (e instanceof TimeoutException) {
            msg = "Connection timed out. Please try again with better signal.";
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            msg = "The password is too weak.";
        } else if (e instanceof FirebaseNetworkException) {
            msg = "Network error. Check your connection.";
        }

        Log.e(TAG, "Error: ", e);
        Notify.createPopup(activity, msg, null);
    }

    private static void updateLocalCache(Activity act, String email, String name, String pass, FirebaseCallback cb) {
        User.Builder builder = Repository.getInstance(act).editUser();
        if (builder != null) {
            builder.setUserName(email).setName(name).setPassword(pass)
                    .save((success, m) -> {
                        if (success) Notify.createPopup(act, "Profile Synced Successfully", null);
                        if (cb != null) cb.isSuccessful(success);
                    });
        }
    }

    static void logout(Context context) {

        LocalStore store = Repository.getInstance(context).getStore();
        InMemoryCache cache = Repository.getInstance(context).getCache();

        store.setStaySignedIn(false);
        store.setUid(null);
        cache.setUid(null);

        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_PREVENT_AUTO_LOGIN, true);
        context.startActivity(intent);
    }

    static void deleteUserAccount(Context context, AuthCredential credential, Repository.OnCompleteCallback callback) {

        InMemoryCache cache = Repository.getInstance(context).getCache();
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();

        if (authUser == null || cache.getUid() == null) {
            callback.onComplete(false, "No active session found.");
            return;
        }

        authUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (reAuthTask.isSuccessful()) {
                performFullDataWipe(context, authUser, callback);
            } else {
                callback.onComplete(false, "Authentication failed. Incorrect password.");
            }
        });
    }

    private static void performFullDataWipe(Context context, FirebaseUser authUser, Repository.OnCompleteCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        InMemoryCache cache = Repository.getInstance(context).getCache();

        String[] subCollections = {"bills", "payments", "expenses"};
        final int[] collectionsProcessed = {0};

        for (String sub : subCollections) {
            db.collection("users").document(cache.getUid()).collection(sub).get().addOnSuccessListener(snapshot -> {
                if (snapshot.isEmpty()) {
                    checkWipeProgress(context, collectionsProcessed, subCollections.length, authUser, callback);
                    return;
                }

                final int totalInSub = snapshot.size();
                final int[] deletedInSub = {0};

                for (DocumentSnapshot doc : snapshot) {
                    doc.getReference().delete().addOnCompleteListener(task -> {
                        deletedInSub[0]++;
                        if (deletedInSub[0] == totalInSub) {
                            checkWipeProgress(context, collectionsProcessed, subCollections.length, authUser, callback);
                        }
                    });
                }
            }).addOnFailureListener(e -> callback.onComplete(false, "Failed to read " + sub));
        }
    }

    static void setPhotoData () {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            String photoUrl = "";

            if (firebaseUser.getPhotoUrl() != null) {
                photoUrl = firebaseUser.getPhotoUrl().toString().replace("s96-c", "s400-c");
            }

            Map<String, Object> photoData = new HashMap<>();
            photoData.put("photoUrl", photoUrl);
            photoData.put("displayName", firebaseUser.getDisplayName());

            FirebaseFirestore.getInstance().collection("userPhotos").document(uid).set(photoData, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d(TAG, "Photo successfully synced"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error syncing photo", e));
        }
    }

    private static void checkWipeProgress(Context context, int[] counter, int target, FirebaseUser authUser, Repository.OnCompleteCallback callback) {

        Repository repo = Repository.getInstance(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        InMemoryCache cache = repo.getCache();
        LocalStore store = repo.getStore();

        store.saveCredentials(null, null, false);
        store.setPin(null);
        store.setSignedInWithGoogle(false);

        counter[0]++;
        if (counter[0] == target) {
            db.collection("users").document(cache.getUid()).delete().addOnSuccessListener(aVoid -> {
                authUser.delete().addOnCompleteListener(authDeleteTask -> {
                    if (authDeleteTask.isSuccessful()) {
                        repo.wipeDisk();
                        callback.onComplete(true, "Account and data fully deleted.");
                    } else {
                        callback.onComplete(false, "Data wiped, but Auth removal failed. Try logging in again.");
                    }
                });
            });
        }
    }

    static void getPayments(Context context, String userId, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("payments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Repository.getInstance(context).getPayments().addAll(task.getResult().toObjects(Payment.class));
            }
            callback.isSuccessful(true);
        });
    }

    static void getBills(Context context, String userId, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("bills").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Repository.getInstance(context).getBills().addAll(task.getResult().toObjects(Bill.class));
            }
            callback.isSuccessful(true);
        });
    }

    static void getExpenses(Context context, String userId, FirebaseCallback callback) {
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("expenses").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Repository.getInstance(context).getExpenses().addAll(task.getResult().toObjects(Expense.class));
            }
            callback.isSuccessful(true);
        });
    }

    static void loadCloudData(Context context, Repository.OnCompleteCallback callback) {

        Repository repo = Repository.getInstance(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        InMemoryCache cache = repo.getCache();
        LocalStore store = repo.getStore();

        cache.setBills(new Bills(new ArrayList<>()));
        cache.setPayments(new Payments(new ArrayList<>()));
        cache.setExpenses(new Expenses(new ArrayList<>()));

        db.collection("users").document(cache.getUid()).get().addOnSuccessListener(doc -> {

            if (doc.exists()) {
                cache.setThisUser(doc.toObject(User.class));

                final int TOTAL_SUBCOLLECTIONS = 3;
                final int[] loadedCount = {0};

                Runnable checkTaskCompletion = () -> {
                    loadedCount[0]++;
                    if (loadedCount[0] == TOTAL_SUBCOLLECTIONS) {
                        repo.commitToDisk();
                        store.setNeedsDownload(repo.getUid(), false);
                        callback.onComplete(true, "Cloud data synced successfully.");
                    }
                };

                db.collection("users").document(cache.getUid()).collection("bills").get()
                        .addOnSuccessListener(snap -> {
                            cache.setBills(new Bills((ArrayList<Bill>) snap.toObjects(Bill.class)));
                            cache.setBills(new Bills(new ArrayList<>(new HashSet<>(cache.getBills().getBills()))));
                            checkTaskCompletion.run();
                        });

                db.collection("users").document(cache.getUid()).collection("payments").get()
                        .addOnSuccessListener(snap -> {
                            cache.setPayments(new Payments((ArrayList<Payment>) snap.toObjects(Payment.class)));
                            cache.setPayments(new Payments(new ArrayList<>(new HashSet<>(cache.getPayments().getPayments()))));
                            checkTaskCompletion.run();
                        });

                db.collection("users").document(cache.getUid()).collection("expenses").get()
                        .addOnSuccessListener(snap -> {
                            cache.setExpenses(new Expenses((ArrayList<Expense>) snap.toObjects(Expense.class)));
                            cache.setExpenses(new Expenses(new ArrayList<>(new HashSet<>(cache.getExpenses().getExpenses()))));
                            checkTaskCompletion.run();
                        });

            } else {
                // HANDLE NEW USER
                handleNewUserCreation(context, repo.getUid(), callback);
            }
        }).addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    private static void handleNewUserCreation(Context context, String uid, Repository.OnCompleteCallback callback) {

        Repository repo = Repository.getInstance(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        InMemoryCache cache = repo.getCache();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            User newUser = new User(user.getEmail(), uid, user.getDisplayName(), uid);

            cache.setBills(new Bills(new ArrayList<>()));
            cache.setPayments(new Payments(new ArrayList<>()));
            cache.setExpenses(new Expenses(new ArrayList<>()));

            db.collection("users").document(uid).set(newUser).addOnCompleteListener(task -> {
                cache.setThisUser(newUser);
                saveData(context, (success, message) -> {
                    if (success) {
                        callback.onComplete(true, "New user account initialized.");
                    }
                    if (!success) {
                        callback.onComplete(false, message);
                    }
                });
            });
        } else {
            callback.onComplete(false, "No authenticated user found.");
        }
    }

    static void saveData (Context context, Repository.OnCompleteCallback callback) {

        Repository repo = Repository.getInstance(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        InMemoryCache cache = repo.getCache();
        LocalStore store = repo.getStore();

        if (cache.getUid() == null) {
            if (callback != null) callback.onComplete(false, "User ID is null.");
            return;
        }
        repo.commitToDisk();

        WriteBatch batch = db.batch();
        boolean hasChanges = false;

        List<Bill> dirtyBills = new ArrayList<>();
        List<Payment> dirtyPayments = new ArrayList<>();
        List<Expense> dirtyExpenses = new ArrayList<>();
        boolean userNeedsSync = false;

        if (cache.getThisUser() != null && cache.getThisUser().isNeedsSync()) {
            batch.set(db.collection("users").document(cache.getUid()), cache.getThisUser(), SetOptions.merge());
            userNeedsSync = true;
            hasChanges = true;
        }

        if (cache.getBills() != null) {
            for (Bill bill : cache.getBills().getBills()) {
                if (bill.isNeedsDelete() || bill.isNeedsSync()) {
                    DocumentReference ref = db.collection("users").document(cache.getUid()).collection("bills").document(bill.getBillerName());
                    if (bill.isNeedsDelete()) batch.delete(ref);
                    else batch.set(ref, bill, SetOptions.merge());
                    dirtyBills.add(bill);
                    hasChanges = true;
                }
            }
        }

        if (cache.getPayments() != null) {
            for (Payment p : cache.getPayments().getPayments()) {
                if (p.isNeedsDelete() || p.isNeedsSync()) {
                    DocumentReference ref = db.collection("users").document(cache.getUid()).collection("payments").document(String.valueOf(p.getPaymentId()));
                    if (p.isNeedsDelete()) batch.delete(ref);
                    else batch.set(ref, p, SetOptions.merge());
                    dirtyPayments.add(p);
                    hasChanges = true;
                }
            }
        }

        if (cache.getExpenses() != null) {
            for (Expense e : cache.getExpenses().getExpenses()) {
                if (e.isNeedsDelete() || e.isNeedsSync()) {
                    DocumentReference ref = db.collection("users").document(cache.getUid()).collection("expenses").document(e.getId());
                    if (e.isNeedsDelete()) batch.delete(ref);
                    else batch.set(ref, e, SetOptions.merge());
                    dirtyExpenses.add(e);
                    hasChanges = true;
                }
            }
        }

        if (!hasChanges) {
            store.setNeedsDownload(repo.getUid(), false);
            if (callback != null) callback.onComplete(true, "Cloud is already up to date.");
            return;
        }

        final boolean finalUserNeedsSync = userNeedsSync;
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                repo.clearDirtyFlags(dirtyBills, dirtyPayments, dirtyExpenses, finalUserNeedsSync);
                repo.commitToDisk();

                if (callback != null) callback.onComplete(true, "Sync successful.");
            } else {
                repo.scheduleRetry();
                String error = task.getException() != null ? task.getException().getMessage() : "Offline";
                if (callback != null) callback.onComplete(false, "Saved locally. Cloud sync pending: " + error);
            }
        });
    }

    static void removeFromRemotePartner(Context context, String partnerId) {

        Repository repo = Repository.getInstance(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        InMemoryCache cache = repo.getCache();

        if (partnerId == null || cache.getUid() == null) return;
        DocumentReference partnerRef = db.collection("users").document(partnerId);
        partnerRef.get().addOnSuccessListener(documentSnapshot -> {
            User partnerUser = documentSnapshot.toObject(User.class);
            if (partnerUser != null && partnerUser.getPartners() != null) {
                Partner meAsPartner = null;
                for (Partner p : partnerUser.getPartners()) {
                    if (p.getPartnerUid().equals(cache.getUid())) {
                        meAsPartner = p;
                        break;
                    }
                }
                if (meAsPartner != null) {
                    partnerRef.update("partners", com.google.firebase.firestore.FieldValue.arrayRemove(meAsPartner))
                            .addOnFailureListener(e -> Log.e("Repo", "Failed to remove from remote", e));
                }
            }
        });
    }

    interface FirebaseCallback {
        void isSuccessful(boolean isSuccessful);
    }

}