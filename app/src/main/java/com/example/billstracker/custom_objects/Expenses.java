package com.example.billstracker.custom_objects;

import java.util.ArrayList;

public class Expenses {
    private ArrayList<Expense> expenseList;

    public Expenses(ArrayList<Expense> expenseList) {

        setExpenses(expenseList);
    }

    public Expenses() {

    }

    public ArrayList<Expense> getExpenses() {
        if (expenseList == null)
            expenseList = new ArrayList<>();
        return expenseList;
    }

    public void setExpenses(ArrayList<Expense> expenseList) {
        this.expenseList = expenseList;
    }
}
