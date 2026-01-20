package com.example.billstracker.custom_objects;

import android.content.Context;

import com.example.billstracker.tools.Repository;
import com.google.firebase.database.Exclude;

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
    private boolean autoPay;
    private boolean needsSync = false;
    private boolean needsDelete = false;

    public Bill(String billerName, double amountDue, long dueDate, long dateLastPaid, String billsId, boolean recurring, int frequency, String website, int category, String icon, int paymentsRemaining,
                double balance, double escrow, String owner, boolean autoPay) {

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
        setAutoPay(autoPay);
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isAutoPay() {
        return autoPay;
    }

    public void setAutoPay(boolean autoPay) {
        this.autoPay = autoPay;
    }

    @Exclude
    public boolean isNeedsSync() {
        return needsSync;
    }

    public void setNeedsSync(boolean needsSync) {
        this.needsSync = needsSync;
    }

    @Exclude
    public boolean isNeedsDelete() { return needsDelete; }
    public void setNeedsDelete(boolean needsDelete) { this.needsDelete = needsDelete; }

    public static class Builder {
        private final Bill bill;
        private final Context context;

        public Builder(Context context, Bill bill) {
            this.context = context;
            this.bill = bill;
        }

        public Builder setBillerName(String name) {
            bill.setBillerName(name);
            bill.needsSync = true;
            return this;
        }

        public Builder setWebsite(String url) {
            bill.setWebsite(url);
            bill.needsSync = true;
            return this;
        }

        public Builder setAmountDue(double amount) {
            bill.setAmountDue(amount);
            bill.needsSync = true;
            return this;
        }

        public Builder setDueDate(long date) {
            bill.setDueDate(date);
            bill.needsSync = true;
            return this;
        }

        public Builder setDateLastPaid(long date) {
            bill.setDateLastPaid(date);
            bill.needsSync = true;
            return this;
        }

        public Builder setFrequency(int freq) {
            bill.setFrequency(freq);
            bill.needsSync = true;
            return this;
        }

        public Builder setRecurring(boolean rec) {
            bill.setRecurring(rec);
            bill.needsSync = true;
            return this;
        }

        public Builder setCategory(int cat) {
            bill.setCategory(cat);
            bill.needsSync = true;
            return this;
        }

        public Builder setIcon(String icon) {
            bill.setIcon(icon);
            bill.needsSync = true;
            return this;
        }

        public Builder setPaymentsRemaining(int paymentsRemaining) {
            bill.setPaymentsRemaining(paymentsRemaining);
            bill.needsSync = true;
            return this;
        }

        public Builder setBalance(double bal) {
            bill.setBalance(bal);
            bill.needsSync = true;
            return this;
        }

        public Builder setEscrow(double esc) {
            bill.setEscrow(esc);
            bill.needsSync = true;
            return this;
        }

        public Builder setOwner(String owner) {
            bill.setOwner(owner);
            bill.needsSync = true;
            return this;
        }

        public Builder setAutoPay(boolean auto) {
            bill.setAutoPay(auto);
            bill.needsSync = true;
            return this;
        }

        public void save(Repository.OnCompleteCallback callback) {
            if (bill == null) {
                if (callback != null) callback.onComplete(false, "Bill was not found.");
                return;
            }
            Repository.getInstance().saveData(context, callback);
        }
    }
}
