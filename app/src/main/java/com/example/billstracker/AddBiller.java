package com.example.billstracker;

import static android.content.ContentValues.TAG;
import static com.example.billstracker.Logon.billers;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddBiller extends AppCompatActivity {

    BillerManager billerManager = new BillerManager();
    LinearLayout backAddBillerLayout, pb, numberOfPaymentsLayout, currentBalanceLayout, escrowLayout;
    DateFormatter df = new DateFormatter();

    ConstraintLayout photoChooser;
    AutoCompleteTextView etBillerName;
    EditText etWebsite, etAmountDue, addNumberOfPayments, currentBalance, addEscrow;
    TextView etDueDate, billerNameError, websiteError, amountDueError, useDefault, estimatedRate;
    Spinner sFrequency, sCategory;
    Context mContext;
    Button submitBiller;
    SwitchCompat recurringSwitch, continuousSwitch;
    com.google.android.material.imageview.ShapeableImageView addBillerIcon;
    boolean customIcon;
    FixNumber fn = new FixNumber();
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    com.google.android.gms.ads.AdView adview;
    ArrayList<Integer> icons;
    LoadIcon loadIcon = new LoadIcon();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_biller);

        boolean darkMode = false;
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            darkMode = true;
        }
        mContext = this;
        pb = findViewById(R.id.pb2);
        adview = findViewById(R.id.adView2);
        etWebsite = findViewById(R.id.etWebsite);
        sCategory = findViewById(R.id.category);
        useDefault = findViewById(R.id.useDefault);
        etDueDate = findViewById(R.id.etDueDate);
        addEscrow = findViewById(R.id.addEscrow);
        sFrequency = findViewById(R.id.frequency);
        etAmountDue = findViewById(R.id.etAmountDue);
        websiteError = findViewById(R.id.websiteError);
        etBillerName = findViewById(R.id.etBillerName);
        escrowLayout = findViewById(R.id.escrowLayout);
        submitBiller = findViewById(R.id.btnSubmitBiller);
        estimatedRate = findViewById(R.id.estimatedRate);
        addBillerIcon = findViewById(R.id.addBillerIcon);
        photoChooser = findViewById(R.id.addBillerPhotoChooser);
        amountDueError = findViewById(R.id.amountDueError);
        currentBalance = findViewById(R.id.etcurrentBalance);
        billerNameError = findViewById(R.id.billerNameError);
        recurringSwitch = findViewById(R.id.recurringSwitch);
        continuousSwitch = findViewById(R.id.continuousSwitch);
        addNumberOfPayments = findViewById(R.id.addNumberOfPayments);
        backAddBillerLayout = findViewById(R.id.backAddBillerLayout);
        currentBalanceLayout = findViewById(R.id.currentBalanceLayout);
        numberOfPaymentsLayout = findViewById(R.id.numberOfPaymentsLayout);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);

        if (billers == null || billers.isEmpty()) {
            loadBillers();
        }
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::loadImage);
        backAddBillerLayout.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            onBackPressed();
        });
        customIcon = false;

        addBillerIcon.setImageDrawable(AppCompatResources.getDrawable(AddBiller.this, R.drawable.invoice));

        final boolean[] found = {false};

        ArrayList <String> knownBillers = new ArrayList<>();
        for (Biller biller: billers) {
            knownBillers.add(biller.getBillerName());
        }
        Set <String> set = new HashSet<>(knownBillers);
        knownBillers.clear();
        knownBillers.addAll(set);
        ArrayAdapter <String> knownBillersAdapter = new ArrayAdapter<>(AddBiller.this, android.R.layout.simple_list_item_1, knownBillers);
        etBillerName.setAdapter(knownBillersAdapter);

        String[] spinnerArray = new String[]{getString(R.string.daily), getString(R.string.weekly), getString(R.string.biweekly), getString(R.string.monthly),
                getString(R.string.quarterly), getString(R.string.yearly)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sFrequency.setAdapter(adapter);
        String[] spinnerArray1 = new String[]{getString(R.string.autoLoan), getString(R.string.creditCard), getString(R.string.entertainment),
                getString(R.string.insurance), getString(R.string.miscellaneous), getString(R.string.mortgage), getString(R.string.personalLoans), getString(R.string.utilities)};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(adapter1);
        submitBiller.setEnabled(false);
        sCategory.setSelection(4);
        icons = new ArrayList<>(Arrays.asList(R.drawable.auto, R.drawable.credit_card, R.drawable.entertainment, R.drawable.insurance,
                R.drawable.invoice, R.drawable.mortgage, R.drawable.personal_loan, R.drawable.utilities));
        final int[] userSelection = {4};
        sCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!found[0] && !customIcon) {
                    userSelection[0] = sCategory.getSelectedItemPosition();
                    loadIcon.loadDefault(AddBiller.this, icons.get(adapter1.getPosition(sCategory.getSelectedItem().toString())), addBillerIcon);
                    customIcon = false;
                }
                if (sCategory.getSelectedItemPosition() == 0 || sCategory.getSelectedItemPosition() == 5 || sCategory.getSelectedItemPosition() == 6) {
                    numberOfPaymentsLayout.setVisibility(View.VISIBLE);
                    recurringSwitch.setChecked(true);
                    continuousSwitch.setVisibility(View.VISIBLE);
                    continuousSwitch.setChecked(true);
                    currentBalanceLayout.setVisibility(View.VISIBLE);
                    if (sCategory.getSelectedItemPosition() == 5) {
                        escrowLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        escrowLayout.setVisibility(View.GONE);
                    }

                }
                else {
                    numberOfPaymentsLayout.setVisibility(View.GONE);
                    continuousSwitch.setVisibility(View.GONE);
                    currentBalanceLayout.setVisibility(View.GONE);
                    escrowLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recurringSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (recurringSwitch.isChecked()) {
                continuousSwitch.setVisibility(View.VISIBLE);
            }
            else {
                addNumberOfPayments.setText("");
                continuousSwitch.setChecked(false);
                continuousSwitch.setVisibility(View.GONE);
                numberOfPaymentsLayout.setVisibility(View.GONE);
            }
        });

        continuousSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (continuousSwitch.isChecked()) {
                numberOfPaymentsLayout.setVisibility(View.VISIBLE);
                currentBalanceLayout.setVisibility(View.VISIBLE);
            }
            else {
                addNumberOfPayments.setText("");
                numberOfPaymentsLayout.setVisibility(View.GONE);
                currentBalanceLayout.setVisibility(View.GONE);
            }
        });

        useDefault.setOnClickListener(v -> {
            useDefault.setVisibility(View.GONE);
            userSelection[0] = sCategory.getSelectedItemPosition();
            loadIcon.loadDefault(AddBiller.this, icons.get(adapter1.getPosition(sCategory.getSelectedItem().toString())), addBillerIcon);
            customIcon = false;
        });

        if (darkMode) {
            addBillerIcon.setBackground(AppCompatResources.getDrawable(AddBiller.this, R.drawable.circle));
            addBillerIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.tiles, getTheme())));
        }
        else {
            addBillerIcon.setBackground(null);
        }

        photoChooser.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
        final boolean[] nameLength = {false}, websiteLength = {false}, amountDue = {false}, dueDate = {false};

        etBillerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (Biller biller : billers) {
                    if (etBillerName.getText().toString().trim().toLowerCase().contains(biller.getBillerName().trim().toLowerCase())) {
                        found[0] = true;
                        loadIcon.loadImageFromDatabase(AddBiller.this, addBillerIcon, biller.getIcon());
                        customIcon = true;
                        etWebsite.setText(biller.getWebsite());
                        int selection = adapter1.getPosition(biller.getType());
                        sCategory.setSelection(selection);
                        useDefault.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList <String> billerNames = new ArrayList<>();
        for (Bill bills: thisUser.getBills()) {
            billerNames.add(bills.getBillerName().toLowerCase(Locale.getDefault()).trim());
        }

        boolean finalDarkMode = darkMode;
        etBillerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                etBillerName.removeTextChangedListener(this);
                String name = etBillerName.getText().toString().toLowerCase(Locale.getDefault()).trim();
                for (String bill: billerNames) {
                    if (bill.equals(name)) {
                        etBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                        nameLength[0] = false;
                        submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                        billerNameError.setVisibility(View.VISIBLE);
                        billerNameError.setText(R.string.billerExists);
                    }
                }
                if (name.length() > 1 && !customIcon) {
                    nameLength[0] = true;
                    for (Biller biller : billers) {
                        if (name.contains(biller.getBillerName().toLowerCase())) {
                            found[0] = true;
                            loadIcon.loadImageFromDatabase(AddBiller.this, addBillerIcon, biller.getIcon());
                            customIcon = true;
                            etWebsite.setText(biller.getWebsite());
                            int selection = adapter1.getPosition(biller.getType());
                            sCategory.setSelection(selection);
                            useDefault.setVisibility(View.VISIBLE);
                            break;
                        } else {
                            customIcon = false;
                            found[0] = false;
                            loadIcon.loadDefault(AddBiller.this, icons.get(userSelection[0]), addBillerIcon);
                            etWebsite.setText("");
                            sCategory.setSelection(userSelection[0]);
                            useDefault.setVisibility(View.GONE);
                        }
                    }
                }
                if (name.isEmpty()) {
                    etBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                    found[0] = false;
                    billerNameError.setVisibility(View.VISIBLE);
                    billerNameError.setText(R.string.billerNameCantBeBlank);
                    nameLength[0] = false;
                }
                else {
                    for (Bill bill: thisUser.getBills()) {
                        if (bill.getBillerName().equals(etBillerName.getText().toString())) {
                            etBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                            nameLength[0] = false;
                            submitBiller.setEnabled(false);
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                            billerNameError.setVisibility(View.VISIBLE);
                            billerNameError.setText(R.string.billerExists);
                        }
                        else {
                            etBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall,0);
                            billerNameError.setVisibility(View.GONE);
                            nameLength[0] = true;
                            if (websiteLength[0] && amountDue[0] && dueDate[0]) {
                                submitBiller.setEnabled(true);
                                if (finalDarkMode) {
                                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.blueGrey)));
                                }
                                else {
                                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.button)));
                                }
                            }
                        }
                    }
                }
                etBillerName.addTextChangedListener(this);
            }
        });

        etWebsite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (etWebsite.getText().toString().length() < 7 && etWebsite.getText().toString().length() > 0) {
                    etWebsite.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                    submitBiller.setEnabled(false);
                    websiteLength[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    websiteError.setVisibility(View.VISIBLE);
                    websiteError.setText(R.string.websiteTooShort);
                }
                else if (etWebsite.getText().toString().length() == 0) {
                    etWebsite.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                    websiteError.setVisibility(View.GONE);
                    websiteLength[0] = false;
                }
                else if (!(etWebsite.getText().toString().length() > 9)) {
                    etWebsite.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                    submitBiller.setEnabled(false);
                    websiteLength[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    websiteError.setVisibility(View.VISIBLE);
                    websiteError.setText(R.string.enterValidWebsite);
                }
                else {
                    etWebsite.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall,0);
                    websiteError.setVisibility(View.GONE);
                    websiteLength[0] = true;
                    if (nameLength[0] && amountDue[0] && dueDate[0]) {
                        submitBiller.setEnabled(true);
                        if (finalDarkMode) {
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.blueGrey)));
                        } else {
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.button)));
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etDueDate.setOnClickListener(view -> {
            if (finalDarkMode) {
                etDueDate.setTextColor(getResources().getColor(R.color.white, getTheme()));
            }
            else {
                etDueDate.setTextColor(getResources().getColor(R.color.black, getTheme()));
            }
            getDateFromUser(etDueDate, etAmountDue);

        });

        etDueDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (etDueDate.getText().toString().length() < 5) {
                    etDueDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                    submitBiller.setEnabled(false);
                    dueDate[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    etDueDate.setError(getString(R.string.dueDateCantBeBlank));
                }
                else {
                    etDueDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall,0);
                    TextViewCompat.setCompoundDrawableTintList(etDueDate, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                    etDueDate.setError(null);
                    dueDate[0] = true;
                    if (nameLength[0] && amountDue[0] && websiteLength[0]) {
                        submitBiller.setEnabled(true);
                        if (finalDarkMode) {
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.blueGrey)));
                        } else {
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.button)));
                        }
                    }
                    etAmountDue.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etAmountDue.setText(fn.addSymbol("0"));
        etAmountDue.addTextChangedListener( new MoneyInput(etAmountDue));
        currentBalance.setText(fn.addSymbol("0"));
        currentBalance.addTextChangedListener(new MoneyInput(currentBalance));
        addEscrow.setText(fn.addSymbol("0"));
        addEscrow.addTextChangedListener(new MoneyInput(currentBalance));

        currentBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etAmountDue.getText().length() > 1 && addNumberOfPayments.getText().length() > 0) {
                    double amountDue = Double.parseDouble(fn.makeDouble(etAmountDue.getText().toString()));
                    if (addEscrow.getText().length() > 1) {
                        amountDue = amountDue - Double.parseDouble(fn.makeDouble(addEscrow.getText().toString()));
                    }
                    double totalPaid = amountDue * Double.parseDouble(fn.makeDouble(addNumberOfPayments.getText().toString()));
                    double totalInterest = totalPaid - Double.parseDouble(fn.makeDouble(currentBalance.getText().toString()));
                    double numYears;
                    double numberOfPayments = Double.parseDouble(addNumberOfPayments.getText().toString());
                    double balance = Double.parseDouble(fn.makeDouble(currentBalance.getText().toString()));
                    int freq = sFrequency.getSelectedItemPosition();
                    if (freq == 0) {
                        numYears = numberOfPayments / (365);
                    }
                    else if (freq == 1) {
                        numYears = numberOfPayments / 52.1428571429;
                    }
                    else if (freq == 2) {
                        numYears = numberOfPayments / 26.0714285714;
                    }
                    else if (freq == 3) {
                        numYears = numberOfPayments / (365.0 / 30.4166666667);
                    }
                    else if (freq == 4) {
                        numYears = numberOfPayments / (365.0 / 91.2500000001);
                    }
                    else {
                        numYears = numberOfPayments;
                    }
                    DecimalFormat df = new DecimalFormat("###,###,##0.00");
                    double rate = (totalInterest / (balance * numYears)) * 100;
                    estimatedRate.setText(String.format("%s %s%%", getString(R.string.estimated_rate), df.format(rate)));
                }
                else {
                    estimatedRate.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etAmountDue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int n, int i1, int i2) {

                if (etAmountDue.getText().toString().length() < 2 && etAmountDue.getText().toString().length() > 0) {
                    etAmountDue.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    submitBiller.setEnabled(false);
                    amountDue[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    amountDueError.setVisibility(View.VISIBLE);
                } else if (etAmountDue.getText().toString().length() == 0) {
                    etAmountDue.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    amountDueError.setVisibility(View.GONE);
                    amountDue[0] = false;
                } else {
                    etAmountDue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall,0);
                    amountDueError.setVisibility(View.GONE);
                    amountDue[0] = true;
                    if (nameLength[0] && dueDate[0] && websiteLength[0]) {
                        submitBiller.setEnabled(true);
                        if (finalDarkMode) {
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.blueGrey)));
                        } else {
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.button)));
                        }
                    }
                    etAmountDue.setOnKeyListener((view, i, keyEvent) -> {
                        if (i == KeyEvent.KEYCODE_ENTER) {
                            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(etAmountDue.getWindowToken(), 0);
                            sFrequency.requestFocus();
                            sFrequency.performClick();
                        }
                        return false;
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        sFrequency.setOnFocusChangeListener((view, b) -> {

            if (view.hasFocus()) {
                sFrequency.performClick();
            }
        });

        submitBiller.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);

            String billerName = etBillerName.getText().toString();
            String website = etWebsite.getText().toString();
            String amountDue1 = fn.makeDouble(etAmountDue.getText().toString());
            double balance = 0.00;
            double escrow = 0;
            if (currentBalance.getText().length() > 1 && Double.parseDouble(fn.makeDouble(currentBalance.getText().toString())) > 0) {
                balance = Double.parseDouble(fn.makeDouble(currentBalance.getText().toString()));
            }
            String amountDue2 = fn.addSymbol(amountDue1);
            if (amountDue1.equals("")) {
                amountDue1 = "0.00";
            }
            if (addEscrow.getText().length() > 1 && Double.parseDouble(fn.makeDouble(addEscrow.getText().toString())) > 0) {
                escrow = Double.parseDouble(fn.makeDouble(addEscrow.getText().toString()));
            }
            String frequency = String.valueOf(sFrequency.getSelectedItemPosition());
            String category = String.valueOf(sCategory.getSelectedItemPosition());
            String numberOfPayments = "1000";
            boolean recurring = recurringSwitch.isChecked();
            if (recurring && continuousSwitch.isChecked()) {
                if (!addNumberOfPayments.getText().toString().equals("")) {
                    numberOfPayments = addNumberOfPayments.getText().toString();
                }
            }

            String billerId = String.valueOf(billerManager.id());

            int dueDateValue = df.convertDateStringToInt(etDueDate.getText().toString());

            String icon = "";
            if (customIcon) {
                BillerImage billerImage = new BillerImage();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        icon = billerImage.storeImage(addBillerIcon.getDrawable(), billerName, customIcon);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                icon = String.valueOf(icons.get(adapter1.getPosition(sCategory.getSelectedItem().toString())));
            }

            Bill newBiller = new Bill(billerName, amountDue1, dueDateValue, 0, billerId, recurring, frequency, website, category, icon, numberOfPayments, balance, escrow);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String finalIcon1 = icon;
            thisUser.getBills().add(newBiller);
            db.collection("users").document(uid).update("bills", thisUser.getBills());
            billerManager.savePayments();
            if (thisUser != null) {
                SaveUserData save = new SaveUserData();
                save.saveUserData(AddBiller.this);
            }
            Intent main = new Intent(mContext, BillerAdded.class);
            main.putExtra("Category", newBiller.getCategory());
            main.putExtra("Biller Name", newBiller.getBillerName());
            main.putExtra("Amount Due", amountDue2);
            main.putExtra("Website", newBiller.getWebsite());
            main.putExtra("Day Due", etDueDate.getText().toString());
            main.putExtra("Frequency", newBiller.getFrequency());
            main.putExtra("Recurring", newBiller.isRecurring());
            main.putExtra("Icon", finalIcon1);
            startActivity(main);
        });
    }

    public void loadImage (Uri uri) {
        if (uri != null) {
            customIcon = true;
            useDefault.setVisibility(View.VISIBLE);
            addBillerIcon.setImageTintList(null);
            loadIcon.loadImageFromDatabase(AddBiller.this, addBillerIcon, String.valueOf(uri));
            Log.d("PhotoPicker", "Selected URI: " + uri);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    }

    public void loadBillers () {

        if (billers == null || billers.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("billers").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Biller bill = document.toObject(Biller.class);
                        billers.add(bill);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
        }
    }

    public void getDateFromUser(TextView dueDate, EditText amountDue) {

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        int day = today.getDayOfMonth();
        int year = today.getYear();
        int month = today.getMonthValue();

        DatePickerDialog datePicker;
        datePicker = new DatePickerDialog(AddBiller.this, R.style.MyDatePickerStyle, (datePicker1, i, i1, i2) -> {
            int fixMonth = i1 + 1;
            LocalDate date = LocalDate.of(i, fixMonth, i2);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            String startDate = formatter.format(date);
            dueDate.setText(startDate);
            amountDue.requestFocus();
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(amountDue, InputMethodManager.RESULT_SHOWN);
            //mgr.showSoftInput(amountDue.getRootView(), InputMethodManager.SHOW_FORCED);
        }, year, month - 1, day);
        datePicker.setTitle(getString(R.string.selectDate));
        datePicker.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}