package com.example.billstracker.custom_objects;

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
    public String getOwner () {
        return owner;
    }
    public void setOwner (String owner) {
        this.owner = owner;
    }
    public boolean getAutoPay() {
        return autoPay;
    }
    public void setAutoPay (boolean autoPay) {
        this.autoPay = autoPay;
    }
}
