package com.example.billstracker.custom_objects;

public class DateRange {

    public long startDate;
    public long endDate;

    public DateRange(long startDate, long endDate) {
        setStartDate(startDate);
        setEndDate(endDate);
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
}
