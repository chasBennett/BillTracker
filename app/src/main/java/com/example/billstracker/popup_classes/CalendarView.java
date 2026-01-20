package com.example.billstracker.popup_classes;

import static com.example.billstracker.activities.MainActivity2.dueThisMonth;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.recycler_adapters.RecyclerAdapter;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;

public class CalendarView {

    public static void create(Context context, LocalDate date, LinearLayout container, RecyclerView today, RecyclerView later, RecyclerView evenLater, RecyclerView earlier, ScrollView scroll) {

        container.removeAllViews();
        container.invalidate();
        int daysInPriorMonth = date.minusMonths(1).lengthOfMonth();
        int priorMonthDaysToShow;
        int totalDays = date.lengthOfMonth();
        int todayDay = date.getDayOfMonth() + 1;
        int weekCounter = 7;
        int dayCounter = 1;
        LocalDate firstDay = date.withDayOfMonth(1);
        priorMonthDaysToShow = firstDay.getDayOfWeek().getValue();

        LinearLayout firstWeek = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        firstWeek.setLayoutParams(lp);
        container.addView(firstWeek);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        while (priorMonthDaysToShow > 0) {
            View pastMonthDay = View.inflate(context, R.layout.past_month_day, null);
            TextView dateView = pastMonthDay.findViewById(R.id.dateNumber);
            pastMonthDay.setLayoutParams(lp2);
            --priorMonthDaysToShow;
            dateView.setText(String.valueOf(daysInPriorMonth - priorMonthDaysToShow));
            weekCounter = weekCounter - 1;
            firstWeek.addView(pastMonthDay);
        }
        while (totalDays > 0) {
            if (weekCounter > 0) {
                View dayBlock = View.inflate(context, R.layout.day_block, null);
                TextView dateView = dayBlock.findViewById(R.id.dateNumber);
                TextView amountDue = dayBlock.findViewById(R.id.dateAmountDue);
                TextView dayStatus = dayBlock.findViewById(R.id.dayStatusBar);
                dateView.setText(String.valueOf(dayCounter));
                ++dayCounter;
                --totalDays;
                --weekCounter;
                firstWeek.addView(dayBlock);
                dayBlock.setLayoutParams(lp1);
                if (dayCounter == todayDay && date.getMonth() == LocalDate.now(ZoneId.systemDefault()).getMonth()) {
                    dateView.setBackground(AppCompatResources.getDrawable(context, R.drawable.circle));
                    dateView.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.billsapp_background, context.getTheme())));
                    dateView.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
                }
                double total = 0;
                boolean paid = true;
                for (Payment payment : dueThisMonth) {
                    if (DateFormat.makeLocalDate(payment.getDueDate()).getDayOfMonth() + 1 == dayCounter) {
                        total = total + payment.getPaymentAmount();
                        paid = payment.isPaid();
                    }
                }
                if (total > 0) {
                    if (FixNumber.addSymbol((FixNumber.makeDouble(String.valueOf(total)))).length() > 7 && FixNumber.addSymbol((FixNumber.makeDouble(String.valueOf(total)))).contains(".")) {
                        amountDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(total))).substring(0, FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(total))).indexOf('.')));
                    } else {
                        amountDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(total))));
                    }
                    if (paid) {
                        amountDue.setTextColor(context.getResources().getColor(R.color.payBill, context.getTheme()));
                        dayStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.payBill, context.getTheme())));
                    } else {
                        dayStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.primary, context.getTheme())));
                    }
                } else {
                    amountDue.setText("");
                    dayStatus.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(android.R.color.transparent, context.getTheme())));
                }
                int finalDayCounter = dayCounter;
                dayBlock.setOnClickListener(v -> {
                    ArrayList<Payment> recycles = new ArrayList<>();
                    for (Payment payment : dueThisMonth) {
                        if (DateFormat.makeLocalDate(payment.getDueDate()).getDayOfMonth() == finalDayCounter - 1) {
                            recycles.add(payment);
                        }
                    }
                    recycles.sort(Comparator.comparing(Payment::isPaid));
                    RecyclerAdapter adapter;
                    Animation ani = new TranslateAnimation(0, 30, 0, 0);
                    ani.setDuration(200);
                    ani.setStartOffset(200);
                    ani.setRepeatMode(Animation.REVERSE);
                    ani.setRepeatCount(1);
                    if (!recycles.isEmpty()) {
                        adapter = (RecyclerAdapter) today.getAdapter();
                        if (adapter != null) {
                            if (adapter.getArrayList() != null) {
                                for (Payment pay : adapter.getArrayList()) {
                                    if (recycles.contains(pay)) {
                                        RecyclerView.ViewHolder viewHolder = today.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                        int[] pos = new int[2];
                                        if (viewHolder != null) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1]);
                                            viewHolder.itemView.startAnimation(ani);
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
                                        RecyclerView.ViewHolder viewHolder = later.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                        int[] pos = new int[2];
                                        if (viewHolder != null) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1]);
                                            viewHolder.itemView.startAnimation(ani);
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
                                        RecyclerView.ViewHolder viewHolder = evenLater.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                        int[] pos = new int[2];
                                        if (viewHolder != null) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1]);
                                            viewHolder.itemView.startAnimation(ani);
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
                                        RecyclerView.ViewHolder viewHolder = earlier.findViewHolderForAdapterPosition(adapter.getArrayList().indexOf(pay));
                                        int[] pos = new int[2];
                                        if (viewHolder != null) {
                                            viewHolder.itemView.getLocationInWindow(pos);
                                            scroll.smoothScrollTo(pos[0], pos[1]);
                                            viewHolder.itemView.startAnimation(ani);
                                        }
                                    }
                                }
                            }
                        }
                    }

                });
            } else {
                firstWeek = new LinearLayout(context);
                container.addView(firstWeek);
                weekCounter = 7;
            }
        }
        dayCounter = 1;
        while (weekCounter > 0) {
            View futureMonthDay = View.inflate(context, R.layout.past_month_day, null);
            TextView dateView = futureMonthDay.findViewById(R.id.dateNumber);
            dateView.setText(String.valueOf(dayCounter));
            ++dayCounter;
            --weekCounter;
            firstWeek.addView(futureMonthDay);
            futureMonthDay.setLayoutParams(lp2);
        }
    }
}
