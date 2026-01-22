package com.example.billstracker.custom_objects;

import android.content.Context;

import com.example.billstracker.tools.Repository;
import com.google.firebase.database.Exclude;

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
    private boolean needsSync = false;
    private boolean needsDelete = false;

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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Exclude
    public boolean isNeedsSync() {
        return needsSync;
    }

    public void setNeedsSync(boolean needsSync) {
        this.needsSync = needsSync;
    }

    @Exclude
    public boolean isNeedsDelete() {
        return needsDelete;
    }

    public void setNeedsDelete(boolean needsDelete) {
        this.needsDelete = needsDelete;
    }

    public static class Builder {
        private final Payment payment;
        private final Context context;

        public Builder(Context context, Payment payment) {
            this.context = context;
            this.payment = payment;
        }

        public Builder setPaymentAmount(double amount) {
            payment.setPaymentAmount(amount);
            payment.needsSync = true;
            return this;
        }

        public Builder setPartialPayment(double partial) {
            payment.setPartialPayment(partial);
            payment.needsSync = true;
            return this;
        }

        public Builder setDueDate(long date) {
            payment.setDueDate(date);
            payment.needsSync = true;
            return this;
        }

        public Builder setPaid(boolean paid) {
            payment.setPaid(paid);
            payment.needsSync = true;
            return this;
        }

        public Builder setDateChanged(boolean changed) {
            payment.setDateChanged(changed);
            payment.needsSync = true;
            return this;
        }

        public Builder setPaymentNumber(int num) {
            payment.setPaymentNumber(num);
            payment.needsSync = true;
            return this;
        }

        public Builder setBillerName(String name) {
            payment.setBillerName(name);
            payment.needsSync = true;
            return this;
        }

        public Builder setDatePaid(long date) {
            payment.setDatePaid(date);
            payment.needsSync = true;
            return this;
        }

        public Builder setOwner(String owner) {
            payment.setOwner(owner);
            payment.needsSync = true;
            return this;
        }

        public void save(Repository.OnCompleteCallback callback) {
            if (payment == null) {
                if (callback != null) callback.onComplete(false, "Payment was not found.");
                return;
            }
            Repository.getInstance().saveData(context, callback);
        }
    }
}
