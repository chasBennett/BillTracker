package com.example.billstracker.tools;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.example.billstracker.activities.MainActivity2.dueThisMonth;
import static com.example.billstracker.activities.MainActivity2.selectedDate;
import static com.example.billstracker.tools.BillerManager.deleteFuturePayments;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.billstracker.R;
import com.example.billstracker.activities.MainActivity2;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.Notify;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public interface DataTools {
    static Bill getBill(String nameOrId) {
        if (nameOrId != null) {
            if (Repository.getInstance().getBills() != null) {
                for (Bill bill : Repository.getInstance().getBills()) {
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

    static void changePaymentDueDate(Context context, Payment payment, long newDueDate, boolean changeAll, FirebaseTools.FirebaseCallback callback) {
        Repository repo = Repository.getInstance();

        if (repo.getPayments() != null) {
            for (Payment existingPayment : repo.getPayments()) {
                if (existingPayment.getBillerName().equals(payment.getBillerName()) &&
                        existingPayment.getDueDate() == newDueDate &&
                        existingPayment.getPaymentId() != payment.getPaymentId()) {

                    Notify.createPopup((Activity) context, "A payment for this biller already exists on this date.", null);
                    callback.isSuccessful(false);
                    return;
                }
            }
        }

        if (changeAll) {
            // Step 1: Update Bill (uses internal context)
            Bill.Builder bill = repo.editBill(payment.getBillerName(), context);
            if (bill != null) {
                bill.setDueDate(newDueDate);
                bill.save((billSuccess, msg) -> {
                    if (!billSuccess) {
                        callback.isSuccessful(false);
                        return;
                    }

                    deleteFuturePayments(payment.getBillerName(), newDueDate, deleteSuccess -> {
                        // Step 2: Update specific payment
                        repo.editPayment(payment.getPaymentId(), context)
                                .setDueDate(newDueDate)
                                .setDateChanged(false)
                                .save((paySuccess, msg2) -> {
                                    if (paySuccess) {
                                        syncLocalData(payment, newDueDate, true);
                                        callback.isSuccessful(true);
                                    } else {
                                        callback.isSuccessful(false);
                                    }
                                });
                    });
                });
            }
        } else {
            repo.editPayment(payment.getPaymentId(), context)
                    .setDueDate(newDueDate)
                    .setDateChanged(true)
                    .save((wasSuccessful, message) -> {
                        if (wasSuccessful) {
                            syncLocalData(payment, newDueDate, false);
                            callback.isSuccessful(true);
                        } else {
                            callback.isSuccessful(false);
                        }
                    });
        }
    }

    private static void syncLocalData(Payment payment, long newDate, boolean updateBill) {
        payment.setDueDate(newDate);
        payment.setDateChanged(!updateBill);

        if (updateBill && Repository.getInstance().getBills() != null) {
            for (Bill bill : Repository.getInstance().getBills()) {
                if (bill.getBillerName().equals(payment.getBillerName())) {
                    bill.setDueDate(newDate);
                }
            }
        }
    }

    static ArrayList<String> getCategories(Context context) {
        if (context != null) {
            return new ArrayList<>(Arrays.asList(context.getString(R.string.autoLoan), context.getString(R.string.creditCard), context.getString(R.string.entertainment),
                    context.getString(R.string.insurance), context.getString(R.string.miscellaneous), context.getString(R.string.mortgage), context.getString(R.string.personalLoans), context.getString(R.string.utilities)));
        } else {
            return new ArrayList<>();
        }
    }

    static ArrayList<String> getFrequencies(Context context) {
        if (context != null) {
            return new ArrayList<>(Arrays.asList(context.getString(R.string.one_time), context.getString(R.string.daily), context.getString(R.string.weekly), context.getString(R.string.biweekly), context.getString(R.string.monthly),
                    context.getString(R.string.bi_monthly), context.getString(R.string.quarterly), context.getString(R.string.yearly)));
        } else {
            return new ArrayList<>();
        }
    }

    static ArrayList<String> getBudgetCategories(Context context) {

        if (context != null) {
            return new ArrayList<>(Arrays.asList(context.getString(R.string.clothing), context.getString(R.string.entertainment), context.getString(R.string.gas), context.getString(R.string.groceries),
                    context.getString(R.string.personal_care), context.getString(R.string.restaurants), context.getString(R.string.shopping)));
        } else {
            return new ArrayList<>();
        }
    }

    static ArrayList<Integer> getIcons() {
        return new ArrayList<>(Arrays.asList(R.drawable.auto, R.drawable.credit_card, R.drawable.entertainment, R.drawable.insurance,
                R.drawable.invoice, R.drawable.mortgage, R.drawable.personal_loan, R.drawable.utilities));
    }

    static Budget getBudget(Context context, int budgetId) {
        if (Repository.getInstance().getUser(context) != null && Repository.getInstance().getUser(context).getBudgets() != null) {
            for (Budget bud : Repository.getInstance().getUser(context).getBudgets()) {
                if (bud.getBudgetId() == budgetId) {
                    return bud;
                }
            }
        }
        return new Budget(0, 2, DateFormat.makeLong(LocalDate.now().minusMonths(6)), DateFormat.makeLong(LocalDate.now().plusMonths(6)), budgetId, 20, new ArrayList<>());
    }

    static ArrayList<Payment> whatsDueThisMonth(Context context) {
        dueThisMonth.clear();
        MainActivity2.pastDue = 0;

        // Calculate the time boundaries for the selected month
        long monthStart = DateFormat.makeLong(LocalDate.from(selectedDate.withDayOfMonth(1).atStartOfDay()));
        long monthEnd = DateFormat.makeLong(LocalDate.from(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()).atStartOfDay()));
        long today = DateFormat.currentDateAsLong();

        BillerManager.refreshPayments(context);
        ArrayList<Payment> payments = Repository.getInstance().getPayments();
        payments.sort(Comparator.comparing(Payment::getDueDate));

        if (!payments.isEmpty()) {
            for (Payment payment : payments) {
                long dueDate = payment.getDueDate();
                boolean isPaid = payment.isPaid();

                // 1. Payments that were due in prior months but are still unpaid (Past Due)
                // We only show these if the user is looking at the "current" month view
                boolean isCurrentMonthView = selectedDate.getMonth() == DateFormat.convertIntDateToLocalDate(today).getMonth()
                        && selectedDate.getYear() == DateFormat.convertIntDateToLocalDate(today).getYear();

                boolean isPastDueFromPrior = dueDate < monthStart && !isPaid && isCurrentMonthView;

                // 2. Payments that fall within the selected month's date range (Due this month)
                boolean isDueInSelectedMonth = dueDate >= monthStart && dueDate <= monthEnd;

                // 3. Payments that were paid in the selected month
                // Note: This assumes your Payment object has a getDatePaid() method.
                // If it doesn't, 'isDueInSelectedMonth' covers items due and paid in the same month.
                boolean wasPaidInSelectedMonth = isPaid && (dueDate >= monthStart && dueDate <= monthEnd);

                if (isPastDueFromPrior || isDueInSelectedMonth || wasPaidInSelectedMonth) {
                    if (!dueThisMonth.contains(payment)) {
                        dueThisMonth.add(payment);

                        // Increment the pastDue counter for the UI if it's actually late
                        if (dueDate < today && !isPaid) {
                            MainActivity2.pastDue++;
                        }
                    }
                }
            }
        }
        return clearDuplicatePayments(dueThisMonth);
    }

    static ArrayList<Payment> clearDuplicatePayments(ArrayList<Payment> paymentsList) {
        ArrayList<Payment> remove = new ArrayList<>();
        for (Payment payment : paymentsList) {
            boolean found = false;
            for (Payment pay : paymentsList) {
                if (pay.getPaymentId() == payment.getPaymentId()) {
                    if (!found) {
                        found = true;
                    } else {
                        remove.add(pay);
                    }
                }
            }
        }
        if (!remove.isEmpty()) {
            paymentsList.removeAll(remove);
        }
        return paymentsList;
    }

    static void getLatestBillersVersion(OnVersionRetrievedListener listener) {
        FirebaseFirestore.getInstance().collection("versions").document("billers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Long version = document.getLong("version");

                    if (version != null) {
                        Log.d(TAG, "Fetched version number: " + version);
                        listener.onComplete(true, Math.toIntExact(version));
                    } else {
                        Log.d(TAG, "Version field not found or is null in the document.");
                        listener.onComplete(false, 0);
                    }
                } else {
                    listener.onComplete(false, 0);
                }
            } else {
                listener.onComplete(false, 0);
                Log.e(TAG, "Failed to fetch document: ", task.getException());
            }
        });
    }

    interface OnVersionRetrievedListener {
        void onComplete(boolean wasSuccessful, int version);
    }
}
