package com.example.billstracker.recycler_adapters;

import static com.example.billstracker.activities.MainActivity2.todayTotal;
import static com.example.billstracker.tools.CalculateBalance.calculateApr;
import static com.example.billstracker.tools.CalculateBalance.calculateNewBalance;
import static com.example.billstracker.tools.CalculateBalance.interestPaid;
import static com.example.billstracker.tools.CalculateBalance.paymentsPaid;
import static com.example.billstracker.tools.CalculateBalance.totalPaid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.activities.AddBiller;
import com.example.billstracker.activities.MainActivity2;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    final String type;
    final Activity activity;
    private final ArrayList<Payment> payments;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemClickListener mClickListener1;
    private ItemClickListener listener;

    public RecyclerAdapter(Activity activity1, Context context, ArrayList<Payment> data, String typ) {
        this.mInflater = LayoutInflater.from(context);
        this.payments = data;
        this.activity = activity1;
        this.type = typ;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.bill_box, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Payment payment = payments.get(holder.getBindingAdapterPosition());

        Bill bil = new Bill();
        double pastDue = 0;
        long finalPay = 0;
        double partPayment = 0;

        if (payment.isPaid()) {
            holder.markPaid.setText(R.string.mark_as_unpaid);
        }

        todayTotal += pastDue;
        for (Bill bill : Repository.getInstance().getBills()) {
            if (bill.getBillerName().equals(payment.getBillerName())) {
                bil = bill;
                if (bill.getPaymentsRemaining() != 0) {
                    for (Payment pay : Repository.getInstance().getPayments()) {
                        if (pay.getBillerName().equals(bill.getBillerName()) && pay.getDueDate() > finalPay && !pay.isPaid()) {
                            finalPay = pay.getDueDate();
                        }
                        if (pay.getBillerName().equals(payment.getBillerName()) && !pay.isPaid() && pay.getDueDate() < payment.getDueDate() && payment.getDueDate() <= DateFormat.currentDateAsLong()) {
                            pastDue = pastDue + pay.getPaymentAmount() - pay.getPartialPayment();
                        }
                        if (!pay.isPaid() && pay.getPartialPayment() > 0) {
                            partPayment += pay.getPartialPayment();
                        }
                    }
                    todayTotal += pastDue;
                }
                if (bill.isAutoPay()) {
                    holder.autoPay.setVisibility(View.VISIBLE);
                } else {
                    holder.autoPay.setVisibility(View.GONE);
                }
                Tools.loadIcon(holder.icon, bill.getCategory(), bill.getIcon());
                break;
            }
        }

        final boolean[] showing = {false};
        int paymentsMadeCounter = paymentsPaid(bil);
        double totalPaidCounter = totalPaid(bil);
        int paymentsRemaining;
        double amountRemaining;
        double interestPaid;
        double rate;
        if (bil.getCategory() == 0 || bil.getCategory() == 1 || bil.getCategory() == 5 || bil.getCategory() == 6) {
            rate = calculateApr(bil);
            interestPaid = interestPaid(bil);
            if (bil.getCategory() != 1) {
                paymentsRemaining = bil.getPaymentsRemaining();
                amountRemaining = bil.getPaymentsRemaining() * bil.getAmountDue();
            } else {
                paymentsRemaining = 0;
                amountRemaining = 0;
            }
        } else {
            paymentsRemaining = 0;
            rate = 0;
            interestPaid = 0;
            amountRemaining = 0;
        }
        double escrow = bil.getEscrow();
        double balance = calculateNewBalance(bil);

        if (pastDue == 0) {
            holder.balanceForwardLabel.setVisibility(View.GONE);
            holder.balanceForward.setVisibility(View.GONE);
        } else {
            holder.balanceForwardLabel.setVisibility(View.VISIBLE);
            holder.balanceForward.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(pastDue))));
        }

        double finalPartPayment = partPayment;
        holder.viewDetails.setOnClickListener(v -> {
            if (!showing[0]) {
                showing[0] = true;
                holder.arrow.animate().rotation(-180);
                holder.detailedView.setVisibility(View.VISIBLE);
                holder.viewDetailsLabel.setText(activity.getString(R.string.hide_details));
                holder.payMade.setText(String.valueOf(paymentsMadeCounter));
                holder.totalPaid.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(totalPaidCounter))));
                if (paymentsRemaining > 0) {
                    holder.payRemainLayout.setVisibility(View.VISIBLE);
                    holder.payRemain.setText(String.valueOf(paymentsRemaining));
                }
                if (amountRemaining > 0) {
                    holder.amountRemainLayout.setVisibility(View.VISIBLE);
                    holder.amountRemain.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(amountRemaining))));
                }
                if (rate > 0) {
                    holder.estRateLayout.setVisibility(View.VISIBLE);
                    holder.estRate.setText(String.format("%s%%", FixNumber.makeDouble(String.valueOf(rate))));
                }
                if (escrow > 0) {
                    holder.escAmountLayout.setVisibility(View.VISIBLE);
                    holder.escAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(escrow))));
                }
                if (balance > 0) {
                    holder.estBalanceLayout.setVisibility(View.VISIBLE);
                    holder.estBalance.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(balance))));
                }
                if (finalPartPayment > 0) {
                    holder.partPaymentLayout.setVisibility(View.VISIBLE);
                    holder.partPaymentMade.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(finalPartPayment))));
                }
                if (interestPaid > 0) {
                    holder.estInterestPaidLayout.setVisibility(View.VISIBLE);
                    holder.estInterestPaid.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(interestPaid))));
                }
            } else {
                showing[0] = false;
                holder.arrow.animate().rotation(0);
                holder.detailedView.setVisibility(View.GONE);
                holder.viewDetailsLabel.setText(activity.getString(R.string.view_details));
            }
        });
        if (bil.getPaymentsRemaining() == 1) {
            holder.finalPayment.setVisibility(View.VISIBLE);
        }
        holder.billerName.setText(payment.getBillerName());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        TextView divider = new TextView(activity);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(activity.getResources().getColor(R.color.lightGrey, activity.getTheme()));
        holder.amountDue.setText(FixNumber.addSymbol(String.valueOf(payment.getPaymentAmount() - payment.getPartialPayment() + pastDue)));
        View onTime = View.inflate(activity, R.layout.on_time, null);
        LocalDate todayDateValue = DateFormat.makeLocalDate(MainActivity2.todayDateValue);
        LocalDate sunday = DateFormat.makeLocalDate(MainActivity2.sunday);
        LocalDate dueDate = DateFormat.makeLocalDate(payment.getDueDate());
        switch (type) {
            case "Today":
                int daysLate = 0;
                if (todayDateValue.isAfter(dueDate)) {
                    daysLate = DateFormat.daysBetween(todayDateValue, dueDate);
                }
                if (daysLate < 0) {
                    daysLate = daysLate * -1;
                }
                if (!payment.isPaid() && daysLate != 0) {

                    holder.status.removeAllViews();
                    View late = View.inflate(activity, R.layout.late, null);
                    holder.status.addView(late);

                    if (daysLate == 1) {
                        holder.dueDate.setText(R.string.dueYesterday);
                    } else {
                        holder.dueDate.setText(String.format(Locale.getDefault(), "%s %d %s", activity.getString(R.string.due), daysLate, activity.getString(R.string.daysAgo)));
                    }
                } else {
                    holder.dueDate.setText(R.string.dueToday);
                }
                break;
            case "LaterThisWeek":
                LocalDate local = DateFormat.makeLocalDate(payment.getDueDate());
                if (todayDateValue.isBefore(local) && (int) ChronoUnit.DAYS.between(todayDateValue, local) == 1) {
                    holder.dueDate.setText(R.string.dueTomorrow);
                } else if (local.isAfter(sunday) && local.isBefore(sunday.plusDays(7))) {
                    holder.dueDate.setText(String.format("%s %s", activity.getString(R.string.due), local.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())));
                } else {
                    holder.dueDate.setText(String.format(Locale.getDefault(), "%s %s", activity.getString(R.string.due), DateFormat.makeDateString(payment.getDueDate())));
                }
                break;
            case "LaterThisMonth":
                holder.dueDate.setText(String.format(Locale.getDefault(), "%s %s", activity.getString(R.string.due), DateFormat.makeDateString(payment.getDueDate())));
                break;
            default:
                holder.dueDate.setText(String.format(Locale.getDefault(), "%s %s", activity.getString(R.string.paid), DateFormat.makeDateString(payment.getDatePaid())));
                holder.status.removeAllViews();
                holder.status.addView(onTime);
                break;
        }

        holder.itemView.setOnClickListener(view -> {
            if (mClickListener != null)
                mClickListener.onItemClick(position, payments.get(position));
        });
        holder.itemView.setOnLongClickListener(view -> {

            PopupWindow popupWindow = new PopupWindow(holder.itemView);
            View popup = View.inflate(activity, R.layout.bill_menu, null);
            TextView visitWebsite = popup.findViewById(R.id.visit_website);
            TextView editBill = popup.findViewById(R.id.edit_bill);
            TextView paidOff = popup.findViewById(R.id.paidOff);
            popupWindow.setFocusable(true);
            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setContentView(popup);

            int[] values = new int[2];
            holder.itemView.getLocationInWindow(values);
            int positionOfIcon = values[1];
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            int height = (displayMetrics.heightPixels * 2) / 4;
            if (positionOfIcon > height) {
                popupWindow.showAsDropDown(holder.itemView, 50, -(holder.itemView.getMeasuredHeight()) - 550);
            } else {
                popupWindow.showAsDropDown(holder.itemView, 25, 25);
            }

            visitWebsite.setOnClickListener(view13 -> {
                for (Bill bill : Repository.getInstance().getBills()) {
                    if (bill.getBillerName().equals(payment.getBillerName())) {
                        String address = bill.getWebsite();
                        if (!address.startsWith("http://") && !address.startsWith("https://")) {
                            address = "http://" + address;
                        }
                        Intent launch = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                        activity.startActivity(launch);
                        popupWindow.dismiss();
                    }
                }
                if (mClickListener1 != null)
                    mClickListener1.onItemClick(position, payments.get(position));

            });

            editBill.setOnClickListener(view1 -> {
                for (Bill bill : Repository.getInstance().getBills()) {
                    if (bill.getBillerName().equals(payment.getBillerName())) {
                        activity.startActivity(new Intent(activity, AddBiller.class).putExtra("billerId", bill.getBillsId()));
                        popupWindow.dismiss();
                    }
                }
                if (listener != null) listener.onItemClick(position, payments.get(position));
            });
            paidOff.setOnClickListener(view12 -> {
                Tools.billPaidOff(activity, payment);
                if (mClickListener1 != null)
                    mClickListener1.onItemClick(position, payments.get(position));
            });
            return false;
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return payments.size();
    }

    // convenience method for getting data at click position
    public Payment getPayment(int id) {
        return payments.get(id);
    }

    // allows clicks events to be caught
    public ArrayList<Payment> getArrayList() {
        return payments;
    }

    public void setPayBillClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setRefreshPaymentsClickListener(ItemClickListener itemClickListener) {
        this.mClickListener1 = itemClickListener;
    }

    public void setLoadingClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int ignoredPosition, Payment payment);
    }

    // stores and recycles views as they are scrolled off screen
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView icon;
        final ImageView arrow;
        final ImageView autoPay;
        final TextView billerName;
        final TextView amountDue;
        final TextView dueDate;
        final TextView finalPayment;
        final TextView viewDetailsLabel;
        final TextView payMade;
        final TextView totalPaid;
        final TextView payRemain;
        final TextView amountRemain;
        final TextView estRate;
        final TextView escAmount;
        final TextView estBalance;
        final TextView partPaymentMade;
        final TextView estInterestPaid;
        final TextView balanceForwardLabel;
        final TextView balanceForward;
        final TextView markPaid;
        final LinearLayout status;
        final LinearLayout viewDetails;
        final LinearLayout detailedView;
        final LinearLayout payRemainLayout;
        final LinearLayout amountRemainLayout;
        final LinearLayout estRateLayout;
        final LinearLayout escAmountLayout;
        final LinearLayout estBalanceLayout;
        final LinearLayout partPaymentLayout;
        final LinearLayout estInterestPaidLayout;


        ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.billIcon);
            billerName = itemView.findViewById(R.id.tvBillerName);
            amountDue = itemView.findViewById(R.id.amountDue);
            dueDate = itemView.findViewById(R.id.tvDueDate);
            status = itemView.findViewById(R.id.status);
            autoPay = itemView.findViewById(R.id.autoPay);
            estRateLayout = itemView.findViewById(R.id.estRateLayout);
            escAmountLayout = itemView.findViewById(R.id.escAmountLayout);
            estBalanceLayout = itemView.findViewById(R.id.estBalanceLayout);
            estInterestPaidLayout = itemView.findViewById(R.id.estInterestPaidLayout);
            payMade = itemView.findViewById(R.id.payMade);
            balanceForwardLabel = itemView.findViewById(R.id.balanceForwardLabel);
            balanceForward = itemView.findViewById(R.id.balanceForward);
            totalPaid = itemView.findViewById(R.id.totalPaid);
            payRemain = itemView.findViewById(R.id.payRemain);
            amountRemain = itemView.findViewById(R.id.amountRemain);
            payRemainLayout = itemView.findViewById(R.id.payRemainLayout);
            amountRemainLayout = itemView.findViewById(R.id.amountRemainLayout);
            partPaymentLayout = itemView.findViewById(R.id.partPaymentLayout);
            partPaymentMade = itemView.findViewById(R.id.partPaymentMade);
            estRate = itemView.findViewById(R.id.estRate);
            markPaid = itemView.findViewById(R.id.markPaid);
            escAmount = itemView.findViewById(R.id.escAmount);
            estBalance = itemView.findViewById(R.id.estBalance);
            estInterestPaid = itemView.findViewById(R.id.estInterestPaid);
            viewDetails = itemView.findViewById(R.id.btnViewDetails);
            detailedView = itemView.findViewById(R.id.detailedView);
            arrow = itemView.findViewById(R.id.arrowViewDetails);
            viewDetailsLabel = itemView.findViewById(R.id.viewDetailsLabel);
            finalPayment = itemView.findViewById(R.id.finalPayment);

            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(lp1);
            itemView.setElevation(15);

            itemView.animate().translationX(0).setDuration(500);
        }
    }
}