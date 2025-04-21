package com.example.billstracker.popup_classes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.R;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class DatePicker extends DialogFragment {

    public static View.OnClickListener listener;

    public void setListener(View.OnClickListener listener) {
        DatePicker.listener = listener;
    }

    View picker;
    TextView header, cancel, submit, pickedDate;
    ImageView backMonth, forwardMonth;
    LinearLayout calendar;
    public static LocalDate selection;
    LocalDate savedDate;
    String headerString;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        selection = null;
        Bundle mArgs = getArguments();
        if (mArgs != null) {
            selection = DateFormat.makeLocalDate(mArgs.getLong("selection"));
            savedDate = DateFormat.makeLocalDate(mArgs.getLong("selection"));
            headerString = mArgs.getString("headerString");
        }
        if (selection == null) {
            selection = LocalDate.now(ZoneId.systemDefault());
            savedDate = LocalDate.now(ZoneId.systemDefault());
        }
        if (headerString == null) {
            headerString = getString(R.string.select_a_date);
        }

        picker = inflater.inflate(R.layout.date_picker, null);
        header = picker.findViewById(R.id.datePickerHeader);
        backMonth = picker.findViewById(R.id.pickerBackMonth);
        forwardMonth = picker.findViewById(R.id.pickerForwardMonth);
        calendar = picker.findViewById(R.id.calendar_layout);
        pickedDate = picker.findViewById(R.id.picked_date);
        cancel = picker.findViewById(R.id.cancelDate);
        submit = picker.findViewById(R.id.submitDate);

        header.setText(headerString);

        getSelectedMonth();

        buildCalendar();

        pickedDate.setOnClickListener(v -> {
            MonthYearPickerDialog pd = new MonthYearPickerDialog();
            FragmentManager manager = null;
            if (getActivity() != null) {
                manager = getActivity().getSupportFragmentManager();
            }
            if (manager != null) {
                Bundle args = new Bundle();
                args.putInt("month", selection.getMonthValue());
                args.putInt("year", selection.getYear());
                pd.setArguments(args);
                pd.show(manager, "MonthYearPickerDialog");
                pd.setListener((view, year, month, dayOfMonth) -> {
                    selection = LocalDate.of(year, month, 1);
                    if (pd.getDialog() != null) {
                        pd.getDialog().dismiss();
                    }
                    getSelectedMonth();
                    buildCalendar();
                    pd.dismiss();
                    if (pd.getDialog() != null) {
                        pd.getDialog().dismiss();
                    }
                });
            }
        });

        backMonth.setOnClickListener(v -> {
            selection = selection.minusMonths(1);
            getSelectedMonth();
            buildCalendar();
        });
        forwardMonth.setOnClickListener(v -> {
            selection = selection.plusMonths(1);
            getSelectedMonth();
            buildCalendar();
        });

        cancel.setOnClickListener(v -> Objects.requireNonNull(DatePicker.this.getDialog()).cancel());
        submit.setOnClickListener(v -> {
            listener.onClick(v);
            Objects.requireNonNull(DatePicker.this.getDialog()).cancel();
        });
        builder.setView(picker);
        return builder.create();
    }

    public void getSelectedMonth() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        pickedDate.setText(dtf.format(selection));
    }

    public void buildCalendar() {
        calendar.removeAllViews();
        calendar.invalidate();
        int daysInPriorMonth = selection.minusMonths(1).lengthOfMonth();
        int priorMonthDaysToShow;
        int totalDays = selection.lengthOfMonth();
        int todayDay = selection.getDayOfMonth() + 1;
        int weekCounter = 7;
        int dayCounter = 1;
        LocalDate firstDay = selection.withDayOfMonth(1);
        priorMonthDaysToShow = firstDay.getDayOfWeek().getValue();

        LinearLayout firstWeek = new LinearLayout(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        firstWeek.setLayoutParams(lp);
        calendar.addView(firstWeek);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        ArrayList<TextView> views = new ArrayList<>();

        while (priorMonthDaysToShow > 0) {
            View pastMonthDay = View.inflate(requireContext(), R.layout.current_month_day, null);
            TextView dateView = pastMonthDay.findViewById(R.id.currentDateNumber);
            dateView.setTextColor(requireContext().getResources().getColor(R.color.lightGrey, requireContext().getTheme()));
            pastMonthDay.setLayoutParams(lp2);
            --priorMonthDaysToShow;
            dateView.setText(String.valueOf(daysInPriorMonth - priorMonthDaysToShow));
            weekCounter = weekCounter - 1;
            firstWeek.addView(pastMonthDay);
        }
        while (totalDays > 0) {
            if (weekCounter > 0) {
                View currentMonthDay = View.inflate(requireContext(), R.layout.current_month_day, null);
                currentMonthDay.setLayoutParams(lp1);
                TextView dayBlock = currentMonthDay.findViewById(R.id.currentDateNumber);
                dayBlock.setText(String.valueOf(dayCounter));
                if (dayCounter == LocalDate.now().getDayOfMonth() && selection.getMonth() == LocalDate.now().getMonth() && selection.getYear() == LocalDate.now().getYear()) {
                    dayBlock.setTextColor(requireContext().getResources().getColor(R.color.tabsSemiTransparent, requireContext().getTheme()));
                }
                ++dayCounter;
                --totalDays;
                --weekCounter;
                firstWeek.addView(currentMonthDay);
                if (dayCounter == todayDay && selection.getMonth() == savedDate.getMonth() && selection.getYear() == savedDate.getYear()) {
                    dayBlock.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.circle));
                    dayBlock.setBackgroundTintList(ColorStateList.valueOf(requireContext().getResources().getColor(R.color.billsapp_background, requireContext().getTheme())));
                    dayBlock.setTextColor(requireContext().getResources().getColor(R.color.white, requireContext().getTheme()));
                }
                views.add(dayBlock);
                dayBlock.setOnClickListener(v -> {
                    for (TextView view : views) {
                        if (view.getBackground() != null) {
                            TextView day = view.findViewById(R.id.currentDateNumber);
                            view.setBackground(null);
                            view.setTextColor(requireContext().getResources().getColor(R.color.blackAndWhite, requireContext().getTheme()));
                            if (Integer.parseInt(day.getText().toString()) == LocalDate.now().getDayOfMonth() && selection.getMonth() == LocalDate.now().getMonth() && selection.getYear() == LocalDate.now().getYear()) {
                                day.setTextColor(requireContext().getResources().getColor(R.color.tabsSemiTransparent, requireContext().getTheme()));
                            }
                        }
                    }
                    dayBlock.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.circle));
                    dayBlock.setBackgroundTintList(ColorStateList.valueOf(requireContext().getResources().getColor(R.color.billsapp_background, requireContext().getTheme())));
                    dayBlock.setTextColor(requireContext().getResources().getColor(R.color.white, requireContext().getTheme()));
                    selection = selection.withDayOfMonth(Integer.parseInt(dayBlock.getText().toString()));
                });
            } else {
                firstWeek = new LinearLayout(requireContext());
                calendar.addView(firstWeek);
                weekCounter = 7;
            }
        }
        dayCounter = 1;
        while (weekCounter > 0) {
            View futureMonthDay = View.inflate(requireContext(), R.layout.current_month_day, null);
            TextView dateView = futureMonthDay.findViewById(R.id.currentDateNumber);
            dateView.setText(String.valueOf(dayCounter));
            dateView.setTextColor(requireContext().getResources().getColor(R.color.lightGrey, requireContext().getTheme()));
            ++dayCounter;
            --weekCounter;
            firstWeek.addView(futureMonthDay);
            futureMonthDay.setLayoutParams(lp2);
        }
    }
}