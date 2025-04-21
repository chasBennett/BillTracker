package com.example.billstracker.activities;

import static com.example.billstracker.activities.Login.bills;
import static com.example.billstracker.activities.Login.payments;
import static com.example.billstracker.activities.Login.uid;
import static com.google.common.io.Files.getFileExtension;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Bill;
import com.example.billstracker.custom_objects.Biller;
import com.example.billstracker.custom_objects.Payment;
import com.example.billstracker.popup_classes.BottomDrawer;
import com.example.billstracker.popup_classes.DatePicker;
import com.example.billstracker.popup_classes.Notify;
import com.example.billstracker.recycler_adapters.BillerNameAdapter;
import com.example.billstracker.tools.BillerManager;
import com.example.billstracker.tools.Data;
import com.example.billstracker.tools.DateFormat;
import com.example.billstracker.tools.FirebaseTools;
import com.example.billstracker.tools.FixNumber;
import com.example.billstracker.tools.MoneyFormatterWatcher;
import com.example.billstracker.tools.Prefs;
import com.example.billstracker.tools.Tools;
import com.example.billstracker.tools.UserData;
import com.example.billstracker.tools.Watcher;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class AddBiller extends AppCompatActivity {

    LinearLayout billerCategoryBox, billerFrequencyBox, noBillerData;
    ConstraintLayout pb;
    AutoCompleteTextView addBillerName;
    TextInputEditText addBillerWebsite, addPaymentAmount, addEscrowAmount, addPaymentsRemaining, addBalance;
    TextInputLayout escrowBox, payRemainBox, balanceBox;
    TextView billerCardName, billerCardWebsite, noBillerName, noBillerWebsite, billerData, addPaymentDate, addBillerTitle;
    ShapeableImageView addBillerIcon;
    Button proceed;
    LinearLayout back;
    TextView billerCategorySpinner, billerFrequencySpinner;
    Bill bill;
    boolean edit, customIcon, recurring;
    ArrayList <Biller> billers;
    String billerId;
    ImageView addBillerLogo;
    String customUri;
    int category, frequency;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    String originalBillerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_biller);

        addBillerName = findViewById(R.id.addBillerName);
        billerCategoryBox = findViewById(R.id.billerCategoryBox);
        addBillerWebsite = findViewById(R.id.addBillerWebsite);
        addPaymentDate = findViewById(R.id.addPaymentDate);
        addPaymentAmount = findViewById(R.id.addPaymentAmount);
        addEscrowAmount = findViewById(R.id.addEscrowAmount);
        billerFrequencyBox = findViewById(R.id.billerFrequencyBox);
        addPaymentsRemaining = findViewById(R.id.addPaymentsRemaining);
        addBalance = findViewById(R.id.addBalance);
        billerCardName = findViewById(R.id.billerCardName);
        billerCardWebsite = findViewById(R.id.billerCardWebsite);
        addBillerTitle = findViewById(R.id.addBillerTitle);
        noBillerName = findViewById(R.id.noBillerName);
        noBillerWebsite = findViewById(R.id.noBillerWebsite);
        noBillerData = findViewById(R.id.noBillerData);
        billerData = findViewById(R.id.billerData);
        billerCategorySpinner = findViewById(R.id.billerCategorySpinner);
        billerFrequencySpinner = findViewById(R.id.billerFrequencySpinner);
        addBillerIcon = findViewById(R.id.addBillerIcon);
        addBillerLogo = findViewById(R.id.addBillerLogo);
        back = findViewById(R.id.backButton);
        proceed = findViewById(R.id.submitBiller);
        escrowBox = findViewById(R.id.escrowBox);
        payRemainBox = findViewById(R.id.payRemainBox);
        balanceBox = findViewById(R.id.balanceBox);
        pb = findViewById(R.id.pb5);
        Tools.fixProgressBarLogo(pb);
        billers = new ArrayList<>();
        category = 0;
        frequency = 0;
        recurring = false;

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::loadImage);

        edit = false;
        customIcon = false;
        bill = null;
        customUri = "default";
        escrowBox.setVisibility(View.GONE);
        balanceBox.setVisibility(View.GONE);
        payRemainBox.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);

        Tools.fixLogo(addBillerLogo);

        loadBillers();
    }

    public void loadBillers () {

        if (billers.isEmpty()) {
            billers = Prefs.getBillers(AddBiller.this);
            if (billers == null || billers.isEmpty()) {
                billers = new ArrayList<>();
            }
            if (billers.isEmpty()) {
                FirebaseTools.loadBillers(AddBiller.this, billers, isSuccessful -> initialize());
            }
            else {
                initialize();
            }
        }
        else {
            initialize();
        }
    }

    public void initialize () {

        ArrayList <String> categories = Data.getCategories(AddBiller.this);
        ArrayList<String> frequencies = Data.getFrequencies(AddBiller.this);

        pb.setVisibility(View.GONE);

        if (Tools.isDarkMode(AddBiller.this)) {
            addBillerIcon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle, getTheme()));
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().getString("billerId", "") != null && !getIntent().getExtras().getString("billerId", "").isEmpty()) {
            billerId = getIntent().getExtras().getString("billerId", "");
            edit = true;
        }
        else {
            edit = false;
            billerId = String.valueOf(BillerManager.id());
        }

        if (!billerId.isEmpty()) {

            bill = Data.getBill(billerId);

            if (bill != null) {
                recurring = bill.isRecurring();

                if (recurring) {
                    billerFrequencySpinner.setText(frequencies.get(bill.getFrequency() + 1));
                } else {
                    billerFrequencySpinner.setText(frequencies.get(0));
                }
                frequency = bill.getFrequency();
                category = bill.getCategory();
                edit = true;

                originalBillerName = bill.getBillerName();
                billerCardName.setText(bill.getBillerName());
                billerCardWebsite.setText(bill.getWebsite());
                addBillerName.setText(bill.getBillerName());
                addBillerWebsite.setText(bill.getWebsite());
                addBalance.setText(FixNumber.addSymbol(bill.getBalance()));
                billerCategorySpinner.setText(categories.get(bill.getCategory()));
                addEscrowAmount.setText(FixNumber.addSymbol(bill.getEscrow()));
                addPaymentsRemaining.setText(String.valueOf(bill.getPaymentsRemaining()));
                addPaymentAmount.setText(FixNumber.addSymbol(bill.getAmountDue()));
                addPaymentDate.setText(DateFormat.makeDateString(bill.getDueDate()));
                addBillerTitle.setText(getString(R.string.editBiller));

                noBillerName.setVisibility(View.GONE);
                billerCardName.setVisibility(View.VISIBLE);
                noBillerWebsite.setVisibility(View.GONE);
                billerCardWebsite.setVisibility(View.VISIBLE);
                payRemainBox.setVisibility(View.VISIBLE);

                if (category == 0 || category == 5 || category == 6) {
                    balanceBox.setVisibility(View.VISIBLE);
                    if (category == 5) {
                        escrowBox.setVisibility(View.VISIBLE);
                    } else {
                        escrowBox.setVisibility(View.GONE);
                    }
                } else {
                    balanceBox.setVisibility(View.GONE);
                    escrowBox.setVisibility(View.GONE);
                    payRemainBox.setVisibility(View.GONE);
                }

                Tools.loadIcon(addBillerIcon, bill.getCategory(), bill.getIcon());

                if (!bill.getIcon().equals("default")) {
                    customUri = bill.getIcon();
                    customIcon = true;
                }

                generateMessage();
            } else {
                edit = false;
                recurring = false;
                category = 4;
                frequency = 0;
                Tools.loadIcon(addBillerIcon, category, "default");
                addBillerTitle.setText(getString(R.string.add_a_new_biller));
                billerCategorySpinner.setText(categories.get(4));
                billerFrequencySpinner.setText(frequencies.get(0));
                billerId = String.valueOf(BillerManager.id());
            }
        }

        back.setOnClickListener(view -> Tools.onBackSelected(AddBiller.this));

        Tools.setupUI(AddBiller.this, findViewById(android.R.id.content));

        addBillerIcon.setOnClickListener(view -> {
            Tools.hideKeyboard(AddBiller.this);
            if (getCurrentFocus() != null) {
                getCurrentFocus().clearFocus();
            }
            BottomDrawer bd = new BottomDrawer(AddBiller.this);
            bd.setDefaultButtonListener(v12 -> {
                customIcon = false;
                customUri = "default";

                Glide.with(addBillerIcon).load(Data.getIcons().get(category)).fitCenter().into(addBillerIcon);
                addBillerIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.neutralGray, getTheme())));
                addBillerIcon.setContentPadding(40, 40, 40, 40);
                bd.dismissDialog();
            });
            bd.setSelectImageButtonListener(v13 -> {
                pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                bd.dismissDialog();
            });
        });


        if (billers != null && !billers.isEmpty()) {
            BillerNameAdapter adapter = new BillerNameAdapter(AddBiller.this, R.layout.biller_search_result, billers);
            addBillerName.setAdapter(adapter);

            adapter.setClickListener((ignoredPosition, bill) -> {
                Tools.hideKeyboard(AddBiller.this);
                billerCardName.setText(bill.getBillerName());
                billerCardWebsite.setText(bill.getWebsite());
                noBillerWebsite.setVisibility(View.GONE);
                billerCardWebsite.setVisibility(View.VISIBLE);
                category = bill.getType();
                billerCategorySpinner.setText(categories.get(bill.getType()));
                addBillerName.setText(bill.getBillerName());
                addBillerWebsite.setText(bill.getWebsite());
                Glide.with(addBillerIcon).load(bill.getIcon()).into(addBillerIcon);
                customUri = bill.getIcon();
                addBillerIcon.setContentPadding(0, 0, 0, 0);
                addBillerIcon.setImageTintList(null);
                addBillerName.setSelection(addBillerName.getText().length());
                customIcon = true;
                addBillerName.dismissDropDown();
                addBillerWebsite.requestFocus();
            });
        }

        addBillerName.setThreshold(1);

        billerCategoryBox.setOnClickListener(view -> {
            Tools.hideKeyboard(AddBiller.this);
            Tools.spinnerPopup(categories, Data.getIcons(), billerCategorySpinner, item -> {
                category = categories.indexOf(item);

                if (!customIcon) {
                    Glide.with(addBillerIcon).load(Data.getIcons().get(categories.indexOf(item))).fitCenter().into(addBillerIcon);
                }
                generateMessage();
            });
        });

        billerFrequencyBox.setOnClickListener(view -> Tools.spinnerPopup(frequencies, null, billerFrequencySpinner, item -> {
            if (frequencies.indexOf(item) > 1) {
                frequency = frequencies.indexOf(item) - 1;
            }
            else {
                frequency = 0;
            }
            recurring = frequencies.indexOf(item) > 0;
            generateMessage();
        }));

        addPaymentDate.setOnClickListener(view -> {
            long date = DateFormat.makeLong(LocalDate.now(ZoneId.systemDefault()));
            if (bill != null) {
                date = bill.getDueDate();
            }
            DatePicker dp = DateFormat.getPaymentDateFromUser(getSupportFragmentManager(), date, getString(R.string.when_is_this_bill_due));
            dp.setListener(v1 -> {
                addPaymentDate.setError(null);
                if (DatePicker.selection != null) {
                    addPaymentDate.setText(DateFormat.makeDateString(DatePicker.selection));
                    Tools.hideKeyboard(AddBiller.this);
                    generateMessage();
                }
            });
        });

        Watcher watcher = new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                    generateMessage();
            }
        };

        addPaymentAmount.addTextChangedListener(watcher);
        addPaymentAmount.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                Tools.hideKeyboard(AddBiller.this);
                addPaymentDate.performClick();
            }
            return true;
        });

        addPaymentDate.addTextChangedListener(watcher);


        addBillerWebsite.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    noBillerWebsite.setVisibility(View.GONE);
                    billerCardWebsite.setVisibility(View.VISIBLE);
                    billerCardWebsite.setText(editable);
                } else {
                    noBillerWebsite.setVisibility(View.VISIBLE);
                    billerCardWebsite.setVisibility(View.GONE);
                }

            }
        });

        addBillerWebsite.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT) {
                Tools.hideKeyboard(AddBiller.this);
                billerCategorySpinner.performClick();
            }
            return true;
        });

        addPaymentAmount.addTextChangedListener(new MoneyFormatterWatcher(addPaymentAmount));
        addEscrowAmount.addTextChangedListener(new MoneyFormatterWatcher(addEscrowAmount));
        addBalance.addTextChangedListener(new MoneyFormatterWatcher(addBalance));

        proceed.setOnClickListener(view -> {
            Tools.hideKeyboard(AddBiller.this);
            pb.setVisibility(View.VISIBLE);
            boolean complete = true;
            double escrow;
            int paymentsRemaining = 0;
            double balance;

            if (addBillerName.getText().toString().isEmpty()) {
                Notify.createPopup(AddBiller.this, getString(R.string.biller_name_can_t_be_blank), null);
                complete = false;
            }
            if (bills != null && bills.getBills() != null && !bills.getBills().isEmpty() && !edit) {
                for (Bill bill: bills.getBills()) {
                    if (bill.getBillerName().equalsIgnoreCase(addBillerName.getText().toString())) {
                        Notify.createPopup(AddBiller.this, getString(R.string.biller_name_is_already_in_use), null);
                        complete = false;
                        break;
                    }
                }
            }
            else if (edit && !originalBillerName.equals(addBillerName.getText().toString())) {
                for (Bill bill: bills.getBills()) {
                    if (bill.getBillerName().equalsIgnoreCase(addBillerName.getText().toString())) {
                        Notify.createPopup(AddBiller.this, getString(R.string.biller_name_is_already_in_use), null);
                        complete = false;
                    }
                }
            }
            if (addBillerWebsite.getText() != null && addBillerWebsite.getText().toString().isEmpty()) {
                Notify.createPopup(AddBiller.this, getString(R.string.website_can_t_be_blank), null);
                complete = false;
            }
            if (addBillerWebsite.getText() != null && !Patterns.WEB_URL.matcher(addBillerWebsite.getText().toString()).matches()) {
                Notify.createPopup(AddBiller.this, getString(R.string.please_enter_a_valid_web_address), null);
                complete = false;
            }
            if (addPaymentAmount.getText() != null && addPaymentAmount.getText().toString().isEmpty()) {
                Notify.createPopup(AddBiller.this, getString(R.string.payment_amount_can_t_be_blank), null);
                complete = false;
            }
            if (addPaymentAmount.getText() != null && FixNumber.makeDouble(addPaymentAmount.getText().toString()) <= 0) {
                Notify.createPopup(AddBiller.this, getString(R.string.payment_amount_must_be_greater_than_zero), null);
                complete = false;
            }
            if (addPaymentDate.getText().toString().isEmpty()) {
                Notify.createPopup(AddBiller.this, getString(R.string.payment_date_can_t_be_blank), null);
                complete = false;
            }
            if (addEscrowAmount.getText() != null && addEscrowAmount.getText().toString().isEmpty()) {
                escrow = 0;
            }
            else {
                if (addEscrowAmount.getText() != null) {
                    escrow = FixNumber.makeDouble(addEscrowAmount.getText().toString());
                }
                else {
                    escrow = 0;
                }
            }
            if (frequencies.indexOf(billerFrequencySpinner.getText().toString()) > 0) {
                frequency = frequencies.indexOf(billerFrequencySpinner.getText().toString()) -1;
                recurring = true;
            }
            else {
                frequency = 0;
                recurring = false;
            }

            if (!edit) {
                if (frequencies.indexOf(billerFrequencySpinner.getText().toString()) == 0) {
                    paymentsRemaining = 1;
                } else {
                    if (category == 0 || category == 5 || category == 6) {
                        if (addPaymentsRemaining.getText() != null) {
                            paymentsRemaining = FixNumber.makeInt(addPaymentsRemaining.getText().toString());
                        } else {
                            paymentsRemaining = 1000;
                        }
                    } else {
                        paymentsRemaining = 1000;
                    }
                }
            }
            else {
                if (recurring) {
                    if (category == 0 || category == 5 || category == 6) {
                        if (addPaymentsRemaining.getText() != null) {
                            paymentsRemaining = FixNumber.makeInt(addPaymentsRemaining.getText().toString());
                        } else {
                            paymentsRemaining = bill.getPaymentsRemaining();
                        }
                    } else {
                        paymentsRemaining = bill.getPaymentsRemaining();
                    }
                }
                else {
                    boolean found = false;
                    for (Payment payment: payments.getPayments()) {
                        if (payment.getBillerName().equals(originalBillerName) && payment.isPaid()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        paymentsRemaining = 1;
                    }
                }
            }
            if (addBalance.getText() != null && addBalance.getText().toString().replaceAll("[^0-9 ]", "").isEmpty()) {
                balance = 0;
            }
            else {
                if (addBalance.getText() != null) {
                    balance = FixNumber.makeDouble(addBalance.getText().toString());
                }
                else {
                    balance = 0;
                }
            }

            if (complete && addBillerWebsite.getText() != null && addPaymentAmount.getText() != null) {
                String billerName = addBillerName.getText().toString();
                String webAddress = addBillerWebsite.getText().toString();
                double paymentAmount = FixNumber.makeDouble(addPaymentAmount.getText().toString());
                long dueDate = DateFormat.makeLong(addPaymentDate.getText().toString());
                String uri = customUri;

                if (edit) {
                    Bill biller = null;
                    for (Bill bil : bills.getBills()) {
                        if (bil.getBillsId().equals(bill.getBillsId())) {
                            biller = bil;
                            break;
                        }
                    }
                    if (biller != null) {
                        biller.updateBiller(billerName, paymentAmount, dueDate, bill.getDateLastPaid(), bill.getBillsId(), recurring, frequency, webAddress, category, uri, paymentsRemaining, balance, escrow, uid, isSuccessful -> {
                            if (isSuccessful) {
                                pb.setVisibility(View.GONE);
                                Notify.createPopup(AddBiller.this, getString(R.string.biller_was_updated_successfully), null);
                                startActivity(new Intent(AddBiller.this, MainActivity2.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                pb.setVisibility(View.GONE);
                                Notify.createPopup(AddBiller.this, getString(R.string.biller_update_failed), null);
                            }
                        });
                    } else {
                        pb.setVisibility(View.GONE);
                        Notify.createPopup(AddBiller.this, getString(R.string.biller_update_failed), null);
                    }
                }
                else {
                    Bill newBiller = new Bill(billerName, paymentAmount, dueDate, 0, billerId, recurring, frequency, webAddress, category, uri, paymentsRemaining, balance, escrow, uid);
                    bills.getBills().add(newBiller);
                    UserData.save();
                    Intent main = new Intent(AddBiller.this, MainActivity2.class);
                    main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(main);
                }
            }
            else {
                pb.setVisibility(View.GONE);
            }
        });
        addBillerName.addTextChangedListener(new Watcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    noBillerName.setVisibility(View.GONE);
                    billerCardName.setVisibility(View.VISIBLE);
                    billerCardName.setText(editable);
                } else {
                    noBillerName.setVisibility(View.VISIBLE);
                    billerCardName.setVisibility(View.GONE);
                }

            }
        });
    }

    public void generateMessage() {

        String payAmount = FixNumber.addSymbol("0.00");
        String frequencyString;
        String payDate = null;

        if (addPaymentAmount.getText() != null && addPaymentAmount.getText().toString().isEmpty() && addPaymentDate.getText().toString().isEmpty()) {
            noBillerData.setVisibility(View.VISIBLE);
            billerData.setVisibility(View.GONE);
        } else {
            noBillerData.setVisibility(View.GONE);
            billerData.setVisibility(View.VISIBLE);
        }

        if (addPaymentAmount.getText() != null && !addPaymentAmount.getText().toString().isEmpty()) {
            payAmount = FixNumber.addSymbol(FixNumber.makeDouble(addPaymentAmount.getText().toString()));
        }
        frequencyString = billerFrequencySpinner.getText().toString();

        if (!addPaymentDate.getText().toString().isEmpty()) {
            payDate = addPaymentDate.getText().toString();
        }

        if (payDate == null) {
            billerData.setText(String.format(Locale.getDefault(),"%s%s %s", getString(R.string.pay), payAmount, frequencyString));
        } else {
            billerData.setText(String.format(Locale.getDefault(),"%s%s %s%s%s", getString(R.string.pay), payAmount, frequencyString, getString(R.string.starting_on), payDate));
        }
        if (category == 0 || category == 5 || category == 6) {
            if (recurring) {
                payRemainBox.setVisibility(View.VISIBLE);
                balanceBox.setVisibility(View.VISIBLE);
                if (category == 5) {
                    escrowBox.setVisibility(View.VISIBLE);
                } else {
                    escrowBox.setVisibility(View.GONE);
                }
            } else {
                payRemainBox.setVisibility(View.GONE);
                balanceBox.setVisibility(View.GONE);
                escrowBox.setVisibility(View.GONE);
            }
        }
        else {
            payRemainBox.setVisibility(View.GONE);
            balanceBox.setVisibility(View.GONE);
            escrowBox.setVisibility(View.GONE);
        }
    }

    public void loadImage(Uri uri) {
        if (uri != null) {
            addBillerIcon.setImageTintList(null);
            Glide.with(addBillerIcon).load(uri).circleCrop().into(addBillerIcon);
            addBillerIcon.setContentPadding(0,0,0,0);
            customIcon = true;
            uploadImage(uri);
            if (customUri == null) {
                customUri = "default";
            }
            Log.d("PhotoPicker", "Selected URI: " + uri);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    }

    @Override
    protected void onResume() {
        pb.setVisibility(View.GONE);
        super.onResume();
    }
    private void uploadImage(Uri uri) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");

        if (uri != null) {
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString() + "." + getFileExtension(String.valueOf(uri)));
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
            }, 1000);
            StorageTask<UploadTask.TaskSnapshot> uploadTask = fileReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(uri1 -> customUri = uri1.toString());
                    }
                }
            });
        }
    }
}