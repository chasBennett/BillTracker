package com.example.billstracker.custom_objects;

import android.content.Context;

import com.example.billstracker.tools.Repository;
import com.google.firebase.database.Exclude;

/**
 * @noinspection unused
 */
public class Expense {

    private String description;
    private String category;
    private long date;
    private double amount;
    private String id;
    private String owner;
    private boolean needsSync = false;
    private boolean needsDelete = false;


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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Exclude
    public boolean isNeedsSync() {
        return needsSync;
    }

    public void setNeedsSync(boolean needsSync) {
        this.needsSync = needsSync;
    }

    @Exclude
    public boolean isNeedsDelete() {
        return needsDelete;
    }

    public void setNeedsDelete(boolean needsDelete) {
        this.needsDelete = needsDelete;
    }

    public static class Builder {
        private final Expense expense;
        private final Context context;

        public Builder(Context context, Expense expense) {
            this.context = context;
            this.expense = expense;
        }

        public Builder setDescription(String desc) {
            expense.setDescription(desc);
            expense.needsSync = true;
            return this;
        }

        public Builder setCategory(String cat) {
            expense.setCategory(cat);
            expense.needsSync = true;
            return this;
        }

        public Builder setDate(long date) {
            expense.setDate(date);
            expense.needsSync = true;
            return this;
        }

        public Builder setAmount(double amt) {
            expense.setAmount(amt);
            expense.needsSync = true;
            return this;
        }

        public Builder setOwner(String owner) {
            expense.setOwner(owner);
            expense.needsSync = true;
            return this;
        }

        public void save(Repository.OnCompleteCallback callback) {
            if (expense == null) {
                if (callback != null) callback.onComplete(false, "Expense was not found.");
                return;
            }
            Repository.getInstance().saveData(context, callback);
        }
    }
}
