package com.example.billstracker.popup_classes;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.example.billstracker.activities.MainActivity2.makePayment;
import static com.example.billstracker.tools.DataTools.changePaymentDueDate;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.tools.DataTools;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MoneyFormatterWatcher;
import com.example.billstracker.tools.Repo;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

public class PaymentConfirm extends Dialog {

    public static double paymentAmount;
    public static View.OnClickListener confirmListener;
    public static View.OnClickListener closeListener;
    public static View.OnClickListener perimeterListener;
    public static long newPaymentDate;

    public void setConfirmListener(View.OnClickListener confirmListener) {
        PaymentConfirm.confirmListener = confirmListener;
    }
    public void setCloseListener(View.OnClickListener closeListener) {
        PaymentConfirm.closeListener = closeListener;
    }
    public void setPerimeterListener (View.OnClickListener perimeterListener) {
        PaymentConfirm.perimeterListener = perimeterListener;
    }
    public Dialog getDialog () {
        if (this.confirm != null) {
            return this.getDialog();
        }
        else {
            return null;
        }
    }
    final LinearLayout paymentDetails;
    final LinearLayout paymentConfirmation;
    final LinearLayout paymentsList;
    final LinearLayout totalDueBox;
    final LinearLayout balanceForwardBox;
    final LinearLayout otherAmountBox;
    final TextView payBalanceForward;
    final TextView payTotalDue;
    final TextView paymentDate;
    final TextView paymentId;
    final TextView paymentDueDate;
    final TextView changeAmountDue;
    final Button submit;
    final Button cancel;
    final Button confirmPayment;
    final Button editPayment;
    final View confirm;
    final EditText payOtherAmount;
    double finalBalanceForward;
    double balanceForward;
    final int[] selection;
    final ViewGroup main;


