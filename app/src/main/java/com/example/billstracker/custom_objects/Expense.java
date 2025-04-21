package com.example.billstracker.custom_objects;

/** @noinspection unused*/
public class Expense {

    private String description;
    private String category;
    private long date;
    private double amount;
    private String id;
    private String owner;

    public Expense(String description, String category, long date, double amount, String id, String owner) {

        setDescription(description);
        setCategory(category);
        setDate(date);
        setAmount(amount);
        setId(id);
        setOwner(owner);
    }

    public Expense() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getOwner () {
        return owner;
    }
    public void setOwner (String owner) {
        this.owner = owner;
    }
}
