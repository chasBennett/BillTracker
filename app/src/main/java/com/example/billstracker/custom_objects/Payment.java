package com.example.billstracker.custom_objects;

import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.payments;

import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.UserData;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

public class Payment implements Serializable {

    private double paymentAmount;
    private double partialPayment;
    private long dueDate;
    private boolean paid;
    private boolean dateChanged;
    private int paymentNumber;
    private String billerName;
    private int paymentId;
    private long datePaid;
    private String owner;

    public Payment(double paymentAmount, double partialPayment, long dueDate, boolean paid, boolean dateChanged, int paymentNumber, String billerName, int paymentId, long datePaid, String owner) {

        setPaymentAmount(paymentAmount);
        setPartialPayment(partialPayment);
        setDueDate(dueDate);
        setPaid(paid);
        setDateChanged(dateChanged);
        setPaymentNumber(paymentNumber);
        setBillerName(billerName);
        setPaymentId(paymentId);
        setDatePaid(datePaid);
        setOwner(owner);
    }

    public void updatePayment (Payment payment) {
        setPaymentAmount(payment.getPaymentAmount());
        setPartialPayment(payment.getPartialPayment());
        setDueDate(payment.getDueDate());
        setPaid(payment.isPaid());
        setDateChanged(payment.isDateChanged());
        setPaymentNumber(payment.getPaymentNumber());
        setBillerName(payment.getBillerName());
        setPaymentId(payment.getPaymentId());
        setDatePaid(payment.getDatePaid());
        setOwner(payment.getOwner());
    }

    public Payment() {

    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {

        this.paymentAmount = paymentAmount;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getBillerName() {
        return billerName;
    }

    public void setBillerName(String billerName) {
        this.billerName = billerName;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public long getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(long datePaid) {
        this.datePaid = datePaid;
    }

    public double getPartialPayment() {
        return partialPayment;
    }

    public void setPartialPayment(double partialPayment) {
        this.partialPayment = partialPayment;
    }

    public int getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(int paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public boolean isDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(boolean dateChanged) {
        this.dateChanged = dateChanged;
    }
    public String getOwner () {
        return owner;
    }
    public void setOwner (String owner) {
        this.owner = owner;
    }

    public void changePaymentDueDate(long newDueDate, boolean changeAll, FirebaseTools.FirebaseCallback callback) {
        if (changeAll) {
            for (Bill bill : bills.getBills()) {
                if (bill.getBillerName().equals(billerName)) {
                    bill.changeDueDate(newDueDate, callback::isSuccessful);
                }
            }
        }
        else {
            for (Payment payment: payments.getPayments()) {
                if (payment.getPaymentId() == paymentId) {
                    payment.setDateChanged(true);
                    payment.setDueDate(newDueDate);
                    break;
                }
            }
            UserData.save();
            callback.isSuccessful(true);
        }
    }

    public void deletePayment (boolean addPaymentToBiller, OnSuccessCallback callback) {

            FirebaseFirestore.getInstance().collection("users").document(getOwner()).collection("payments").document(String.valueOf(paymentId)).delete().addOnCompleteListener(task -> {
                if (task.isComplete() && task.isSuccessful()) {
                    for (Bill bill : bills.getBills()) {
                        if (bill.getBillerName().equals(billerName)) {
                            if (addPaymentToBiller) {
                                bill.setPaymentsRemaining(bill.getPaymentsRemaining() + 1);
                            }
                            long highest = 0;
                            for (Payment pay : payments.getPayments()) {
                                if (pay.getBillerName().equals(billerName) && pay.isPaid() && pay.getDatePaid() > highest) {
                                    highest = pay.getDatePaid();
                                }
                            }
                            bill.setDateLastPaid(highest);
                            break;
                        }
                    }
                    payments.getPayments().remove(this);
                    UserData.save();
                    callback.isSuccessful(true);
                }
                else {
                    callback.isSuccessful(false);
                }
            });
    }
    public interface OnSuccessCallback {
        void isSuccessful(boolean isSuccessful);
    }
}
