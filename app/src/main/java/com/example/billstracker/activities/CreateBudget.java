package com.example.billstracker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Category;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.DatePicker;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.DataTools;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MoneyFormatterWatcher;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.Watcher;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class CreateBudget extends BaseActivity {

    EditText etPayAmount, etSavingsPercentage;
    TextView savingsAmount, billsAverage, disposableIncome, budgetStartDate, budgetEndDate, createForMe, addCategory, savingsError, categoriesError, header;
    Spinner payFrequency;
    LinearLayout categoriesList;
    LinearLayout back;
    Button submit;
    int budgetId;
    LocalDate startDate, endDate;
    boolean saveError;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_create_budget);
        Tools.setupUI(CreateBudget.this, findViewById(android.R.id.content));

        budgetId = 0;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("budgetId")) {
                budgetId = bundle.getInt("budgetId");
            }
        }
        etPayAmount = findViewById(R.id.etPayAmount);
        etSavingsPercentage = findViewById(R.id.etSavingsPercentage);
        budgetStartDate = findViewById(R.id.budgetStartDate);
        budgetEndDate = findViewById(R.id.budgetEndDate);
        savingsAmount = findViewById(R.id.savingsAmount);
        billsAverage = findViewById(R.id.billsAverage);
        savingsError = findViewById(R.id.savingsError2);
        categoriesError = findViewById(R.id.categoriesError2);
        disposableIncome = findViewById(R.id.weeklyDisposableIncome);
        payFrequency = findViewById(R.id.payFrequencySpinner);
        createForMe = findViewById(R.id.createForMe);
        submit = findViewById(R.id.submitBudget);
        back = findViewById(R.id.backButton);
        categoriesList = findViewById(R.id.categoriesList);
        addCategory = findViewById(R.id.addCategory);
        header = findViewById(R.id.createBudgetHeader);

        startDate = LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1);
        endDate = startDate.plusYears(1);

        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        budgetStartDate.setOnClickListener(v -> getDateFromUser(budgetStartDate, true, DateFormat.makeLong(budgetStartDate.getText().toString())));
        budgetEndDate.setOnClickListener(v -> getDateFromUser(budgetEndDate, false, DateFormat.makeLong(budgetEndDate.getText().toString())));

        etPayAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(repo.getUser(CreateBudget.this).getIncome()))));
        etPayAmount.addTextChangedListener(new MoneyFormatterWatcher(etPayAmount));

        etPayAmount.addTextChangedListener(new Watcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateValues();
            }
        });

        String[] spinnerArray = new String[]{getString(R.string.weekly), getString(R.string.biweekly), getString(R.string.monthly)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payFrequency.setAdapter(adapter);

        payFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        etSavingsPercentage.setText("20%");

        updateValues();

        percentageInput(etSavingsPercentage);

        createForMe.setOnClickListener(view -> createDefaultBudget());

        if (budgetId != 0) {
            Budget bud = DataTools.getBudget(CreateBudget.this, budgetId);
            header.setText(getString(R.string.edit_budget));
            etPayAmount.setText(FixNumber.addSymbol(bud.getPayAmount()));
            payFrequency.setSelection(bud.getPayFrequency());
            etSavingsPercentage.setText(bud.getSavingsPercentage() + "%");
            startDate = DateFormat.makeLocalDate(bud.getStartDate());
            endDate = DateFormat.makeLocalDate(bud.getEndDate());
            if (bud.getCategories() != null && !bud.getCategories().isEmpty()) {
                categoriesList.removeAllViews();
                for (Category categories : bud.getCategories()) {
                    View category = View.inflate(CreateBudget.this, R.layout.category_item, null);
                    EditText categoryName = category.findViewById(R.id.categoryName);
                    EditText categoryPercentage = category.findViewById(R.id.categoryPercentage);
                    ImageView removeCategory = category.findViewById(R.id.removeCategory);
                    categoryName.setText(categories.getCategoryName());
                    categoryPercentage.setText(categories.getCategoryPercentage() + "%");
                    percentageInput(categoryPercentage);
                    categoriesList.addView(category);
                    removeCategory.setOnClickListener(view -> categoriesList.removeView(category));
                }
            }
            updateValues();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        budgetStartDate.setText(formatter.format(startDate));
        budgetEndDate.setText(formatter.format(endDate));

        addCategory.setOnClickListener(view -> {
            View category = View.inflate(CreateBudget.this, R.layout.category_item, null);
            EditText categoryPercentage = category.findViewById(R.id.categoryPercentage);
            ImageView removeCategory = category.findViewById(R.id.removeCategory);
            percentageInput(categoryPercentage);
            categoriesList.addView(category);
            removeCategory.setOnClickListener(view1 -> categoriesList.removeView(category));
        });

        submit.setOnClickListener(v -> {

            if (!saveError) {
                if (etPayAmount.length() > 1 && etSavingsPercentage.length() > 1) {
                    createNewBudget();
                } else {
                    Toast.makeText(CreateBudget.this, getString(R.string.all_fields_must_be_filled_in), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void createDefaultBudget() {

        categoriesList.removeAllViews();
        ArrayList<String> categoryNames = DataTools.getBudgetCategories(CreateBudget.this);
        ArrayList<Integer> percentages = new ArrayList<>(Arrays.asList(10, 10, 15, 20, 10, 15, 20));
        for (int i = 0; i < categoryNames.size(); ++i) {
            View category = View.inflate(CreateBudget.this, R.layout.category_item, null);
            EditText categoryName = category.findViewById(R.id.categoryName);
            EditText categoryPercentage = category.findViewById(R.id.categoryPercentage);
            ImageView removeCategory = category.findViewById(R.id.removeCategory);
            percentageInput(categoryPercentage);
            categoriesList.addView(category);
            categoryName.setText(categoryNames.get(i));
            categoryPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentages.get(i)));
            percentageInput(categoryPercentage);
            removeCategory.setOnClickListener(view -> categoriesList.removeView(category));
        }
    }

    public void createNewBudget() {

        double payAmount = FixNumber.makeDouble(etPayAmount.getText().toString());
        int payFreq = payFrequency.getSelectedItemPosition();
        long start = DateFormat.makeLong(startDate);
        long end = DateFormat.makeLong(endDate);
        int savings = Integer.parseInt(etSavingsPercentage.getText().toString().replaceAll("%", ""));
        ArrayList<Category> budgetCategories = new ArrayList<>();
        if (categoriesList.getChildCount() > 0) {
            int categoryCount = categoriesList.getChildCount();
            for (int i = 0; i < categoryCount; ++i) {
                LinearLayout categoryItem = (LinearLayout) categoriesList.getChildAt(i);
                EditText name = (EditText) categoryItem.getChildAt(0);
                EditText percentage = (EditText) categoryItem.getChildAt(1);
                if (name.getText().toString().isEmpty()) {
                    return;
                }
                if (percentage.getText().toString().isEmpty()) {
                    return;
                } else {
                    Category cat = new Category(name.getText().toString(), Integer.parseInt(percentage.getText().toString().replaceAll("%", "")));
                    budgetCategories.add(cat);
                }
            }
        }
        boolean miscellaneous = false;
        for (Category cat : budgetCategories) {
            if (cat.getCategoryName().equals(getString(R.string.miscellaneous))) {
                miscellaneous = true;
            }
        }
        Budget a = null;
        for (Expense expense : repo.getExpenses()) {
            boolean found = false;
            for (Category cat : budgetCategories) {
                if (cat.getCategoryName().equals(expense.getCategory())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (!miscellaneous) {
                    budgetCategories.add(new Category(getString(R.string.miscellaneous), 5));
                }
                expense.setCategory(getString(R.string.miscellaneous));
            }
        }
        if (budgetId != 0) {
            Budget budget = DataTools.getBudget(CreateBudget.this, budgetId);
            budget.setPayAmount(payAmount);
            budget.setPayFrequency(payFreq);
            budget.setStartDate(start);
            budget.setEndDate(end);
            budget.setSavingsPercentage(savings);
            budget.setCategories(budgetCategories);
        } else {
            a = new Budget(payAmount, payFreq, start, end, id(), savings, budgetCategories);
        }

        ArrayList<Budget> remove = new ArrayList<>();
        for (Budget budget : repo.getUser(CreateBudget.this).getBudgets()) {
            if (budget.getStartDate() > start && budget.getStartDate() < end && budget.getBudgetId() != budgetId) {
                budget.setStartDate(end + 1);
                if (end + 1 > budget.getEndDate()) {
                    remove.add(budget);
                }

            } else if (budget.getEndDate() > start && budget.getEndDate() < end && budget.getBudgetId() != budgetId) {
                budget.setEndDate(start - 1);
                if (start - 1 < budget.getStartDate()) {
                    remove.add(budget);
                }
            } else if (budget.getStartDate() == start && budget.getEndDate() == end && budget.getBudgetId() != budgetId) {
                remove.add(budget);
            } else if (budget.getStartDate() == start && budget.getEndDate() < end && budget.getBudgetId() != budgetId) {
                remove.add(budget);
            } else if (budget.getStartDate() > start && budget.getEndDate() == end && budget.getBudgetId() != budgetId) {
                remove.add(budget);
            }
        }
        repo.getUser(CreateBudget.this).getBudgets().removeAll(remove);
        if (a != null) {
            repo.getUser(CreateBudget.this).getBudgets().add(a);
        }
        repo.editUser(CreateBudget.this)
                .setIncome(payAmount)
                .setPayFrequency(payFreq)
                .save((wasSuccessful, message) -> {
                    if (wasSuccessful) {
                        Intent budget = new Intent(CreateBudget.this, ViewBudget.class);
                        startActivity(budget);
                    }
                    else {
                        Notify.createPopup(CreateBudget.this, "Error: " + message, null);
                    }
                });
    }

    public void updateValues() {

        double monthlyPay = 0;
        double monthlyBills = 0;
        double monthlySavings = 0;
        double monthlyDisposable;
        int daysInMonth = LocalDate.now().lengthOfMonth();
        int weeksInMonth = daysInMonth / 7;
        long monthStart = DateFormat.makeLong(LocalDate.from(LocalDate.now().withDayOfMonth(1).atStartOfDay()));
        long monthEnd = DateFormat.makeLong(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        TextView billsLabel = findViewById(R.id.billsLabel);
        TextView savingsLabel = findViewById(R.id.savingsLabel);
        TextView disposableLabel = findViewById(R.id.disposableLabel);

        if (etPayAmount.getText().length() > 1) {
            monthlyPay = switch (payFrequency.getSelectedItemPosition()) {
                case 0 -> FixNumber.makeDouble(etPayAmount.getText().toString()) * weeksInMonth;
                case 1 ->
                        FixNumber.makeDouble(etPayAmount.getText().toString()) * ((double) weeksInMonth / 2);
                case 2 -> FixNumber.makeDouble(etPayAmount.getText().toString());
                default -> monthlyPay;
            };
        } else {
            monthlyPay = switch (repo.getUser(CreateBudget.this).getPayFrequency()) {
                case 0 ->
                        repo.getUser(CreateBudget.this).getIncome() * weeksInMonth;
                case 1 ->
                        repo.getUser(CreateBudget.this).getIncome() * ((double) weeksInMonth / 2);
                case 2 -> repo.getUser(CreateBudget.this).getIncome();
                default -> monthlyPay;
            };
        }
        if (etSavingsPercentage.getText().length() > 1) {
            double savingsPercentage = FixNumber.makeDouble(etSavingsPercentage.getText().toString().replaceAll("%", ""));
            monthlySavings = switch (payFrequency.getSelectedItemPosition()) {
                case 0 -> ((savingsPercentage / 100) * monthlyPay) / weeksInMonth;
                case 1 -> ((savingsPercentage / 100) * monthlyPay) / ((double) weeksInMonth / 2);
                case 2 -> ((savingsPercentage / 100) * monthlyPay);
                default -> monthlySavings;
            };
        }
        for (Payment payment : repo.getPayments()) {
            if (payment.getDueDate() >= monthStart && payment.getDueDate() <= monthEnd) {
                monthlyBills = monthlyBills + payment.getPaymentAmount();
            }
        }

        monthlyDisposable = monthlyPay - monthlySavings - monthlyBills;

        switch (payFrequency.getSelectedItemPosition()) {
            case 0:
                billsAverage.setText(FixNumber.addSymbol(String.valueOf(monthlyBills / weeksInMonth)));
                savingsAmount.setText(FixNumber.addSymbol(String.valueOf(monthlySavings / weeksInMonth)));
                disposableIncome.setText(FixNumber.addSymbol(String.valueOf(monthlyDisposable / weeksInMonth)));
                billsLabel.setText(getString(R.string.weekly_bills_average1));
                savingsLabel.setText(getString(R.string.weekly_savings1));
                disposableLabel.setText(getString(R.string.weekly_disposable_income1));
                break;
            case 1:
                billsAverage.setText(FixNumber.addSymbol(String.valueOf(monthlyBills / ((double) weeksInMonth / 2))));
                savingsAmount.setText(FixNumber.addSymbol(String.valueOf(monthlySavings / ((double) weeksInMonth / 2))));
                disposableIncome.setText(FixNumber.addSymbol(String.valueOf(monthlyDisposable / ((double) weeksInMonth / 2))));
                billsLabel.setText(getString(R.string.bi_weekly_bills_average));
                savingsLabel.setText(getString(R.string.bi_weekly_savings));
                disposableLabel.setText(getString(R.string.bi_weekly_disposable_income));
                break;
            case 2:
                billsAverage.setText(FixNumber.addSymbol(String.valueOf(monthlyBills)));
                savingsAmount.setText(FixNumber.addSymbol(String.valueOf(monthlySavings)));
                disposableIncome.setText(FixNumber.addSymbol(String.valueOf(monthlyDisposable)));
                billsLabel.setText(getString(R.string.monthly_bills_average));
                savingsLabel.setText(getString(R.string.monthly_savings));
                disposableLabel.setText(getString(R.string.monthly_disposable_income));
                break;
        }

        boolean error = false;
        int totalPercentage = 0;
        int categoryCount = categoriesList.getChildCount();
        for (int i = 0; i < categoryCount; ++i) {
            LinearLayout categoryItem = (LinearLayout) categoriesList.getChildAt(i);
            EditText name = (EditText) categoryItem.getChildAt(0);
            EditText percentage = (EditText) categoryItem.getChildAt(1);
            if (percentage.length() <= 1 || name.getText().toString().length() < 2) {
                error = true;
            } else if (percentage.getText().toString().length() == 1 && percentage.getText().toString().equalsIgnoreCase("%") || percentage.getText().toString().isEmpty()) {
                error = true;
            } else {
                totalPercentage += Integer.parseInt(percentage.getText().toString().replaceAll("%", ""));
            }
        }
        if (totalPercentage > 100) {
            categoriesError.setText(getString(R.string.percent_values_cannot_equal_more_than_100_when_combined));
            categoriesError.setVisibility(View.VISIBLE);
            submit.setEnabled(false);
        } else if (error) {
            categoriesError.setText(getString(R.string.a_percent_value_is_required_for_every_category));
            categoriesError.setVisibility(View.VISIBLE);
            submit.setEnabled(false);
        } else {
            categoriesError.setVisibility(View.GONE);
            submit.setEnabled(true);
        }

        if (monthlyDisposable < 0) {
            savingsError.setVisibility(View.VISIBLE);
            saveError = true;
        } else {
            savingsError.setVisibility(View.GONE);
            saveError = false;
        }

    }

    public void updateWeek(LocalDate date) {
        startDate = date;
        updateValues();
    }

    public void getDateFromUser(TextView dueDate, boolean start, long date) {

        DatePicker dp = DateFormat.getPaymentDateFromUser(getSupportFragmentManager(), date, getString(R.string.selectDate));
        dp.setListener(v12 -> {
            if (DatePicker.selection != null) {
                String startDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).format(DatePicker.selection);
                dueDate.setText(startDate);
                if (start) {
                    updateWeek(DatePicker.selection);
                } else {
                    endDate = DatePicker.selection;
                }
            }
        });
    }

    public void percentageInput(EditText edit) {

        final String[] formatted = new String[1];
        edit.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (s.isEmpty()) return;
                edit.removeTextChangedListener(this);
                if (edit.getText().toString().isEmpty()) {
                    edit.setText("%");
                    edit.setSelection(0);
                } else {
                    String cleanString = s.replaceAll("%", "");
                    formatted[0] = cleanString + "%";
                    if (formatted[0].length() > 3) {
                        formatted[0] = formatted[0].substring(0, 2) + formatted[0].substring(formatted[0].length() - 1);
                    }
                    edit.setText(formatted[0]);
                    edit.setSelection(edit.length() - 1);
                }
                edit.addTextChangedListener(this);
                updateValues();
            }
        });
    }

    int id() {
        final String AB = "0123456789";
        SecureRandom rnd = new SecureRandom();
        int length = 9;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return Integer.parseInt(sb.toString());
    }
}