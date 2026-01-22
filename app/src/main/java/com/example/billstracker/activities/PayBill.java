package com.example.billstracker.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.billstracker.activities.MainActivity2.makePayment;
import static com.example.billstracker.activities.MainActivity2.startAddBiller;
import static com.example.billstracker.popup_classes.PaymentConfirm.newPaymentDate;
import static com.example.billstracker.popup_classes.PaymentConfirm.paymentAmount;
import static com.example.billstracker.tools.DataTools.changePaymentDueDate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.DatePicker;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.popup_classes.PaymentConfirm;
import com.example.billstracker.tools.BillerManager;
import com.example.billstracker.tools.DataTools;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MoneyTextWatcher;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.NotificationManager;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;

import java.util.Locale;

public class PayBill extends BaseActivity {

    final Context mContext = this;
    TextView paymentDueDate;
    TextView paymentAmountDue;
    TextView amountDueLabel;
    TextView webAddress;
    TextView tvBillerName;
    TextView paymentDatePaid;
    TextView displayPaymentsRemaining;
    TextView displayPartialPayment;
    TextView balForward;
    TextView dueThisPeriod;
    TextView remainingDueLabel;
    ImageView paidIcon;
    com.google.android.material.imageview.ShapeableImageView payBillIcon;
    Button payButton, editBiller, viewPayments;
    int paymentId;
    Payment pay;
    Bill bil;
    LinearLayout datePaidLayout;
    ConstraintLayout pb;
    LinearLayout paymentsRemainingLayout;
    LinearLayout partialPaymentLayout;
    LinearLayout balForwardBox;
    LinearLayout totalAmountDueBox;
    boolean found;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_pay_bill);
        payBillIcon = findViewById(R.id.payBillIcon);
        tvBillerName = findViewById(R.id.billerName);
        payButton = findViewById(R.id.payButton);
        paymentAmountDue = findViewById(R.id.paymentAmountDue);
        totalAmountDueBox = findViewById(R.id.totalAmountDueBox);
        dueThisPeriod = findViewById(R.id.dueThisPeriod);
        editBiller = findViewById(R.id.editBillerButton);
        viewPayments = findViewById(R.id.viewPayments);
        paidIcon = findViewById(R.id.paidIcon);
        pb = findViewById(R.id.progressBar);
        amountDueLabel = findViewById(R.id.amountDueLabel);
        balForwardBox = findViewById(R.id.balForwardBox);
        balForward = findViewById(R.id.balForward);
        webAddress = findViewById(R.id.webAddress);
        paymentDueDate = findViewById(R.id.paymentDueDate1);
        paymentDatePaid = findViewById(R.id.paymentDatePaid);
        partialPaymentLayout = findViewById(R.id.partialPaymentLayout);
        displayPartialPayment = findViewById(R.id.displayPartialPayment);
        displayPaymentsRemaining = findViewById(R.id.displayPaymentsRemaining);
        paymentsRemainingLayout = findViewById(R.id.paymentsRemainingLayout2);
        remainingDueLabel = findViewById(R.id.amountDueThisPeriodLabel);
        datePaidLayout = findViewById(R.id.datePaidLayout);
        ActivityOptionsCompat.makeCustomAnimation(PayBill.this, 0, 0);
        Tools.fixProgressBarLogo(pb);

        NavController nc = new NavController();
        nc.navController(PayBill.this, PayBill.this, pb, "payBill");

        startPayBill();
    }

    public void startPayBill() {

        int paymentId = getIntent().getIntExtra("paymentId", -1);

        // Force the repository to load if it's currently empty
        if (repo.getPayments().isEmpty()) {
            repo.loadLocalData(this, (success, message) -> {
                // This callback runs once the data is actually ready
                populateUI(paymentId);
            });
        } else {
            populateUI(paymentId);
        }
    }

    private void populateUI(int paymentId) {

        pay = repo.getPaymentById(paymentId);

        if (pay == null) {
            // If still null after loading, handle navigation back
            if (isTaskRoot()) {
                startActivity(new Intent(this, MainActivity2.class));
            }
            finish();
        } else {
            bil = repo.getBillByName(pay.getBillerName());
            if (bil != null) {
                if (bil.getCategory() == 0 || bil.getCategory() == 5 || bil.getCategory() == 6) {
                    paymentsRemainingLayout.setVisibility(View.VISIBLE);
                    if (bil.getPaymentsRemaining() == 0) {
                        displayPaymentsRemaining.setText(getString(R.string.paid_in_full));
                    } else {
                        displayPaymentsRemaining.setText(String.valueOf(bil.getPaymentsRemaining()));
                    }
                }
            }

            payBillIcon.setImageDrawable(null);
            Tools.loadIcon(payBillIcon, bil.getCategory(), bil.getIcon());
            paymentDueDate.setText(DateFormat.makeDateString(pay.getDueDate()));
            tvBillerName.setText(pay.getBillerName());
            paymentAmountDue.setText(FixNumber.addSymbol(String.valueOf(pay.getPaymentAmount())));
            dueThisPeriod.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(pay.getPaymentAmount() - pay.getPartialPayment()))));

            if (pay.isPaid()) {
                datePaidLayout.setVisibility(View.VISIBLE);
                paymentDatePaid.setText(DateFormat.makeDateString(pay.getDatePaid()));
                amountDueLabel.setText(getString(R.string.amount_paid));
                payButton.setText(getString(R.string.unmarkAsPaid));
            } else {
                datePaidLayout.setVisibility(View.GONE);
                payButton.setText(getString(R.string.markAsPaid));
                paidIcon.setImageDrawable(null);
            }
            if (pay.getPartialPayment() > 0 && !pay.isPaid()) {
                partialPaymentLayout.setVisibility(View.VISIBLE);
                displayPartialPayment.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(pay.getPartialPayment()))));
                remainingDueLabel.setText(getString(R.string.remaining_this_period));
                amountDueLabel.setText(getString(R.string.amount_remaining));
                paymentAmountDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(pay.getPaymentAmount() - pay.getPartialPayment()))));
            } else {
                remainingDueLabel.setText(getString(R.string.due_this_period));
            }

            double balanceForward = 0;
            Payment payment = repo.getPaymentByBillerName(pay.getBillerName());
            if (payment != null) {
                if (payment.getDueDate() < pay.getDueDate() && !payment.isPaid()) {
                    balanceForward = balanceForward + (payment.getPaymentAmount() - payment.getPartialPayment());
                }
            }

            if (balanceForward > 0) {
                totalAmountDueBox.setVisibility(View.VISIBLE);
                balForwardBox.setVisibility(View.VISIBLE);
                balForward.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(balanceForward))));
                paymentAmountDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(pay.getPaymentAmount() - pay.getPartialPayment() + balanceForward))));
            } else {
                totalAmountDueBox.setVisibility(View.GONE);
                balForwardBox.setVisibility(View.GONE);
            }

            webAddress.setOnClickListener(view -> {
                if (!bil.getWebsite().startsWith("http://") && !bil.getWebsite().startsWith("https://")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + bil.getWebsite())));
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(bil.getWebsite())));
                }
            });

            displayPartialPayment.setOnClickListener(v -> {
                CustomDialog cd = new CustomDialog(PayBill.this, getString(R.string.changePartialPayment), getString(R.string.would_you_like_to_change_your_partial_payment_or_remove_it), getString(R.string.changeAmount),
                        getString(R.string.cancel), getString(R.string.remove));
                cd.setEditText(getString(R.string.partial_payment_amount), String.format(Locale.getDefault(), "  %s", FixNumber.addSymbol(String.valueOf(pay.getPartialPayment()))), null);
                cd.isMoneyInput(true);
                cd.setTextWatcher(new MoneyTextWatcher(cd.getEditText()));
                cd.setPositiveButtonListener(v2 -> {
                    repo.editPayment(pay.getPaymentId(), PayBill.this)
                            .setPartialPayment(FixNumber.makeDouble(cd.getInput()))
                            .setDateChanged(true)
                            .save((wasSuccessful, message) -> {
                                if (wasSuccessful) {
                                    pay = repo.getPaymentById(pay.getPaymentId());
                                    cd.dismissDialog();
                                    if (pay.getPartialPayment() > 0) {
                                        TextTools.changeMoneyTextValue(displayPartialPayment, pay.getPartialPayment(), isSuccessful -> {
                                        });
                                    } else {
                                        TextTools.changeMoneyTextValue(displayPartialPayment, 0, isSuccessful -> Tools.fadeOutAndRemove(partialPaymentLayout, isFinished -> {
                                        }));
                                    }
                                } else {
                                    Notify.createPopup(PayBill.this, "Error: " + message, null);
                                }
                            });
                });
                cd.setNeutralButtonListener(v3 -> {
                    repo.editPayment(pay.getPaymentId(), PayBill.this)
                            .setPartialPayment(0)
                            .setDateChanged(false)
                            .save((isSuccessful, message) -> {
                                pay = repo.getPaymentById(pay.getPaymentId());
                                cd.dismissDialog();
                                TextTools.changeMoneyTextValue(displayPartialPayment, 0, isSuccessful2 -> Tools.fadeOutAndRemove(partialPaymentLayout, isFinished -> {
                                }));
                            });
                });
                cd.show();
            });

            paymentDatePaid.setOnClickListener(v -> {
                DatePicker dp = DateFormat.getPaymentDateFromUser(getSupportFragmentManager(), pay.getDatePaid(), getString(R.string.when_did_you_pay_this_bill));
                dp.setListener(v12 -> {
                    if (DatePicker.selection != null) {
                        pay.setDatePaid(DateFormat.makeLong(DatePicker.selection));
                        repo.editPayment(pay.getPaymentId(), PayBill.this)
                                .setDatePaid(pay.getDatePaid())
                                .save((isSuccessful, message) -> {
                                    TextTools.changeMoneyTextValue(displayPartialPayment, 0, isSuccessful2 -> Tools.fadeOutAndRemove(partialPaymentLayout, isFinished -> {
                                    }));
                                    dp.dismiss();
                                    TextTools.updateText(paymentDatePaid, DateFormat.makeDateString(DatePicker.selection));
                                });
                    }
                });
            });

            dueThisPeriod.setOnClickListener(view -> {
                if (DataTools.getBill(pay.getBillerName()).getPaymentsRemaining() > 1) {
                    CustomDialog cd = new CustomDialog(PayBill.this, getString(R.string.change_amount_due), getString(R.string.pleaseEnterYourPaymentAmount), getString(R.string.updateAllFutureBills),
                            getString(R.string.cancel), getString(R.string.justThisOccurence));
                    cd.setEditText(getString(R.string.payment_amount), String.format(Locale.getDefault(), "  %s", FixNumber.addSymbol(String.valueOf(pay.getPaymentAmount()))), null);
                    cd.isMoneyInput(true);
                    cd.setTextWatcher(new MoneyTextWatcher(cd.getEditText()));
                    cd.setPositiveButtonListener(v12 -> {
                        if (cd.getInput().isEmpty()) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_amount_can_t_be_blank), null);
                        } else if (FixNumber.makeDouble(cd.getInput()) <= 0) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_amount_must_be_greater_than_zero), null);
                        } else {
                            pb.setVisibility(View.VISIBLE);
                            double newAmountDue = FixNumber.makeDouble(cd.getInput());
                            Bill.Builder bill = repo.editBill(bil.getBillerName(), PayBill.this);
                            if (bill != null) {
                                bill.setAmountDue(newAmountDue);
                            }
                            repo.editPayment(pay.getPaymentId(), PayBill.this)
                                    .setPaymentAmount(newAmountDue)
                                    .save((wasSuccessful, message) -> {
                                        pay = repo.getPaymentById(pay.getPaymentId());
                                        pb.setVisibility(View.GONE);
                                        cd.dismissDialog();
                                        TextTools.changeMoneyTextValue(dueThisPeriod, pay.getPaymentAmount() - pay.getPartialPayment(), isSuccessful -> {
                                            //startPayBill();
                                        });
                                    });
                        }
                    });
                    cd.setNeutralButtonListener(v13 -> {
                        if (cd.getInput().isEmpty()) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_amount_can_t_be_blank), null);
                        } else if (FixNumber.makeDouble(cd.getInput()) <= 0) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_amount_must_be_greater_than_zero), null);
                        } else {
                            double newAmountDue = FixNumber.makeDouble(cd.getInput());
                            pb.setVisibility(View.VISIBLE);
                            if (pay != null) {
                                found = false;
                                repo.editPayment(paymentId, PayBill.this)
                                        .setPaymentAmount(newAmountDue)
                                        .setDateChanged(true)
                                        .save((wasSuccessful, message) -> {
                                            BillerManager.refreshPayments(PayBill.this);
                                            pb.setVisibility(View.GONE);
                                            cd.dismissDialog();
                                            found = true;
                                            TextTools.changeMoneyTextValue(dueThisPeriod, pay.getPaymentAmount() - pay.getPartialPayment(), isSuccessful -> {
                                            });
                                        });
                            } else {
                                pb.setVisibility(View.GONE);
                                Notify.createPopup(PayBill.this, getString(R.string.anErrorHasOccurred), null);
                            }
                        }
                    });
                    cd.show();
                } else {
                    CustomDialog cd = new CustomDialog(PayBill.this, getString(R.string.change_amount_due), getString(R.string.pleaseEnterYourPaymentAmount), "Update",
                            getString(R.string.cancel), null);
                    cd.setEditText(getString(R.string.payment_amount), String.format(Locale.getDefault(), "  %s", FixNumber.addSymbol(String.valueOf(pay.getPaymentAmount()))), null);
                    cd.isMoneyInput(true);
                    cd.setTextWatcher(new MoneyTextWatcher(cd.getEditText()));
                    cd.setPositiveButtonListener(v12 -> {
                        if (cd.getInput().isEmpty()) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_amount_can_t_be_blank), null);
                        } else if (FixNumber.makeDouble(cd.getInput()) <= 0) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_amount_must_be_greater_than_zero), null);
                        } else {
                            double newAmountDue = FixNumber.makeDouble(cd.getInput());
                            pb.setVisibility(View.VISIBLE);
                            if (pay != null) {
                                repo.editPayment(paymentId, PayBill.this)
                                        .setPaymentAmount(newAmountDue)
                                        .setDateChanged(true)
                                        .save((wasSuccessful, message) -> {
                                            BillerManager.refreshPayments(PayBill.this);
                                            pb.setVisibility(View.GONE);
                                            cd.dismissDialog();
                                            found = true;
                                            TextTools.changeMoneyTextValue(dueThisPeriod, pay.getPaymentAmount() - pay.getPartialPayment(), isSuccessful -> {
                                            });
                                        });
                            } else {
                                pb.setVisibility(View.GONE);
                                Notify.createPopup(PayBill.this, getString(R.string.anErrorHasOccurred), null);
                            }
                        }
                    });
                    cd.show();
                }

            });

            editBiller.setOnClickListener(view -> {
                pb.setVisibility(View.VISIBLE);
                startAddBiller = false;
                Intent edit = new Intent(mContext, AddBiller.class);
                edit.putExtra("billerId", bil.getBillsId());
                startActivity(edit);
            });

            viewPayments.setOnClickListener(view -> {
                pb.setVisibility(View.VISIBLE);
                startActivity(new Intent(mContext, PaymentHistory.class).putExtra("Bill Id", bil.getBillsId()));
            });

            payButton.setOnClickListener(view -> {
                if (pay != null) {
                    makePayment = pay;
                    if (!makePayment.isPaid()) {
                        PaymentConfirm pc = new PaymentConfirm(PayBill.this);
                        pc.setConfirmListener(v -> {
                            repo.sortPaymentsByDueDate();
                            while (paymentAmount > 0) {
                                for (Payment pays : repo.getPayments()) {
                                    if (pays.getBillerName().equals(pay.getBillerName()) && !pays.isPaid()) {
                                        if (paymentAmount != 0) {
                                            if (paymentAmount < pays.getPaymentAmount() - pays.getPartialPayment()) {
                                                pays.setPartialPayment(pays.getPartialPayment() + paymentAmount);
                                                pays.setDateChanged(true);
                                                pays.setDatePaid(newPaymentDate);
                                                pays.setOwner(repo.getUid(PayBill.this));
                                                paymentAmount = 0;
                                                break;
                                            } else {
                                                paymentAmount = paymentAmount - (pays.getPaymentAmount() - pays.getPartialPayment());
                                                pays.setPartialPayment(0);
                                                pays.setDateChanged(false);
                                                pays.setPaid(true);
                                                pays.setDatePaid(newPaymentDate);
                                                pays.setOwner(repo.getUid(PayBill.this));
                                                pay = pays;
                                                for (Bill bill : repo.getBills()) {
                                                    if (bill.getBillerName().equals(pay.getBillerName())) {
                                                        bill.setPaymentsRemaining(bill.getPaymentsRemaining() - 1);
                                                        bill.setDateLastPaid(newPaymentDate);
                                                        NotificationManager.scheduleNotifications(this);
                                                        Notify.createPopup(PayBill.this, getString(R.string.bill_marked_as_paid_successfully), null);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            repo.saveData(PayBill.this, (wasSuccessful, message) -> {

                            });
                            pc.dismissDialog();
                            if (getIntent().getExtras() != null) {
                                getIntent().getExtras().putInt("Payment Id", pay.getPaymentId());
                                recreate();
                            } else {
                                startActivity(new Intent(PayBill.this, MainActivity2.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK));
                            }
                        });
                        pc.setCloseListener(v -> pc.dismissDialog());
                        pc.setPerimeterListener(view1 -> pc.dismissDialog());
                        pc.show();

                    } else {

                        pb.setVisibility(View.VISIBLE);
                        repo.deletePayment(pay.getPaymentId(), PayBill.this, (wasSuccessful, message) -> {
                            pb.setVisibility(View.GONE);
                            pay.setPaid(false);
                            pay.setDatePaid(0);
                            repo.saveData(PayBill.this, (wasSuccessful1, message1) -> {
                                NotificationManager.scheduleNotifications(PayBill.this);
                                Notify.createPopup(PayBill.this, getString(R.string.bill_marked_as_unpaid_successfully), null);
                                if (getIntent().getExtras() != null) {
                                    getIntent().getExtras().putInt("Payment Id", pay.getPaymentId());
                                    recreate();
                                } else {
                                    startActivity(new Intent(PayBill.this, MainActivity2.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK));
                                }
                            });
                        });
                    }
                } else {
                    Notify.createPopup(PayBill.this, getString(R.string.anErrorHasOccurred), null);
                }
            });

            displayPaymentsRemaining.setOnClickListener(v -> {
                CustomDialog cd = new CustomDialog(PayBill.this, getString(R.string.change_payments_remaining), getString(R.string.enter_how_many_payments_are_remaining_to_update_your_biller), getString(R.string.update_biller),
                        getString(R.string.cancel), null);
                cd.setEditText(getString(R.string.payments_remaining_), String.format(Locale.getDefault(), "  %s", bil.getPaymentsRemaining()), null);
                cd.isMoneyInput(true);
                cd.setPositiveButtonListener(v12 -> {
                    if (cd.getInput().replaceAll("[^0-9]", "").isEmpty()) {
                        Notify.createPopup(PayBill.this, getString(R.string.remaining_payments_can_t_be_blank), null);
                    } else {
                        pb.setVisibility(View.VISIBLE);
                        int remaining;
                        remaining = Integer.parseInt(cd.getInput().replaceAll("[^0-9]", ""));
                        for (Bill bill : repo.getBills()) {
                            if (bill.getBillerName().equals(bil.getBillerName())) {
                                bill.setPaymentsRemaining(remaining);
                                bil.setPaymentsRemaining(remaining);
                                repo.saveData(PayBill.this, (wasSuccessful, message) -> {
                                });
                                break;
                            }
                        }
                        TextTools.updateText(displayPaymentsRemaining, String.valueOf(remaining));
                        pb.setVisibility(View.GONE);
                        cd.dismissDialog();
                    }
                });
                cd.show();
            });

            paymentDueDate.setOnClickListener(v -> {
                DatePicker dp = DateFormat.getPaymentDateFromUser(getSupportFragmentManager(), pay.getDueDate(), getString(R.string.when_is_this_bill_due));
                dp.setListener(v12 -> {
                    if (DatePicker.selection != null) {
                        dp.dismiss();
                        if (DateFormat.makeLocalDate(DataTools.getBill(pay.getBillerName()).getDueDate()).isAfter(DatePicker.selection)) {
                            Notify.createPopup(PayBill.this, getString(R.string.payment_due_date_cannot_be_before_the_biller_due_date_of) + DateFormat.makeDateString(DataTools.getBill(pay.getBillerName()).getDueDate()), null);
                        } else {
                            if (DataTools.getBill(pay.getBillerName()).getPaymentsRemaining() > 1) {
                                CustomDialog cd = new CustomDialog(PayBill.this, getString(R.string.change_all_payments), getString(R.string.would_you_like_to_apply_this_new_due_date_to_all_occurrences_of_this_bill), getString(R.string.change_all),
                                        getString(R.string.cancel), getString(R.string.just_this_one));
                                cd.setPositiveButtonListener(v15 -> changePaymentDueDate(PayBill.this, pay, DateFormat.makeLong(DatePicker.selection), true, isSuccessful -> {
                                    pay.setDueDate(DateFormat.makeLong(DatePicker.selection));
                                    TextTools.updateText(paymentDueDate, DateFormat.makeDateString(pay.getDueDate()));
                                    cd.dismissDialog();
                                }));
                                cd.setNegativeButtonListener(v16 -> cd.dismissDialog());
                                cd.setNeutralButtonListener(v17 -> changePaymentDueDate(PayBill.this, pay, DateFormat.makeLong(DatePicker.selection), false, isSuccessful -> {
                                    pay.setDueDate(DateFormat.makeLong(DatePicker.selection));
                                    TextTools.updateText(paymentDueDate, DateFormat.makeDateString(pay.getDueDate()));
                                    cd.dismissDialog();
                                }));
                                cd.show();
                            } else {
                                changePaymentDueDate(PayBill.this, pay, DateFormat.makeLong(DatePicker.selection), false, isSuccessful -> {
                                    pay.setDueDate(DateFormat.makeLong(DatePicker.selection));
                                    TextTools.updateText(paymentDueDate, DateFormat.makeDateString(pay.getDueDate()));
                                });
                            }
                        }
                    } else {
                        Notify.createPopup(PayBill.this, getString(R.string.anErrorHasOccurred), null);
                        dp.dismiss();
                    }
                });
            });
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //recreate();
        startPayBill();
    }

    @Override
    protected void onResume() {
        super.onResume();
        payBillIcon.setContentPadding(50, 50, 50, 50);
        pb.setVisibility(View.GONE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // This updates the Activity's intent with the notification's intent
        startPayBill();    // Re-run the loading logic with the new ID
    }
}