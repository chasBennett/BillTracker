package com.example.billstracker;

public class Expenses {

    private String description;
    private int category;
    private int date;
    private double amount;

    public Expenses (String description, int category, int date, double amount) {

        setDescription(description);
        setCategory(category);
        setDate(date);
        setAmount(amount);
    }

    public Expenses () {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