    public PaymentConfirm(Activity activity) {
        super(activity);

        main = activity.findViewById(android.R.id.content);
        confirm = View.inflate(activity, R.layout.payment_confirm, null);
        balanceForwardBox = confirm.findViewById(R.id.balanceForwardBox);
        paymentDetails = confirm.findViewById(R.id.paymentDetails);
        paymentConfirmation = confirm.findViewById(R.id.paymentConfirmation);
        paymentsList = confirm.findViewById(R.id.paymentsList);
        confirmPayment = confirm.findViewById(R.id.confirmPayment);
        changeAmountDue = confirm.findViewById(R.id.changeAmountDue);
        editPayment = confirm.findViewById(R.id.editPayment);
        totalDueBox = confirm.findViewById(R.id.totalDueBox);
        otherAmountBox = confirm.findViewById(R.id.otherAmountBox);
        paymentDueDate = confirm.findViewById(R.id.paymentDueDate);
        payBalanceForward = confirm.findViewById(R.id.payBalanceForward);
        payTotalDue = confirm.findViewById(R.id.payTotalDue);
        paymentDate = confirm.findViewById(R.id.paymentDate);
        payOtherAmount = confirm.findViewById(R.id.payOtherAmount);
        paymentId = confirm.findViewById(R.id.paymentId);
        submit = confirm.findViewById(R.id.submitPayment);
        cancel = confirm.findViewById(R.id.cancelPayment);
        selection = new int[]{0};
        paymentDetails.setVisibility(View.VISIBLE);
        paymentConfirmation.setVisibility(View.GONE);
        confirm.setFocusable(true);

        //Tools.setupUI(activity, confirm);

        calculateBalances(activity);

        confirm.findViewById(R.id.dialog_parent).setOnClickListener(v -> {
            dismissDialog();
            if (perimeterListener != null) {
                perimeterListener.onClick(v);
            }
        });

        balanceForwardBox.setOnClickListener(v -> {
            InputMethodManager mgr = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(payOtherAmount.getWindowToken(), 0);
            balanceForwardBox.requestFocus();
            balanceForwardBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke_blue, activity.getTheme()));
            totalDueBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
            otherAmountBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
            selection[0] = 0;
            paymentAmount = finalBalanceForward;
        });

        double finalBalanceForward1 = balanceForward;
        changeAmountDue.setOnClickListener(v -> {
            if (DataTools.getBill(makePayment.getBillerName()).getPaymentsRemaining() == 1) {
                CustomDialog cd = new CustomDialog(activity, activity.getString(R.string.change_amount_due), activity.getString(R.string.pleaseEnterYourPaymentAmount), activity.getString(R.string.update),
                        activity.getString(R.string.cancel), null);
                cd.setEditText(activity.getString(R.string.payment_amount), String.format(Locale.getDefault(), "  %s", FixNumber.addSymbol(String.valueOf(makePayment.getPaymentAmount()))), null);
                cd.setTextWatcher(new MoneyFormatterWatcher(cd.getEditText()));
                cd.isMoneyInput(true);
                cd.setPositiveButtonListener(v12 -> {
                    if (cd.getInput().isEmpty()) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_amount_can_t_be_blank), null);
                    }
                    else if (FixNumber.makeDouble(cd.getInput()) <= 0) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_amount_must_be_greater_than_zero), null);
                    }
                    else {
                        double newAmountDue = FixNumber.makeDouble(cd.getInput());
                        for (Payment payment : Repo.getInstance().getPayments()) {
                            if (payment.getPaymentId() == (makePayment.getPaymentId())) {
                                payment.setPaid(false);
                                payment.setPaymentAmount(newAmountDue);
                                payment.setDateChanged(true);
                                makePayment = payment;
                                paymentAmount = makePayment.getPaymentAmount() - makePayment.getPartialPayment() + finalBalanceForward;
                                TextTools.changeMoneyTextValue(changeAmountDue, makePayment.getPaymentAmount() - makePayment.getPartialPayment(), isSuccessful -> {});
                                TextTools.changeMoneyTextValue(payTotalDue, makePayment.getPaymentAmount() + finalBalanceForward1 - makePayment.getPartialPayment(), isSuccessful -> {});
                                calculateBalances(activity);
                                Repo.getInstance().save(activity);
                                break;
                            }
                        }
                        cd.dismissDialog();
                    }
                });
                cd.setNegativeButtonListener(v1 -> cd.dismissDialog());
                cd.setPerimeterListener(v14 -> cd.dismissDialog());
            }
            else {
                CustomDialog cd = new CustomDialog(activity, activity.getString(R.string.change_amount_due), activity.getString(R.string.pleaseEnterYourPaymentAmount), activity.getString(R.string.updateAllFutureBills),
                        activity.getString(R.string.cancel), activity.getString(R.string.justThisOccurence));
                cd.setEditText(activity.getString(R.string.payment_amount), String.format(Locale.getDefault(), "  %s", FixNumber.addSymbol(String.valueOf(makePayment.getPaymentAmount()))), null);
                cd.setTextWatcher(new MoneyFormatterWatcher(cd.getEditText()));
                cd.isMoneyInput(true);
                cd.setPositiveButtonListener(v12 -> {
                    double newAmountDue = FixNumber.makeDouble(cd.getInput());
                    if (cd.getInput().isEmpty()) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_amount_can_t_be_blank), null);
                    } else if (FixNumber.makeDouble(cd.getInput()) <= 0) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_amount_must_be_greater_than_zero), null);
                    } else {
                        for (Bill bill : Repo.getInstance().getBills()) {
                            if (bill.getBillerName().equals(makePayment.getBillerName())) {
                                bill.setAmountDue(newAmountDue);
                                break;
                            }
                        }
                        for (Payment payment : Repo.getInstance().getPayments()) {
                            if (!payment.isPaid() && payment.getBillerName().equals(makePayment.getBillerName())) {
                                payment.setPaymentAmount(newAmountDue);
                                makePayment.setPaymentAmount(newAmountDue);
                            }
                        }
                        paymentAmount = makePayment.getPaymentAmount() + finalBalanceForward - makePayment.getPartialPayment();
                        TextTools.changeMoneyTextValue(changeAmountDue, makePayment.getPaymentAmount() - makePayment.getPartialPayment(), isSuccessful -> {});
                        TextTools.changeMoneyTextValue(payTotalDue, makePayment.getPaymentAmount() + finalBalanceForward1 - makePayment.getPartialPayment(), isSuccessful -> {});
                        calculateBalances(activity);
                        Repo.getInstance().save(activity);
                        cd.dismissDialog();
                    }
                });
                cd.setNeutralButtonListener(v13 -> {
                    if (cd.getInput().isEmpty()) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_amount_can_t_be_blank), null);
                    } else if (FixNumber.makeDouble(cd.getInput()) <= 0) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_amount_must_be_greater_than_zero), null);
                    } else {
                        double newAmountDue = FixNumber.makeDouble(cd.getInput());
                        for (Payment payment : Repo.getInstance().getPayments()) {
                            if (payment.getPaymentId() == (makePayment.getPaymentId())) {
                                payment.setPaymentAmount(newAmountDue);
                                payment.setDateChanged(true);
                                makePayment = payment;
                                paymentAmount = makePayment.getPaymentAmount() - makePayment.getPartialPayment() + finalBalanceForward;
                                TextTools.changeMoneyTextValue(changeAmountDue, makePayment.getPaymentAmount() - makePayment.getPartialPayment(), isSuccessful -> {});
                                TextTools.changeMoneyTextValue(payTotalDue, makePayment.getPaymentAmount() + finalBalanceForward1 - makePayment.getPartialPayment(), isSuccessful -> {});
                                calculateBalances(activity);
                                Repo.getInstance().save(activity);
                                break;
                            }
                        }
                        cd.dismissDialog();
                    }
                });
                cd.setNegativeButtonListener(v1 -> cd.dismissDialog());
                cd.setPerimeterListener(v14 -> cd.dismissDialog());
            }
        });

        totalDueBox.setOnClickListener(v -> {
            InputMethodManager mgr = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(payOtherAmount.getWindowToken(), 0);
            totalDueBox.requestFocus();
            balanceForwardBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
            totalDueBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke_blue, activity.getTheme()));
            otherAmountBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
            selection[0] = 1;
            paymentAmount = (makePayment.getPaymentAmount() - makePayment.getPartialPayment()) + finalBalanceForward;
        });

        View.OnClickListener otherAmountListener = view -> {
            balanceForwardBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
            totalDueBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
            otherAmountBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke_blue, activity.getTheme()));
            selection[0] = 2;
            paymentAmount = FixNumber.makeDouble(payOtherAmount.getText().toString());
            payOtherAmount.requestFocus();
            Tools.showKeyboard(payOtherAmount);
        };
        otherAmountBox.setOnClickListener(otherAmountListener);
        payOtherAmount.setOnFocusChangeListener((view, b) -> {
            if (b) {
                balanceForwardBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
                totalDueBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
                otherAmountBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke_blue, activity.getTheme()));
                selection[0] = 2;
                paymentAmount = FixNumber.makeDouble(payOtherAmount.getText().toString());
            }
        });

                paymentDate.setOnClickListener(v -> {
                    FragmentManager ft = ((FragmentActivity) activity).getSupportFragmentManager();
                    DatePicker dp = DateFormat.getPaymentDateFromUser(ft, DateFormat.makeLong(paymentDate.getText().toString()), activity.getString(R.string.when_did_you_pay_this_bill));
                    dp.setListener(v12 -> {
                        if (DatePicker.selection != null) {
                            newPaymentDate = DateFormat.makeLong(DatePicker.selection);
                            TextTools.updateText(paymentDate, DateFormat.makeDateString(DatePicker.selection));
                        } else {
                            dp.dismiss();
                            Notify.createPopup(activity, activity.getString(R.string.anErrorHasOccurred), null);
                        }
                    });
                });

        payOtherAmount.addTextChangedListener(new MoneyFormatterWatcher(payOtherAmount));

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        submit.setOnClickListener(v -> {
            paymentsList.removeAllViews();
            paymentsList.invalidate();
            if (selection[0] == 0) {
                paymentAmount = finalBalanceForward;
            }
            else if (selection[0] == 1) {
                paymentAmount = (makePayment.getPaymentAmount() - makePayment.getPartialPayment()) + finalBalanceForward;
            }
            else {
                paymentAmount = FixNumber.makeDouble(payOtherAmount.getText().toString());
            }
            if (paymentAmount <= 0) {
                Notify.createPopup(activity, activity.getString(R.string.payment_amount_must_be_greater_than_zero), submit);
            }
            paymentDetails.setVisibility(View.GONE);
            paymentConfirmation.setVisibility(View.VISIBLE);
            Repo.getInstance().getPayments().sort(Comparator.comparing(Payment::getDueDate));
            while (paymentAmount > 0) {
                for (Payment pays : Repo.getInstance().getPayments()) {
                    if (pays.getBillerName().equals(makePayment.getBillerName()) && !pays.isPaid() && pays.getPaymentNumber() <= makePayment.getPaymentNumber()) {
                        View tile = View.inflate(getContext(), R.layout.payment_tile, null);
                        TextView billerName = tile.findViewById(R.id.confirmBillerName), paymentNumber = tile.findViewById(R.id.confirmPaymentNumber), dueDate = tile.findViewById(R.id.confirmDueDate),
                                paidDate = tile.findViewById(R.id.confirmPaidDate), paymentAmounts1 = tile.findViewById(R.id.confirmPaymentAmount), amountRemaining = tile.findViewById(R.id.confirmAmountRemaining);
                        billerName.setText(pays.getBillerName());
                        paymentNumber.setText(String.format(Locale.getDefault(), "%s %d", activity.getString(R.string.payment_number), pays.getPaymentNumber()));
                        dueDate.setText(String.format(Locale.getDefault(), "%s: %s", activity.getString(R.string.due), DateFormat.makeDateString(pays.getDueDate())));
                        paidDate.setText(String.format(Locale.getDefault(), "%s: %s", activity.getString(R.string.paid), DateFormat.makeDateString(newPaymentDate)));
                        if (paymentAmount < pays.getPaymentAmount() - pays.getPartialPayment()) {
                            paymentAmounts1.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(paymentAmount))));
                            amountRemaining.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf((pays.getPaymentAmount() - pays.getPartialPayment()) - paymentAmount))));
                            if (paymentAmount > 0) {
                                paymentsList.addView(tile);
                            }
                            paymentAmount = 0;
                            break;
                        } else {
                            paymentAmounts1.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(pays.getPaymentAmount() - pays.getPartialPayment()))));
                            amountRemaining.setText(FixNumber.addSymbol(FixNumber.makeDouble("0")));
                            if (paymentAmount > 0 && pays.getPaymentAmount() - pays.getPartialPayment() > 0) {
                                paymentsList.addView(tile);
                            }
                            paymentAmount = paymentAmount - (pays.getPaymentAmount() - pays.getPartialPayment());
                        }
                    }
                }
            }
        });

        editPayment.setOnClickListener(v -> {
            paymentDetails.setVisibility(View.VISIBLE);
            paymentConfirmation.setVisibility(View.GONE);
            if (selection[0] == 0) {
                paymentAmount = finalBalanceForward;
            }
            else if (selection[0] == 1) {
                paymentAmount = (makePayment.getPaymentAmount() - makePayment.getPartialPayment()) + finalBalanceForward;
            }
            else {
                paymentAmount = FixNumber.makeDouble(payOtherAmount.getText().toString());
            }
        });

        paymentDueDate.setOnClickListener(view -> {
            FragmentManager ft = ((FragmentActivity) activity).getSupportFragmentManager();
            DatePicker dp = DateFormat.getPaymentDateFromUser(ft, DateFormat.makeLong(paymentDueDate.getText().toString()), activity.getString(R.string.when_is_this_payment_due));
            dp.setListener(v12 -> {
                if (DatePicker.selection != null) {
                    LocalDate chosenDate = DatePicker.selection;
                    Bill bill = DataTools.getBill(makePayment.getBillerName());
                    if (DateFormat.makeLocalDate(bill.getDueDate()).isAfter(chosenDate)) {
                        Notify.createPopup(activity, activity.getString(R.string.payment_due_date_cannot_be_before_the_biller_due_date_of) + DateFormat.makeDateString(bill.getDueDate()), null);
                    }
                    else {
                        dp.dismiss();
                        if (bill.getPaymentsRemaining() > 1) {
                            CustomDialog cd = new CustomDialog(activity, activity.getString(R.string.change_all_payments), activity.getString(R.string.would_you_like_to_apply_this_new_due_date_to_all_occurrences_of_this_bill),
                                    activity.getString(R.string.change_all), activity.getString(R.string.cancel), activity.getString(R.string.just_this_one));

                            cd.setPositiveButtonListener(v15 -> {
                                if (makePayment == null) {
                                    activity.recreate();
                                } else {
                                    changePaymentDueDate(makePayment, DateFormat.makeLong(chosenDate), true, isSuccessful -> {
                                        if (isSuccessful) {
                                            TextTools.updateText(paymentDueDate, DateFormat.makeDateString(makePayment.getDueDate()));
                                            cd.dismissDialog();
                                        }
                                        else {
                                            Notify.createPopup(activity, activity.getString(R.string.anErrorHasOccurred), null);
                                            cd.dismissDialog();
                                        }
                                    });
                                }
                            });
                            cd.setNegativeButtonListener(v16 -> cd.dismissDialog());
                            cd.setNeutralButtonListener(v17 -> {
                                if (makePayment == null) {
                                    activity.recreate();
                                } else {
                                    changePaymentDueDate(makePayment, DateFormat.makeLong(chosenDate), false, isSuccessful -> {
                                        if (isSuccessful) {
                                            TextTools.updateText(paymentDueDate, DateFormat.makeDateString(makePayment.getDueDate()));
                                            cd.dismissDialog();
                                        } else {
                                            Notify.createPopup(activity, activity.getString(R.string.anErrorHasOccurred), null);
                                            dp.dismiss();
                                        }
                                    });
                                }
                            });
                        } else {
                            if (makePayment == null) {
                                activity.recreate();
                            } else {
                                changePaymentDueDate(makePayment, DateFormat.makeLong(chosenDate), false, isSuccessful -> {
                                    if (isSuccessful) {
                                        TextTools.updateText(paymentDueDate, DateFormat.makeDateString(makePayment.getDueDate()));
                                    } else {
                                        Notify.createPopup(activity, activity.getString(R.string.anErrorHasOccurred), null);
                                    }
                                });
                            }
                        }
                    }
                }
            });
        });

        confirmPayment.setOnClickListener(v -> {
            if (selection[0] == 0) {
                paymentAmount = finalBalanceForward;
            }
            else if (selection[0] == 1) {
                paymentAmount = (makePayment.getPaymentAmount() - makePayment.getPartialPayment()) + finalBalanceForward;
            }
            else {
                paymentAmount = FixNumber.makeDouble(payOtherAmount.getText().toString());
            }
            InputMethodManager mgr = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(payOtherAmount.getWindowToken(), 0);
            confirmListener.onClick(v);
            this.cancel();
        });
        cancel.setOnClickListener(v -> {
            closeListener.onClick(v);
            this.cancel();
        });

        main.addView(confirm);
    }

    public void calculateBalances(Activity activity) {

        if (makePayment != null) {
            balanceForward = 0;
            paymentAmount = 0;
            payOtherAmount.setText(FixNumber.addSymbol("0"));
            newPaymentDate = DateFormat.currentDateAsLong();
            paymentDate.setText(DateFormat.makeDateString(newPaymentDate));
            paymentDueDate.setText(DateFormat.makeDateString(makePayment.getDueDate()));
            paymentId.setText(String.format(Locale.getDefault(),"%s: %d", activity.getString(R.string.payment_id), makePayment.getPaymentId()));

            for (Payment payment : Repo.getInstance().getPayments()) {
                if (payment.getBillerName().equals(makePayment.getBillerName()) && !payment.isPaid() && payment.getDueDate() < makePayment.getDueDate()) {
                    balanceForward = balanceForward + (payment.getPaymentAmount() - payment.getPartialPayment());
                }
            }
            payBalanceForward.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(balanceForward))));
            payTotalDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(makePayment.getPaymentAmount() + balanceForward - makePayment.getPartialPayment()))));
            paymentAmount = makePayment.getPaymentAmount() + balanceForward - makePayment.getPartialPayment();
            changeAmountDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(makePayment.getPaymentAmount()))));

            if (balanceForward == 0) {
                balanceForwardBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
                totalDueBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke_blue, activity.getTheme()));
                otherAmountBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
                selection[0] = 1;
            } else {
                balanceForwardBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke_blue, activity.getTheme()));
                totalDueBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
                otherAmountBox.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));
                paymentAmount = balanceForward;
                selection[0] = 0;
            }

            finalBalanceForward = balanceForward;
        }
    }
    public void dismissDialog () {
        if (confirm != null) {
            ViewGroup parent = (ViewGroup) confirm.getParent();
            if (payOtherAmount != null) {
                TextTools.closeSoftInput(payOtherAmount);
            }
            if (parent != null) {
                parent.removeView(confirm);
            }
        }
    }
}