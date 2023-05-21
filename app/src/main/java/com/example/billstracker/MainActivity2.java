package com.example.billstracker;

import static com.example.billstracker.Logon.billList;
import static com.example.billstracker.Logon.checkTrophies;
import static com.example.billstracker.Logon.expenseInfo;
import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;
import static com.example.billstracker.R.layout.main_activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.facebook.login.LoginManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import me.thanel.swipeactionview.SwipeActionView;
import me.thanel.swipeactionview.SwipeGestureListener;

public class MainActivity2 extends AppCompatActivity {

    static String channelId;
    String name, userName;
    int month, year, day, counter, todayDateValue, sunday;
    ArrayList<Payments> dueThisMonth = new ArrayList<>();
    Context mContext;
    long delay;
    boolean even;
    public static LocalDate selectedDate;
    LinearLayout noResults;
    LinearLayout navDrawer;
    LinearLayout hideNav;
    LinearLayout billsList1;
    LinearLayout hideForNavDrawer;
    LinearLayout billsTab, expensesTab, budgetTab;
    LinearLayout pb;
    LinearLayout paymentConfirm;
    LinearLayout trophyContainer, todayList, laterThisWeekList, laterThisMonthList, earlierThisMonthList;
    ImageView drawerToggle, settingsButton, help, addBiller, payNext;
    TextView displayUserName, displayEmail, navHome, navViewBillers, navPaymentHistory, selectedMonth, backMonth, forwardMonth, admin, myStats, ticketCounter, popupMessage, myAchievements;
    ScrollView scroll;
    SharedPreferences sp;
    DateFormatter dateFormatter = new DateFormatter();
    CalculateBalance cb = new CalculateBalance();
    ArrayList<Payments> today = new ArrayList<>();
    boolean previousMonth;
    FixNumber fn = new FixNumber();
    LinearLayout billsList;
    ArrayList<Payments> earlier = new ArrayList<>(), laterThisWeek = new ArrayList<>(), later = new ArrayList<>();
    double earlyTotal = 0, todayTotal = 0, laterThisWeekTotal = 0, laterTotal = 0;
    LineChart lineChart;
    double max;
    BillerManager bm = new BillerManager();

