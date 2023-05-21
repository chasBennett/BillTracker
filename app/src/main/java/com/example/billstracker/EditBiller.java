package com.example.billstracker;

import static android.content.ContentValues.TAG;
import static com.example.billstracker.Logon.billers;
import static com.example.billstracker.Logon.paymentInfo;
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
import android.view.MenuItem;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

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

public class EditBiller extends AppCompatActivity {

    LinearLayout backEditBillerLayout, pb, numberOfPaymentsLayout, currentBalanceLayout, escrowLayout;
    TextView billerNameHeader, editDueDate, billerNameError, websiteError, amountDueError, useDefault, estimatedRate;
    EditText editWebsite, editAmountDue, addNumberOfPayments, currentBalance, addEscrow;
    AutoCompleteTextView editBillerName;
    Spinner editFrequency, sCategory;
    SwitchCompat editRecurring, continuousSwitch;
    com.google.android.material.imageview.ShapeableImageView editBillerIcon;
    Button submitBiller;
    Bundle extras;
    String billerName;
    String website;
    String amountDue;
    String frequency;
    int dueDate;
    boolean recurring, dueDateChanged, frequencyChanged, recurringChanged;
    Context mContext;
    ConstraintLayout photoChooser;
    DateFormatter df = new DateFormatter();
    boolean custom;
    LoadIcon loadIcon = new LoadIcon();
    Bill bil;
    FixNumber fn = new FixNumber();
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_biller);

        boolean darkMode = false;
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            darkMode = true;
        }

        if (billers == null || billers.size() == 0) {
            loadBillers();
        }

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::loadImage);

        pb = findViewById(R.id.pb5);
        extras = getIntent().getExtras();
        mContext = this;
        sCategory = findViewById(R.id.category1);
        addEscrow = findViewById(R.id.addEscrow1);
        useDefault = findViewById(R.id.useDefaultIcon);
        editWebsite = findViewById(R.id.editWebsite);
        escrowLayout = findViewById(R.id.escrowLayout1);
        editDueDate = findViewById(R.id.editDueDate);
        submitBiller = findViewById(R.id.btnSubmitEdit);
        photoChooser = findViewById(R.id.editBillerPhotoChooser);
        websiteError = findViewById(R.id.websiteError1);
        estimatedRate = findViewById(R.id.estimatedRate1);
        editAmountDue = findViewById(R.id.editAmountDue);
        editFrequency = findViewById(R.id.editFrequency);
        editRecurring = findViewById(R.id.editRecurring);
        editBillerName = findViewById(R.id.editBillerName);
        currentBalance = findViewById(R.id.etcurrentBalance1);
        amountDueError = findViewById(R.id.amountDueError1);
        editBillerIcon = findViewById(R.id.editBillerIcon1);
        billerNameError = findViewById(R.id.billerNameError1);
        billerNameHeader = findViewById(R.id.billerNameHeader);
        continuousSwitch = findViewById(R.id.continuousSwitch1);
        addNumberOfPayments = findViewById(R.id.addNumberOfPayments1);
        backEditBillerLayout = findViewById(R.id.backEditBillerLayout);
        numberOfPaymentsLayout = findViewById(R.id.numberOfPaymentsLayout1);
        currentBalanceLayout = findViewById(R.id.currentBalanceLayout1);

        dueDateChanged = false;
        recurringChanged = false;
        frequencyChanged = false;
        custom = false;

        billerName = extras.getString("userName");
        website = extras.getString("website");
        dueDate = extras.getInt("dueDate");
        amountDue = extras.getString("amountDue");
        frequency = extras.getString("frequency");
        recurring = extras.getBoolean("recurring");

        for (Bill bills: thisUser.getBills()) {
            if (bills.getBillerName().equals(billerName)) {
                bil = bills;
                if (recurring) {
                    continuousSwitch.setVisibility(View.VISIBLE);
                }

                if (Integer.parseInt(bills.getPaymentsRemaining()) < 400) {
                    continuousSwitch.setChecked(true);
                    numberOfPaymentsLayout.setVisibility(View.VISIBLE);
                    addNumberOfPayments.setText(bills.getPaymentsRemaining());
                }
                if (bills.getEscrow() > 0) {
                    addEscrow.setText(fn.addSymbol(String.valueOf(bills.getEscrow())));
                }
                else {
                    addEscrow.setText(fn.addSymbol("0"));
                }
                if (bills.getBalance() > 0) {
                    currentBalance.setText(fn.addSymbol(String.valueOf(bills.getBalance())));
                }
                else {
                    currentBalance.setText(fn.addSymbol("0"));
                }
                break;
            }
        }

        editBillerName.setText(billerName);
        editWebsite.setText(website);
        editAmountDue.setText(fn.addSymbol(fn.makeDouble(amountDue)));

        editRecurring.setChecked(recurring);
        editRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (editRecurring.isChecked()) {
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
                if (!bil.getPaymentsRemaining().equals("1000")) {
                    addNumberOfPayments.setText(bil.getPaymentsRemaining());
                }
            }
            else {
                numberOfPaymentsLayout.setVisibility(View.GONE);
            }
        });

        ArrayList <String> knownBillers = new ArrayList<>();
        for (Biller bills: billers) {
            knownBillers.add(bills.getBillerName());
        }
        Set<String> set = new HashSet<>(knownBillers);
        knownBillers.clear();
        knownBillers.addAll(set);
        ArrayAdapter <String> knownBillersAdapter = new ArrayAdapter<>(EditBiller.this, android.R.layout.simple_list_item_1, knownBillers);
        editBillerName.setAdapter(knownBillersAdapter);

        int earliest = 10000;
        for (Payments pay: paymentInfo.getPayments()) {
            if (!pay.isPaid() && pay.getPaymentDate() < earliest && pay.getBillerName().equals(bil.getBillerName())) {
                earliest = pay.getPaymentDate();
            }
        }
        if (earliest == 10000) {
            editDueDate.setText(df.convertIntDateToString(dueDate));
        }
        else {
            editDueDate.setText(df.convertIntDateToString(earliest));
        }
        if (darkMode) {
            editBillerIcon.setBackground(AppCompatResources.getDrawable(EditBiller.this, R.drawable.circle));
            editBillerIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.tiles, getTheme())));
        }
        loadIcon.loadIcon(EditBiller.this, editBillerIcon, bil.getCategory(), bil.getIcon());
        if (bil.getIcon().contains("custom")) {
            custom = true;
            useDefault.setVisibility(View.VISIBLE);
        }

        billerNameHeader.setText(billerName);

        backEditBillerLayout.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            onBackPressed();
        });

        String[] spinnerArray = new String[]{getString(R.string.daily), getString(R.string.weekly), getString(R.string.biweekly), getString(R.string.monthly),
                getString(R.string.quarterly), getString(R.string.yearly)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editFrequency.setAdapter(adapter);
        editFrequency.setSelection(Integer.parseInt(frequency));

        String[] spinnerArray1 = new String[]{getString(R.string.autoLoan), getString(R.string.creditCard), getString(R.string.entertainment),
                getString(R.string.insurance), getString(R.string.miscellaneous), getString(R.string.mortgage), getString(R.string.personalLoans), getString(R.string.utilities)};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(adapter1);

        sCategory.setSelection(Integer.parseInt(bil.getCategory()));
        ArrayList<Integer> icons = new ArrayList<>(Arrays.asList(R.drawable.auto, R.drawable.credit_card, R.drawable.entertainment, R.drawable.insurance,
                R.drawable.invoice, R.drawable.mortgage, R.drawable.personal_loan, R.drawable.utilities));
        final int[] userSelection = {Integer.parseInt(bil.getCategory())};
        editWebsite.setText(bil.getWebsite());
        final boolean[] found = {false};
        sCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!found[0] && !custom) {
                    userSelection[0] = sCategory.getSelectedItemPosition();
                    loadIcon.loadDefault(EditBiller.this, icons.get(adapter1.getPosition(sCategory.getSelectedItem().toString())), editBillerIcon);
                    custom = false;
                }
                if (sCategory.getSelectedItemPosition() == 0 || sCategory.getSelectedItemPosition() == 5 || sCategory.getSelectedItemPosition() == 6) {
                    numberOfPaymentsLayout.setVisibility(View.VISIBLE);
                    editRecurring.setChecked(true);
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
                calculateRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (editRecurring.isChecked()) {
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
            userSelection[0] = sCategory.getSelectedItemPosition();
            loadIcon.loadDefault(EditBiller.this, icons.get(adapter1.getPosition(sCategory.getSelectedItem().toString())), editBillerIcon);
            editBillerIcon.setContentPadding(100,100,100,100);
            useDefault.setVisibility(View.GONE);
            custom = false;
        });

        photoChooser.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        editDueDate.setOnClickListener(view -> {

            dueDateChanged = true;
            getDateFromUser(editDueDate, editAmountDue);
        });

        editFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                frequencyChanged = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editRecurring.setOnClickListener(view -> recurringChanged = true);

        ArrayList <String> billerNames = new ArrayList<>();
        for (Bill bills: thisUser.getBills()) {
            billerNames.add(bills.getBillerName().toLowerCase(Locale.getDefault()).trim());
        }

        final boolean[] nameLength = {true};
        final boolean[] websiteLength = {true};
        final boolean[] amountDue = {true};
        final boolean[] dueDate = {true};

        boolean finalDarkMode = darkMode;

        editBillerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (Biller biller : billers) {
                    if (editBillerName.getText().toString().trim().toLowerCase().contains(biller.getBillerName().trim().toLowerCase())) {
                        found[0] = true;
                        loadIcon.loadImageFromDatabase(EditBiller.this, editBillerIcon, biller.getIcon());
                        custom = true;
                        editWebsite.setText(biller.getWebsite());
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

        editBillerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                billerNameHeader.setText(editBillerName.getText());
                editBillerName.removeTextChangedListener(this);
                String name = editBillerName.getText().toString().toLowerCase(Locale.getDefault()).trim();
                for (String bill: billerNames) {
                    if (bill.equals(name) && !bill.equalsIgnoreCase(billerName)) {
                        editBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                        nameLength[0] = false;
                        submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                        billerNameError.setVisibility(View.VISIBLE);
                        billerNameError.setText(R.string.billerExists);
                    }
                }
                if (name.length() > 1 && !custom) {
                    nameLength[0] = true;
                    for (Biller biller : billers) {
                        if (name.contains(biller.getBillerName().toLowerCase())) {
                            found[0] = true;
                            loadIcon.loadImageFromDatabase(EditBiller.this, editBillerIcon, biller.getIcon());
                            //editBillerIcon.setContentPadding(100,100,100,100);
                            custom = true;
                            editWebsite.setText(biller.getWebsite());
                            int selection = adapter1.getPosition(biller.getType());
                            sCategory.setSelection(selection);
                            useDefault.setVisibility(View.VISIBLE);
                            break;
                        } else {
                            custom = false;
                            found[0] = false;
                            loadIcon.loadDefault(EditBiller.this, icons.get(userSelection[0]), editBillerIcon);
                            editBillerIcon.setContentPadding(100,100,100,100);
                            editWebsite.setText("");
                            sCategory.setSelection(userSelection[0]);
                            useDefault.setVisibility(View.GONE);
                        }
                    }
                }
                if (name.isEmpty()) {
                    editBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                    found[0] = false;
                    billerNameError.setVisibility(View.VISIBLE);
                    billerNameError.setText(R.string.billerNameCantBeBlank);
                    nameLength[0] = false;
                }
                else {
                    for (Bill bill: thisUser.getBills()) {
                        if (bill.getBillerName().equals(editBillerName.getText().toString()) && !bill.getBillerName().equalsIgnoreCase(billerName)) {
                            editBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0);
                            nameLength[0] = false;
                            submitBiller.setEnabled(false);
                            submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                            billerNameError.setVisibility(View.VISIBLE);
                            billerNameError.setText(R.string.billerExists);
                        }
                        else {
                            editBillerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall,0);
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
                editBillerName.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        addNumberOfPayments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateRate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editWebsite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editWebsite.getText().toString().length() < 7 && editWebsite.getText().toString().length() > 0) {
                    editWebsite.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                    submitBiller.setEnabled(false);
                    websiteLength[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    websiteError.setVisibility(View.VISIBLE);
                }
                else if (editWebsite.getText().toString().length() == 0) {
                    editWebsite.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                    websiteError.setVisibility(View.GONE);
                    websiteLength[0] = false;
                }
                else {
                    editWebsite.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.checkmarksmall, 0);
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

        editDueDate.setOnClickListener(view -> {
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                editDueDate.setTextColor(getResources().getColor(R.color.white, getTheme()));
            }
            else {
                editDueDate.setTextColor(getResources().getColor(R.color.black, getTheme()));
            }
            getDateFromUser(editDueDate, editAmountDue);

        });

        editDueDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (editDueDate.getText().toString().length() < 5) {
                    submitBiller.setEnabled(false);
                    dueDate[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    editDueDate.setError(getString(R.string.dueDateCantBeBlank));
                    editDueDate.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                }
                else {
                    editDueDate.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.checkmarksmall, 0);
                    TextViewCompat.setCompoundDrawableTintList(editDueDate, ColorStateList.valueOf(getResources().getColor(R.color.green, getTheme())));
                    editDueDate.setError(null);
                    dueDate[0] = true;
                    if (nameLength[0] && amountDue[0] && websiteLength[0]) {
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

        if (bil.getAmountDue() != null && !bil.getAmountDue().equals("")) {
            editAmountDue.setText(fn.addSymbol(bil.getAmountDue()));
        }
        else {
            editAmountDue.setText(fn.addSymbol("0"));
        }
        editAmountDue.addTextChangedListener( new MoneyInput(editAmountDue));
        currentBalance.addTextChangedListener(new MoneyInput(currentBalance));
        addEscrow.addTextChangedListener(new MoneyInput(currentBalance));

        calculateRate();

        currentBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateRate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        editAmountDue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int n, int i1, int i2) {

                if (editAmountDue.getText().toString().length() < 2 && editAmountDue.getText().toString().length() > 0) {
                    editAmountDue.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                    submitBiller.setEnabled(false);
                    amountDue[0] = false;
                    submitBiller.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.lightblue)));
                    amountDueError.setVisibility(View.VISIBLE);
                }
                else if (editAmountDue.getText().toString().length() == 0) {
                    editAmountDue.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                    amountDueError.setVisibility(View.GONE);
                    amountDue[0] = false;
                }
                else {
                    editAmountDue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.checkmarksmall,0);
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
                }
                calculateRate();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        editFrequency.setOnFocusChangeListener((view, b) -> {

            if (view.hasFocus()) {
                editFrequency.performClick();
            }
        });

        submitBiller.setOnClickListener(view -> {

            pb.setVisibility(View.VISIBLE);

            String billerName = editBillerName.getText().toString();
            String website = editWebsite.getText().toString();
            String amount = fn.makeDouble(editAmountDue.getText().toString());
            String frequency = String.valueOf(editFrequency.getSelectedItemPosition());
            String category = String.valueOf(sCategory.getSelectedItemPosition());
            double escrow = 0;
            String numberOfPayments = "1000";
            boolean recurring = editRecurring.isChecked();
            if (recurring && continuousSwitch.isChecked()) {
                if (!addNumberOfPayments.getText().toString().equals("")) {
                    numberOfPayments = addNumberOfPayments.getText().toString();
                }
            }
            if (addEscrow.getText().length() > 1 && Double.parseDouble(fn.makeDouble(addEscrow.getText().toString())) > 0) {
                escrow = Double.parseDouble(fn.makeDouble(addEscrow.getText().toString()));
            }

            String icon1 = "";
            BillerImage billerImage = new BillerImage();
            if (custom) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        icon1 = billerImage.storeImage(editBillerIcon.getDrawable(), billerName, custom);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                icon1 = String.valueOf(icons.get(adapter1.getPosition(sCategory.getSelectedItem().toString())));
            }
            if (editAmountDue.getText().toString().length() == 0) {
                amount = "0.00";
            }

            int dueDateValue = df.convertDateStringToInt(editDueDate.getText().toString());
            Bill newBiller = bil;
            
            newBiller.setIcon(icon1);
            newBiller.setBillerName(billerName);
            newBiller.setAmountDue(amount);
            newBiller.setDayDue(dueDateValue);
            newBiller.setWebsite(website);
            newBiller.setEscrow(escrow);
            newBiller.setFrequency(frequency);
            newBiller.setCategory(category);
            newBiller.setRecurring(recurring);
            newBiller.setIcon(icon1);
            newBiller.setBalance(Double.parseDouble(fn.makeDouble(currentBalance.getText().toString())));
            newBiller.setPaymentsRemaining(numberOfPayments);
            thisUser.getBills().remove(bil);
            thisUser.getBills().add(newBiller);
            if (thisUser != null) {
                SaveUserData save = new SaveUserData();
                save.saveUserData(EditBiller.this);
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).update("bills", thisUser.getBills());
            db.collection("payments").document(uid).set(paymentInfo, SetOptions.merge());

            Intent launchMain = new Intent(mContext, ViewBillers.class);
            launchMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launchMain);
        });
    }

    public void calculateRate() {

        if (editAmountDue.getText().length() > 1 && addNumberOfPayments.getText().length() > 0) {
            double amountDue1 = Double.parseDouble(fn.makeDouble(editAmountDue.getText().toString()));
            if (addEscrow.getText().length() > 1) {
                amountDue1 = amountDue1 - Double.parseDouble(fn.makeDouble(addEscrow.getText().toString()));
            }
            double totalPaid = amountDue1 * Double.parseDouble(fn.makeDouble(addNumberOfPayments.getText().toString()));
            double totalInterest = totalPaid - Double.parseDouble(fn.makeDouble(currentBalance.getText().toString()));
            double numYears;
            double numberOfPayments = Double.parseDouble(addNumberOfPayments.getText().toString());
            double balance = Double.parseDouble(fn.makeDouble(currentBalance.getText().toString()));
            int freq = editFrequency.getSelectedItemPosition();
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

    public void loadImage (Uri uri) {
        if (uri != null) {
            custom = true;
            useDefault.setVisibility(View.VISIBLE);
            editBillerIcon.setImageTintList(null);
            loadIcon.loadImageFromDatabase(EditBiller.this, editBillerIcon, String.valueOf(uri));
            //editBillerIcon.setContentPadding(100,100,100,100);
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
        datePicker = new DatePickerDialog(EditBiller.this, R.style.MyDatePickerStyle, (datePicker1, i, i1, i2) -> {
            int fixMonth = i1 + 1;
            LocalDate date = LocalDate.of(i, fixMonth, i2);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            String startDate = formatter.format(date);
            dueDate.setText(startDate);
            amountDue.requestFocus();
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.showSoftInput(amountDue, InputMethodManager.RESULT_SHOWN);
        }, year, month - 1, day);
        datePicker.setTitle(getString(R.string.selectDate));
        datePicker.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pb.setVisibility(View.GONE);
    }
}