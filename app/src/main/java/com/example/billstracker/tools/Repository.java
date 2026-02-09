package com.example.billstracker.tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Expenses;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.example.billstracker.custom_objects.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Repository {
    private static Repository instance;
    private final FirebaseFirestore db;
    private final InMemoryCache cache;
    private final LocalStore store;
    private Context context;
    private Repository (Context context) {
        this.context = context.getApplicationContext();
        this.cache = new InMemoryCache();
        this.store = new LocalStore(context.getApplicationContext(), this.cache);
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }

    public LocalStore getStore() {
        return store;
    }

    public InMemoryCache getCache() {
        return cache;
    }

    public Context getContext() {
        return context;
    }

    public void clearDisk () {
        if (cache.getUid() == null) return;
        store.clearUserPrefs(cache.getUid());
        store.setNeedsDownload(cache.getUid(), true);
        String uid = cache.getUid();
        cache.clear();
        setUid(uid);
        store.setLastUid(uid);
    }

    public void wipeDisk () {
        store.clearUserPrefs(cache.getUid());
        store.clearEncryptedPrefs();
        store.setNeedsDownload(cache.getUid(), true);
        cache.clear();
    }
    public boolean isStoreDataComplete () {
        return cache.isLoaded();
    }

    public void initializeBackEnd(OnCompleteCallback callback) {
        context = context.getApplicationContext();
        FirebaseApp.initializeApp(context);
        FirebaseAuth.getInstance().useAppLanguage();
        loadLocalData(callback);
    }

    public boolean checkIfGoogleUser () {
        if (store.getSignedInWithGoogle()) {
            store.setUid(store.getLastUid());
            return true;
        }
        else {
            return false;
        }
    }

    public void loginUser (String email, String password, String uid, boolean isGoogleUser, boolean needsDownload, OnCompleteCallback onComplete) {
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty() && uid != null && !uid.isEmpty() && FirebaseAuth.getInstance().getCurrentUser() != null) {
            store.saveCredentials(email, password, isGoogleUser);
            store.setLastUid(uid);
            setUid(uid);
            store.setNeedsDownload(uid, needsDownload);
            onComplete.onComplete(true, "User logged in successfully.");
        }
        else {
            if (email == null || email.isEmpty()) {
                onComplete.onComplete(false, "Email is invalid.");
            } else if (password == null || password.isEmpty()) {
                onComplete.onComplete(false, "Password is invalid.");
            } else if (uid == null || uid.isEmpty()) {
                onComplete.onComplete(false, "User ID is invalid.");
            } else {
                onComplete.onComplete(false, "User not logged in.");
            }
        }
    }

    public void loadLocalData(OnCompleteCallback onComplete) {
        if (cache.getUid() == null) {
            cache.setUid(getUid());
            if (cache.getUid() == null || cache.getUid().isEmpty()) {
                onComplete.onComplete(false, "No saved user found.");
                return;
            }
        }
        cache.setThisUser(store.getUser(cache.getUid()));
        if (cache.getThisUser() == null) {
            cache.setThisUser(new User());
            store.setNeedsDownload(getUid(), true);
        }

        if (cache.getThisUser().getBudgets() == null) cache.getThisUser().setBudgets(new ArrayList<>());
        if (cache.getThisUser().getPartners() == null) cache.getThisUser().setPartners(new ArrayList<>());
        if (cache.getThisUser().getBills() == null) cache.getThisUser().setBills(new ArrayList<>());

        cache.setBills(store.getBills(cache.getUid()));
        cache.setPayments(store.getPayments(cache.getUid()));
        cache.setExpenses(store.getExpenses(cache.getUid()));

        if (cache.getBills() == null) cache.setBills(new Bills(new ArrayList<>()));
        if (cache.getPayments() == null) cache.setPayments(new Payments(new ArrayList<>()));
        if (cache.getExpenses() == null) cache.setExpenses(new Expenses(new ArrayList<>()));

        if (onComplete != null) {
            onComplete.onComplete(true, "Local data loaded successfully.");
        }
    }

    public void setUid(String uid) {
        cache.setUid(uid);
        store.setUid(uid);
    }

    public String getUid() {
        return cache.getUid() != null ? cache.getUid() : store.getUid();
    }


    public User getUser() {
        if (cache.getThisUser() == null) {
            loadLocalData((success, message) -> {
            });
        }
        return cache.getThisUser();
    }

    //----------Cloud Data Management----------

    public void fetchCloudData(String userUid, OnCompleteCallback callback) {
        cache.setUid(userUid);

        boolean flaggedForDownload = store.getNeedsDownload(cache.getUid());

        if (!isStoreDataComplete() || flaggedForDownload) {
            Toast.makeText(context, "Fetching cloud data...", Toast.LENGTH_SHORT).show();
            FirebaseTools.loadCloudData(context, callback);
        }
        else {
            callback.onComplete(true, "Local data is up to date.");
        }
    }

    public void loadPartnerData(OnCompleteCallback callback) {

        User thisUser = getUser();
        if (thisUser.getPartners() == null) thisUser.setPartners(new ArrayList<>());

        if (!thisUser.getPartners().isEmpty()) {
            final int totalPartners = thisUser.getPartners().size();
            final int[] loadedPartners = {0};

            for (Partner partner : thisUser.getPartners()) {
                FirebaseTools.getPartner(context, partner, wasSuccessful -> {
                    loadedPartners[0]++;
                    if (loadedPartners[0] == totalPartners) {
                        callback.onComplete(true, "Partner data loaded successfully.");
                    }
                    else {
                        callback.onComplete(false, "Partner data failed to load.");
                    }
                });
            }
        } else {
            callback.onComplete(true, "No partners found.");
        }
    }

    void commitToDisk() {
        String uid = getUid();
        if (uid != null) {
            store.writeToDisk();
        }
    }

    void clearDirtyFlags(List<Bill> bills, List<Payment> payments, List<Expense> expenses, boolean userSynced) {
        if (userSynced) cache.getThisUser().setNeedsSync(false);

        for (Bill b : bills) b.setNeedsSync(false);
        for (Payment p : payments) p.setNeedsSync(false);
        for (Expense e : expenses) e.setNeedsSync(false);

        if (cache.getBills() != null) cache.getBills().getBills().removeIf(Bill::isNeedsDelete);
        if (cache.getPayments() != null) cache.getPayments().getPayments().removeIf(Payment::isNeedsDelete);
        if (cache.getExpenses() != null) cache.getExpenses().getExpenses().removeIf(Expense::isNeedsDelete);
    }

    void scheduleRetry() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).setConstraints(constraints).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES).build();

        WorkManager.getInstance(context).enqueueUniqueWork("cloud_sync_retry", ExistingWorkPolicy.REPLACE, syncRequest);
    }

    public User.Builder editUser() {
        User user = cache.getThisUser();
        if (user == null) {
            return null;
        }
        return new User.Builder(context, user);
    }

    public void updateUser(java.util.function.Consumer<User.Builder> actions, OnCompleteCallback callback) {
        User currentUser = this.cache.getThisUser();

        if (currentUser != null) {
            User.Builder builder = new User.Builder(context, currentUser);
            actions.accept(builder);
            builder.save(callback);
        } else {
            if (callback != null) {
                callback.onComplete(false, "Update failed: No active user session found.");
            }
        }
    }

    public Bill.Builder editBill(String billerName) {
        Bill bill = getBill(billerName);
        return new Bill.Builder(context, bill);
    }

    public void updateBill(String billerName, Consumer<Bill.Builder> actions, OnCompleteCallback callback) {
        Bill.Builder builder = editBill(billerName);
        if (builder != null) {
            actions.accept(builder);
            builder.save(callback);
        } else if (callback != null) {
            callback.onComplete(false, "Bill could not be found");
        }
    }

    public Payment.Builder editPayment(int paymentId) {
        return new Payment.Builder(context, getPayment(paymentId));
    }

    public void updatePayment(int paymentId, Consumer<Payment.Builder> actions, OnCompleteCallback callback) {
        performUpdate(getPayment(paymentId), actions, callback, String.valueOf(paymentId));
    }

    public void updatePayment(String billerName, Consumer<Payment.Builder> actions, OnCompleteCallback callback) {
        performUpdate(getPayment(billerName), actions, callback, billerName);
    }

    private void performUpdate(Payment payment, Consumer<Payment.Builder> actions, OnCompleteCallback callback, String idLabel) {
        if (payment != null) {
            Payment.Builder builder = new Payment.Builder(context, payment);
            actions.accept(builder);
            builder.save(callback);
        } else if (callback != null) {
            callback.onComplete(false, "Payment not found: " + idLabel);
        }
    }

    public Expense.Builder editExpense(String expenseId) {
        return new Expense.Builder(context, getExpenseById(expenseId));
    }

    public void updateExpense(String expenseId, Consumer<Expense.Builder> actions, OnCompleteCallback callback) {
        Expense.Builder builder = editExpense(expenseId);
        if (builder != null) {
            actions.accept(builder);
            builder.save(callback);
        } else if (callback != null) {
            callback.onComplete(false, "Expense not found.");
        }
    }

    public User.Builder addUser(String email, String password, String name, String id) {
        if (id == null) return null;
        cache.setThisUser(new User(email, password, name, id));
        cache.setUid(id);

        return new User.Builder(context, cache.getThisUser());
    }

    public ArrayList<Bill> getBills() {
        if (cache.getBills() == null) {
            cache.setBills(new Bills(new ArrayList<>()));
        }
        return cache.getBills().getBills();
    }

    public Bill getBill (String nameOrId) {
        if (cache.getBills() == null) return null;
        if (nameOrId != null) {
            if (getBills() != null) {
                for (Bill bill : getBills()) {
                    if (bill.getBillsId().equals(nameOrId)) {
                        return bill;
                    }
                    if (bill.getBillerName().equalsIgnoreCase(nameOrId)) {
                        return bill;
                    }
                }
            }
        }
        return null;
    }

    public void addBill(Bill bill, OnCompleteCallback callback) {
        if (cache.getBills() == null) cache.setBills(new Bills(new ArrayList<>()));
        bill.setNeedsSync(true);
        cache.getBills().getBills().add(bill);

        FirebaseTools.saveData(context, (wasSuccessful, message) -> {
            if (wasSuccessful) {
                callback.onComplete(true, "Bill created successfully.");
            }
            if (!wasSuccessful) {
                callback.onComplete(false, message);
            }
        });
    }

    public void deleteBill(String billerName, OnCompleteCallback callback) {
        if (cache.getUid() == null || cache.getBills() == null) {
            callback.onComplete(false, "Session or data missing.");
            return;
        }

        Bill toRemove = getBill(billerName);
        if (toRemove == null) {
            callback.onComplete(false, "Bill not found.");
            return;
        }

        toRemove.setNeedsDelete(true);

        if (cache.getPayments() != null && cache.getPayments().getPayments() != null) {
            for (Payment p : cache.getPayments().getPayments()) {
                if (p.getBillerName().equalsIgnoreCase(billerName)) {
                    p.setNeedsDelete(true);
                }
            }
        }

        FirebaseTools.saveData(context, (success, message) -> {
            if (success) {
                cache.getBills().getBills().removeIf(Bill::isNeedsDelete);
                if (cache.getPayments() != null) {
                    cache.getPayments().getPayments().removeIf(Payment::isNeedsDelete);
                }
                callback.onComplete(true, "Bill and associated data deleted.");
            } else {
                callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public void sortBills() {
        cache.getBills().setBills((ArrayList<Bill>) cache.getBills().getBills().stream().distinct().collect(Collectors.toList()));
    }

    public void sortPaymentsByDueDate() {
        cache.getPayments().getPayments().sort(Comparator.comparing(Payment::getDueDate));
    }

    public void addPayment(Payment payment, OnCompleteCallback callback) {
        if (cache.getPayments() == null) cache.setPayments(new Payments(new ArrayList<>()));
        payment.setNeedsSync(true);
        cache.getPayments().getPayments().add(payment);

        FirebaseTools.saveData(context, (wasSuccessful, message) -> {
            if (wasSuccessful) {
                callback.onComplete(true, "Payment created successfully.");
            }
            if (!wasSuccessful) {
                callback.onComplete(false, message);
            }
        });
    }

    public void deletePayment(int paymentId, OnCompleteCallback callback) {
        if (cache.getUid() == null || cache.getPayments() == null) {
            if (callback != null) callback.onComplete(false, "Session or data missing.");
            return;
        }

        Payment toRemove = getPayment(paymentId);
        if (toRemove == null) {
            if (callback != null) callback.onComplete(false, "Payment not found.");
            return;
        }
        toRemove.setNeedsDelete(true);

        if (toRemove.isPaid() || toRemove.getPartialPayment() > 0) {
            Bill parentBill = getBill(toRemove.getBillerName());

            if (parentBill != null) {
                parentBill.setBalance(parentBill.getBalance() + (toRemove.isPaid() ? toRemove.getPaymentAmount() : 0) + toRemove.getPartialPayment());
                parentBill.setNeedsSync(true);
                parentBill.setPaymentsRemaining(parentBill.getPaymentsRemaining() + 1);
            }
        }

        FirebaseTools.saveData(context, (success, message) -> {
            if (success) {
                if (callback != null) callback.onComplete(true, "Payment deleted successfully.");
            } else {
                if (callback != null) callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public Payment getPayment(int paymentId) {
        if (cache.getPayments() == null) return null;
        return cache.getPayments().getPayments().stream().filter(p -> p.getPaymentId() == paymentId).findFirst().orElse(null);
    }

    public Payment getPayment(String billerName) {
        if (cache.getPayments() == null) return null;
        return cache.getPayments().getPayments().stream().filter(p -> p.getBillerName().equalsIgnoreCase(billerName)).findFirst().orElse(null);
    }

    public ArrayList<Payment> getPayments() {
        if (cache.getPayments() == null || cache.getPayments().getPayments() == null) {
            cache.setPayments(new Payments(new ArrayList<>()));
        }
        return cache.getPayments().getPayments();
    }

    public ArrayList <Payment> getPaymentsByBillerName(String billerName) {
        if (cache.getPayments() == null) return null;
        ArrayList <Payment> foundPayments = new ArrayList<>();
        for (Payment p : cache.getPayments().getPayments()) {
            if (p.getBillerName().equals(billerName)) {
                foundPayments.add(p);
            }
        }
        return foundPayments;
    }

    public void addExpense(Expense expense, OnCompleteCallback callback) {
        if (cache.getExpenses() == null) cache.setExpenses(new Expenses(new ArrayList<>()));
        if (expense.getId() == null) expense.setId(String.valueOf(BillerManager.id()));
        expense.setNeedsSync(true);
        cache.getExpenses().getExpenses().add(expense);

        FirebaseTools.saveData(context, (wasSuccessful, message) -> {
            if (wasSuccessful) {
                if (callback != null) callback.onComplete(true, "Expense created successfully.");
            }
            if (!wasSuccessful) {
                if (callback != null) callback.onComplete(false, message);
            }
        });
    }

    public void deleteExpense(String expenseId, OnCompleteCallback callback) {
        if (cache.getUid() == null || cache.getExpenses() == null) {
            if (callback != null) callback.onComplete(false, "Session or data missing.");
            return;
        }

        Expense toRemove = getExpenseById(expenseId);
        if (toRemove == null) {
            if (callback != null) callback.onComplete(false, "Expense not found.");
            return;
        }
        toRemove.setNeedsDelete(true);
        FirebaseTools.saveData(context, (success, message) -> {
            if (success) {
                callback.onComplete(true, "Expense deleted successfully.");
            } else {
                if (callback != null) callback.onComplete(false, "Sync failed: " + message);
            }
        });
    }

    public Expense getExpenseById(String id) {
        if (cache.getExpenses() == null) return null;
        for (Expense e : cache.getExpenses().getExpenses()) {
            if (e.getId() != null && e.getId().equals(id)) return e;
        }
        return null;
    }

    public ArrayList<Expense> getExpenses() {
        if (cache.getExpenses() == null || cache.getExpenses().getExpenses() == null) {
            cache.setExpenses(new Expenses(new ArrayList<>()));
        }
        return cache.getExpenses().getExpenses();
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