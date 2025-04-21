package com.example.billstracker.custom_objects;

import java.util.ArrayList;

/** @noinspection unused*/
public class Budget {

    private double payAmount;
    private int payFrequency;
    private long startDate;
    private long endDate;
    private int budgetId;
    private int savingsPercentage;
    private ArrayList <Category> categories;

    public Budget(double payAmount, int payFrequency, long startDate, long endDate, int budgetId, int savingsPercentage, ArrayList <Category> categories) {

        setPayAmount(payAmount);
        setPayFrequency(payFrequency);
        setStartDate(startDate);
        setEndDate(endDate);
        setBudgetId(budgetId);
        setSavingsPercentage(savingsPercentage);
        setCategories(categories);
    }

    public Budget() {

    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public double getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(double payAmount) {
        this.payAmount = payAmount;
    }

    public int getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(int payFrequency) {
        this.payFrequency = payFrequency;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public int getSavingsPercentage() {
        return savingsPercentage;
    }

    public void setSavingsPercentage(int savingsPercentage) {
        this.savingsPercentage = savingsPercentage;
    }
}
