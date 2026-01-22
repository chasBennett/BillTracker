package com.example.billstracker.popup_classes;

import static com.example.billstracker.activities.PaymentHistory.range;
import static com.example.billstracker.activities.PaymentHistory.selectedBillers;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;

import java.util.ArrayList;

public class FilterPayments {

    public static View.OnClickListener positiveButtonListener;
    ViewGroup main;
    View dialog;
    LinearLayout parent;
    TextView startDate, endDate, positiveButton, negativeButton;
    LinearLayout billersList;
    ArrayList<View> tabs = new ArrayList<>();

    public FilterPayments(Activity activity) {

        setViews(activity);

        setDateRange(range.getStartDate(), range.getEndDate());

        startDate.setOnClickListener(v -> {
            FragmentManager ft = ((FragmentActivity) activity).getSupportFragmentManager();
            DatePicker dp = DateFormat.getPaymentDateFromUser(ft, range.getStartDate(), activity.getString(R.string.select_a_new_start_date));
            dp.setListener(v12 -> {
                if (DatePicker.selection != null) {
                    startDate.setText(DateFormat.makeDateString(DatePicker.selection));
                    range.setStartDate(DateFormat.makeLong(DatePicker.selection));
                }
            });
        });
        endDate.setOnClickListener(v -> {
            FragmentManager ft = ((FragmentActivity) activity).getSupportFragmentManager();
            DatePicker dp = DateFormat.getPaymentDateFromUser(ft, range.getEndDate(), activity.getString(R.string.select_a_new_end_date));
            dp.setListener(v12 -> {
                if (DatePicker.selection != null) {
                    endDate.setText(DateFormat.makeDateString(DatePicker.selection));
                    range.setEndDate(DateFormat.makeLong(DatePicker.selection));
                }
            });
        });

        generateList(activity);

        positiveButton.setOnClickListener(v -> positiveButtonListener.onClick(v));
        if (negativeButton != null) {
            negativeButton.setOnClickListener(v -> main.removeView(dialog));
        }
        parent.setOnClickListener(v -> dismissDialog());
        main.addView(dialog);
    }

    public void setPositiveButtonListener(View.OnClickListener listener1) {
        FilterPayments.positiveButtonListener = listener1;
    }

