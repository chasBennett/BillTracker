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
        for (Bill bill : Repository.getInstance().getBills()) {
            for (Payment payment : Repository.getInstance().getPayments()) {
                boolean found = false;
                if (payment.getBillerName().equals(bill.getBillerName()) && payment.isPaid() || payment.getBillerName().equals(bill.getBillerName()) && payment.isDateChanged() && payment.getDueDate() >= bill.getDueDate()) {
                    paymentList.add(payment);
                    found = true;
                } else if (payment.getBillerName().equals(bill.getBillerName()) && !payment.isPaid() && payment.isDateChanged() && payment.getDueDate() < bill.getDueDate()) {
                    Repository.getInstance().deletePayment(payment.getPaymentId(), context, (wasSuccessful, message) -> {});
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
        if (Repository.getInstance().getUser(context) != null && Repository.getInstance().getBills() != null) {
            for (Bill bill : Repository.getInstance().getBills()) {
                int paymentNumber = 1;
                long iterateDate = bill.getDueDate();
                int frequency = bill.getFrequency();
                int paymentsRemaining = bill.getPaymentsRemaining();

                while (iterateDate <= endDate && paymentsRemaining > 0) {
                    boolean found = false;
                    for (Payment payment : Repository.getInstance().getPayments()) {
                        if (payment.getBillerName().equals(bill.getBillerName()) && payment.getDueDate() >= iterateDate && payment.getDueDate() <= DateFormat.incrementDate(frequency, iterateDate)) {
                            found = true;
                            iterateDate = DateFormat.incrementDate(frequency, iterateDate);
                            payment.setPaymentNumber(paymentNumber);
                            ++paymentNumber;
                            break;
                        }
                    }
                    if (!found) {
                        int id = id();
                        while (idExists(id)) {
                            id = id();
                        }
                        Payment payment = new Payment(bill.getAmountDue(), 0, iterateDate, false, false, paymentNumber, bill.getBillerName(), id, 0, bill.getOwner());
                        ++paymentNumber;
                        Repository.getInstance().getPayments().add(payment);
                        iterateDate = DateFormat.incrementDate(frequency, iterateDate);
                        --paymentsRemaining;
                    }
                }
            }
        }

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
        ArrayList<Bill> remove = new ArrayList<>();
        for (Bill bill : Repository.getInstance().getBills()) {
            boolean found = false;
            for (Bill bil : Repository.getInstance().getBills()) {
                if (bill.getBillerName().equals(bil.getBillerName())) {
                    if (!found) {
                        found = true;
                    } else {
                        remove.add(bil);
                    }
                }
            }
        }
        Repository.getInstance().getBills().removeAll(remove);
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
