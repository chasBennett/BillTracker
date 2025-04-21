package com.example.billstracker.tools;

import static com.example.billstracker.activities.Login.payments;

import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;

public interface CalculateBalance {

    static double calculateNewBalance (Bill bill) {

        int paymentsPaid = paymentsPaid(bill);
        double balancePaid = (paymentsPaid * bill.getAmountDue()) - (paymentsPaid * interestPaid(bill)) - (paymentsPaid * bill.getEscrow());
        return bill.getBalance() - balancePaid;
    }

    static int paymentsPaid (Bill bill) {
        int paidPayments = 0;
        for (Payment payment: payments.getPayments()) {
            if (payment.getBillerName().equals(bill.getBillerName()) && payment.isPaid()) {
                ++paidPayments;
            }
        }
        return paidPayments;
    }
    static double totalPaid (Bill bill) {
        return paymentsPaid(bill) * bill.getAmountDue();
    }

    static double interestPaid (Bill bill) {

        double yearlyRate = calculateApr(bill);
        double numYears = numYears(bill);
        double balance = bill.getBalance();
        double totalInterest = (balance * yearlyRate * numYears) / 100;

        return (totalInterest / numberOfPayments(bill)) * paymentsPaid(bill);
    }

    static int numberOfPayments (Bill bill) {
        int numberOfPayments = bill.getPaymentsRemaining();
        for (Payment pay: payments.getPayments()) {
            if (pay.getBillerName().equals(bill.getBillerName()) && pay.isPaid()) {
                ++numberOfPayments;
            }
        }
        return numberOfPayments;
    }

    static double numYears (Bill bill) {

        int numberOfPayments = numberOfPayments(bill);
        switch (bill.getFrequency()) {
            case 0:
                return numberOfPayments / 365.0;
            case 1:
                return numberOfPayments / 52.0;
            case 2:
                return numberOfPayments / 26.0;
            case 3:
                return numberOfPayments / 12.0;
            case 4:
                return numberOfPayments / 6.0;
            case 5:
                return numberOfPayments / 4.0;
            default:
                return numberOfPayments;
        }
    }

    static double calculateApr (Bill bil) {

        int numberOfPayments = numberOfPayments(bil);
        double balance = bil.getBalance();
        double rate = 1.0;
        double totalInterest = (numberOfPayments * bil.getAmountDue()) - (numberOfPayments * bil.getEscrow()) - balance;
        double numYears = numYears(bil);

        int counter = 0;
        if (totalInterest < 100) {
            rate = 0.0;
        }
        if (rate > 0.0) {
            while ((balance * rate * numYears) / 100 < totalInterest && counter < 1000) {
                rate = rate + 0.01;
                ++counter;
            }
        }
        return rate;
    }

    static double compoundInterest (double principal, double rate, double time) {

        return principal * (Math.pow((1 + rate / 100), time));
    }
}
