package com.example.billstracker;

import static com.example.billstracker.Logon.billers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

// Extends the Adapter class to RecyclerView.Adapter
// and implement the unimplemented methods
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    DateFormatter df = new DateFormatter();

    private final Context context;
    private final ArrayList <Payments> paymentList;
    Payments payment;

    public Adapter(Context context, ArrayList <Payments> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_box, parent, false);
        v.setClipToOutline(true);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(paymentList.get(position));

        payment = paymentList.get(position);

        for (Biller biller: billers) {
            if (payment.getBillerName().toLowerCase().trim().contains(biller.getBillerName().toLowerCase().trim())) {
                Picasso.get().load(biller.getIcon()).into(holder.icon);
            }
        }
        holder.billerName.setText(payment.getBillerName());
        holder.dueDate.setText("Due Today");
        DecimalFormat df = new DecimalFormat("###,###,##0.00");
        double total = Double.parseDouble(payment.getPaymentAmount().replaceAll("[$]", ""));
        String amount = df.format(total);
        holder.amountDue.setText(String.format(Locale.US, "$%s", amount));

    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView icon;
        public TextView billerName;
        public TextView dueDate;
        public TextView amountDue;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.billIcon);
            billerName = itemView.findViewById(R.id.tvBillerName);
            dueDate = itemView.findViewById(R.id.tvDueDate);
            amountDue = itemView.findViewById(R.id.amountDue);
            int todayDateValue = df.currentDateAsInt();

            itemView.setOnClickListener(view -> {

                Intent pay = new Intent(context, PayBill.class);
                pay.putExtra("Due Date", dueDate.getText());
                pay.putExtra("Biller Name", billerName.getText());
                pay.putExtra("Amount Due", amountDue.getText());
                pay.putExtra("Is Paid", payment.isPaid());
                pay.putExtra("Payment Id", payment.getPaymentId());
                pay.putExtra("Current Date", todayDateValue);
                context.startActivity(pay);

            });

        }
    }
}