package com.example.billstracker.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.DateRange;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.FilterPayments;
import com.example.billstracker.recycler_adapters.PaymentsRecyclerAdapter;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.Repo;
import com.example.billstracker.tools.Tools;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

public class PaymentHistory extends AppCompatActivity {

    ArrayList<Payment> paymentList = new ArrayList<>();
    Context mContext;
    TextView filterPayments;
    Bundle extras;
    ConstraintLayout pb;
    public static ArrayList <Bill> selectedBillers;
    public static DateRange range;
    RecyclerView recycler;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        mContext = this;
        paymentList = new ArrayList<>();

        pb = findViewById(R.id.pb);
        Tools.fixProgressBarLogo(pb);
        extras = getIntent().getExtras();
        filterPayments = findViewById(R.id.filterPayments);
        recycler = findViewById(R.id.paymentsRecycler);

        NavController nc = new NavController();
        nc.navController(PaymentHistory.this, PaymentHistory.this, pb, "paymentHistory");

        selectedBillers = new ArrayList<>();
        range = new DateRange(DateFormat.makeLong(LocalDate.now().minusMonths(3)), DateFormat.makeLong(LocalDate.now().plusMonths(3)));

        if (extras != null) {
            String billId = extras.getString("Bill Id", "");
            Bill bil = Repo.getInstance().getBillById(billId);
            if (bil != null) selectedBillers.add(bil);
        }
        else {
            selectedBillers.addAll(Repo.getInstance().getBills());
        }
        filterPayments.setOnClickListener(v -> filterPayments());
        filterResults();
    }
    public void filterPayments () {
        FilterPayments fp = new FilterPayments(PaymentHistory.this);
        fp.setPositiveButtonListener(v -> {
            pb.setVisibility(View.VISIBLE);
            filterResults();
            fp.dismissDialog();
        });
    }

    public void filterResults () {

        paymentList.clear();

        for (Payment payment : Repo.getInstance().getPayments()) {
            if (payment.isPaid() || payment.isDateChanged() && payment.getPartialPayment() > 0) {
                if (payment.getDatePaid() >= range.getStartDate() && payment.getDatePaid() <= range.getEndDate()) {
                    if (!paymentList.contains(payment)) {
                        for (Bill bill: selectedBillers) {
                            if (bill.getBillerName().equals(payment.getBillerName())) {
                                paymentList.add(payment);
                            }
                        }
                    }
                }
            }
        }
        paymentList.sort(Comparator.comparing(Payment::getDatePaid).reversed());
        generateList();
    }

    public void generateList() {

        LinearLayout paymentList1 = findViewById(R.id.paymentsList);
        if (paymentList1.getChildCount() > 2) {
            paymentList1.removeViews(1, paymentList1.getChildCount() - 1);
        }
        if (paymentList != null) {
            if (!paymentList.isEmpty()) {
                paymentList.sort(Comparator.comparing(Payment::getDatePaid).reversed());
            }
        }
        recycler.setLayoutManager(new LinearLayoutManager(PaymentHistory.this));
        PaymentsRecyclerAdapter adapter = new PaymentsRecyclerAdapter(PaymentHistory.this, paymentList);
        recycler.setAdapter(adapter);
        recycler.setNestedScrollingEnabled(false);
        recycler.setHasFixedSize(false);
        adapter.setClickListener((position, payment) -> {
            pb.setVisibility(View.VISIBLE);
            Intent pay = new Intent(mContext, PayBill.class);
            pay.putExtra("Due Date", DateFormat.makeDateString(payment.getDueDate()));
            pay.putExtra("Biller Name", payment.getBillerName());
            pay.putExtra("Amount Due", payment.getPaymentAmount());
            pay.putExtra("Is Paid", payment.isPaid());
            pay.putExtra("Payment Id", payment.getPaymentId());
            pay.putExtra("Current Date", DateFormat.currentDateAsLong());
            startActivity(pay);
        });

        if (paymentList.isEmpty()) {
            ImageView searching = new ImageView(mContext);
            searching.setImageDrawable(AppCompatResources.getDrawable(mContext, R.drawable.noun_not_found_2503986));
            searching.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(400, 400);
            lp.setMargins(0, 100, 0, 0);
            lp.gravity = Gravity.CENTER;
            lp.setMargins(0, 100, 0, 0);
            searching.setForegroundGravity(Gravity.CENTER);
            searching.setLayoutParams(lp);
            TextView text = new TextView(mContext);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextSize(14);
            text.setTypeface(null, Typeface.BOLD);
            text.setText(R.string.noPaymentsFound);
            text.setPadding(25, 120, 25, 100);
            text.setSingleLine(false);
            paymentList1.addView(searching);
            paymentList1.addView(text);
        }
        pb.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}