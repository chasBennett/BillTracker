package com.example.billstracker.tools;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Objects;

public class MoneyFormatterWatcher implements TextWatcher {
    final EditText edit;
    final String formatted = FixNumber.addSymbol("0.00");
    boolean start;

    public MoneyFormatterWatcher(@NonNull EditText editText) {
        edit = editText;
        updateStartFlag();

        if (edit.getText().toString().isEmpty()) {
            edit.setText(formatted);
        }

        edit.setOnFocusChangeListener((view, b) -> {
            if (edit.hasFocus()) {
                edit.post(this::handleSelection);
            }
        });

        edit.setOnClickListener(view -> handleSelection());
    }

    private void updateStartFlag() {
        // Detect prefix vs suffix locale
        String currentSymbol = Objects.requireNonNull(NumberFormat.getCurrencyInstance().getCurrency()).getSymbol();
        String sample = NumberFormat.getCurrencyInstance().format(0);
        start = sample.startsWith(currentSymbol);
    }

    private void handleSelection() {
        if (start) {
            edit.setSelection(edit.getText().length());
        } else {
            // Find symbol position to avoid landing on the decimal point
            String text = edit.getText().toString();
            String symbol = Objects.requireNonNull(NumberFormat.getCurrencyInstance().getCurrency()).getSymbol();
            int symbolIndex = text.indexOf(symbol);
            if (symbolIndex > 0) {
                edit.setSelection(symbolIndex);
            } else {
                edit.setSelection(text.length());
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start1, int count, int after) {
        // No longer forcing text reset here as afterTextChanged handles the "0.00" floor
    }

    @Override
    public void onTextChanged(CharSequence s, int start1, int before, int count) {
        // Unused
    }

    @Override
    public void afterTextChanged(Editable editable) {
        edit.removeTextChangedListener(this);

        // 1. Get only the digits
        String cleanString = editable.toString().replaceAll("\\D", "");

        if (cleanString.isEmpty() || Long.parseLong(cleanString) == 0) {
            // Reset to "0.00" if user deletes everything
            edit.setText(formatted);
        } else {
            // 2. Parse digits as cents (e.g., "5" becomes 0.05)
            double parsed = Double.parseDouble(cleanString);
            double value = parsed / 100;

            // 3. Format with 2 decimal places and commas
            String rawFormatted = String.format("%.2f", value);
            StringBuilder sb = new StringBuilder(rawFormatted);

            int decimalPos = sb.indexOf(".");
            for (int i = decimalPos - 3; i > 0; i -= 3) {
                sb.insert(i, ',');
            }

            // 4. Wrap with symbol (e.g., "$0.05")
            String finalOutput = FixNumber.addSymbol(sb.toString());
            edit.setText(finalOutput);
        }

        updateStartFlag();
        handleSelection();
        edit.addTextChangedListener(this);
    }
}