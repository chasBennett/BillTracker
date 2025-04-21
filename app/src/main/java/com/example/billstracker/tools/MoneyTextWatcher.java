package com.example.billstracker.tools;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;
import java.util.Objects;

public class MoneyTextWatcher implements TextWatcher {
    final EditText edit;
    final String formatted = FixNumber.addSymbol("  0.00");
    boolean start = formatted.charAt(0) == Character.CURRENCY_SYMBOL;

    public MoneyTextWatcher(EditText editText) {

        edit = editText;
        if (edit.getText().toString().isEmpty()) {
            edit.setText(formatted);
        }
        edit.setOnFocusChangeListener((view, b) -> {
            if (edit.hasFocus()) {
                edit.post(() -> {
                    if (start) {
                        edit.setSelection(edit.getText().length());
                    }
                    else {
                        edit.setSelection(edit.getText().length() - 2);
                    }
                });
            }
        });
        edit.setOnClickListener(view -> {
            if (start) {
                edit.setSelection(edit.getText().length());
            }
            else {
                edit.setSelection(edit.getText().length() - 2);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start1, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        start = formatted.charAt(0) == Objects.requireNonNull(NumberFormat.getCurrencyInstance().getCurrency()).getSymbol().charAt(0);
        if (editable.length() < 4) {
            edit.removeTextChangedListener(this);
            edit.setText(formatted);
        }
        else {
            String s = editable.toString().replaceAll("\\D", "");
            if (s.isEmpty()) return;
            StringBuilder sb = new StringBuilder(s);
            sb.insert(sb.length() - 2, '.').insert(0, "  ");
            int commas = sb.length() - 3;
            while (commas > 3) {
                commas -= 3;
                sb.insert(commas, ',');
            }
            s = FixNumber.addSymbol(sb.toString());
            edit.removeTextChangedListener(this);
            edit.setText(s);
        }
        if (start) {
            edit.setSelection(edit.getText().length());
        }
        else {
            edit.setSelection(edit.getText().length() - 2);
        }
        edit.addTextChangedListener(this);
    }
}