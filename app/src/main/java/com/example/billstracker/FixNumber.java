package com.example.billstracker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class FixNumber {

    NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
    public String addSymbol (String string) {

        string = String.format(Locale.getDefault(), string);
        return nf.format(Double.parseDouble(string.replaceAll("\\s", "").replaceAll(",", ".")));
    }
    public String makeDouble (String string) {

        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        string = string.replaceAll(symbol, "").replace(" ", "").replaceAll("\\s", "").replaceAll("\\$", "");
        if (string.isEmpty()) {
            string = "0";
        }

        int findLastComma = 0;
        int counter3 = 0;
        boolean found;
        StringBuilder sb = new StringBuilder();
        int commaCounter = 0;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == ',' || c == '.') {
                ++commaCounter;
            }
        }
        boolean build = false;
        for (int i = 0; i < string.length(); ++i) {
            char a = string.charAt(i);
            if (a != ' ') {
                if (a == ',' || a == '.') {
                    ++findLastComma;
                    if (findLastComma == commaCounter) {
                        sb.append(a);
                        build =true;
                    }
                }
                else {
                    if (build && counter3 < 2) {
                        ++counter3;
                        sb.append(a);
                    }
                    else if (counter3 < 2) {
                        sb.append(a);
                    }
                }
            }
        }
        found = false;
        string = sb.toString();
        int index = 0;
        for (int i = 0; i < string.length(); ++i) {
            char d = string.charAt(i);
            if (d == ',' || d == '.') {
                found = true;
                index = string.indexOf(d);
            }
        }
        if (!found) {
            string = string + ".00";
        }
        else {
            if (index == string.length() -2) {
                string = string + "0";
            }
            else if (index == string.length() - 1) {
                string = string + "00";
            }
            else if (index < string.length() - 3) {
                string = string.substring(0, index + 3);
            }
        }
        DecimalFormat df = new DecimalFormat("########.00");
        string = String.format(string, df);

        return string.replaceAll(",", ".").replaceAll(" ", "").replaceAll("\\s", "");
    }
}
