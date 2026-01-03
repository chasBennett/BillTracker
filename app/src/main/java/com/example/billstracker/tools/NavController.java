package com.example.billstracker.tools;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.billstracker.activities.MainActivity2.startAddBiller;
import static com.example.billstracker.activities.MainActivity2.tickets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.billstracker.R;
import com.example.billstracker.activities.AddBiller;
import com.example.billstracker.activities.CreateBudget;
import com.example.billstracker.activities.Login;
import com.example.billstracker.activities.MainActivity2;
import com.example.billstracker.activities.MyStats;
import com.example.billstracker.activities.PayBill;
import com.example.billstracker.activities.PaymentHistory;
import com.example.billstracker.activities.Settings;
import com.example.billstracker.activities.Spending;
import com.example.billstracker.activities.Support;
import com.example.billstracker.activities.ViewBillers;
import com.example.billstracker.activities.ViewBudget;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.AddExpense;
import com.example.billstracker.popup_classes.CustomDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Objects;

public class NavController {

    Window window;
    View scrollDetect;
    View topDetect;
    ScrollView scroll;
    Activity activity;
    ShapeableImageView settingsButton;
    ImageView payNext;
    ImageView addBiller;
    ImageView back;
    boolean iconFound;
    boolean blueBackground;
    String page;
    ConstraintLayout progressBar;


