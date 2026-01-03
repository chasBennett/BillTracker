package com.example.billstracker.custom_objects;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    private String userName;
    private String password;
    private String name;
    private boolean admin;
    private boolean registeredWithGoogle;
    private String lastLogin;
    private String dateRegistered;
    private String id;
    private ArrayList<Bill> bills;
    private int totalLogins;
    private String ticketNumber;
    private double income;
    private int payFrequency;
    private String termsAcceptedOn;
    private ArrayList <Trophy> trophies;
    private ArrayList <Budget> budgets;
    private String phoneNumber;
    private ArrayList <Partner> partners;
    private int versionNumber;

    public User(String userName, String password, String name, boolean admin, boolean registeredWithGoogle,
                String lastLogin, String dateRegistered, String id, int totalLogins, String ticketNumber, double income, int payFrequency,
                ArrayList <Budget> budgets, ArrayList <Partner> partners, int versionNumber) {

        setUserName(userName);
        setPassword(password);
        setName(name);
        setAdmin(admin);
        setRegisteredWithGoogle(registeredWithGoogle);
        setLastLogin(lastLogin);
        setDateRegistered(dateRegistered);
        setId(id);
        setTotalLogins(totalLogins);
        setTicketNumber(ticketNumber);
        setIncome(income);
        setPayFrequency(payFrequency);
        setBudgets(budgets);
        setPartners(partners);
        setVersionNumber(versionNumber);
    }

    public User(String userName, String password, String name, boolean admin, String lastLogin, String dateRegistered, String id, int totalLogins, String ticketNumber, double income, int payFrequency,
                ArrayList <Trophy> trophies, ArrayList <Budget> budgets, ArrayList <Partner> partners, int versionNumber) {

        setUserName(userName);
        setPassword(password);
        setName(name);
        setAdmin(admin);
        setLastLogin(lastLogin);
        setDateRegistered(dateRegistered);
        setId(id);
        setTotalLogins(totalLogins);
        setTicketNumber(ticketNumber);
        setIncome(income);
        setPayFrequency(payFrequency);
        setTrophies(trophies);
        setBudgets(budgets);
        setPartners(partners);
        setVersionNumber(versionNumber);
    }

    public User(String userName, String password, String name, boolean admin, boolean registeredWithGoogle,
                String lastLogin, String dateRegistered, String id, ArrayList<Bill> bills, int totalLogins, String ticketNumber, double income, int payFrequency,
                boolean termsAccepted, String termsAcceptedOn, ArrayList <Trophy> trophies, ArrayList <Budget> budgets, String phoneNumber, ArrayList <Partner> partners, int versionNumber) {

        setUserName(userName);
        setPassword(password);
        setName(name);
        setAdmin(admin);
        setRegisteredWithGoogle(true);
        setLastLogin(lastLogin);
        setDateRegistered(dateRegistered);
        setId(id);
        setBills(bills);
        setTotalLogins(totalLogins);
        setTicketNumber(ticketNumber);
        setIncome(income);
        setPayFrequency(payFrequency);
        setTermsAcceptedOn(termsAcceptedOn);
        setTrophies(trophies);
        setBudgets(budgets);
        setPhoneNumber(phoneNumber);
        setPartners(partners);
        setVersionNumber(versionNumber);
    }

    public User(String userName, String password, String name, boolean admin, String lastLogin, String dateRegistered, String id, int totalLogins, String ticketNumber, double income, int payFrequency,
                ArrayList <Trophy> trophies, ArrayList <Partner> partners, int versionNumber) {

        setUserName(userName);
        setPassword(password);
        setName(name);
        setAdmin(admin);
        setLastLogin(lastLogin);
        setDateRegistered(dateRegistered);
        setId(id);
        setTotalLogins(totalLogins);
        setTicketNumber(ticketNumber);
        setIncome(income);
        setPayFrequency(payFrequency);
        setTrophies(trophies);
        setPartners(partners);
    }

    public User() {
        this.bills = new ArrayList<>();
        this.trophies = new ArrayList<>();
        this.budgets = new ArrayList<>();
        this.partners = new ArrayList<>();
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean getRegisteredWithGoogle() {
        return registeredWithGoogle;
    }

    public void setRegisteredWithGoogle(boolean registeredWithGoogle) {
        this.registeredWithGoogle = registeredWithGoogle;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Bill> getBills() {
        return bills;
    }

    public void setBills(ArrayList<Bill> bills) {
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

    public int getPayFrequency() {
        return payFrequency;
    }

    public void setPayFrequency(int payFrequency) {
        this.payFrequency = payFrequency;
    }

    public String getTermsAcceptedOn() {
        return termsAcceptedOn;
    }

    public void setTermsAcceptedOn(String termsAcceptedOn) {
        this.termsAcceptedOn = termsAcceptedOn;
    }

    public ArrayList<Trophy> getTrophies() {
        return trophies;
    }

    public void setTrophies(ArrayList<Trophy> trophies) {
        this.trophies = trophies;
    }

    public ArrayList<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(ArrayList<Budget> budgets) {
        this.budgets = budgets;
    }
    public String getPhoneNumber () {
        return phoneNumber;
    }
    public void setPhoneNumber (String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public ArrayList <Partner> getPartners () {
        return partners;
    }
    public void setPartners (ArrayList <Partner> partners) {
        this.partners = partners;
    }
    public int getVersionNumber () {
        return versionNumber;
    }
    public void setVersionNumber (int versionNumber) {
        this.versionNumber = versionNumber;
    }
}

