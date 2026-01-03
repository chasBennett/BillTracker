package com.example.billstracker.tools;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.billstracker.tools.BillerManager.id;

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
import com.example.billstracker.custom_objects.Biller;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Repo {
    private static Repo instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Gson gson = new Gson();

    // In-memory cache
    private User thisUser;
    private Payments payments;
    private Expenses expenses;
    private Bills bills;
    private String uid;
    private static final String KEY_PAYMENTS = "payments_list";

    private Repo() {}

    public static synchronized Repo getInstance() {
        if (instance == null) instance = new Repo();
        return instance;
    }

    /**
     * Initializes Firebase and calls FirebaseAuth.getInstance().useAppLanguage().
     * Then loads the savedUid, thisUser, bills, payments, and expenses from the SharedPreferences branch specific to the saved uid or returns if the uid is null.
     * @param context The application's context.
     */
    public void initialize(Context context) {
        context = context.getApplicationContext();
        FirebaseApp.initializeApp(context);
        FirebaseAuth.getInstance().useAppLanguage();
        loadLocalData(context);
    }

    /**
     * Saves the user's data to a SharedPreferences branch that is specific to their uid.
     * Elements saved include User, Bills, Payments, and Expenses.
     * @param context The application's context.
     */
    public void saveLocalData (Context context) {
        context = context.getApplicationContext();
        if (uid == null) {
            return;
        }
        SharedPreferences.Editor editor = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).edit();
        editor.putString("user_json", gson.toJson(thisUser));
        editor.putString("bills_json", gson.toJson(bills));
        editor.putString("payments_json", gson.toJson(payments));
        editor.putString("expenses_json", gson.toJson(expenses));
        editor.apply();
    }

    /**
     * Loads the user's data from a sharedPreferences branch that is specific to their uid.
     * @param context The application's context.
     */
    public void loadLocalData(Context context) {
        context = context.getApplicationContext();
        if (uid == null) {
            retrieveSavedUid(context);
            if (uid == null) return;
        }

        SharedPreferences prefs = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE);
        String userJson = prefs.getString("user_json", null);

        if (userJson != null) {
            thisUser = gson.fromJson(userJson, User.class);
        }

        if (thisUser == null) {
            thisUser = new User();
        } else {
            if (thisUser.getBudgets() == null) thisUser.setBudgets(new ArrayList<>());
            if (thisUser.getPartners() == null) thisUser.setPartners(new ArrayList<>());
            if (thisUser.getBills() == null) thisUser.setBills(new ArrayList<>());
        }

        bills = gson.fromJson(prefs.getString("bills_json", null), Bills.class);
        payments = gson.fromJson(prefs.getString("payments_json", null), Payments.class);
        expenses = gson.fromJson(prefs.getString("expenses_json", null), Expenses.class);

        if (bills == null) bills = new Bills(new ArrayList<>());
        if (payments == null) payments = new Payments(new ArrayList<>());
        if (expenses == null) expenses = new Expenses(new ArrayList<>());
    }

    /**
     * Saves the current user's uid to a global SharedPreferences instance for use during future logins.
     * @param uid The user UID.
     * @param context The application's context
     */
    public void setSavedUid(String uid, Context context) {
        context = context.getApplicationContext();
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).edit().putString("lastUid", uid)
                .apply();
    }

    public void retrieveSavedUid(Context context) {
        String value = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getString("lastUid", "");
        if (!value.isEmpty()) {
            uid = value;
        }
    }

    public void logout(Context context) {
        thisUser = null;
        payments = null;
        expenses = null;
        bills = null;
        uid = null;

        setStaySignedIn(false, context);
        FirebaseAuth.getInstance().signOut();
        Prefs.setSignedInWithGoogle((Activity) context, false);

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

    public void setOwnership () {
        if (uid != null) {
            if (bills != null && !bills.getBills().isEmpty()) {
                for (Bill bill : bills.getBills()) {
                    if (bill.getOwner() == null) {
                        bill.setOwner(uid);
                    }
                }
            }
            if (payments != null && !payments.getPayments().isEmpty()) {
                for (Payment payment: payments.getPayments()) {
                    if (payment.getOwner() == null) {
                        payment.setOwner(uid);
                    }
                }
            }
            if (expenses != null && !expenses.getExpenses().isEmpty()) {
                for (Expense expense: expenses.getExpenses()) {
                    if (expense.getOwner() == null) {
                        expense.setOwner(uid);
                    }
                }
            }
        }
    }

    public boolean getStaySignedIn(Context context) {
        boolean value = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getBoolean("stay_signed_in", false);
        if (value) {
            uid = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getString("lastUid", "");
            return !uid.isEmpty();
        }
        return false;
    }

    public void setStaySignedIn(boolean value, Context context) {
        if (!getAllowBiometrics(context) && !value) {
            clearLoginCredentials(context);
        }
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("stay_signed_in", value)
                .putString("lastUid", uid)
                .apply();
    }

    // --- ALLOW BIOMETRIC SIGN IN ---
    public boolean getAllowBiometrics(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getBoolean("allow_biometrics", false);
    }

    public void setAllowBiometrics(boolean value, Context context) {
        if (!getStaySignedIn(context) && !value) {
            clearLoginCredentials(context);
        }
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("allow_biometrics", value)
                .apply();
    }

    // --- ALLOW BIOMETRIC PROMPT (The "Ask Again" logic) ---
    public boolean getShowBiometricPrompt(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getBoolean("show_biometric_prompt", true);
    }

    public void setShowBiometricPrompt(boolean value, Context context) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("show_biometric_prompt", value)
                .apply();
    }

    public void saveCredentials(Context context, String email, String password) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .putString("saved_email", email)
                .putString("saved_password", password)
                .apply();
    }

    public String getSavedEmail(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getString("saved_email", "");
    }

    public String getSavedPassword(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .getString("saved_password", "");
    }

    public void clearLoginCredentials(Context context) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE)
                .edit()
                .remove("saved_email")
                .remove("saved_password")
                .apply();
        saveLocalData(context);
    }

    public void save(Context context) {
        if (uid == null || thisUser == null) return;

        if (thisUser.getBills() != null || thisUser.getPhoneNumber() != null || thisUser.getTermsAcceptedOn() != null) {
            thisUser = new User(thisUser.getUserName(), thisUser.getPassword(), thisUser.getName(), thisUser.isAdmin(), thisUser.getLastLogin(), thisUser.getDateRegistered(), thisUser.getId(), thisUser.getTotalLogins(),
                    thisUser.getTicketNumber(), thisUser.getIncome(), thisUser.getPayFrequency(), thisUser.getTrophies(), thisUser.getBudgets(), thisUser.getPartners(), 0);
            db.collection("users").document(uid).set(thisUser);
        }
        else {
            if (uid != null) {
                db.collection("users").document(uid).set(thisUser, SetOptions.merge());
            }
        }

        ArrayList<Payment> save = new ArrayList<>();
        for (Payment payment : payments.getPayments()) {
            if (payment.isPaid() || payment.isDateChanged()) {
                save.add(payment);
            }
        }

        if (!save.isEmpty()) {
            for (Payment payment : save) {
                db.collection("users").document(payment.getOwner()).collection("payments").document(String.valueOf(payment.getPaymentId())).set(payment, SetOptions.merge());
            }
        }

        if (bills == null) {
            bills = new Bills(new ArrayList<>());
        }

        for (Bill bill : bills.getBills()) {
            db.collection("users").document(bill.getOwner()).collection("bills").document(bill.getBillerName()).set(bill, SetOptions.merge());
        }

        if (expenses != null && expenses.getExpenses() != null && !expenses.getExpenses().isEmpty() && uid != null) {
            int id = id();
            for (Expense expense : expenses.getExpenses()) {
                if (expense.getId() == null) {
                    expense.setId(String.valueOf(id));
                    ++id;
                }
                db.collection("users").document(expense.getOwner()).collection("expenses").document(expense.getId()).set(expense, SetOptions.merge());
            }
        }
        saveLocalData(context);
    }

    public void fetchCloudData(String userUid, OnCompleteCallback callback) {
        this.uid = userUid;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                this.thisUser = doc.toObject(User.class);

                final int TOTAL_SUBCOLLECTIONS = 3;
                final int[] loadedCount = {0};

                Runnable checkTaskCompletion = () -> {
                    loadedCount[0]++;
                    if (loadedCount[0] == TOTAL_SUBCOLLECTIONS) {
                        callback.onComplete(true, "Data retrieved successfully for user UID: " + userUid);
                    }
                };

                db.collection("users").document(uid).collection("bills").get()
                        .addOnSuccessListener(querySnapshot -> {
                            ArrayList<Bill> billList = (ArrayList<Bill>) querySnapshot.toObjects(Bill.class);
                            this.bills = new Bills(billList);
                            checkTaskCompletion.run();
                        });

                db.collection("users").document(uid).collection("payments").get()
                        .addOnSuccessListener(querySnapshot -> {
                            ArrayList<Payment> paymentList = (ArrayList<Payment>) querySnapshot.toObjects(Payment.class);
                            this.payments = new Payments(paymentList);
                            checkTaskCompletion.run();
                        });

                db.collection("users").document(uid).collection("expenses").get()
                        .addOnSuccessListener(querySnapshot -> {
                            ArrayList<Expense> expenseList = (ArrayList<Expense>) querySnapshot.toObjects(Expense.class);
                            this.expenses = new Expenses(expenseList);
                            checkTaskCompletion.run();
                        });
            } else {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.getEmail() != null && user.getDisplayName() != null) {
                        User newUser = new User(user.getEmail(), uid, user.getDisplayName(), false, true, DateFormat.createCurrentDateStringWithTime(), DateFormat.createCurrentDateStringWithTime(), uid, 0,
                                "0", 0, 2, new ArrayList<>(), new ArrayList<>(), 1);
                        db.collection("users").document(uid).set(newUser).addOnCompleteListener(task -> {
                            Repo.getInstance().setUser(newUser);
                            callback.onComplete(true, "New user file created for user UID: " + userUid);
                        });
                    }
                }
                else {
                    callback.onComplete(false, "Failed to retrieve data for user UID: " + userUid);
                }
            }
        }).addOnFailureListener(e -> callback.onComplete(false, "Failed to retrieve data for user UID: " + userUid));
    }

    public void saveBillers (Activity activity, ArrayList<Biller> billers) {

        Set<Biller> billerSet = new LinkedHashSet<>(billers);
        SharedPreferences prefs = activity.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(billerSet);
        editor.putString("billers", json);
        editor.apply();
    }
    public ArrayList <Biller> getBillers (Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("billers", null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Biller>>() {}.getType();
        return gson.fromJson(json, type);

    }

    public void removeDuplicateBills () {
        bills = new Bills(new ArrayList<>(new HashSet<>(bills.getBills())));
    }

    public void removeDuplicatePayments () {
        payments = new Payments(new ArrayList<>(new HashSet<>(payments.getPayments())));
    }

    public void loadPartnerData (String userId, OnCompleteCallback onCompleteCallback) {
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
                        thisUser = new User(thisUser.getUserName(), thisUser.getPassword(), thisUser.getName(), thisUser.isAdmin(), thisUser.getLastLogin(), thisUser.getDateRegistered(), thisUser.getId(), thisUser.getTotalLogins(),
                                thisUser.getTicketNumber(), thisUser.getIncome(), thisUser.getPayFrequency(), thisUser.getTrophies(), thisUser.getBudgets(), thisUser.getPartners(), thisUser.getVersionNumber());
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
                    }
                    else {
                        if (expenses == null || expenses.getExpenses() == null) {
                            expenses = new Expenses(new ArrayList<>());
                        }
                    }
                    onCompleteCallback.onComplete(true, "Partner data was loaded successfully.");
                });
            });
        });
    }

    // --- UID UPDATER ---
    public void setUid(String uid, Context context) {
        this.uid = uid;
        setSavedUid(uid, context);
        loadLocalData(context);
    }

    public String getUid() {
        return uid;
    }

    // --- USER OPERATIONS (Account Deletion) ---

    public User getUser (Context context) {
        if (thisUser == null) {
            loadLocalData(context);
            if (thisUser == null) {
                thisUser = new User();
            }
        }
        return thisUser;
    }

    public void addUser(String userName, String password, String name, boolean admin, String lastLogin, String id, Context context) {

        if (id == null) return;

        this.thisUser = new User(userName, password, name, admin, false, lastLogin, DateFormat.createCurrentDateStringWithTime(), id, new ArrayList<>(), 0, id, 0, 2, true,
                DateFormat.createCurrentDateStringWithTime(), new ArrayList<>(), new ArrayList<>(), "",new ArrayList<>(), 1);

        this.uid = id;

        db.collection("users").document(uid).set(thisUser);

        saveLocalData(context);
    }

    public void deleteUserAccount(Context context, AuthCredential credential, OnCompleteCallback callback) {
        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null || uid == null) {
            callback.onComplete(false, "No active session found.");
            return;
        }

        authUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (reAuthTask.isSuccessful()) {
                performFullDataWipe(context, authUser, callback);
            } else {
                callback.onComplete(false, "Authentication failed. Please verify your credentials.");
            }
        });
    }

    private void performFullDataWipe(Context context, FirebaseUser authUser, OnCompleteCallback callback) {
        String[] subCollections = {"bills", "payments", "expenses"};
        final int[] collectionsProcessed = {0};

        for (String sub : subCollections) {
            db.collection("users").document(uid).collection(sub).get().addOnSuccessListener(snapshot -> {
                WriteBatch batch = db.batch();
                for (DocumentSnapshot doc : snapshot) {
                    batch.delete(doc.getReference());
                }
                batch.commit().addOnCompleteListener(task -> {
                    collectionsProcessed[0]++;
                    if (collectionsProcessed[0] == subCollections.length) {
                        db.collection("users").document(uid).delete().addOnSuccessListener(aVoid -> authUser.delete().addOnCompleteListener(authDeleteTask -> {
                            if (authDeleteTask.isSuccessful()) {
                                wipeLocalData(context);
                                callback.onComplete(true, "Account and data deleted.");
                            } else {
                                callback.onComplete(false, "Data wiped, but account removal failed.");
                            }
                        }));
                    }
                });
            });
        }
    }

    private void wipeLocalData(Context context) {
        context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).edit().clear().apply();
        thisUser = null;
        bills = null;
        payments = null;
        expenses = null;
        uid = null;
        setAllowBiometrics(false, context);
        setShowBiometricPrompt(true, context);
    }

    public void updateUser(Context context, UserUpdater updater) {
        if (thisUser != null) {
            updater.update(thisUser);
            save(context);
        }
    }

    public void changeUser (String newUid, Context context) {
        this.uid = newUid;
        clearLoginCredentials(context);
        loadLocalData(context);
    }

    public void setUser (User user) {
        this.thisUser = user;
    }

    public Bill getBillByName(String name) {
        if (bills == null) return null;
        for (Bill bill : bills.getBills()) {
            if (bill.getBillerName().equalsIgnoreCase(name)) return bill;
        }
        return null;
    }

    public Bill getBillById(String billId) {
        if (bills == null) return null;
        for (Bill bill : bills.getBills()) {
            if (bill.getBillsId().equalsIgnoreCase(billId)) return bill;
        }
        return null;
    }

    public void addBill(Bill bill, Context context) {
        if (bills == null) bills = new Bills(new ArrayList<>());
        bills.getBills().add(bill);

        db.collection("users").document(uid).collection("bills")
                .document(bill.getBillerName()).set(bill, SetOptions.merge());

        saveLocalData(context);
    }

    public void updateBill(String billerName, Context context, BillUpdater updater) {
        Bill bill = getBillByName(billerName);
        if (bill != null) {
            updater.update(bill);
            save(context);
        }
    }

    public void updateBillById(String billerId, Context context, BillUpdater updater) {
        Bill bill = getBillById(billerId);
        if (bill != null) {
            updater.update(bill);
            BillerManager.refreshPayments(context);
            save(context);
        }
    }

    public void updateBill(Bill bill, Context context, BillUpdater updater) {
        if (bill != null) {
            updater.update(bill);
            BillerManager.refreshPayments(context);
            save(context);
        }
    }

    public void deleteBill(String billerName, boolean deletePayments, Context context) {
        if (bills == null || uid == null) return;

        Bill toRemove = getBillByName(billerName);
        if (toRemove != null) {
            bills.getBills().remove(toRemove);

            db.collection("users").document(uid).collection("bills")
                    .document(billerName).delete();

            if (deletePayments) {
                deletePaymentsForBill(billerName);
            } else {
                BillerManager.refreshPayments(context);
            }

            saveLocalData(context);
        }
    }

    public ArrayList <Bill> getBills () {
        if (bills == null || bills.getBills() == null) {
            bills = new Bills (new ArrayList<>());
        }
        return bills.getBills();
    }

    public void sortBills () {
        bills.setBills((ArrayList<Bill>) bills.getBills().stream().distinct().collect(Collectors.toList()));
    }

    public void sortPaymentsByDueDate () {
        payments.getPayments().sort(Comparator.comparing(Payment::getDueDate));
    }
    public void deletePaymentsForBill(String billerName) {
        if (payments == null || payments.getPayments() == null) return;

        ArrayList<Payment> toDelete = new ArrayList<>();

        for (Payment p : payments.getPayments()) {
            if (p.getBillerName().equalsIgnoreCase(billerName)) {
                toDelete.add(p);

                db.collection("users").document(uid).collection("payments")
                        .document(String.valueOf(p.getPaymentId())).delete();
            }
        }

        payments.getPayments().removeAll(toDelete);
    }

    // --- PAYMENT OPERATIONS ---

    public void addPayment(Payment payment, Context context) {
        if (payments == null) payments = new Payments(new ArrayList<>());
        payments.getPayments().add(payment);

        db.collection("users").document(uid).collection("payments")
                .document(String.valueOf(payment.getPaymentId())).set(payment, SetOptions.merge());

        saveLocalData(context);
    }

    public void deletePayment(int paymentId, Context context) {
        Payment toRemove = getPaymentById(paymentId);
        if (toRemove != null) {
            payments.getPayments().remove(toRemove);
            db.collection("users").document(uid).collection("payments")
                    .document(String.valueOf(paymentId)).delete();
            if (toRemove.isPaid() || toRemove.getPartialPayment() > 0) {
                updateBill(toRemove.getBillerName(), context, bill -> {
                    if (bill != null) {
                        bill.setPaymentsRemaining(bill.getPaymentsRemaining() + 1);
                        if (toRemove.isPaid()) {
                            bill.setBalance(bill.getBalance() + toRemove.getPaymentAmount());
                        }
                        else {
                            bill.setBalance(bill.getBalance() + toRemove.getPartialPayment());
                        }
                    }
                });
            }
            saveLocalData(context);
        }
    }

    public void saveDataForWorker(Context context, ArrayList<Payment> payments, String channelId) {
        SharedPreferences prefs = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        editor.putString(KEY_PAYMENTS, gson.toJson(payments));
        editor.putString("channelId", channelId);
        editor.apply();
    }

    public ArrayList<Payment> getSavedPayments(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PAYMENTS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<Payment>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public String getSavedChannelId(Context context) {
        return context.getSharedPreferences("Global_Preferences", Context.MODE_PRIVATE).getString(getUid(), "default_channel");
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

    public void updatePayment(int paymentId, Context context, PaymentUpdater updater) {
        Payment payment = getPaymentById(paymentId);
        if (payment != null) {
            updater.update(payment);
            save(context);
        }
    }

    public void updatePayment(Payment payment, Context context, PaymentUpdater updater) {
        if (payment != null) {
            updater.update(payment);
            save(context);
        }
    }

    public ArrayList <Payment> getPayments() {
        if (payments == null || payments.getPayments() == null) {
            payments = new Payments(new ArrayList<>());
        }
        return payments.getPayments();
    }

    // --- EXPENSE OPERATIONS ---

    public void addExpense(Expense expense, Context context) {
        if (expenses == null) expenses = new Expenses(new ArrayList<>());

        // Generate ID if missing (using BillerManager.id logic)
        if (expense.getId() == null) expense.setId(String.valueOf(BillerManager.id()));

        expenses.getExpenses().add(expense);

        db.collection("users").document(uid).collection("expenses")
                .document(expense.getId()).set(expense, SetOptions.merge());

        saveLocalData(context);
    }

    public void deleteExpense(String expenseId, Context context) {
        Expense toRemove = getExpenseById(expenseId);
        if (toRemove != null) {
            expenses.getExpenses().remove(toRemove);
            db.collection("users").document(uid).collection("expenses")
                    .document(expenseId).delete();
            saveLocalData(context);
        }
    }

    public Expense getExpenseById(String id) {
        if (expenses == null) return null;
        for (Expense e : expenses.getExpenses()) {
            if (e.getId() != null && e.getId().equals(id)) return e;
        }
        return null;
    }

    public ArrayList <Expense> getExpenses () {
        if (expenses == null || expenses.getExpenses() == null) {
            expenses = new Expenses(new ArrayList<>());
        }
        return expenses.getExpenses();
    }

    public void updateExpense(String id, Context context, ExpenseUpdater updater) {
        Expense expense = getExpenseById(id);
        if (expense != null) {
            updater.update(expense);
            save(context);
        }
    }

    public void updateExpense(Expense expense, Context context, ExpenseUpdater updater) {
        if (expense != null) {
            updater.update(expense);
            save(context);
        }
    }

    public interface BillUpdater { void update(Bill bill); }
    public interface PaymentUpdater { void update(Payment payment); }
    public interface ExpenseUpdater { void update(Expense expense); }
    public interface UserUpdater { void update(User user); }

    public interface OnCompleteCallback {
        void onComplete(boolean wasSuccessful, String message);
    }
}