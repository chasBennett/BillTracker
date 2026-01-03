package com.example.billstracker.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class FixNumber {

    public static String addSymbol (String string) {

        final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());

        string = String.format(Locale.getDefault(), String.valueOf(makeDouble(string)));
        return nf.format(Double.parseDouble(string.replaceAll("\\s", "").replaceAll(",", ".")));
    }
    public static String addSymbol (double dub) {
        final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String string = String.format(Locale.getDefault(), String.valueOf(makeDouble(dub)));
        return nf.format(Double.parseDouble(string.replaceAll("\\s", "").replaceAll(",", ".")));
    }
    public static double makeDouble (String string) {

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
        DecimalFormat df = new DecimalFormat("#####.00");
        string = String.format(string, df);
        if (string.equals("5.0E")) {
            string = "0.01";
        }
        return Double.parseDouble(string.replaceAll(",", "."));
    }
    public static int makeInt (String string) {

        String numberString = string.replaceAll("\\D", "");
        if (numberString.isEmpty()) {
            numberString = "0";
        }
        return Integer.parseInt(numberString);
    }

    public static double makeDouble (double dub) {

        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        String string = String.valueOf(dub).replaceAll(symbol, "").replace(" ", "").replaceAll("\\s", "").replaceAll("\\$", "");
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

        return Double.parseDouble(string.replaceAll(",", ".").replaceAll(" ", "").replaceAll("\\s", ""));
    }
}