    @SuppressLint("ClickableViewAccessibility")
    public void navController (Context context, Activity activity, ConstraintLayout pb, String page) {

        this.activity = activity;
        scrollDetect = null;
        scroll = null;
        iconFound = false;
        blueBackground = false;
        window = this.activity.getWindow();
        this.page = page;
        progressBar = pb;

        //Toolbar
        back = this.activity.findViewById(R.id.backToHome);
        settingsButton = this.activity.findViewById(R.id.profileIcon);
        addBiller = this.activity.findViewById(R.id.btnAddBiller);
        payNext = this.activity.findViewById(R.id.payNext);

        int backgroundColor = activity.getColor(R.color.whiteAndBlack);

        if (page.equals("main") || page.equals("spending") || page.equals("budget")) {
            ImageView billsIcon = activity.findViewById(R.id.billsIcon);
            ImageView expensesIcon = activity.findViewById(R.id.expensesIcon);
            ImageView budgetIcon = activity.findViewById(R.id.budgetIcon);
            LinearLayout billsTab = activity.findViewById(R.id.billsTab);
            LinearLayout budgetTab = activity.findViewById(R.id.budgetTab);
            LinearLayout expensesTab = activity.findViewById(R.id.expensesTab);
            ImageView billsDot = activity.findViewById(R.id.billsDot);
            ImageView budgetDot = activity.findViewById(R.id.budgetDot);
            ImageView spendingDot = activity.findViewById(R.id.spendingDot);
            back.setVisibility(View.GONE);
            blueBackground = true;
            Tools.fixToolbar(activity, true, iconFound);

            switch (page) {
                case "main":
                    scroll = activity.findViewById(R.id.mainScroll);
                    scrollDetect = activity.findViewById(R.id.scrollDetect);
                    topDetect = activity.findViewById(R.id.topDetect3);
                    billsIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    billsTab.setBackgroundColor(backgroundColor);
                    billsDot.setVisibility(View.VISIBLE);
                    billsDot.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    spendingDot.setVisibility(View.GONE);
                    budgetDot.setVisibility(View.GONE);
                    ProgressBar loadingBills = activity.findViewById(R.id.loadingBills);
                    loadingBills.setIndeterminateTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white, context.getTheme())));
                    break;
                case "spending":
                    scrollDetect = activity.findViewById(R.id.scrollDetect1);
                    topDetect = activity.findViewById(R.id.topDetect1);
                    scroll = activity.findViewById(R.id.scroll7);
                    expensesIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    expensesTab.setBackgroundColor(backgroundColor);
                    billsDot.setVisibility(View.GONE);
                    spendingDot.setVisibility(View.VISIBLE);
                    spendingDot.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    budgetDot.setVisibility(View.GONE);
                    break;
                case "budget":
                    scrollDetect = activity.findViewById(R.id.scrollDetect2);
                    topDetect = activity.findViewById(R.id.topDetect2);
                    scroll = activity.findViewById(R.id.scroll7);
                    budgetIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    budgetTab.setBackgroundColor(backgroundColor);
                    billsDot.setVisibility(View.GONE);
                    spendingDot.setVisibility(View.GONE);
                    budgetDot.setVisibility(View.VISIBLE);
                    budgetDot.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    break;
            }

            billsTab.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                Intent main = new Intent (context, MainActivity2.class);
                context.startActivity(main);
            });
            expensesTab.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                Intent spending = new Intent(context, Spending.class);
                context.startActivity(spending);
            });
            budgetTab.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                if (!Repo.getInstance().getUser(activity).getBudgets().isEmpty()) {
                    Intent budget = new Intent(context, ViewBudget.class);
                    context.startActivity(budget);
                } else {
                    Intent createBudget = new Intent(context, CreateBudget.class);
                    context.startActivity(createBudget);
                }
            });
        }

        back.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Tools.onBackSelected((ComponentActivity) activity);
        });

        if (page.equals("paymentHistory") || page.equals("myStats") || page.equals("payBill")) {
            addBiller.setVisibility(View.GONE);
            blueBackground = false;
        }
        else if (page.equals("viewBillers")) {
            scroll = activity.findViewById(R.id.scroll14);
            topDetect = activity.findViewById(R.id.viewBillersHeader);
            scrollDetect = activity.findViewById(R.id.scrollDetect3);
            addBiller.setVisibility(View.GONE);
            blueBackground = true;
        }
        if (scroll != null) {
            scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        personalize();
        fixStatusBar();

        addBiller.setOnClickListener(view -> {
            switch (page) {
                case "main":
                    pb.setVisibility(View.VISIBLE);
                    startAddBiller = true;
                    context.startActivity(new Intent(context, AddBiller.class));
                    break;
                case "spending":
                    AddExpense ae = new AddExpense(activity, null);
                    ae.setSubmitExpenseListener(v1 -> activity.recreate());
                    break;
                case "budget":
                    pb.setVisibility(View.VISIBLE);
                    context.startActivity(new Intent(context, CreateBudget.class));
                    break;
            }
        });
        settingsButton.setOnClickListener(view -> {
            View popup = View.inflate(activity, R.layout.settings_popup, null);
            PopupWindow popupWindow = new PopupWindow(popup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            TextView displayUserName = popup.findViewById(R.id.tvName);
            TextView displayEmail = popup.findViewById(R.id.tvUserName2);
            TextView navHome = popup.findViewById(R.id.navHome);
            TextView viewBillers = popup.findViewById(R.id.navViewBillers);
            TextView paymentHistory = popup.findViewById(R.id.navPaymentHistory);
            TextView analysis = popup.findViewById(R.id.navAnalysis);
            TextView settings = popup.findViewById(R.id.navSettings);
            ConstraintLayout help = popup.findViewById(R.id.navHelp);
            TextView ticketCounter = popup.findViewById(R.id.ticketCounter);
            TextView logout = popup.findViewById(R.id.navLogout);
            popupWindow.setFocusable(true);
            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setContentView(popup);
            //popupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.border_stroke, activity.getTheme()));

            if (tickets > 0) {
                ticketCounter.setVisibility(View.VISIBLE);
                ticketCounter.setText(String.valueOf(tickets));
            } else {
                ticketCounter.setVisibility(View.GONE);
            }

            int[] values = new int[2];
            settingsButton.getLocationInWindow(values);
            int positionOfIcon = values[1];
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            int height = (displayMetrics.heightPixels * 2) / 4;
            if (positionOfIcon > height) {
                popupWindow.showAsDropDown(settingsButton, -600, -(settingsButton.getMeasuredHeight()) - 550);
            } else {
                popupWindow.showAsDropDown(settingsButton, -600, 25);
            }
            navHome.setOnClickListener(view1 -> {
                pb.setVisibility(View.VISIBLE);
                context.startActivity(new Intent(context, MainActivity2.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK));
                popupWindow.dismiss();
            });

            settings.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                context.startActivity(new Intent(context, Settings.class));
                popupWindow.dismiss();
            });

            viewBillers.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                context.startActivity(new Intent(context, ViewBillers.class));
                popupWindow.dismiss();
            });

            paymentHistory.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                context.startActivity(new Intent(context, PaymentHistory.class));
                popupWindow.dismiss();
            });

            analysis.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                context.startActivity(new Intent(context, MyStats.class));
                popupWindow.dismiss();
            });

            help.setOnClickListener(v -> {
                pb.setVisibility(View.VISIBLE);
                context.startActivity(new Intent(context, Support.class));
                popupWindow.dismiss();
            });

            logout.setOnClickListener(v -> {
                popupWindow.dismiss();
                pb.setVisibility(View.VISIBLE);
                logout();
            });

            if (Repo.getInstance().getUser(context) == null || Repo.getInstance().getUser(context).getName() == null || Repo.getInstance().getUser(context).getUserName() == null) {
                Repo.getInstance().loadLocalData(context);
            }
            if (Repo.getInstance().getUser(context).getName() != null && Repo.getInstance().getUser(context).getUserName() != null) {
                displayUserName.setText(Repo.getInstance().getUser(context).getName());
                displayEmail.setText(Repo.getInstance().getUser(context).getUserName());
            }
            else {
                this.activity.startActivity(new Intent(activity, Login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| FLAG_ACTIVITY_NEW_TASK| FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        payNext.setOnClickListener(view -> {

            pb.setVisibility(View.VISIBLE);
            Payment next = new Payment();
            next.setDueDate(DateFormat.makeLong(LocalDate.now(ZoneId.systemDefault()).plusDays(60)));
            boolean found = false;

            Repo.getInstance().getPayments().sort(Comparator.comparing(Payment::getDueDate));
            for (Payment payment : Repo.getInstance().getPayments()) {
                if (!payment.isPaid() && payment.getDueDate() < next.getDueDate()) {
                    next = payment;
                    found = true;
                }
            }

            if (found) {
                activity.startActivity(new Intent(activity, PayBill.class).putExtra("Payment Id", next.getPaymentId()));
            } else {
                pb.setVisibility(View.GONE);
                CustomDialog cd = new CustomDialog(activity, context.getString(R.string.noBillsDue), context.getString(R.string.noUpcomingBills), context.getString(R.string.ok), null, null);
                cd.setPositiveButtonListener(v -> cd.dismissDialog());
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public void personalize () {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
                    Tools.fixToolbar(activity, blueBackground, iconFound);
                    Glide.with(settingsButton).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).circleCrop().into(settingsButton);
                    iconFound = true;
                }
                else {
                    Glide.with(settingsButton).load(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.profile_icon, activity.getTheme())).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(settingsButton);
                }
            }
            else {
                Glide.with(settingsButton).load(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.profile_icon, activity.getTheme())).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(settingsButton);
            }
    }

    public void fixStatusBar () {

        if (scroll != null && scrollDetect != null && window != null && page != null && topDetect != null) {
                scrollDetect.post(() -> {
                    Rect scrollBounds = new Rect();
                    scroll.getHitRect(scrollBounds);
                    if (scrollDetect.getLocalVisibleRect(scrollBounds) || topDetect.getLocalVisibleRect(scrollBounds)) {
                        blueBackground = page.equals("main") || Objects.equals(page, "viewBillers") || Objects.equals(page, "budget") || Objects.equals(page, "spending");
                    } else {
                        blueBackground = false;
                    }
                    Tools.fixToolbar(this.activity, blueBackground, iconFound);
                    scroll.setOnScrollChangeListener((view1, i, i1, i2, i3) -> {
                        scroll.getHitRect(scrollBounds);
                        if (scrollDetect.getLocalVisibleRect(scrollBounds) || topDetect.getLocalVisibleRect(scrollBounds)) {
                            blueBackground = page.equals("main") || Objects.equals(page, "viewBillers") || Objects.equals(page, "budget") || Objects.equals(page, "spending");
                        } else {
                            blueBackground = false;
                        }
                        Tools.fixToolbar(this.activity, blueBackground, iconFound);
                    });
                });
        }
        else {
            if (window != null) {
                Tools.fixToolbar(this.activity, blueBackground, iconFound);
            }
        }
    }

    public void logout () {

        CustomDialog cd = new CustomDialog(activity, activity.getString(R.string.logout), activity.getString(R.string.are_you_sure_you_would_like_to_logout), activity.getString(R.string.logout), activity.getString(R.string.cancel), null);
        cd.setPositiveButtonListener(v -> {
            cd.dismissDialog();
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            Repo.getInstance().logout(activity);
        });
        cd.setNegativeButtonListener(view -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}