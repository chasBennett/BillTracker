package com.example.billstracker.recycler_adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Category;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class TransactionsRecyclerAdapter extends RecyclerView.Adapter<TransactionsRecyclerAdapter.ViewHolder> {

    public static ArrayList<Expense> transactions;
    private final LayoutInflater mInflater;
    final Budget budget;
    final ArrayList <Long> headers = new ArrayList<>();
    final Context context;
    final ArrayList <String> categories = new ArrayList<>();
    Expense transaction;

    public TransactionsRecyclerAdapter(Context context1, ArrayList<Expense> data, Budget budget) {
        this.mInflater = LayoutInflater.from(context1);
        transactions = data;
        this.context = context1;
        this.budget = budget;

        categories.clear();

        if (this.budget != null && this.budget.getCategories() != null && !this.budget.getCategories().isEmpty()) {
            for (Category category: this.budget.getCategories()) {
                if (!categories.contains(category.getCategoryName())) {
                    categories.add(category.getCategoryName());
                }
            }
        }
        else {
            categories.add(context.getString(R.string.miscellaneous));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        this.transaction = transactions.get(holder.getBindingAdapterPosition());
        if (transaction != null) {
            holder.description.setText(transaction.getDescription());
            holder.category.setText(transaction.getCategory());
            holder.amount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(transaction.getAmount()))));

            if (!headers.contains(transaction.getDate())) {

                holder.dateHeader.setText(DateFormat.makeDateString(transaction.getDate()));
                holder.dateHeader.setVisibility(View.VISIBLE);
                headers.add(transaction.getDate());
            } else {
                holder.dateHeader.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView description;
        final TextView category;
        final TextView amount;
        final ShapeableImageView transactionIcon;
        final TextView dateHeader;


        ViewHolder(View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.viewExpenseDescription);
            category = itemView.findViewById(R.id.viewExpenseCategory);
            amount = itemView.findViewById(R.id.viewExpenseAmount);
            dateHeader = itemView.findViewById(R.id.expenseDateHeader);
            transactionIcon = itemView.findViewById(R.id.transactionPartnerName);
        }
    }
    public Expense getTransaction(int id) {
        return transactions.get(id);
    }
}
