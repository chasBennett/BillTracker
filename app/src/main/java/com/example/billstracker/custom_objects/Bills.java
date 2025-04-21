package com.example.billstracker.custom_objects;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Bills {
        private ArrayList<Bill> bills;

        public Bills(ArrayList <Bill> bills) {

            setBills(bills);
        }

        public Bills() {

        }

        public ArrayList<Bill> getBills() {
            if (bills == null) {
                bills = new ArrayList<>();
            }
            bills = (ArrayList<Bill>) bills.stream().distinct().collect(Collectors.toList());
            return bills;
        }

        public void setBills(ArrayList<Bill> bills) {
            this.bills = bills;
        }
}
