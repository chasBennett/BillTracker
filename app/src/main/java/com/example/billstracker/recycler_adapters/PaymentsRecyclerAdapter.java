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
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;

import java.util.ArrayList;

public class PaymentsRecyclerAdapter extends RecyclerView.Adapter<PaymentsRecyclerAdapter.ViewHolder> {

    private final ArrayList<Payment> payments;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public PaymentsRecyclerAdapter(Context context, ArrayList<Payment> data) {
        this.mInflater = LayoutInflater.from(context);
        this.payments = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.payment_box, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Payment payment = payments.get(holder.getBindingAdapterPosition());
        holder.billerName.setText(payment.getBillerName());
        holder.datePaid.setText(DateFormat.makeDateString(payment.getDatePaid()));
        if (payment.isPaid()) {
            holder.paymentAmount.setText(FixNumber.addSymbol(String.valueOf(payment.getPaymentAmount())));
        }
        else {
            holder.paymentAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(payment.getPartialPayment()))));
        }
        holder.dateDue.setText(DateFormat.makeDateString(payment.getDueDate()));

        holder.itemView.setOnClickListener(view -> {
            if (mClickListener != null) mClickListener.onItemClick(position, payments.get(position));
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return payments.size();
    }


    // stores and recycles views as they are scrolled off screen
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView billerName;
        final TextView dateDue;
        final TextView datePaid;
        final TextView paymentAmount;


        ViewHolder(View itemView) {
            super(itemView);

            billerName = itemView.findViewById(R.id.billerNameBox);
            dateDue =itemView.findViewById(R.id.paymentDateBox3);
            datePaid = itemView.findViewById(R.id.paymentDateBox);
            paymentAmount = itemView.findViewById(R.id.paymentAmountBox);
        }
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int ignoredPosition, Payment payment);
    }

}