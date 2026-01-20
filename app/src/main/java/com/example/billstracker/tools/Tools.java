package com.example.billstracker.tools;

import static android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.example.billstracker.R;
import com.example.billstracker.activities.Login;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Budget;
import com.example.billstracker.custom_objects.Expense;
import com.example.billstracker.custom_objects.Payment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public interface Tools {


    static boolean isDarkMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    static void fixToolbar(Activity activity, boolean blueBackground, boolean iconFound) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        WindowInsetsControllerCompat wic = new WindowInsetsControllerCompat(window, window.getDecorView());
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean nightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        ImageView backToHome = null, payNext = null, addBiller = null, toolbarLogo = null;
        ShapeableImageView settings = null;
        LinearLayout toolbar = null;
        if (activity.findViewById(R.id.toolbar) != null) {
            toolbar = activity.findViewById(R.id.toolbar);
        }
        if (activity.findViewById(R.id.backToHome) != null) {
            backToHome = activity.findViewById(R.id.backToHome);
        }
        if (activity.findViewById(R.id.payNext) != null) {
            payNext = activity.findViewById(R.id.payNext);
        }
        if (activity.findViewById(R.id.btnAddBiller) != null) {
            addBiller = activity.findViewById(R.id.btnAddBiller);
        }
        if (activity.findViewById(R.id.toolbarLogo) != null) {
            toolbarLogo = activity.findViewById(R.id.toolbarLogo);
        }
        if (activity.findViewById(R.id.profileIcon) != null) {
            settings = activity.findViewById(R.id.profileIcon);
        }
        if (!blueBackground) {
            if (toolbar != null) {
                toolbar.setBackgroundColor(activity.getResources().getColor(R.color.whiteAndBlack, activity.getTheme()));
            }
            if (backToHome != null) {
                backToHome.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.grayAndWhite, activity.getTheme())));
            }
            if (payNext != null) {
                payNext.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.grayAndWhite, activity.getTheme())));
            }
            if (addBiller != null) {
                addBiller.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.grayAndWhite, activity.getTheme())));
            }
            if (toolbarLogo != null && !nightMode) {
                toolbarLogo.setImageTintList(null);
            }
            if (settings != null) {
                if (!iconFound) {
                    settings.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.grayAndWhite, activity.getTheme())));
                } else {
                    settings.setImageTintList(null);
                }
            }
            window.setStatusBarColor(ResourcesCompat.getColor(activity.getResources(), R.color.whiteAndBlack, activity.getTheme()));
        } else {
            if (toolbar != null) {
                toolbar.setBackgroundColor(activity.getResources().getColor(R.color.blueAndBlack, activity.getTheme()));
            }
            if (backToHome != null) {
                backToHome.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme())));
            }
            if (payNext != null) {
                payNext.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme())));
            }
            if (addBiller != null) {
                addBiller.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme())));
            }
            if (toolbarLogo != null && !nightMode) {
                toolbarLogo.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme())));
            }
            if (settings != null) {
                if (!iconFound) {
                    settings.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme())));
                } else {
                    settings.setImageTintList(null);
                }
            }
            window.setStatusBarColor(ResourcesCompat.getColor(activity.getResources(), R.color.blueAndBlack, activity.getTheme()));
        }
        if (nightMode && toolbarLogo != null) {
            toolbarLogo.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme())));
        }
        wic.setAppearanceLightStatusBars(!blueBackground && !nightMode);
    }

    static void setOnClickListener(View view, OnClickListener clickListener) {
        if (view != null && view.getContext() != null) {
            Context context = view.getContext();
            View.OnClickListener listener = view1 -> {
                InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                clickListener.onClick(true);
            };
            view.setOnClickListener(listener);
        }
    }

    static void hideKeyboard(Activity activity) {

        if (activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isAcceptingText()) {
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    static void showKeyboard(EditText editText) {
        if (editText != null && editText.getContext() != null) {
            Context context = editText.getContext();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    static void openEmailApp(Context context) {
        List<Intent> emailAppLauncherIntents = new ArrayList<>();

        Intent emailAppIntent = new Intent(Intent.ACTION_SENDTO);
        emailAppIntent.setData(Uri.parse("mailto:"));
        emailAppIntent.putExtra(Intent.EXTRA_EMAIL, "");
        emailAppIntent.putExtra(Intent.EXTRA_SUBJECT, "");

        PackageManager packageManager = context.getPackageManager();

        List<ResolveInfo> emailApps = packageManager.queryIntentActivities(emailAppIntent, PackageManager.MATCH_ALL);

        for (ResolveInfo resolveInfo : emailApps) {
            emailAppLauncherIntents.add(packageManager.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName));
        }

        context.startActivity(Intent.createChooser(new Intent(), context.getString(R.string.select_email_app)).putExtra(Intent.EXTRA_INITIAL_INTENTS, emailAppLauncherIntents.toArray(new Parcelable[0])));
        context.startActivity(new Intent(context, Login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    static void spinnerPopup(ArrayList<String> list, ArrayList<Integer> icons, TextView spinner, ItemSelectedListener itemSelectedListener) {
        if (spinner != null && spinner.getContext() != null) {
            Context context = spinner.getContext();
            PopupWindow popupWindow = new PopupWindow(context);
            LinearLayout layout = new LinearLayout(context);
            NestedScrollView scroll = new NestedScrollView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.VERTICAL);
            scroll.setLayoutParams(params);
            popupWindow.setElevation(10);
            scroll.setElevation(10);
            layout.setLayoutParams(params);
            layout.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.whiteAndBlack, context.getTheme()));
            scroll.addView(layout);
            if (list != null && !list.isEmpty()) {
                for (String string : list) {
                    View spinnerItem = View.inflate(context, R.layout.spinner_dropdown_item, null);
                    spinnerItem.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.border_stroke, context.getTheme()));
                    TextView itemName = spinnerItem.findViewById(R.id.dropdownText);
                    int dp10 = Tools.createDPValue(context, 10);
                    int dp20 = Tools.createDPValue(context, 20);
                    spinnerItem.setPadding(dp20, dp10, dp20, dp10);
                    itemName.setText(string);
                    ImageView image = spinnerItem.findViewById(R.id.dropDownImage);
                    if (icons != null && icons.size() >= list.indexOf(string)) {
                        image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), icons.get(list.indexOf(string)), context.getTheme()));
                        image.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.grayAndWhite, context.getTheme())));
                    } else {
                        image.setVisibility(View.GONE);
                    }
                    layout.addView(spinnerItem);
                    spinnerItem.setOnClickListener(view -> {
                        itemSelectedListener.itemSelected(string);
                        spinner.setText(string);
                        popupWindow.dismiss();
                    });
                    layout.setDividerDrawable(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL).getDrawable());
                    layout.setShowDividers(SHOW_DIVIDER_MIDDLE);
                }
            } else {
                View spinnerItem = View.inflate(context, R.layout.spinner_item, null);
                TextView itemName = spinnerItem.findViewById(R.id.spinnerText);
                itemName.setText(context.getString(R.string.no_data_available));
                layout.addView(spinnerItem);
                spinnerItem.setOnClickListener(view -> {
                    itemSelectedListener.itemSelected(itemName.getText().toString());
                    popupWindow.dismiss();
                });
            }

            popupWindow.setFocusable(true);
            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setContentView(scroll);

            int[] values = new int[2];
            spinner.getLocationInWindow(values);
            int positionOfIcon = values[1];
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int height = (displayMetrics.heightPixels * 2) / 4;
            if (positionOfIcon > height) {
                popupWindow.showAsDropDown(spinner, 50, -(spinner.getMeasuredHeight()) - 550);
            } else {
                popupWindow.showAsDropDown(spinner, 25, 25);
            }
        }
    }

    static void setEditTextChecked(EditText editText, boolean isAcceptable) {
        if (editText.getContext() != null) {
            Context context = editText.getContext();
            if (isAcceptable && !editText.getText().toString().isEmpty()) {
                editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.border_stroke_blue, context.getTheme()));
            } else {
                editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.border_stroke, context.getTheme()));
            }
        }
    }

    static boolean isValidString(EditText editText, int minimumLength) {
        if (editText != null && editText.getText() != null) {
            if (!editText.getText().toString().isEmpty()) {
                setEditTextChecked(editText, editText.getText().toString().length() >= minimumLength);
                return editText.getText().toString().length() >= minimumLength;
            } else {
                setEditTextChecked(editText, false);
                return false;
            }
        } else {
            return false;
        }
    }

    static void addValidEmailListener(EditText editText) {
        editText.addTextChangedListener(new Watcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isValidEmail(editText);
            }
        });
    }

    static boolean isValidEmail(EditText editText) {
        if (editText != null && editText.getText() != null) {
            if (!editText.getText().toString().isEmpty()) {
                setEditTextChecked(editText, android.util.Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches());
                return android.util.Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches();
            } else {
                setEditTextChecked(editText, false);
                return false;
            }
        } else {
            return false;
        }
    }

    static void onBackSelected(ComponentActivity activity) {
        activity.getOnBackPressedDispatcher().onBackPressed();
    }

    static Budget getBudget(Context context, LocalDate selectedDate) {
        if (Repository.getInstance().getUser(context) == null) {
            Repository.getInstance().loadLocalData(context, null);
        }

        if (Repository.getInstance().getUser(context).getBudgets() != null && !Repository.getInstance().getUser(context).getBudgets().isEmpty()) {
            for (Budget budget : Repository.getInstance().getUser(context).getBudgets()) {
                if (budget.getStartDate() <= DateFormat.makeLong(selectedDate) && budget.getEndDate() >= DateFormat.makeLong(selectedDate)) {
                    return budget;
                }
            }
        }
        return null;
    }

    static void requestPermissionLauncher(Activity activity, ActivityResultLauncher<String> launcher) {

        if (Build.VERSION.SDK_INT >= 33) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
                if (activity.checkSelfPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED) {
                    Prefs.setNotificationPreference(activity, true);
                }
            }
        }
    }

    static void billPaidOff(Context context, Payment payment) {
        if (Repository.getInstance().getBills() != null) {
            Bill biller = null;
            for (Bill bill : Repository.getInstance().getBills()) {
                if (bill.getBillerName().equals(payment.getBillerName())) {
                    bill.setPaymentsRemaining(0);
                    biller = bill;
                    break;
                }
            }
            if (biller != null && Repository.getInstance().getPayments() != null) {
                ArrayList<Payment> remove = new ArrayList<>();
                for (Payment payments : Repository.getInstance().getPayments()) {
                    if (payments.getBillerName().equals(biller.getBillerName()) && !payments.isPaid() && payments.isDateChanged()) {
                        remove.add(payments);
                    }
                }
                if (!remove.isEmpty()) {
                    Repository.getInstance().getPayments().removeAll(remove);
                }
            }
            Repository.getInstance().saveData(context, (wasSuccessful, message) -> {});
        }
    }

    static void fixLogo(ImageView view) {
        if (view.getContext() != null) {
            Context context = view.getContext();
            if (isDarkMode(context)) {
                view.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.white, context.getTheme())));
            } else {
                view.setImageTintList(null);
            }
        }
    }

    static void fixProgressBarLogo(ConstraintLayout pb) {
        if (pb.getContext() != null) {
            Context context = pb.getContext();
            ImageView view = pb.findViewById(R.id.pbLogo);
            if (isDarkMode(context)) {
                view.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.white, context.getTheme())));
            } else {
                view.setImageTintList(null);
            }
        }
    }

    static double getBillsAmount(int frequency, LocalDate selectedDate) {
        double billsAmount = 0;
        if (Repository.getInstance().getPayments() != null) {
            if (Repository.getInstance().getPayments() != null) {
                for (Payment payment : Repository.getInstance().getPayments()) {
                    switch (frequency) {
                        case 0:
                            if (payment.getDueDate() == DateFormat.makeLong(selectedDate)) {
                                billsAmount = billsAmount + payment.getPaymentAmount();
                            }
                        case 1:
                            if (payment.getDueDate() >= DateFormat.makeLong(selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))) &&
                                    payment.getDueDate() <= DateFormat.makeLong(selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))) {
                                billsAmount = billsAmount + payment.getPaymentAmount();
                            }
                        case 2:
                            if (payment.getDueDate() >= DateFormat.makeLong(selectedDate.withDayOfMonth(1)) && payment.getDueDate() <= DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()))) {
                                billsAmount = billsAmount + payment.getPaymentAmount();
                            }
                    }
                }
                return billsAmount;
            }
        }
        return billsAmount;
    }

    static int createDPValue(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    static void fadeOutAndRemove(View view, OnCompleteListener listener) {
        AlphaAnimation animation = new AlphaAnimation(1f, 0f);
        animation.setDuration(1200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                listener.isFinished(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    @SuppressLint("ClickableViewAccessibility")
    static void setupUI(Activity activity, View view) {

        int CLICK_THRESHOLD = 100;
        if (!(view instanceof TextView) && !view.hasOnClickListeners()) {
            view.setOnTouchListener((View view1, MotionEvent motionEvent) -> {
                long duration = motionEvent.getEventTime() - motionEvent.getDownTime();
                if (motionEvent.getAction() != MotionEvent.ACTION_SCROLL && motionEvent.getAction() == MotionEvent.ACTION_UP && duration < CLICK_THRESHOLD) {
                    hideKeyboard(activity);
                    if (activity.getCurrentFocus() != null) {
                        if (activity.getCurrentFocus() instanceof EditText || activity.getCurrentFocus() instanceof TextInputEditText) {
                            EditText text = (EditText) activity.getCurrentFocus();
                            text.setFocusable(false);
                            text.setOnTouchListener((v, event) -> {
                                text.setFocusableInTouchMode(true);
                                return false;
                            });
                        }
                    }
                }
                return false;
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(activity, innerView);
            }
        }
    }

    static void loadIcon(ShapeableImageView view, int category, String path) {

        if (view.getContext() != null) {
            Context context = view.getContext();

            boolean darkMode = Tools.isDarkMode(context);

            ArrayList<Integer> icons = DataTools.getIcons();

            view.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.grey, context.getTheme())));
            if (darkMode) {
                view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme()));
                view.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black, context.getTheme())));
            }

            if (path.contains("default")) {
                view.setContentPadding(30, 30, 30, 30);
                view.setPadding(0, 0, 0, 0);
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(context).load(icons.get(category)).into(view);
            } else {
                view.setImageTintList(null);
                view.setContentPadding(20, 20, 20, 20);
                view.setPadding(0, 0, 0, 0);
                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(context).load(path).fitCenter().into(view);
            }
        }
    }

    static void removePartnerData(String partnerId) {
        ArrayList<Bill> removeBills = new ArrayList<>();
        if (Repository.getInstance().getBills() != null) {
            for (Bill bill : Repository.getInstance().getBills()) {
                if (bill.getOwner().equals(partnerId)) {
                    removeBills.add(bill);
                }
            }
            Repository.getInstance().getBills().removeAll(removeBills);
        }
        ArrayList<Payment> removePayments = new ArrayList<>();
        if (Repository.getInstance().getPayments() != null) {
            for (Payment payment : Repository.getInstance().getPayments()) {
                if (payment.getOwner().equals(partnerId)) {
                    removePayments.add(payment);
                }
            }
            Repository.getInstance().getPayments().removeAll(removePayments);
        }
        ArrayList<Expense> removeExpense = new ArrayList<>();
        if (Repository.getInstance().getExpenses() != null) {
            for (Expense expense : Repository.getInstance().getExpenses()) {
                if (expense.getOwner().equals(partnerId)) {
                    removeExpense.add(expense);
                }
            }
            Repository.getInstance().getExpenses().removeAll(removeExpense);
        }
    }


    interface OnClickListener {
        void onClick(boolean isClicked);
    }

    interface ItemSelectedListener {
        void itemSelected(String item);
    }

    interface OnCompleteListener {
        void isFinished(boolean isFinished);
    }
}
