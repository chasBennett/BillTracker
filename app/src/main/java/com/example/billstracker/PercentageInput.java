package com.example.billstracker;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class PercentageInput implements TextWatcher {
    EditText edit;
    TextView text1;
    String formatted;
    FixNumber fn;

    public PercentageInput(EditText editText, TextView text) {

        edit = editText;
        text1 = text;
        fn = new FixNumber();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start1, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

        if (edit == null) return;
        String s = editable.toString();
        if (s.isEmpty()) return;
        edit.removeTextChangedListener(this);
        if (edit.getText().toString().length() < 1) {
            edit.setText("%");
            edit.setSelection(0);
            //edit.addTextChangedListener(this);
        }
        else {
            String cleanString = s.replaceAll("%", "");
            formatted = cleanString + "%";
            if (formatted.length() > 3) {
                formatted = formatted.substring(0,2) + formatted.substring(formatted.length() - 1);
            }
            edit.setText(formatted);
            edit.setSelection(edit.length() - 1);
        }
        edit.addTextChangedListener(this);
    }
}