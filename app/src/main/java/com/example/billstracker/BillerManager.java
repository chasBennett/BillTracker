package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;

public class BillerManager {

    DateFormatter df = new DateFormatter();
    public void refreshPayments (LocalDate selectedDate) {

        ArrayList <Payments> remove = new ArrayList<>();
        for (Payments clear: paymentInfo.getPayments()) {
            if (!clear.isPaid() && !clear.isDateChanged()) {
                remove.add(clear);
            }
        }
        paymentInfo.getPayments().removeAll(remove);
        int startDate = df.calcDateValue(selectedDate.minusMonths(5));
        int endDate = df.calcDateValue(selectedDate.plusMonths(5));
        int iterateDate;
        int paymentCounter;
        if (thisUser != null) {
            if (thisUser.getBills() != null) {
                for (Bill bill : thisUser.getBills()) {
                    paymentCounter = 0;
                    iterateDate = bill.getDayDue();
                    int frequency = Integer.parseInt(bill.getFrequency());
                    int paymentsRemaining = Integer.parseInt(bill.getPaymentsRemaining());
                    if (paymentInfo.getPayments() != null && paymentInfo.getPayments().size() > 0) {
                        paymentInfo.getPayments().sort(Comparator.comparing(Payments::getPaymentDate));
                        boolean found = false;
                        for (Payments pays : paymentInfo.getPayments()) {
                            if (pays.getBillerName().equals(bill.getBillerName()) && pays.isPaid()) {
                                paymentCounter = paymentCounter + 1;
                                pays.setPaymentNumber(paymentCounter);
                                iterateDate = pays.getPaymentDate();
                                found = true;
                            }
                        }
                        if (found) {
                            if (frequency == 0) {
                                iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusDays(1));
                            } else if (frequency == 1) {
                                iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusWeeks(1));
                            } else if (frequency == 2) {
                                iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusWeeks(2));
                            } else if (frequency == 3) {
                                iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusMonths(1));
                            } else if (frequency == 4) {
                                iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusMonths(3));
                            } else if (frequency == 5) {
                                iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusYears(1));
                            }
                        }
                    }
                    while (iterateDate <= endDate && paymentsRemaining > 0) {
                        paymentCounter = paymentCounter + 1;
                        Payments payment = new Payments(bill.getAmountDue(), 0, iterateDate, false, false, paymentCounter, bill.getBillerName(), id(), 0);
                        if (frequency == 0) {
                            iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusDays(1));
                        } else if (frequency == 1) {
                            iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusWeeks(1));
                        } else if (frequency == 2) {
                            iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusWeeks(2));
                        } else if (frequency == 3) {
                            iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusMonths(1));
                        } else if (frequency == 4) {
                            iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusMonths(3));
                        } else if (frequency == 5) {
                            iterateDate = df.calcDateValue(df.convertIntDateToLocalDate(iterateDate).plusYears(1));
                        }
                        if (iterateDate >= startDate && iterateDate <= endDate) {
                            boolean match = false;
                            for (Payments pay: paymentInfo.getPayments()) {
                                if (pay.getBillerName().equals(payment.getBillerName()) && pay.getPaymentNumber() == payment.getPaymentNumber()) {
                                    match = true;
                                    break;
                                }
                            }
                            if (!match) {
                                paymentInfo.getPayments().add(payment);
                            }
                        }
                        paymentsRemaining = paymentsRemaining - 1;
                    }
                }
            } else {
                thisUser.setBills(new ArrayList<>());
            }

        }
    }

    public void savePayments () {

        ArrayList <Payments> remove = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (paymentInfo.getPayments() != null) {
            for (Payments payment : paymentInfo.getPayments()) {
                if (!payment.isPaid() && !payment.isDateChanged()) {
                    remove.add(payment);
                }
            }
        }
        else {
            paymentInfo.setPayments(new ArrayList<>());
        }
        paymentInfo.getPayments().removeAll(remove);
        db.collection("payments").document(uid).set(paymentInfo, SetOptions.merge());
        refreshPayments(LocalDate.now(ZoneId.systemDefault()));
    }

    int id() {
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
