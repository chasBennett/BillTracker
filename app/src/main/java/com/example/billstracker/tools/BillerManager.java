package com.example.billstracker.tools;

import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.payments;
import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.MainActivity2.selectedDate;

import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Bills;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Payments;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class BillerManager {

    public static void refreshPayments() {

        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }
        if (payments == null || payments.getPayments() == null) {
            payments = new Payments(new ArrayList<>());
        }
        ArrayList<Payment> paymentList = new ArrayList<>();
        payments.getPayments().sort(Comparator.comparing(Payment::getDueDate));
        for (Bill bill : bills.getBills()) {
            for (Payment payment : payments.getPayments()) {
                boolean found = false;
                if (payment.getBillerName().equals(bill.getBillerName()) && payment.isPaid() || payment.getBillerName().equals(bill.getBillerName()) && payment.isDateChanged() && payment.getDueDate() >= bill.getDueDate()) {
                    paymentList.add(payment);
                    found = true;
                }
                else if (payment.getBillerName().equals(bill.getBillerName()) && !payment.isPaid() && payment.isDateChanged() && payment.getDueDate() < bill.getDueDate()) {
                    payment.deletePayment(false, isSuccessful -> {});
                }
                if (!bill.isRecurring() && found) {
                    bill.setPaymentsRemaining(0);
                    break;
                }
            }
        }
        Set<Payment> set = new LinkedHashSet<>(paymentList);
        payments.getPayments().clear();
        payments.getPayments().addAll(set);
        payments.getPayments().sort(Comparator.comparing(Payment::getDueDate));
        long endDate = DateFormat.makeLong(selectedDate.plusYears(1).withDayOfMonth(1));
        if (thisUser != null && bills.getBills() != null) {
            for (Bill bill : bills.getBills()) {
                int paymentNumber = 1;
                long iterateDate = bill.getDueDate();
                int frequency = bill.getFrequency();
                int paymentsRemaining = bill.getPaymentsRemaining();

                while (iterateDate <= endDate && paymentsRemaining > 0) {
                    boolean found = false;
                    for (Payment payment : payments.getPayments()) {
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
                        payments.getPayments().add(payment);
                        iterateDate = DateFormat.incrementDate(frequency, iterateDate);
                        --paymentsRemaining;
                    }
                }
            }
        }

        if (payments != null && payments.getPayments() != null) {
            ArrayList <Payment> remove = new ArrayList<>();
            for (Payment pay: payments.getPayments()) {
                boolean found = false;
                for (Payment payment: payments.getPayments()) {
                    if (pay.getBillerName().equals(payment.getBillerName()) && pay.getDueDate() == payment.getDueDate()) {
                        if (!found) {
                            found = true;
                        }
                        else {
                            remove.add(payment);
                        }
                    }
                }
            }
            if (!remove.isEmpty()) {
                payments.getPayments().removeAll(remove);
            }
        }
        if (bills == null || bills.getBills() == null) {
            bills = new Bills(new ArrayList<>());
        }
        ArrayList <Bill> remove = new ArrayList<>();
        for (Bill bill: bills.getBills()) {
            boolean found = false;
            for (Bill bil: bills.getBills()) {
                if (bill.getBillerName().equals(bil.getBillerName())) {
                    if (!found) {
                        found = true;
                    }
                    else {
                        remove.add(bil);
                    }
                }
            }
        }
        bills.getBills().removeAll(remove);
    }
    public static void deleteFuturePayments(String billerName, long newDueDate, FirebaseTools.FirebaseCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
            ArrayList <Payment> remove = new ArrayList<>();
            for (Payment payment: payments.getPayments()){
                if (payment.getBillerName().equals(billerName) && payment.getDueDate() >= newDueDate) {
                    remove.add(payment);
                }
                else if (payment.getBillerName().equals(billerName) && payment.getDueDate() < newDueDate && payment.isDateChanged()) {
                    remove.add(payment);
                }
            }
            if (!remove.isEmpty()) {
                payments.getPayments().removeAll(remove);
                for (Payment payment : remove) {
                    db.collection("users").document(payment.getOwner()).collection("payments").document(String.valueOf(payment.getPaymentId())).delete();
                }
                Data.getBill(billerName).setPaymentsRemaining(Data.getBill(billerName).getPaymentsRemaining() + 1);
                long highest = 0;
                for (Payment pay : payments.getPayments()) {
                    if (pay.getBillerName().equals(billerName) && pay.isPaid() && pay.getDatePaid() > highest) {
                        highest = pay.getDatePaid();
                    }
                }
                Data.getBill(billerName).setDateLastPaid(highest);
                callback.isSuccessful(true);
            }
    }
    static boolean idExists (int id) {

        if (payments != null && payments.getPayments() != null) {
            for (Payment payment : payments.getPayments()) {
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
