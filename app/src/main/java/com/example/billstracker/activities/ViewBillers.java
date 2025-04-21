package com.example.billstracker.activities;

import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.payments;
import static com.example.billstracker.activities.Login.thisUser;
import static com.example.billstracker.activities.MainActivity2.startAddBiller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.CustomDialog;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.tools.BillerManager;
import com.example.billstracker.tools.Data;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.NavController;
import com.example.billstracker.tools.Tools;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Comparator;

public class ViewBillers extends AppCompatActivity {
    static int choice;
    static ArrayList<View> views;
    LinearLayout details, billerTiles;
    ConstraintLayout pb;
    SwitchCompat showPaidBillers;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_billers);

        pb = findViewById(R.id.progressBar);
        billerTiles = findViewById(R.id.billerTiles);
        details = findViewById(R.id.biller_details);
        showPaidBillers = findViewById(R.id.showPaidBillers);

        Tools.fixProgressBarLogo(pb);

        MobileAds.initialize(this, initializationStatus -> {
        });

        NavController nc = new NavController();
        nc.navController(ViewBillers.this, ViewBillers.this, pb, "viewBillers");

        BillerManager.refreshPayments();

        showPaidBillers.setChecked(false);

        showPaidBillers.setOnCheckedChangeListener((compoundButton, b) -> listBills(b));

        listBills(false);
    }

    public void listBills(boolean showPaid) {

        Context mContext = this;
        choice = 0;
        billerTiles.invalidate();
        billerTiles.removeAllViews();
        pb.setVisibility(View.GONE);
        ArrayList<String> categories = Data.getCategories(ViewBillers.this);
        ArrayList<String> freq = Data.getFrequencies(ViewBillers.this);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String userId = thisUser.getid();
        views = new ArrayList<>();
        ArrayList<Bill> billsList = new ArrayList<>();
        if (bills != null && bills.getBills() != null && !bills.getBills().isEmpty()) {
            bills.getBills().sort(Comparator.comparing(Bill::getBillerName));
            for (Bill bill : bills.getBills()) {
                if (!showPaid) {
                    if (!billsList.contains(bill) && bill.getPaymentsRemaining() > 0) {
                        billsList.add(bill);
                    }
                } else {
                    if (!billsList.contains(bill)) {
                        billsList.add(bill);
                    }
                }
            }
        }
        int counter = 0;
        for (Bill bills : billsList) {
            counter = counter + 1;
            String recurring;
            String billId = bills.getBillsId();
            String dueDate;
            boolean paidOff = false;
            if (bills.isRecurring() && bills.getPaymentsRemaining() > 0) {
                recurring = getString(R.string.yes);
                long earliest = DateFormat.makeLong(DateFormat.makeLocalDate(bills.getDueDate()).plusYears(1));
                if (payments != null && payments.getPayments() != null) {
                    for (Payment pay : payments.getPayments()) {
                        if (pay.getBillerName().equalsIgnoreCase(bills.getBillerName()) && !pay.isPaid() && pay.getDueDate() < earliest) {
                            earliest = pay.getDueDate();
                        }
                    }
                    dueDate = DateFormat.makeDateString(earliest);
                } else {
                    dueDate = DateFormat.makeDateString(bills.getDueDate());
                }
            } else {
                recurring = getString(R.string.no);
                dueDate = getString(R.string.paid_in_full);
                paidOff = true;
            }
            View tile = View.inflate(ViewBillers.this, R.layout.biller_tile, null);
            ShapeableImageView icon = tile.findViewById(R.id.billerImage);
            TextView billerName = tile.findViewById(R.id.billerTitle);
            ConstraintLayout ll = (ConstraintLayout) icon.getParent();
            ImageView billerPaid = tile.findViewById(R.id.billerPaid);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER | Gravity.BOTTOM;
            layoutParams.setMargins(0, Tools.createDPValue(ViewBillers.this, 5), 0, 0);
            if (!paidOff) {
                billerPaid.setVisibility(View.GONE);
            } else {
                billerPaid.setVisibility(View.VISIBLE);
            }
            if (ll != null) {
                ll.setLayoutParams(layoutParams);
            }
            billerName.setText(bills.getBillerName());
            Tools.loadIcon(icon, bills.getCategory(), bills.getIcon());
            billerTiles.addView(tile);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Tools.createDPValue(ViewBillers.this, 150), ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(Tools.createDPValue(ViewBillers.this, 5), Tools.createDPValue(ViewBillers.this, 5), Tools.createDPValue(ViewBillers.this, 5), Tools.createDPValue(ViewBillers.this, 5));
            tile.setLayoutParams(lp);
            views.add(tile);
            tile.setElevation(15);
            tile.setOnClickListener(v -> {
                choice = views.indexOf(tile);
                tile.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.tab_item_selected, getTheme()));
                if (!views.isEmpty()) {
                    for (View view : views) {
                        if (view != tile) {
                            view.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.border_styles_no_outline, getTheme()));
                        }
                    }
                }
                View view = View.inflate(ViewBillers.this, R.layout.view_biller, null);
                TextView category = view.findViewById(R.id.showCategory);
                TextView showBillerName = view.findViewById(R.id.showBillerName);
                TextView showAmountDue = view.findViewById(R.id.showAmountDue);
                TextView showNextDueDate = view.findViewById(R.id.showNextDueDate);
                TextView showDateLastPaid = view.findViewById(R.id.showDateLastPaid);
                TextView showRecurring = view.findViewById(R.id.showRecurring);
                TextView frequency = view.findViewById(R.id.showFrequency);
                TextView showWebsite = view.findViewById(R.id.showWebsite);
                TextView showPaymentsRemaining = view.findViewById(R.id.showPaymentsRemaining);
                LinearLayout showPaymentsRemainingLayout = view.findViewById(R.id.showPaymentsRemainingLayout);
                Button btnVisitWebsite = view.findViewById(R.id.btnVisitWebsite);
                Button btnEditBiller = view.findViewById(R.id.btnEditBiller);
                Button btnViewPaymentHistory = view.findViewById(R.id.btnPaymentHistory);
                Button btnDeleteBiller = view.findViewById(R.id.btnDeleteBiller);
                ShapeableImageView iconView = view.findViewById(R.id.iconView);
                Tools.loadIcon(iconView, bills.getCategory(), bills.getIcon());
                details.removeAllViews();
                details.invalidate();
                details.addView(view);
                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(lp1);
                String lastPaid;
                if (bills.getDateLastPaid() == 0) {
                    lastPaid = getString(R.string.no_payments_reported);
                } else {
                    lastPaid = DateFormat.makeDateString(bills.getDateLastPaid());
                }
                int cat = bills.getCategory();
                category.setText(categories.get(cat));
                showBillerName.setText(bills.getBillerName());
                showAmountDue.setText(FixNumber.addSymbol(FixNumber.makeDouble(String.valueOf(bills.getAmountDue()))));
                showNextDueDate.setText(dueDate);
                showDateLastPaid.setText(lastPaid);
                showRecurring.setText(recurring);
                if (bills.getPaymentsRemaining() < 400) {
                    showPaymentsRemainingLayout.setVisibility(View.VISIBLE);
                    if (bills.getPaymentsRemaining() == 0) {
                        showPaymentsRemaining.setText(getString(R.string.paid_in_full));
                    } else {
                        showPaymentsRemaining.setText(String.valueOf(bills.getPaymentsRemaining()));
                    }
                }
                if (bills.getFrequency() < freq.size()) {
                    frequency.setText(freq.get(bills.getFrequency()));
                }
                showWebsite.setText(HtmlCompat.fromHtml(bills.getWebsite(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                showWebsite.setOnClickListener(view1 -> {
                    pb.setVisibility(View.VISIBLE);
                    String address = bills.getWebsite();
                    if (!address.startsWith("http://") && !address.startsWith("https://")) {
                        address = "http://" + address;
                    }
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(address)));
                });
                btnEditBiller.setOnClickListener(view11 -> {
                    pb.setVisibility(View.VISIBLE);
                    startAddBiller = false;
                    startActivity(new Intent(mContext, AddBiller.class).putExtra("billerId", bills.getBillsId()));
                });
                btnVisitWebsite.setOnClickListener(view112 -> {
                    pb.setVisibility(View.VISIBLE);
                    String address = bills.getWebsite();
                    if (!address.startsWith("http://") && !address.startsWith("https://")) {
                        address = "http://" + address;
                    }
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(address)));
                });
                btnViewPaymentHistory.setOnClickListener(view113 -> {
                    pb.setVisibility(View.VISIBLE);
                    startActivity(new Intent(mContext, PaymentHistory.class).putExtra("User Id", userId).putExtra("Bill Id", billId));
                });
                btnDeleteBiller.setOnClickListener(view114 -> {
                    CustomDialog cd = new CustomDialog(ViewBillers.this, getString(R.string.deleteBiller), getString(R.string.confirmDeletion), getString(R.string.deleteBiller), getString(R.string.cancel), null);
                    cd.setPositiveButtonListener(v1 -> {
                        pb.setVisibility(View.VISIBLE);
                        if (Login.bills != null && Login.bills.getBills() != null) {
                            Bill bill = null;
                            for (Bill bil : Login.bills.getBills()) {
                                if (bil.getBillsId().equals(bills.getBillsId())) {
                                    bill = bil;
                                    break;
                                }
                            }
                            if (bill != null) {
                                bill.deleteBiller(isSuccessful -> {
                                    if (isSuccessful) {
                                        Notify.createPopup(ViewBillers.this, getString(R.string.billerWasDeletedSuccessfully), null);
                                        cd.dismissDialog();
                                        listBills(showPaidBillers.isChecked());
                                    }
                                    else {
                                        Notify.createPopup(ViewBillers.this, getString(R.string.anErrorHasOccurred), null);
                                    }
                                });
                            }
                            else {
                                Notify.createPopup(ViewBillers.this, getString(R.string.anErrorHasOccurred), null);
                            }
                        }
                        else {
                            Notify.createPopup(ViewBillers.this, getString(R.string.anErrorHasOccurred), null);
                        }
                    });
                });
            });
        }
        if (!views.isEmpty() && views.size() >= choice) {
            views.get(choice).performClick();
        }
        if (counter == 0) {
            View tile = View.inflate(ViewBillers.this, R.layout.biller_tile, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Tools.createDPValue(ViewBillers.this, 150), ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(Tools.createDPValue(ViewBillers.this, 5), Tools.createDPValue(ViewBillers.this, 5), Tools.createDPValue(ViewBillers.this, 5), Tools.createDPValue(ViewBillers.this, 5));
            tile.setLayoutParams(lp);
            tile.setBackground(AppCompatResources.getDrawable(ViewBillers.this, R.drawable.border_styles_no_outline));
            billerTiles.addView(tile);
            tile.setElevation(15);
            tile.setOnClickListener(view -> {
                Intent addBiller1 = new Intent(mContext, AddBiller.class);
                startAddBiller = true;
                startActivity(addBiller1);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listBills(showPaidBillers.isChecked());
        pb.setVisibility(View.GONE);
    }
}