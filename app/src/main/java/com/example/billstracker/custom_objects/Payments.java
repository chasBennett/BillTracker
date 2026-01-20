package com.example.billstracker.custom_objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Payments implements Serializable {
    private ArrayList<Payment> payments;

    public Payments(ArrayList<Payment> payments) {

        setPayments(payments);
    }

    public Payments() {

    }

    public ArrayList<Payment> getPayments() {
        if (payments == null) {
            payments = new ArrayList<>();
        }
        payments = (ArrayList<Payment>) payments.stream().distinct().collect(Collectors.toList());
        return payments;
    }

    public void setPayments(ArrayList<Payment> payments) {
        this.payments = payments;
    }
}
