package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.example.billstracker.activities.Login;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Repository {
    private static Repository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Gson gson = new Gson();
    // In-memory cache
    private User thisUser;
    private Payments payments;
    private Expenses expenses;
    private Bills bills;
    private String uid;

    private Repository() {
    }

    /**
     * Returns the singleton instance of the Repository class.
     * If the instance does not exist, it creates a new one.
     *
     * @return The singleton instance of the Repository class.
     */
    public static synchronized Repository getInstance() {
        if (instance == null) instance = new Repository();
        return instance;
    }

    /**
     * Checks if the user data is loaded into memory.
     *
     * @return True if the user data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return thisUser != null && bills != null && payments != null && expenses != null;
    }

    /**
     * Initializes Firebase and calls FirebaseAuth.getInstance().useAppLanguage().
     * Then loads the savedUid, thisUser, bills, payments, and expenses from the SharedPreferences branch specific to the saved uid or returns if the uid is null.
     *
     * @param context The application's context.
     * @param callback A callback to be executed after the data is loaded.
     */
    public void initializeBackEnd (Context context, OnCompleteCallback callback) {
        context = context.getApplicationContext();
        FirebaseApp.initializeApp(context);
        FirebaseAuth.getInstance().useAppLanguage();
        loadLocalData(context, callback);
    }

    /**
     * Saves the user's data to a SharedPreferences branch that is specific to their uid.
     * Then it uploads the data to the Firebase Firestore instance.
     * Elements saved include User, Bills, Payments, and Expenses.
     *
     * @param context The application's context.
     * @param callback A callback to be executed after the data is saved.
     */
    public void saveData(Context context, OnCompleteCallback callback) {
        Context appContext = context.getApplicationContext();
        if (uid == null) {
            callback.onComplete(false, "User ID is null.");
            return;
        }

        writeToDisk(appContext, (success, message) -> {});

        WriteBatch batch = db.batch();
        boolean hasChanges = false;
        boolean userNeedsSync = false;

        // These lists track which objects need their flags cleared after success
        List<Bill> dirtyBills = new ArrayList<>();
        List<Payment> dirtyPayments = new ArrayList<>();
        List<Expense> dirtyExpenses = new ArrayList<>();

        // 1. Process User Profile
        if (thisUser != null && thisUser.isNeedsSync()) {
            DocumentReference userRef = db.collection("users").document(uid);
            batch.set(userRef, thisUser, SetOptions.merge());
            userNeedsSync = true;
            hasChanges = true;
        }

        // 2. Process Bills
        if (bills != null && bills.getBills() != null) {
            for (Bill bill : bills.getBills()) {
                DocumentReference ref = db.collection("users").document(uid)
                        .collection("bills").document(bill.getBillerName());
                if (bill.isNeedsDelete() || bill.isNeedsSync()) {
                    if (bill.isNeedsDelete()) {
                        batch.delete(ref);
                    } else {
                        batch.set(ref, bill, SetOptions.merge());
                    }
                    dirtyBills.add(bill);
                    hasChanges = true;
                }
            }
        }

        // 3. Process Payments
        if (payments != null && payments.getPayments() != null) {
            for (Payment p : payments.getPayments()) {
                DocumentReference ref = db.collection("users").document(uid)
                        .collection("payments").document(String.valueOf(p.getPaymentId()));
                if (p.isNeedsDelete() || p.isNeedsSync()) {
                    if (p.isNeedsDelete()) {
                        batch.delete(ref);
                    } else {
                        batch.set(ref, p, SetOptions.merge());
                    }
                    dirtyPayments.add(p);
                    hasChanges = true;
                }
            }
        }

        // 4. Process Expenses
        if (expenses != null && expenses.getExpenses() != null) {
            for (Expense e : expenses.getExpenses()) {
                DocumentReference ref = db.collection("users").document(uid)
                        .collection("expenses").document(e.getId());
                if (e.isNeedsDelete() || e.isNeedsSync()) {
                    if (e.isNeedsDelete()) {
                        batch.delete(ref);
                    } else {
                        batch.set(ref, e, SetOptions.merge());
                    }
                    dirtyExpenses.add(e);
                    hasChanges = true;
                }
            }
        }

        if (!hasChanges) {
            callback.onComplete(true, "Local data saved. Cloud is already up to date.");
            return;
        }

        final boolean finalUserNeedsSync = userNeedsSync;
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (finalUserNeedsSync) thisUser.setNeedsSync(false);

                for (Bill b : dirtyBills) b.setNeedsSync(false);
                for (Payment p : dirtyPayments) p.setNeedsSync(false);
                for (Expense e : dirtyExpenses) e.setNeedsSync(false);

                // Re-save to disk to clear the 'needsSync' flags in SharedPreferences
                writeToDisk(appContext, (success, msg) -> {
                    callback.onComplete(true, "Local and Cloud data synced successfully.");
                });
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                callback.onComplete(false, "Cloud sync failed: " + errorMsg);
            }
        });
    }

    /**
     * Loads the user's data from a sharedPreferences branch that is specific to their uid.
     *
     * @param context The application's context.
     */
    public void loadLocalData(Context context, OnCompleteCallback onComplete) {
        context = context.getApplicationContext();
        if (uid == null) {
            uid = retrieveUid(context); // Actually assign the value
            if (uid == null || uid.isEmpty()) {
                // Even if there is no UID, we must trigger the callback so the UI finishes loading
                onComplete.onComplete(false, "No saved user found.");
                return;
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(uid, Context.MODE_PRIVATE);
        String userJson = prefs.getString("user_json", null);

        if (userJson != null) {
            thisUser = gson.fromJson(userJson, User.class);
        }

        if (thisUser == null) {
            thisUser = new User();
        }
        if (thisUser.getBudgets() == null) thisUser.setBudgets(new ArrayList<>());
        if (thisUser.getPartners() == null) thisUser.setPartners(new ArrayList<>());
        if (thisUser.getBills() == null) thisUser.setBills(new ArrayList<>());

        bills = gson.fromJson(prefs.getString("bills_json", null), Bills.class);
        payments = gson.fromJson(prefs.getString("payments_json", null), Payments.class);
        expenses = gson.fromJson(prefs.getString("expenses_json", null), Expenses.class);

        if (bills == null) bills = new Bills(new ArrayList<>());
        if (payments == null) payments = new Payments(new ArrayList<>());
        if (expenses == null) expenses = new Expenses(new ArrayList<>());

        if (onComplete != null) {
            onComplete.onComplete(true, "Local data loaded successfully.");
        }
    }

    public User.Builder editUser(Context context) {
        User user = thisUser;
        return new User.Builder(context, user);
    }

    /**
     * Creates a new Bill.Builder instance and returns it.
     *
     * @param billerName The name of the biller.
     * @param context    The application's context.
     * @return A new Bill.Builder instance.
     */
    public Bill.Builder editBill(String billerName, Context context) {
        Bill bill = getBillByName(billerName);
        return new Bill.Builder(context, bill);
    }

    /**
     * Creates a new Payment.Builder instance and returns it.
     *
     * @param paymentId The ID of the payment.
     * @param context   The application's context.
     * @return A new Payment.Builder instance.
     */
    public Payment.Builder editPayment(int paymentId, Context context) {
        return new Payment.Builder(context, getPaymentById(paymentId));
    }

    /**
     * Creates a new Expense.Builder instance and returns it.
     *
     * @param expenseId The ID of the expense.
     * @param context   The application's context.
     * @return A new Expense.Builder instance.
     */
    public Expense.Builder editExpense(String expenseId, Context context) {
        return new Expense.Builder(context, getExpenseById(expenseId));
    }

    /**
     * Fetches the user's cloud data and loads it into memory.
     *
     * @param userUid The user's UID.
     * @param context The application's context.
     * @param callback A callback to be executed after the data is fetched.
     *
     * This callback will be executed on the main thread.
     */
    public void fetchCloudData(String userUid, Context context, OnCompleteCallback callback) {
        this.uid = userUid;
        Context appContext = context.getApplicationContext();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                this.thisUser = doc.toObject(User.class);

                final int TOTAL_SUBCOLLECTIONS = 3;
                final int[] loadedCount = {0};

                // This is our final step after all 3 sub-collections are in memory
                Runnable checkTaskCompletion = () -> {
                    loadedCount[0]++;
                    if (loadedCount[0] == TOTAL_SUBCOLLECTIONS) {
                        // CRITICAL STRATEGY STEP:
                        // Mirror the cloud data to local storage immediately
                        writeToDisk(context, (success, message) -> {
                            if (success) {
                                callback.onComplete(true, "Cloud data synced successfully.");
                            }
                            if (!success) {
                                callback.onComplete(false, message);
                            }
                        });
                    }
                };

                // Fetch Sub-collections
                db.collection("users").document(uid).collection("bills").get()
                        .addOnSuccessListener(snap -> {
                            this.bills = new Bills((ArrayList<Bill>) snap.toObjects(Bill.class));
                            this.bills = new Bills(new ArrayList<>(new HashSet<>(this.bills.getBills())));
                            checkTaskCompletion.run();
                        });

                db.collection("users").document(uid).collection("payments").get()
                        .addOnSuccessListener(snap -> {
                            this.payments = new Payments((ArrayList<Payment>) snap.toObjects(Payment.class));
                            this.payments = new Payments(new ArrayList<>(new HashSet<>(this.payments.getPayments())));
                            checkTaskCompletion.run();
                        });

                db.collection("users").document(uid).collection("expenses").get()
                        .addOnSuccessListener(snap -> {
                            this.expenses = new Expenses((ArrayList<Expense>) snap.toObjects(Expense.class));
                            this.expenses = new Expenses(new ArrayList<>(new HashSet<>(this.expenses.getExpenses())));
                            checkTaskCompletion.run();
                        });

            } else {
                // HANDLE NEW USER
                handleNewUserCreation(userUid, appContext, callback);
            }
        }).addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
    }

    private void writeToDisk(Context context, OnCompleteCallback callback) {
        SharedPreferences.Editor editor = context.getSharedPreferences(uid, Context.MODE_PRIVATE).edit();
        editor.putString("user_json", gson.toJson(thisUser));
        editor.putString("bills_json", gson.toJson(bills));
        editor.putString("payments_json", gson.toJson(payments));
        editor.putString("expenses_json", gson.toJson(expenses));
        editor.apply();
        callback.onComplete(true, "Local data saved successfully.");
    }

    /**
     * Handles the creation of a new user account.
     * @param uid The user's UID.
     * @param context The application's context.
     * @param callback A callback to be executed after the user account is created.
     */
    private void handleNewUserCreation(String uid, Context context, OnCompleteCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            User newUser = new User(user.getEmail(), uid, user.getDisplayName(), uid);

            // Ensure lists aren't null even for new users
            this.bills = new Bills(new ArrayList<>());
            this.payments = new Payments(new ArrayList<>());
            this.expenses = new Expenses(new ArrayList<>());

            db.collection("users").document(uid).set(newUser).addOnCompleteListener(task -> {
                this.thisUser = newUser;
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


    /**
     * Saves the current user's uid to a global SharedPreferences instance for use during future logins.
     *
     * @param uid     The user UID.
     * @param context The application's context
     *
     */
    public void setUid(String uid, Context context) {
        context = context.getApplicationContext();
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).edit().putString("lastUid", uid)
                .apply();
    }

    /**
     * Retrieves the user's uid from a global SharedPreferences instance.
     *
     * @param context The application's context.
     * @return The user's uid.
     *
     */
    public String retrieveUid(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getString("lastUid", "");
    }

    /**
     * Logs the user out of the application.
     *
     * @param context The application's context.
     *
     */
    public void logout(Context context) {

        Prefs.setSignedInWithGoogle((Activity) context, false);

        thisUser = null;
        payments = null;
        expenses = null;
        bills = null;
        uid = null;

        setStaySignedIn(false, context);
        FirebaseAuth.getInstance().signOut();

        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        CredentialManager manager = CredentialManager.create(context);

        manager.clearCredentialStateAsync(clearRequest, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<>() {
            @Override
            public void onResult(Void unused) {

                Intent intent = new Intent(context, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Welcome", true);
                context.startActivity(intent);
            }

            @Override
            public void onError(@NonNull ClearCredentialException e) {
                Log.e(TAG, "Couldn't clear user credentials: " + e);
                context.startActivity(new Intent(context, Login.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    /**
     * Sets the ownership of the user's data.
     * This includes setting the owner of all bills, payments, and expenses.
     *
     */
    public void setOwnership() {
        if (uid != null) {
            if (bills != null && !bills.getBills().isEmpty()) {
                for (Bill bill : bills.getBills()) {
                    if (bill.getOwner() == null) {
                        bill.setOwner(uid);
                    }
                }
            }
            if (payments != null && !payments.getPayments().isEmpty()) {
                for (Payment payment : payments.getPayments()) {
                    if (payment.getOwner() == null) {
                        payment.setOwner(uid);
                    }
                }
            }
            if (expenses != null && !expenses.getExpenses().isEmpty()) {
                for (Expense expense : expenses.getExpenses()) {
                    if (expense.getOwner() == null) {
                        expense.setOwner(uid);
                    }
                }
            }
        }
    }

    /**
     * Checks if the user has previously elected to stay signed in.
     *
     * @param context The application's context.
     * @return True if the user chose to stay signed in, false otherwise.
     *
     */
    public boolean getStaySignedIn(Context context) {
        boolean value = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getBoolean("stay_signed_in", false);
        if (value) {
            uid = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getString("uid", "");
            return !uid.isEmpty();
        }
        return false;
    }

    /**
     * Sets the user's preference for whether they want to stay signed in.
     *
     * @param value   True if the user previously chose to stay signed in, false otherwise.
     * @param context The application's context.
     *
     */
    public void setStaySignedIn(boolean value, Context context) {
        if (!value) {
            context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                    .edit()
                    .remove("saved_email")
                    .remove("saved_password")
                    .apply();
        }
        saveData(context, (wasSuccessful, message) -> Log.d("Repository Message", "Credentials cleared."));
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("stay_signed_in", value)
                .putString("uid", uid)
                .apply();
    }

    // --- ALLOW BIOMETRIC SIGN IN ---

    /**
     * Checks if the user has previously elected to use biometric authentication.
     *
     * @param context The application's context.
     * @return True if the user chose to use biometric authentication, false otherwise.
     *
     */
    public boolean getAllowBiometrics(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getBoolean("allow_biometrics", false);
    }

    /**
     * Sets the user's preference for whether they want to use biometric authentication.
     *
     * @param value   True if the user chose to allow biometric authentication, false otherwise.
     * @param context The application's context.
     *
     */
    public void setAllowBiometrics(boolean value, Context context) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("allow_biometrics", value)
                .apply();
    }

    // --- ALLOW BIOMETRIC PROMPT (The "Ask Again" logic) ---

    /**
     * Checks if the user has previously elected to allow a biometric prompt at sign in.
     *
     * @param context The application's context.
     * @return True if the user chose to allow a biometric prompt, false otherwise.
     *
     */
    public boolean getShowBiometricPrompt(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getBoolean("show_biometric_prompt", true);
    }

    /**
     * Sets the user's preference for whether they want to allow a biometric prompt at sign in.
     *
     * @param value   True if the user chose to allow a biometric prompt, false otherwise.
     * @param context The application's context.
     *
     */
    public void setShowBiometricPrompt(boolean value, Context context) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("show_biometric_prompt", value)
                .apply();
    }

    /**
     * Saves the user's email and password to a SharedPreferences instance for biometric sign in.
     *
     * @param context The application's context.
     * @param email   The user's email.
     * @param password The user's password.
     *
     */
    public void saveCredentials(Context context, String email, String password) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putString("saved_email", email)
                .putString("saved_password", password)
                .apply();
    }

    /**
     * Retrieves the user's email from a SharedPreferences instance.
     *
     * @param context The application's context.
     * @return The user's saved email, or an empty string if not saved.
     *
     */
    public String getSavedEmail(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getString("saved_email", "");
    }

    /**
     * Retrieves the user's password from a SharedPreferences instance.
     *
     * @param context The application's context.
     * @return The user's saved password, or an empty string if not saved.
     *
     */
    public String getSavedPassword(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getString("saved_password", "");
    }

    /**
     * Loads the user's partner data from Firebase Firestore.
     *
     * @param userId The user's ID.
     * @param onCompleteCallback A callback to be executed after the data is loaded.
     *
     */
    public void loadPartnerData(String userId, OnCompleteCallback onCompleteCallback) {
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        if (payments == null || payments.getPayments() == null) {
            payments = new Payments(new ArrayList<>());
        }
        db1.collection("users").document(userId).collection("payments").get().addOnCompleteListener(task11 -> {
            if (task11.isSuccessful() && task11.getResult() != null && !task11.getResult().isEmpty()) {
                payments.getPayments().addAll(task11.getResult().toObjects(Payment.class));
            } else {
                if (payments == null || payments.getPayments() == null) {
                    payments = new Payments(new ArrayList<>());
                }
            }
            if (bills == null || bills.getBills() == null) {
                bills = new Bills(new ArrayList<>());
            }
            db1.collection("users").document(userId).collection("bills").get().addOnCompleteListener(task111 -> {
                if (task111.isSuccessful() && task111.getResult() != null && !task111.getResult().isEmpty()) {
                    bills.getBills().addAll(task111.getResult().toObjects(Bill.class));
                    Set<Bill> billList = new HashSet<>(bills.getBills());
                    bills.setBills(new ArrayList<>(billList));
                } else {
                    if (thisUser.getBills() != null && !thisUser.getBills().isEmpty()) {
                        bills.getBills().addAll(thisUser.getBills());
                        thisUser = new User(thisUser.getUserName(), thisUser.getPassword(), thisUser.getName(), thisUser.isAdmin(), thisUser.getRegisteredWithGoogle(), thisUser.getLastLogin(), thisUser.getDateRegistered(),
                                thisUser.getId(), thisUser.getBills(), thisUser.getTotalLogins(), thisUser.getTicketNumber(), thisUser.getIncome(), thisUser.getPayFrequency(), thisUser.getTermsAcceptedOn(), thisUser.getTrophies(),
                                thisUser.getBudgets(), thisUser.getPhoneNumber(), thisUser.getPartners(), thisUser.getVersionNumber());
                    }
                    if (bills == null || bills.getBills() == null) {
                        bills = new Bills(new ArrayList<>());
                    }

                }
                if (expenses == null || expenses.getExpenses() == null) {
                    expenses = new Expenses(new ArrayList<>());
                }
                db1.collection("users").document(userId).collection("expenses").get().addOnCompleteListener(task1111 -> {
                    if (task1111.isSuccessful() && task1111.getResult() != null && !task1111.getResult().isEmpty()) {
                        expenses.getExpenses().addAll(task1111.getResult().toObjects(Expense.class));
                    } else {
                        if (expenses == null || expenses.getExpenses() == null) {
                            expenses = new Expenses(new ArrayList<>());
                        }
                    }
                    onCompleteCallback.onComplete(true, "Partner data was loaded successfully.");
                });
            });
        });
    }

    /**
     * Creates a new user account.
     *
     * @param email The new User's email address.
     * @param password The new User's password.
     * @param name The new User's name.
     * @param id The new User's ID.
     * @param context The application's context.
     * @return A new User.Builder instance.
     */
    public User.Builder addUser(String email, String password, String name, String id, Context context) {
        if (id == null) return null;

        // 1. Initialize the local User using the Safety Constructor
        // This ensures all ArrayLists (bills, budgets, etc.) are non-null
        this.thisUser = new User(email, password, name, id);
        this.uid = id;

        // 2. Return the Builder so the Activity can chain additional fields
        return new User.Builder(context, thisUser);
    }

    /**
     * Deletes the user's account.
     *
     * @param context The application's context.
     * @param credential The user's authentication credential.
     * @param callback A callback to be executed after the user account is deleted.
     */
    public void deleteUserAccount(Context context, AuthCredential credential, OnCompleteCallback callback) {
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null || uid == null) {
            callback.onComplete(false, "No active session found.");
            return;
        }

        // Re-authentication is required by Firebase for account deletion
        authUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (reAuthTask.isSuccessful()) {
                performFullDataWipe(context, authUser, callback);
            } else {
                callback.onComplete(false, "Authentication failed. Incorrect password.");
            }
        });
    }

    public void removeFromRemotePartner(String partnerId) {
        if (partnerId == null || uid == null) return;

        // 1. Identify the partner's document
        DocumentReference partnerRef = db.collection("users").document(partnerId);

        // 2. Fetch the partner's data to find the specific Partner object that matches YOU
        partnerRef.get().addOnSuccessListener(documentSnapshot -> {
            User partnerUser = documentSnapshot.toObject(User.class);
            if (partnerUser != null && partnerUser.getPartners() != null) {
                Partner meAsPartner = null;
                for (Partner p : partnerUser.getPartners()) {
                    if (p.getPartnerUid().equals(this.uid)) {
                        meAsPartner = p;
                        break;
                    }
                }

                // 3. Use arrayRemove to delete that specific object from their cloud list
                if (meAsPartner != null) {
                    partnerRef.update("partners", com.google.firebase.firestore.FieldValue.arrayRemove(meAsPartner))
                            .addOnFailureListener(e -> Log.e("Repo", "Failed to remove from remote", e));
                }
            }
        });
    }

    /**
     * Deletes the user's account information from Firebase Firestore.
     *
     * @param context The application's context.
     * @param authUser The user's authentication user.
     * @param callback A callback to be executed after the user account is deleted.
     */
    private void performFullDataWipe(Context context, FirebaseUser authUser, OnCompleteCallback callback) {
        String[] subCollections = {"bills", "payments", "expenses"};
        final int[] collectionsProcessed = {0};

        for (String sub : subCollections) {
            db.collection("users").document(uid).collection(sub).get().addOnSuccessListener(snapshot -> {
                if (snapshot.isEmpty()) {
                    checkWipeProgress(collectionsProcessed, subCollections.length, authUser, context, callback);
                    return;
                }

                // Safe approach: Delete each document individually to avoid 500-limit crashes
                final int totalInSub = snapshot.size();
                final int[] deletedInSub = {0};

                for (DocumentSnapshot doc : snapshot) {
                    doc.getReference().delete().addOnCompleteListener(task -> {
                        deletedInSub[0]++;
                        if (deletedInSub[0] == totalInSub) {
                            checkWipeProgress(collectionsProcessed, subCollections.length, authUser, context, callback);
                        }
                    });
                }
            }).addOnFailureListener(e -> callback.onComplete(false, "Failed to read " + sub));
        }
    }

    // Helper to track when all sub-collections AND the user document are gone

    /**
     * Checks if all sub-collections and the user document are gone.
     * If so, deletes the user document.
     *
     * @param counter The number of sub-collections processed.
     * @param target The total number of sub-collections.
     * @param authUser The user's authentication user.
     * @param context The application's context.
     * @param callback A callback to be executed after the user account is deleted.
     */
    private void checkWipeProgress(int[] counter, int target, FirebaseUser authUser, Context context, OnCompleteCallback callback) {
        counter[0]++;
        if (counter[0] == target) {
            // Delete the main user document
            db.collection("users").document(uid).delete().addOnSuccessListener(aVoid -> {
                // Finally, delete the Auth account
                authUser.delete().addOnCompleteListener(authDeleteTask -> {
                    if (authDeleteTask.isSuccessful()) {
                        wipeLocalData(context);
                        callback.onComplete(true, "Account and data fully deleted.");
                    } else {
                        callback.onComplete(false, "Data wiped, but Auth removal failed. Try logging in again.");
                    }
                });
            });
        }
    }

    public User getUser(Context context) {
        if (thisUser == null) {
            loadLocalData(context, (success, message) -> {
            });
        }
        return thisUser;
    }

    /**
     * Deletes the user's local data.
     *
     * @param context The application's context.
     *
     */
    private void wipeLocalData(Context context) {
        // 1. Target the UID-specific file before clearing the UID reference
        if (uid != null) {
            context.getSharedPreferences(uid, Context.MODE_PRIVATE).edit().clear().apply();
        }

        // 2. Clear Global Preferences (credentials, stay signed in)
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).edit().clear().apply();

        // 3. Nullify memory references to trigger the BaseActivity Gatekeeper
        thisUser = null;
        bills = null;
        payments = null;
        expenses = null;
        uid = null;

        // 4. Reset biometric states
        setAllowBiometrics(false, context);
        setShowBiometricPrompt(true, context);
    }

    public ArrayList<Bill> getBills() {
        if (bills == null) return new ArrayList<>();
        return bills.getBills();
    }

    /**
     * Retrieves a Bill from the Bills collection using the billerName field.
     *
     * @param name The name of the biller.
     * @return The Bill object with the specified name, or null if not found.
     */
    public Bill getBillByName(String name) {
        if (bills == null) return null;
        for (Bill bill : bills.getBills()) {
            if (bill.getBillerName().equalsIgnoreCase(name)) return bill;
        }
        return null;
    }

    /**
     * Retrieves a Bill from the Bills collection using the billId field.
     *
     * @param billId The ID of the bill.
     * @return The Bill object with the specified ID, or null if not found.
     */
    public Bill getBillById(String billId) {
        if (bills == null) return null;
        for (Bill bill : bills.getBills()) {
            if (bill.getBillsId().equalsIgnoreCase(billId)) return bill;
        }
        return null;
    }

    public void addBill(Bill bill, Context context, OnCompleteCallback callback) {
        if (bills == null) bills = new Bills(new ArrayList<>());
        bills.getBills().add(bill);

        db.collection("users").document(uid).collection("bills")
                .document(bill.getBillerName()).set(bill, SetOptions.merge());

        saveData(context, (wasSuccessful, message) -> {
            if (wasSuccessful) {
                callback.onComplete(true, "Bill created successfully.");
            }
            if (!wasSuccessful) {
                callback.onComplete(false, message);
            }
        });
    }

    public void deleteBill(String billerName, Context context, OnCompleteCallback callback) {
        if (uid == null || bills == null) {
            callback.onComplete(false, "Session or data missing.");
            return;
        }

        Bill toRemove = getBillByName(billerName);
        if (toRemove == null) {
            callback.onComplete(false, "Bill not found.");
            return;
        }

        // 1. Mark the bill for deletion
        toRemove.setNeedsDelete(true);

        // 2. Handle associated payments
        if (payments != null && payments.getPayments() != null) {
            for (Payment p : payments.getPayments()) {
                if (p.getBillerName().equalsIgnoreCase(billerName)) {
                    p.setNeedsDelete(true);
                }
            }
        }

        // 3. Perform the unified save (Local + Cloud Batch)
        saveData(context, (success, message) -> {
            if (success) {
                // Only remove from the local memory lists AFTER cloud success
                bills.getBills().removeIf(Bill::isNeedsDelete);
                if (payments != null) {
                    payments.getPayments().removeIf(Payment::isNeedsDelete);
                }

                // Mirror the 'clean' state to disk (removing the deleted items from JSON)
                writeToDisk(context, (s, m) -> callback.onComplete(true, "Bill and associated data deleted."));
            } else {
                // If it failed, we don't remove them from the list,
                // so the user can try again.
                callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public void sortBills() {
        bills.setBills((ArrayList<Bill>) bills.getBills().stream().distinct().collect(Collectors.toList()));
    }

    public void sortPaymentsByDueDate() {
        payments.getPayments().sort(Comparator.comparing(Payment::getDueDate));
    }

    // --- PAYMENT OPERATIONS ---

    public void addPayment(Payment payment, Context context, OnCompleteCallback callback) {
        if (payments == null) payments = new Payments(new ArrayList<>());
        payments.getPayments().add(payment);

        db.collection("users").document(uid).collection("payments")
                .document(String.valueOf(payment.getPaymentId())).set(payment, SetOptions.merge());

        saveData(context, (wasSuccessful, message) -> {
            if (wasSuccessful) {
                callback.onComplete(true, "Payment created successfully.");
            }
            if (!wasSuccessful) {
                callback.onComplete(false, message);
            }
        });
    }

    public void deletePayment(int paymentId, Context context, OnCompleteCallback callback) {
        if (uid == null || payments == null) {
            if (callback != null) callback.onComplete(false, "Session or data missing.");
            return;
        }

        Payment toRemove = getPaymentById(paymentId);
        if (toRemove == null) {
            if (callback != null) callback.onComplete(false, "Payment not found.");
            return;
        }

        // 1. Mark the payment for deletion
        toRemove.setNeedsDelete(true);

        // 2. Revert Bill Balance using the Bill.Builder
        if (toRemove.isPaid() || toRemove.getPartialPayment() > 0) {
            Bill parentBill = getBillByName(toRemove.getBillerName());

            if (parentBill != null) {
                double revertAmount = toRemove.isPaid() ? toRemove.getPaymentAmount() : toRemove.getPartialPayment();

                // Use the Builder to update the bill.
                // Note: We don't call .save() here because we want to batch it with the payment deletion below.
                new Bill.Builder(context, parentBill)
                        .setPaymentsRemaining(parentBill.getPaymentsRemaining() + 1)
                        .setBalance(parentBill.getBalance() + revertAmount);
            }
        }

        // 3. Perform the unified save
        // This batch will include the Payment DELETE and the Bill UPDATE
        saveData(context, (success, message) -> {
            if (success) {
                // Remove from local memory only after cloud success
                payments.getPayments().removeIf(Payment::isNeedsDelete);

                // Mirror clean state to disk
                writeToDisk(context, (s, m) -> {
                    if (callback != null) callback.onComplete(true, "Payment deleted and Bill balance adjusted.");
                });
            } else {
                if (callback != null) callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public void saveDataForWorker(Context context, ArrayList<Payment> payments, String channelId) {
        SharedPreferences prefs = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        editor.putString("payments", gson.toJson(payments));
        editor.putString("channelId", channelId);
        editor.apply();
    }

    public String getSavedChannelId(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getString("1234567890", "1234567890");
    }

    public void setSavedChannelId(Context context, String channelId) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).edit().putString("1234567890", "1234567890").apply();
    }

    public Payment getPaymentById(int id) {
        if (payments == null) return null;
        for (Payment p : payments.getPayments()) {
            if (p.getPaymentId() == id) return p;
        }
        return null;
    }

    public Payment getPaymentByBillerName(String billerName) {
        if (payments == null) return null;
        for (Payment p : payments.getPayments()) {
            if (p.getBillerName().equals(billerName)) return p;
        }
        return null;
    }

    public ArrayList<Payment> getPayments() {
        if (payments == null || payments.getPayments() == null) {
            payments = new Payments(new ArrayList<>());
        }
        return payments.getPayments();
    }

    // --- EXPENSE OPERATIONS ---

    public void addExpense(Expense expense, Context context, OnCompleteCallback callback) {
        if (expenses == null) expenses = new Expenses(new ArrayList<>());

        // Generate ID if missing (using BillerManager.id logic)
        if (expense.getId() == null) expense.setId(String.valueOf(BillerManager.id()));

        expenses.getExpenses().add(expense);

        db.collection("users").document(uid).collection("expenses")
                .document(expense.getId()).set(expense, SetOptions.merge());

        saveData(context, (wasSuccessful, message) -> {
            if (wasSuccessful) {
                callback.onComplete(true, "Expense created successfully.");
            }
            if (!wasSuccessful) {
                callback.onComplete(false, message);
            }
        });
    }

    public void deleteExpense(String expenseId, Context context, OnCompleteCallback callback) {
        if (uid == null || expenses == null) {
            if (callback != null) callback.onComplete(false, "Session or data missing.");
            return;
        }

        Expense toRemove = getExpenseById(expenseId);
        if (toRemove == null) {
            if (callback != null) callback.onComplete(false, "Expense not found.");
            return;
        }

        // 1. Mark the expense for deletion (picked up by saveData batch)
        toRemove.setNeedsDelete(true);

        // 2. Perform the unified save
        // This batch handles the Expense DELETE and the User UPDATE atomically
        saveData(context, (success, message) -> {
            if (success) {
                // Remove from local memory only after cloud confirmation
                expenses.getExpenses().removeIf(Expense::isNeedsDelete);

                // Mirror clean state to disk (update JSON)
                writeToDisk(context, (s, m) -> {
                    if (callback != null) callback.onComplete(true, "Expense deleted successfully.");
                });
            } else {
                if (callback != null) callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public Expense getExpenseById(String id) {
        if (expenses == null) return null;
        for (Expense e : expenses.getExpenses()) {
            if (e.getId() != null && e.getId().equals(id)) return e;
        }
        return null;
    }

    public ArrayList<Expense> getExpenses() {
        if (expenses == null || expenses.getExpenses() == null) {
            expenses = new Expenses(new ArrayList<>());
        }
        return expenses.getExpenses();
    }

    public interface OnCompleteCallback {
        void onComplete(boolean wasSuccessful, String message);

        default void log(boolean success, String message) {
            String tag = Repository.class.getSimpleName();
            if (success) {
                Log.i(tag, "[Operation Success] " + message);
            } else {
                Log.e(tag, "[Operation Failed] " + message);
            }
            onComplete(success, message);
        }
    }
}