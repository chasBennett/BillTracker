package com.example.billstracker.popup_classes;

import static com.example.billstracker.activities.MainActivity2.selectedDate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.billstracker.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MonthYearPickerDialog extends DialogFragment {

    private static final int MAX_YEAR = 2099;
    private DatePickerDialog.OnDateSetListener listener;

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyDialogStyle));
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        int year = selectedDate.getYear();
        int month = selectedDate.getMonthValue();

        Bundle mArgs = getArguments();
        if (mArgs != null) {
            month = mArgs.getInt("month");
            year = mArgs.getInt("year");
        }

        Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.date_picker_dialog, null);
        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_day);
        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        ArrayList<String> monthNames = new ArrayList<>(Arrays.asList(symbols.getShortMonths()));
        monthPicker.setDisplayedValues(monthNames.toArray(new String[0]));
        monthPicker.setValue(cal.get(Calendar.MONTH));

        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setValue(year);
        monthPicker.setValue(month);

        builder.setView(dialog).setPositiveButton(R.string.ok, (dialog1, id) -> listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0))
                .setNegativeButton(R.string.cancel, (dialog12, id) -> Objects.requireNonNull(MonthYearPickerDialog.this.getDialog()).cancel());
        return builder.create();
    }
}