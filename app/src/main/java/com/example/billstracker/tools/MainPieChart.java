package com.example.billstracker.tools;

import static com.example.billstracker.activities.MainActivity2.dueThisMonth;
import static com.example.billstracker.activities.MainActivity2.selectedDate;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.activities.AddBiller;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.recycler_adapters.RecyclerAdapter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MainPieChart {

    public static void setupPieChart(Activity activity, PieChart pieChart) {
        activity.findViewById(R.id.pieChartLayout).setVisibility(View.GONE);
        activity.findViewById(R.id.lineChartLoading).setVisibility(View.VISIBLE);
        pieChart.setNoDataText(activity.getString(R.string.loading_));
        pieChart.setNoDataTextColor(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme()));
        pieChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(activity.getResources().getColor(R.color.white, activity.getTheme()));
        pieChart.setEntryLabelTextSize(8);
        pieChart.canResolveTextAlignment();
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(85f);
        pieChart.setHoleColor(ResourcesCompat.getColor(activity.getResources(), R.color.blueAndBlack, activity.getTheme()));
        pieChart.invalidate();
    }

    public static void loadPieChartData(Activity activity, PieChart pieChart, ProgressBar pb, TextView noResults, RecyclerView today, RecyclerView later, RecyclerView evenLater, RecyclerView earlier, ScrollView scroll) {

        ArrayList<PieEntry> entries = new ArrayList<>();
        PieDataSet dataSet;
        long monthStart = DateFormat.makeLong(selectedDate.withDayOfMonth(1));
        long monthEnd = DateFormat.makeLong(selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()));
        double autoLoans = 0;
        double creditCards = 0;
        double entertainment = 0;
        double insurance = 0;
        double miscellaneous = 0;
        double mortgage = 0;
        double personalLoan = 0;
        double utilities = 0;
        double remaining = 0;
        double total = 0;
        int counter = 0;
        ArrayList<Integer> foundPayments = new ArrayList<>();

        if (Repository.getInstance().getPayments() != null) {
            for (Payment payment : Repository.getInstance().getPayments()) {
                if (payment.getDueDate() >= monthStart && payment.getDueDate() <= monthEnd && !foundPayments.contains(payment.getPaymentId())) {
                    foundPayments.add(payment.getPaymentId());
                    total = total + payment.getPaymentAmount();
                    ++counter;
                    if (Repository.getInstance().getBills() != null) {
                        for (Bill bill : Repository.getInstance().getBills()) {
                            if (bill.getBillerName().equals(payment.getBillerName())) {
                                if (!payment.isPaid()) {
                                    remaining = remaining + (payment.getPaymentAmount() - payment.getPartialPayment());
                                }
                                switch (bill.getCategory()) {
                                    case 0:
                                        autoLoans = autoLoans + payment.getPaymentAmount();
                                        break;
                                    case 1:
                                        creditCards = creditCards + payment.getPaymentAmount();
                                        break;
                                    case 2:
                                        entertainment = entertainment + payment.getPaymentAmount();
                                        break;
                                    case 3:
                                        insurance = insurance + payment.getPaymentAmount();
                                        break;
                                    case 4:
                                        miscellaneous = miscellaneous + payment.getPaymentAmount();
                                        break;
                                    case 5:
                                        mortgage = mortgage + payment.getPaymentAmount();
                                        break;
                                    case 6:
                                        personalLoan = personalLoan + payment.getPaymentAmount();
                                        break;
                                    case 7:
                                        utilities = utilities + payment.getPaymentAmount();
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }

        ArrayList<String> strings = DataTools.getCategories(activity);
        ArrayList<Double> values = new ArrayList<>(Arrays.asList(autoLoans, creditCards, entertainment, insurance, miscellaneous, mortgage, personalLoan, utilities));

        if (counter > 0) {
            if (utilities > 0) {
                entries.add(0, new PieEntry((float) ((utilities * 100) / total), strings.get(7) + " " + FixNumber.addSymbol(utilities), strings.get(7)));
            }
            if (personalLoan > 0) {
                entries.add(0, new PieEntry((float) ((personalLoan * 100) / total), strings.get(6) + " " + FixNumber.addSymbol(personalLoan), strings.get(6)));
            }
            if (mortgage > 0) {
                entries.add(0, new PieEntry((float) ((mortgage * 100) / total), strings.get(5) + " " + FixNumber.addSymbol(mortgage), strings.get(5)));
            }
            if (miscellaneous > 0) {
                entries.add(0, new PieEntry((float) ((miscellaneous * 100) / total), strings.get(4) + " " + FixNumber.addSymbol(miscellaneous), strings.get(4)));
            }
            if (insurance > 0) {
                entries.add(0, new PieEntry((float) ((insurance * 100) / total), strings.get(3) + " " + FixNumber.addSymbol(insurance), strings.get(3)));
            }
            if (entertainment > 0) {
                entries.add(0, new PieEntry((float) ((entertainment * 100) / total), strings.get(2) + " " + FixNumber.addSymbol(entertainment), strings.get(2)));
            }
            if (creditCards > 0) {
                entries.add(0, new PieEntry((float) ((creditCards * 100) / total), strings.get(1) + " " + FixNumber.addSymbol(creditCards), strings.get(1)));
            }
            if (autoLoans > 0) {
                entries.add(0, new PieEntry((float) ((autoLoans * 100) / total), strings.get(0) + " " + FixNumber.addSymbol(autoLoans), strings.get(0)));
            }
        } else {
            entries.add(0, new PieEntry(100, "No Bills Added"));
        }

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (dueThisMonth != null) {
                    onTouch(e.getData().toString(), today, later, evenLater, earlier, scroll);
                    pieChart.highlightValue(null);
                    pieChart.getOnTouchListener().setLastHighlighted(null);
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : activity.getResources().getIntArray(R.array.pieChartCorresponding)) {
            colors.add(color);
        }
        ArrayList<Integer> pieChartColors = new ArrayList<>();
        if (counter > 0) {
            for (int color : colors) {
                if (values.get(colors.indexOf(color)) > 0) {
                    pieChartColors.add(color);
                }
            }
        } else {
            pieChartColors.add(ResourcesCompat.getColor(activity.getResources(), R.color.payBill, activity.getTheme()));
        }

        LinearLayout pieChartLayout = activity.findViewById(R.id.pieChartLayout);
        LinearLayout labelsLayout = activity.findViewById(R.id.pieChartLabels);
        LinearLayout lineChartLoading = activity.findViewById(R.id.lineChartLoading);
        lineChartLoading.setVisibility(View.GONE);
        pieChartLayout.setVisibility(View.VISIBLE);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        final double[] progress = {0};
        if (remaining == 0) {
            progress[0] = 100;
        } else {
            progress[0] = 100 - ((remaining) * 100) / (total);
        }
        pb.post(() -> {
            if (progress[0] < 1) {
                progress[0] = 0;
            }
            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", (int) progress[0]);
            animation.setDuration(500);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
        });

        dataSet = new PieDataSet(entries, "");
        dataSet.setColors(pieChartColors);
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        if (counter > 0) {
            pieChart.setCenterText(activity.getString(R.string.remaining3) + FixNumber.addSymbol(remaining) + activity.getString(R.string.total3) + FixNumber.addSymbol(total));
            pieChart.setCenterTextColor(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme()));
        } else {
            pieChart.setCenterText(activity.getString(R.string.nothing_due_this_month));
            pieChart.setCenterTextColor(ResourcesCompat.getColor(activity.getResources(), R.color.white, activity.getTheme()));
        }

        pieChart.setData(data);
        pieChart.setNoDataText("");
        pieChart.invalidate();
        activity.findViewById(R.id.lineChartLoading).setVisibility(View.GONE);
        pieChart.animateY(1400, Easing.EaseOutCirc);
        pieChart.bringToFront();
        pieChart.setHoleColor(android.R.color.transparent);
        displayValues(labelsLayout, values, strings, colors, today, later, evenLater, earlier, scroll, counter);
    }

    public static void displayValues(LinearLayout layout, ArrayList<Double> values, ArrayList<String> strings, ArrayList<Integer> colors, RecyclerView today, RecyclerView later, RecyclerView evenLater, RecyclerView earlier, ScrollView scroll,
                                     int counter) {
        if (layout.getContext() != null) {
            layout.removeAllViews();
            layout.invalidate();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Context context = layout.getContext();
            if (counter > 0) {
                for (Double value : values) {
                    if (value > 0) {
                        TextView textView = new TextView(context);
                        Drawable circle = ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme());
                        if (circle != null) {
                            circle.setBounds(0, 0, 30, 30);
                        }
                        textView.setCompoundDrawables(circle, null, null, null);
                        textView.setCompoundDrawablePadding(20);
                        TextViewCompat.setCompoundDrawableTintList(textView, ColorStateList.valueOf(colors.get(values.indexOf(value))));
                        textView.setText(strings.get(values.indexOf(value)));
                        textView.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.white, context.getTheme()));
                        textView.setTextSize(12);
                        textView.setLayoutParams(params);
                        textView.setTypeface(Typeface.DEFAULT_BOLD);
                        layout.addView(textView);
                        String query = textView.getText().toString().replaceAll("[^A-Za-z ]", "");
                        textView.setOnClickListener(view -> onTouch(query, today, later, evenLater, earlier, scroll));
                    }
                }
            } else {
                TextView textView = new TextView(context);
                Drawable circle = ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme());
                if (circle != null) {
                    circle.setBounds(0, 0, 30, 30);
                }
                textView.setCompoundDrawables(circle, null, null, null);
                textView.setCompoundDrawablePadding(20);
                TextViewCompat.setCompoundDrawableTintList(textView, ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.payBill, context.getTheme())));
                textView.setText(context.getString(R.string.addABiller));
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.white, context.getTheme()));
                textView.setTextSize(13);
                params.setMarginEnd(50);
                textView.setLayoutParams(params);
                layout.addView(textView);
                textView.setOnClickListener(view -> context.startActivity(new Intent(context, AddBiller.class)));
            }
        }
    }

    public static void onTouch(String selection, RecyclerView today, RecyclerView later, RecyclerView evenLater, RecyclerView earlier, ScrollView scroll) {
        ArrayList<Payment> recycles = new ArrayList<>(dueThisMonth);
        ArrayList<String> categories = DataTools.getCategories(scroll.getContext());
        for (String cat : categories) {
            cat = cat.replaceAll("[^A-Za-z ]", "");
        }
        recycles.sort(Comparator.comparing(Payment::isPaid));
        RecyclerAdapter adapter;
        Animation ani = new TranslateAnimation(0, 30, 0, 0);
        ani.setDuration(200);
        ani.setStartOffset(200);
        ani.setRepeatMode(Animation.REVERSE);
        ani.setRepeatCount(1);
        boolean found = false;
        int[] pos = new int[2];
        if (!recycles.isEmpty()) {
            adapter = (RecyclerAdapter) today.getAdapter();
            if (adapter != null && adapter.getArrayList() != null) {
                for (Payment pay : adapter.getArrayList()) {
                    if (recycles.contains(pay)) {
                        for (Bill bill : Repository.getInstance().getBills()) {
                            if (bill.getBillerName().equals(pay.getBillerName()) && bill.getCategory() == categories.indexOf(selection)) {
                                RecyclerView.ViewHolder viewHolder = today.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                if (viewHolder != null) {
                                    if (!found) {
                                        viewHolder.itemView.getLocationInWindow(pos);
                                        scroll.smoothScrollTo(pos[0], pos[1] - 400);
                                    }
                                    viewHolder.itemView.startAnimation(ani);
                                    found = true;
                                }
                            }
                        }
                    }
                }
            }
            adapter = (RecyclerAdapter) later.getAdapter();
            if (adapter != null) {
                if (adapter.getArrayList() != null) {
                    for (Payment pay : adapter.getArrayList()) {
                        if (recycles.contains(pay)) {
                            for (Bill bill : Repository.getInstance().getBills()) {
                                if (bill.getBillerName().equals(pay.getBillerName()) && bill.getCategory() == categories.indexOf(selection)) {
                                    RecyclerView.ViewHolder viewHolder = later.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                    if (viewHolder != null) {
                                        if (!found) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1] - 400);
                                        }
                                        viewHolder.itemView.startAnimation(ani);
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            adapter = (RecyclerAdapter) evenLater.getAdapter();
            if (adapter != null) {
                if (adapter.getArrayList() != null) {
                    for (Payment pay : adapter.getArrayList()) {
                        if (recycles.contains(pay)) {
                            for (Bill bill : Repository.getInstance().getBills()) {
                                if (bill.getBillerName().equals(pay.getBillerName()) && bill.getCategory() == categories.indexOf(selection)) {
                                    RecyclerView.ViewHolder viewHolder = evenLater.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                    if (viewHolder != null) {
                                        if (!found) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1] - 400);
                                        }
                                        viewHolder.itemView.startAnimation(ani);
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            adapter = (RecyclerAdapter) earlier.getAdapter();
            if (adapter != null) {
                if (adapter.getArrayList() != null) {
                    for (Payment pay : adapter.getArrayList()) {
                        if (recycles.contains(pay)) {
                            for (Bill bill : Repository.getInstance().getBills()) {
                                if (bill.getBillerName().equals(pay.getBillerName()) && bill.getCategory() == categories.indexOf(selection)) {
                                    RecyclerView.ViewHolder viewHolder = earlier.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                    if (viewHolder != null) {
                                        if (!found) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1] - 400);
                                        }
                                        viewHolder.itemView.startAnimation(ani);
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
