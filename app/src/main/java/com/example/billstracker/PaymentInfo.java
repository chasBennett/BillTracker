package com.example.billstracker;

import java.util.ArrayList;

public class PaymentInfo {
    private ArrayList <Payments> payments;

    public PaymentInfo (ArrayList <Payments> payments) {

        setPayments(payments);
    }

    public PaymentInfo () {

    }

    public ArrayList<Payments> getPayments() {
        return payments;
    }

    public void setPayments(ArrayList<Payments> payments) {
        this.payments = payments;
    }
}
