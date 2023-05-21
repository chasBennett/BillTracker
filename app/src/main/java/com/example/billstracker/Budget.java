package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class Budget extends AppCompatActivity {

    String name, userName;
    Context mContext;
    LinearLayout navDrawer, hideNav, billsTab, expensesTab, budgetTab, viewBudget, editBudget, pb;
    ImageView drawerToggle, settingsButton, help, addBiller, payNext, exit;
    TextView displayUserName, displayEmail, navHome, navViewBillers, navPaymentHistory, myStats, ticketCounter, myAchievements, savingsAmount, billsAverage, disposableIncome, automotiveAmount, beautyAmount, clothingAmount,
            entertainmentAmount, groceriesAmount, healthAmount, restaurantAmount, otherAmount, budgetStartDate, budgetEndDate, categoriesError, savingsError, btnEditBudget, btnDaily, btnWeekly, btnMonthly, incomeView,
            savePercentage, saveAmount, spendingPercentage, spendingAmount, totalIncomeView, billsView;
    SharedPreferences sp;
    ArrayList<String> categories;
    EditText etPayAmount, etSavingsPercentage, etAutomotivePercentage, etBeautyPercentage, etClothingPercentage, etEntertainmentPercentage, etGroceriesPercentage,
            etHealthPercentage, etRestaurantsPercentage, etOtherPercentage;
    Spinner payFrequency;
    androidx.appcompat.widget.SwitchCompat createForMe;
    Button submit;
    double weeklyPay, weeklyBudget, weeklyBills, disposable, savings, dailyPay, dailyBudget, dailySavings;
    int weekStart, weekEnd, budgetId, counter;
    LocalDate startDate, endDate, selectedDate;
    Budgets bud;
    SaveUserData load = new SaveUserData();
    DateFormatter df = new DateFormatter();
    FixNumber fn = new FixNumber();
    ArrayList <Integer> values = new ArrayList<>();
    ScrollView scroll;
    boolean catError, saveError, firstRun;
    PieChart pieChart;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        etPayAmount = findViewById(R.id.etPayAmount);
        etSavingsPercentage = findViewById(R.id.etSavingsPercentage);
        etAutomotivePercentage = findViewById(R.id.etAutomotivePercentage);
        etBeautyPercentage = findViewById(R.id.etBeautyPercentage);
        etClothingPercentage = findViewById(R.id.etClothingPercentage);
        etEntertainmentPercentage = findViewById(R.id.etEntertainmentPercentage);
        etGroceriesPercentage = findViewById(R.id.etGroceriesPercentage);
        etHealthPercentage = findViewById(R.id.etHealthPercentage);
        etRestaurantsPercentage = findViewById(R.id.etRestaurantsPercentage);
        etOtherPercentage = findViewById(R.id.etOtherPercentage);
        totalIncomeView = findViewById(R.id.totalIncomeView);
        budgetStartDate = findViewById(R.id.budgetStartDate);
        budgetEndDate = findViewById(R.id.budgetEndDate);
        savingsAmount = findViewById(R.id.savingsAmount);
        billsAverage = findViewById(R.id.billsAverage);
        savingsError = findViewById(R.id.savingsError);
        billsView = findViewById(R.id.billsView);
        categoriesError = findViewById(R.id.categoriesError);
        scroll = findViewById(R.id.scrollView7);
        disposableIncome = findViewById(R.id.weeklyDisposableIncome);
        automotiveAmount = findViewById(R.id.automotiveAmount);
        beautyAmount = findViewById(R.id.beautyAmount);
        clothingAmount = findViewById(R.id.clothingAmount);
        entertainmentAmount = findViewById(R.id.entertainmentAmount);
        groceriesAmount = findViewById(R.id.groceriesAmount);
        healthAmount = findViewById(R.id.healthAmount);
        restaurantAmount = findViewById(R.id.restaurantsAmount);
        otherAmount = findViewById(R.id.otherAmount);
        payFrequency = findViewById(R.id.payFrequencySpinner);
        createForMe = findViewById(R.id.createForMe);
        submit = findViewById(R.id.submitBudget);

        pieChart = findViewById(R.id.pieChart3);
        viewBudget = findViewById(R.id.viewBudget);
        editBudget = findViewById(R.id.editBudget);
        incomeView = findViewById(R.id.incomeView);
        savePercentage = findViewById(R.id.savePercentage);
        spendingPercentage = findViewById(R.id.spendingPercentage);
        spendingAmount = findViewById(R.id.spendingAmount);
        saveAmount = findViewById(R.id.saveAmount);
        btnDaily = findViewById(R.id.btnDaily);
        btnWeekly = findViewById(R.id.btnWeekly);
        btnMonthly = findViewById(R.id.btnMonthly);
        btnEditBudget = findViewById(R.id.btnEditBudget);
        exit = findViewById(R.id.exitEditBudget);

        catError = false;
        saveError = false;

        editBudget.setVisibility(View.GONE);
        viewBudget.setVisibility(View.VISIBLE);
        setupPieChart();

        selectedDate = LocalDate.now();
        weekStart = df.calcDateValue(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = df.calcDateValue(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));

        boolean found = false;
        for (Budgets budget : thisUser.getBudgets()) {
            if (budget.getStartDate() <= df.calcDateValue(selectedDate) && budget.getEndDate() >= df.calcDateValue(selectedDate)) {
                budgetId = budget.getBudgetId();
                bud = budget;
                found = true;

            }
        }
        if (!found) {
            bud = new Budgets(thisUser.getIncome(), Integer.parseInt(thisUser.getPayFrequency()), weekStart, weekEnd, id(), 5,5,5,5,5,5,5,5, 5);
        }

        exit.setOnClickListener(v -> {
            editBudget.setVisibility(View.GONE);
            viewBudget.setVisibility(View.VISIBLE);
        });

        mContext = Budget.this;
        SaveUserData save = new SaveUserData();
        if (thisUser == null) {
            save.loadUserData(Budget.this);
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(thisUser.getUserName(), thisUser.getPassword());
        }

        btnEditBudget.setOnClickListener(v -> {
            viewBudget.setVisibility(View.GONE);
            editBudget.setVisibility(View.VISIBLE);
        });

        categories = new ArrayList<>(Arrays.asList(getString(R.string.automotive), getString(R.string.beauty), getString(R.string.clothing),
                getString(R.string.entertainment), getString(R.string.groceries), getString(R.string.health), getString(R.string.restaurants), getString(R.string.other)));
        userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        pb = findViewById(R.id.progressBar9);
        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //TOOLBAR, NAVDRAWER, and NAVTRAY

        //NAVTRAY

        billsTab = findViewById(R.id.billsTab);
        budgetTab = findViewById(R.id.budgetTab);
        expensesTab = findViewById(R.id.expensesTab);

        budgetTab.setBackgroundColor(getResources().getColor(R.color.fingerprint, getTheme()));
        billsTab.setOnClickListener(v -> {
            Intent main = new Intent(Budget.this, MainActivity2.class);
            startActivity(main);
        });
        expensesTab.setOnClickListener(v -> {
            Intent spend = new Intent(Budget.this, Spending.class);
            startActivity(spend);
        });
        budgetTab.setOnClickListener(v -> {

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
        navHome.setBackground(AppCompatResources.getDrawable(Budget.this, R.drawable.border_selected));

        //updates int value on support icon notification bubble
        CountTickets countTickets = new CountTickets();
        countTickets.countTickets(ticketCounter);

        myAchievements.setOnClickListener(v -> {
            Intent achievements = new Intent(Budget.this, AwardCase.class);
            startActivity(achievements);
        });

        help.setOnClickListener(view -> {
            Intent support = new Intent(Budget.this, Support.class);
            support.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pb.setVisibility(View.VISIBLE);
            startActivity(support);
        });

        myStats.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent stats = new Intent(Budget.this, MyStats.class);
            startActivity(stats);
        });

        addBiller.setOnClickListener(v -> {
            Intent createBudget = new Intent(Budget.this, CreateBudget.class);
            startActivity(createBudget);
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

        navDrawer.setOnTouchListener(new OnSwipeTouchListener(Budget.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                navDrawer.setVisibility(View.GONE);
            }
        });

        payNext.setOnClickListener(view -> {

            pb.setVisibility(View.VISIBLE);
            Payments next = new Payments();
            next.setPaymentDate(df.currentDateAsInt() + 60);
            boolean found1 = false;
            paymentInfo.getPayments().sort(Comparator.comparing(Payments::getPaymentDate));
                for (Payments payment : paymentInfo.getPayments()) {
                    if (!payment.isPaid() && payment.getPaymentDate() < next.getPaymentDate()) {
                        next = payment;
                        found1 = true;
                    }
                }

            if (found1) {
                Intent pay = new Intent(mContext, PayBill.class);
                pay.putExtra("Due Date", df.convertIntDateToString(next.getPaymentDate()));
                pay.putExtra("Biller Name", next.getBillerName());
                pay.putExtra("Amount Due", next.getPaymentAmount());
                pay.putExtra("Is Paid", next.isPaid());
                pay.putExtra("Payment Id", next.getPaymentId());
                pay.putExtra("Current Date", df.currentDateAsInt());
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

        counter = 0;

        if (found) {
            generateBudgetView(1);
        }

        startDate = df.convertIntDateToLocalDate(bud.getStartDate());
        endDate = df.convertIntDateToLocalDate(bud.getEndDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        budgetStartDate.setText(formatter.format(startDate));
        budgetEndDate.setText(formatter.format(endDate));

        firstRun = false;

        budgetStartDate.setOnClickListener(v -> getDateFromUser(budgetStartDate, true));
        budgetEndDate.setOnClickListener(v -> getDateFromUser(budgetEndDate, false));

        weekStart = df.calcDateValue(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = df.calcDateValue(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));

        if (thisUser == null) {
            load.loadUserData(Budget.this);
        }
        etPayAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(bud.getPayAmount()))));

        etPayAmount.addTextChangedListener(new MoneyInput(etPayAmount));

        etPayAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String[] spinnerArray = new String[]{getString(R.string.weekly), getString(R.string.biweekly), getString(R.string.monthly)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payFrequency.setAdapter(adapter);
        payFrequency.setSelection(bud.getPayFrequency());

        payFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        etSavingsPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getSavingsPercentage()));
        etAutomotivePercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getAutomotivePercentage()));
        etBeautyPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getBeautyPercentage()));
        etClothingPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getClothingPercentage()));
        etEntertainmentPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getEntertainmentPercentage()));
        etGroceriesPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getGroceriesPercentage()));
        etHealthPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getHealthPercentage()));
        etRestaurantsPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getRestaurantsPercentage()));
        etOtherPercentage.setText(String.format(Locale.getDefault(), "%d%%", bud.getOtherPercentage()));

        updateValues();

        createForMe.setChecked(false);
        createForMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                values.clear();
                if (etAutomotivePercentage.getText().toString().replaceAll("%", "").length() > 0 && !etAutomotivePercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etAutomotivePercentage.getText().toString().replaceAll("%", "")));
                }
                if (etBeautyPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etBeautyPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etBeautyPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etClothingPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etClothingPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etClothingPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etEntertainmentPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etEntertainmentPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etEntertainmentPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etGroceriesPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etGroceriesPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etGroceriesPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etHealthPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etHealthPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etHealthPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etOtherPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etOtherPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etOtherPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etRestaurantsPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etRestaurantsPercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etRestaurantsPercentage.getText().toString().replaceAll("%", "")));
                }
                autoBudget();
            }
            else {

                if (!values.isEmpty() && values.size() > 7) {
                    etAutomotivePercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(0)));
                    etBeautyPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(1)));
                    etClothingPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(2)));
                    etEntertainmentPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(3)));
                    etGroceriesPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(4)));
                    etHealthPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(5)));
                    etRestaurantsPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(6)));
                    etOtherPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(7)));
                }

                etAutomotivePercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etBeautyPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etClothingPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etEntertainmentPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etGroceriesPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etHealthPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etRestaurantsPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etOtherPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

                etAutomotivePercentage.setEnabled(true);
                etBeautyPercentage.setEnabled(true);
                etClothingPercentage.setEnabled(true);
                etEntertainmentPercentage.setEnabled(true);
                etGroceriesPercentage.setEnabled(true);
                etHealthPercentage.setEnabled(true);
                etRestaurantsPercentage.setEnabled(true);
                etOtherPercentage.setEnabled(true);
                values.clear();
                updateValues();
            }
        });

        etSavingsPercentage.addTextChangedListener(new PercentageInput(etSavingsPercentage, savingsAmount));
        etSavingsPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etAutomotivePercentage.addTextChangedListener(new PercentageInput(etAutomotivePercentage, automotiveAmount));
        etBeautyPercentage.addTextChangedListener(new PercentageInput(etBeautyPercentage, beautyAmount));
        etClothingPercentage.addTextChangedListener(new PercentageInput(etClothingPercentage, clothingAmount));
        etEntertainmentPercentage.addTextChangedListener(new PercentageInput(etEntertainmentPercentage, entertainmentAmount));
        etGroceriesPercentage.addTextChangedListener(new PercentageInput(etGroceriesPercentage, groceriesAmount));
        etHealthPercentage.addTextChangedListener(new PercentageInput(etHealthPercentage, healthAmount));
        etRestaurantsPercentage.addTextChangedListener(new PercentageInput(etRestaurantsPercentage, restaurantAmount));
        etOtherPercentage.addTextChangedListener(new PercentageInput(etOtherPercentage, automotiveAmount));

        etAutomotivePercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etBeautyPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etClothingPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etEntertainmentPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etGroceriesPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etHealthPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etOtherPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etRestaurantsPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submit.setOnClickListener(v -> {
            if (!catError && !saveError) {
                if (etPayAmount.length() > 1 && etSavingsPercentage.length() > 1 && etAutomotivePercentage.length() > 1 && etBeautyPercentage.length() > 1 && etClothingPercentage.length() > 1 && etEntertainmentPercentage.length() > 1 &&
                        etGroceriesPercentage.length() > 1 && etHealthPercentage.length() > 1 && etRestaurantsPercentage.length() > 1 && etOtherPercentage.length() > 1) {
                    createNewBudget();
                    generateBudgetView(1);
                } else {
                    Toast.makeText(Budget.this, getString(R.string.all_fields_must_be_filled_in), Toast.LENGTH_LONG).show();
                }
            }
        });
        btnDaily.setOnClickListener(v -> generateBudgetView(0));
        btnWeekly.setOnClickListener(v -> generateBudgetView(1));
        btnMonthly.setOnClickListener(v -> generateBudgetView(2));
        btnEditBudget.setOnClickListener(v -> {
            editBudget.setVisibility(View.VISIBLE);
            viewBudget.setVisibility(View.GONE);
        });
    }

    public void generateBudgetView (int period) {

        editBudget.setVisibility(View.GONE);
        viewBudget.setVisibility(View.VISIBLE);
        boolean found = false;
        for (Budgets budget : thisUser.getBudgets()) {
            if (budget.getStartDate() <= df.calcDateValue(selectedDate) && budget.getEndDate() >= df.calcDateValue(selectedDate)) {
                budgetId = budget.getBudgetId();
                bud = budget;
                found = true;

            }
        }
        int monthStart = df.calcDateValue(selectedDate.withDayOfMonth(1));
        int monthEnd = df.calcDateValue(selectedDate.withDayOfMonth(LocalDate.now().lengthOfMonth()));
        double monthlyBills = 0;
        double dailyBills;
        for (Payments payments: paymentInfo.getPayments()) {
            if (payments.getPaymentDate() >= monthStart && payments.getPaymentDate() <= monthEnd) {
                monthlyBills = monthlyBills + Double.parseDouble(payments.getPaymentAmount());
            }
        }
        dailyBills = monthlyBills / selectedDate.lengthOfMonth();
        if (!found) {
            bud = new Budgets(thisUser.getIncome(), Integer.parseInt(thisUser.getPayFrequency()), weekStart, weekEnd, id(), 5,5,5,5,5,5,5,5, 5);
        }

        double pay = bud.getPayAmount();
        int frequency = bud.getPayFrequency();
        double budPercentage = bud.getAutomotivePercentage() + bud.getBeautyPercentage() + bud.getClothingPercentage() + bud.getEntertainmentPercentage() + bud.getGroceriesPercentage() + bud.getHealthPercentage() +
                bud.getOtherPercentage() + bud.getRestaurantsPercentage();
        if (frequency == 0) {
            weeklyPay = pay;
        }
        else if (frequency == 1) {
            weeklyPay = (pay / 2);
        }
        else {
            weeklyPay = (pay / ((double) selectedDate.lengthOfMonth() / 7));
        }
        dailyPay = (weeklyPay / 7);
        dailySavings = (dailyPay) * ((double) bud.getSavingsPercentage() / 100);
        disposable = dailyPay - dailyBills - dailySavings;
        dailyBudget = disposable * (budPercentage / 100);
        double disposablePercent = disposable * 100 / dailyPay;
        double billsPercent = dailyBills * 100 / dailyPay;
        TextView disposablePercentage = findViewById(R.id.disposablePercentage);
        TextView billsPercentage = findViewById(R.id.billsPercentage);
        billsPercentage.setText(String.format(Locale.getDefault(), "%s%%", fn.makeDouble(String.valueOf(billsPercent))));
        disposablePercentage.setText(String.format(Locale.getDefault(), "%s%%", fn.makeDouble(String.valueOf(disposablePercent))));
        savePercentage.setText(String.format(Locale.getDefault(), "%s%%", fn.makeDouble(String.valueOf(bud.getSavingsPercentage()))));

        switch (period) {
            case 0:
                btnDaily.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                btnWeekly.setBackgroundTintList(null);
                btnMonthly.setBackgroundTintList(null);
                totalIncomeView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyPay))));
                billsView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyBills))));
                incomeView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable))));
                saveAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailySavings))));
                spendingAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyBudget))));
                break;
            case 1:
                btnDaily.setBackgroundTintList(null);
                btnWeekly.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                btnMonthly.setBackgroundTintList(null);
                totalIncomeView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyPay * 7))));
                billsView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyBills * 7))));
                incomeView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * 7))));
                saveAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailySavings * 7))));
                spendingAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyBudget * 7))));
                break;
            case 2:
                btnDaily.setBackgroundTintList(null);
                btnWeekly.setBackgroundTintList(null);
                btnMonthly.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
                totalIncomeView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyPay * selectedDate.lengthOfMonth()))));
                billsView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyBills * selectedDate.lengthOfMonth()))));
                incomeView.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * selectedDate.lengthOfMonth()))));
                saveAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailySavings * selectedDate.lengthOfMonth()))));
                spendingAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(dailyBudget * selectedDate.lengthOfMonth()))));
                break;
        }
        loadPieChartData(period);
    }

    public void setupPieChart() {
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(8);
        pieChart.canResolveTextAlignment();
        pieChart.setDrawEntryLabels(false);
        pieChart.setNoDataText(getString(R.string.noBudgetDataWasFound));
        pieChart.setEntryLabelColor(getResources().getColor(R.color.black, getTheme()));
        pieChart.setNoDataTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        pieChart.setExtraOffsets(5,0,0,0);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setDrawInside(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setEnabled(true);
    }

    public void loadPieChartData(int period) {
        ArrayList <PieEntry> entries = new ArrayList<>();
        double auto = (double) bud.getAutomotivePercentage() / 100, beauty = (double) bud.getBeautyPercentage() / 100, clothing = (double) bud.getClothingPercentage() / 100, entertainment =
                (double) bud.getEntertainmentPercentage() / 100, groceries = (double) bud.getGroceriesPercentage() / 100, health = (double) bud.getHealthPercentage() / 100, other = (double) bud.getOtherPercentage() / 100,
                restaurants = (double) bud.getRestaurantsPercentage() / 100;
        double totalPercent = auto * 100 + beauty * 100 + clothing * 100 + entertainment * 100 + groceries * 100 + health * 100 + other * 100 + restaurants * 100;
        double unassigned = 100 - totalPercent;
        double payAmount = 0;
        switch (period) {
            case 0:
                payAmount = disposable;
                break;
            case 1:
                payAmount = (disposable * 7);
                break;
            case 2:
                payAmount = (disposable * selectedDate.lengthOfMonth());
                break;
        }
        double autoAmount = auto * payAmount, beautyAmount = beauty * payAmount, clothingAmount = clothing * payAmount, entertainmentAmount = entertainment * payAmount, groceriesAmount = groceries * payAmount,
                healthAmount = health * payAmount, otherAmount = other * payAmount, restaurantsAmount = restaurants * payAmount;
        double total = autoAmount + beautyAmount + clothingAmount + entertainmentAmount + groceriesAmount + healthAmount + otherAmount + restaurantsAmount;

        if (auto != 0) {
            entries.add(new PieEntry((float) (auto), getString(R.string.automotive) + " " + fn.addSymbol(String.valueOf(autoAmount))));
        }
        if (beauty != 0) {
            entries.add(new PieEntry((float) (beauty), getString(R.string.beauty) + " " + fn.addSymbol(String.valueOf(beautyAmount))));
        }
        if (clothing != 0) {
            entries.add(new PieEntry((float) (clothing), getString(R.string.clothing) + " " + fn.addSymbol(String.valueOf(clothingAmount))));
        }
        if (entertainment != 0) {
            entries.add(new PieEntry((float) (entertainment), getString(R.string.entertainment) + " " + fn.addSymbol(String.valueOf(entertainmentAmount))));
        }
        if (groceries != 0) {
            entries.add(new PieEntry((float) (groceries), getString(R.string.groceries) + " " + fn.addSymbol(String.valueOf(groceriesAmount))));
        }
        if (health != 0) {
            entries.add(new PieEntry((float) (health), getString(R.string.health) + " " + fn.addSymbol(String.valueOf(healthAmount))));
        }
        if (other != 0) {
            entries.add(new PieEntry((float) (other), getString(R.string.other) + " " + fn.addSymbol(String.valueOf(otherAmount))));
        }
        if (restaurants != 0) {
            entries.add(new PieEntry((float) (restaurants), getString(R.string.restaurants) + " " + fn.addSymbol(String.valueOf(restaurantsAmount))));
        }
        if (unassigned > 0) {
            entries.add(new PieEntry((float) (unassigned / 100), getString(R.string.unassigned) + " " + fn.addSymbol(String.valueOf((unassigned / 100) * payAmount))));
        }

        ArrayList <Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color: Budget.this.getResources().getIntArray(R.array.pieChart)) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.total) + " " + fn.addSymbol(fn.makeDouble(String.valueOf(total))));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(8f);
        data.setValueTextColor(getResources().getColor(R.color.black, getTheme()));

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    public void addPercentages () {

        int auto = 0;
        int beauty = 0;
        int clothes = 0;
        int entertainment = 0;
        int groceries = 0;
        int health = 0;
        int other = 0;
        int restaurants = 0;

        if (etAutomotivePercentage.getText().length() > 1) {
            auto = Integer.parseInt(etAutomotivePercentage.getText().toString().replaceAll("%", ""));
        }
        if (etBeautyPercentage.getText().length() > 1) {
            beauty = Integer.parseInt(etBeautyPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etClothingPercentage.getText().length() > 1) {
            clothes = Integer.parseInt(etClothingPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etEntertainmentPercentage.getText().length() > 1) {
            entertainment = Integer.parseInt(etEntertainmentPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etGroceriesPercentage.getText().length() > 1) {
            groceries = Integer.parseInt(etGroceriesPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etHealthPercentage.getText().length() > 1) {
            health = Integer.parseInt(etHealthPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etOtherPercentage.getText().length() > 1) {
            other = Integer.parseInt(etOtherPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etRestaurantsPercentage.getText().length() > 1) {
            restaurants = Integer.parseInt(etRestaurantsPercentage.getText().toString().replaceAll("%", ""));
        }

        int total = auto + beauty + clothes + entertainment + groceries + health + other + restaurants;

        if (total > 100) {
            categoriesError.setVisibility(View.VISIBLE);
            catError = true;
        }
        else {
            categoriesError.setVisibility(View.GONE);
            catError = false;
        }

    }

    public void createNewBudget() {

        Budgets bud1 = bud;
        bud1.setPayAmount(Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString())));
        bud1.setPayFrequency(payFrequency.getSelectedItemPosition());
        bud1.setStartDate(df.calcDateValue(startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))));
        bud1.setEndDate(df.calcDateValue(endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))));
        bud1.setAutomotivePercentage(Integer.parseInt(etAutomotivePercentage.getText().toString().replaceAll("%", "")));
        bud1.setBeautyPercentage(Integer.parseInt(etBeautyPercentage.getText().toString().replaceAll("%", "")));
        bud1.setClothingPercentage(Integer.parseInt(etClothingPercentage.getText().toString().replaceAll("%", "")));
        bud1.setEntertainmentPercentage(Integer.parseInt(etEntertainmentPercentage.getText().toString().replaceAll("%", "")));
        bud1.setGroceriesPercentage(Integer.parseInt(etGroceriesPercentage.getText().toString().replaceAll("%", "")));
        bud1.setHealthPercentage(Integer.parseInt(etHealthPercentage.getText().toString().replaceAll("%", "")));
        bud1.setRestaurantsPercentage(Integer.parseInt(etRestaurantsPercentage.getText().toString().replaceAll("%", "")));
        bud1.setOtherPercentage(Integer.parseInt(etOtherPercentage.getText().toString().replaceAll("%", "")));
        bud1.setSavingsPercentage(Integer.parseInt(etSavingsPercentage.getText().toString().replaceAll("%", "")));
        thisUser.setIncome(Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString())));
        thisUser.setPayFrequency(String.valueOf(payFrequency.getSelectedItemPosition()));
        thisUser.getBudgets().remove(bud);
        thisUser.getBudgets().add(bud1);
        load.saveUserData(Budget.this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).set(thisUser, SetOptions.merge());
    }

    public void updateValues() {

        weeklyPay = 0;
        weeklyBills = 0;
        savings = 0;
        disposable = 0;
        weeklyBudget = 0;

        if (etPayAmount.getText().length() > 1) {
            switch (payFrequency.getSelectedItemPosition()) {
                case 0:
                    weeklyPay = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString()));
                    break;
                case 1:
                    weeklyPay = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString())) / 2;
                    break;
                case 2:
                    int numDays = LocalDate.now().lengthOfMonth();
                    weeklyPay = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString())) / (double) (numDays / 7);
                    break;
            }
        }

        int monthStart = df.calcDateValue(selectedDate.withDayOfMonth(1));
        int monthEnd = df.calcDateValue(selectedDate.withDayOfMonth(startDate.lengthOfMonth()));
            for (Payments payment: paymentInfo.getPayments()) {
                if (payment.getPaymentDate() >= monthStart && payment.getPaymentDate() <= monthEnd) {
                    weeklyBills = weeklyBills + Double.parseDouble(fn.makeDouble(payment.getPaymentAmount()));
                }
            }
        int daysInMonth = startDate.lengthOfMonth();
        weeklyBills = weeklyBills / ((double) daysInMonth / 7);
        billsAverage.setText(fn.addSymbol(String.valueOf(weeklyBills)));
        if (etSavingsPercentage.getText().toString().length() > 1) {
            savings = (weeklyPay) * (Double.parseDouble(etSavingsPercentage.getText().toString().replaceAll("%", "")) / 100);
        }
        disposable = (weeklyPay - weeklyBills) - savings;
        if (disposable < 0) {
            savingsError.setVisibility(View.VISIBLE);
            saveError = true;
        }
        else {
            savingsError.setVisibility(View.GONE);
            saveError = false;
        }
        savingsAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(savings))));
        disposableIncome.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable))));
        calculateAmounts();
    }

    @SuppressLint("SetTextI18n")
    public void autoBudget () {
        etAutomotivePercentage.setText("12%");
        etBeautyPercentage.setText("8%");
        etClothingPercentage.setText("9%");
        etEntertainmentPercentage.setText("8%");
        etGroceriesPercentage.setText("18%");
        etHealthPercentage.setText("20%");
        etRestaurantsPercentage.setText("10%");
        etOtherPercentage.setText("15%");

        etAutomotivePercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etBeautyPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etClothingPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etEntertainmentPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etGroceriesPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etHealthPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etRestaurantsPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etOtherPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));

        etAutomotivePercentage.setEnabled(false);
        etBeautyPercentage.setEnabled(false);
        etClothingPercentage.setEnabled(false);
        etEntertainmentPercentage.setEnabled(false);
        etGroceriesPercentage.setEnabled(false);
        etHealthPercentage.setEnabled(false);
        etRestaurantsPercentage.setEnabled(false);
        etOtherPercentage.setEnabled(false);

        calculateAmounts();
    }

    public void calculateAmounts () {
        if (!etAutomotivePercentage.getText().toString().equals("") && !etAutomotivePercentage.getText().toString().equals("%")) {
            automotiveAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etAutomotivePercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            automotiveAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etBeautyPercentage.getText().toString().equals("") && !etBeautyPercentage.getText().toString().equals("%")) {
            beautyAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etBeautyPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            beautyAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etClothingPercentage.getText().toString().equals("") && !etClothingPercentage.getText().toString().equals("%")) {
            clothingAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etClothingPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            clothingAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etEntertainmentPercentage.getText().toString().equals("") && !etEntertainmentPercentage.getText().toString().equals("%")) {
            entertainmentAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etEntertainmentPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            entertainmentAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etGroceriesPercentage.getText().toString().equals("") && !etGroceriesPercentage.getText().toString().equals("%")) {
            groceriesAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etGroceriesPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            groceriesAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etHealthPercentage.getText().toString().equals("") && !etHealthPercentage.getText().toString().equals("%")) {
            healthAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etHealthPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            healthAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etRestaurantsPercentage.getText().toString().equals("") && !etRestaurantsPercentage.getText().toString().equals("%")) {
            restaurantAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etRestaurantsPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            restaurantAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etOtherPercentage.getText().toString().equals("") && !etOtherPercentage.getText().toString().equals("%")) {
            otherAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etOtherPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            otherAmount.setText(fn.addSymbol("0.00"));
        }
    }

    public void updateWeek (LocalDate date) {
        startDate = date;
        weekStart = df.calcDateValue(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = df.calcDateValue(date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        updateValues();
    }

    public void getDateFromUser(TextView dueDate, boolean start) {

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        int day = today.getDayOfMonth();
        int year = today.getYear();
        int month = today.getMonthValue();

        DatePickerDialog datePicker;
        datePicker = new DatePickerDialog(Budget.this, R.style.MyDatePickerStyle, (datePicker1, i, i1, i2) -> {
            int fixMonth = i1 + 1;
            LocalDate date = LocalDate.of(i, fixMonth, i2);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            String startDate = formatter.format(date);
            dueDate.setText(startDate);
            if (start) {
                updateWeek(date);
            }
            else {
                endDate = date;
            }
        }, year, month - 1, day);
        datePicker.setTitle(getString(R.string.selectDate));
        datePicker.show();
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

        navDrawer.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
    }
}