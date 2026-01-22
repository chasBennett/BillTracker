package com.example.billstracker.tools;

import static com.example.billstracker.activities.MainActivity2.selectedDate;

import android.content.Context;

import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class BillerManager {

    public static void refreshPayments(Context context) {

        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }

        ArrayList<Payment> paymentList = new ArrayList<>();
        Repository.getInstance().getPayments().sort(Comparator.comparing(Payment::getDueDate));
        long today = DateFormat.currentDateAsLong(); // Get today's date once for comparison

        // Part 1: Initial filtering of existing payments
        for (Bill bill : Repository.getInstance().getBills()) {
            for (Payment payment : Repository.getInstance().getPayments()) {
                boolean found = false;
                if (payment.getBillerName().equals(bill.getBillerName()) && payment.isPaid() ||
                        payment.getBillerName().equals(bill.getBillerName()) && payment.isDateChanged() && payment.getDueDate() >= bill.getDueDate()) {
                    paymentList.add(payment);
                    found = true;
                } else if (payment.getBillerName().equals(bill.getBillerName()) && !payment.isPaid() && payment.isDateChanged() && payment.getDueDate() < bill.getDueDate()) {
                    Repository.getInstance().deletePayment(payment.getPaymentId(), context, (wasSuccessful, message) -> {
                    });
                }
                if (!bill.isRecurring() && found) {
                    bill.setPaymentsRemaining(0);
                    break;
                }
            }
        }

        Set<Payment> set = new LinkedHashSet<>(paymentList);
        Repository.getInstance().getPayments().clear();
        Repository.getInstance().getPayments().addAll(set);
        Repository.getInstance().getPayments().sort(Comparator.comparing(Payment::getDueDate));

        long endDate = DateFormat.makeLong(selectedDate.plusYears(1).withDayOfMonth(1));
        boolean updatesMade = false;

        if (Repository.getInstance().getUser(context) != null && Repository.getInstance().getBills() != null) {
            for (Bill bill : Repository.getInstance().getBills()) {
                int paymentNumber = 1;
                long iterateDate = bill.getDueDate();
                int frequency = bill.getFrequency();
                int paymentsRemaining = bill.getPaymentsRemaining();

                // Part 2: Generate upcoming payments
                while (iterateDate <= endDate && paymentsRemaining > 0) {
                    boolean found = false;
                    for (Payment payment : Repository.getInstance().getPayments()) {
                        if (payment.getBillerName().equals(bill.getBillerName())) {

                            boolean matchesStandardWindow = payment.getDueDate() >= iterateDate &&
                                    payment.getDueDate() <= DateFormat.incrementDate(frequency, iterateDate);

                            boolean isSameCycleManual = payment.isDateChanged() &&
                                    isWithinSameCycle(payment.getDueDate(), iterateDate, frequency);

                            if (matchesStandardWindow || isSameCycleManual) {
                                // UPDATED LOGIC: Mark as paid only if AutoPay is ON and Due Date <= Today
                                if (bill.isAutoPay() && payment.getDueDate() <= today) {
                                    payment.setPaid(true);
                                    payment.setDatePaid(payment.getDueDate());
                                    payment.setNeedsSync(true); // Flag for Repository.java
                                    updatesMade = true;
                                }
                                found = true;
                                iterateDate = DateFormat.incrementDate(frequency, iterateDate);
                                payment.setPaymentNumber(paymentNumber);
                                ++paymentNumber;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        int id = id();
                        while (idExists(id)) {
                            id = id();
                        }
                        Payment payment = new Payment(bill.getAmountDue(), 0, iterateDate, false, false, paymentNumber, bill.getBillerName(), id, 0, bill.getOwner());

                        // UPDATED LOGIC: Mark as paid only if AutoPay is ON and Due Date <= Today
                        if (bill.isAutoPay() && payment.getDueDate() <= today) {
                            payment.setPaid(true);
                            payment.setDatePaid(payment.getDueDate());
                        }

                        ++paymentNumber;
                        Repository.getInstance().getPayments().add(payment);
                        iterateDate = DateFormat.incrementDate(frequency, iterateDate);
                        --paymentsRemaining;
                    }
                }
            }
        }

        if (updatesMade) {
            Repository.getInstance().saveData(context, (wasSuccessful, message) -> {
                if (wasSuccessful) {
                    android.util.Log.d("BillerManager", "Autopayments synced: " + message);
                }
            });
        }

        // Part 3: Final cleanup of duplicates
        if (Repository.getInstance().getPayments() != null) {
            ArrayList<Payment> remove = new ArrayList<>();
            for (Payment pay : Repository.getInstance().getPayments()) {
                boolean found = false;
                for (Payment payment : Repository.getInstance().getPayments()) {
                    if (pay.getBillerName().equals(payment.getBillerName()) && pay.getDueDate() == payment.getDueDate()) {
                        if (!found) {
                            found = true;
                        } else {
                            remove.add(payment);
                        }
                    }
                }
            }
            if (!remove.isEmpty()) {
                Repository.getInstance().getPayments().removeAll(remove);
            }
        }

        ArrayList<Bill> removeBills = new ArrayList<>();
        for (Bill bill : Repository.getInstance().getBills()) {
            boolean found = false;
            for (Bill bil : Repository.getInstance().getBills()) {
                if (bill.getBillerName().equals(bil.getBillerName())) {
                    if (!found) {
                        found = true;
                    } else {
                        removeBills.add(bil);
                    }
                }
            }
        }
    }

    /**
     * Helper to determine if a manually moved payment still "covers" the current billing period.
     */
    private static boolean isWithinSameCycle(long paymentDate, long iterateDate, int frequency) {
        long difference = Math.abs(paymentDate - iterateDate);
        // Use a 75% threshold of the billing frequency to identify the same period
        // e.g., if monthly (30 days), a move within 22 days is considered the same cycle.
        long cycleThreshold = (long) (DateFormat.getDaysInFrequency(frequency) * 0.75);
        return difference < cycleThreshold;
    }

    public static void deleteFuturePayments(String billerName, long newDueDate, FirebaseTools.FirebaseCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<Payment> remove = new ArrayList<>();
        for (Payment payment : Repository.getInstance().getPayments()) {
            if (payment.getBillerName().equals(billerName) && payment.getDueDate() >= newDueDate) {
                remove.add(payment);
            } else if (payment.getBillerName().equals(billerName) && payment.getDueDate() < newDueDate && payment.isDateChanged()) {
                remove.add(payment);
            }
        }
        if (!remove.isEmpty()) {
            Repository.getInstance().getPayments().removeAll(remove);
            for (Payment payment : remove) {
                db.collection("users").document(payment.getOwner()).collection("payments").document(String.valueOf(payment.getPaymentId())).delete();
            }
            DataTools.getBill(billerName).setPaymentsRemaining(DataTools.getBill(billerName).getPaymentsRemaining() + 1);
            long highest = 0;
            for (Payment pay : Repository.getInstance().getPayments()) {
                if (pay.getBillerName().equals(billerName) && pay.isPaid() && pay.getDatePaid() > highest) {
                    highest = pay.getDatePaid();
                }
            }
            DataTools.getBill(billerName).setDateLastPaid(highest);
            callback.isSuccessful(true);
        }
    }

    static boolean idExists(int id) {

        if (Repository.getInstance().getPayments() != null) {
            for (Payment payment : Repository.getInstance().getPayments()) {
                if (payment.getPaymentId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int id() {
        final String AB = "0123456789";
        SecureRandom rnd = new SecureRandom();
        int length = 9;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return Integer.parseInt(String.valueOf(sb));
    }
}
