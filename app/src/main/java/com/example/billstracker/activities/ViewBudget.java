package com.example.billstracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Category;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.popup_classes.MonthYearPickerDialog;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.Repo;
import com.example.billstracker.tools.Tools;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Locale;

public class ViewBudget extends AppCompatActivity {

    ConstraintLayout pb;
    TextView editBudget;
    TextView btnDaily;
    TextView btnWeekly;
    TextView btnMonthly;
    TextView incomeAmount;
    TextView billsAmount;
    TextView savingsAmount;
    TextView disposableAmount;
    TextView billsPercentage;
    TextView savingsPercentage;
    TextView disposablePercentage;
    TextView spentAmount, budgetTitle;
    TextView budgetRemaining;
    TextView dateView;
    ImageView backMonth, forwardMonth;
    long weekStart;
    long weekEnd;
    long monthStart;
    long monthEnd;
    int daysInMonth;
    long dateIntValue;
    int freq;
    LocalDate selectedDate;
    double dailyIncome;
    double dailySavings;
    double dailyBills;
    double dailyDisposable;
    double spendingAmount;
    double monthlyBillAmount;
    Budget budget;
    PieChart pieChart;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_budget);

        pb = findViewById(R.id.progressBar);
        editBudget = findViewById(R.id.btnEditBudget);
        pieChart = findViewById(R.id.pieChart3);
        btnDaily = findViewById(R.id.btnDaily);
        btnWeekly = findViewById(R.id.btnWeekly);
        btnMonthly = findViewById(R.id.btnMonthly);
        incomeAmount = findViewById(R.id.totalIncomeView);
        billsAmount = findViewById(R.id.billsView);
        savingsAmount = findViewById(R.id.saveAmount);
        disposableAmount = findViewById(R.id.totalDisposable);
        billsPercentage = findViewById(R.id.billsPercentage);
        savingsPercentage = findViewById(R.id.savePercentage);
        disposablePercentage = findViewById(R.id.disposablePercentage);
        spentAmount = findViewById(R.id.spentAmount);
        budgetRemaining = findViewById(R.id.budgetRemaining);
        backMonth = findViewById(R.id.backMonth);
        forwardMonth = findViewById(R.id.forwardMonth);
        dateView = findViewById(R.id.dateView);
        budgetTitle = findViewById(R.id.budgetTitle);

        Tools.fixProgressBarLogo(pb);
        NavController nc = new NavController();
        nc.navController(ViewBudget.this, ViewBudget.this, pb, "budget");

        selectedDate = LocalDate.now(ZoneId.systemDefault());
        weekStart = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        dateIntValue = DateFormat.makeLong(selectedDate);
        monthlyBillAmount = 0;
        dailyIncome = 0;
        monthStart = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        monthEnd = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()));
        daysInMonth = selectedDate.lengthOfMonth();

        btnDaily.setBackground(null);
        btnWeekly.setBackground(AppCompatResources.getDrawable(ViewBudget.this, R.drawable.border_selected));
        btnMonthly.setBackground(null);

        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        editor = sp.edit();

        btnDaily.setOnClickListener(view -> {
            editor.putInt("BudgetFrequency", 0);
            editor.commit();
            btnDaily.setBackground(AppCompatResources.getDrawable(ViewBudget.this, R.drawable.border_selected));
            btnWeekly.setBackground(null);
            btnMonthly.setBackground(null);
            freq = 0;
            calculateValues(0);
        });
        btnWeekly.setOnClickListener(view -> {
            editor.putInt("BudgetFrequency", 1);
            editor.commit();
            btnDaily.setBackground(null);
            btnWeekly.setBackground(AppCompatResources.getDrawable(ViewBudget.this, R.drawable.border_selected));
            btnMonthly.setBackground(null);
            freq = 1;
            calculateValues(1);
        });
        btnMonthly.setOnClickListener(view -> {
            editor.putInt("BudgetFrequency", 2);
            editor.commit();
            btnDaily.setBackground(null);
            btnWeekly.setBackground(null);
            btnMonthly.setBackground(AppCompatResources.getDrawable(ViewBudget.this, R.drawable.border_selected));
            freq = 2;
            calculateValues(2);
        });
        backMonth.setOnClickListener(view -> {
            switch (freq) {
                case 0:
                    selectedDate = selectedDate.minusDays(1);
                    break;
                case 1:
                    selectedDate = selectedDate.minusWeeks(1);
                    break;
                case 2:
                    selectedDate = selectedDate.minusMonths(1);
            }
            loadBudget(freq);
        });
        dateView.setOnClickListener(v -> {
            MonthYearPickerDialog pd = new MonthYearPickerDialog();
            pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            pd.setListener((view, year, month, dayOfMonth) -> {
                selectedDate = LocalDate.of(year, month, 1);
                loadBudget(freq);
            });
        });
        forwardMonth.setOnClickListener(view -> {
            switch (freq) {
                case 0:
                    selectedDate = selectedDate.plusDays(1);
                    break;
                case 1:
                    selectedDate = selectedDate.plusWeeks(1);
                    break;
                case 2:
                    selectedDate = selectedDate.plusMonths(1);
            }
            loadBudget(freq);
        });

        setupPieChart();
        loadBudget(freq);

        if (sp.contains("BudgetFrequency")) {
            if (sp.getInt("BudgetFrequency", 0) == 0) {
                btnDaily.performClick();
                freq = 0;
            }
            else if (sp.getInt("BudgetFrequency", 0) == 1) {
                btnWeekly.performClick();
                freq = 1;
            }
            else if (sp.getInt("BudgetFrequency", 0) == 2) {
                btnMonthly.performClick();
                freq = 2;
            }
            else {
                freq = 1;
            }
        }

    }
    public void loadBudget (int frequency) {

        dateIntValue = DateFormat.makeLong(selectedDate);
        monthlyBillAmount = 0;
        dailyIncome = 0;
        weekStart = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        monthStart = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        monthEnd = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()));
        daysInMonth = selectedDate.lengthOfMonth();

        boolean found = false;

        if (Repo.getInstance().getUser(ViewBudget.this).getBudgets() != null && !Repo.getInstance().getUser(ViewBudget.this).getBudgets().isEmpty()) {
            for (Budget bud : Repo.getInstance().getUser(ViewBudget.this).getBudgets()) {
                if (bud.getStartDate() <= dateIntValue && bud.getEndDate() >= dateIntValue) {
                    budget = bud;
                    found = true;
                    break;
                }
            }
        }
        else {
            Repo.getInstance().getUser(ViewBudget.this).setBudgets(new ArrayList<>());
        }
        if (!found) {
            budget = new Budget(Repo.getInstance().getUser(ViewBudget.this).getIncome(), Repo.getInstance().getUser(ViewBudget.this).getPayFrequency(), weekStart, weekEnd, id(), 0, new ArrayList<>());
            editBudget.setText(getString(R.string.create_a_new_budget));
            editBudget.setOnClickListener(view -> startActivity(new Intent(ViewBudget.this, CreateBudget.class)));
        }
        else {
            editBudget.setOnClickListener(view -> startActivity(new Intent(ViewBudget.this, CreateBudget.class).putExtra("budgetId", budget.getBudgetId())));
            editBudget.setText(getString(R.string.edit));
            if (budget.getStartDate() > DateFormat.makeLong(LocalDate.now(ZoneId.systemDefault()))) {
                budgetTitle.setText(R.string.future_budget);
            }
            else if (budget.getEndDate() < DateFormat.makeLong(LocalDate.now(ZoneId.systemDefault()))) {
                budgetTitle.setText(R.string.previous_budget);
            }
            else {
                budgetTitle.setText(getString(R.string.current_budget));
            }
        }

        monthlyBillAmount = Tools.getBillsAmount(2, selectedDate);
        if (budget.getPayFrequency() == 0) {
            dailyIncome = budget.getPayAmount() / 7;
        }
        else if (budget.getPayFrequency() == 1) {
            dailyIncome = budget.getPayAmount() / 14;
        }
        else {
            dailyIncome = budget.getPayAmount() / daysInMonth;
        }
        dailySavings = (budget.getSavingsPercentage() / 100.0) * dailyIncome;
        dailyBills = monthlyBillAmount / daysInMonth;
        dailyDisposable = dailyIncome - dailySavings - dailyBills;
        calculateValues(frequency);
    }

    public void calculateValues (int frequency) {

        dateIntValue = DateFormat.makeLong(selectedDate);
        weekStart = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        monthStart = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        monthEnd = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()));
        daysInMonth = selectedDate.lengthOfMonth();

        spendingAmount = 0;
        if (Repo.getInstance().getExpenses() != null) {
            for (Expense expense : Repo.getInstance().getExpenses()) {
                switch (frequency) {
                    case 0:
                        if (expense.getDate() >= dateIntValue && expense.getDate() < DateFormat.makeLong(DateFormat.makeLocalDate(dateIntValue).plusDays(1))) {
                            spendingAmount += expense.getAmount();
                        }
                        break;
                    case 1:
                        if (expense.getDate() >= weekStart && expense.getDate() <= weekEnd) {
                            spendingAmount += expense.getAmount();
                        }
                        break;
                    case 2:
                        if (expense.getDate() >= monthStart && expense.getDate() <= monthEnd) {
                            spendingAmount += expense.getAmount();
                        }
                        break;
                }
            }
        }

        if (frequency == 0) {
            dateView.setText(String.format(Locale.getDefault(), "%s %d, %d", selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.getDayOfMonth(), selectedDate.getYear()));
        }
        else if (frequency == 1) {
            dateView.setText(String.format(Locale.getDefault(), "%s %d - %s %d", selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).getDayOfMonth(), selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).getDayOfMonth()));
        }
        else {
            dateView.setText(String.format(Locale.getDefault(), "%s %d", selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.getYear()));
        }

        updateUi(frequency);
    }

    public void updateUi (int frequency) {

        if ((int)((dailyBills * 100) / dailyIncome) == 0 && (dailyBills * 100.0) / dailyIncome > 0) {
            billsPercentage.setText("<1%");
        }
        else {
            billsPercentage.setText(String.format(Locale.getDefault(), "%d%%", (int) ((dailyBills * 100) / dailyIncome)));
        }
        if ((int)((dailyDisposable * 100) / dailyIncome) == 0 && (dailyDisposable * 100.0) / dailyIncome > 0) {
            disposablePercentage.setText("<1%");
        }
        else {
            disposablePercentage.setText(String.format(Locale.getDefault(), "%d%%", (int) ((dailyDisposable * 100) / dailyIncome)));
        }
        savingsPercentage.setText(String.format(Locale.getDefault(), "%d%%", budget.getSavingsPercentage()));
        spentAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(spendingAmount))));

        switch (frequency) {
            case 0:
                incomeAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyIncome))));
                billsAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyBills))));
                savingsAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailySavings))));
                disposableAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable))));
                budgetRemaining.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable - spendingAmount))));
                break;
            case 1:
                incomeAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyIncome * 7))));
                billsAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyBills * 7))));
                savingsAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailySavings * 7))));
                disposableAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable * 7))));
                budgetRemaining.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf((dailyDisposable * 7) - spendingAmount))));
                break;
            case 2:
                incomeAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyIncome * daysInMonth))));
                billsAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyBills * daysInMonth))));
                savingsAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailySavings * daysInMonth))));
                disposableAmount.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable * daysInMonth))));
                budgetRemaining.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf((dailyDisposable * daysInMonth) - spendingAmount))));
        }
        loadPieChartData(frequency);
    }

    public void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(getResources().getColor(android.R.color.transparent, getTheme()));
        pieChart.setHoleRadius(80f);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(getResources().getColor(R.color.whiteAndBlack, getTheme()));
        pieChart.setEntryLabelTextSize(8);
        pieChart.canResolveTextAlignment();
        pieChart.setDrawEntryLabels(false);
        pieChart.setNoDataText(getString(R.string.noBudgetDataWasFound));
        pieChart.setNoDataTextColor(getResources().getColor(R.color.black, getTheme()));
        pieChart.setExtraOffsets(5,0,0,0);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterTextColor(ResourcesCompat.getColor(getResources(), R.color.blackAndWhite, getTheme()));
        pieChart.setCenterTextSize(11);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        //pieChart.setMinimumWidth(1000);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setDrawInside(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTypeface(Typeface.DEFAULT_BOLD);
        l.setEnabled(true);
    }

    public void loadPieChartData(int period) {
        ArrayList <PieEntry> entries = new ArrayList<>();
        double totalSpent = 0;

        ArrayList <String> categoryNames = new ArrayList<>();
        if (budget.getCategories() != null && !budget.getCategories().isEmpty() && budget.getStartDate() <= dateIntValue && budget.getEndDate() >= dateIntValue) {
            for (Category category : budget.getCategories()) {
                categoryNames.add(category.getCategoryName());
            }
        }
        else {
            budget.setCategories(new ArrayList<>());
            //entries.add(new PieEntry((float) (0), getString(R.string.no_budget_categories_assigned)));
        }
        if (Repo.getInstance().getExpenses() != null) {
            for (int i = 0; i < categoryNames.size(); ++i) {
                double totalForCategory = 0;
                for (Expense expense : Repo.getInstance().getExpenses()) {
                    if (period == 0) {
                        if (expense.getDate() == dateIntValue && expense.getCategory().equals(categoryNames.get(i))) {
                            totalForCategory = totalForCategory + expense.getAmount();
                            totalSpent += expense.getAmount();
                        }
                    } else if (period == 1) {
                        if (expense.getDate() >= weekStart && expense.getDate() <= weekEnd && expense.getCategory().equals(categoryNames.get(i))) {
                            totalForCategory += expense.getAmount();
                            totalSpent += expense.getAmount();
                        }
                    } else {
                        if (expense.getDate() >= monthStart && expense.getDate() <= monthEnd && expense.getCategory().equals(categoryNames.get(i))) {
                            totalForCategory += expense.getAmount();
                            totalSpent += expense.getAmount();
                            break;
                        }
                    }
                }
                if (totalForCategory > 0) {
                    entries.add(new PieEntry((float) (totalForCategory), categoryNames.get(i) + " " + FixNumber.addSymbol(String.valueOf(totalForCategory))));
                }
            }
            if (entries.isEmpty()) {
                entries.add(new PieEntry((float) (100), ""));
                pieChart.getLegend().setEnabled(false);
            }
            else {
                pieChart.getLegend().setEnabled(true);
            }
        }
        else {
            entries.add(new PieEntry((float) (100), ""));
        }
        PieDataSet dataSet;
        switch (period) {
            case 0:
                pieChart.setCenterText( getString(R.string.remaining) + " " + FixNumber.addSymbol(String.valueOf(dailyDisposable - totalSpent)) + "\n\n" +
                        getString(R.string.total_budget) + " " + FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable))));
                dataSet = new PieDataSet(entries, "");
                break;
            case 1:
                pieChart.setCenterText( getString(R.string.remaining) + " " + FixNumber.addSymbol(String.valueOf(dailyDisposable * 7 - totalSpent)) + "\n\n" +
                        getString(R.string.total_budget) + " " + FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable * 7))));
                dataSet = new PieDataSet(entries, "");
                break;
            default:
                pieChart.setCenterText( getString(R.string.remaining) + " " + FixNumber.addSymbol(String.valueOf(dailyDisposable * daysInMonth - totalSpent)) + "\n\n" +
                        getString(R.string.total_budget) + " " + FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyDisposable * daysInMonth))));
                dataSet = new PieDataSet(entries, "");
                break;

        }

        ArrayList <Integer> colors = new ArrayList<>();
        for (int color: ViewBudget.this.getResources().getIntArray(R.array.pieChartCorresponding)) {
            colors.add(color);
        }
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(1400, Easing.EaseInOutQuad);
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

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}