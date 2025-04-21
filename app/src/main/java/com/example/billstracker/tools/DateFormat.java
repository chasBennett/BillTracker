package com.example.billstracker.tools;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.example.billstracker.R;
import com.example.billstracker.popup_classes.DatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public interface DateFormat {

    static long makeLong (LocalDate date) {
        return date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    static long currentTimeInMillis () {
        LocalDateTime localDateTime = LocalDateTime.now().withSecond(0);
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
    static String createLoginTime () {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withLocale(Locale.US);
        return formatter.format(LocalDate.now());
    }
    static long loginTimeToLong (String loginTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy").withLocale(Locale.US);
        return makeLong(LocalDate.parse(loginTime, formatter));
    }
    static long makeLong (String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    static LocalDate makeLocalDate (long date) {
        return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
    }
    static LocalDate makeLocalDate (String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()));
    }
    static long plusDays (long date, long days) {
        return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().plusDays(days).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    static String currentPhaseOfDay(Context context) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH", Locale.getDefault());
        dtf.format(now);
        if (now.getHour() <= 11) {
            return context.getString(R.string.morning);
        } else if (now.getHour() >= 12 && now.getHour() <= 16) {
            return context.getString(R.string.afternoon);
        } else {
            return context.getString(R.string.evening);
        }
    }
    static long makeTimedLong(long date, int hour, int minutes) {
        return ZonedDateTime.of(LocalDateTime.from(makeLocalDate(date).atTime(hour, minutes)), ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    static String makeDateString(LocalDate date) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).format(date);
    }
    static String makeDateString(long date) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).format(makeLocalDate(date));
    }
    static long currentDateAsLong() {
        return makeLong(LocalDate.now(ZoneId.systemDefault()));
    }
    static String createCurrentDateStringWithTime() {
        LocalDateTime loginTime = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a", Locale.getDefault());
        return formatter.format(loginTime);
    }
    static String createMonthYearString (LocalDate date) {
        return DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).format(date);
    }
    static long incrementDate (int frequency, long date) {
        switch (frequency) {
            case 0:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusDays(1));
            case 1:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusWeeks(1));
            case 2:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusWeeks(2));
            case 3:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusMonths(1));
            case 4:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusMonths(2));
            case 5:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusMonths(3));
            case 6:
                return DateFormat.makeLong(DateFormat.makeLocalDate(date).plusYears(1));
            default:
                return date;
        }
    }
    static int daysBetween (long date1, long date2) {
        return (int) ChronoUnit.DAYS.between(DateFormat.makeLocalDate(date1), DateFormat.makeLocalDate(date2));
    }
    static int daysBetween (LocalDate date1, LocalDate date2) {
        return (int) ChronoUnit.DAYS.between(date1, date2);
    }
    static LocalDate convertIntDateToLocalDate(long date) {
        return LocalDate.parse(convertIntDateToString(date), DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()));
    }
    static long intToLong (long date) {
        return makeLong(convertIntDateToLocalDate(date));
    }
    static String convertIntDateToString(long date) {

        long currentYear = (long) Math.floor(date / 365.25), leapYears = currentYear / 4, remainder = date - leapYears - (currentYear * 365);
        if (remainder == 0) {
            remainder = 1;
        }
        LocalDate localDate = LocalDate.now(ZoneId.systemDefault()).withYear((int) currentYear).withDayOfYear((int) remainder);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        return formatter.format(localDate);
    }
    static DatePicker getPaymentDateFromUser (FragmentManager fragmentManager, long currentlySelectedDate, String headerString) {
        Bundle args = new Bundle();
        args.putLong("selection", currentlySelectedDate);
        args.putString("headerString", headerString);
        DatePicker dp = new DatePicker();
        dp.setArguments(args);
        dp.show(fragmentManager, "DatePicker");
        return dp;
    }
}
