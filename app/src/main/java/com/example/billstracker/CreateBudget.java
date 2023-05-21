package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Locale;

public class CreateBudget extends AppCompatActivity {

    EditText etPayAmount, etSavingsPercentage, etAutomotivePercentage, etBeautyPercentage, etClothingPercentage, etEntertainmentPercentage, etGroceriesPercentage,
            etHealthPercentage, etRestaurantsPercentage, etOtherPercentage;
    TextView savingsAmount, billsAverage, disposableIncome, automotiveAmount, beautyAmount, clothingAmount, entertainmentAmount, groceriesAmount, healthAmount, restaurantAmount, otherAmount, budgetStartDate, budgetEndDate,
    savingsError, categoriesError;
    Spinner payFrequency;
    androidx.appcompat.widget.SwitchCompat createForMe;
    ImageView back;
    Button submit;
    SaveUserData load = new SaveUserData();
    FixNumber fn = new FixNumber();
    DateFormatter df = new DateFormatter();
    double weeklyBills;
    double weeklyPay;
    double disposable;
    double savings;
    int weekStart;
    int weekEnd;
    LocalDate startDate, endDate;
    boolean firstRun;
    boolean catError;
    boolean saveError;
    ArrayList <Integer> values = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        etPayAmount = findViewById(R.id.etPayAmount);
        etSavingsPercentage = findViewById(R.id.etSavingsPercentage);
        etAutomotivePercentage = findViewById(R.id.etAutomotivePercentage);
        etBeautyPercentage = findViewById(R.id.etBeautyPercentage);
        etClothingPercentage = findViewById(R.id.etClothingPercentage);
        etEntertainmentPercentage = findViewById(R.id.etEntertainmentPercentage);
        etGroceriesPercentage = findViewById(R.id.etGroceriesPercentage);
        etHealthPercentage = findViewById(R.id.etHealthPercentage);
        etRestaurantsPercentage = findViewById(R.id.etRestaurantsPercentage);
        etOtherPercentage = findViewById(R.id.etOtherPercentage);
        budgetStartDate = findViewById(R.id.budgetStartDate);
        budgetEndDate = findViewById(R.id.budgetEndDate);
        savingsAmount = findViewById(R.id.savingsAmount);
        billsAverage = findViewById(R.id.billsAverage);
        savingsError = findViewById(R.id.savingsError2);
        categoriesError = findViewById(R.id.categoriesError2);
        disposableIncome = findViewById(R.id.weeklyDisposableIncome);
        automotiveAmount = findViewById(R.id.automotiveAmount);
        beautyAmount = findViewById(R.id.beautyAmount);
        clothingAmount = findViewById(R.id.clothingAmount);
        entertainmentAmount = findViewById(R.id.entertainmentAmount);
        groceriesAmount = findViewById(R.id.groceriesAmount);
        healthAmount = findViewById(R.id.healthAmount);
        restaurantAmount = findViewById(R.id.restaurantsAmount);
        otherAmount = findViewById(R.id.otherAmount);
        payFrequency = findViewById(R.id.payFrequencySpinner);
        createForMe = findViewById(R.id.createForMe);
        submit = findViewById(R.id.submitBudget);
        back = findViewById(R.id.backCreateBudget);

