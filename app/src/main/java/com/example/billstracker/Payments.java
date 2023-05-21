package com.example.billstracker;

import java.util.Comparator;

class PaymentsComparator implements Comparator<Payments> {

    public int compare(Payments t1, Payments t2) {

        if (t1.isPaid()) {
            return Boolean.compare(t1.isPaid(), t2.isPaid());
        }

        return t1.getPaymentDate() - t2.getPaymentDate();
    }

}

public class Payments {

    private String paymentAmount;
    private double partialPayment;
    private int paymentDate;
    private boolean paid;
    private boolean dateChanged;
    private int paymentNumber;
    private String billerName;
    private int paymentId;
    private int datePaid;

    public Payments(String paymentAmount, double partialPayment, int paymentDate, boolean paid, boolean dateChanged, int paymentNumber, String billerName, int paymentId, int datePaid) {

        setPaymentAmount(paymentAmount);
        setPartialPayment(partialPayment);
        setPaymentDate(paymentDate);
        setPaid(paid);
        setDateChanged(dateChanged);
        setPaymentNumber(paymentNumber);
        setBillerName(billerName);
        setPaymentId(paymentId);
        setDatePaid(datePaid);
    }

    public Payments() {

    }

    public String getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(String paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public int getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(int paymentDate) {
        this.paymentDate = paymentDate;
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

    public int getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(int datePaid) {
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
}
