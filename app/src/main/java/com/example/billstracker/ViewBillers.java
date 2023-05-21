package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.TextViewCompat;

import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ViewBillers extends AppCompatActivity {

    ArrayList<Bill> listBills = new ArrayList<>();
    Context mContext;
    boolean darkMode;
    DateFormatter df = new DateFormatter();
    TextView myStats, navHome, navPaymentHistory,displayUserName, displayEmail, ticketCounter, logout, header, subHeader, navViewBillers, myAchievements;
    ImageView settingsButton, drawerToggle, help, payNext, addBiller;
    LinearLayout navDrawer, hideNav, pb, text;
    FixNumber fn = new FixNumber();
    ScrollView scroll;
    com.google.android.gms.ads.AdView adview;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_billers);

        mContext = this;
        darkMode = false;

        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            darkMode = true;
        }

        pb = findViewById(R.id.pb14);
        adview = findViewById(R.id.adView3);
        scroll = findViewById(R.id.scrollView6);
        header = findViewById(R.id.viewBillersHeader);
        subHeader = findViewById(R.id.textView43);
        text = findViewById(R.id.noBillers);

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);

        if (thisUser == null) {
            SaveUserData load = new SaveUserData();
            load.loadUserData(ViewBillers.this);
        }


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //TOOLBAR AND NAVDRAWER
        //Toolbar
        drawerToggle = findViewById(R.id.drawerToggle);
        settingsButton = findViewById(R.id.settingsButton);
        payNext = findViewById(R.id.payNext);
        help = findViewById(R.id.helpMe);
        addBiller = findViewById(R.id.btnAddBiller);
        ticketCounter = findViewById(R.id.ticketCounter);

        //Navigation Drawer
        navDrawer = findViewById(R.id.navDrawer);
        hideNav = findViewById(R.id.hideNavDrawer);
        navHome = findViewById(R.id.navHome);
        logout = findViewById(R.id.logoutButton);
        navViewBillers = findViewById(R.id.navViewBillers);
        navPaymentHistory = findViewById(R.id.navPaymentHistory);
        myAchievements = findViewById(R.id.myAchievements);
        displayUserName = findViewById(R.id.tvName);
        displayEmail = findViewById(R.id.tvUserName2);
        myStats = findViewById(R.id.myStats);
        TextViewCompat.setCompoundDrawableTintList(navViewBillers, ColorStateList.valueOf(getResources().getColor(R.color.button, getTheme())));
        TextViewCompat.setCompoundDrawableTintList(navHome, ColorStateList.valueOf(getResources().getColor(R.color.blackAndWhite, getTheme())));

        //Hide nav drawer on create
        navDrawer.setVisibility(View.GONE);
        navViewBillers.setBackground(AppCompatResources.getDrawable(ViewBillers.this, R.drawable.border_selected));

        //updates int value on support icon notification bubble
        CountTickets countTickets = new CountTickets();
        countTickets.countTickets(ticketCounter);

        myAchievements.setOnClickListener(v -> {
            Intent achievements = new Intent(ViewBillers.this, AwardCase.class);
            startActivity(achievements);
        });

        help.setOnClickListener(view -> {
            Intent support = new Intent(ViewBillers.this, Support.class);
            support.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pb.setVisibility(View.VISIBLE);
            startActivity(support);
        });

        myStats.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent stats = new Intent(ViewBillers.this, MyStats.class);
            startActivity(stats);
        });

        addBiller.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent addBiller1 = new Intent(mContext, AddBiller.class);
            startActivity(addBiller1);
        });
        settingsButton.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent settings = new Intent(mContext, Settings.class);
            startActivity(settings);
        });
        drawerToggle.setOnClickListener(view -> {
            if (navDrawer.getVisibility() == View.VISIBLE) {
                navDrawer.setVisibility(View.GONE);
            } else {
                navDrawer.setVisibility(View.VISIBLE);
                navDrawer.setFocusableInTouchMode(true);
                navDrawer.setClickable(true);
                hideNav.setOnClickListener(view1 -> navDrawer.setVisibility(View.GONE));
            }
        });

        navHome.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent home = new Intent(mContext, MainActivity2.class);
            startActivity(home);
        });

        navViewBillers.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            Intent billers = new Intent(mContext, ViewBillers.class);
            startActivity(billers);
        });

        navPaymentHistory.setOnClickListener(view -> {
            Intent payments = new Intent(mContext, PaymentHistory.class);
            pb.setVisibility(View.VISIBLE);
            startActivity(payments);
        });

        navDrawer.setOnTouchListener(new OnSwipeTouchListener(ViewBillers.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                navDrawer.setVisibility(View.GONE);
            }
        });

        payNext.setOnClickListener(view -> {

            DateFormatter dateFormatter = new DateFormatter();
            pb.setVisibility(View.VISIBLE);
            Payments next = new Payments();
            next.setPaymentDate(dateFormatter.currentDateAsInt() + 60);
            boolean found = false;
                paymentInfo.getPayments().sort(Comparator.comparing(Payments::getPaymentDate));
                for (Payments payment : paymentInfo.getPayments()) {
                    if (!payment.isPaid() && payment.getPaymentDate() < next.getPaymentDate()) {
                        next = payment;
                        found = true;
                    }
                }

            if (found) {
                Intent pay = new Intent(mContext, PayBill.class);
                pay.putExtra("Due Date", dateFormatter.convertIntDateToString(next.getPaymentDate()));
                pay.putExtra("Biller Name", next.getBillerName());
                pay.putExtra("Amount Due", next.getPaymentAmount());
                pay.putExtra("Is Paid", next.isPaid());
                pay.putExtra("Payment Id", next.getPaymentId());
                pay.putExtra("Current Date", dateFormatter.currentDateAsInt());
                startActivity(pay);
            } else {
                pb.setVisibility(View.GONE);
                androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                alert.setTitle(getString(R.string.noBillsDue));
                alert.setMessage(getString(R.string.noUpcomingBills));
                alert.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {

                });
                androidx.appcompat.app.AlertDialog builder = alert.create();
                builder.show();
            }
        });

        logout.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            GoogleSignIn.getClient(ViewBillers.this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
            LoginManager.getInstance().logOut();
            SharedPreferences sp = ViewBillers.this.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("Stay Signed In", false);
            editor.putString("Username", "");
            editor.putString("Password", "");
            editor.apply();
            Intent validate = new Intent(ViewBillers.this, Logon.class);
            validate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            validate.putExtra("Welcome", true);
            startActivity(validate);
        });

        displayUserName.setText(thisUser.getName());
        displayEmail.setText(thisUser.getUserName());

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        listBills();
    }

    public void refresh () {
        SaveUserData load = new SaveUserData();
        load.loadUserData(ViewBillers.this);
    }

    public void listBills() {

        Context mContext = this;
        LinearLayout linearLayout = findViewById(R.id.llViewBillers);
        linearLayout.invalidate();
        linearLayout.removeAllViews();
        linearLayout.addView(text);
        text.setVisibility(View.GONE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList <String> spinnerArray1 = new ArrayList<>(Arrays.asList(getString(R.string.autoLoan), getString(R.string.creditCard), getString(R.string.entertainment),
                getString(R.string.insurance), getString(R.string.miscellaneous), getString(R.string.mortgage), getString(R.string.personalLoans), getString(R.string.utilities)));
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (thisUser.getBills() == null) {
            thisUser.setBills(new ArrayList<>());
        } else {
            listBills = thisUser.getBills();
        }
        String userId = thisUser.getid();
        int counter = 0;
        if (listBills.size() > 0) {
            listBills.sort(Comparator.comparing(Bill::getBillerName));
            for (Bill bills : listBills) {
                counter = counter + 1;
                if (counter <= 50) {
                    String recurring;
                    String billId = bills.getBillsId();
                    String dueDate;
                    if (bills.isRecurring()) {
                        recurring = getString(R.string.yes);
                        int earliest = 10000;
                        for (Payments pay: paymentInfo.getPayments()) {
                            if (pay.getBillerName().equals(bills.getBillerName()) && !pay.isPaid() && pay.getPaymentDate() < earliest) {
                                earliest = pay.getPaymentDate();
                            }
                        }
                        if (earliest == 10000) {
                            dueDate = df.convertIntDateToString(bills.getDayDue());
                        }
                        else {
                            dueDate = df.convertIntDateToString(earliest);
                        }
                    } else {
                        recurring = getString(R.string.no);
                        dueDate = df.convertIntDateToString(bills.getDayDue());
                    }
                    View viewBiller = View.inflate(ViewBillers.this, R.layout.view_biller, null);
                    ImageView billerArrow = viewBiller.findViewById(R.id.billerArrow);
                    TextView viewBillerName = viewBiller.findViewById(R.id.viewBillerName);
                    LinearLayout billerDetails = viewBiller.findViewById(R.id.billerDetails);
                    String lastPaid;
                    if (bills.getDateLastPaid() == 0) {
                        lastPaid = getString(R.string.no_payments_reported);
                    } else {
                        lastPaid = df.convertIntDateToString(bills.getDateLastPaid());
                    }
                    TextView category = viewBiller.findViewById(R.id.showCategory);
                    TextView showBillerName = viewBiller.findViewById(R.id.showBillerName);
                    TextView showAmountDue = viewBiller.findViewById(R.id.showAmountDue);
                    TextView showNextDueDate = viewBiller.findViewById(R.id.showNextDueDate);
                    TextView showDateLastPaid = viewBiller.findViewById(R.id.showDateLastPaid);
                    TextView showRecurring = viewBiller.findViewById(R.id.showRecurring);
                    TextView frequency = viewBiller.findViewById(R.id.showFrequency);
                    TextView showWebsite = viewBiller.findViewById(R.id.showWebsite);
                    TextView showPaymentsRemaining = viewBiller.findViewById(R.id.showPaymentsRemaining);
                    LinearLayout showPaymentsRemainingLayout = viewBiller.findViewById(R.id.showPaymentsRemainingLayout);
                    Button btnVisitWebsite = viewBiller.findViewById(R.id.btnVisitWebsite);
                    Button btnEditBiller = viewBiller.findViewById(R.id.btnEditBiller);
                    Button btnViewPaymentHistory = viewBiller.findViewById(R.id.btnPaymentHistory);
                    Button btnDeleteBiller = viewBiller.findViewById(R.id.btnDeleteBiller);
                    com.google.android.material.imageview.ShapeableImageView iconView = viewBiller.findViewById(R.id.iconView);
                    viewBillerName.setText(bills.getBillerName());
                    linearLayout.addView(viewBiller);
                    final boolean[] showing = {false};
                    viewBiller.setOnClickListener(view1 -> {
                        if (showing[0]) {
                            billerArrow.setRotation(billerArrow.getRotation() - 90);
                            billerDetails.setVisibility(View.GONE);
                            showing[0] = false;
                        } else {
                            billerArrow.setRotation(billerArrow.getRotation() + 90);
                            LoadIcon loadIcon = new LoadIcon();
                            loadIcon.loadIcon(ViewBillers.this, iconView, bills.getCategory(), bills.getIcon());
                            showBillerName.setVisibility(View.GONE);
                            String cat = bills.getCategory();
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
                            showBillerName.setText(bills.getBillerName());
                            showAmountDue.setText(fn.addSymbol(bills.getAmountDue()));
                            showNextDueDate.setText(dueDate);
                            showDateLastPaid.setText(lastPaid);
                            showRecurring.setText(recurring);
                            if (!bills.getPaymentsRemaining().equals("1000")) {
                                showPaymentsRemainingLayout.setVisibility(View.VISIBLE);
                                if (bills.getPaymentsRemaining().equals("0")) {
                                    showPaymentsRemaining.setText(getString(R.string.paid_in_full));
                                }
                                else {
                                    showPaymentsRemaining.setText(bills.getPaymentsRemaining());
                                }
                            }
                            String freq = bills.getFrequency();
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
                            showWebsite.setText(HtmlCompat.fromHtml(bills.getWebsite(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            billerDetails.setVisibility(View.VISIBLE);
                            showing[0] = true;
                            viewBiller.post(() -> scroll.smoothScrollTo(0, viewBiller.getTop()));
                            btnEditBiller.setOnClickListener(view11 -> {
                                pb.setVisibility(View.VISIBLE);
                                Intent edit1 = new Intent(ViewBillers.this, EditBiller.class);
                                edit1.putExtra("userName", bills.getBillerName());
                                edit1.putExtra("website", bills.getWebsite());
                                edit1.putExtra("dueDate", bills.getDayDue());
                                edit1.putExtra("amountDue", bills.getAmountDue());
                                edit1.putExtra("frequency", bills.getFrequency());
                                edit1.putExtra("recurring", bills.isRecurring());
                                edit1.putExtra("Payment Id", 1);
                                edit1.putExtra("Paid", false);
                                startActivity(edit1);
                            });
                            btnVisitWebsite.setOnClickListener(view112 -> {
                                pb.setVisibility(View.VISIBLE);
                                String address = bills.getWebsite();
                                if (!address.startsWith("http://") && !address.startsWith("https://")) {
                                    address = "http://" + address;
                                }
                                Intent launch = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                                startActivity(launch);
                            });
                            btnViewPaymentHistory.setOnClickListener(view113 -> {
                                pb.setVisibility(View.VISIBLE);
                                Intent history = new Intent(mContext, PaymentHistory.class);
                                history.putExtra("User Id", userId);
                                history.putExtra("Bill Id", billId);
                                startActivity(history);
                            });
                            btnDeleteBiller.setOnClickListener(view114 -> {
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                                builder.setMessage(getString(R.string.confirmDeletion)).setTitle(
                                        getString(R.string.deleteBiller)).setPositiveButton(getString(R.string.deleteBiller), (dialogInterface, i) -> {
                                    pb.setVisibility(View.VISIBLE);
                                    listBills.remove(bills);
                                    thisUser.setBills(listBills);
                                    ArrayList <Payments> remove = new ArrayList<>();
                                    for (Payments payment: paymentInfo.getPayments()) {
                                        if (payment.getBillerName().equals(bills.getBillerName())) {
                                            remove.add(payment);
                                        }
                                    }
                                    paymentInfo.getPayments().removeAll(remove);
                                    db.collection("users").document(uid).set(thisUser, SetOptions.merge());
                                    BillerManager bm = new BillerManager();
                                    bm.savePayments();
                                    if (thisUser != null) {
                                        SaveUserData save = new SaveUserData();
                                        save.saveUserData(ViewBillers.this);
                                    }
                                    Toast.makeText(mContext, getString(R.string.billerWasDeletedSuccessfully), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(mContext, ViewBillers.class);
                                    startActivity(intent);
                                }).setNegativeButton(getString(R.string.dontDelete), (dialogInterface, i) -> {

                                });

                                androidx.appcompat.app.AlertDialog alert = builder.create();
                                alert.show();
                            });
                        }
                    });
                }
            }
            TextView spacer = new TextView(ViewBillers.this);
            spacer.setHeight(200);
            linearLayout.addView(spacer);
            linearLayout.animate().translationY(0).setDuration(500);
            header.animate().translationX(0).setDuration(500);
            subHeader.animate().translationX(0).setDuration(500);
        } else {
            text.setVisibility(View.VISIBLE);

            linearLayout.animate().translationY(0).setDuration(500);
            header.animate().translationX(0).setDuration(500);
            subHeader.animate().translationX(0).setDuration(500);
            text.setOnClickListener(view -> {
                Intent addBiller1 = new Intent(mContext, AddBiller.class);
                startActivity(addBiller1);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navDrawer.setVisibility(View.GONE);
        refresh();
        listBills();
        pb.setVisibility(View.GONE);
    }
}