    public void generateList(Activity activity) {

        if (Repository.getInstance().getBills() != null && !Repository.getInstance().getBills().isEmpty()) {
            tabs = new ArrayList<>();
            billersList.removeAllViews();
            billersList.invalidate();
            View allBillersTab = View.inflate(activity, R.layout.found_biller, null);
            CheckBox selectAllBillers = allBillersTab.findViewById(R.id.selectBiller);
            com.google.android.material.imageview.ShapeableImageView allBillersImage = allBillersTab.findViewById(R.id.biller_icon);
            if (Tools.isDarkMode(activity)) {
                allBillersImage.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.circle, activity.getTheme()));
                allBillersImage.setImageTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.whiteAndBlack, activity.getTheme())));
            }
            allBillersImage.setContentPadding(20, 20, 20, 20);
            allBillersImage.setPadding(0, 0, 0, 0);
            allBillersImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(activity.getApplicationContext()).load(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.invoices, activity.getTheme())).fitCenter().into(allBillersImage);
            billersList.addView(allBillersTab);
            selectAllBillers.setOnCheckedChangeListener((buttonView, isChecked) -> setAllBillersChecked(selectAllBillers));
            boolean all = true;
            for (Bill bill : Repository.getInstance().getBills()) {
                View billerTab = View.inflate(activity, R.layout.found_biller, null);
                CheckBox select = billerTab.findViewById(R.id.selectBiller);
                com.google.android.material.imageview.ShapeableImageView billerImage = billerTab.findViewById(R.id.biller_icon);
                TextView billerName = billerTab.findViewById(R.id.biller_name);
                Tools.loadIcon(billerImage, bill.getCategory(), bill.getIcon());
                billerName.setText(bill.getBillerName());
                billersList.addView(billerTab);
                tabs.add(billerTab);
                boolean found = false;
                for (Bill bill1 : selectedBillers) {
                    if (bill1.getBillerName().equals(billerName.getText().toString())) {
                        found = true;
                        select.setChecked(true);
                        break;
                    }
                }
                if (!found) {
                    all = false;
                }
                select.setOnCheckedChangeListener((buttonView, isChecked) -> setIndividualBillerSelected(allBillersTab, billerTab));
            }

            if (all) {
                selectAllBillers.setChecked(true);
                setAllBillersChecked(selectAllBillers);
            }
        }
    }

    public void setAllBillersChecked(CheckBox selectAllBillers) {
        if (selectAllBillers.isChecked()) {
            if (!tabs.isEmpty()) {
                for (View tab : tabs) {
                    CheckBox button = tab.findViewById(R.id.selectBiller);
                    button.setChecked(false);
                }
            }
            selectedBillers.clear();
            if (Repository.getInstance().getBills() != null && !Repository.getInstance().getBills().isEmpty()) {
                selectedBillers.addAll(Repository.getInstance().getBills());
            }
        }
    }

    public void setIndividualBillerSelected(View allBillersTab, View selectedBiller) {

        CheckBox allBillersButton = allBillersTab.findViewById(R.id.selectBiller);
        CheckBox selectedBillerButton = selectedBiller.findViewById(R.id.selectBiller);
        if (selectedBillerButton.isChecked()) {
            allBillersButton.setChecked(false);
            selectedBillers.clear();
            if (tabs != null && !tabs.isEmpty()) {
                for (View view : tabs) {
                    CheckBox button = view.findViewById(R.id.selectBiller);
                    if (button.isChecked()) {
                        TextView name = view.findViewById(R.id.biller_name);
                        if (Repository.getInstance().getBills() != null && !Repository.getInstance().getBills().isEmpty()) {
                            for (Bill bill : Repository.getInstance().getBills()) {
                                if (bill.getBillerName().equals(name.getText().toString())) {
                                    if (!selectedBillers.contains(bill)) {
                                        selectedBillers.add(bill);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            TextView name = selectedBiller.findViewById(R.id.biller_name);
            selectedBillerButton.setChecked(false);
            if (selectedBillers != null && !selectedBillers.isEmpty()) {
                Bill remove = null;
                for (Bill bill : selectedBillers) {
                    if (bill.getBillerName().equals(name.getText().toString())) {
                        remove = bill;
                        break;
                    }
                }
                if (remove != null) {
                    selectedBillers.remove(remove);
                }
            }
            boolean billersSelected = false;
            if (!tabs.isEmpty()) {
                for (View tab : tabs) {
                    CheckBox button1 = tab.findViewById(R.id.selectBiller);
                    if (button1.isChecked()) {
                        billersSelected = true;
                        break;
                    }
                }
            }
            if (billersSelected) {
                allBillersButton.setChecked(false);
            } else {
                allBillersButton.setChecked(true);
                selectedBillers.clear();
                if (Repository.getInstance().getBills() != null && !Repository.getInstance().getBills().isEmpty()) {
                    selectedBillers.addAll(Repository.getInstance().getBills());
                }
            }
        }
    }

    public void setViews(Activity activity) {
        main = activity.findViewById(android.R.id.content);
        dialog = View.inflate(activity, R.layout.filter_payments, null);
        parent = dialog.findViewById(R.id.dialog_parent);
        startDate = dialog.findViewById(R.id.startDate);
        endDate = dialog.findViewById(R.id.endDate);
        billersList = dialog.findViewById(R.id.billersList);
        positiveButton = dialog.findViewById(R.id.positiveButton);
        negativeButton = dialog.findViewById(R.id.negativeButton);
    }

    public void dismissDialog() {
        main.removeView(dialog);
    }

    public void setDateRange(long start, long end) {
        range.setStartDate(start);
        range.setEndDate(end);
        startDate.setText(DateFormat.makeDateString(start));
        endDate.setText(DateFormat.makeDateString(end));
    }
}
