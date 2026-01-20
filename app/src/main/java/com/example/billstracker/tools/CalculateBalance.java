package com.example.billstracker.tools;

import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;

public interface CalculateBalance {

    static double calculateNewBalance(Bill bill) {
        int paymentsPaid = paymentsPaid(bill);

        // Principal Reduction = (Total Money Paid) - (Total Interest Paid) - (Total Escrow Paid)
        // Note: interestPaid(bill) now returns the cumulative interest for all payments made.
        double balancePaid = (paymentsPaid * bill.getAmountDue()) - interestPaid(bill) - (paymentsPaid * bill.getEscrow());

        return bill.getBalance() - balancePaid;
    }

    static int paymentsPaid(Bill bill) {
        int paidPayments = 0;
        for (Payment payment : Repository.getInstance().getPayments()) {
            if (payment.getBillerName().equals(bill.getBillerName()) && payment.isPaid()) {
                ++paidPayments;
            }
        }
        return paidPayments;
    }

    static double totalPaid(Bill bill) {
        return paymentsPaid(bill) * bill.getAmountDue();
    }

    static double interestPaid(Bill bill) {
        double principal = bill.getBalance();
        double periodicPayment = bill.getAmountDue() - bill.getEscrow();
        int totalPayments = numberOfPayments(bill);

        // Total lifetime interest = (Payment * Total Terms) - Original Principal
        double totalLifetimeInterest = (periodicPayment * totalPayments) - principal;

        if (totalLifetimeInterest <= 0) return 0.0;

        // Returns cumulative interest paid so far (straight-line approximation)
        return (totalLifetimeInterest / totalPayments) * paymentsPaid(bill);
    }

    static int numberOfPayments(Bill bill) {
        int numberOfPayments = bill.getPaymentsRemaining();
        for (Payment pay : Repository.getInstance().getPayments()) {
            if (pay.getBillerName().equals(bill.getBillerName()) && pay.isPaid()) {
                ++numberOfPayments;
            }
        }
        return numberOfPayments;
    }

    /**
     * Calculates accurate APR for amortized loans using a Binary Search solver.
     */
    static double calculateApr(Bill bil) {
        double principal = bil.getBalance();
        double periodicPayment = bil.getAmountDue() - bil.getEscrow();
        int totalPayments = numberOfPayments(bil);

        if (principal <= 0 || periodicPayment <= 0 || totalPayments <= 0) return 0.0;

        // Determine periods per year based on frequency index
        double periodsPerYear = switch (bil.getFrequency()) {
            case 0 -> 365.0; // Daily
            case 1 -> 52.0; // Weekly
            case 2 -> 26.0; // Bi-weekly
            case 3 -> 12.0; // Monthly
            case 4 -> 6.0; // Bi-monthly
            case 5 -> 4.0; // Quarterly
            default -> 1.0;
        };

        // If total payments <= principal, there is no interest (0% APR)
        if (periodicPayment * totalPayments <= principal) return 0.0;

        // Binary Search for the periodic interest rate 'r'
        // Formula: Payment = (P * r) / (1 - (1 + r)^-n)
        double low = 0.0;
        double high = 1.0; // Represents 100% interest per period
        double mid = 0;

        for (int i = 0; i < 100; i++) { // 100 iterations for high precision
            mid = (low + high) / 2.0;
            if (mid <= 0.0000001) break;

            // Standard Amortization Formula
            double calculatedPayment = (principal * mid) / (1 - Math.pow(1 + mid, -totalPayments));

            if (calculatedPayment > periodicPayment) {
                high = mid;
            } else {
                low = mid;
            }
        }

        // Convert periodic rate to Annual Percentage Rate (APR)
        return mid * periodsPerYear * 100.0;
    }

    static double compoundInterest(double principal, double rate, double time) {
        return principal * (Math.pow((1 + rate / 100), time));
    }
}