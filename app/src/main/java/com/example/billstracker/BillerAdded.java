package com.example.billstracker;

import static com.example.billstracker.Logon.thisUser;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BillerAdded extends AppCompatActivity {

    TextView billerName, amountDue, dueDate, recurring, frequency, website, category, showPaymentsRemaining;
    Button btnContinue;
    com.google.android.material.imageview.ShapeableImageView billerIcon;
    LinearLayout pb;
    Context mContext;
    Bundle bundle;
    boolean darkMode;
    LinearLayout paymentsRemainingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biller_added);

        darkMode = false;
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            darkMode = true;
        }

        billerName = findViewById(R.id.viewBillerName1);
        amountDue = findViewById(R.id.showAmountDue1);
        dueDate = findViewById(R.id.showNextDueDate1);
        recurring = findViewById(R.id.showRecurring1);
        frequency = findViewById(R.id.showFrequency1);
        website = findViewById(R.id.showWebsite1);
        category = findViewById(R.id.showCategory);
        btnContinue = findViewById(R.id.btnContinue);
        billerIcon = findViewById(R.id.addedBillerIcon);
        showPaymentsRemaining = findViewById(R.id.showPaymentsRemaining2);
        paymentsRemainingLayout = findViewById(R.id.paymentsRemainingLayout3);
        pb = findViewById(R.id.pb3);
        mContext = this;
        bundle = getIntent().getExtras();
        billerName.setText(bundle.getString("Biller Name"));
        amountDue.setText(bundle.getString("Amount Due"));
        String cat = bundle.getString("Category");
        switch (cat) {
            case "0":
                category.setText(getString(R.string.autoLoan));
                break;
            case "1":
                category.setText(getString(R.string.creditCard));
                break;
            case "2":
                category.setText(getString(R.string.entertainment));
                break;
            case "3":
                category.setText(getString(R.string.insurance));
                break;
            case "4":
                category.setText(getString(R.string.miscellaneous));
                break;
            case "5":
                category.setText(getString(R.string.mortgage));
                break;
            case "6":
                category.setText(getString(R.string.personalLoans));
                break;
            case "7":
                category.setText(getString(R.string.utilities));
                break;
        }
        String getDate = bundle.getString("Day Due");
        String icon = bundle.getString("Icon");
        billerIcon.setVisibility(View.VISIBLE);
        billerIcon.setImageDrawable(Drawable.createFromPath(icon));
        LoadIcon loadIcon = new LoadIcon();
        loadIcon.loadImageFromDatabase(BillerAdded.this, billerIcon, icon);
        for (Bill bill: thisUser.getBills()) {
            if (bill.getBillerName().equals(billerName.getText().toString())) {
                loadIcon.loadIcon(BillerAdded.this, billerIcon, bill.getCategory(), bill.getIcon());
                if (!bill.getPaymentsRemaining().equals("1000")) {
                    paymentsRemainingLayout.setVisibility(View.VISIBLE);
                    showPaymentsRemaining.setText(bill.getPaymentsRemaining());
                }
            }
        }
        dueDate.setText(getDate);
        if (bundle.getBoolean("Recurring")) {
            recurring.setText(R.string.yes);
        } else {
            recurring.setText(R.string.no);
        }

        String freq = bundle.getString("Frequency");
        switch (freq) {
            case "0":
                frequency.setText(getString(R.string.daily));
                break;
            case "1":
                frequency.setText(getString(R.string.weekly));
                break;
            case "2":
                frequency.setText(getString(R.string.biweekly));
                break;
            case "3":
                frequency.setText(getString(R.string.monthly));
                break;
            case "4":
                frequency.setText(getString(R.string.quarterly));
                break;
            case "5":
                frequency.setText(getString(R.string.yearly));
                break;
        }
        website.setText(bundle.getString("Website"));

        btnContinue.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent mainAct = new Intent(mContext, MainActivity2.class);
            mainAct.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainAct);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}