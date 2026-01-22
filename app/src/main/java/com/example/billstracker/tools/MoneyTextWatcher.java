package com.example.billstracker.tools;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Objects;

public class MoneyTextWatcher implements TextWatcher {
    final EditText edit;
    final String formatted = FixNumber.addSymbol("  0.00");
    boolean start;

    public MoneyTextWatcher(EditText editText) {
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
        String currentSymbol = Objects.requireNonNull(NumberFormat.getCurrencyInstance().getCurrency()).getSymbol();
        String sample = NumberFormat.getCurrencyInstance().format(0);
        start = sample.startsWith(currentSymbol);
    }

    private void handleSelection() {
        if (start) {
            edit.setSelection(edit.getText().length());
        } else {
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
    public void beforeTextChanged(CharSequence s, int start1, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        edit.removeTextChangedListener(this);

        // 1. Clean the string to only digits
        String cleanString = editable.toString().replaceAll("\\D", "");

        if (cleanString.isEmpty() || Long.parseLong(cleanString) == 0) {
            // If empty or all zeros, reset to default state
            edit.setText(formatted);
        } else {
            // 2. Treat the string as a number of cents (e.g., "3" becomes 0.03, "300" becomes 3.00)
            double parsed = Double.parseDouble(cleanString);
            double value = parsed / 100;

            // 3. Format with leading spaces and commas manually to match your FixNumber logic
            // We use String.format to ensure we always have 2 decimal places
            String rawFormatted = String.format("%.2f", value);

            StringBuilder sb = new StringBuilder(rawFormatted);

            // 4. Handle Commas (Manual insertion to respect your specific spacing)
            int decimalPos = sb.indexOf(".");
            for (int i = decimalPos - 3; i > 0; i -= 3) {
                sb.insert(i, ',');
            }

            // 5. Add your two mandatory leading spaces
            sb.insert(0, "  ");

            // 6. Wrap with Currency Symbol using your tool
            String finalOutput = FixNumber.addSymbol(sb.toString());

            edit.setText(finalOutput);
        }

        updateStartFlag();
        handleSelection();
        edit.addTextChangedListener(this);
    }
}