package com.example.billstracker.activities;

import static com.example.billstracker.tools.BillerManager.id;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Category;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.popup_classes.AddExpense;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.recycler_adapters.CategoriesRecyclerAdapter;
import com.example.billstracker.recycler_adapters.TransactionsRecyclerAdapter;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.Tools;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class Spending extends BaseActivity {

    public static LocalDate selectedDate;
    int freq;
    LocalDate weekStart, weekEnd;
    ConstraintLayout pb;
    LinearLayout noExpensesFound, cats, categoriesListLayout;
    RecyclerView expensesList;
    TextView backMonth, forwardMonth, totalSpent, selectedWeek, budgetRemaining, btnAddExpense, btnDaily, btnWeekly, btnMonthly, showCategories, barChartTitle, noChartData;
    SharedPreferences sp;
    Budget budget;
    ArrayList<String> categories, days, weeks, months;
    SharedPreferences.Editor editor;
    Expense expense, transaction;
    RelativeLayout transactionsLayout;
    TransactionsRecyclerAdapter adapter;
    CategoriesRecyclerAdapter catAdapter;
    BarChart barChart;
    BarDataSet barDataSet;
    ArrayList<BarEntry> barEntries;
    double periodBudget;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_spending);

        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        editor = sp.edit();
        selectedDate = LocalDate.now(ZoneId.systemDefault());
        categories = new ArrayList<>();
        days = new ArrayList<>();
        weeks = new ArrayList<>();
        months = new ArrayList<>();

        pb = findViewById(R.id.progressBar);
        cats = findViewById(R.id.cats);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        budgetRemaining = findViewById(R.id.createABudget);
        totalSpent = findViewById(R.id.totalSpent);
        selectedWeek = findViewById(R.id.selectedMonth);
        expensesList = findViewById(R.id.expensesList);
        backMonth = findViewById(R.id.backMonth);
        forwardMonth = findViewById(R.id.forwardMonth);
        btnDaily = findViewById(R.id.btnDaily);
        btnWeekly = findViewById(R.id.btnWeekly);
        barChart = findViewById(R.id.idBarChart);
        showCategories = findViewById(R.id.btnShowCategories);
        barChartTitle = findViewById(R.id.barChartTitle);
        noChartData = findViewById(R.id.noChartData);
        btnMonthly = findViewById(R.id.btnMonthly);
        noExpensesFound = findViewById(R.id.noExpensesFound);
        transactionsLayout = findViewById(R.id.transactionsLayout);
        categoriesListLayout = findViewById(R.id.categories_list_layout);

        Tools.fixProgressBarLogo(pb);
        NavController nc = new NavController();
        nc.navController(Spending.this, Spending.this, pb, "spending");

        transactionsLayout.setVisibility(View.GONE);
        categoriesListLayout.setVisibility(View.GONE);

        ArrayList<Expense> remove = new ArrayList<>();
        for (Expense expense : repo.getExpenses()) {
            boolean found = false;
            for (Expense exp : repo.getExpenses()) {
                if (expense.getId().equals(exp.getId())) {
                    if (!found) {
                        found = true;
                    } else {
                        remove.add(exp);
                    }
                }
            }
        }

        if (!remove.isEmpty()) {
            repo.getExpenses().removeAll(remove);
        }

        addListeners();

        if (sp.contains("frequency")) {
            if (sp.getInt("frequency", 0) == 0) {
                btnDaily.performClick();
                freq = 0;
            } else if (sp.getInt("frequency", 0) == 1) {
                btnWeekly.performClick();
                freq = 1;
            } else if (sp.getInt("frequency", 0) == 2) {
                btnMonthly.performClick();
                freq = 2;
            } else {
                freq = 1;
            }
        } else {
            btnDaily.setBackground(null);
            btnWeekly.setBackground(AppCompatResources.getDrawable(Spending.this, R.drawable.border_selected));
            btnMonthly.setBackground(null);
            editor.putInt("frequency", 1);
            editor.commit();
            freq = 1;
        }

        setupBarChart();
        listExpenses();
    }

    private void addListeners() {
        btnAddExpense.setOnClickListener(v -> {
            AddExpense ae = new AddExpense(Spending.this, null);
            ae.setSubmitExpenseListener(v1 -> listExpenses());
        });

        showCategories.setOnClickListener(v -> {
            if (transactionsLayout.getVisibility() == View.VISIBLE) {
                transactionsLayout.setVisibility(View.GONE);
                showCategories.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
            } else {
                transactionsLayout.setVisibility(View.VISIBLE);
                showCategories.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_keyboard_arrow_up_24, 0);
            }
        });

        backMonth.setOnClickListener(view -> {
            switch (freq) {
                case 0:
                    selectedDate = LocalDate.from(selectedDate.minusDays(1).atStartOfDay());
                    break;
                case 1:
                    selectedDate = LocalDate.from(selectedDate.minusWeeks(1).atStartOfDay());
                    break;
                case 2:
                    selectedDate = LocalDate.from(selectedDate.minusMonths(1).atStartOfDay());
                    break;
            }
            listExpenses();
        });

        forwardMonth.setOnClickListener(view -> {
            switch (freq) {
                case 0:
                    selectedDate = LocalDate.from(selectedDate.plusDays(1).atStartOfDay());
                    break;
                case 1:
                    selectedDate = LocalDate.from(selectedDate.plusWeeks(1).atStartOfDay());
                    break;
                case 2:
                    selectedDate = LocalDate.from(selectedDate.plusMonths(1).atStartOfDay());
                    break;
            }
            listExpenses();
        });

        btnDaily.setOnClickListener(view -> selectFrequency(0));
        btnWeekly.setOnClickListener(view -> selectFrequency(1));
        btnMonthly.setOnClickListener(view -> selectFrequency(2));
    }

    private void selectFrequency(int frequency) {
        editor.putInt("frequency", frequency);
        editor.commit();
        btnDaily.setBackground(null);
        btnWeekly.setBackground(null);
        btnMonthly.setBackground(null);
        switch (frequency) {
            case 0:
                btnDaily.setBackground(AppCompatResources.getDrawable(Spending.this, R.drawable.border_selected));
                break;
            case 1:
                btnWeekly.setBackground(AppCompatResources.getDrawable(Spending.this, R.drawable.border_selected));
                break;
            case 2:
                btnMonthly.setBackground(AppCompatResources.getDrawable(Spending.this, R.drawable.border_selected));
                break;
        }
        freq = frequency;
        listExpenses();
    }

    private void setupBarChart() {

        barEntries = getBarEntries();
        boolean hasValue = false;
        if (barEntries != null && !barEntries.isEmpty()) {
            for (BarEntry entry : barEntries) {
                if (barEntries.indexOf(entry) < barEntries.size() - 1 && entry.getY() > 0) {
                    hasValue = true;
                    break;
                }
            }
        }
        if (hasValue) {
            barChart.setVisibility(View.VISIBLE);
            noChartData.setVisibility(View.GONE);
        } else {
            barChart.setVisibility(View.GONE);
            noChartData.setVisibility(View.VISIBLE);
        }
        barDataSet = new BarDataSet(getBarEntries(), "Data");
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : getResources().getIntArray(R.array.pieChartCorresponding)) {
            colors.add(color);
        }
        barDataSet.setColors(colors);
        barDataSet.setDrawValues(false);
        BarData data = new BarData(barDataSet);
        barDataSet.setBarBorderColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
        barDataSet.setBarBorderWidth(.7f);
        barChart.setData(data);
        barChart.animateY(500);
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(35);
        barChart.setDrawGridBackground(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.setPinchZoom(false);
        barChart.getXAxis().setLabelRotationAngle(65);

        data.setBarWidth(0.5f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
        xAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineWidth(2);
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisLineWidth(3);
        leftAxis.setAxisLineColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridLineWidth(.3f);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawAxisLine(false);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.getXAxis().setAxisMinimum(0);
        barChart.animate();
        leftAxis.setValueFormatter(new MyYAxisValueFormatter());
        if (freq == 0 && days != null && !days.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        }
        if (freq == 1 && weeks != null && !weeks.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(weeks));
        } else if (freq == 2 && months != null && !months.isEmpty()) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        }
        xAxis.setAxisMinimum(data.getXMin() - 0.5f);
        xAxis.setAxisMaximum(data.getXMax() - 0.5f);
        xAxis.setTextSize(10);
        barChart.setClipToPadding(false);
        periodBudget = 0;
        barChart.setScaleEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.invalidate();
    }

    private ArrayList<BarEntry> getBarEntries() {
        barEntries = new ArrayList<>();

        if (days == null) {
            days = new ArrayList<>();
        }
        if (weeks == null) {
            weeks = new ArrayList<>();
        }
        if (months == null) {
            months = new ArrayList<>();
        }
        if (repo.getExpenses() != null) {
            if (freq == 0) {
                days.clear();
                LocalDate start = selectedDate.minusDays(3);
                for (int i = 0; i < 8; ++i) {
                    double total = 0;
                    for (Expense expense : repo.getExpenses()) {
                        if (LocalDate.from(DateFormat.makeLocalDate(expense.getDate()).atStartOfDay()).isEqual(start)) {
                            total = total + expense.getAmount();
                        }
                    }
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault());
                    days.add(dateFormatter.format(start));
                    barEntries.add(new BarEntry((float) i, (float) total));
                    start = start.plusDays(1);
                }
                barChartTitle.setText(getString(R.string.daily_spending));
            } else if (freq == 1) {
                weeks.clear();
                LocalDate start = selectedDate.minusWeeks(3).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                for (int i = 0; i < 8; ++i) {
                    double total = 0;
                    for (Expense expense : repo.getExpenses()) {
                        if (expense.getDate() >= DateFormat.makeLong(start.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))) && expense.getDate() <= DateFormat.makeLong(start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))) {
                            total = total + expense.getAmount();
                        }
                    }
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault());
                    weeks.add(dateFormatter.format(start));
                    barEntries.add(new BarEntry((float) i, (float) total));
                    start = start.plusWeeks(1);
                }
                barChartTitle.setText(getString(R.string.weekly_spending));
            } else {
                months.clear();
                LocalDate start = selectedDate.minusMonths(3);
                for (int i = 0; i < 8; ++i) {
                    double total = 0;
                    for (Expense expense : repo.getExpenses()) {
                        if (expense.getDate() >= DateFormat.makeLong(start.withDayOfMonth(1)) && expense.getDate() <= DateFormat.makeLong(start.withDayOfMonth(start.lengthOfMonth()))) {
                            total = total + expense.getAmount();
                        }
                    }
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault());
                    months.add(dateFormatter.format(start));
                    barEntries.add(new BarEntry((float) i, (float) total));
                    start = start.plusMonths(1);
                }
                barChartTitle.setText(getString(R.string.monthly_spending));
            }
        }

        return barEntries;
    }


    private void listExpenses() {

        setupBarChart();
        double dailyBills;
        double dailyBudget;
        double monthlyBills;
        double totalAmountSpent = 0;
        double totalBudget = 0;
        double expenseTotal = 0;
        double dailyIncome = 0;
        ArrayList<Expense> expenseList;
        long start = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        long end = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()));
        boolean misc = false;
        weekStart = LocalDate.from(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay());
        weekEnd = LocalDate.from(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atStartOfDay());
        categories.clear();

        if (repo.getUser(Spending.this).getBudgets() != null) {
            if (!repo.getUser(Spending.this).getBudgets().isEmpty()) {
                for (Budget bud : repo.getUser(Spending.this).getBudgets()) {
                    if (bud.getStartDate() <= DateFormat.makeLong(selectedDate) && bud.getEndDate() >= DateFormat.makeLong(selectedDate)) {
                        budget = bud;
                        break;
                    }
                }
            }
        }
        if (budget == null) {
            budget = new Budget(repo.getUser(Spending.this).getIncome(), repo.getUser(Spending.this).getPayFrequency(), DateFormat.makeLong(selectedDate.withDayOfMonth(1).minusMonths(6)),
                    DateFormat.makeLong(LocalDate.from(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()).atStartOfDay()).plusMonths(6)), id(), 20, new ArrayList<>());
        }
        dailyIncome = switch (budget.getPayFrequency()) {
            case 0 -> budget.getPayAmount() / 7;
            case 1 -> budget.getPayAmount() / 14;
            case 2 -> budget.getPayAmount() / selectedDate.lengthOfMonth();
            default -> dailyIncome;
        };

        monthlyBills = Tools.getBillsAmount(2, selectedDate);

        if (budget.getCategories() != null) {
            if (!budget.getCategories().isEmpty()) {
                for (Category category : budget.getCategories()) {
                    categories.add(category.getCategoryName());
                }
            } else {
                budget.getCategories().add(new Category("Miscellaneous", 20));
                misc = true;
                categories.add("Miscellaneous");
            }
        } else {
            budget.setCategories(new ArrayList<>());
            budget.getCategories().add(new Category("Miscellaneous", 20));
            misc = true;
            categories.add("Miscellaneous");
        }

        dailyBills = monthlyBills / selectedDate.lengthOfMonth();
        dailyBudget = dailyIncome - (dailyIncome * (budget.getSavingsPercentage() / 100.0)) - dailyBills;

        switch (freq) {
            case 0:
                selectedWeek.setText(String.format(Locale.getDefault(), "%s %d, %d", selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.getDayOfMonth(), selectedDate.getYear()));
                start = DateFormat.makeLong(selectedDate);
                end = DateFormat.makeLong(selectedDate.plusDays(1));
                totalBudget = dailyBudget;
                break;
            case 1:
                selectedWeek.setText(String.format(Locale.getDefault(), "%s %d - %s %d", weekStart.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), weekStart.getDayOfMonth(),
                        weekEnd.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), weekEnd.getDayOfMonth()));
                start = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
                end = DateFormat.makeLong(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
                totalBudget = dailyBudget * 7;
                break;
            case 2:
                selectedWeek.setText(String.format(Locale.getDefault(), "%s %d", selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.getYear()));
                start = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
                end = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()));
                totalBudget = dailyBudget * selectedDate.lengthOfMonth();
                break;
        }

        expenseList = new ArrayList<>();
        if (repo.getExpenses() != null && !repo.getExpenses().isEmpty()) {
            for (Expense expense : repo.getExpenses()) {
                if (expense.getDate() >= start && expense.getDate() <= end) {
                    if (!expenseList.contains(expense)) {
                        expenseList.add(expense);
                    }
                }
            }
        }

        ArrayList<Expense> remove = new ArrayList<>();
        for (Expense expense : expenseList) {
            boolean found = false;
            for (Expense exp : expenseList) {
                if (expense.getId().equals(exp.getId())) {
                    if (!found) {
                        found = true;
                    } else {
                        remove.add(exp);
                    }
                }
            }
        }

        if (!remove.isEmpty()) {
            expenseList.removeAll(remove);
        }
        for (Expense expense : expenseList) {
            totalAmountSpent = totalAmountSpent + expense.getAmount();
            expenseTotal = expenseTotal + expense.getAmount();
        }

        totalSpent.setText(FixNumber.addSymbol(String.valueOf(totalAmountSpent)));

        DividerItemDecoration did = new DividerItemDecoration(Spending.this, DividerItemDecoration.VERTICAL);
        did.setDrawable(new ColorDrawable(getResources().getColor(R.color.grey, getTheme())));

        RecyclerView categoryList = findViewById(R.id.categoryList);
        catAdapter = new CategoriesRecyclerAdapter(Spending.this, budget.getCategories(), start, end, freq, dailyBudget, misc);
        categoryList.setAdapter(catAdapter);
        categoryList.setNestedScrollingEnabled(false);
        categoryList.setHasFixedSize(false);
        categoryList.setLayoutManager(new LinearLayoutManager(Spending.this));
        categoryList.addItemDecoration(did);
        catAdapter.setClickListener((position, category) -> adjustCategories(dailyBudget));

        adapter = new TransactionsRecyclerAdapter(Spending.this, expenseList, budget);
        expensesList.setAdapter(adapter);
        expensesList.setNestedScrollingEnabled(false);
        expensesList.setLayoutManager(new LinearLayoutManager(Spending.this));
        expensesList.addItemDecoration(did);

        ItemTouchHelper itemTouchHelper = getItemTouchHelper(adapter);
        itemTouchHelper.attachToRecyclerView(expensesList);

        if (expenseList.isEmpty()) {
            noExpensesFound.setVisibility(View.VISIBLE);
        } else {
            noExpensesFound.setVisibility(View.GONE);
        }
        if (totalBudget > 0) {
            budgetRemaining.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(totalBudget - expenseTotal))));
            budgetRemaining.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
            budgetRemaining.setEnabled(false);
        } else {
            budgetRemaining.setText(R.string.create_a_new_budget);
            budgetRemaining.setTextColor(getResources().getColor(R.color.primary, getTheme()));
            budgetRemaining.setEnabled(true);
            budgetRemaining.setOnClickListener(v -> {
                Intent createBudget = new Intent(Spending.this, CreateBudget.class);
                startActivity(createBudget);
            });
        }
        periodBudget = totalBudget;
        barChart.getAxisLeft().removeAllLimitLines();
        LimitLine ll = new LimitLine((float) periodBudget, getString(R.string.budget));
        ll.setLineColor(ResourcesCompat.getColor(getResources(), R.color.payBill, getTheme()));
        ll.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
        ll.setLineWidth(3);
        barChart.getAxisLeft().addLimitLine(ll);
    }

    @NonNull
    private ItemTouchHelper getItemTouchHelper(TransactionsRecyclerAdapter adapter) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                final int position = viewHolder.getBindingAdapterPosition();
                if (adapter.getTransaction(position) != null) {
                    expense = adapter.getTransaction(position);
                }

                switch (swipeDir) {

                    case ItemTouchHelper.LEFT:
                        AddExpense ae = new AddExpense(Spending.this, expense);
                        ae.setSubmitExpenseListener(v -> {
                            adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
                            listExpenses();
                        });
                        break;

                    case ItemTouchHelper.RIGHT:
                        transaction = adapter.getTransaction(position);
                        FirebaseFirestore.getInstance().collection("users").document(transaction.getOwner()).collection("expenses").document(transaction.getId()).delete();
                        repo.getExpenses().remove(transaction);
                        String message = transaction.getDescription() + " " + getString(R.string.has_been_removed);
                        Button snackButton = Notify.createButtonPopup(Spending.this, message, getString(R.string.undo), null);
                        snackButton.setOnClickListener(v -> {
                            repo.getExpenses().add(transaction);
                            repo.getExpenses().sort(Comparator.comparing(Expense::getDate).reversed());
                            repo.saveData(Spending.this, (wasSuccessful, message1) -> listExpenses());
                        });
                        repo.saveData(Spending.this, (wasSuccessful, message1) -> {
                            adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
                            listExpenses();
                        });
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(Spending.this, R.color.primary))
                        .addSwipeLeftActionIcon(R.drawable.baseline_edit_note_24)
                        .setSwipeLeftActionIconTint(ContextCompat.getColor(Spending.this, R.color.white))
                        .setSwipeLeftLabelColor(ContextCompat.getColor(Spending.this, R.color.white))
                        .addSwipeLeftLabel(getString(R.string.edit_transaction))
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(Spending.this, R.color.red))
                        .addSwipeRightLabel(getString(R.string.delete_transaction))
                        .setSwipeRightActionIconTint(ContextCompat.getColor(Spending.this, R.color.white))
                        .setSwipeRightLabelColor(ContextCompat.getColor(Spending.this, R.color.white))
                        .addSwipeRightActionIcon(R.drawable.baseline_delete_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };

        return new ItemTouchHelper(simpleItemTouchCallback);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void adjustCategories(double dailyBudget) {

        Button submit = findViewById(R.id.submit_changes);
        TextView addCategory = findViewById(R.id.add_category);
        LinearLayout categoriesList = findViewById(R.id.categories_list);
        cats.setVisibility(View.GONE);
        categoriesListLayout.setVisibility(View.VISIBLE);
        ArrayList<Slider> sliders = new ArrayList<>();
        for (Category category : budget.getCategories()) {
            final int[] remainder = {0};
            View slider = View.inflate(Spending.this, R.layout.category_slider, null);
            EditText categoryName = slider.findViewById(R.id.category_name);
            TextView remove = slider.findViewById(R.id.remove_category);
            Slider categorySlider = slider.findViewById(R.id.category_slider);
            if (!sliders.contains(categorySlider)) {
                sliders.add(categorySlider);
            }
            TextView categoryPercent = slider.findViewById(R.id.category_percent);
            TextView categoryTotal = slider.findViewById(R.id.category_total);
            categoryName.setText(category.getCategoryName());
            categoryName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        category.setCategoryName(editable.toString());
                    }
                }
            });
            remove.setOnClickListener(view -> {
                AlertDialog dialog = new AlertDialog.Builder(Spending.this)
                        .setTitle(R.string.remove_category)
                        .setMessage(getString(R.string.are_you_sure_you_want_to_remove_this_category))
                        .setPositiveButton(getString(R.string.remove), (dialogInterface, i) -> {
                            budget.getCategories().remove(category);
                            categoriesList.removeView(slider);
                            sliders.remove(categorySlider);
                            FirebaseFirestore.getInstance().collection("users").document(repo.retrieveUid(Spending.this)).set(repo.getUser(Spending.this), SetOptions.merge());
                        })
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .create();
                dialog.show();
            });
            categorySlider.setValue(category.getCategoryPercentage());
            categoryPercent.setText(String.format(Locale.getDefault(), "%d%%", category.getCategoryPercentage()));
            categoryTotal.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf((dailyBudget * (category.getCategoryPercentage() / 100.0)) * 30))));
            categorySlider.addOnChangeListener((slider1, value, fromUser) -> {
                remainder[0] = getRemainder(sliders, categorySlider);
                if (categorySlider.getValue() > remainder[0]) {
                    categorySlider.setValue(remainder[0]);
                }
                categoryPercent.setText(String.format(Locale.getDefault(), "%d%%", (int) categorySlider.getValue()));
                categoryTotal.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyBudget * (categorySlider.getValue() / 100.0) * 30))));
                category.setCategoryPercentage((int) categorySlider.getValue());
            });
            if (categoriesList.getChildCount() > 0) {
                View divider = new View(Spending.this);
                divider.setMinimumHeight(3);
                divider.setMinimumWidth(categoriesList.getMeasuredWidth());
                divider.setBackgroundColor(getResources().getColor(R.color.lightGrey, getTheme()));
                categoriesList.addView(divider);
            }
            categoriesList.addView(slider);
        }
        submit.setOnClickListener(view -> {
            FirebaseFirestore.getInstance().collection("users").document(repo.retrieveUid(Spending.this)).set(repo.getUser(Spending.this), SetOptions.merge());
            cats.setVisibility(View.VISIBLE);
            categoriesListLayout.setVisibility(View.GONE);
            categoriesList.removeAllViews();
            if (catAdapter != null) {
                catAdapter.notifyDataSetChanged();
            }
        });
        addCategory.setOnClickListener(view -> {
            Category newCat = new Category("", 0);
            budget.getCategories().add(newCat);
            final int[] remainder = {0};
            View slider = View.inflate(Spending.this, R.layout.category_slider, null);
            EditText categoryName = slider.findViewById(R.id.category_name);
            TextView remove = slider.findViewById(R.id.remove_category);
            String newName = getString(R.string.miscellaneous);
            int counter = 1;
            for (Category category : budget.getCategories()) {
                while (category.getCategoryName().equalsIgnoreCase(newName)) {
                    newName = getString(R.string.miscellaneous) + " (" + counter + ")";
                    ++counter;
                }
            }
            categoryName.setText(newName);
            newCat.setCategoryName(newName);
            Slider categorySlider = slider.findViewById(R.id.category_slider);
            if (!sliders.contains(categorySlider)) {
                sliders.add(categorySlider);
            }
            TextView categoryPercent = slider.findViewById(R.id.category_percent);
            TextView categoryTotal = slider.findViewById(R.id.category_total);
            categoryName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        newCat.setCategoryName(editable.toString());
                    }
                }
            });
            remove.setOnClickListener(view1 -> {
                AlertDialog dialog = new AlertDialog.Builder(Spending.this)
                        .setTitle(getString(R.string.remove_category))
                        .setMessage(getString(R.string.are_you_sure_you_want_to_remove_this_category))
                        .setPositiveButton(getString(R.string.remove), (dialogInterface, i) -> {
                            budget.getCategories().remove(newCat);
                            categoriesList.removeView(slider);
                            FirebaseFirestore.getInstance().collection("users").document(repo.retrieveUid(Spending.this)).set(repo.getUser(Spending.this), SetOptions.merge());
                        })
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        })
                        .create();
                dialog.show();
            });
            categorySlider.setValue(5);
            categoryPercent.setText(String.format(Locale.getDefault(), "%d%%", newCat.getCategoryPercentage()));
            categoryTotal.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf((dailyBudget * (newCat.getCategoryPercentage() / 100.0)) * 30))));
            categorySlider.addOnChangeListener((slider1, value, fromUser) -> {
                remainder[0] = getRemainder(sliders, categorySlider);
                if (categorySlider.getValue() > remainder[0]) {
                    categorySlider.setValue(remainder[0]);
                }
                categoryPercent.setText(String.format(Locale.getDefault(), "%d%%", (int) categorySlider.getValue()));
                categoryTotal.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(dailyBudget * (categorySlider.getValue() / 100.0) * 30))));
                newCat.setCategoryPercentage((int) categorySlider.getValue());
            });
            if (categoriesList.getChildCount() > 0) {
                View divider = new View(Spending.this);
                divider.setMinimumHeight(3);
                divider.setMinimumWidth(categoriesList.getMeasuredWidth());
                divider.setBackgroundColor(getResources().getColor(R.color.lightGrey, getTheme()));
                categoriesList.addView(divider);
            }
            categoriesList.addView(slider);
        });
    }

    public int getRemainder(ArrayList<Slider> sliders, Slider chosenSlider) {

        float remainder = 100;
        for (Slider slider : sliders) {
            if (!slider.equals(chosenSlider)) {
                remainder = remainder - slider.getValue();
            }
        }
        if (remainder < 0) {
            remainder = 0;
        }
        return (int) remainder;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }

    public static class MyYAxisValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            return FixNumber.addSymbol(String.valueOf(value));  // Format value, and get any string here
        }
    }
}