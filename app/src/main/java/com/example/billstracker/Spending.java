package com.example.billstracker;

import static com.example.billstracker.Logon.expenseInfo;
import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.login.LoginManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.divider.MaterialDivider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import me.thanel.swipeactionview.SwipeActionView;
import me.thanel.swipeactionview.SwipeGestureListener;

public class Spending extends AppCompatActivity {

    String name, userName;
    int counter;
    Context mContext;
    LocalDate selectedDate, weekStart, weekEnd;
    LinearLayout navDrawer, hideNav, billsTab, expensesTab, budgetTab, pb, expensesList, confirmRemove;
    ImageView drawerToggle, settingsButton, help, addBiller, payNext;
    ConstraintLayout addExpense;
    TextView displayUserName, backMonth, forwardMonth, myStats, ticketCounter, totalSpent, displayEmail, navHome, navViewBillers, expenseDate, navPaymentHistory, selectedWeek, myAchievements, popupMessage, undoButton, createABudget, btnAddExpense;
    ScrollView main;
    EditText expenseDescription, expenseAmount;
    Spinner expenseCategory;
    Button submitExpense;
    BarChart barChart;
    SharedPreferences sp;
    DateFormatter dateFormatter = new DateFormatter();
    DateFormatter df = new DateFormatter();
    FixNumber fn = new FixNumber();
    ImageView close;
    ArrayList<String> categories;
    double totalBills;
    double weeklySavings;
    double budgetTotal;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spending);

        mContext = Spending.this;
        SaveUserData save = new SaveUserData();
        if (thisUser == null) {
            save.loadUserData(Spending.this);
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(thisUser.getUserName(), thisUser.getPassword());
        }

        if (selectedDate == null) {
            selectedDate = dateFormatter.convertIntDateToLocalDate(dateFormatter.currentDateAsInt()).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        }

        categories = new ArrayList<>(Arrays.asList(getString(R.string.automotive), getString(R.string.beauty), getString(R.string.clothing),
                getString(R.string.entertainment), getString(R.string.groceries), getString(R.string.health), getString(R.string.restaurants), getString(R.string.other)));
        userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        pb = findViewById(R.id.progressBar9);
        barChart = findViewById(R.id.barChart_view);
        main = findViewById(R.id.scroll7);
        expenseDescription = findViewById(R.id.expenseDescription);
        expenseAmount = findViewById(R.id.expenseAmount);
        expenseCategory = findViewById(R.id.expenseCategory);
        expenseDate = findViewById(R.id.expenseDate);
        close = findViewById(R.id.closeAddExpense);
        addExpense = findViewById(R.id.addExpense);
        confirmRemove = findViewById(R.id.confirmRemove);
        popupMessage = findViewById(R.id.popupMessage1);
        undoButton = findViewById(R.id.undoButton1);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        createABudget = findViewById(R.id.createABudget);
        submitExpense = findViewById(R.id.submitExpense);
        totalSpent = findViewById(R.id.totalSpent);
        selectedWeek = findViewById(R.id.selectedMonth);
        expensesList = findViewById(R.id.expensesList);
        backMonth = findViewById(R.id.backMonth);
        forwardMonth = findViewById(R.id.forwardMonth);
        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);

        addExpense.setVisibility(View.GONE);
        confirmRemove.setVisibility(View.GONE);

        btnAddExpense.setOnClickListener(v -> {
            addExpense.setVisibility(View.VISIBLE);
            main.setAlpha(.4f);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseCategory.setAdapter(adapter);

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        LocalDate date = LocalDate.now();
        expenseDate.setText(formatter.format(date));
        selectedDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        expenseDate.setOnClickListener(view -> getDateFromUser(expenseDate));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //TOOLBAR, NAVDRAWER, and NAVTRAY

        //NAVTRAY

        billsTab = findViewById(R.id.billsTab);
        budgetTab = findViewById(R.id.budgetTab);
        expensesTab = findViewById(R.id.expensesTab);

        expensesTab.setBackgroundColor(getResources().getColor(R.color.fingerprint, getTheme()));
        billsTab.setOnClickListener(v -> {
            Intent main = new Intent(Spending.this, MainActivity2.class);
            startActivity(main);
        });
        expensesTab.setOnClickListener(v -> {

        });
        budgetTab.setOnClickListener(v -> {
            if (thisUser.getBudgets().size() > 0) {
                Intent budget = new Intent(Spending.this, Budget.class);
                startActivity(budget);
            } else {
                Intent createBudget = new Intent(Spending.this, CreateBudget.class);
                startActivity(createBudget);
            }
        });
        //Toolbar
        drawerToggle = findViewById(R.id.drawerToggle);
        settingsButton = findViewById(R.id.settingsButton);
        payNext = findViewById(R.id.payNext);
        help = findViewById(R.id.helpMe);
        addBiller = findViewById(R.id.btnAddBiller);
        ticketCounter = findViewById(R.id.ticketCounter);

        //Navigation Drawer
        navDrawer = findViewById(R.id.navDrawer);
        hideNav = findViewById(R.id.hideNavDrawer);
        navHome = findViewById(R.id.navHome);
        navViewBillers = findViewById(R.id.navViewBillers);
        navPaymentHistory = findViewById(R.id.navPaymentHistory);
        myAchievements = findViewById(R.id.myAchievements);
        displayUserName = findViewById(R.id.tvName);
        displayEmail = findViewById(R.id.tvUserName2);
        myStats = findViewById(R.id.myStats);

        displayUserName.setText(thisUser.getName());
        displayEmail.setText(thisUser.getUserName());

        //Hide nav drawer on create
        navDrawer.setVisibility(View.GONE);
        navHome.setBackground(AppCompatResources.getDrawable(Spending.this, R.drawable.border_selected));

        //updates int value on support icon notification bubble
        CountTickets countTickets = new CountTickets();
        countTickets.countTickets(ticketCounter);

        myAchievements.setOnClickListener(v -> {
            Intent achievements = new Intent(Spending.this, AwardCase.class);
            startActivity(achievements);
        });

        help.setOnClickListener(view -> {
            Intent support = new Intent(Spending.this, Support.class);
            support.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pb.setVisibility(View.VISIBLE);
            startActivity(support);
        });

        myStats.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent stats = new Intent(Spending.this, MyStats.class);
            startActivity(stats);
        });

        addBiller.setOnClickListener(v -> {
            addExpense.setVisibility(View.VISIBLE);
            main.setAlpha(.4f);
        });
        settingsButton.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent settings = new Intent(mContext, Settings.class);
            startActivity(settings);
        });
        drawerToggle.setOnClickListener(view -> {
            if (navDrawer.getVisibility() == View.VISIBLE) {
                navDrawer.setVisibility(View.GONE);
            } else {
                navDrawer.setVisibility(View.VISIBLE);
                navDrawer.setFocusableInTouchMode(true);
                navDrawer.setClickable(true);
                hideNav.setOnClickListener(view1 -> navDrawer.setVisibility(View.GONE));
            }
        });

        navHome.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent home = new Intent(mContext, MainActivity2.class);
            startActivity(home);
        });

        navViewBillers.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent billers = new Intent(mContext, ViewBillers.class);
            startActivity(billers);
        });

        navPaymentHistory.setOnClickListener(view -> {
            Intent payments = new Intent(mContext, PaymentHistory.class);
            pb.setVisibility(View.VISIBLE);
            startActivity(payments);
        });

        navDrawer.setOnTouchListener(new OnSwipeTouchListener(Spending.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                navDrawer.setVisibility(View.GONE);
            }
        });

        payNext.setOnClickListener(view -> {

            pb.setVisibility(View.VISIBLE);
            Payments next = new Payments();
            next.setPaymentDate(dateFormatter.currentDateAsInt() + 60);
            boolean found = false;

            paymentInfo.getPayments().sort(Comparator.comparing(Payments::getPaymentDate));
            for (Payments payment : paymentInfo.getPayments()) {
                if (!payment.isPaid() && payment.getPaymentDate() < next.getPaymentDate()) {
                    next = payment;
                    found = true;
                }
            }

            if (found) {
                Intent pay = new Intent(mContext, PayBill.class);
                pay.putExtra("Due Date", dateFormatter.convertIntDateToString(next.getPaymentDate()));
                pay.putExtra("Biller Name", next.getBillerName());
                pay.putExtra("Amount Due", next.getPaymentAmount());
                pay.putExtra("Is Paid", next.isPaid());
                pay.putExtra("Payment Id", next.getPaymentId());
                pay.putExtra("Current Date", dateFormatter.currentDateAsInt());
                startActivity(pay);
            } else {
                pb.setVisibility(View.GONE);
                androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                alert.setTitle(getString(R.string.noBillsDue));
                alert.setMessage(getString(R.string.noUpcomingBills));
                alert.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {

                });
                androidx.appcompat.app.AlertDialog builder = alert.create();
                builder.show();
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        expenseAmount.setText(fn.addSymbol("0"));
        expenseAmount.addTextChangedListener(new MoneyInput(expenseAmount));

        close.setOnClickListener(v -> {
            addExpense.setVisibility(View.GONE);
            main.setAlpha(1f);
        });

        submitExpense.setOnClickListener(v -> {
            if (expenseDescription.getText() != null && expenseAmount.getText() != null && !expenseDescription.getText().toString().equals("") && !expenseAmount.getText().toString().equals("")) {
                Expenses a = new Expenses(expenseDescription.getText().toString(), expenseCategory.getSelectedItemPosition(), df.convertDateStringToInt(expenseDate.getText().toString()),
                        Double.parseDouble(fn.makeDouble(expenseAmount.getText().toString())));
                expenseInfo.getExpenses().add(a);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("expenses").document(uid).set(expenseInfo, SetOptions.merge());
                recreate();
            }
        });

        backMonth.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            selectedDate = selectedDate.minusWeeks(1);
            setCurrentWeek(selectedWeek);
            initBarChart();
            listExpenses();
            pb.setVisibility(View.GONE);
        });

        forwardMonth.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            selectedDate = selectedDate.plusWeeks(1);
            setCurrentWeek(selectedWeek);
            initBarChart();
            listExpenses();
            pb.setVisibility(View.GONE);
        });

        setCurrentWeek(selectedWeek);
        listExpenses();
        counter = 0;

        refreshUser();
        initBarChart();
    }

    private void listExpenses() {

        int start = df.calcDateValue(weekStart.withDayOfMonth(1));
        int end = df.calcDateValue(weekStart.withDayOfMonth(weekStart.lengthOfMonth()));
        totalBills = 0;
        int percentTotal;
        budgetTotal = 0;
        for (Payments payment : paymentInfo.getPayments()) {
            if (payment.getPaymentDate() >= start && payment.getPaymentDate() <= end) {
                totalBills = totalBills + Double.parseDouble(payment.getPaymentAmount());
            }
        }

        double weeklyPay = 0;
        double savingsPercentage;
        totalBills = totalBills / ((double) weekStart.lengthOfMonth() / 7);
        for (Budgets budget : thisUser.getBudgets()) {
            if (budget.getStartDate() >= df.calcDateValue(weekStart) && budget.getStartDate() <= df.calcDateValue(weekEnd) ||
                    budget.getStartDate() < df.calcDateValue(weekStart) && budget.getEndDate() >= df.calcDateValue(weekStart) && budget.getEndDate() <= df.calcDateValue(weekEnd) ||
                    budget.getStartDate() < df.calcDateValue(weekStart) && budget.getEndDate() > df.calcDateValue(weekEnd)) {
                percentTotal = budget.getAutomotivePercentage() + budget.getBeautyPercentage() + budget.getClothingPercentage() + budget.getEntertainmentPercentage() + budget.getGroceriesPercentage() + budget.getHealthPercentage() +
                        budget.getRestaurantsPercentage() + budget.getOtherPercentage();
                savingsPercentage = budget.getSavingsPercentage() / 100.0;
                if (budget.getPayFrequency() == 0) {
                    weeklyPay = budget.getPayAmount();
                } else if (budget.getPayFrequency() == 1) {
                    weeklyPay = budget.getPayAmount() / 2;
                } else if (budget.getPayFrequency() == 2) {
                    int days = weekStart.lengthOfMonth();
                    int weeks = days / 7;
                    weeklyPay = budget.getPayAmount() / weeks;
                }
                weeklySavings = (weeklyPay) * savingsPercentage;
                budgetTotal = (weeklyPay - totalBills - weeklySavings) * (percentTotal / 100.0);
            }
        }
        counter = 0;
        int startWeek = df.calcDateValue(weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        int endWeek = df.calcDateValue(weekStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        expensesList.removeAllViews();
        expensesList.invalidate();
        double expenseTotal = 0;

        ArrayList<Integer> headers = new ArrayList<>();
        expenseInfo.getExpenses().sort(Comparator.comparing(Expenses::getDate).reversed());
        for (Expenses expense : expenseInfo.getExpenses()) {
            if (expense.getDate() >= startWeek && expense.getDate() <= endWeek) {
                if (!headers.contains(expense.getDate())) {
                    View header = View.inflate(Spending.this, R.layout.date_header, null);
                    TextView dateValue = header.findViewById(R.id.expenseDateHeader);
                    dateValue.setText(df.convertIntDateToString(expense.getDate()));
                    expensesList.addView(header);
                    headers.add(expense.getDate());
                }
                ++counter;
                View view = View.inflate(Spending.this, R.layout.transaction, null);
                TextView description = view.findViewById(R.id.viewExpenseDescription);
                TextView category = view.findViewById(R.id.viewExpenseCategory);
                TextView amount = view.findViewById(R.id.viewExpenseAmount);
                description.setText(expense.getDescription());
                category.setText(categories.get(expense.getCategory()));
                amount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(expense.getAmount()))));
                expenseTotal = expenseTotal + expense.getAmount();
                expensesList.addView(view);
                swipeViewListener(view, expense);
                MaterialDivider divider = new MaterialDivider(Spending.this);
                expensesList.addView(divider);
            }
        }

        if (counter == 0) {
            View notFound = View.inflate(Spending.this, R.layout.no_expenses_found, null);
            expensesList.addView(notFound);
        }
        if (budgetTotal > 0) {
            createABudget.setText(fn.addSymbol(fn.makeDouble(String.valueOf(budgetTotal - expenseTotal))));
            createABudget.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
            createABudget.setEnabled(false);
        } else {
            createABudget.setText(R.string.create_a_new_budget);
            createABudget.setTextColor(getResources().getColor(R.color.blue, getTheme()));
            createABudget.setEnabled(true);
            createABudget.setOnClickListener(v -> {
                Intent createBudget = new Intent(Spending.this, CreateBudget.class);
                startActivity(createBudget);
            });
        }
    }

    public void swipeViewListener(View view, Expenses expense) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SwipeActionView swipeView;
        swipeView = view.findViewById(R.id.expenseSwipeView);
        final boolean[] changed = {false};
        swipeView.setSwipeGestureListener(new SwipeGestureListener() {
            @Override
            public boolean onSwipedLeft(@NonNull SwipeActionView swipeActionView) {
                return false;
            }

            @Override
            public boolean onSwipedRight(@NonNull SwipeActionView swipeActionView) {
                Expenses remove = null;
                for (Expenses expenses : expenseInfo.getExpenses()) {
                    if (expenses == expense) {
                        remove = expense;
                        break;
                    }
                }
                if (remove != null) {
                    confirmRemove.setVisibility(View.VISIBLE);
                    popupMessage.setText(String.format(Locale.getDefault(), "%s %s %s", getString(R.string.transaction_titled), expense.getDescription(), getString(R.string.has_been_removed)));
                    expenseInfo.getExpenses().remove(remove);
                    changed[0] = true;
                    Expenses finalRemove = remove;
                    undoButton.setOnClickListener(v -> {
                        changed[0] = false;
                        expenseInfo.getExpenses().add(finalRemove);
                        confirmRemove.setVisibility(View.GONE);
                        listExpenses();
                    });
                    swipeView.postDelayed(() -> {
                        confirmRemove.setVisibility(View.GONE);
                        if (changed[0]) {
                            db.collection("expenses").document(uid).set(expenseInfo, SetOptions.merge());
                        }
                    }, 6000);
                }
                listExpenses();
                return true;
            }

            @Override
            public void onSwipeLeftComplete(@NonNull SwipeActionView swipeActionView) {

            }

            @Override
            public void onSwipeRightComplete(@NonNull SwipeActionView swipeActionView) {

            }
        });
    }

    private void initBarChart() {

        barChart.invalidate();
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawBorders(false);

        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);

        barChart.animateY(600);
        barChart.animateX(600);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setTextSize(6);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        legend.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        showBarChart();

    }

    private void showBarChart() {

        int start = df.calcDateValue(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        int end = df.calcDateValue(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<BarEntry> entries1 = new ArrayList<>();
        double largest = 0;

        for (int i = 0; i < categories.size(); ++i) {
            double spent = 0.0;
            if (expenseInfo.getExpenses() != null) {
                for (Expenses expense : expenseInfo.getExpenses()) {
                    if (expense.getCategory() == i && expense.getDate() >= start && expense.getDate() <= end) {
                        spent = spent + expense.getAmount();
                    }
                }
            }
            if (spent == 0) {
                spent = .1;
            }
            if (spent > largest) {
                largest = spent;
            }
            entries.add(new BarEntry(i, (float) spent));
        }
        if (thisUser.getBudgets() != null) {
            for (Budgets budget : thisUser.getBudgets()) {
                if (budget.getStartDate() >= df.calcDateValue(weekStart) && budget.getStartDate() <= df.calcDateValue(weekEnd) ||
                        budget.getStartDate() < df.calcDateValue(weekStart) && budget.getEndDate() >= df.calcDateValue(weekStart) && budget.getEndDate() <= df.calcDateValue(weekEnd) ||
                        budget.getStartDate() < df.calcDateValue(weekStart) && budget.getEndDate() > df.calcDateValue(weekEnd)) {
                    ArrayList<Double> entry = new ArrayList<>(Arrays.asList(budgetTotal * budget.getAutomotivePercentage() / 100, budgetTotal * budget.getBeautyPercentage() / 100, budgetTotal *
                            budget.getClothingPercentage() / 100, budgetTotal * budget.getEntertainmentPercentage() / 100, budgetTotal * budget.getGroceriesPercentage() / 100, budgetTotal *
                            budget.getHealthPercentage() / 100, budgetTotal * budget.getOtherPercentage() / 100, budgetTotal * budget.getRestaurantsPercentage() / 100));
                    entries1.add(new BarEntry(0, (float) budgetTotal * budget.getAutomotivePercentage() / 100));
                    entries1.add(new BarEntry(1, (float) budgetTotal * budget.getBeautyPercentage() / 100));
                    entries1.add(new BarEntry(2, (float) budgetTotal * budget.getClothingPercentage() / 100));
                    entries1.add(new BarEntry(3, (float) budgetTotal * budget.getEntertainmentPercentage() / 100));
                    entries1.add(new BarEntry(4, (float) budgetTotal * budget.getGroceriesPercentage() / 100));
                    entries1.add(new BarEntry(5, (float) budgetTotal * budget.getHealthPercentage() / 100));
                    entries1.add(new BarEntry(6, (float) budgetTotal * budget.getOtherPercentage() / 100));
                    entries1.add(new BarEntry(7, (float) budgetTotal * budget.getRestaurantsPercentage() / 100));
                    double max = Collections.max(entry);
                    if (max > largest) {
                        largest = max;
                    }
                    break;
                }
            }
        }
        BarDataSet barDataSet = new BarDataSet(entries, getString(R.string.spent));
        BarDataSet barDataSet1 = new BarDataSet(entries1, getString(R.string.total_budget));
        barDataSet.setColor(getResources().getColor(R.color.payBill, getTheme()));
        barDataSet1.setColor(getResources().getColor(R.color.button, getTheme()));
        barDataSet1.setDrawValues(false);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(categories));

        if (largest == 0) {
            largest = 50;
        }
        largest = largest + 10;
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(((float) largest));
        barChart.getAxisRight().setAxisMinimum(0f);
        barChart.getAxisRight().setAxisMaximum(((float) largest));

        float groupSpace = 0.16f;
        float barSpace = 0.01f; // x2 dataset
        float barWidth = 0.41f; // x2 dataset

        BarData data = new BarData(barDataSet1, barDataSet);
        data.setBarWidth(barWidth); // set the width of each bar
        barChart.setData(data);
        barChart.groupBars(-.5f, groupSpace, barSpace); // perform the "explicit" grouping
        barChart.setScaleEnabled(false);
        barDataSet.setDrawValues(false);
        barChart.invalidate(); // refresh

    }

    public void setCurrentWeek(TextView selectedMonth) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault());
        selectedMonth.setText(String.format(Locale.getDefault(), "%s - %s", dtf.format(selectedDate), dtf.format(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))));
        weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        weekEnd = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        int start = df.calcDateValue(weekStart);
        int end = df.calcDateValue(weekEnd);
        double total = 0;
        for (Expenses expense : expenseInfo.getExpenses()) {
            if (expense.getDate() >= start && expense.getDate() <= end) {
                total = total + expense.getAmount();
            }
        }
        totalSpent.setText(fn.addSymbol(String.valueOf(total)));
    }

    public void refreshUser() {

        SaveUserData save = new SaveUserData();
        save.loadUserData(Spending.this);
    }

    public void getDateFromUser(TextView dueDate) {

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        int day = today.getDayOfMonth();
        int year = today.getYear();
        int month = today.getMonthValue();

        DatePickerDialog datePicker;
        datePicker = new DatePickerDialog(Spending.this, R.style.MyDatePickerStyle, (datePicker1, i, i1, i2) -> {
            int fixMonth = i1 + 1;
            LocalDate date = LocalDate.of(i, fixMonth, i2);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            String startDate = formatter.format(date);
            dueDate.setText(startDate);
        }, year, month - 1, day);
        datePicker.setTitle(getString(R.string.selectDate));
        datePicker.show();
    }

    public void logout(View view) {

        Context mContext = this;
        GoogleSignIn.getClient(Spending.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        LoginManager.getInstance().logOut();
        SharedPreferences sp = mContext.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("Stay Signed In", false);
        editor.putString("Username", "");
        editor.putString("Password", "");
        editor.apply();
        Intent validate = new Intent(mContext, Logon.class);
        validate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        validate.putExtra("Welcome", true);
        recreate();
        startActivity(validate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navDrawer.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }
}