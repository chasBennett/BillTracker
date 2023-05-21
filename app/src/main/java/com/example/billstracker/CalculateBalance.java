package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

public class CalculateBalance {
    FixNumber fn = new FixNumber();

    public double calculateNewBalance (Bill bill, ArrayList <Payments> paymentsList) {

        double rate = calculateRate(bill);
        int startDate;
        int endDate = bill.getDayDue();
        double interestPaid = 0;
        double balance = bill.getBalance();
        paymentsList.sort(Comparator.comparing(Payments::getDatePaid));
        for (Payments pay : paymentsList) {
            if (pay.isPaid() && pay.getBillerName().equalsIgnoreCase(bill.getBillerName()) || pay.getPartialPayment() > 0 && pay.getBillerName().equals(bill.getBillerName())) {
                startDate = endDate;
                endDate = pay.getDatePaid();
                double daysBetweenInYears = ((double) (endDate - startDate)) / 365.0;
                interestPaid = interestPaid + compoundInterest(balance, rate, daysBetweenInYears);
                balance = balance + compoundInterest(balance, rate, daysBetweenInYears) - (Double.parseDouble(fn.makeDouble(pay.getPaymentAmount())) - bill.getEscrow());
            }
        }
        return balance;
    }

    public double interestPaid (Bill bill, ArrayList <Payments> paymentsList) {

        double rate = calculateRate(bill);
        int startDate;
        int endDate = bill.getDayDue();
        double interestPaid = 0;
        double balance = bill.getBalance();
        paymentsList.sort(Comparator.comparing(Payments::getDatePaid));
        for (Payments pay : paymentsList) {
            if (pay.isPaid() && pay.getBillerName().equalsIgnoreCase(bill.getBillerName()) || pay.getPartialPayment() > 0 && pay.getBillerName().equals(bill.getBillerName())) {
                startDate = endDate;
                endDate = pay.getDatePaid();
                double daysBetweenInYears = ((double) (endDate - startDate)) / 365.0;
                interestPaid = interestPaid + compoundInterest(balance, rate, daysBetweenInYears);
                balance = balance + compoundInterest(balance, rate, daysBetweenInYears) - (Double.parseDouble(fn.makeDouble(pay.getPaymentAmount())) - bill.getEscrow());
            }
        }
        return interestPaid;
    }
    public double calculateRate (Bill bil) {

        double amountDue = Double.parseDouble(fn.makeDouble(bil.getAmountDue())) - bil.getEscrow();
        double numYears;
        double numberOfPayments = Double.parseDouble(bil.getPaymentsRemaining());
        for (Payments pay: paymentInfo.getPayments()) {
            if (pay.getBillerName().equals(bil.getBillerName()) && pay.isPaid()) {
                numberOfPayments = numberOfPayments + 1;
            }
        }
        double balance = Double.parseDouble(fn.makeDouble(String.valueOf(bil.getBalance())));
        double totalPaid = (amountDue * numberOfPayments);
        double totalInterest = totalPaid - balance;
        int freq = Integer.parseInt(bil.getFrequency());
        if (freq == 0) {
            numYears = numberOfPayments / (365);
        }
        else if (freq == 1) {
            numYears = numberOfPayments / 52.1428571429;
        }
        else if (freq == 2) {
            numYears = numberOfPayments / 26.0714285714;
        }
        else if (freq == 3) {
            numYears = numberOfPayments / (365.0 / 30.4166666667);
        }
        else if (freq == 4) {
            numYears = numberOfPayments / (365.0 / 91.2500000001);
        }
        else {
            numYears = numberOfPayments;
        }
        DecimalFormat df = new DecimalFormat("###,###,##0.00");
        return Double.parseDouble(df.format((totalInterest / (balance * numYears)) * 100));
    }
    public double compoundInterest (double principal, double rate, double time) {

        double a = principal * (Math.pow((1 + rate / 100), time));
        return a - principal;
    }
}
