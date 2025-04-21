package com.example.billstracker.custom_objects;

public class Stat {

    private String billerName;
    private double totalPaymentsAmount;
    private int totalPaymentsMade;
    private long dateLastPaid;
    private long nextPaymentDueDate;
    private double lastPaymentAmount;
    private int paymentsRemaining;

    public Stat(String billerName, double totalPaymentsAmount, int totalPaymentsMade, long dateLastPaid, long nextPaymentDueDate, double lastPaymentAmount, int paymentsRemaining) {

        setBillerName(billerName);
        setTotalPaymentsAmount(totalPaymentsAmount);
        setTotalPaymentsMade(totalPaymentsMade);
        setDateLastPaid(dateLastPaid);
        setNextPaymentDueDate(nextPaymentDueDate);
        setLastPaymentAmount(lastPaymentAmount);
        setPaymentsRemaining(paymentsRemaining);
    }

    public String getBillerName() {
        return billerName;
    }

    public void setBillerName(String billerName) {
        this.billerName = billerName;
    }

    public long getDateLastPaid() {
        return dateLastPaid;
    }

    public void setDateLastPaid(long dateLastPaid) {
        this.dateLastPaid = dateLastPaid;
    }

    public double getTotalPaymentsAmount() {
        return totalPaymentsAmount;
    }

    public void setTotalPaymentsAmount(double totalPaymentsAmount) {
        this.totalPaymentsAmount = totalPaymentsAmount;
    }

    public int getTotalPaymentsMade() {
        return totalPaymentsMade;
    }

    public void setTotalPaymentsMade(int totalPaymentsMade) {
        this.totalPaymentsMade = totalPaymentsMade;
    }
    public long getNextPaymentDueDate () {
        return nextPaymentDueDate;
    }
    public void setNextPaymentDueDate (long nextPaymentDueDate) {
        this.nextPaymentDueDate = nextPaymentDueDate;
    }

    public double getLastPaymentAmount() {
        return lastPaymentAmount;
    }

    public void setLastPaymentAmount(double lastPaymentAmount) {
        this.lastPaymentAmount = lastPaymentAmount;
    }

    public int getPaymentsRemaining() {
        return paymentsRemaining;
    }

    public void setPaymentsRemaining(int paymentsRemaining) {
        this.paymentsRemaining = paymentsRemaining;
    }
}
