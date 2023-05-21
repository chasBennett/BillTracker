package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;
import static com.example.billstracker.MainActivity2.channelId;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class PayBill extends AppCompatActivity {

    Dialog dialog;
    TextView paymentDueDate, paymentAmountDue, editBiller, viewPayments, dueDateLabel, amountDueLabel, webAddress, tvBillerName, datePaidLabel, paymentDatePaid, displayPaymentsRemaining,
            displayPartialPayment;
    ImageView paidIcon;
    com.google.android.material.imageview.ShapeableImageView payBillIcon;
    ConstraintLayout payBill;
    Button payButton, partialPayment;
    int paymentId;
    Payments pay;
    Bill bil;
    Context mContext = this;
    LinearLayout datePaidLayout, back, pb, paymentsRemainingLayout, partialPaymentLayout;
    DateFormatter df;
    String billerName;
    boolean darkMode;
    FixNumber fn = new FixNumber();
    SaveUserData save = new SaveUserData();
    BillerManager bm = new BillerManager();
    int newDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bill);
        back = findViewById(R.id.backPayBillLayout);
        payBillIcon = findViewById(R.id.payBillIcon);
        payBill = findViewById(R.id.payBill);
        tvBillerName = findViewById(R.id.billerName);
        payButton = findViewById(R.id.payButton);
        paymentAmountDue = findViewById(R.id.paymentAmountDue);
        editBiller = findViewById(R.id.editBillerButton);
        viewPayments = findViewById(R.id.viewPayments);
        dueDateLabel = findViewById(R.id.dueDateLabel);
        paidIcon = findViewById(R.id.paidIcon);
        pb = findViewById(R.id.pb10);
        amountDueLabel = findViewById(R.id.amountDueLabel);
        partialPayment = findViewById(R.id.btnPartialPayment);
        webAddress = findViewById(R.id.webAddress);
        paymentDueDate = findViewById(R.id.paymentDueDate1);
        datePaidLabel = findViewById(R.id.datePaidLabel);
        paymentDatePaid = findViewById(R.id.paymentDatePaid);
        partialPaymentLayout = findViewById(R.id.partialPaymentLayout);
        displayPartialPayment = findViewById(R.id.displayPartialPayment);
        displayPaymentsRemaining = findViewById(R.id.displayPaymentsRemaining);
        paymentsRemainingLayout = findViewById(R.id.paymentsRemainingLayout2);
        datePaidLayout = findViewById(R.id.datePaidLayout);
        df = new DateFormatter();
        darkMode = false;
        overridePendingTransition(0,0);
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            darkMode = true;
        }
        back.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            onBackPressed();
        });
        startPayBill();
    }

    public void startPayBill() {

        Bundle bundle = getIntent().getExtras();
        String dueDate = null;
        boolean isPaid = false;
        paymentId = bundle.getInt("Payment Id");
        int todaysDate = df.currentDateAsInt();

        if (thisUser == null) {
            SaveUserData load = new SaveUserData();
            load.loadUserData(PayBill.this);
        }
        ArrayList<Bill> billList = thisUser.getBills();
        String website = "www.google.com";

        for (Payments payment : paymentInfo.getPayments()) {
            if (payment.getPaymentId() == paymentId) {
                dueDate = df.convertIntDateToString(payment.getPaymentDate());
                paymentDueDate.setText(dueDate);
                pay = payment;
                billerName = pay.getBillerName();
                isPaid = pay.isPaid();
                paymentAmountDue.setText(fn.addSymbol(pay.getPaymentAmount()));
                if (payment.isPaid()) {
                    datePaidLayout.setVisibility(View.VISIBLE);
                    paymentDatePaid.setText(df.convertIntDateToString(payment.getDatePaid()));
                    amountDueLabel.setText(R.string.amount_paid);
                    payButton.setText(R.string.unmarkAsPaid);
                } else {
                    datePaidLayout.setVisibility(View.GONE);
                    payButton.setText(R.string.markAsPaid);
                    paidIcon.setImageDrawable(null);
                }
                if (payment.getPartialPayment() > 0 && !payment.isPaid()) {
                    partialPaymentLayout.setVisibility(View.VISIBLE);
                    displayPartialPayment.setText(fn.addSymbol(fn.makeDouble(String.valueOf(payment.getPartialPayment()))));
                    amountDueLabel.setText(R.string.amount_remaining);
                    paymentAmountDue.setText(fn.addSymbol(fn.makeDouble(String.valueOf(Double.parseDouble(payment.getPaymentAmount()) - payment.getPartialPayment()))));
                }
            }

        }

        for (Bill bill : thisUser.getBills()) {
            if (bill.getBillerName().equals(billerName)) {
                bil = bill;
                if (Integer.parseInt(bil.getPaymentsRemaining()) < 400) {
                    paymentsRemainingLayout.setVisibility(View.VISIBLE);
                    if (bil.getPaymentsRemaining().equals("0")) {
                        displayPaymentsRemaining.setText(getString(R.string.paid_in_full));
                    }
                    else {
                        displayPaymentsRemaining.setText(bil.getPaymentsRemaining());
                    }
                }
            }
            website = bill.getWebsite();
        }
        LoadIcon loadIcon = new LoadIcon();
        loadIcon.loadIcon(PayBill.this, payBillIcon, bil.getCategory(), bil.getIcon());
        tvBillerName.setText(pay.getBillerName());

        String finalWebsite = website;
        webAddress.setOnClickListener(view -> {
            String address = finalWebsite;
            if (!address.startsWith("http://") && !address.startsWith("https://")) {
                address = "http://" + address;
            }
            Intent launch = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
            startActivity(launch);
        });

        displayPartialPayment.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(getString(R.string.changePartialPayment));
            alert.setMessage(getString(R.string.would_you_like_to_change_your_partial_payment_or_remove_it));
            alert.setPositiveButton(getString(R.string.changeAmount), (dialogInterface, i) -> {
                AlertDialog.Builder alert1 = new AlertDialog.Builder(mContext);
                alert1.setTitle(getString(R.string.change_partial_payment_amount));
                alert1.setMessage(getString(R.string.please_enter_the_new_partial_payment_amount));
                LinearLayout ll = new LinearLayout(PayBill.this);
                final EditText input = new EditText(PayBill.this);
                input.setText(String.format(Locale.getDefault(), "  %s", fn.addSymbol(String.valueOf(pay.getPartialPayment()))));
                input.setBackground(AppCompatResources.getDrawable(PayBill.this, R.drawable.edit_text));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50, 50, 50, 50);
                ll.setLayoutParams(lp);
                ll.setPadding(30, 0, 0, 0);
                input.setLayoutParams(lp);
                ll.addView(input);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.addTextChangedListener(new MoneyTextWatcher(input));
                alert1.setView(ll);
                final double[] remaining = {0};
                alert1.setPositiveButton(getString(R.string.submit), (dialogInterface1, n) -> {
                    for (Payments pay: paymentInfo.getPayments()) {
                        if (pay.getPaymentId() == paymentId) {
                            pay.setPartialPayment(Double.parseDouble(fn.makeDouble(input.getText().toString())));
                            remaining[0] = Double.parseDouble(fn.makeDouble(pay.getPaymentAmount())) - Double.parseDouble(fn.makeDouble(input.getText().toString()));
                            pay.setDateChanged(true);
                        }
                    }
                    partialPaymentLayout.setVisibility(View.VISIBLE);
                    displayPartialPayment.setText(fn.addSymbol(fn.makeDouble(input.getText().toString())));
                    amountDueLabel.setText(R.string.amount_remaining);
                    paymentAmountDue.setText(fn.addSymbol(fn.makeDouble(String.valueOf(remaining[0]))));
                    bm.savePayments();
                    save.saveUserData(PayBill.this);
                });
                alert1.setNegativeButton(getString(R.string.cancel), (dialogInterface1, n) -> {

                });
                AlertDialog builder = alert1.create();
                builder.show();
            });
            alert.setNeutralButton(getString(R.string.remove), (dialogInterface, i) -> {
                for (Payments payments: paymentInfo.getPayments()) {
                    if (payments.getPaymentId() == pay.getPaymentId()) {
                        payments.setPartialPayment(0);
                    }
                }
                partialPaymentLayout.setVisibility(View.GONE);
                displayPartialPayment.setText("");
                amountDueLabel.setText(R.string.amount_due);
                paymentAmountDue.setText(fn.addSymbol(fn.makeDouble(String.valueOf(pay.getPaymentAmount()))));
                bm.savePayments();
                save.saveUserData(PayBill.this);
                    });
            alert.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {

            });
            AlertDialog builder = alert.create();
            builder.show();
        });

        partialPayment.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(getString(R.string.partial_payment));
            alert.setMessage(getString(R.string.pleaseEnterYourPaymentAmount));
            LinearLayout ll = new LinearLayout(PayBill.this);
            final EditText input = new EditText(PayBill.this);
            input.setText(String.format(Locale.getDefault(), "  %s", fn.addSymbol(pay.getPaymentAmount())));
            input.setBackground(AppCompatResources.getDrawable(PayBill.this, R.drawable.edit_text));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 50, 50, 50);
            ll.setLayoutParams(lp);
            ll.setPadding(30, 0, 0, 0);
            input.setLayoutParams(lp);
            ll.addView(input);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.addTextChangedListener(new MoneyTextWatcher(input));
            alert.setView(ll);
            final double[] remaining = {0};
            alert.setPositiveButton(getString(R.string.submit), (dialogInterface, i) -> {
                for (Payments pay: paymentInfo.getPayments()) {
                    if (pay.getPaymentId() == paymentId) {
                        pay.setPartialPayment(Double.parseDouble(fn.makeDouble(input.getText().toString())));
                        remaining[0] = Double.parseDouble(fn.makeDouble(pay.getPaymentAmount())) - Double.parseDouble(fn.makeDouble(input.getText().toString()));
                        pay.setDateChanged(true);
                    }
                }
                partialPaymentLayout.setVisibility(View.VISIBLE);
                displayPartialPayment.setText(fn.addSymbol(fn.makeDouble(input.getText().toString())));
                amountDueLabel.setText(R.string.amount_remaining);
                paymentAmountDue.setText(fn.addSymbol(fn.makeDouble(String.valueOf(remaining[0]))));
                bm.savePayments();
                save.saveUserData(PayBill.this);
            });
            alert.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {

            });
            AlertDialog builder = alert.create();
            builder.show();
        });

        paymentDatePaid.setOnClickListener(view -> getDateFromUser(paymentDatePaid));

        String finalDueDate = dueDate;
        boolean finalIsPaid = isPaid;
        paymentAmountDue.setOnClickListener(view -> {
            androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(mContext);
            alert.setTitle(getString(R.string.changeAmount));
            alert.setMessage(getString(R.string.pleaseEnterYourPaymentAmount));
            LinearLayout ll = new LinearLayout(PayBill.this);
            final EditText input = new EditText(PayBill.this);
            input.setText(String.format(Locale.getDefault(), "  %s", fn.addSymbol(pay.getPaymentAmount())));
            input.setBackground(AppCompatResources.getDrawable(this, R.drawable.edit_text));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 50, 50, 50);
            ll.setLayoutParams(lp);
            ll.setPadding(30, 0, 0, 0);
            input.setLayoutParams(lp);
            ll.addView(input);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.addTextChangedListener(new MoneyTextWatcher(input));
            alert.setView(ll);
            alert.setPositiveButton(getString(R.string.update), (dialogInterface, i) -> {
                AlertDialog.Builder alert1 = new AlertDialog.Builder(mContext);
                alert1.setTitle(getString(R.string.updateAllFutureBills));
                alert1.setMessage(getString(R.string.updatePaymentAmountForAll));
                String newAmountDue = fn.makeDouble(input.getText().toString());
                if (newAmountDue.equals("")) {
                    newAmountDue = "0.00";
                }
                String finalNewAmountDue = newAmountDue;
                alert1.setPositiveButton(getString(R.string.updateAll), (dialogInterface1, i12) -> {
                    pb.setVisibility(View.VISIBLE);

                    for (Bill bill : thisUser.getBills()) {
                        if (bill.getBillerName().equals(tvBillerName.getText().toString())) {
                            bill.setAmountDue(finalNewAmountDue);
                        }
                    }
                    for (Payments payment : paymentInfo.getPayments()) {
                        if (!payment.isPaid() && payment.getBillerName().equals(tvBillerName.getText().toString())) {
                            payment.setPaymentAmount(finalNewAmountDue);
                        }
                    }
                    paymentAmountDue.setText(fn.addSymbol(finalNewAmountDue));
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    if (thisUser != null) {
                        SaveUserData save = new SaveUserData();
                        save.saveUserData(PayBill.this);
                    }
                    db.collection("users").document(uid).set(thisUser, SetOptions.merge());
                    bm.savePayments();
                    pb.setVisibility(View.GONE);
                });
                alert1.setNeutralButton(getString(R.string.justThisOccurence), (dialogInterface12, i13) -> {
                    pb.setVisibility(View.VISIBLE);
                    for (Payments payment : paymentInfo.getPayments()) {
                        if (payment.getPaymentId() == (paymentId)) {
                            payment.setPaymentAmount(finalNewAmountDue);
                            payment.setDateChanged(true);
                        }
                    }
                    paymentAmountDue.setText(fn.addSymbol(finalNewAmountDue));
                    if (thisUser != null) {
                        SaveUserData save = new SaveUserData();
                        save.saveUserData(PayBill.this);
                    }
                    bm.savePayments();
                    pb.setVisibility(View.GONE);
                });
                alert1.setNegativeButton(getString(R.string.cancel), (dialogInterface13, i14) -> startPay(finalDueDate, billerName, fn.makeDouble(bil.getAmountDue()), finalIsPaid, todaysDate));
                alert1.create();
                alert1.show();
            });
            alert.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {

            });
            androidx.appcompat.app.AlertDialog builder = alert.create();
            builder.show();
        });

        boolean finalIsPaid1 = isPaid;
        editBiller.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);

            Intent edit = new Intent(mContext, EditBiller.class);
            edit.putExtra("userName", bil.getBillerName());
            edit.putExtra("website", bil.getWebsite());
            edit.putExtra("dueDate", pay.getPaymentDate());
            edit.putExtra("amountDue", pay.getPaymentAmount());
            edit.putExtra("frequency", bil.getFrequency());
            edit.putExtra("recurring", bil.isRecurring());
            edit.putExtra("Payment Id", paymentId);
            edit.putExtra("Paid", finalIsPaid1);
            startActivity(edit);
        });

        viewPayments.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);

            for (Bill bill : billList) {
                if (bill.getBillerName().equals(billerName)) {
                    Intent history = new Intent(mContext, PaymentHistory.class);
                    history.putExtra("User Id", thisUser.getid());
                    history.putExtra("Bill Id", bill.getBillsId());
                    startActivity(history);
                }
            }
        });

        boolean finalIsPaid2 = isPaid;
        payButton.setOnClickListener(view -> {

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
            if (!finalIsPaid2) {

                builder.setMessage(getString(R.string.areYouSureYouWantToMarkThisBillAsPaid)).setTitle(
                        getString(R.string.markAsPaid)).setPositiveButton(getString(R.string.markAsPaid), (dialogInterface, i) -> {
                    pb.setVisibility(View.VISIBLE);
                    for (Bill bill : billList) {
                        if (bill.getBillerName().equals(billerName)) {
                            if (!bill.getPaymentsRemaining().equals("1000")) {
                                bill.setPaymentsRemaining(String.valueOf(Integer.parseInt(bill.getPaymentsRemaining()) - 1));
                            }
                            ArrayList<Payments> find = new ArrayList<>();
                            for (Payments pay : paymentInfo.getPayments()) {
                                if (pay.getBillerName().equals(bill.getBillerName())) {
                                    find.add(pay);
                                }
                            }
                            for (Payments payment : find) {
                                if (payment.getPaymentId() == (paymentId)) {
                                    payment.setPaid(true);
                                    payment.setDatePaid(todaysDate);
                                    bill.setDateLastPaid(todaysDate);
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    nm.cancel(paymentId);
                                    nm.cancel(paymentId + 1);
                                    nm.cancel(paymentId + 11);
                                    db.collection("users").document(uid).set(thisUser, SetOptions.merge());
                                    bm.savePayments();
                                    if (thisUser != null) {
                                        SaveUserData save = new SaveUserData();
                                        save.saveUserData(PayBill.this);
                                    }
                                    Intent home = new Intent(mContext, MainActivity2.class);
                                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(home);
                                }
                            }
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                });

                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();

            } else {

                builder.setMessage(getString(R.string.areYouSure));
                builder.setTitle(getString(R.string.unmarkAsPaid));
                builder.setPositiveButton(getString(R.string.unmarkAsPaid), (dialogInterface, i) -> {
                    pb.setVisibility(View.VISIBLE);
                    for (Bill bill : billList) {
                        if (bill.getBillerName().equals(billerName)) {
                            if (!bill.getPaymentsRemaining().equals("1000")) {
                                bill.setPaymentsRemaining(String.valueOf(Integer.parseInt(bill.getPaymentsRemaining()) + 1));
                            }
                            ArrayList<Payments> find = new ArrayList<>();
                            for (Payments pay : paymentInfo.getPayments()) {
                                if (pay.getBillerName().equals(bill.getBillerName())) {
                                    find.add(pay);
                                }
                            }
                            for (Payments payment : find) {
                                if (payment.getPaymentId() == (paymentId)) {
                                    payment.setPaid(false);
                                    payment.setDatePaid(0);
                                    int highest = 0;
                                    for (Payments pay : paymentInfo.getPayments()) {
                                        if (pay.getBillerName().equals(bill.getBillerName()) && pay.isPaid() && pay.getDatePaid() > highest) {
                                            highest = pay.getDatePaid();
                                        }
                                    }
                                    bill.setDateLastPaid(highest);
                                    scheduleNotifications(payment);
                                    if (thisUser != null) {
                                        SaveUserData save = new SaveUserData();
                                        save.saveUserData(PayBill.this);
                                    }
                                    payBill.setBackground(new ColorDrawable(Color.TRANSPARENT));
                                    Intent home = new Intent(mContext, MainActivity2.class);
                                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(home);
                                }
                            }
                            ArrayList<Integer> dates = new ArrayList<>();
                            for (Payments pay : find) {
                                dates.add(pay.getDatePaid());
                            }
                            int lastPaid = Collections.max(dates);
                            bill.setDateLastPaid(lastPaid);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(uid).set(thisUser, SetOptions.merge());
                            bm.savePayments();
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialog.dismiss());

                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
            }
        });

        paymentDueDate.setOnClickListener(view -> getDateFromUser(paymentDueDate));
    }

    public void startPay(String dueDate, String billerName, String amountDue, boolean isPaid, int todaysDate) {
        pb.setVisibility(View.VISIBLE);

        Intent pay = new Intent(PayBill.this, PayBill.class);
        pay.putExtra("Due Date", dueDate);
        pay.putExtra("Biller Name", billerName);
        pay.putExtra("Amount Due", amountDue);
        pay.putExtra("Is Paid", isPaid);
        pay.putExtra("Payment Id", paymentId);
        pay.putExtra("Current Date", todaysDate);
        startActivity(pay);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        recreate();
        startPayBill();
    }

    public void refreshDate() {

        String billerName1 = tvBillerName.getText().toString();

        if (bil.isRecurring()) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PayBill.this);
            builder.setCancelable(true);
            builder.setTitle(getString(R.string.changeDueDate));
            builder.setMessage(getString(R.string.changeAllOccurrences));
            builder.setPositiveButton(getString(R.string.changeAll), (dialogInterface, i) -> {
                pb.setVisibility(View.VISIBLE);
                Bill newBill = bil;
                newBill.setDayDue(newDueDate);
                thisUser.getBills().remove(bil);
                thisUser.getBills().add(newBill);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(uid).set(thisUser, SetOptions.merge());
                for (Payments pay: paymentInfo.getPayments()) {
                    if (pay.getBillerName().equals(billerName1) && !pay.isPaid()) {
                        pay.setPaymentDate(newDueDate);
                    }
                }
                if (thisUser != null) {
                    SaveUserData save = new SaveUserData();
                    save.saveUserData(PayBill.this);
                }
                bm.savePayments();
                pb.setVisibility(View.GONE);
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
            });
            builder.setNeutralButton(getString(R.string.justThisOne), (dialogInterface, i) -> {
                pb.setVisibility(View.VISIBLE);

                for (Payments payments : paymentInfo.getPayments()) {
                    if (payments.getPaymentId() == paymentId) {
                        payments.setPaymentDate(newDueDate);
                        payments.setDateChanged(true);
                        bm.savePayments();
                        bm.refreshPayments(df.convertIntDateToLocalDate(newDueDate));
                        if (thisUser != null) {
                            SaveUserData save = new SaveUserData();
                            save.saveUserData(PayBill.this);
                        }
                        break;
                    }
                }
                pb.setVisibility(View.GONE);
            });
            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            pb.setVisibility(View.VISIBLE);
            bil.setDayDue(newDueDate);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).set(thisUser, SetOptions.merge());
            for (Payments pay: paymentInfo.getPayments()) {
                if (pay.getPaymentId() == paymentId) {
                    pay.setDateChanged(true);
                    pay.setPaymentDate(newDueDate);
                    bm.savePayments();
                }
            }
            pb.setVisibility(View.GONE);
        }
        pay.setPaymentDate(newDueDate);
        bil.setBillerName(billerName1);
    }

    public void getDateFromUser(TextView dueDate) {

        LocalDate local = df.convertIntDateToLocalDate(pay.getPaymentDate());
        int day = local.getDayOfMonth();
        int year = local.getYear();
        int month = local.getMonthValue();

        DatePickerDialog datePicker;
        datePicker = new DatePickerDialog(PayBill.this, R.style.MyDatePickerStyle, (datePicker1, i, i1, i2) -> {
            int fixMonth = i1 + 1;
            LocalDate selected = LocalDate.of(i, fixMonth, i2);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            dueDate.setText(formatter.format(selected));
            newDueDate = df.calcDateValue(selected);
            refreshDate();
        }, year, month - 1, day);
        datePicker.setTitle(getString(R.string.selectDate));
        datePicker.show();
    }

    public void scheduleNotifications(Payments payment) {

        DateFormatter df = new DateFormatter();

        long currentDate = df.currentDateLong();
        long today = df.convertIntDateToLong(payment.getPaymentDate(), 8, 0);
        long tomorrow = df.convertIntDateToLong(payment.getPaymentDate() - 1, 8, 0);
        long threeDay = df.convertIntDateToLong(payment.getPaymentDate() - 3, 8, 0);
        String amount = fn.addSymbol(payment.getPaymentAmount());
        String billerName = payment.getBillerName();
        int paymentId = payment.getPaymentId();

        if (today >= currentDate) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", getString(R.string.billDue));
            intent.putExtra("message", getString(R.string.yourBillFor) + " " + amount + " " + getString(R.string.at) + " " + billerName + " " + getString(R.string.isDueToday));
            intent.putExtra("channel id", channelId);
            intent.putExtra("notification id", paymentId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, paymentId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            intent.putExtra("pi", pendingIntent);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, today, pendingIntent);
        }

        if (tomorrow >= currentDate + 1) {
            Intent intent1 = new Intent(this, NotificationReceiver.class);
            intent1.putExtra("title", getString(R.string.billDue));
            intent1.putExtra("message", getString(R.string.yourBillFor) + " " + amount + " " + getString(R.string.at) + " " + billerName + " " + getString(R.string.isDueTomorrow));
            intent1.putExtra("channel id", channelId);
            intent1.putExtra("notification id", paymentId + 1);
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, paymentId + 1, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            intent1.putExtra("pi", pendingIntent1);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow, pendingIntent1);
        }

        if (threeDay >= currentDate + 3) {
            Intent intent2 = new Intent(this, NotificationReceiver.class);
            intent2.putExtra("title", getString(R.string.billDue));
            intent2.putExtra("message", getString(R.string.yourBillFor) + " " + amount + " " + getString(R.string.at) + " " + billerName + " " + getString(R.string.isDueInThreeDays));
            intent2.putExtra("channel id", channelId);
            intent2.putExtra("notification id", paymentId + 2);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, paymentId + 2, intent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            intent2.putExtra("pi", pendingIntent2);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, threeDay, pendingIntent2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}