    public static boolean isVisible(final View view) {

        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }
        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);
        final Rect screen = new Rect(0, 0, Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels);
        return actualPosition.intersects(screen.left, screen.top, screen.right, screen.bottom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(main_activity);

        SaveUserData save = new SaveUserData();
        if (thisUser == null) {
            save.loadUserData(MainActivity2.this);
        }
        if (paymentInfo == null) {
            paymentInfo = new PaymentInfo(new ArrayList<>());
        }
        Set<Payments> pay = new LinkedHashSet<>(paymentInfo.getPayments());
        paymentInfo.getPayments().clear();
        paymentInfo.getPayments().addAll(pay);
        for (Payments payments : paymentInfo.getPayments()) {
            if (!payments.isPaid() && payments.getPaymentDate() < dateFormatter.currentDateAsInt() + 14) {
                scheduleNotifications(payments);
            }
        }
        bm.refreshPayments(LocalDate.now(ZoneId.systemDefault()));
        initialize();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initialize() {

        mContext = MainActivity2.this;
        SaveUserData save = new SaveUserData();
        if (paymentInfo.getPayments() == null) {
            paymentInfo.setPayments(new ArrayList<>());
        }
        if (thisUser == null) {
            save.loadUserData(MainActivity2.this);
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(thisUser.getUserName(), thisUser.getPassword());
        }
        userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        uid = FirebaseAuth.getInstance().getUid();
        pb = findViewById(R.id.progressBar);
        even = false;
        selectedMonth = findViewById(R.id.selectedMonth);
        backMonth = findViewById(R.id.backMonth);
        lineChart = findViewById(R.id.lineChart);
        trophyContainer = findViewById(R.id.trophyContainer);
        forwardMonth = findViewById(R.id.forgotPasswordHeader);
        todayList = findViewById(R.id.todayList);
        laterThisWeekList = findViewById(R.id.laterThisWeekList);
        laterThisMonthList = findViewById(R.id.laterThisMonthList);
        earlierThisMonthList = findViewById(R.id.earlierThisMonthList);
        delay = 2000;
        billsList1 = findViewById(R.id.billsList1);
        noResults = findViewById(R.id.noResults);
        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        admin = findViewById(R.id.admin);
        hideForNavDrawer = findViewById(R.id.hideForNavDrawer);
        scroll = findViewById(R.id.scroll);
        paymentConfirm = findViewById(R.id.paymentConfirm);
        popupMessage = findViewById(R.id.popupMessage);
        previousMonth = false;

        if (selectedDate == null) {
            selectedDate = dateFormatter.convertIntDateToLocalDate(dateFormatter.currentDateAsInt());
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //TOOLBAR, NAVDRAWER, and NAVTRAY

        //NAVTRAY
        billsTab = findViewById(R.id.billsTab);
        budgetTab = findViewById(R.id.budgetTab);
        expensesTab = findViewById(R.id.expensesTab);

        billsTab.setBackgroundColor(getResources().getColor(R.color.fingerprint, getTheme()));
        billsTab.setOnClickListener(v -> {

        });
        expensesTab.setOnClickListener(v -> {
            Intent spending = new Intent(MainActivity2.this, Spending.class);
            startActivity(spending);
        });
        budgetTab.setOnClickListener(v -> {
            if (thisUser.getBudgets().size() > 0) {
                Intent budget = new Intent(MainActivity2.this, Budget.class);
                startActivity(budget);
            } else {
                Intent createBudget = new Intent(MainActivity2.this, CreateBudget.class);
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

        //Hide nav drawer on create
        navDrawer.setVisibility(View.GONE);
        navHome.setBackground(AppCompatResources.getDrawable(MainActivity2.this, R.drawable.border_selected));

        //updates int value on support icon notification bubble
        CountTickets countTickets = new CountTickets();
        countTickets.countTickets(ticketCounter);

        myAchievements.setOnClickListener(v -> {
            Intent achievements = new Intent(MainActivity2.this, AwardCase.class);
            startActivity(achievements);
        });

        help.setOnClickListener(view -> {
            Intent support = new Intent(MainActivity2.this, Support.class);
            support.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pb.setVisibility(View.VISIBLE);
            startActivity(support);
        });

        myStats.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent stats = new Intent(MainActivity2.this, MyStats.class);
            startActivity(stats);
        });

        addBiller.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent addBiller = new Intent(mContext, AddBiller.class);
            startActivity(addBiller);
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

        navDrawer.setOnTouchListener(new OnSwipeTouchListener(MainActivity2.this) {
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

        selectedMonth.setOnClickListener(v -> {
            MonthYearPickerDialog pd = new MonthYearPickerDialog();
            pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            pd.setListener((view, year, month, dayOfMonth) -> {
                pb.setVisibility(View.VISIBLE);
                selectedDate = selectedDate.withMonth(month).withYear(year);
                bm.refreshPayments(selectedDate);
                setCurrentMonth(selectedMonth);
                previousMonth = true;
                listBills();
                setupLineChart();
                pb.setVisibility(View.GONE);
            });
        });


        if (thisUser.getAdmin()) {
            admin.setEnabled(true);
            admin.setOnClickListener(view -> {
                Intent admin = new Intent(MainActivity2.this, Administrator.class);
                startActivity(admin);
            });
        } else {
            admin.setEnabled(false);
        }

        CheckTrophies check = new CheckTrophies();
        check.checkTrophies(MainActivity2.this, trophyContainer);
        checkTrophies = false;

        backMonth.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            selectedDate = selectedDate.minusMonths(1);
            bm.refreshPayments(selectedDate);
            setCurrentMonth(selectedMonth);
            previousMonth = true;
            listBills();
            setupLineChart();
            pb.setVisibility(View.GONE);
        });

        forwardMonth.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            selectedDate = selectedDate.plusMonths(1);
            bm.refreshPayments(selectedDate);
            setCurrentMonth(selectedMonth);
            listBills();
            setupLineChart();
            pb.setVisibility(View.GONE);
        });

        createNotificationChannel();
        getValues();
        getCurrentMonth();
        counter = 0;

        refreshUser();
        dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
        listBills();
        setupLineChart();
    }

    private void setupLineChart() {

        lineChart.invalidate();

        max = 0;

        ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
        LineDataSet lineDataSet = new LineDataSet(lineChartDataSet(), getString(R.string.bills));
        iLineDataSets.add(lineDataSet);
        lineDataSet.setColor(getResources().getColor(R.color.button, getTheme()));
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(2);
        lineDataSet.setDrawValues(false);

        if (thisUser.getBudgets() != null && !thisUser.getBudgets().isEmpty()) {
            LineDataSet lineDataSet1 = new LineDataSet(spendingData(), getString(R.string.spending));
            iLineDataSets.add(lineDataSet1);
            lineDataSet1.setLineWidth(2);
            lineDataSet1.setColor(getResources().getColor(R.color.payBill, getTheme()));
            lineDataSet1.setDrawCircles(false);
            lineDataSet1.setDrawValues(false);

        }

        if (thisUser.getBudgets() != null && !thisUser.getBudgets().isEmpty()) {
            LineDataSet lineDataSet2 = new LineDataSet(BudgetData(), getString(R.string.budget));
            iLineDataSets.add(lineDataSet2);
            lineDataSet2.setLineWidth(2);
            lineDataSet2.setColor(getResources().getColor(R.color.green, getTheme()));
            lineDataSet2.setDrawCircles(false);
            lineDataSet2.setDrawValues(false);

        }

        Legend legend = lineChart.getLegend();
        legend.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0);
        if (max != 0) {
            leftAxis.setAxisMaximum((float) max);
        }
        leftAxis.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setAxisMinimum(0);
        if (max != 0) {
            rightAxis.setAxisMaximum((float) max);
        }
        rightAxis.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

        Description description = new Description();
        description.setEnabled(false);
        lineChart.setDescription(description);

        LineData lineData = new LineData(iLineDataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);

        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawMarkers(false);
        lineChart.setNoDataText(getString(R.string.noDataAvailable));

        LocalDate startDate = selectedDate.minusMonths(3).withDayOfMonth(1);
        ArrayList<String> months = new ArrayList<>();
        int counter = 1;
        while (counter < 8) {
            months.add(startDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            startDate = startDate.plusMonths(1).withDayOfMonth(1);
            ++counter;
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

    }

    private ArrayList<Entry> spendingData() {

        ArrayList<Entry> dataSet = new ArrayList<>();
        double month1 = 0;
        double month2 = 0;
        double month3 = 0;
        double month4 = 0;
        double month5 = 0;
        double month6 = 0;
        double month7 = 0;

        for (Expenses spend : expenseInfo.getExpenses()) {
            if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(3).withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(3)
                    .withDayOfMonth(selectedDate.minusMonths(3).lengthOfMonth()))) {
                month1 = month1 + spend.getAmount();
            } else if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(2).withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(2)
                    .withDayOfMonth(selectedDate.minusMonths(2).lengthOfMonth()))) {
                month2 = month2 + spend.getAmount();
            } else if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(1).withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(1)
                    .withDayOfMonth(selectedDate.minusMonths(1).lengthOfMonth()))) {
                month3 = month3 + spend.getAmount();
            } else if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()))) {
                month4 = month4 + spend.getAmount();
            } else if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(1).withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(1)
                    .withDayOfMonth(selectedDate.plusMonths(1).lengthOfMonth()))) {
                month5 = month5 + spend.getAmount();
            } else if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(2).withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(2)
                    .withDayOfMonth(selectedDate.plusMonths(2).lengthOfMonth()))) {
                month6 = month6 + spend.getAmount();
            } else if (spend.getDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(3).withDayOfMonth(1)) && spend.getDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(3)
                    .withDayOfMonth(selectedDate.plusMonths(3).lengthOfMonth()))) {
                month7 = month7 + spend.getAmount();
            }
        }

        ArrayList<Double> monthsList = new ArrayList<>(Arrays.asList(month1, month2, month3, month4, month5, month6, month7));
        if (Collections.max(monthsList) > max) {
            max = Collections.max(monthsList);
            max = max + (max * .4);
        }

        dataSet.add(new Entry(0, (float) month1));
        dataSet.add(new Entry(1, (float) month2));
        dataSet.add(new Entry(2, (float) month3));
        dataSet.add(new Entry(3, (float) month4));
        dataSet.add(new Entry(4, (float) month5));
        dataSet.add(new Entry(5, (float) month6));
        dataSet.add(new Entry(6, (float) month7));
        return dataSet;

    }

    private ArrayList<Entry> BudgetData() {

        ArrayList<Entry> dataSet = new ArrayList<>();
        double month1 = 0;
        double month2 = 0;
        double month3 = 0;
        double month4 = 0;
        double month5 = 0;
        double month6 = 0;
        double month7 = 0;

        int totalDays;
        double pay;
        double savings;

        for (Budgets budget : thisUser.getBudgets()) {
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(3).withDayOfMonth(selectedDate.minusMonths(3).lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(3).withDayOfMonth(selectedDate.minusMonths(3).lengthOfMonth()))) {
                totalDays = selectedDate.minusMonths(3).lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(3).withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(3).withDayOfMonth(selectedDate.minusMonths(3).lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = (disposable * (budget.getAutomotivePercentage() / 100.0)) + (disposable * (budget.getBeautyPercentage() / 100.0)) + (disposable * (budget.getClothingPercentage() / 100.0)) +
                        (disposable * (budget.getEntertainmentPercentage() / 100.0)) + (disposable * (budget.getGroceriesPercentage() / 100.0)) + (disposable * (budget.getHealthPercentage() / 100.0)) +
                        (disposable * (budget.getOtherPercentage() / 100.0)) + (disposable * (budget.getRestaurantsPercentage() / 100.0));
                if (month > month1) {
                    month1 = month;
                }
            }
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(2).withDayOfMonth(selectedDate.minusMonths(2).lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(2).withDayOfMonth(selectedDate.minusMonths(2).lengthOfMonth()))) {
                totalDays = selectedDate.minusMonths(2).lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(2).withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(2).withDayOfMonth(selectedDate.minusMonths(2).lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = disposable * (budget.getAutomotivePercentage() / 100.0) + disposable * (budget.getBeautyPercentage() / 100.0) + disposable * (budget.getClothingPercentage() / 100.0) + disposable * (budget.getEntertainmentPercentage() / 100.0) +
                        disposable * (budget.getGroceriesPercentage() / 100.0) + disposable * (budget.getHealthPercentage() / 100.0) + disposable * (budget.getOtherPercentage() / 100.0) + disposable * (budget.getRestaurantsPercentage() / 100.0);
                if (month > month2) {
                    month2 = month;
                }
            }
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(1).withDayOfMonth(selectedDate.minusMonths(1).lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(1).withDayOfMonth(selectedDate.minusMonths(1).lengthOfMonth()))) {
                totalDays = selectedDate.minusMonths(1).lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(1).withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(1).withDayOfMonth(selectedDate.minusMonths(1).lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = disposable * (budget.getAutomotivePercentage() / 100.0) + disposable * (budget.getBeautyPercentage() / 100.0) + disposable * (budget.getClothingPercentage() / 100.0) + disposable * (budget.getEntertainmentPercentage() / 100.0) +
                        disposable * (budget.getGroceriesPercentage() / 100.0) + disposable * (budget.getHealthPercentage() / 100.0) + disposable * (budget.getOtherPercentage() / 100.0) + disposable * (budget.getRestaurantsPercentage() / 100.0);
                if (month > month3) {
                    month3 = month;
                }
            }
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()))) {
                totalDays = selectedDate.lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = disposable * (budget.getAutomotivePercentage() / 100.0) + disposable * (budget.getBeautyPercentage() / 100.0) + disposable * (budget.getClothingPercentage() / 100.0) + disposable * (budget.getEntertainmentPercentage() / 100.0) +
                        disposable * (budget.getGroceriesPercentage() / 100.0) + disposable * (budget.getHealthPercentage() / 100.0) + disposable * (budget.getOtherPercentage() / 100.0) + disposable * (budget.getRestaurantsPercentage() / 100.0);
                if (month > month4) {
                    month4 = month;
                }
            }
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(1).withDayOfMonth(selectedDate.plusMonths(1).lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(1).withDayOfMonth(selectedDate.plusMonths(1).lengthOfMonth()))) {
                totalDays = selectedDate.plusMonths(1).lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(1).withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(1).withDayOfMonth(selectedDate.plusMonths(1).lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = disposable * (budget.getAutomotivePercentage() / 100.0) + disposable * (budget.getBeautyPercentage() / 100.0) + disposable * (budget.getClothingPercentage() / 100.0) + disposable * (budget.getEntertainmentPercentage() / 100.0) +
                        disposable * (budget.getGroceriesPercentage() / 100.0) + disposable * (budget.getHealthPercentage() / 100.0) + disposable * (budget.getOtherPercentage() / 100.0) + disposable * (budget.getRestaurantsPercentage() / 100.0);
                if (month > month5) {
                    month5 = month;
                }
            }
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(2).withDayOfMonth(selectedDate.plusMonths(2).lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(2).withDayOfMonth(selectedDate.plusMonths(2).lengthOfMonth()))) {
                totalDays = selectedDate.plusMonths(2).lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(2).withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(2).withDayOfMonth(selectedDate.plusMonths(2).lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = disposable * (budget.getAutomotivePercentage() / 100.0) + disposable * (budget.getBeautyPercentage() / 100.0) + disposable * (budget.getClothingPercentage() / 100.0) + disposable * (budget.getEntertainmentPercentage() / 100.0) +
                        disposable * (budget.getGroceriesPercentage() / 100.0) + disposable * (budget.getHealthPercentage() / 100.0) + disposable * (budget.getOtherPercentage() / 100.0) + disposable * (budget.getRestaurantsPercentage() / 100.0);
                if (month > month6) {
                    month6 = month;
                }
            }
            if (budget.getStartDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(3).withDayOfMonth(selectedDate.plusMonths(3).lengthOfMonth())) &&
                    budget.getEndDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(3).withDayOfMonth(selectedDate.plusMonths(3).lengthOfMonth()))) {
                totalDays = selectedDate.plusMonths(3).lengthOfMonth();
                double bills = 0;
                for (Payments payment: paymentInfo.getPayments()) {
                    if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(3).withDayOfMonth(1)) &&
                            payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(3).withDayOfMonth(selectedDate.plusMonths(3).lengthOfMonth()))) {
                        bills = bills + Double.parseDouble(fn.makeDouble(String.valueOf(payment.getPaymentAmount())));
                    }
                }
                if (budget.getPayFrequency() == 0) {
                    pay = budget.getPayAmount() * (totalDays / 7.0);
                }
                else if (budget.getPayFrequency() == 1) {
                    pay = budget.getPayAmount() * (totalDays / 14.0);
                }
                else {
                    pay = budget.getPayAmount();
                }
                savings = (budget.getSavingsPercentage() / 100.0) * pay;
                double disposable = pay - savings - bills;
                double month = disposable * (budget.getAutomotivePercentage() / 100.0) + disposable * (budget.getBeautyPercentage() / 100.0) + disposable * (budget.getClothingPercentage() / 100.0) + disposable * (budget.getEntertainmentPercentage() / 100.0) +
                        disposable * (budget.getGroceriesPercentage() / 100.0) + disposable * (budget.getHealthPercentage() / 100.0) + disposable * (budget.getOtherPercentage() / 100.0) + disposable * (budget.getRestaurantsPercentage() / 100.0);
                if (month > month7) {
                    month7 = month;
                }
            }
        }

        ArrayList<Double> monthsList = new ArrayList<>(Arrays.asList(month1, month2, month3, month4, month5, month6, month7));
        if (Collections.max(monthsList) > max) {
            max = Collections.max(monthsList);
            max = max + (max * .4);
        }

        dataSet.add(new Entry(0, (float) month1));
        dataSet.add(new Entry(1, (float) month2));
        dataSet.add(new Entry(2, (float) month3));
        dataSet.add(new Entry(3, (float) month4));
        dataSet.add(new Entry(4, (float) month5));
        dataSet.add(new Entry(5, (float) month6));
        dataSet.add(new Entry(6, (float) month7));
        return dataSet;

    }


    private ArrayList<Entry> lineChartDataSet() {
        ArrayList<Entry> dataSet = new ArrayList<>();
        double month1 = 0;
        double month2 = 0;
        double month3 = 0;
        double month4 = 0;
        double month5 = 0;
        double month6 = 0;
        double month7 = 0;

        for (Payments payment : paymentInfo.getPayments()) {
            if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(3).withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(3)
                    .withDayOfMonth(selectedDate.minusMonths(3).lengthOfMonth()))) {
                month1 = month1 + Double.parseDouble(payment.getPaymentAmount());
            } else if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(2).withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(2)
                    .withDayOfMonth(selectedDate.minusMonths(2).lengthOfMonth()))) {
                month2 = month2 + Double.parseDouble(payment.getPaymentAmount());
            } else if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.minusMonths(1).withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.minusMonths(1)
                    .withDayOfMonth(selectedDate.minusMonths(1).lengthOfMonth()))) {
                month3 = month3 + Double.parseDouble(payment.getPaymentAmount());
            } else if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()))) {
                month4 = month4 + Double.parseDouble(payment.getPaymentAmount());
            } else if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(1).withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(1)
                    .withDayOfMonth(selectedDate.plusMonths(1).lengthOfMonth()))) {
                month5 = month5 + Double.parseDouble(payment.getPaymentAmount());
            } else if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(2).withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(2)
                    .withDayOfMonth(selectedDate.plusMonths(2).lengthOfMonth()))) {
                month6 = month6 + Double.parseDouble(payment.getPaymentAmount());
            } else if (payment.getPaymentDate() >= dateFormatter.calcDateValue(selectedDate.plusMonths(3).withDayOfMonth(1)) && payment.getPaymentDate() <= dateFormatter.calcDateValue(selectedDate.plusMonths(3)
                    .withDayOfMonth(selectedDate.plusMonths(3).lengthOfMonth()))) {
                month7 = month7 + Double.parseDouble(payment.getPaymentAmount());
            }
        }
        ArrayList<Double> monthsList = new ArrayList<>(Arrays.asList(month1, month2, month3, month4, month5, month6, month7));
        if (Collections.max(monthsList) > max) {
            max = Collections.max(monthsList);
            max = max + (max * .4);
        }

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0);
        if (max != 0) {
            leftAxis.setAxisMaximum((float) max);
        }

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setAxisMinimum(0);
        if (max != 0) {
            rightAxis.setAxisMaximum((float) max);
        }

        dataSet.add(new Entry(0, (float) month1));
        dataSet.add(new Entry(1, (float) month2));
        dataSet.add(new Entry(2, (float) month3));
        dataSet.add(new Entry(3, (float) month4));
        dataSet.add(new Entry(4, (float) month5));
        dataSet.add(new Entry(5, (float) month6));
        dataSet.add(new Entry(6, (float) month7));
        return dataSet;
    }

    public void getValues() {

        displayUserName.setText(thisUser.getName());
        displayEmail.setText(thisUser.getUserName());
        if (thisUser.getName().contains(" ")) {
            if (thisUser.getName().length() > 2 && thisUser.getName().indexOf(' ') != 0) {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s%s", getString(R.string.good), dateFormatter.currentPhaseOfDay(MainActivity2.this), thisUser.getName().substring(0, 1).toUpperCase(), thisUser.getName().substring(1, thisUser.getName().indexOf(' '))));
            } else {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s", getString(R.string.good), dateFormatter.currentPhaseOfDay(MainActivity2.this), thisUser.getName().toUpperCase()));
            }
        } else {
            if (thisUser.getName().length() > 1) {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s%s", getString(R.string.good), dateFormatter.currentPhaseOfDay(MainActivity2.this), thisUser.getName().substring(0, 1).toUpperCase(), thisUser.getName().substring(1)));
            } else {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s", getString(R.string.good), dateFormatter.currentPhaseOfDay(MainActivity2.this), thisUser.getName().toUpperCase()));
            }
        }

    }

    public void getCurrentMonth() {

        if (selectedDate != null) {
            month = selectedDate.getMonth().getValue();
            day = 1;
            year = selectedDate.getYear();
        } else {
            LocalDateTime loginTime = LocalDateTime.now();
            DateTimeFormatter formattedMonth = DateTimeFormatter.ofPattern("MM", Locale.getDefault());
            month = Integer.parseInt(loginTime.format(formattedMonth));
            DateTimeFormatter formattedYear = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault());
            year = Integer.parseInt(loginTime.format(formattedYear));
            DateTimeFormatter formattedDay = DateTimeFormatter.ofPattern("dd", Locale.getDefault());
            day = Integer.parseInt(loginTime.format(formattedDay));
        }
        setCurrentMonth(selectedMonth);
    }

    public void setCurrentMonth(TextView selectedMonth) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        selectedMonth.setText(dtf.format(selectedDate));
    }

    public void listBills() {

        todayList.removeAllViews();
        todayList.invalidate();
        laterThisWeekList.removeAllViews();
        laterThisWeekList.invalidate();
        laterThisMonthList.removeAllViews();
        laterThisMonthList.invalidate();
        earlierThisMonthList.removeAllViews();
        earlierThisMonthList.invalidate();
        billList = thisUser.getBills();
        billsList = findViewById(R.id.billsList1);
        dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
        LinearLayout billsList = findViewById(R.id.billsList1);
        sunday = dateFormatter.calcDateValue(dateFormatter.convertIntDateToLocalDate(dateFormatter.currentDateAsInt()).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        todayDateValue = dateFormatter.currentDateAsInt();
        counter = 0;

        billsList.animate().translationY(0).setDuration(500);
        admin.animate().translationX(0).setDuration(500);

        if (dueThisMonth.size() > 0) {
            noResults.setVisibility(View.GONE);
            for (Payments due : dueThisMonth) {
                counter = counter + 1;
                if (due.isPaid()) {
                    earlier.add(due);
                    earlyTotal = earlyTotal + Double.parseDouble(fn.makeDouble(due.getPaymentAmount()));
                } else if (due.getPaymentDate() == todayDateValue || !due.isPaid() && due.getPaymentDate() < todayDateValue) {
                    today.add(due);
                    if (due.getPartialPayment() > 0) {
                        todayTotal = todayTotal + Double.parseDouble(fn.makeDouble(String.valueOf(Double.parseDouble(due.getPaymentAmount()) - due.getPartialPayment())));
                    }
                    else {
                        todayTotal = todayTotal + Double.parseDouble(fn.makeDouble(due.getPaymentAmount()));
                    }
                } else if (due.getPaymentDate() <= sunday + 7) {
                    laterThisWeek.add(due);
                    if (due.getPartialPayment() > 0) {
                        laterThisWeekTotal = laterThisWeekTotal + Double.parseDouble(fn.makeDouble(String.valueOf(Double.parseDouble(due.getPaymentAmount()) - due.getPartialPayment())));
                    }
                    else {
                        laterThisWeekTotal = laterThisWeekTotal + Double.parseDouble(fn.makeDouble(due.getPaymentAmount()));
                    }
                } else {
                    later.add(due);
                    if (due.getPartialPayment() > 0) {
                        laterTotal = laterTotal + Double.parseDouble(fn.makeDouble(String.valueOf(Double.parseDouble(due.getPaymentAmount()) - due.getPartialPayment())));
                    }
                    else {
                        laterTotal = laterTotal + Double.parseDouble(fn.makeDouble(due.getPaymentAmount()));
                    }
                }
            }
            earlier.sort(Comparator.comparingInt(Payments::getDatePaid));
            today.sort(new PaymentsComparator());
            laterThisWeek.sort(new PaymentsComparator());
            later.sort(new PaymentsComparator());

            generateBillBoxes();

            ConstraintLayout header = findViewById(R.id.linearLayout3);
            header.animate().translationX(0).setDuration(500);
            billsList.animate().translationY(0).setDuration(500);

        } else {
            noResults.setVisibility(View.VISIBLE);
            todayList.setVisibility(View.GONE);
            laterThisWeekList.setVisibility(View.GONE);
            laterThisMonthList.setVisibility(View.GONE);
            earlierThisMonthList.setVisibility(View.GONE);
        }
        previousMonth = false;
        dueThisMonth.clear();
        earlier.clear();
        today.clear();
        laterThisWeek.clear();
        later.clear();
        earlyTotal = 0;
        todayTotal = 0;
        laterThisWeekTotal = 0;
        laterTotal = 0;
    }

    public void generateBillBoxes() {

        boolean earlyHeader = false, laterThisWeekHeader = false, todayHeader = false, laterHeader = false;
        todayList.setVisibility(View.VISIBLE);
        laterThisWeekList.setVisibility(View.VISIBLE);
        laterThisMonthList.setVisibility(View.VISIBLE);
        earlierThisMonthList.setVisibility(View.VISIBLE);

        for (Payments td : today) {

            todayList.setClipToOutline(true);
            if (!todayHeader) {
                View header = buildHeader(todayTotal, getString(R.string.today));
                todayHeader = true;
                todayList.addView(header);
            }
            billBoxValues("Today", td);
        }
        for (Payments late : laterThisWeek) {

            laterThisWeekList.setClipToOutline(true);
            if (!laterThisWeekHeader) {
                View header = buildHeader(laterThisWeekTotal, getString(R.string.laterThisWeek));
                laterThisWeekHeader = true;
                laterThisWeekList.addView(header);
            }
            billBoxValues("LaterThisWeek", late);
        }
        for (Payments late : later) {

            laterThisMonthList.setClipToOutline(true);
            if (!laterHeader) {
                View header = buildHeader(laterTotal, getString(R.string.laterThisMonth));
                laterHeader = true;
                laterThisMonthList.addView(header);
            }
            billBoxValues("LaterThisMonth", late);
        }

        for (Payments early : earlier) {

            earlierThisMonthList.setClipToOutline(true);
            if (earlier.size() > 0 && !earlyHeader) {
                View header = buildHeader(earlyTotal, getString(R.string.earlierThisMonth));
                earlyHeader = true;
                earlierThisMonthList.addView(header);
            }
            billBoxValues("EarlierThisMonth", early);
        }
        if (!todayHeader) {
            todayList.setVisibility(View.GONE);
        }
        if (!laterThisWeekHeader) {
            laterThisWeekList.setVisibility(View.GONE);
        }
        if (!laterHeader) {
            laterThisMonthList.setVisibility(View.GONE);
        }
        if (!earlyHeader) {
            earlierThisMonthList.setVisibility(View.GONE);
        }
    }

    public void swipeViewListener(View view, Payments payment) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        view.setClipToOutline(true);
        boolean paid = false;
        String billerName = payment.getBillerName();
        SwipeActionView swipeView;
        TextView paidBox;
        if (!previousMonth) {
            swipeView = view.findViewById(R.id.paidSwipeView);
        } else {
            swipeView = view.findViewById(R.id.paidSwipeView1);
        }
        paidBox = view.findViewById(R.id.markAsPaidBox);
        if (payment.isPaid()) {
            paid = true;
            paidBox.setText(R.string.unmarkAsPaid);
            paidBox.setBackgroundColor(getResources().getColor(R.color.red, getTheme()));
        } else {
            paidBox.setText(R.string.markAsPaid);
            paidBox.setBackgroundColor(getResources().getColor(R.color.payBill, getTheme()));
        }

        boolean finalPaid = paid;
        swipeView.setSwipeGestureListener(new SwipeGestureListener() {
            @Override
            public boolean onSwipedLeft(@NonNull SwipeActionView swipeActionView) {
                for (Bill bill : billList) {
                    if (bill.getBillerName().equals(payment.getBillerName())) {
                        Intent history = new Intent(MainActivity2.this, PaymentHistory.class);
                        history.putExtra("User Id", thisUser.getid());
                        history.putExtra("Bill Id", bill.getBillsId());
                        startActivity(history);
                    }
                }
                return true;
            }

            @Override
            public boolean onSwipedRight(@NonNull SwipeActionView swipeActionView) {
                if (!finalPaid) {

                    payment.setPaid(true);
                    payment.setDatePaid(dateFormatter.currentDateAsInt());
                            for (Bill bill : thisUser.getBills()) {
                                if (bill.getBillerName().equals(payment.getBillerName())) {
                                    bill.setPaymentsRemaining(String.valueOf(Integer.parseInt(bill.getPaymentsRemaining()) - 1));
                                    bill.setDateLastPaid(dateFormatter.currentDateAsInt());
                                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    nm.cancel(payment.getPaymentId());
                                    nm.cancel(payment.getPaymentId() + 1);
                                    nm.cancel(payment.getPaymentId() + 11);
                                }
                            }
                    db.collection("payments").document(uid).set(paymentInfo, SetOptions.merge());
                    bm.savePayments();
                    if (thisUser != null) {
                        SaveUserData save = new SaveUserData();
                        save.saveUserData(MainActivity2.this);
                    }
                    dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
                    listBills();
                    popupMessage.setText(String.format(Locale.getDefault(), "%s %s %s", getString(R.string.paymentFor), payment.getBillerName(), getString(R.string.markedAsPaid)));
                    paymentConfirm.setVisibility(View.VISIBLE);
                    paymentConfirm.postDelayed(() -> {
                        paymentConfirm.setVisibility(View.GONE);
                    }, 5000);
                    paymentConfirm.setOnClickListener(view12 -> {

                        payment.setPaid(false);
                        payment.setDatePaid(0);
                                for (Bill bill : thisUser.getBills()) {
                                    if (bill.getBillerName().equals(billerName)) {
                                        bill.setPaymentsRemaining(String.valueOf(Integer.parseInt(bill.getPaymentsRemaining()) + 1));
                                        scheduleNotifications(payment);
                                        int highest = 0;
                                        for (Payments pay : paymentInfo.getPayments()) {
                                            if (pay.getBillerName().equals(billerName) && pay.isPaid() && pay.getDatePaid() > highest) {
                                                highest = pay.getDatePaid();
                                            }
                                        }
                                        bill.setDateLastPaid(highest);
                                    }
                                }
                        dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
                        listBills();
                        paymentConfirm.setVisibility(View.GONE);
                    });

                } else {
                    int datePaid = payment.getDatePaid();
                    payment.setPaid(false);
                    payment.setDatePaid(0);
                            for (Bill bill : thisUser.getBills()) {
                                if (bill.getBillerName().equals(payment.getBillerName())) {
                                    bill.setPaymentsRemaining(String.valueOf(Integer.parseInt(bill.getPaymentsRemaining()) + 1));
                                    scheduleNotifications(payment);
                                    int highest = 0;
                                    for (Payments pay : paymentInfo.getPayments()) {
                                        if (pay.getBillerName().equals(payment.getBillerName()) && pay.isPaid() && pay.getDatePaid() > highest) {
                                            highest = pay.getDatePaid();
                                        }
                                    }
                                    bill.setDateLastPaid(highest);
                                }
                            }
                    db.collection("payments").document(uid).set(paymentInfo, SetOptions.merge());
                    bm.savePayments();
                    if (thisUser != null) {
                        SaveUserData save = new SaveUserData();
                        save.saveUserData(MainActivity2.this);
                    }
                    dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
                    listBills();
                    popupMessage.setText(String.format(Locale.getDefault(), "%s %s %s", getString(R.string.paymentFor), payment.getBillerName(), getString(R.string.unmarkedAsPaid)));
                    paymentConfirm.setVisibility(View.VISIBLE);
                    paymentConfirm.postDelayed(() -> {
                        paymentConfirm.setVisibility(View.GONE);
                    }, 5000);
                    paymentConfirm.setOnClickListener(view1 -> {
                        payment.setPaid(true);
                        payment.setDatePaid(datePaid);
                                for (Bill bill : thisUser.getBills()) {
                                    if (bill.getBillerName().equals(billerName)) {
                                        bill.setPaymentsRemaining(String.valueOf(Integer.parseInt(bill.getPaymentsRemaining()) - 1));
                                        bill.setDateLastPaid(datePaid);
                                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        nm.cancel(payment.getPaymentId());
                                        nm.cancel(payment.getPaymentId() + 1);
                                        nm.cancel(payment.getPaymentId() + 11);
                                        db.collection("payments").document(uid).set(paymentInfo, SetOptions.merge());
                                        bm.savePayments();
                                        if (thisUser != null) {
                                            SaveUserData save = new SaveUserData();
                                            save.saveUserData(MainActivity2.this);
                                        }
                                    }
                                }
                        dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
                        listBills();
                        paymentConfirm.setVisibility(View.GONE);
                    });
                }
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

    private View buildHeader(double todayTotal, String timePeriod) {

        View header = View.inflate(MainActivity2.this, R.layout.bill_box_header, null);
        TextView timeFrame = header.findViewById(R.id.timeFrame), totalDue = header.findViewById(R.id.totalDue);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 100, 0, 100);
        timeFrame.setText(timePeriod);
        timeFrame.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        totalDue.setText(fn.addSymbol(String.valueOf(todayTotal)));
        totalDue.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        header.setLayoutParams(lp);
        return header;
    }

    private void billBoxValues(String type, Payments payment) {

        View billBox;
        ShapeableImageView icon;
        ImageView arrow;
        Bill bil = new Bill();
        TextView billerName, amountDue, dueDate, finalPayment, viewDetailsLabel, payMade, totalPaid, payRemain, amountRemain, estRate, escAmount, estBalance, partPaymentMade, estInterestPaid;
        LinearLayout status, billBoxSelect, viewDetails, detailedView, payRemainLayout, amountRemainLayout, estRateLayout, escAmountLayout, estBalanceLayout, partPaymentLayout, estInterestPaidLayout;

        if (!previousMonth) {
            billBox = View.inflate(MainActivity2.this, R.layout.bill_box, null);
        } else {
            billBox = View.inflate(MainActivity2.this, R.layout.bill_box2, null);
        }
        icon = billBox.findViewById(R.id.billIcon);
        billerName = billBox.findViewById(R.id.tvBillerName);
        amountDue = billBox.findViewById(R.id.amountDue);
        dueDate = billBox.findViewById(R.id.tvDueDate);
        status = billBox.findViewById(R.id.status);
        billBoxSelect = billBox.findViewById(R.id.billBoxSelect);
        estRateLayout = billBox.findViewById(R.id.estRateLayout);
        escAmountLayout = billBox.findViewById(R.id.escAmountLayout);
        estBalanceLayout = billBox.findViewById(R.id.estBalanceLayout);
        estInterestPaidLayout = billBox.findViewById(R.id.estInterestPaidLayout);
        payMade = billBox.findViewById(R.id.payMade);
        totalPaid = billBox.findViewById(R.id.totalPaid);
        payRemain = billBox.findViewById(R.id.payRemain);
        amountRemain = billBox.findViewById(R.id.amountRemain);
        payRemainLayout = billBox.findViewById(R.id.payRemainLayout);
        amountRemainLayout = billBox.findViewById(R.id.amountRemainLayout);
        partPaymentLayout = billBox.findViewById(R.id.partPaymentLayout);
        partPaymentMade = billBox.findViewById(R.id.partPaymentMade);
        estRate = billBox.findViewById(R.id.estRate);
        escAmount = billBox.findViewById(R.id.escAmount);
        estBalance = billBox.findViewById(R.id.estBalance);
        estInterestPaid = billBox.findViewById(R.id.estInterestPaid);
        viewDetails = billBox.findViewById(R.id.btnViewDetails);
        detailedView = billBox.findViewById(R.id.detailedView);
        arrow = billBox.findViewById(R.id.arrowViewDetails);
        viewDetailsLabel = billBox.findViewById(R.id.viewDetailsLabel);
        final boolean[] showing = {false};
        finalPayment = billBox.findViewById(R.id.finalPayment);
        int finalPay = 0;
        double partPayment = 0;
        int paymentsMadeCounter = 0;
        double totalPaidCounter = 0.0;
        int paymentRemaining = 0;
        double amountRemaining = 0.0;
        double interestPaid = 0;
        double rate = 0;
        double escrow = 0;
        double balance = 0;

        for (Bill bill : thisUser.getBills()) {
            if (bill.getBillerName().equals(payment.getBillerName())) {
                bil = bill;
                if (!bill.getPaymentsRemaining().equals("1000")) {
                    for (Payments pay : paymentInfo.getPayments()) {
                        if (pay.getBillerName().equals(bill.getBillerName()) && pay.getPaymentDate() > finalPay && !pay.isPaid()) {
                            finalPay = pay.getPaymentDate();
                        }
                    }
                }
                LoadIcon loadIcon = new LoadIcon();
                loadIcon.loadIcon(MainActivity2.this, icon, bill.getCategory(), bill.getIcon());
                break;
            }
        }

        if (bil.getPaymentsRemaining() != null) {
            if (Integer.parseInt(bil.getPaymentsRemaining()) < 400) {
                paymentRemaining = Integer.parseInt(bil.getPaymentsRemaining());
                amountRemaining = Double.parseDouble(bil.getAmountDue()) * paymentRemaining;
                if (bil.getBalance() > 0) {
                    balance = cb.calculateNewBalance(bil, paymentInfo.getPayments());
                    rate = cb.calculateRate(bil);
                    interestPaid = cb.interestPaid(bil, paymentInfo.getPayments());
                }
                if (bil.getEscrow() > 0) {
                    escrow = bil.getEscrow();
                }
            }
        }
        for (Payments pay : paymentInfo.getPayments()) {
            if (pay.isPaid() && pay.getBillerName().equals(bil.getBillerName())) {
                paymentsMadeCounter = paymentsMadeCounter + 1;
                totalPaidCounter = totalPaidCounter + Double.parseDouble(pay.getPaymentAmount());
            }
            else if (pay.getPartialPayment() > 0 && pay.getBillerName().equals(bil.getBillerName())) {
                partPayment = pay.getPartialPayment();
                totalPaidCounter = totalPaidCounter + partPayment;
            }
        }

        int finalPaymentsMadeCounter = paymentsMadeCounter;
        double finalTotalPaidCounter = totalPaidCounter;
        int finalPaymentRemaining = paymentRemaining;
        double finalAmountRemaining = amountRemaining;
        double finalRate = rate;
        double finalEscrow = escrow;
        double finalBalance = balance;
        double finalInterestPaid = interestPaid;
        double finalPartPayment = partPayment;
        viewDetails.setOnClickListener(v -> {
            if (!showing[0]) {
                showing[0] = true;
                arrow.animate().rotation(-180);
                detailedView.setVisibility(View.VISIBLE);
                viewDetailsLabel.setText(getString(R.string.hide_details));
                payMade.setText(String.valueOf(finalPaymentsMadeCounter));
                totalPaid.setText(fn.addSymbol(fn.makeDouble(String.valueOf(finalTotalPaidCounter))));
                if (finalPaymentRemaining > 0) {
                    payRemainLayout.setVisibility(View.VISIBLE);
                    payRemain.setText(String.valueOf(finalPaymentRemaining));
                }
                if (finalAmountRemaining > 0) {
                    amountRemainLayout.setVisibility(View.VISIBLE);
                    amountRemain.setText(fn.addSymbol(fn.makeDouble(String.valueOf(finalAmountRemaining))));
                }
                if (finalRate > 0) {
                    estRateLayout.setVisibility(View.VISIBLE);
                    estRate.setText(String.format(Locale.getDefault(), "%s%%", fn.makeDouble(String.valueOf(finalRate))));
                }
                if (finalEscrow > 0) {
                    escAmountLayout.setVisibility(View.VISIBLE);
                    escAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(finalEscrow))));
                }
                if (finalBalance > 0) {
                    estBalanceLayout.setVisibility(View.VISIBLE);
                    estBalance.setText(fn.addSymbol(fn.makeDouble(String.valueOf(finalBalance))));
                }
                if (finalPartPayment > 0) {
                    partPaymentLayout.setVisibility(View.VISIBLE);
                    partPaymentMade.setText(fn.addSymbol(fn.makeDouble(String.valueOf(finalPartPayment))));
                }
                if (finalInterestPaid > 0) {
                    estInterestPaidLayout.setVisibility(View.VISIBLE);
                    estInterestPaid.setText(fn.addSymbol(fn.makeDouble(String.valueOf(finalInterestPaid))));
                }
            }
            else {
                showing[0] = false;
                arrow.animate().rotation(0);
                detailedView.setVisibility(View.GONE);
                viewDetailsLabel.setText(getString(R.string.view_details));
            }
        });

        int paidPayments = 0;
        for (Payments pay: paymentInfo.getPayments()) {
            if (pay.isPaid() && pay.getBillerName().equals(payment.getBillerName())) {
                paidPayments = paidPayments + 1;
            }
        }
        for (Bill bill: thisUser.getBills()) {
            if (bill.getBillerName().equals(payment.getBillerName()) && (payment.getPaymentNumber() - paidPayments) == Integer.parseInt(bill.getPaymentsRemaining())) {
                finalPayment.setVisibility(View.VISIBLE);
            }
        }
        billerName.setText(payment.getBillerName());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        TextView divider = new TextView(mContext);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(getResources().getColor(R.color.buttonStroke, getTheme()));
        amountDue.setText(fn.addSymbol(String.valueOf(Double.parseDouble(payment.getPaymentAmount()) - payment.getPartialPayment())));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        billBox.setLayoutParams(lp1);
        View onTime = View.inflate(MainActivity2.this, R.layout.on_time, null);
        switch (type) {
            case "Today":
                int daysLate = todayDateValue - payment.getPaymentDate();
                if (!payment.isPaid() && daysLate > 0) {

                    status.removeAllViews();
                    View late = View.inflate(MainActivity2.this, R.layout.late, null);
                    status.addView(late);

                    if (daysLate == 1) {
                        dueDate.setText(R.string.dueYesterday);
                    } else {
                        dueDate.setText(String.format(Locale.getDefault(), "%s %d %s", getString(R.string.due), daysLate, getString(R.string.daysAgo)));
                    }
                    billBox.setElevation(15);
                } else {
                    dueDate.setText(R.string.dueToday);
                    billBox.setElevation(15);
                }
                todayList.addView(divider);
                todayList.addView(billBox);
                break;
            case "LaterThisWeek":
                if (payment.getPaymentDate() - todayDateValue == 1) {
                    dueDate.setText(R.string.dueTomorrow);
                } else if (payment.getPaymentDate() - sunday >= 1 && payment.getPaymentDate() - sunday <= 7) {
                    LocalDate local = dateFormatter.convertIntDateToLocalDate(payment.getPaymentDate());
                    dueDate.setText(String.format("%s %s", getString(R.string.due), local.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())));
                } else {
                    dueDate.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.due), dateFormatter.convertIntDateToString(payment.getPaymentDate())));
                }
                laterThisWeekList.addView(divider);
                laterThisWeekList.addView(billBox);
                break;
            case "LaterThisMonth":
                dueDate.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.due), dateFormatter.convertIntDateToString(payment.getPaymentDate())));
                laterThisMonthList.addView(divider);
                laterThisMonthList.addView(billBox);
                break;
            default:
                dueDate.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.paid), dateFormatter.convertIntDateToString(payment.getDatePaid())));
                status.removeAllViews();
                status.addView(onTime);
                earlierThisMonthList.addView(divider);
                earlierThisMonthList.addView(billBox);
                break;
        }
        swipeViewListener(billBox, payment);
        billBoxSelect.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            if (billerName.getText() != null) {
                startPay(dueDate, billerName, amountDue, payment, todayDateValue);
            }
        });
        billBox.animate().translationX(0).setDuration(500);
    }

    private void startPay(TextView tvDueDate1, TextView tvBillerName1, TextView tvAmountDue1, Payments td, int todayDateValue) {
        StringFixer sf = new StringFixer();
        Intent pay = new Intent(mContext, PayBill.class);
        pay.putExtra("Due Date", tvDueDate1.getText().toString());
        pay.putExtra("Biller Name", tvBillerName1.getText().toString());
        pay.putExtra("Amount Due", sf.numbersOnly(tvAmountDue1.getText().toString()));
        pay.putExtra("Is Paid", td.isPaid());
        pay.putExtra("Payment Id", td.getPaymentId());
        pay.putExtra("Current Date", todayDateValue);
        startActivity(pay);
    }

    public ArrayList<Payments> whatsDueThisMonth(ArrayList<Payments> dueThisMonth, LocalDate selectedDate) {

        dueThisMonth.clear();
        TextView tvTotal = findViewById(R.id.tvTotal), tvRemaining = findViewById(R.id.tvRemaining);
        int todaysDate = dateFormatter.currentDateAsInt();
        double total = 0, remaining = 0;
        LocalDate todayLocalDate = dateFormatter.convertIntDateToLocalDate(todaysDate);
        int currentMonthEnd = dateFormatter.calcDateValue(todayLocalDate.withDayOfMonth(todayLocalDate.getMonth().length(todayLocalDate.isLeapYear())).withYear(todayLocalDate.getYear()));
        int counter = 0, monthStart = dateFormatter.calcDateValue(selectedDate.withDayOfMonth(1)),
                monthEnd = dateFormatter.calcDateValue(selectedDate.withDayOfMonth(selectedDate.getMonth().length(selectedDate.isLeapYear())));
        ArrayList<Payments> payments = paymentInfo.getPayments();
        payments.sort(Comparator.comparing(Payments::getPaymentDate));

        if (payments.size() > 0) {
            for (Payments payment : payments) {
                if (payment.getPaymentDate() >= monthStart && payment.getPaymentDate() <= monthEnd && !payment.isPaid() && todaysDate < monthEnd || payment.getDatePaid() >= monthStart &&
                        payment.getDatePaid() <= monthEnd || payment.getPaymentDate() >= monthStart && payment.getPaymentDate() <= monthEnd && !dueThisMonth.contains(payment) && todaysDate < monthEnd ||
                        !payment.isPaid() && payment.getPaymentDate() < currentMonthEnd && selectedDate.getMonth().equals(dateFormatter.convertIntDateToLocalDate(todaysDate).getMonth()) &&
                                selectedDate.getYear() == (dateFormatter.convertIntDateToLocalDate(todaysDate).getYear())) {
                    if (!dueThisMonth.contains(payment)) {
                        dueThisMonth.add(payment);
                    }
                    ++counter;
                    if (!payment.isPaid()) {
                        if (payment.getPartialPayment() > 0) {
                            remaining = remaining + Double.parseDouble(fn.makeDouble(String.valueOf(Double.parseDouble(payment.getPaymentAmount()) - payment.getPartialPayment())));
                        }
                        else {
                            remaining = remaining + Double.parseDouble(fn.makeDouble(payment.getPaymentAmount().replaceAll(",", ".").replaceAll(" ", "").replaceAll("\\s", "")));
                        }
                    }
                    total = total + Double.parseDouble(fn.makeDouble(payment.getPaymentAmount().replaceAll(",", ".").replaceAll(" ", "").replaceAll("\\s", "")));
                }
            }
        }
        if (counter == 0) {
            noResults.setVisibility(View.VISIBLE);
        }
        String fixTotal = (getString(R.string.total) + " " + fn.addSymbol(String.valueOf(total))), fixRemaining = (getString(R.string.remaining) + " " + fn.addSymbol(String.valueOf(remaining)));
        tvTotal.setText(fixTotal);
        ProgressBar pb8 = findViewById(R.id.progressBar8);
        int progress;
        if (remaining == 0) {
            progress = 100;
        } else {
            progress = 100 - ((int) (remaining) * 100) / ((int) total);
        }
        pb8.post(() -> {
            ObjectAnimator animation = ObjectAnimator.ofInt(pb8, "progress", progress);
            animation.setDuration(500);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
        });
        tvRemaining.setText(fixRemaining);
        return dueThisMonth;
    }

    private void createNotificationChannel() {

        CharSequence name = (getString(R.string.billTracker));
        String description = (getString(R.string.billTrackerNotificationChannel));
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        channelId = thisUser.getid();
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
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

    public void refreshUser() {

        SaveUserData save = new SaveUserData();
        save.loadUserData(MainActivity2.this);
    }

    public void logout(View view) {

        Context mContext = this;
        GoogleSignIn.getClient(MainActivity2.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
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

        selectedDate = LocalDate.now(ZoneId.systemDefault());
        setCurrentMonth(selectedMonth);
        navDrawer.setVisibility(View.GONE);
        hideForNavDrawer.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
        getValues();
        getCurrentMonth();
        counter = 0;
        dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
        listBills();
        setupLineChart();
        CountTickets countTickets = new CountTickets();
        countTickets.countTickets(ticketCounter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        selectedDate = LocalDate.now(ZoneId.systemDefault());
        setCurrentMonth(selectedMonth);
        navDrawer.setVisibility(View.GONE);
        hideForNavDrawer.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
        getValues();
        getCurrentMonth();
        counter = 0;
        dueThisMonth = whatsDueThisMonth(dueThisMonth, selectedDate);
        listBills();
        setupLineChart();
        CountTickets countTickets = new CountTickets();
        countTickets.countTickets(ticketCounter);
    }

}