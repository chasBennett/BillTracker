package com.example.billstracker;

import java.io.Serializable;
import java.util.ArrayList;

public class Login implements Serializable {


    private static final long serialVersionUID = 1L;
    private String userName;
    private String password;
    private String name;
    private boolean admin;
    private String lastLogin;
    private String dateRegistered;
    private String id;
    private ArrayList<Bills> bills;
    private int totalLogins;
    private String ticketNumber;
    private double income;
    private String payFrequency;
    private boolean termsAccepted;
    private String termsAcceptedOn;
    private String currency;
    private ArrayList <Trophy> trophies;
    private ArrayList <Expenses> expenses;
    private ArrayList <Budgets> budgets;


    public Login(String userName, String password, String name, boolean admin,
                 String lastLogin, String dateRegistered, String id, ArrayList<Bills> bills, int totalLogins, String ticketNumber, double income, String payFrequency,
                 boolean termsAccepted, String termsAcceptedOn, String currency, ArrayList <Trophy> trophies, ArrayList <Expenses> expenses, ArrayList <Budgets> budgets) {

        setUserName(userName);
        setPassword(password);
        setName(name);
        setAdmin(admin);
        setLastLogin(lastLogin);
        setDateRegistered(dateRegistered);
        setid(id);
        setBills(bills);
        setTotalLogins(totalLogins);
        setTicketNumber(ticketNumber);
        setIncome(income);
        setPayFrequency(payFrequency);
        setTermsAccepted(termsAccepted);
        setTermsAcceptedOn(termsAcceptedOn);
        setCurrency(currency);
        setTrophies(trophies);
        setExpenses(expenses);
        setBudgets(budgets);
    }

    public Login() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(String dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getid() {
        return id;
    }

    public void setid(String id) {
        this.id = id;
    }

    public ArrayList<Bills> getBills() {
        return bills;
    }

    public void setBills(ArrayList<Bills> bills) {
        this.bills = bills;
    }

    public int getTotalLogins() {
        return totalLogins;
    }

    public void setTotalLogins(int totalLogins) {
        this.totalLogins = totalLogins;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public double getIncome() {
        return income;
    }

    public void setIncome(double income) {
        this.income = income;
    }

    public String getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(String payFrequency) {
        this.payFrequency = payFrequency;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public String getTermsAcceptedOn() {
        return termsAcceptedOn;
    }

    public void setTermsAcceptedOn(String termsAcceptedOn) {
        this.termsAcceptedOn = termsAcceptedOn;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ArrayList<Trophy> getTrophies() {
        return trophies;
    }

    public void setTrophies(ArrayList<Trophy> trophies) {
        this.trophies = trophies;
    }

    public ArrayList<Expenses> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expenses> expenses) {
        this.expenses = expenses;
    }

    public ArrayList<Budgets> getBudgets() {
        return budgets;
    }

    public void setBudgets(ArrayList<Budgets> budgets) {
        this.budgets = budgets;
    }
}


