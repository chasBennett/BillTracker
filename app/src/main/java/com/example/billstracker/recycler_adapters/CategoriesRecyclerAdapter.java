package com.example.billstracker.recycler_adapters;

import static com.example.billstracker.activities.Spending.selectedDate;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Category;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.Repo;

import java.util.ArrayList;
import java.util.Locale;

public class CategoriesRecyclerAdapter extends RecyclerView.Adapter<CategoriesRecyclerAdapter.ViewHolder> {

    private final ArrayList<Category> categories;
    private final LayoutInflater mInflater;
    final int freq;
    final long start, end;
    final double dailyBudget;
    final boolean misc;
    private CategoryItemClickListener mClickListener;

    public CategoriesRecyclerAdapter(Context context, ArrayList<Category> data, long startDate, long endDate, int frequency, double dailyBud, boolean miscellaneous) {
        this.mInflater = LayoutInflater.from(context);
        this.categories = data;
        this.start = startDate;
        this.end = endDate;
        this.freq = frequency;
        this.dailyBudget = dailyBud;
        this.misc = miscellaneous;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.category_view, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Category category = categories.get(holder.getBindingAdapterPosition());

        double catTotal = 0;
        double remaining;
        double totalBudget = 0;
        holder.budgetCategory.setText(category.getCategoryName());
        if (Repo.getInstance().getExpenses() != null) {
            for (Expense expense : Repo.getInstance().getExpenses()) {
                if (expense.getDate() >= start && expense.getDate() <= end && expense.getCategory().equals(category.getCategoryName())) {
                    catTotal = catTotal + expense.getAmount();
                }
            }
        }
        holder.totalSpend.setText(String.format(Locale.getDefault(), "%s %s", mInflater.getContext().getString(R.string.total_spent), FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(catTotal)))));
        switch (freq) {
            case 0:
                if (misc) {
                    totalBudget = dailyBudget;
                }
                else {
                    totalBudget = dailyBudget * (category.getCategoryPercentage() / 100.0);
                }
                break;
            case 1:
                if (misc) {
                    totalBudget = dailyBudget * 7;
                }
                else {
                    totalBudget = (dailyBudget * 7) * (category.getCategoryPercentage() / 100.0);
                }
                break;
            case 2:
                if (misc) {
                    totalBudget = dailyBudget * selectedDate.lengthOfMonth();
                }
                else {
                    totalBudget = (dailyBudget * selectedDate.lengthOfMonth()) * (category.getCategoryPercentage() / 100.0);
                }
                break;
        }
        if (!misc) {
            remaining = totalBudget - catTotal;
        }
        else {
            remaining = 0;
        }
        holder.budgetRemaining.setText(String.format(Locale.getDefault(), "%s %s", mInflater.getContext().getString(R.string.budget_remaining), FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(remaining)))));
        int percentSpent = (int) ((catTotal * 100) / totalBudget);
        holder.spendPercentage.post(() -> {
            ObjectAnimator animation = ObjectAnimator.ofInt(holder.spendPercentage, "progress", percentSpent);
            animation.setDuration(500);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (mClickListener != null) mClickListener.onItemClick(position, categories.get(position));
            return false;
        });

    }
    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView budgetCategory;
        final TextView totalSpend;
        final TextView budgetRemaining;
        final ProgressBar spendPercentage;


        ViewHolder(View itemView) {
            super(itemView);

            budgetCategory = itemView.findViewById(R.id.budgetCategory);
            totalSpend = itemView.findViewById(R.id.totalSpend);
            budgetRemaining = itemView.findViewById(R.id.remainingBudget);
            spendPercentage = itemView.findViewById(R.id.spendPercentage);
        }
    }
    public void setClickListener(CategoryItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public interface CategoryItemClickListener {
        void onItemClick(int ignoredPosition, Category ignoredCategory);
    }

}