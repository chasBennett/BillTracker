package com.example.billstracker.popup_classes;

import static com.example.billstracker.activities.Spending.selectedDate;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Category;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MoneyFormatterWatcher;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;
import com.google.android.material.textfield.TextInputEditText;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;

public class AddExpense {

    public static View.OnClickListener listener;
    final Activity activity;
    ViewGroup main;
    View dialog;
    LinearLayout parent;
    TextInputEditText expenseDescription, expenseAmount;
    Spinner expenseCategory;
    TextView expenseDate, submitExpense, cancel, header;
    ArrayList<String> categories;
    public AddExpense(Activity activity, Expense expense) {
        this.activity = activity;
        setViews();
        buildAdapter();

        expenseDate.setText(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).format(selectedDate));
        expenseAmount.setText(FixNumber.addSymbol("0"));

        if (expense != null) {
            if (categories.contains(expense.getCategory())) {
                expenseCategory.setSelection(categories.indexOf(expense.getCategory()));
            } else {
                expenseCategory.setSelection(0);
            }
            expenseDescription.setText(expense.getDescription());
            expenseDate.setText(DateFormat.makeDateString(expense.getDate()));
            expenseAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(expense.getAmount())));
            header.setText(R.string.edit_expense);
        }
        expenseDate.setOnClickListener(v -> {
            FragmentManager ft = ((FragmentActivity) activity).getSupportFragmentManager();
            DatePicker dp = DateFormat.getPaymentDateFromUser(ft, DateFormat.makeLong(selectedDate), activity.getString(R.string.when_did_this_expense_occur));
            dp.setListener(v12 -> {
                if (DatePicker.selection != null) {
                    expenseDate.setText(DateFormat.makeDateString(DatePicker.selection));
                }
            });
        });
        expenseAmount.addTextChangedListener(new MoneyFormatterWatcher(expenseAmount));
        expenseAmount.setOnEditorActionListener((view, actionId, event) -> {
            if ((actionId & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_DONE) {
                Tools.hideKeyboard(activity);
            }
            return false;
        });

        submitExpense.setOnClickListener(v -> {
            if (expenseDescription.getText() == null || expenseDescription.getText().isEmpty()) {
                Notify.createPopup(activity, activity.getString(R.string.expense_description_cannot_be_blank), null);
            } else if (expenseAmount.getText() == null || expenseAmount.getText().isEmpty() || FixNumber.makeDouble(expenseAmount.getText().toString()) <= 0.0) {
                Notify.createPopup(activity, activity.getString(R.string.expense_amount_must_be_greater_than_0), null);
            } else {
                if (expense != null) {
                    for (Expense expenses : Repository.getInstance().getExpenses()) {
                        if (expenses.getId().equals(expense.getId())) {
                            expenses.setDescription(expenseDescription.getText().toString());
                            expenses.setAmount(FixNumber.makeDouble(expenseAmount.getText().toString()));
                            expenses.setCategory(categories.get(expenseCategory.getSelectedItemPosition()));
                            expenses.setDate(DateFormat.makeLong(expenseDate.getText().toString()) + 1);
                            break;
                        }
                    }
                } else {
                    Expense a = new Expense(expenseDescription.getText().toString(), categories.get(expenseCategory.getSelectedItemPosition()), DateFormat.makeLong(expenseDate.getText().toString()) + 1,
                            FixNumber.makeDouble(expenseAmount.getText().toString()), id(), Repository.getInstance().retrieveUid(activity));
                    Repository.getInstance().getExpenses().add(a);
                }
                Repository.getInstance().saveData(activity, (wasSuccessful, message) -> {
                    if (wasSuccessful) {
                        dismissDialog();
                        listener.onClick(v);
                        Notify.createPopup(activity, activity.getString(R.string.expense_was_saved_successfully), null);
                    }
                    else {
                        Notify.createPopup(activity, message, null);
                    }
                });
            }
        });

        cancel.setOnClickListener(v -> {
            dismissDialog();
            listener.onClick(v);
        });
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        parent.setOnClickListener(v -> {
            dismissDialog();
            listener.onClick(v);
        });

        main.addView(dialog);
        dialog.setElevation(10);
        main.bringChildToFront(dialog);
    }

    public void setSubmitExpenseListener(View.OnClickListener listener) {
        AddExpense.listener = listener;
    }

    public void setViews() {

        main = activity.findViewById(android.R.id.content);
        dialog = View.inflate(activity, R.layout.add_expense, null);
        parent = dialog.findViewById(R.id.addExpenseParent);
        cancel = dialog.findViewById(R.id.cancelExpense);
        header = dialog.findViewById(R.id.addExpenseHeader);
        expenseDescription = dialog.findViewById(R.id.expenseDescription);
        expenseAmount = dialog.findViewById(R.id.expenseAmount);
        expenseCategory = dialog.findViewById(R.id.expenseCategory);
        expenseDate = dialog.findViewById(R.id.expenseDate);
        submitExpense = dialog.findViewById(R.id.submitExpense);
        Tools.setupUI(activity, dialog);

    }

    public void buildAdapter() {
        categories = new ArrayList<>();

        Budget bud = Tools.getBudget(activity, selectedDate);
        if (bud != null && bud.getCategories() != null && !bud.getCategories().isEmpty()) {
            categories.clear();
            for (Category category : bud.getCategories()) {
                categories.add(category.getCategoryName());
            }
        } else {
            categories.add(activity.getString(R.string.miscellaneous));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseCategory.setAdapter(adapter);
    }

    public void dismissDialog() {
        if (main != null && dialog != null) {
            main.removeView(dialog);
        }
    }

    String id() {
        final String AB = "0123456789";
        SecureRandom rnd = new SecureRandom();
        int length = 9;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return String.valueOf(sb);
    }
}
