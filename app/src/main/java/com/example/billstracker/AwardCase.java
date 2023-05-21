package com.example.billstracker;

import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.firestore.FirebaseFirestore;

public class AwardCase extends AppCompatActivity {

    LinearLayout back;
    ImageView trophy1;
    ImageView trophy2;
    ImageView trophy3;
    ImageView trophy4;
    ImageView trophy5;
    ImageView trophy6;
    ImageView trophy7;
    ImageView trophy8;
    ImageView trophy9;
    ImageView trophyView;
    TextView title;
    TextView description;
    ImageView closeTrophyView;
    LinearLayout trophyPopup;
    ImageView share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award_case);

        back = findViewById(R.id.backTrophyCase);
        trophy1 = findViewById(R.id.trophy1);
        trophy2 = findViewById(R.id.trophy2);
        trophy3 = findViewById(R.id.trophy3);
        trophy4 = findViewById(R.id.trophy4);
        trophy5 = findViewById(R.id.trophy5);
        trophy6 = findViewById(R.id.trophy6);
        trophy7 = findViewById(R.id.trophy7);
        trophy8 = findViewById(R.id.trophy8);
        trophy9 = findViewById(R.id.trophy9);
        trophyView = findViewById(R.id.trophyView);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        closeTrophyView = findViewById(R.id.closeTrophyView1);
        trophyPopup = findViewById(R.id.trophyPopup);
        share = findViewById(R.id.shareTrophy1);

        closeTrophyView.setOnClickListener(v -> trophyPopup.setVisibility(View.GONE));

        back.setOnClickListener(v -> onBackPressed());

        ShareImage shareImage = new ShareImage();

        for (Trophy trophy: thisUser.getTrophies()) {
            if (trophy.getType() == 1) {
                trophy1.setImageTintList(null);
                trophy1.setOnClickListener(v -> {
                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.welcomeAboard));
                    description.setText(getString(R.string.youAddedYourFirstBillerOnBillTracker));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.trophy));
                    closeTrophyView.setOnClickListener(v1 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v118 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.welcomeAboard) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 2) {
                trophy2.setImageTintList(null);
                trophy2.setOnClickListener(v -> {
                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.thatWasEasy));
                    description.setText(getString(R.string.madeFirstPayment));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.trophy2));
                    closeTrophyView.setOnClickListener(v12 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v117 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.thatWasEasy) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 3) {
                trophy3.setImageTintList(null);
                trophy3.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.bestIconEver));
                    description.setText(getString(R.string.addedFirstCustomBiller));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.trophy3));
                    closeTrophyView.setOnClickListener(v13 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v116 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.bestIconEver) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 4) {
                trophy4.setImageTintList(null);
                trophy4.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.gettingTheHangOfIt));
                    description.setText(getString(R.string.completed10payments));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.trophy4));
                    closeTrophyView.setOnClickListener(v14 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v115 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.gettingTheHangOfIt) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 5) {
                trophy5.setImageTintList(null);
                trophy5.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.journeyman));
                    description.setText(getString(R.string.added10Billers));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.medal));
                    closeTrophyView.setOnClickListener(v15 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v114 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.journeyman) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 6) {
                trophy6.setImageTintList(null);
                trophy6.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.welcomeToBillTracker));
                    description.setText(getString(R.string.firstTimeLoggingIn));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.medal2));
                    closeTrophyView.setOnClickListener(v16 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v113 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.welcomeToBillTracker) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 7) {
                trophy7.setImageTintList(null);
                trophy7.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.gettingThoseRepsIn));
                    description.setText(getString(R.string.thirtyPayments));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.medal3));
                    closeTrophyView.setOnClickListener(v17 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v112 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.gettingThoseRepsIn) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 8) {
                trophy9.setImageTintList(null);
                trophy9.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.earlierThanEarly));
                    description.setText(getString(R.string.you_paid_a_bill_at_least_14_days_early));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.early_bird_removebg_preview));
                    closeTrophyView.setOnClickListener(v18 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v111 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.earlierThanEarly) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
            if (trophy.getType() == 9) {
                trophy8.setImageTintList(null);
                trophy8.setOnClickListener(v -> {

                    trophyPopup.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.unstoppable));
                    description.setText(getString(R.string.youve_made_50_payments));
                    trophyView.setImageDrawable(AppCompatResources.getDrawable(AwardCase.this, R.drawable.medal));
                    closeTrophyView.setOnClickListener(v19 -> trophyPopup.setVisibility(View.GONE));

                    share.setOnClickListener(v110 -> {
                        for (Trophy trophies: thisUser.getTrophies()) {
                            if (trophies.getType() == 1) {
                                trophies.setShared(true);
                            }
                        }
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).set(thisUser);
                        shareImage.shareImage(AwardCase.this, trophyPopup, getString(R.string.ijustearnedthe) + " \"" + getString(R.string.unstoppable) + "\" " + getString(R.string.badgeonbilltracker));
                    });
                });
            }
        }
    }
}