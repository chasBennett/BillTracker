package com.example.billstracker.custom_objects;

import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.payments;

import com.example.billstracker.tools.BillerManager;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.UserData;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Bill {

    private String billerName;
    private String website;
    private double amountDue;
    private long dueDate;
    private long dateLastPaid;
    private String billsId;
    private int frequency;
    private boolean recurring;
    private int category;
    private String icon;
    private int paymentsRemaining;
    private double balance;
    private double escrow;
    private String owner;

    public Bill(String billerName, double amountDue, long dueDate, long dateLastPaid, String billsId, boolean recurring, int frequency, String website, int category, String icon, int paymentsRemaining,
                double balance, double escrow, String owner) {

        setBillerName(billerName);
        setWebsite(website);
        setAmountDue(amountDue);
        setDueDate(dueDate);
        setDateLastPaid(dateLastPaid);
        setBillsId(billsId);
        setFrequency(frequency);
        setRecurring(recurring);
        setCategory(category);
        setIcon(icon);
        setPaymentsRemaining(paymentsRemaining);
        setBalance(balance);
        setEscrow(escrow);
        setOwner(owner);
    }

    public Bill() {

    }

    public String getBillerName() {
        return billerName;
    }

    public void setBillerName(String billerName) {
        this.billerName = billerName;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public long getDateLastPaid() {
        return dateLastPaid;
    }

    public void setDateLastPaid(long dateLastPaid) {
        this.dateLastPaid = dateLastPaid;
    }

    public String getBillsId() {
        return billsId;
    }

    public void setBillsId(String billsId) {
        this.billsId = billsId;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPaymentsRemaining() {
        return paymentsRemaining;
    }

    public void setPaymentsRemaining(int paymentsRemaining) {
        this.paymentsRemaining = paymentsRemaining;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getEscrow() {
        return escrow;
    }

    public void setEscrow(double escrow) {
        this.escrow = escrow;
    }
    public String getOwner () {
        return owner;
    }
    public void setOwner (String owner) {
        this.owner = owner;
    }
    public void updateBiller (String newBillerName, double newAmountDue, long newDueDate, long newDateLastPaid, String newBillsId, boolean newRecurring, int newFrequency, String newWebsite, int newCategory,
                              String newIcon, int newPaymentsRemaining, double newBalance, double newEscrow, String newOwner, FirebaseTools.FirebaseCallback callback) {
        if (bills != null && bills.getBills() != null) {
            setAmountDue(newAmountDue);
            setDateLastPaid(newDateLastPaid);
            setBillsId(newBillsId);
            setRecurring(newRecurring);
            setFrequency(newFrequency);
            setWebsite(newWebsite);
            setCategory(newCategory);
            setIcon(newIcon);
            setPaymentsRemaining(newPaymentsRemaining);
            setBalance(newBalance);
            setEscrow(newEscrow);
            setOwner(newOwner);
            if (!newBillerName.equals(billerName) || newDueDate != dueDate) {
                if (!newBillerName.equals(billerName)) {
                    if (payments != null && payments.getPayments() != null) {
                        for (Payment payment : payments.getPayments()) {
                            if (payment.getBillerName().equals(billerName)) {
                                payment.setBillerName(newBillerName);
                            }
                        }
                    }
                    bills.getBills().remove(this);
                    FirebaseFirestore.getInstance().collection("users").document(getOwner()).collection("bills").document(getBillerName()).delete().addOnCompleteListener(task -> {
                        if (task.isComplete() && task.isSuccessful()) {
                            setBillerName(newBillerName);
                            bills.getBills().add(this);
                            UserData.save();
                            BillerManager.refreshPayments();
                            callback.isSuccessful(true);
                        } else {
                            callback.isSuccessful(false);
                        }
                    });
                }
                if (newDueDate != dueDate) {
                    BillerManager.deleteFuturePayments(billerName, newDueDate, isSuccessful -> {
                        if (isSuccessful) {
                            setDueDate(newDueDate);
                            UserData.save();
                            BillerManager.refreshPayments();
                            callback.isSuccessful(true);
                        }
                        else {
                            callback.isSuccessful(false);
                        }
                    });
                }
            }
            else {
                BillerManager.refreshPayments();
                UserData.save();
                callback.isSuccessful(true);
            }
        }
        else {
            callback.isSuccessful(false);
        }
    }
    public void deleteBiller (FirebaseTools.FirebaseCallback callback) {
        if (owner != null && billerName != null) {
            bills.getBills().remove(this);
            FirebaseFirestore.getInstance().collection("users").document(owner).collection("bills").document(billerName).delete().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.isComplete()) {
                    ArrayList<Payment> remove = new ArrayList<>();
                    for (Payment payment : payments.getPayments()) {
                        if (payment.getBillerName().equals(billerName)) {
                            remove.add(payment);
                        }
                    }
                    if (!remove.isEmpty()) {
                        for (Payment payment : remove) {
                            payment.deletePayment(false, isSuccessful -> {
                            });
                        }
                    }
                    callback.isSuccessful(true);
                }
                else {
                    callback.isSuccessful(false);
                }
            });
        }
        else {
            callback.isSuccessful(false);
        }
    }
    public void updateAmountDue (double newAmountDue) {
        setAmountDue(newAmountDue);
        if (payments != null && payments.getPayments() != null) {
            for (Payment payment: payments.getPayments()) {
                if (payment.getBillerName().equals(billerName) && !payment.isPaid()) {
                    payment.setPaymentAmount(newAmountDue);
                }
            }
        }
        UserData.save();
    }
    public void changeDueDate (long newDueDate, OnSuccessCallback callback) {
        setDueDate(newDueDate);
        for (Payment payment: payments.getPayments()) {
            if (payment.getBillerName().equals(billerName) && payment.isPaid() && payment.getDueDate() >= newDueDate || payment.getBillerName().equals(billerName) && payment.isDateChanged() && payment.getDueDate() >= newDueDate) {
                payment.deletePayment(false, isSuccessful -> {});
            }
        }
        UserData.save();
        callback.isSuccessful(true);
    }
    public interface OnSuccessCallback {
        void isSuccessful(boolean isSuccessful);
    }
}
