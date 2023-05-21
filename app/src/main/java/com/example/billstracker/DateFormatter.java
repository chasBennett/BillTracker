package com.example.billstracker;

import static java.time.temporal.ChronoUnit.DAYS;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateFormatter {
    public long currentDateLong () {

        return ZonedDateTime.now(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public int convertDateStringToInt (String dateString) {

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        LocalDate date = LocalDate.parse(dateString, formatter);
        return Math.toIntExact(DAYS.between(LocalDate.of(0, 1, 1), date) + 15);

    }

    public String currentPhaseOfDay (Context context) {

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH", Locale.getDefault());
        dtf.format(now);
        if (now.getHour() <= 11) {
            return context.getString(R.string.morning);
        }
        else if (now.getHour() >= 12 && now.getHour() <= 16) {
            return context.getString(R.string.afternoon);
        }
        else {
            return context.getString(R.string.evening);
        }
    }

    public LocalDate convertIntDateToLocalDate (int date) {

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        return LocalDate.parse(convertIntDateToString(date), formatter);
    }

    public String currentDateAndTimeString () {

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault());
        return formatter.format(LocalDateTime.now(ZoneId.systemDefault()));
    }

    public long convertIntDateToLong (int date, int hour, int minutes) {

        return ZonedDateTime.of(LocalDateTime.from(convertIntDateToLocalDate(date).atTime(hour, minutes)),
                ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public String convertIntDateToString(int date) {

        int currentYear = (int) (date / 365.25), leapYears = currentYear / 4, remainder = date - leapYears - (currentYear * 365);
        LocalDate localDate = LocalDate.now(ZoneId.systemDefault()).withYear(currentYear).withDayOfYear(remainder);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        return formatter.format(localDate);
    }

    public int calcDateValue(LocalDate date) {

        return (int) (( date.getYear() * 365.25) + date.getDayOfYear());
    }

    public int currentDateAsInt () {

        return calcDateValue(LocalDate.now(ZoneId.systemDefault()));
    }

    public String createCurrentDateStringWithTime() {

        LocalDateTime loginTime = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a", Locale.getDefault());
        return formatter.format(loginTime);
    }

}
