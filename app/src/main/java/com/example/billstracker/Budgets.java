package com.example.billstracker;

public class Budgets {

    private double payAmount;
    private int payFrequency;
    private int startDate;
    private int endDate;
    private int budgetId;
    private int automotivePercentage;
    private int beautyPercentage;
    private int clothingPercentage;
    private int entertainmentPercentage;
    private int groceriesPercentage;
    private int healthPercentage;
    private int restaurantsPercentage;
    private int otherPercentage;
    private int savingsPercentage;

    public Budgets (double payAmount, int payFrequency, int startDate, int endDate, int budgetId, int automotivePercentage, int beautyPercentage, int clothingPercentage, int entertainmentPercentage, int groceriesPercentage,
                    int healthPercentage, int restaurantsPercentage, int otherPercentage, int savingsPercentage) {

        setPayAmount(payAmount);
        setPayFrequency(payFrequency);
        setStartDate(startDate);
        setEndDate(endDate);
        setBudgetId(budgetId);
        setAutomotivePercentage(automotivePercentage);
        setBeautyPercentage(beautyPercentage);
        setClothingPercentage(clothingPercentage);
        setEntertainmentPercentage(entertainmentPercentage);
        setGroceriesPercentage(groceriesPercentage);
        setHealthPercentage(healthPercentage);
        setRestaurantsPercentage(restaurantsPercentage);
        setOtherPercentage(otherPercentage);
        setSavingsPercentage(savingsPercentage);
    }

    public Budgets () {

    }

    public int getStartDate() {
        return startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public int getAutomotivePercentage() {
        return automotivePercentage;
    }

    public void setAutomotivePercentage(int automotivePercentage) {
        this.automotivePercentage = automotivePercentage;
    }

    public int getBeautyPercentage() {
        return beautyPercentage;
    }

    public void setBeautyPercentage(int beautyPercentage) {
        this.beautyPercentage = beautyPercentage;
    }

    public int getClothingPercentage() {
        return clothingPercentage;
    }

    public void setClothingPercentage(int clothingPercentage) {
        this.clothingPercentage = clothingPercentage;
    }

    public int getEntertainmentPercentage() {
        return entertainmentPercentage;
    }

    public void setEntertainmentPercentage(int entertainmentPercentage) {
        this.entertainmentPercentage = entertainmentPercentage;
    }

    public int getGroceriesPercentage() {
        return groceriesPercentage;
    }

    public void setGroceriesPercentage(int groceriesPercentage) {
        this.groceriesPercentage = groceriesPercentage;
    }

    public int getHealthPercentage() {
        return healthPercentage;
    }

    public void setHealthPercentage(int healthPercentage) {
        this.healthPercentage = healthPercentage;
    }

    public int getRestaurantsPercentage() {
        return restaurantsPercentage;
    }

    public void setRestaurantsPercentage(int restaurantsPercentage) {
        this.restaurantsPercentage = restaurantsPercentage;
    }

    public int getOtherPercentage() {
        return otherPercentage;
    }

    public void setOtherPercentage(int otherPercentage) {
        this.otherPercentage = otherPercentage;
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

    public void setSavingsPercentage(int savingsPercentage) {
        this.savingsPercentage = savingsPercentage;
    }

    public int getSavingsPercentage() {
        return savingsPercentage;
    }
}