        startDate = LocalDate.now(ZoneId.systemDefault());
        endDate = startDate.plusYears(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
        budgetStartDate.setText(formatter.format(startDate));
        budgetEndDate.setText(formatter.format(endDate));

        firstRun = false;
        catError = false;
        saveError = false;

        back.setOnClickListener(v -> onBackPressed());

        budgetStartDate.setOnClickListener(v -> getDateFromUser(budgetStartDate, true));
        budgetEndDate.setOnClickListener(v -> getDateFromUser(budgetEndDate, false));

        weekStart = df.calcDateValue(startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = df.calcDateValue(startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));

        if (thisUser == null) {
            load.loadUserData(CreateBudget.this);
        }
            etPayAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(thisUser.getIncome()))));

        etPayAmount.addTextChangedListener(new MoneyInput(etPayAmount));

        etPayAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String[] spinnerArray = new String[]{getString(R.string.weekly), getString(R.string.biweekly), getString(R.string.monthly)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payFrequency.setAdapter(adapter);
        if (thisUser.getPayFrequency() != null) {
            payFrequency.setSelection(Integer.parseInt(thisUser.getPayFrequency()));
        }

        payFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        etSavingsPercentage.setText("20%");

        updateValues();

        createForMe.setChecked(false);
        createForMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                values.clear();
                if (etAutomotivePercentage.getText().toString().replaceAll("%", "").length() > 0 && !etAutomotivePercentage.getText().toString().equals("")) {
                    values.add(Integer.parseInt(etAutomotivePercentage.getText().toString().replaceAll("%", "")));
                }
                if (etBeautyPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etBeautyPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etBeautyPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etClothingPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etClothingPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etClothingPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etEntertainmentPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etEntertainmentPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etEntertainmentPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etGroceriesPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etGroceriesPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etGroceriesPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etHealthPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etHealthPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etHealthPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etOtherPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etOtherPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etOtherPercentage.getText().toString().replaceAll("%", "")));
                }
                if (etRestaurantsPercentage.getText().toString().replaceAll("%", "").length() > 0 && !etRestaurantsPercentage.getText().toString().equals("")) {
                values.add(Integer.parseInt(etRestaurantsPercentage.getText().toString().replaceAll("%", "")));
                }
                autoBudget();
            }
            else {

                if (!values.isEmpty() && values.size() > 7) {
                    etAutomotivePercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(0)));
                    etBeautyPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(1)));
                    etClothingPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(2)));
                    etEntertainmentPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(3)));
                    etGroceriesPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(4)));
                    etHealthPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(5)));
                    etRestaurantsPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(6)));
                    etOtherPercentage.setText(String.format(Locale.getDefault(), "%d%%", values.get(7)));
                }

                etAutomotivePercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etBeautyPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etClothingPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etEntertainmentPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etGroceriesPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etHealthPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etRestaurantsPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));
                etOtherPercentage.setTextColor(getResources().getColor(R.color.blackAndWhite, getTheme()));

                etAutomotivePercentage.setEnabled(true);
                etBeautyPercentage.setEnabled(true);
                etClothingPercentage.setEnabled(true);
                etEntertainmentPercentage.setEnabled(true);
                etGroceriesPercentage.setEnabled(true);
                etHealthPercentage.setEnabled(true);
                etRestaurantsPercentage.setEnabled(true);
                etOtherPercentage.setEnabled(true);
                values.clear();
                updateValues();
            }
        });

        etSavingsPercentage.addTextChangedListener(new PercentageInput(etSavingsPercentage, savingsAmount));
        etSavingsPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etAutomotivePercentage.addTextChangedListener(new PercentageInput(etAutomotivePercentage, automotiveAmount));
        etBeautyPercentage.addTextChangedListener(new PercentageInput(etBeautyPercentage, beautyAmount));
        etClothingPercentage.addTextChangedListener(new PercentageInput(etClothingPercentage, clothingAmount));
        etEntertainmentPercentage.addTextChangedListener(new PercentageInput(etEntertainmentPercentage, entertainmentAmount));
        etGroceriesPercentage.addTextChangedListener(new PercentageInput(etGroceriesPercentage, groceriesAmount));
        etHealthPercentage.addTextChangedListener(new PercentageInput(etHealthPercentage, healthAmount));
        etRestaurantsPercentage.addTextChangedListener(new PercentageInput(etRestaurantsPercentage, restaurantAmount));
        etOtherPercentage.addTextChangedListener(new PercentageInput(etOtherPercentage, automotiveAmount));

        etAutomotivePercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etBeautyPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etClothingPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etEntertainmentPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etGroceriesPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etHealthPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etOtherPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etRestaurantsPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addPercentages();
                updateValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submit.setOnClickListener(v -> {

            if (!catError && !saveError) {
                if (etPayAmount.length() > 1 && etSavingsPercentage.length() > 1 && etAutomotivePercentage.length() > 1 && etBeautyPercentage.length() > 1 && etClothingPercentage.length() > 1 && etEntertainmentPercentage.length() > 1 &&
                        etGroceriesPercentage.length() > 1 && etHealthPercentage.length() > 1 && etRestaurantsPercentage.length() > 1 && etOtherPercentage.length() > 1) {
                    createNewBudget();
                } else {
                    Toast.makeText(CreateBudget.this, getString(R.string.all_fields_must_be_filled_in), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void createNewBudget() {

        double payAmount = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString()));
        int payFreq = payFrequency.getSelectedItemPosition();
        int start = df.calcDateValue(startDate);
        int end = df.calcDateValue(endDate);
        int budgetId = id();
        int auto = Integer.parseInt(etAutomotivePercentage.getText().toString().replaceAll("%", ""));
        int beauty = Integer.parseInt(etBeautyPercentage.getText().toString().replaceAll("%", ""));
        int clothing = Integer.parseInt(etClothingPercentage.getText().toString().replaceAll("%", ""));
        int entertainment = Integer.parseInt(etEntertainmentPercentage.getText().toString().replaceAll("%", ""));
        int groceries = Integer.parseInt(etGroceriesPercentage.getText().toString().replaceAll("%", ""));
        int health = Integer.parseInt(etHealthPercentage.getText().toString().replaceAll("%", ""));
        int restaurants = Integer.parseInt(etRestaurantsPercentage.getText().toString().replaceAll("%", ""));
        int other = Integer.parseInt(etOtherPercentage.getText().toString().replaceAll("%", ""));
        int savings = Integer.parseInt(etSavingsPercentage.getText().toString().replaceAll("%", ""));
        Budgets a = new Budgets(payAmount, payFreq, start, end, budgetId, auto, beauty, clothing, entertainment, groceries, health, restaurants, other, savings);

        ArrayList <Budgets> remove = new ArrayList<>();
        for (Budgets budget: thisUser.getBudgets()) {
            if (budget.getStartDate() > start && budget.getStartDate() < end) {
                budget.setStartDate(end + 1);
                if (end + 1 > budget.getEndDate()) {
                    remove.add(budget);
                }

            }
            else if (budget.getEndDate() > start && budget.getEndDate() < end) {
                budget.setEndDate(start - 1);
                if (start - 1 < budget.getStartDate()) {
                    remove.add(budget);
                }
            }
            else if (budget.getStartDate() == start && budget.getEndDate() == end) {
                remove.add(budget);
            }
            else if (budget.getStartDate() == start && budget.getEndDate() < end) {
                remove.add(budget);
            }
            else if (budget.getStartDate() > start && budget.getEndDate() == end) {
                remove.add(budget);
            }
        }
        thisUser.getBudgets().removeAll(remove);
        thisUser.getBudgets().add(a);
        thisUser.setIncome(payAmount);
        thisUser.setPayFrequency(String.valueOf(payFreq));
        load.saveUserData(CreateBudget.this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).set(thisUser, SetOptions.merge());
        Intent budget = new Intent(CreateBudget.this, Budget.class);
        startActivity(budget);
    }

    public void updateValues() {

        weeklyPay = 0;
        weeklyBills = 0;
        savings = 0;
        disposable = 0;

        if (etPayAmount.getText().length() > 1) {
            switch (payFrequency.getSelectedItemPosition()) {
                case 0:
                    weeklyPay = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString()));
                    break;
                case 1:
                    weeklyPay = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString())) / 2;
                    break;
                case 2:
                    int numDays = LocalDate.now().lengthOfMonth();
                    weeklyPay = Double.parseDouble(fn.makeDouble(etPayAmount.getText().toString())) / (double) (numDays / 7);
                    break;
            }
        }

        int monthStart = df.calcDateValue(startDate.withDayOfMonth(1));
        int monthEnd = df.calcDateValue(startDate.withDayOfMonth(startDate.lengthOfMonth()));

            for (Payments payment: paymentInfo.getPayments()) {
                if (payment.getPaymentDate() >= monthStart && payment.getPaymentDate() <= monthEnd) {
                    weeklyBills = weeklyBills + Double.parseDouble(fn.makeDouble(payment.getPaymentAmount()));
                }
            }

        int daysInMonth = startDate.lengthOfMonth();
        weeklyBills = weeklyBills / ((double) daysInMonth / 7);
        billsAverage.setText(fn.addSymbol(String.valueOf(weeklyBills)));
        if (etSavingsPercentage.getText().toString().length() > 1) {
            savings = (weeklyPay) * (Double.parseDouble(etSavingsPercentage.getText().toString().replaceAll("%", "")) / 100);
        }
        disposable = (weeklyPay - weeklyBills) - savings;
        if (disposable < 0) {
            savingsError.setVisibility(View.VISIBLE);
            saveError = true;
        }
        else {
            savingsError.setVisibility(View.GONE);
            saveError = false;
        }
        savingsAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(savings))));
        disposableIncome.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable))));
        calculateAmounts();
    }

    @SuppressLint("SetTextI18n")
    public void autoBudget () {
        etAutomotivePercentage.setText("12%");
        etBeautyPercentage.setText("8%");
        etClothingPercentage.setText("9%");
        etEntertainmentPercentage.setText("8%");
        etGroceriesPercentage.setText("18%");
        etHealthPercentage.setText("20%");
        etRestaurantsPercentage.setText("10%");
        etOtherPercentage.setText("15%");

        etAutomotivePercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etBeautyPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etClothingPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etEntertainmentPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etGroceriesPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etHealthPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etRestaurantsPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));
        etOtherPercentage.setTextColor(getResources().getColor(R.color.grey, getTheme()));

        //etAutomotivePercentage.setEnabled(false);
        etBeautyPercentage.setEnabled(false);
        etClothingPercentage.setEnabled(false);
        etEntertainmentPercentage.setEnabled(false);
        etGroceriesPercentage.setEnabled(false);
        etHealthPercentage.setEnabled(false);
        etRestaurantsPercentage.setEnabled(false);
        etOtherPercentage.setEnabled(false);

        calculateAmounts();
    }

    public void calculateAmounts () {
        if (!etAutomotivePercentage.getText().toString().equals("") && !etAutomotivePercentage.getText().toString().equals("%")) {
            automotiveAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etAutomotivePercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            automotiveAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etBeautyPercentage.getText().toString().equals("") && !etBeautyPercentage.getText().toString().equals("%")) {
        beautyAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etBeautyPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            beautyAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etClothingPercentage.getText().toString().equals("") && !etClothingPercentage.getText().toString().equals("%")) {
        clothingAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etClothingPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            clothingAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etEntertainmentPercentage.getText().toString().equals("") && !etEntertainmentPercentage.getText().toString().equals("%")) {
        entertainmentAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etEntertainmentPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            entertainmentAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etGroceriesPercentage.getText().toString().equals("") && !etGroceriesPercentage.getText().toString().equals("%")) {
        groceriesAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etGroceriesPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            groceriesAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etHealthPercentage.getText().toString().equals("") && !etHealthPercentage.getText().toString().equals("%")) {
        healthAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etHealthPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            healthAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etRestaurantsPercentage.getText().toString().equals("") && !etRestaurantsPercentage.getText().toString().equals("%")) {
        restaurantAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etRestaurantsPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            restaurantAmount.setText(fn.addSymbol("0.00"));
        }
        if (!etOtherPercentage.getText().toString().equals("") && !etOtherPercentage.getText().toString().equals("%")) {
            otherAmount.setText(fn.addSymbol(fn.makeDouble(String.valueOf(disposable * Double.parseDouble(etOtherPercentage.getText().toString().replaceAll("%", "")) / 100))));
        }
        else {
            otherAmount.setText(fn.addSymbol("0.00"));
        }
    }

    public void addPercentages () {

        int auto = 0;
        int beauty = 0;
        int clothes = 0;
        int entertainment = 0;
        int groceries = 0;
        int health = 0;
        int other = 0;
        int restaurants = 0;

        if (etAutomotivePercentage.getText().length() > 1) {
            auto = Integer.parseInt(etAutomotivePercentage.getText().toString().replaceAll("%", ""));
        }
        if (etBeautyPercentage.getText().length() > 1) {
            beauty = Integer.parseInt(etBeautyPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etClothingPercentage.getText().length() > 1) {
            clothes = Integer.parseInt(etClothingPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etEntertainmentPercentage.getText().length() > 1) {
            entertainment = Integer.parseInt(etEntertainmentPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etGroceriesPercentage.getText().length() > 1) {
            groceries = Integer.parseInt(etGroceriesPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etHealthPercentage.getText().length() > 1) {
            health = Integer.parseInt(etHealthPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etOtherPercentage.getText().length() > 1) {
            other = Integer.parseInt(etOtherPercentage.getText().toString().replaceAll("%", ""));
        }
        if (etRestaurantsPercentage.getText().length() > 1) {
            restaurants = Integer.parseInt(etRestaurantsPercentage.getText().toString().replaceAll("%", ""));
        }

        int total = auto + beauty + clothes + entertainment + groceries + health + other + restaurants;

        if (total > 100) {
            categoriesError.setVisibility(View.VISIBLE);
            catError = true;
        }
        else {
            categoriesError.setVisibility(View.GONE);
            catError = false;
        }

    }

    public void updateWeek (LocalDate date) {
        startDate = date;
        weekStart = df.calcDateValue(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
        weekEnd = df.calcDateValue(date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        updateValues();
    }

    public void getDateFromUser(TextView dueDate, boolean start) {

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        int day = today.getDayOfMonth();
        int year = today.getYear();
        int month = today.getMonthValue();

        DatePickerDialog datePicker;
        datePicker = new DatePickerDialog(CreateBudget.this, R.style.MyDatePickerStyle, (datePicker1, i, i1, i2) -> {
            int fixMonth = i1 + 1;
            LocalDate date = LocalDate.of(i, fixMonth, i2);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());
            String startDate = formatter.format(date);
            dueDate.setText(startDate);
            if (start) {
                updateWeek(date);
            }
            else {
                endDate = date;
            }
        }, year, month - 1, day);
        datePicker.setTitle(getString(R.string.selectDate));
        datePicker.show();
    }

    int id() {
        final String AB = "0123456789";
        SecureRandom rnd = new SecureRandom();
        int length = 9;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return Integer.parseInt(sb.toString());
    }
}