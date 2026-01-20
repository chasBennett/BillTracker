package com.example.billstracker.custom_objects;

import android.content.Context;
import com.example.billstracker.tools.Repository;
import com.google.firebase.firestore.Exclude;
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
    private ArrayList<Trophy> trophies;
    private ArrayList<Budget> budgets;
    private String phoneNumber;
    private ArrayList<Partner> partners;
    private int versionNumber;
    private boolean needsSync = false;

    /**
     * FIREBASE CONSTRUCTOR
     * Required for Firestore. Initializes all lists to prevent NullPointerExceptions.
     */
    public User() {
        this.bills = new ArrayList<>();
        this.budgets = new ArrayList<>();
        this.partners = new ArrayList<>();
        this.trophies = new ArrayList<>();
    }

    /**
     * MASTER CONSTRUCTOR (Legacy Conversion)
     * Used for instances where a legacy user needs to convert to the new model.
     */
    public User(String userName, String password, String name, boolean admin, boolean registeredWithGoogle,
                String lastLogin, String dateRegistered, String id, ArrayList<Bill> bills, int totalLogins,
                String ticketNumber, double income, int payFrequency, String termsAcceptedOn,
                ArrayList<Trophy> trophies, ArrayList<Budget> budgets, String phoneNumber,
                ArrayList<Partner> partners, int versionNumber) {

        this(); // Safety first: Initialize all lists via the empty constructor

        this.userName = userName;
        this.password = password;
        this.name = name;
        this.admin = admin;
        this.registeredWithGoogle = registeredWithGoogle;
        this.lastLogin = lastLogin;
        this.dateRegistered = dateRegistered;
        this.id = id;
        this.bills = (bills != null) ? bills : this.bills;
        this.totalLogins = totalLogins;
        this.ticketNumber = ticketNumber;
        this.income = income;
        this.payFrequency = payFrequency;
        this.termsAcceptedOn = termsAcceptedOn;
        this.trophies = (trophies != null) ? trophies : this.trophies;
        this.budgets = (budgets != null) ? budgets : this.budgets;
        this.phoneNumber = phoneNumber;
        this.partners = (partners != null) ? partners : this.partners;
        this.versionNumber = versionNumber;
        this.needsSync = true;
    }

    /**
     * SAFETY CONSTRUCTOR (New Users)
     */
    public User(String email, String password, String name, String id) {
        this();
        this.userName = email;
        this.password = password;
        this.name = name;
        this.id = id;
        this.needsSync = true;
        this.versionNumber = 1;
    }

    // --- GETTERS AND SETTERS ---
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
    public boolean getRegisteredWithGoogle() { return registeredWithGoogle; }
    public void setRegisteredWithGoogle(boolean registeredWithGoogle) { this.registeredWithGoogle = registeredWithGoogle; }
    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public String getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(String dateRegistered) { this.dateRegistered = dateRegistered; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ArrayList<Bill> getBills() { return bills; }
    public void setBills(ArrayList<Bill> bills) { this.bills = bills; }
    public int getTotalLogins() { return totalLogins; }
    public void setTotalLogins(int totalLogins) { this.totalLogins = totalLogins; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }
    public int getPayFrequency() { return payFrequency; }
    public void setPayFrequency(int payFrequency) { this.payFrequency = payFrequency; }
    public String getTermsAcceptedOn() { return termsAcceptedOn; }
    public void setTermsAcceptedOn(String termsAcceptedOn) { this.termsAcceptedOn = termsAcceptedOn; }
    public ArrayList<Trophy> getTrophies() { return trophies; }
    public void setTrophies(ArrayList<Trophy> trophies) { this.trophies = trophies; }
    public ArrayList<Budget> getBudgets() { return budgets; }
    public void setBudgets(ArrayList<Budget> budgets) { this.budgets = budgets; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public ArrayList<Partner> getPartners() { return partners; }
    public void setPartners(ArrayList<Partner> partners) { this.partners = partners; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    @Exclude
    public boolean isNeedsSync() { return needsSync; }
    public void setNeedsSync(boolean needsSync) { this.needsSync = needsSync; }

    // --- BUILDER CLASS ---
    public static class Builder {
        private final User user;
        private final Context context;

        public Builder(Context context, User user) {
            this.context = context;
            this.user = user;
        }

        public Builder setUserName(String userName) { user.setUserName(userName); user.setNeedsSync(true); return this; }
        public Builder setPassword(String password) { user.setPassword(password); user.setNeedsSync(true); return this; }
        public Builder setName(String name) { user.setName(name); user.setNeedsSync(true); return this; }
        public Builder setAdmin(boolean admin) { user.setAdmin(admin); user.setNeedsSync(true); return this; }
        public Builder setRegisteredWithGoogle(boolean registered) { user.setRegisteredWithGoogle(registered); user.setNeedsSync(true); return this; }
        public Builder setLastLogin(String lastLogin) { user.setLastLogin(lastLogin); user.setNeedsSync(true); return this; }
        public Builder setDateRegistered(String date) { user.setDateRegistered(date); user.setNeedsSync(true); return this; }
        public Builder setTotalLogins(int total) { user.setTotalLogins(total); user.setNeedsSync(true); return this; }
        public Builder setIncome(double income) { user.setIncome(income); user.setNeedsSync(true); return this; }
        public Builder setPayFrequency(int frequency) { user.setPayFrequency(frequency); user.setNeedsSync(true); return this; }
        public Builder setPhoneNumber(String phone) { user.setPhoneNumber(phone); user.setNeedsSync(true); return this; }
        public Builder setVersionNumber(int version) { user.setVersionNumber(version); user.setNeedsSync(true); return this; }
        public Builder setNeedsSync(boolean needsSync) { user.setNeedsSync(needsSync); return this; }
        public Builder setId(String id) { user.setId(id); user.setNeedsSync(true); return this; }

        public void save(Repository.OnCompleteCallback callback) {
            if (user != null) {
                Repository.getInstance().saveData(context, callback);
            }
        }
    }
}