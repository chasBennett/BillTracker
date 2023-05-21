package com.example.billstracker;

import java.util.ArrayList;

public class ExpenseInfo {
    private ArrayList <Expenses> expenses;

    public ExpenseInfo (ArrayList <Expenses> expenses) {

        setExpenses(expenses);
    }
    public ExpenseInfo () {

    }

    public ArrayList<Expenses> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expenses> expenses) {
        this.expenses = expenses;
    }
}
