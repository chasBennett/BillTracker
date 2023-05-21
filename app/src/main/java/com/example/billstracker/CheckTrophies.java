package com.example.billstracker;

import static com.example.billstracker.Logon.paymentInfo;
import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class CheckTrophies {

    Context context;
    boolean trophy1 = false;
    boolean trophy2 = false;
    boolean trophy3 = false;
    boolean trophy4 = false;
    boolean trophy5 = false;
    boolean trophy6 = false;
    boolean trophy7 = false;

    boolean trophy8 = false;
    boolean trophy9 = false;
    boolean customIcon = false;

    boolean earlyBird = false;
    int logins = thisUser.getTotalLogins();
    LinearLayout viewParent;
    int billCounter;
    int paymentCounter;

    public void checkTrophies(Context thisContext, LinearLayout parent) {

        context = thisContext;
        viewParent = parent;
        billCounter = 0;
        paymentCounter = 0;
        for (Bill bill : thisUser.getBills()) {
            ++billCounter;
            if (bill.getIcon().contains("custom")) {
                customIcon = true;
            }
        }
            for (Payments payment : paymentInfo.getPayments()) {
                if (payment.isPaid()) {
                    ++paymentCounter;
                }
                if (payment.getDatePaid() <= payment.getPaymentDate() - 14 && payment.getDatePaid() != 0) {
                    earlyBird = true;
                }
            }

        checkEligibility();
    }

    public void checkEligibility() {

        ArrayList<Trophy> trophyList = thisUser.getTrophies();

        if (billCounter >= 1) {
            trophy1 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 1 && trophy.getShown()) {
                    trophy1 = false;
                    break;
                }
            }
        }
        if (paymentCounter >= 1) {
            trophy2 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 2 && trophy.getShown()) {
                    trophy2 = false;
                    break;
                }
            }
        }
        if (customIcon) {
            trophy3 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 3 && trophy.getShown()) {
                    trophy3 = false;
                    break;
                }
            }
        }
        if (paymentCounter >= 10) {
            trophy4 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 4 && trophy.getShown()) {
                    trophy4 = false;
                    break;
                }
            }
        }
        if (billCounter >= 10) {
            trophy5 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 5 && trophy.getShown()) {
                    trophy5 = false;
                    break;
                }
            }
        }
        if (logins >= 1) {
            trophy6 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 6 && trophy.getShown()) {
                    trophy6 = false;
                    break;
                }
            }
        }
        if (paymentCounter >= 30) {
            trophy7 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 7 && trophy.getShown()) {
                    trophy7 = false;
                    break;
                }
            }
        }
        if (earlyBird) {
            trophy8 = true;
            for (Trophy trophy : trophyList) {
                if (trophy.getType() == 8 && trophy.getShown()) {
                    trophy8 = false;
                    break;
                }
            }
        }
        if (paymentCounter >= 50) {
            trophy9 = true;
            for (Trophy trophy: thisUser.getTrophies()) {
                if (trophy.getType() == 9 && trophy.getShown()) {
                    trophy9 = false;
                    break;
                }
            }
        }
        generateTrophies();
    }
    public void generateTrophies() {

        int counter = 0;
        if (trophy1) {
            ++counter;
        }
        if (trophy2) {
            ++counter;
        }
        if (trophy3) {
            ++counter;
        }
        if (trophy4) {
            ++counter;
        }
        if (trophy5) {
            ++counter;
        }
        if (trophy6) {
            ++counter;
        }
        if (trophy7) {
            ++counter;
        }
        if (trophy8) {
            ++counter;
        }
        if (trophy9) {
            ++counter;
        }

        for (int i = 0; i < counter; ++i) {

            if (trophy1) {
                createBadge(1, context.getString(R.string.welcomeAboard));
                trophy1 = false;
            } else if (trophy2) {
                createBadge(2, context.getString(R.string.thatWasEasy));
                trophy2 = false;
            } else if (trophy3) {
                createBadge(3, context.getString(R.string.bestIconEver));
                trophy3 = false;
            } else if (trophy4) {
                createBadge(4, context.getString(R.string.gettingTheHangOfIt));
                trophy4 = false;
            } else if (trophy5) {
                createBadge(5, context.getString(R.string.journeyman));
                trophy5 = false;
            } else if (trophy6) {
                createBadge(6, context.getString(R.string.welcomeToBillTracker));
                trophy6 = false;
            } else if (trophy7) {
                createBadge(7, context.getString(R.string.gettingThoseRepsIn));
                trophy7 = false;
            }
            else if (trophy8) {
                createBadge(8, context.getString(R.string.earlierThanEarly));
                trophy8 = false;
            }
            else if (trophy9) {
                createBadge(9, context.getString(R.string.unstoppable));
                trophy9 = false;
            }
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).set(thisUser);
        SaveUserData save = new SaveUserData();
        save.saveUserData(context);
        trophy1 = false;
        trophy2 = false;
        trophy3 = false;
        trophy4 = false;
        trophy5 = false;
        trophy6 = false;
        trophy7 = false;
        trophy8 = false;
    }

    public void createBadge (int type, String message) {

        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) dp);
        lp.setMargins(30, 30, 30, 30);

        ArrayList <Integer> trophies = new ArrayList<>(Arrays.asList(R.drawable.trophy, R.drawable.trophy2, R.drawable.trophy3, R.drawable.trophy4, R.drawable.medal2,
                R.drawable.medal3, R.drawable.medal4, R.drawable.early_bird_removebg_preview, R.drawable.medal));

        View trophy;
        trophy = View.inflate(context, R.layout.achievement, null);
        TextView message1 = trophy.findViewById(R.id.trophyMessage);
        ImageView close = trophy.findViewById(R.id.closeTrophy);
        ImageView share = trophy.findViewById(R.id.shareTrophy1);
        ImageView trophyImage = trophy.findViewById(R.id.trophyImage);
        trophyImage.setImageDrawable(AppCompatResources.getDrawable(context, trophies.get(type - 1)));
        trophy.setLayoutParams(lp);
        message1.setText(message);
        viewParent.addView(trophy);
        trophy.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(1500);
        thisUser.getTrophies().add(new Trophy(type, true, message, false));
        addListeners(close, share, trophy, type, message);
    }

    public void addListeners(ImageView close, ImageView share, View trophy, int type, String message) {

        close.setOnClickListener(v -> viewParent.removeView(trophy));
        share.setOnClickListener(v -> {

            for (Trophy trophies: thisUser.getTrophies()) {
                if (trophies.getType() == type) {
                    trophies.setShared(true);
                }
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).set(thisUser);
            ShareImage shareImage = new ShareImage();
            shareImage.shareImage(context, trophy, "I Just Earned The \"" + message + "\" Badge On Bill Tracker!");
        });
        trophy.setOnClickListener(v -> {
            Intent award = new Intent(context, AwardCase.class);
            context.startActivity(award);
        });
        trophy.postDelayed(() -> viewParent.removeView(trophy), 30000);
    }
}
