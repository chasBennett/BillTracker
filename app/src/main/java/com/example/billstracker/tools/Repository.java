package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
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
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Repository {
    private static Repository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Gson gson = new Gson();
    // In-memory cache
    private final InMemoryCache cache = new InMemoryCache();
    private final LocalStore localStore = new LocalStore();
    private static final String KEY_CHANNEL_ID = "channel_id";
    private static final String KEY_LAST_UID = "last_uid";
    public static final String KEY_UID = "uid";

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

    //----------Local Data Management----------

    private void writeToDisk(Context context, OnCompleteCallback callback) {
        localStore.writeAll(
                context,
                cache.uid,
                cache.thisUser,
                cache.bills,
                cache.payments,
                cache.expenses
        );
        callback.onComplete(true, "Local data saved successfully.");
    }

    public void clearDisk(Context context) {
        if (cache.uid == null) return;
        localStore.clear(context, cache.uid);
        //localStore.setNeedsDownload(context, cache.uid, true);
        String lastUid = getLastUid(context);
        String uid = getUid(context);
        cache.clear();
        setLastUid(context, lastUid);
        setUid(uid, context);
    }

    /**
     * Checks if the user data is loaded into memory.
     *
     * @return True if the user data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return cache.isLoaded();
    }

    public void saveDataForWorker(Context context, ArrayList<Payment> payments, String channelId) {
        SharedPreferences prefs = context.getSharedPreferences("Global_Preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        editor.putString("payments", gson.toJson(payments));
        editor.putString("channelId", channelId);
        editor.apply();
    }

    public String getSavedChannelId(Context context) {
        return context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .getString(KEY_CHANNEL_ID, null);
    }

    public void setSavedChannelId(Context context, String channelId) {
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .edit()
                .putString(KEY_CHANNEL_ID, channelId)
                .apply();
    }

    public void setNeedsDownload(Context context, boolean value) {
        if (cache.uid != null) {
            context.getSharedPreferences(cache.uid, MODE_PRIVATE).edit().putBoolean("needsDownload", value).apply();
        }
    }

    /**
     * Initializes Firebase and calls FirebaseAuth.getInstance().useAppLanguage().
     * Then loads the savedUid, thisUser, bills, payments, and expenses from the SharedPreferences branch specific to the saved uid or returns if the uid is null.
     *
     * @param context  The application's context.
     * @param callback A callback to be executed after the data is loaded.
     */
    public void initializeBackEnd(Context context, OnCompleteCallback callback) {
        context = context.getApplicationContext();
        FirebaseApp.initializeApp(context);
        FirebaseAuth.getInstance().useAppLanguage();
        loadLocalData(context, callback);
    }

    /**
     * Loads the user's data from a sharedPreferences branch that is specific to their uid.
     * Then loads the savedUid, thisUser, bills, payments, and expenses from the SharedPreferences branch specific to the saved uid or returns if the uid is null.
     *
     * @param context The application's context.
     * @param onComplete A callback to be executed after the data is loaded.
     */
    public void loadLocalData(Context context, OnCompleteCallback onComplete) {
        context = context.getApplicationContext();
        if (cache.uid == null) {
            cache.uid = getUid(context); // Actually assign the value
            if (cache.uid == null || cache.uid.isEmpty()) {
                // Even if there is no UID, we must trigger the callback so the UI finishes loading
                onComplete.onComplete(false, "No saved user found.");
                return;
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(cache.uid, MODE_PRIVATE);
        String userJson = prefs.getString("user_json", null);

        if (userJson != null) {
            cache.thisUser = gson.fromJson(userJson, User.class);
        }

        if (cache.thisUser == null) {
            cache.thisUser = new User();
            setNeedsDownload(context, true);
        }
        if (cache.thisUser.getBudgets() == null) cache.thisUser.setBudgets(new ArrayList<>());
        if (cache.thisUser.getPartners() == null) cache.thisUser.setPartners(new ArrayList<>());
        if (cache.thisUser.getBills() == null) cache.thisUser.setBills(new ArrayList<>());

        cache.bills = gson.fromJson(prefs.getString("bills_json", null), Bills.class);
        cache.payments = gson.fromJson(prefs.getString("payments_json", null), Payments.class);
        cache.expenses = gson.fromJson(prefs.getString("expenses_json", null), Expenses.class);

        if (cache.bills == null) cache.bills = new Bills(new ArrayList<>());
        if (cache.payments == null) cache.payments = new Payments(new ArrayList<>());
        if (cache.expenses == null) cache.expenses = new Expenses(new ArrayList<>());

        if (onComplete != null) {
            onComplete.onComplete(true, "Local data loaded successfully.");
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
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE).edit().putString(KEY_UID, uid)
                .apply();
        cache.uid = uid;
    }

    /**
     * Retrieves the user's uid from a global SharedPreferences instance.
     *
     * @param context The application's context.
     * @return The user's uid.
     *
     */
    public String getUid(Context context) {
        if (cache.uid != null) {
            return cache.uid;
        }
        // Just return the value or null; don't force a navigation here
        cache.uid = context.getSharedPreferences("Global_Preferences", MODE_PRIVATE).getString(KEY_UID, null);
        return cache.uid;
    }

    /**
     * Logs the user out of the application.
     *
     * @param context The application's context.
     *
     */
    public void logout(Context context) {

        if (context instanceof Activity) {
            Prefs.setSignedInWithGoogle((Activity) context, false);
        }

        cache.uid = null;

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
     * Checks if the user has previously elected to stay signed in.
     *
     * @param context The application's context.
     * @return True if the user chose to stay signed in, false otherwise.
     *
     */
    public boolean getStaySignedIn(Context context) {
        boolean value = context.getSharedPreferences("Global_Preferences", MODE_PRIVATE).getBoolean("stay_signed_in", false);
        if (value) {
            cache.uid = context.getSharedPreferences("Global_Preferences", MODE_PRIVATE).getString(KEY_UID, "");
            return !cache.uid.isEmpty();
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
            context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                    .edit()
                    .remove("saved_email")
                    .remove("saved_password")
                    .apply();
        }
        saveData(context, (wasSuccessful, message) -> Log.d("Repository Message", "Credentials cleared."));
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .edit()
                .putBoolean("stay_signed_in", value)
                .putString(KEY_UID, cache.uid)
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
        return context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
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
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .edit()
                .putBoolean("allow_biometrics", value)
                .apply();
    }

    // --- ALLOW BIOMETRIC PROMPT (The "Ask Again" logic) ---

    /**
     * Sets the user's preference for whether they want to allow a biometric prompt at sign in.
     *
     * @param value   True if the user chose to allow a biometric prompt, false otherwise.
     * @param context The application's context.
     *
     */
    public void setShowBiometricPrompt(boolean value, Context context) {
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .edit()
                .putBoolean("show_biometric_prompt", value)
                .apply();
    }

    public String getLastUid (Context context) {
        return context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .getString(KEY_LAST_UID, "");
    }

    public void setLastUid(Context context, String uid) {
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_UID, uid)
                .apply();
    }


    public User getUser(Context context) {
        if (cache.thisUser == null) {
            loadLocalData(context, (success, message) -> {
            });
        }
        return cache.thisUser;
    }

    /**
     * Deletes the user's local data.
     *
     * @param context The application's context.
     *
     */
    private void wipeLocalData(Context context) {
        // 1. Target the UID-specific file before clearing the UID reference
        if (cache.uid != null) {
            context.getSharedPreferences(cache.uid, MODE_PRIVATE).edit().clear().apply();
        }

        // 2. Clear Global Preferences (credentials, stay signed in)
        context.getSharedPreferences("Global_Preferences", MODE_PRIVATE).edit().clear().apply();

        // 3. Nullify memory references to trigger the BaseActivity Gatekeeper
        cache.clear();

        // 4. Reset biometric states
        setAllowBiometrics(false, context);
        setShowBiometricPrompt(true, context);
    }

    //----------Cloud Data Management----------

    /**
     * Fetches the user's cloud data and loads it into memory.
     *
     * @param userUid  The user's UID.
     * @param context  The application's context.
     * @param callback A callback to be executed after the data is fetched.
     *                 <p>
     *                 This callback will be executed on the main thread.
     */
    public void fetchCloudData(String userUid, Context context, OnCompleteCallback callback) {
        this.cache.uid = userUid;

        boolean diskMissing = !localStore.isDiskComplete(context, cache.uid);
        boolean flaggedForDownload = localStore.needsDownload(context, cache.uid);

        if (diskMissing || flaggedForDownload) {
            this.cache.bills = new Bills(new ArrayList<>());
            this.cache.payments = new Payments(new ArrayList<>());
            this.cache.expenses = new Expenses(new ArrayList<>());
            Context appContext = context.getApplicationContext();

            db.collection("users").document(cache.uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    this.cache.thisUser = doc.toObject(User.class);

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
                                    setNeedsDownload(context, false);
                                    callback.onComplete(true, "Cloud data synced successfully.");
                                }
                                if (!success) {
                                    callback.onComplete(false, message);
                                }
                            });
                        }
                    };

                    // Fetch Sub-collections
                    db.collection("users").document(cache.uid).collection("bills").get()
                            .addOnSuccessListener(snap -> {
                                this.cache.bills = new Bills((ArrayList<Bill>) snap.toObjects(Bill.class));
                                this.cache.bills = new Bills(new ArrayList<>(new HashSet<>(this.cache.bills.getBills())));
                                checkTaskCompletion.run();
                            });

                    db.collection("users").document(cache.uid).collection("payments").get()
                            .addOnSuccessListener(snap -> {
                                this.cache.payments = new Payments((ArrayList<Payment>) snap.toObjects(Payment.class));
                                this.cache.payments = new Payments(new ArrayList<>(new HashSet<>(this.cache.payments.getPayments())));
                                checkTaskCompletion.run();
                            });

                    db.collection("users").document(cache.uid).collection("expenses").get()
                            .addOnSuccessListener(snap -> {
                                this.cache.expenses = new Expenses((ArrayList<Expense>) snap.toObjects(Expense.class));
                                this.cache.expenses = new Expenses(new ArrayList<>(new HashSet<>(this.cache.expenses.getExpenses())));
                                checkTaskCompletion.run();
                            });

                } else {
                    // HANDLE NEW USER
                    handleNewUserCreation(userUid, appContext, callback);
                }
            }).addOnFailureListener(e -> callback.onComplete(false, e.getMessage()));
        }
        else {
            callback.onComplete(true, "Local data is up to date.");
        }
    }

    /**
     * Saves the user's data to a SharedPreferences branch that is specific to their uid.
     * Then it uploads the data to the Firebase Firestore instance.
     * Elements saved include User, Bills, Payments, and Expenses.
     * If the upload fails, creates a WorkManager instance to schedule a retry.
     *
     * @param context  The application's context.
     * @param callback A callback to be executed after the data is saved.
     */
    public void saveData(Context context, OnCompleteCallback callback) {
        Context appContext = context.getApplicationContext();
        if (cache.uid == null) {
            if (callback != null) callback.onComplete(false, "User ID is null.");
            return;
        }

        // 1. STEP ONE: Immediate Local Write
        // We save to disk right away. This ensures that even if the sync fails
        // or the app crashes, the 'needsSync' flags are stored on the device.
        writeToDisk(appContext, (success, message) -> {
            if (!success) {
                if (callback != null) callback.onComplete(false, message);
            }
        });

        WriteBatch batch = db.batch();
        boolean hasChanges = false;

        // Lists to track which objects were included in this specific batch
        List<Bill> dirtyBills = new ArrayList<>();
        List<Payment> dirtyPayments = new ArrayList<>();
        List<Expense> dirtyExpenses = new ArrayList<>();
        boolean userNeedsSync = false;

        // 2. STEP TWO: Prepare the Cloud Batch
        // Profile
        if (cache.thisUser != null && cache.thisUser.isNeedsSync()) {
            batch.set(db.collection("users").document(cache.uid), cache.thisUser, SetOptions.merge());
            userNeedsSync = true;
            hasChanges = true;
        }

        // Bills
        if (cache.bills != null && cache.bills.getBills() != null) {
            for (Bill bill : cache.bills.getBills()) {
                if (bill.isNeedsDelete() || bill.isNeedsSync()) {
                    DocumentReference ref = db.collection("users").document(cache.uid)
                            .collection("bills").document(bill.getBillerName());
                    if (bill.isNeedsDelete()) batch.delete(ref);
                    else batch.set(ref, bill, SetOptions.merge());
                    dirtyBills.add(bill);
                    hasChanges = true;
                }
            }
        }

        // Payments
        if (cache.payments != null && cache.payments.getPayments() != null) {
            for (Payment p : cache.payments.getPayments()) {
                if (p.isNeedsDelete() || p.isNeedsSync()) {
                    DocumentReference ref = db.collection("users").document(cache.uid)
                            .collection("payments").document(String.valueOf(p.getPaymentId()));
                    if (p.isNeedsDelete()) batch.delete(ref);
                    else batch.set(ref, p, SetOptions.merge());
                    dirtyPayments.add(p);
                    hasChanges = true;
                }
            }
        }

        // Expenses
        if (cache.expenses != null && cache.expenses.getExpenses() != null) {
            for (Expense e : cache.expenses.getExpenses()) {
                if (e.isNeedsDelete() || e.isNeedsSync()) {
                    DocumentReference ref = db.collection("users").document(cache.uid)
                            .collection("expenses").document(e.getId());
                    if (e.isNeedsDelete()) batch.delete(ref);
                    else batch.set(ref, e, SetOptions.merge());
                    dirtyExpenses.add(e);
                    hasChanges = true;
                }
            }
        }

        if (!hasChanges) {
            setNeedsDownload(context, false);
            if (callback != null) {
                callback.onComplete(true, "Local data saved. Cloud is already up to date.");
            }
            return;
        }

        // 3. STEP THREE: Execute Cloud Sync
        final boolean finalUserNeedsSync = userNeedsSync;
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 4. STEP FOUR: Success Handling
                // Clear flags in-memory because they are now confirmed in the cloud
                if (finalUserNeedsSync) cache.thisUser.setNeedsSync(false);
                for (Bill b : dirtyBills) b.setNeedsSync(false);
                for (Payment p : dirtyPayments) p.setNeedsSync(false);
                for (Expense e : dirtyExpenses) e.setNeedsSync(false);

                // Save the "Clean" state to disk
                writeToDisk(appContext, (success, msg) -> {
                    if (callback != null) callback.onComplete(true, "Synced successfully.");
                });
            } else {
                // 5. STEP FIVE: Failure Handling (Retain Locally)
                // We DO NOT clear the flags. Because of Step 1, the disk already has the
                // data flagged as 'true' for sync. The next time the app opens or saveData
                // is called, it will attempt to upload these again.
                scheduleRetry(appContext);
                String error = task.getException() != null ? task.getException().getMessage() : "Offline";
                if (callback != null) callback.onComplete(false, "Saved locally. Cloud sync pending: " + error);
            }
        });
    }

    /**
     * Re-attempts to sync the user's data with the cloud once internet access is restored.
     *
     * @param context The application's context.
     */
    private void scheduleRetry(Context context) {
        // Define constraints: Only run when the network is connected
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Create a unique work request
        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build();

        // Enqueue unique work so we don't have 10 workers running at once
        WorkManager.getInstance(context).enqueueUniqueWork(
                "cloud_sync_retry",
                ExistingWorkPolicy.REPLACE,
                syncRequest
        );
    }

    public User.Builder editUser(Context context) {
        User user = cache.thisUser;
        if (user == null) {
            return null;
        }
        return new User.Builder(context, user);
    }

    /**
     * Updates the current User profile using a functional action block.
     *
     * @param context    The application's context.
     * @param actions    A function that takes a User.Builder instance and modifies it.
     */
    public void updateUser(Context context, java.util.function.Consumer<User.Builder> actions, OnCompleteCallback callback) {
        // 1. Get the current user instance from the repository
        User currentUser = this.cache.thisUser;

        if (currentUser != null) {
            // 2. Initialize the Builder (User.java defines this inner class)
            User.Builder builder = new User.Builder(context, currentUser);

            // 3. Apply the requested changes via the consumer
            actions.accept(builder);

            // 4. Trigger the save process
            builder.save(callback);
        } else {
            if (callback != null) {
                callback.onComplete(false, "Update failed: No active user session found.");
            }
        }
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
     * Updates a bill's information.
     *
     * @param billerName The name of the biller.
     * @param context    The application's context.
     * @param actions    A function that takes a Bill.Builder instance and modifies it.
     * @param callback   A callback to be executed after the bill is updated.
     */
    public void updateBill(String billerName, Context context, Consumer<Bill.Builder> actions, OnCompleteCallback callback) {
        Bill.Builder builder = editBill(billerName, context);
        if (builder != null) {
            actions.accept(builder);
            builder.save(callback);
        } else if (callback != null) {
            callback.onComplete(false, "Bill could not be found");
        }
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
     * Updates a payment's information.
     *
     * @param identifier The identifier of the payment. This can accept an integer paymentId or a String billerName.
     * @param context   The application's context.
     * @param actions   A function that takes an Expense.Builder instance and modifies it.
     * @param callback  A callback to be executed after the expense is updated.
     */
    public void updatePayment(Object identifier, Context context, Consumer<Payment.Builder> actions, OnCompleteCallback callback) {
        Payment payment = null;

        if (identifier instanceof Integer) {
            payment = getPaymentById((Integer) identifier);
        } else if (identifier instanceof String) {
            payment = getPaymentByBillerName((String) identifier);
        }

        if (payment != null) {
            Payment.Builder builder = new Payment.Builder(context, payment);
            actions.accept(builder);
            builder.save(callback);
        } else if (callback != null) {
            callback.onComplete(false, "Payment not found for identifier: " + identifier);
        }
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
     * Updates an expense's information.
     *
     * @param expenseId The ID of the expense.
     * @param context   The application's context.
     * @param actions   A function that takes an Expense.Builder instance and modifies it.
     * @param callback  A callback to be executed after the expense is updated.
     */
    public void updateExpense(String expenseId, Context context, Consumer<Expense.Builder> actions, OnCompleteCallback callback) {
        Expense.Builder builder = editExpense(expenseId, context);
        if (builder != null) {
            actions.accept(builder);
            builder.save(callback);
        } else if (callback != null) {
            callback.onComplete(false, "Expense not found.");
        }
    }

    /**
     * Handles the creation of a new user account.
     *
     * @param uid      The user's UID.
     * @param context  The application's context.
     * @param callback A callback to be executed after the user account is created.
     */
    private void handleNewUserCreation(String uid, Context context, OnCompleteCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            User newUser = new User(user.getEmail(), uid, user.getDisplayName(), uid);

            // Ensure lists aren't null even for new users
            this.cache.bills = new Bills(new ArrayList<>());
            this.cache.payments = new Payments(new ArrayList<>());
            this.cache.expenses = new Expenses(new ArrayList<>());

            db.collection("users").document(uid).set(newUser).addOnCompleteListener(task -> {
                this.cache.thisUser = newUser;
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
     * Creates a new user account.
     *
     * @param email    The new User's email address.
     * @param password The new User's password.
     * @param name     The new User's name.
     * @param id       The new User's ID.
     * @param context  The application's context.
     * @return A new User.Builder instance.
     */
    public User.Builder addUser(String email, String password, String name, String id, Context context) {
        if (id == null) return null;

        // 1. Initialize the local User using the Safety Constructor
        // This ensures all ArrayLists (bills, budgets, etc.) are non-null
        this.cache.thisUser = new User(email, password, name, id);
        this.cache.uid = id;

        // 2. Return the Builder so the Activity can chain additional fields
        return new User.Builder(context, cache.thisUser);
    }

    /**
     * Deletes the user's account.
     *
     * @param context    The application's context.
     * @param credential The user's authentication credential.
     * @param callback   A callback to be executed after the user account is deleted.
     */
    public void deleteUserAccount(Context context, AuthCredential credential, OnCompleteCallback callback) {
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null || cache.uid == null) {
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
        if (partnerId == null || cache.uid == null) return;

        // 1. Identify the partner's document
        DocumentReference partnerRef = db.collection("users").document(partnerId);

        // 2. Fetch the partner's data to find the specific Partner object that matches YOU
        partnerRef.get().addOnSuccessListener(documentSnapshot -> {
            User partnerUser = documentSnapshot.toObject(User.class);
            if (partnerUser != null && partnerUser.getPartners() != null) {
                Partner meAsPartner = null;
                for (Partner p : partnerUser.getPartners()) {
                    if (p.getPartnerUid().equals(this.cache.uid)) {
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
     * @param context  The application's context.
     * @param authUser The user's authentication user.
     * @param callback A callback to be executed after the user account is deleted.
     */
    private void performFullDataWipe(Context context, FirebaseUser authUser, OnCompleteCallback callback) {
        String[] subCollections = {"bills", "payments", "expenses"};
        final int[] collectionsProcessed = {0};

        for (String sub : subCollections) {
            db.collection("users").document(cache.uid).collection(sub).get().addOnSuccessListener(snapshot -> {
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
     * @param counter  The number of sub-collections processed.
     * @param target   The total number of sub-collections.
     * @param authUser The user's authentication user.
     * @param context  The application's context.
     * @param callback A callback to be executed after the user account is deleted.
     */
    private void checkWipeProgress(int[] counter, int target, FirebaseUser authUser, Context context, OnCompleteCallback callback) {
        counter[0]++;
        if (counter[0] == target) {
            // Delete the main user document
            db.collection("users").document(cache.uid).delete().addOnSuccessListener(aVoid -> {
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

    public ArrayList<Bill> getBills() {
        if (cache.bills == null) return new ArrayList<>();
        return cache.bills.getBills();
    }

    /**
     * Retrieves a Bill from the Bills collection using the billerName field.
     *
     * @param name The name of the biller.
     * @return The Bill object with the specified name, or null if not found.
     */
    public Bill getBillByName(String name) {
        if (cache.bills == null) return null;
        for (Bill bill : cache.bills.getBills()) {
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
        if (cache.bills == null) return null;
        for (Bill bill : cache.bills.getBills()) {
            if (bill.getBillsId().equalsIgnoreCase(billId)) return bill;
        }
        return null;
    }

    public void addBill(Bill bill, Context context, OnCompleteCallback callback) {
        if (cache.bills == null) cache.bills = new Bills(new ArrayList<>());
        cache.bills.getBills().add(bill);

        db.collection("users").document(cache.uid).collection("bills")
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
        if (cache.uid == null || cache.bills == null) {
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
        if (cache.payments != null && cache.payments.getPayments() != null) {
            for (Payment p : cache.payments.getPayments()) {
                if (p.getBillerName().equalsIgnoreCase(billerName)) {
                    p.setNeedsDelete(true);
                }
            }
        }

        // 3. Perform the unified save (Local + Cloud Batch)
        saveData(context, (success, message) -> {
            if (success) {
                // Only remove from the local memory lists AFTER cloud success
                cache.bills.getBills().removeIf(Bill::isNeedsDelete);
                if (cache.payments != null) {
                    cache.payments.getPayments().removeIf(Payment::isNeedsDelete);
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
        cache.bills.setBills((ArrayList<Bill>) cache.bills.getBills().stream().distinct().collect(Collectors.toList()));
    }

    public void sortPaymentsByDueDate() {
        cache.payments.getPayments().sort(Comparator.comparing(Payment::getDueDate));
    }

    // --- PAYMENT OPERATIONS ---

    public void addPayment(Payment payment, Context context, OnCompleteCallback callback) {
        if (cache.payments == null) cache.payments = new Payments(new ArrayList<>());
        cache.payments.getPayments().add(payment);

        db.collection("users").document(cache.uid).collection("payments")
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
        if (cache.uid == null || cache.payments == null) {
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
                cache.payments.getPayments().removeIf(Payment::isNeedsDelete);

                // Mirror clean state to disk
                writeToDisk(context, (s, m) -> {
                    if (callback != null)
                        callback.onComplete(true, "Payment deleted and Bill balance adjusted.");
                });
            } else {
                if (callback != null) callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public Payment getPaymentById(int id) {
        if (cache.payments == null) return null;
        for (Payment p : cache.payments.getPayments()) {
            if (p.getPaymentId() == id) return p;
        }
        return null;
    }

    public Payment getPaymentByBillerName(String billerName) {
        if (cache.payments == null) return null;
        for (Payment p : cache.payments.getPayments()) {
            if (p.getBillerName().equals(billerName)) return p;
        }
        return null;
    }

    public ArrayList<Payment> getPayments() {
        if (cache.payments == null || cache.payments.getPayments() == null) {
            cache.payments = new Payments(new ArrayList<>());
        }
        return cache.payments.getPayments();
    }

    // --- EXPENSE OPERATIONS ---

    public void addExpense(Expense expense, Context context, OnCompleteCallback callback) {
        if (cache.expenses == null) cache.expenses = new Expenses(new ArrayList<>());

        // Generate ID if missing (using BillerManager.id logic)
        if (expense.getId() == null) expense.setId(String.valueOf(BillerManager.id()));

        cache.expenses.getExpenses().add(expense);

        db.collection("users").document(cache.uid).collection("expenses")
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
        if (cache.uid == null || cache.expenses == null) {
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
                cache.expenses.getExpenses().removeIf(Expense::isNeedsDelete);

                // Mirror clean state to disk (update JSON)
                writeToDisk(context, (s, m) -> {
                    if (callback != null)
                        callback.onComplete(true, "Expense deleted successfully.");
                });
            } else {
                if (callback != null) callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public Expense getExpenseById(String id) {
        if (cache.expenses == null) return null;
        for (Expense e : cache.expenses.getExpenses()) {
            if (e.getId() != null && e.getId().equals(id)) return e;
        }
        return null;
    }

    public ArrayList<Expense> getExpenses() {
        if (cache.expenses == null || cache.expenses.getExpenses() == null) {
            cache.expenses = new Expenses(new ArrayList<>());
        }
        return cache.expenses.getExpenses();
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