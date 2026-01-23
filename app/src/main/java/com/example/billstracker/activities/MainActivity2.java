package com.example.billstracker.activities;

import static com.example.billstracker.popup_classes.PaymentConfirm.newPaymentDate;
import static com.example.billstracker.popup_classes.PaymentConfirm.paymentAmount;
import static com.example.billstracker.tools.DataTools.whatsDueThisMonth;

import android.app.Activity;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Partner;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.custom_objects.User;
import com.example.billstracker.popup_classes.CalendarView;
import com.example.billstracker.popup_classes.InstructionPopup;
import com.example.billstracker.popup_classes.MonthYearPickerDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.popup_classes.PaymentConfirm;
import com.example.billstracker.recycler_adapters.RecyclerAdapter;
import com.example.billstracker.tools.BillerManager;
import com.example.billstracker.tools.CountTickets;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MainPieChart;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.NotificationManager;
import com.example.billstracker.tools.Prefs;
import com.example.billstracker.tools.Tools;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity2 extends BaseActivity {

    public static LocalDate selectedDate;
    public static double todayTotal = 0;
    public static double pastDue;
    public static int tickets;
    public static Payment makePayment;
    public static long todayDateValue;
    public static long sunday;
    public static ArrayList<Payment> dueThisMonth = new ArrayList<>();
    public static boolean startAddBiller;
    public static boolean skipInstruction1, skipInstruction2;
    final ArrayList<Payment> earlier = new ArrayList<>();
    final ArrayList<Payment> laterThisWeek = new ArrayList<>();
    final ArrayList<Payment> later = new ArrayList<>();
    final ArrayList<Payment> today = new ArrayList<>();
    final NavController nc = new NavController();
    public PieChart pieChart;
    Context mContext;
    Activity activity;
    int counter;
    double earlyTotal = 0, laterThisWeekTotal = 0, laterTotal = 0;
    LinearLayout lineChartLoading, noResults, todayList, laterThisWeekList, laterThisMonthList, earlierThisMonthList, calendarView;
    TextView selectedMonth, backMonth, forwardMonth, admin, showCalendar, noBillsFound;
    ProgressBar loadingBills;
    RecyclerView todayRecycler, laterThisWeekRecycler, laterThisMonthRecycler, earlierThisMonthRecycler;
    ConstraintLayout pb;
    ScrollView scroll;
    ProgressBar remainingProgressBar;

    @Override
    protected void onDataReady() {
        setContentView(R.layout.main_activity);

        mContext = getApplicationContext();

        selectedMonth = findViewById(R.id.selectedMonth);
        backMonth = findViewById(R.id.backMonth);
        pieChart = findViewById(R.id.mainPieChart);
        forwardMonth = findViewById(R.id.forgotPasswordHeader);
        todayList = findViewById(R.id.todayList);
        lineChartLoading = findViewById(R.id.lineChartLoading);
        laterThisWeekList = findViewById(R.id.laterThisWeekList);
        laterThisMonthList = findViewById(R.id.laterThisMonthList);
        earlierThisMonthList = findViewById(R.id.earlierThisMonthList);
        noResults = findViewById(R.id.noResults);
        admin = findViewById(R.id.admin);
        pb = findViewById(R.id.progressBar);
        loadingBills = findViewById(R.id.loadingBills);
        loadingBills.setVisibility(View.GONE);
        todayRecycler = findViewById(R.id.todayListRecycler);
        laterThisWeekRecycler = findViewById(R.id.laterThisWeekListRecycler);
        laterThisMonthRecycler = findViewById(R.id.laterThisMonthListRecycler);
        earlierThisMonthRecycler = findViewById(R.id.earlierThisMonthListRecycler);
        calendarView = findViewById(R.id.calendarView);
        showCalendar = findViewById(R.id.showCalendar);
        activity = this;
        tickets = 0;
        remainingProgressBar = findViewById(R.id.progressBar8);
        noBillsFound = findViewById(R.id.noBillsFound);
        scroll = findViewById(R.id.mainScroll);
        skipInstruction1 = false;
        skipInstruction2 = false;

        Tools.fixProgressBarLogo(pb);

        if (selectedDate == null) {
            selectedDate = LocalDate.now(ZoneId.systemDefault());
        }

        CountTickets.countTickets(MainActivity2.this);

        if (repo.getUid(MainActivity2.this) != null) {
            repo.setSavedChannelId(MainActivity2.this, repo.getUid(MainActivity2.this));
        }

        pastDue = 0;

        nc.navController(MainActivity2.this, MainActivity2.this, pb, "main");
        selectedMonth.setOnClickListener(v -> {
            MonthYearPickerDialog pd = new MonthYearPickerDialog();
            pd.show(getSupportFragmentManager(), "MonthYearPickerDialog");
            pd.setListener((view, year, month, dayOfMonth) -> {
                selectedDate = LocalDate.of(year, month, 1);
                showProgress();
            });
        });

        if (selectedDate == null) {
            selectedDate = DateFormat.makeLocalDate(DateFormat.currentDateAsLong());
        }

        if (repo.getUser(MainActivity2.this) != null && repo.getUser(MainActivity2.this).isAdmin()) {
            admin.setEnabled(true);
            admin.setOnClickListener(view -> {
                Intent admin = new Intent(MainActivity2.this, Administrator.class);
                startActivity(admin);
            });
        } else {
            admin.setEnabled(false);
        }

        backMonth.setOnClickListener(view -> {
            selectedDate = selectedDate.minusMonths(1);
            showProgress();
        });

        forwardMonth.setOnClickListener(view -> {
            selectedDate = selectedDate.plusMonths(1);
            showProgress();
        });

        getWindow().getDecorView().post(this::initialize);
        showCalendar.setOnClickListener(v -> {
            if (calendarView.getVisibility() == View.GONE) {
                calendarView.setVisibility(View.VISIBLE);
                showCalendar.setText(R.string.hide_calendar);
                showCalendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_keyboard_arrow_up_24, 0, 0, 0);
                calendarView.post(() -> scroll.smoothScrollTo(0, calendarView.getTop() + ((calendarView.getBottom() - calendarView.getTop()) / 2)));
            } else {
                calendarView.setVisibility(View.GONE);
                showCalendar.setText(R.string.show_calendar);
                showCalendar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_keyboard_arrow_down_24, 0, 0, 0);
                calendarView.post(() -> scroll.smoothScrollTo(0, scroll.getTop()));
            }
        });
        checkIfNewUser();
        MainActivity2.this.getSystemService(android.app.NotificationManager.class).createNotificationChannel(new NotificationChannel(repo
                .getSavedChannelId(MainActivity2.this), "Notifications", android.app.NotificationManager.IMPORTANCE_HIGH));
    }

    public void showProgress() {

        loadingBills.setVisibility(View.VISIBLE);
        calendarView.removeAllViews();
        calendarView.addView(new ProgressBar(MainActivity2.this));
        Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            runOnUiThread(() -> {
                BillerManager.refreshPayments(MainActivity2.this);
                MainPieChart.setupPieChart(MainActivity2.this, pieChart);
                MainPieChart.loadPieChartData(MainActivity2.this, pieChart, remainingProgressBar, noBillsFound, todayRecycler, laterThisWeekRecycler, laterThisMonthRecycler, earlierThisMonthRecycler, scroll);
                listBills();
            });
            handler.post(() -> {
                selectedMonth.setText(String.format(Locale.getDefault(), "%s %d", selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()), selectedDate.getYear()));
                pb.setVisibility(View.GONE);
                loadingBills.setVisibility(View.GONE);
            });
        });
    }

    public void checkIfNewUser() {
        User user = repo.getUser(MainActivity2.this);

        if (user == null) {
            return;
        }

        if (user.getBudgets() == null) {
            user.setBudgets(new ArrayList<>());
        }

        if (!repo.getBills().isEmpty() && !user.getBudgets().isEmpty()) {
            Prefs.setTrainingDone(activity, true);
        }

        if (!Prefs.isTrainingDone(MainActivity2.this) && user.getTotalLogins() < 5) {
            new InstructionPopup(MainActivity2.this);
        }
    }

    public void initialize() {

        startAddBiller = false;
        getValues();
        setCurrentMonth();
        counter = 0;
        listBills();
        lineChartLoading.setVisibility(View.GONE);

        if (repo.getSavedChannelId(MainActivity2.this) != null) {
            NotificationManager.scheduleNotifications(MainActivity2.this);
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (repo.getUser(MainActivity2.this).getPartners() == null) {
            repo.getUser(MainActivity2.this).setPartners(new ArrayList<>());
        }
        if (!repo.getUser(MainActivity2.this).getPartners().isEmpty()) {
            for (Partner partner : repo.getUser(MainActivity2.this).getPartners()) {
                if (partner.getPartnerUid() != null) {
                    db.collection("users").document(partner.getPartnerUid()).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            User partnerUser = task.getResult().toObject(User.class);
                            if (partnerUser != null && partnerUser.getPartners() != null && !partnerUser.getPartners().isEmpty()) {
                                for (Partner part : partnerUser.getPartners()) {
                                    if (part.getPartnerUid().equals(repo.getUid(MainActivity2.this))) {
                                        if (part.getSharingAuthorized()) {
                                            if (!partner.getSharingAuthorized()) {
                                                Notify.createPartnerRequestNotification(MainActivity2.this, partner, null, new Intent(MainActivity2.this, ShareAccount.class));
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    public void getValues() {

        if (repo.getUser(MainActivity2.this).getName().contains(" ")) {
            if (repo.getUser(MainActivity2.this).getName().length() > 2 && repo.getUser(MainActivity2.this).getName().indexOf(' ') != 0) {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s%s!", getString(R.string.good), DateFormat.currentPhaseOfDay(mContext), repo.getUser(MainActivity2.this).getName().substring(0, 1).toUpperCase(),
                        repo.getUser(MainActivity2.this).getName().substring(1, repo.getUser(MainActivity2.this).getName().indexOf(' '))));
            } else {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s!", getString(R.string.good), DateFormat.currentPhaseOfDay(mContext), repo.getUser(MainActivity2.this).getName().toUpperCase()));
            }
        } else {
            if (repo.getUser(MainActivity2.this).getName().length() > 1) {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s%s!", getString(R.string.good), DateFormat.currentPhaseOfDay(mContext), repo.getUser(MainActivity2.this).getName().substring(0, 1).toUpperCase(),
                        repo.getUser(MainActivity2.this).getName().substring(1)));
            } else {
                admin.setText(String.format(Locale.getDefault(), "%s %s %s!", getString(R.string.good), DateFormat.currentPhaseOfDay(mContext), repo.getUser(MainActivity2.this).getName().toUpperCase()));
            }
        }
    }

    public void setCurrentMonth() {
        selectedMonth.setText(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).format(selectedDate));
    }

    public void listBills() {

        earlier.clear();
        today.clear();
        laterThisWeek.clear();
        later.clear();
        dueThisMonth.clear();
        earlyTotal = 0;
        todayTotal = 0;
        laterThisWeekTotal = 0;
        laterTotal = 0;
        dueThisMonth = whatsDueThisMonth(MainActivity2.this);
        sunday = DateFormat.makeLong(DateFormat.makeLocalDate(DateFormat.currentDateAsLong()).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        todayDateValue = DateFormat.currentDateAsLong();
        long todaysDate = DateFormat.makeLong(LocalDate.now(ZoneId.systemDefault()));
        long weekBeginning = DateFormat.makeLong(LocalDate.from(DateFormat.makeLocalDate(todaysDate).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay()));
        long weekEnd = DateFormat.makeLong(LocalDate.from(DateFormat.makeLocalDate(todaysDate).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atStartOfDay()));
        counter = 0;

        if (!dueThisMonth.isEmpty()) {
            Set<Payment> paySet = new LinkedHashSet<>(dueThisMonth);
            dueThisMonth.clear();
            dueThisMonth.addAll(paySet);
            repo.sortBills();
            noResults.setVisibility(View.GONE);
            for (Payment due : dueThisMonth) {
                long dueDate = due.getDueDate();
                counter = counter + 1;
                if (due.isPaid()) {
                    earlyTotal = earlyTotal + FixNumber.makeDouble(due.getPaymentAmount());
                } else if (todaysDate == dueDate || dueDate < todaysDate) {
                    todayTotal = todayTotal + FixNumber.makeDouble(due.getPaymentAmount() - due.getPartialPayment());
                } else if (dueDate >= weekBeginning && dueDate <= weekEnd) {
                    laterThisWeekTotal = laterThisWeekTotal + FixNumber.makeDouble(due.getPaymentAmount() - due.getPartialPayment());
                } else {
                    laterTotal = laterTotal + FixNumber.makeDouble(due.getPaymentAmount() - due.getPartialPayment());
                }
            }

            for (Payment due : dueThisMonth) {
                long dueDate = due.getDueDate();
                counter = counter + 1;
                if (due.isPaid()) {
                    earlier.add(due);
                } else if (dueDate == todaysDate || !due.isPaid() && dueDate < todaysDate) {
                    today.add(due);
                } else if (dueDate >= weekBeginning && dueDate <= weekEnd) {
                    laterThisWeek.add(due);
                } else {
                    later.add(due);
                }
            }
            CalendarView.create(MainActivity2.this, selectedDate, calendarView, todayRecycler, laterThisWeekRecycler, laterThisMonthRecycler, earlierThisMonthRecycler, scroll);
            earlier.sort(Comparator.comparingLong(Payment::getDatePaid));
            today.sort(Comparator.comparingLong(Payment::getDueDate));
            laterThisWeek.sort(Comparator.comparingLong(Payment::getDueDate));
            later.sort(Comparator.comparingLong(Payment::getDueDate));

            generateBillBoxes();

        } else {
            CalendarView.create(MainActivity2.this, selectedDate, calendarView, todayRecycler, laterThisWeekRecycler, laterThisMonthRecycler, earlierThisMonthRecycler, scroll);
            noResults.setVisibility(View.VISIBLE);
            todayList.setVisibility(View.GONE);
            laterThisWeekList.setVisibility(View.GONE);
            laterThisMonthList.setVisibility(View.GONE);
            earlierThisMonthList.setVisibility(View.GONE);
        }

    }

    public void generateBillBoxes() {

        if (!today.isEmpty()) {
            if (todayList.getChildCount() > 1) {
                todayList.removeViews(0, todayList.getChildCount() - 1);
            }
            View header = buildHeader(todayTotal, getString(R.string.today));
            todayList.addView(header, 0);
            todayList.setVisibility(View.VISIBLE);
        } else {
            if (todayList.getChildCount() > 1) {
                todayList.removeViews(0, todayList.getChildCount() - 1);
            }
            todayList.setVisibility(View.GONE);
        }
        if (!laterThisWeek.isEmpty()) {
            if (laterThisWeekList.getChildCount() > 1) {
                laterThisWeekList.removeViews(0, laterThisWeekList.getChildCount() - 1);
            }
            View header = buildHeader(laterThisWeekTotal, getString(R.string.laterThisWeek));
            laterThisWeekList.addView(header, 0);
            laterThisWeekList.setVisibility(View.VISIBLE);
        } else {
            if (laterThisWeekList.getChildCount() > 1) {
                laterThisWeekList.removeViews(0, laterThisWeekList.getChildCount() - 1);
            }
            laterThisWeekList.setVisibility(View.GONE);
        }
        if (!later.isEmpty()) {
            if (laterThisMonthList.getChildCount() > 1) {
                laterThisMonthList.removeViews(0, laterThisMonthList.getChildCount() - 1);
            }
            View header = buildHeader(laterTotal, getString(R.string.laterThisMonth));
            laterThisMonthList.addView(header, 0);
            laterThisMonthList.setVisibility(View.VISIBLE);
        } else {
            if (laterThisMonthList.getChildCount() > 1) {
                laterThisMonthList.removeViews(0, laterThisMonthList.getChildCount() - 1);
            }
            laterThisMonthList.setVisibility(View.GONE);
        }
        if (!earlier.isEmpty()) {
            if (earlierThisMonthList.getChildCount() > 1) {
                earlierThisMonthList.removeViews(0, earlierThisMonthList.getChildCount() - 1);
            }
            View header = buildHeader(earlyTotal, getString(R.string.earlierThisMonth));
            earlierThisMonthList.addView(header, 0);
            earlierThisMonthList.setVisibility(View.VISIBLE);
        } else {
            if (earlierThisMonthList.getChildCount() > 1) {
                earlierThisMonthList.removeViews(0, earlierThisMonthList.getChildCount() - 1);
            }
            earlierThisMonthList.setVisibility(View.GONE);
        }
        ArrayList<RecyclerView> recyclers = new ArrayList<>(Arrays.asList(todayRecycler, laterThisWeekRecycler, laterThisMonthRecycler, earlierThisMonthRecycler));
        for (RecyclerView recycle : recyclers) {
            recycle.setLayoutManager(new LinearLayoutManager(MainActivity2.this));
            recycle.setHasFixedSize(false);
            recycle.setNestedScrollingEnabled(false);
        }
        RecyclerAdapter adapter1 = new RecyclerAdapter(MainActivity2.this, MainActivity2.this, today, "Today");
        RecyclerAdapter adapter2 = new RecyclerAdapter(MainActivity2.this, MainActivity2.this, laterThisWeek, "LaterThisWeek");
        RecyclerAdapter adapter3 = new RecyclerAdapter(MainActivity2.this, MainActivity2.this, later, "LaterThisMonth");
        RecyclerAdapter adapter4 = new RecyclerAdapter(MainActivity2.this, MainActivity2.this, earlier, "earlier");
        todayRecycler.setAdapter(adapter1);
        laterThisWeekRecycler.setAdapter(adapter2);
        laterThisMonthRecycler.setAdapter(adapter3);
        earlierThisMonthRecycler.setAdapter(adapter4);
        ItemTouchHelper(todayRecycler, adapter1);
        ItemTouchHelper(laterThisWeekRecycler, adapter2);
        ItemTouchHelper(laterThisMonthRecycler, adapter3);
        ItemTouchHelper(earlierThisMonthRecycler, adapter4);
    }

    public void ItemTouchHelper(RecyclerView recycler, RecyclerAdapter adapter) {

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                onSwipe(adapter, viewHolder, direction);

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                final int position = viewHolder.getBindingAdapterPosition();
                Payment payment = adapter.getPayment(position);
                int color = getResources().getColor(R.color.payBill, getTheme());
                String pay = getString(R.string.markAsPaid);
                if (payment.isPaid()) {
                    pay = getString(R.string.mark_as_unpaid);
                    color = getResources().getColor(R.color.red, getTheme());
                }
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity2.this, R.color.primary))
                        .addSwipeLeftActionIcon(R.drawable.baseline_history_24)
                        .setSwipeLeftActionIconTint(ContextCompat.getColor(MainActivity2.this, R.color.white))
                        .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity2.this, R.color.white))
                        .addSwipeLeftLabel(getString(R.string.payment_history))
                        .addSwipeRightBackgroundColor(color)
                        .addSwipeRightLabel(pay)
                        .setSwipeRightActionIconTint(ContextCompat.getColor(MainActivity2.this, R.color.white))
                        .setSwipeRightLabelColor(ContextCompat.getColor(MainActivity2.this, R.color.white))
                        .addSwipeRightActionIcon(R.drawable.dollar_sign_icon)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper1.attachToRecyclerView(recycler);
        adapter.setPayBillClickListener((position, payment) -> {
            if (payment != null) {
                pb.setVisibility(View.VISIBLE);
                startActivity(new Intent(activity, PayBill.class).putExtra("paymentId", payment.getPaymentId()));
            } else {
                repo.loadLocalData(MainActivity2.this, null);
                recreate();
            }
        });
        adapter.setRefreshPaymentsClickListener((ignoredPosition, payment) -> showProgress());
        adapter.setLoadingClickListener((ignoredPosition, payment) -> {
            pb.setVisibility(View.VISIBLE);
            pb.bringToFront();
        });
    }

    public void onSwipe(RecyclerAdapter adapter, RecyclerView.ViewHolder viewHolder, int direction) {

        final int position = viewHolder.getBindingAdapterPosition();
        if (adapter.getPayment(position) != null) {
            Payment payment = adapter.getPayment(position);
            makePayment = payment;

            switch (direction) {

                case ItemTouchHelper.LEFT:
                    pb.setVisibility(View.VISIBLE);

                    Bill bill = repo.getBillByName(payment.getBillerName());
                    if (bill != null) {
                        Intent history = new Intent(MainActivity2.this, PaymentHistory.class);
                        history.putExtra("User Id", repo.getUid(MainActivity2.this));
                        history.putExtra("Bill Id", bill.getBillsId());
                        startActivity(history);
                    }
                    adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());

                    break;

                case ItemTouchHelper.RIGHT:
                    swipeRight(payment);
                    adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            }
            adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
        } else {
            repo.loadLocalData(MainActivity2.this, null);
            recreate();
        }
    }

    public void swipeRight(Payment payment) {
        if (!payment.isPaid()) {
            makePayment = payment;
            PaymentConfirm pc = new PaymentConfirm(MainActivity2.this);

            pc.setConfirmListener(v -> {
                // 1. Get current payments and sort
                ArrayList<Payment> paymentList = repo.getPayments();
                paymentList.sort(Comparator.comparing(Payment::getDueDate));

                // 2. We use the Bill Builder for the parent bill to track balance/remaining payments
                Bill.Builder billBuilder = repo.editBill(payment.getBillerName(), MainActivity2.this);
                Bill parentBill = repo.getBillByName(payment.getBillerName());

                double remainingToDistribute = paymentAmount;

                for (Payment pays : paymentList) {
                    if (remainingToDistribute <= 0) break;

                    if (pays.getBillerName().equals(payment.getBillerName()) && !pays.isPaid()) {
                        double amountNeeded = pays.getPaymentAmount() - pays.getPartialPayment();

                        // We use the Payment Builder for each payment found
                        Payment.Builder payBuilder = repo.editPayment(pays.getPaymentId(), MainActivity2.this);

                        if (remainingToDistribute < amountNeeded) {
                            // PARTIAL PAYMENT CASE
                            double newPartial = pays.getPartialPayment() + remainingToDistribute;
                            payBuilder.setPartialPayment(newPartial)
                                    .setDatePaid(newPaymentDate);

                            if (billBuilder != null) {
                                billBuilder.setBalance(parentBill.getBalance() - remainingToDistribute)
                                        .setDateLastPaid(newPaymentDate);
                            }

                            remainingToDistribute = 0;
                        } else {
                            // FULL PAYMENT CASE
                            remainingToDistribute -= amountNeeded;

                            payBuilder.setPaid(true)
                                    .setPartialPayment(0) // Reset partial as it's now fully paid
                                    .setDatePaid(newPaymentDate);

                            if (billBuilder != null) {
                                billBuilder.setPaymentsRemaining(parentBill.getPaymentsRemaining() - 1)
                                        .setBalance(parentBill.getBalance() - amountNeeded)
                                        .setDateLastPaid(newPaymentDate);
                            }
                        }
                    }
                }

                // 3. One single save call to rule them all (local + cloud batch)
                repo.saveData(MainActivity2.this, (success, message) -> {
                    if (success) {
                        NotificationManager.scheduleNotifications(MainActivity2.this);
                        pc.dismissDialog();
                        showProgress();
                    } else {
                        Notify.createPopup(MainActivity2.this, "Sync Error", null);
                    }
                });
            });

            // Close/Perimeter listeners remain similar...
            pc.setCloseListener(v -> {
                pc.dismissDialog();
                BillerManager.refreshPayments(MainActivity2.this);
                showProgress();
            });
            pc.setPerimeterListener(view -> {
                pc.dismissDialog();
                BillerManager.refreshPayments(MainActivity2.this);
                showProgress();
            });
            pc.show();

        } else {
            // Use your new deletePayment which already handles the Bill balance reversion
            repo.deletePayment(payment.getPaymentId(), MainActivity2.this, (success, message) -> {
                NotificationManager.scheduleNotifications(MainActivity2.this);
                showProgress();
            });
        }
    }

    private View buildHeader(double todayTotal, String timePeriod) {

        View header = View.inflate(MainActivity2.this, R.layout.bill_box_header, null);
        TextView timeFrame = header.findViewById(R.id.timeFrame), totalDue = header.findViewById(R.id.totalDue);
        timeFrame.setText(timePeriod);
        totalDue.setText(FixNumber.addSymbol(String.valueOf(todayTotal)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 100, 0, 100);
        header.setLayoutParams(lp);
        return header;

    }

    public void restart() {
        Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        loadingBills.setVisibility(View.VISIBLE);
        LocalDate currentDate = selectedDate;
        executorService.execute(() -> {
            runOnUiThread(() -> {
                if (selectedDate == null) {
                    selectedDate = LocalDate.now(ZoneId.systemDefault());
                }
                if (currentDate != null && currentDate != selectedDate) {
                    BillerManager.refreshPayments(MainActivity2.this);
                    setCurrentMonth();
                    counter = 0;
                    listBills();
                    findViewById(R.id.pieChartLayout).setVisibility(View.GONE);
                    MainPieChart.setupPieChart(MainActivity2.this, pieChart);
                    MainPieChart.loadPieChartData(MainActivity2.this, pieChart, remainingProgressBar, noBillsFound, todayRecycler, laterThisWeekRecycler, laterThisMonthRecycler, earlierThisMonthRecycler, scroll);
                }
                getValues();
                CountTickets.countTickets(MainActivity2.this);
                showProgress();
                repo.sortBills();
                loadingBills.setVisibility(View.GONE);
                pb.setVisibility(View.GONE);
            });
            handler.post(() -> loadingBills.setVisibility(View.GONE));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        restart();
        nc.navController(MainActivity2.this, MainActivity2.this, pb, "main");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restart();
        nc.navController(MainActivity2.this, MainActivity2.this, pb, "main");
    }

}