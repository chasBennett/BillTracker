package com.example.billstracker.activities;

import static com.example.billstracker.activities.MainActivity2.startAddBiller;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.Stat;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MoneyFormatterWatcher;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class MyStats extends BaseActivity {

    Button enterIncome, changeIncome, btnAddNewBiller;
    com.google.android.flexbox.FlexboxLayout bubbles;
    LinearLayout dtiChart;
    LinearLayout noIncomeBox;
    LinearLayout addBillerLayout;
    LinearLayout pieChartLayout;
    LinearLayout nextPaymentDueLayout;
    LinearLayout paidInFullLayout;
    LinearLayout lastPaymentAmountLayout;
    LinearLayout totalBillersAddedLayout;
    LinearLayout statsBox;
    ConstraintLayout pb;
    LocalDate selectedDate;
    TextView monthSpinner;
    TextView dtiRatio;
    TextView dtiRating;
    TextView nameHeader;
    TextView dtiExplanation;
    TextView totalAmountPaid;
    TextView totalBillersAdded;
    TextView totalPaymentsMade;
    TextView billersPaidOff;
    TextView lastPaymentAmount;
    TextView nextPaymentDue;
    ScrollView scroll;
    ShapeableImageView icon;
    PieChart pieChart, pieChart1;
    AdView adview;
    User thisUser;
    ArrayList<Bill> bills;
    ArrayList<Payment> payments;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.activity_my_stats);

        pb = findViewById(R.id.progressBar);
        icon = findViewById(R.id.statIcon);
        scroll = findViewById(R.id.statScroll);
        bubbles = findViewById(R.id.bubbles);
        monthSpinner = findViewById(R.id.myStatsMonthSpinner);
        adview = findViewById(R.id.adView5);
        pieChart = findViewById(R.id.pieChart);
        dtiRatio = findViewById(R.id.dtiRatio);
        dtiRating = findViewById(R.id.dtiRating);
        pieChart1 = findViewById(R.id.pieChart1);
        dtiChart = findViewById(R.id.dtiChartLayout);
        nameHeader = findViewById(R.id.statsLabel);
        noIncomeBox = findViewById(R.id.noIncomeBox);
        enterIncome = findViewById(R.id.btnEnterIncome);
        changeIncome = findViewById(R.id.btnChangeIncome);
        pieChartLayout = findViewById(R.id.pieChartLayout);
        dtiExplanation = findViewById(R.id.textView65);
        addBillerLayout = findViewById(R.id.addBillerLayout);
        btnAddNewBiller = findViewById(R.id.btnAddBiller1);
        nextPaymentDueLayout = findViewById(R.id.nextPaymentDueLayout);
        paidInFullLayout = findViewById(R.id.paidInFullLayout);
        lastPaymentAmountLayout = findViewById(R.id.lastPaymentAmountLayout);
        totalBillersAddedLayout = findViewById(R.id.totalBillersAddedLayout);
        statsBox = findViewById(R.id.statsBox);
        totalAmountPaid = findViewById(R.id.totalAmountPaid);
        totalBillersAdded = findViewById(R.id.totalBillersAdded);
        totalPaymentsMade = findViewById(R.id.totalPaymentsMade);
        billersPaidOff = findViewById(R.id.totalBillersPaidOff);
        lastPaymentAmount = findViewById(R.id.lastPaymentAmount);
        nextPaymentDue = findViewById(R.id.nextPaymentDue);

        Tools.fixProgressBarLogo(pb);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);

        NavController nc = new NavController();
        nc.navController(MyStats.this, MyStats.this, pb, "myStats");

        initialize();

    }

    public void initialize() {

        thisUser = repo.getUser(MyStats.this);
        payments = repo.getPayments();
        bills = repo.getBills();

        ArrayList<String> monthsList = new ArrayList<>();
        ArrayList<Payment> payments = repo.getPayments();
        if (payments != null) {
            payments.sort(Comparator.comparing(Payment::getDueDate));
            for (Payment payment : payments) {
                if (!monthsList.contains(DateFormat.createMonthYearString(DateFormat.makeLocalDate(payment.getDueDate())))) {
                    monthsList.add(DateFormat.createMonthYearString(DateFormat.makeLocalDate(payment.getDueDate())));
                }
            }
        }

        selectedDate = LocalDate.now(ZoneId.systemDefault());

        monthSpinner.setText(DateFormat.createMonthYearString(selectedDate));

        monthSpinner.setOnClickListener(view -> Tools.spinnerPopup(monthsList, null, monthSpinner, item -> {
            monthSpinner.setText(item);
            selectedDate = LocalDate.parse(item + " 1", DateTimeFormatter.ofPattern("MMMM yyyy d", Locale.getDefault()));
            loadPieChartData();
            loadPieChartData1();
        }));

        setupDti();

        if (bills.isEmpty()) {
            pieChartLayout.setVisibility(View.GONE);
            addBillerLayout.setVisibility(View.VISIBLE);
            btnAddNewBiller.setOnClickListener(view -> {
                pb.setVisibility(View.VISIBLE);
                startAddBiller = true;
                Intent addBiller = new Intent(MyStats.this, AddBiller.class);
                startActivity(addBiller);
            });
        } else {
            setupPieChart();
            loadPieChartData();
        }

        String name = thisUser.getName();
        if (name != null && name.contains(" ")) {
            int space = name.indexOf(' ');
            nameHeader.setText(String.format(Locale.getDefault(), "%s%s", name.substring(0, space), getString(R.string.sStats)));
        } else if (name != null && !name.contains(" ")) {
            nameHeader.setText(String.format(Locale.getDefault(), "%s%s", name, getString(R.string.sStats)));
        } else {
            nameHeader.setText(getString(R.string.myStats));
        }

        ArrayList<String> arrayAdapter = new ArrayList<>();
        arrayAdapter.add(getString(R.string.weekly));
        arrayAdapter.add(getString(R.string.biweekly));
        arrayAdapter.add(getString(R.string.monthly));
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayAdapter);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        enterIncome.setOnClickListener(view -> {
            CustomDialog cd = new CustomDialog(MyStats.this, getString(R.string.my_income), getString(R.string.enter_your_income_details_to_calculate_your_dti), getString(R.string.submit),
                    getString(R.string.cancel), null);
            cd.setEditText(getString(R.string.income_amount), FixNumber.addSymbol(FixNumber.makeDouble(thisUser.getIncome())), AppCompatResources.getDrawable(MyStats.this, R.drawable.payment_amount_icon));
            cd.setTextWatcher(new MoneyFormatterWatcher(cd.getEditText()));
            cd.isMoneyInput(true);
            cd.setSpinner(adapter2, getString(R.string.pay_frequency_), thisUser.getPayFrequency(), AppCompatResources.getDrawable(MyStats.this, R.drawable.categories));
            cd.setPositiveButtonListener(v -> repo.editUser(MyStats.this)
                            .setPayFrequency(cd.getSpinnerSelection())
                            .setIncome(FixNumber.makeDouble(cd.getInput()))
                                    .save((wasSuccessful, message) -> {
                                        if (wasSuccessful) {
                                            cd.dismissDialog();
                                            initialize();
                                        }
                                        else {
                                            Notify.createPopup(MyStats.this, "Error: " + message, null);
                                        }
                                    }));
            cd.setNegativeButtonListener(v -> cd.dismissDialog());
        });

        changeIncome.setOnClickListener(view -> {
            CustomDialog cd = new CustomDialog(MyStats.this, getString(R.string.my_income), getString(R.string.enter_your_income_details_to_calculate_your_dti), getString(R.string.submit), getString(R.string.cancel), null);
            cd.setEditText(getString(R.string.income_amount), FixNumber.addSymbol(FixNumber.makeDouble(thisUser.getIncome())), AppCompatResources.getDrawable(MyStats.this, R.drawable.payment_amount_icon));
            cd.setTextWatcher(new MoneyFormatterWatcher(cd.getEditText()));
            cd.setSpinner(adapter2, getString(R.string.pay_frequency_), thisUser.getPayFrequency(), AppCompatResources.getDrawable(MyStats.this, R.drawable.categories));
            cd.isMoneyInput(true);
            cd.setPositiveButtonListener(v -> repo.editUser(MyStats.this)
                    .setPayFrequency(cd.getSpinnerSelection())
                    .setIncome(FixNumber.makeDouble(cd.getInput()))
                    .save((wasSuccessful, message) -> {
                        if (wasSuccessful) {
                            cd.dismissDialog();
                            initialize();
                        }
                        else {
                            Notify.createPopup(MyStats.this, "Error: " + message, null);
                        }
                    }));
            cd.setNegativeButtonListener(v -> cd.dismissDialog());
        });

        createLists();
    }

    public void setupDti() {
        if (thisUser.getIncome() > 0) {
            dtiChart.setVisibility(View.VISIBLE);
            noIncomeBox.setVisibility(View.GONE);
            setupPieChart1();
            loadPieChartData1();
        }
    }

    public void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(80);
        pieChart.setHoleColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, getTheme()));
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(8);
        pieChart.canResolveTextAlignment();
        pieChart.setDrawEntryLabels(false);
        pieChart.setNoDataText(getString(R.string.noDataFound));
        pieChart.setEntryLabelColor(getResources().getColor(R.color.black, getTheme()));
        pieChart.setNoDataTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        pieChart.setExtraOffsets(5, 0, 0, 0);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setDrawInside(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setEnabled(true);
    }

    public void loadPieChartData() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        double auto = 0, creditCard = 0, entertainment = 0, insurance = 0, miscellaneous = 0, mortgage = 0, personalLoan = 0, utilities = 0, total = 0;
        long monthStart = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        long monthEnd = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.getMonth().length(selectedDate.isLeapYear())));
        for (Bill bill : bills) {
            for (Payment payment : payments) {
                if (payment.getBillerName().equals(bill.getBillerName()) && payment.getDueDate() >= monthStart && payment.getDueDate() <= monthEnd) {
                    double value = bill.getAmountDue();
                    total = total + value;
                    switch (bill.getCategory()) {
                        case 0:
                            auto = auto + value;
                            break;
                        case 1:
                            creditCard = creditCard + value;
                            break;
                        case 2:
                            entertainment = entertainment + value;
                            break;
                        case 3:
                            insurance = insurance + value;
                            break;
                        case 4:
                            miscellaneous = miscellaneous + value;
                            break;
                        case 5:
                            mortgage = mortgage + value;
                            break;
                        case 6:
                            personalLoan = personalLoan + value;
                            break;
                        case 7:
                            utilities = utilities + value;
                            break;
                    }
                }
            }
        }

        if (auto != 0) {
            entries.add(new PieEntry((float) ((auto * 100) / total), getString(R.string.autoLoan) + " " + FixNumber.addSymbol(String.valueOf(auto))));
        }
        if (creditCard != 0) {
            entries.add(new PieEntry((float) ((creditCard * 100) / total), getString(R.string.creditCard) + " " + FixNumber.addSymbol(String.valueOf(creditCard))));
        }
        if (entertainment != 0) {
            entries.add(new PieEntry((float) ((entertainment * 100) / total), getString(R.string.entertainment) + " " + FixNumber.addSymbol(String.valueOf(entertainment))));
        }
        if (insurance != 0) {
            entries.add(new PieEntry((float) ((insurance * 100) / total), getString(R.string.insurance) + " " + FixNumber.addSymbol(String.valueOf(insurance))));
        }
        if (miscellaneous != 0) {
            entries.add(new PieEntry((float) ((miscellaneous * 100) / total), getString(R.string.miscellaneous) + " " + FixNumber.addSymbol(String.valueOf(miscellaneous))));
        }
        if (mortgage != 0) {
            entries.add(new PieEntry((float) ((mortgage * 100) / total), getString(R.string.mortgage) + " " + FixNumber.addSymbol(String.valueOf(mortgage))));
        }
        if (personalLoan != 0) {
            entries.add(new PieEntry((float) ((personalLoan * 100) / total), getString(R.string.personalLoans) + " " + FixNumber.addSymbol(String.valueOf(personalLoan))));
        }
        if (utilities != 0) {
            entries.add(new PieEntry((float) ((utilities * 100) / total), getString(R.string.utilities) + " " + FixNumber.addSymbol(String.valueOf(utilities))));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : MyStats.this.getResources().getIntArray(R.array.pieChartCorresponding)) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.total) + " " + FixNumber.addSymbol(String.valueOf(total)));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    public void setupPieChart1() {
        pieChart1.setDrawHoleEnabled(true);
        pieChart1.setHoleRadius(80);
        pieChart1.setHoleColor(ResourcesCompat.getColor(getResources(), android.R.color.transparent, getTheme()));
        pieChart1.setUsePercentValues(true);
        pieChart1.setEntryLabelTextSize(8);
        pieChart1.canResolveTextAlignment();
        pieChart1.setDrawEntryLabels(false);
        pieChart1.setNoDataText(getString(R.string.dtiNotCalculated));
        pieChart1.setEntryLabelColor(getResources().getColor(R.color.black, getTheme()));
        pieChart1.setNoDataTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        pieChart1.setExtraOffsets(0, 0, 0, 0);
        pieChart1.getDescription().setEnabled(false);

        Legend l = pieChart1.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        l.setDrawInside(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setEnabled(true);
    }

    public void loadPieChartData1() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        double bills = 0, income;

        double daysInMonth = selectedDate.getMonth().length(selectedDate.isLeapYear());
        long monthStart = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        long monthEnd = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.getMonth().length(selectedDate.isLeapYear())));
        if (thisUser.getPayFrequency() == 0) {
            income = (daysInMonth / 7) * thisUser.getIncome();
        } else if (thisUser.getPayFrequency() == 1) {
            income = (daysInMonth / 14) * thisUser.getIncome();
        } else {
            income = thisUser.getIncome();
        }
        if (payments != null) {
            for (Payment payment : payments) {
                if (payment.getDueDate() >= monthStart && payment.getDueDate() <= monthEnd) {
                    bills = bills + payment.getPaymentAmount();
                }
            }
        } else {
            repo.loadLocalData(MyStats.this, null);
        }


        double disposable = (income - bills) / income * 100;
        double billPercentage = bills / income * 100;
        DecimalFormat df = new DecimalFormat("###.##");
        if (billPercentage < 50 && billPercentage > 40) {
            dtiRating.setText(getString(R.string.yourDtiRatingGood));
            dtiExplanation.setText(getString(R.string.lendersPreferAbove50));
        } else if (billPercentage <= 40) {
            dtiRating.setText(getString(R.string.yourDtiRatingExcellent));
            dtiExplanation.setText(getString(R.string.dtiIsBelowLimit));
        } else if (billPercentage >= 50 && billPercentage < 60) {
            dtiRating.setText(getString(R.string.dtiRatingIsFair));
            dtiExplanation.setText(getString(R.string.dtiTooHigh));
        } else {
            dtiRating.setText(getString(R.string.dtiRatingPoor));
            dtiExplanation.setText(getString(R.string.dtiIsWayTooHigh));
        }
        if (income > bills) {
            entries.add(new PieEntry((float) disposable, getString(R.string.disposableIncome) + " " + FixNumber.addSymbol(String.valueOf(income - bills))));
        }
        entries.add(new PieEntry((float) billPercentage, getString(R.string.bills) + " " + FixNumber.addSymbol(String.valueOf(bills))));
        dtiRatio.setText(String.format(Locale.getDefault(), "%s %s%%", getString(R.string.yourDebtToIncomeRatioIs), df.format(billPercentage)));

        ArrayList<Integer> colors = new ArrayList<>();

        for (int color : MyStats.this.getResources().getIntArray(R.array.pieChartCorresponding)) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.incomeAmount) + " " + FixNumber.addSymbol(String.valueOf(income)));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart1.invalidate();
        pieChart1.setData(data);

        pieChart1.animateY(1400, Easing.EaseInOutQuad);
    }

    public void createLists() {

        double total = 0;
        int payments = 0;
        int totalBillers = 0;
        int billersPaid = 0;
        ArrayList<Stat> stats = new ArrayList<>();

        if (bills != null) {
            for (Bill bill : bills) {
                ++totalBillers;
                double totalAmount = 0;
                double lastPaymentAmount = 0;
                int paymentsMade = 0;
                long dateLastPaid = 0;
                long nextPaymentDue = 0;
                if (bill.getPaymentsRemaining() == 0) {
                    ++billersPaid;
                }
                Payment payment = repo.getPaymentByBillerName(bill.getBillerName());
                if (payment != null) {
                    if (payment.isPaid()) {
                        ++paymentsMade;
                        ++payments;
                        total += payment.getPaymentAmount();
                        totalAmount += payment.getPaymentAmount();
                        if (payment.getDatePaid() > dateLastPaid) {
                            dateLastPaid = payment.getDatePaid();
                            lastPaymentAmount = payment.getPaymentAmount();
                        }
                    } else {
                        nextPaymentDue = payment.getDueDate();
                    }
                }
                stats.add(new Stat(bill.getBillerName(), totalAmount, paymentsMade, dateLastPaid, nextPaymentDue, lastPaymentAmount, bill.getPaymentsRemaining()));
            }
        }

        nextPaymentDueLayout.setVisibility(View.GONE);
        lastPaymentAmountLayout.setVisibility(View.GONE);
        totalAmountPaid.setText(FixNumber.addSymbol(String.valueOf(total)));
        totalPaymentsMade.setText(String.valueOf(payments));
        totalBillersAdded.setText(String.valueOf(totalBillers));
        billersPaidOff.setText(String.valueOf(billersPaid));

        bubbles.removeAllViews();
        bubbles.invalidate();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lp.gravity = Gravity.CENTER;

        View bubble1 = View.inflate(MyStats.this, R.layout.biller_bubble, null);
        TextView billerName1 = bubble1.findViewById(R.id.textView49);
        billerName1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.turquoise, getTheme())));
        billerName1.setTextSize(14);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(-2, -1, 1);
        bubble1.setLayoutParams(lp1);
        bubble1.setPadding(15, 15, 15, 15);
        bubbles.addView(bubble1);
        int finalTotalBillers = totalBillers;
        int finalBillersPaid = billersPaid;
        double finalTotal = total;
        int finalPayments = payments;
        bubble1.setOnClickListener(view -> {
            String name = thisUser.getName();
            if (name != null && name.contains(" ")) {
                int space = name.indexOf(' ');
                nameHeader.setText(String.format(Locale.getDefault(), "%s%s", name.substring(0, space), getString(R.string.sStats)));
            } else if (name != null && !name.contains(" ")) {
                nameHeader.setText(String.format(Locale.getDefault(), "%s%s", name, getString(R.string.sStats)));
            } else {
                nameHeader.setText(getString(R.string.myStats));
            }
            nextPaymentDueLayout.setVisibility(View.GONE);
            lastPaymentAmountLayout.setVisibility(View.GONE);
            totalBillersAddedLayout.setVisibility(View.VISIBLE);
            paidInFullLayout.setVisibility(View.VISIBLE);
            totalAmountPaid.setText(FixNumber.addSymbol(String.valueOf(finalTotal)));
            totalPaymentsMade.setText(String.valueOf(finalPayments));
            totalBillersAdded.setText(String.valueOf(finalTotalBillers));
            billersPaidOff.setText(String.valueOf(finalBillersPaid));
            icon.setBackground(null);
            icon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.statistics_removebg_preview, getTheme()));
            icon.setImageTintList(null);
            scroll.post(() -> scroll.smoothScrollTo(0, statsBox.getTop() - 50));
        });

        for (Stat stat : stats) {
            View bubble = View.inflate(MyStats.this, R.layout.biller_bubble, null);
            TextView billerName = bubble.findViewById(R.id.textView49);
            billerName.setText(String.format(Locale.getDefault(), " %s ", stat.getBillerName()));
            billerName.setTextSize(14);
            bubble.setPadding(15, 15, 15, 15);
            if (stat.getTotalPaymentsMade() < 1) {
                billerName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey, getTheme())));
            }
            if (stat.getPaymentsRemaining() > 0) {
                if (stat.getNextPaymentDueDate() < DateFormat.currentDateAsLong()) {
                    billerName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red, getTheme())));
                } else if (stat.getNextPaymentDueDate() == DateFormat.currentDateAsLong()) {
                    billerName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.yellow, getTheme())));
                }
            } else {
                billerName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.payBill, getTheme())));
            }
            bubbles.addView(bubble);
            addListener(bubble, stat);
        }

    }

    public void addListener(View view, Stat stat) {
        view.setOnClickListener(view1 -> {
            paidInFullLayout.setVisibility(View.GONE);
            totalBillersAddedLayout.setVisibility(View.GONE);
            nextPaymentDueLayout.setVisibility(View.VISIBLE);
            lastPaymentAmountLayout.setVisibility(View.VISIBLE);
            nameHeader.setText(stat.getBillerName());
            totalAmountPaid.setText(FixNumber.addSymbol(String.valueOf(stat.getTotalPaymentsAmount())));
            totalPaymentsMade.setText(String.valueOf(stat.getTotalPaymentsMade()));
            if (stat.getLastPaymentAmount() == 0) {
                lastPaymentAmount.setText(getString(R.string.no_payments_reported1));
            } else {
                lastPaymentAmount.setText(FixNumber.addSymbol(stat.getLastPaymentAmount()));
            }
            if (stat.getNextPaymentDueDate() == 0) {
                nextPaymentDue.setText(getString(R.string.noPaymentsAreDue));
            } else {
                nextPaymentDue.setText(DateFormat.makeDateString(stat.getNextPaymentDueDate()));
            }
            for (Bill bill : bills) {
                if (bill.getBillerName().equals(stat.getBillerName())) {
                    Tools.loadIcon(icon, bill.getCategory(), bill.getIcon());
                    icon.setContentPadding(60, 60, 60, 60);
                }
            }
            scroll.post(() -> scroll.smoothScrollTo(0, statsBox.getTop() - 50));
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize();
        pb.setVisibility(View.GONE);
    }
}