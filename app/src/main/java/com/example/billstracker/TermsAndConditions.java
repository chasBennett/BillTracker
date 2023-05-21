package com.example.billstracker;

import static com.example.billstracker.Logon.thisUser;
import static com.example.billstracker.Logon.uid;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class TermsAndConditions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        CheckBox termsCheckBox = findViewById(R.id.termsCheckBox);
        Button agree = findViewById(R.id.btnAgree);
        NestedScrollView scroll = findViewById(R.id.termsScroll);
        TextView instructions = findViewById(R.id.scrollInstruction);
        TextView accepted = findViewById(R.id.accepted);
        TextView tac = findViewById(R.id.textView75);
        String sb = getString(R.string.tac1) + getString(R.string.tac2) + getString(R.string.tac3) + getString(R.string.tac4) + getString(R.string.tac5) + getString(R.string.tac6) +
                getString(R.string.tac7) + getString(R.string.tac8) + getString(R.string.tac9) + getString(R.string.tac10) + getString(R.string.tac11) + getString(R.string.tac12) +
                getString(R.string.tac13) + getString(R.string.tac14) + getString(R.string.tac15) + getString(R.string.tac16) + getString(R.string.tac17) + getString(R.string.tac18) +
                getString(R.string.tac19) + getString(R.string.tac20) + getString(R.string.tac21) + getString(R.string.tac22) + getString(R.string.tac23) +
                getString(R.string.tac24) + getString(R.string.tac25);
        tac.setText(sb);

                DateFormatter df = new DateFormatter();

        termsCheckBox.setEnabled(false);

        agree.setEnabled(false);
        agree.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightblue, getTheme())));

        scroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scroll.getChildAt(0).getBottom() <= (scroll.getHeight() + scroll.getScrollY())) {
                instructions.setVisibility(View.GONE);
                termsCheckBox.setVisibility(View.VISIBLE);
                termsCheckBox.setEnabled(true);
            }
        });

        termsCheckBox.setOnClickListener(view -> {
            if (termsCheckBox.isChecked()) {
                accepted.setVisibility(View.VISIBLE);
                accepted.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.acceptedOn), df.createCurrentDateStringWithTime()));
                agree.setEnabled(true);
                agree.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button, getTheme())));
            }
            else {
                accepted.setVisibility(View.GONE);
                agree.setEnabled(false);
                agree.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightblue, getTheme())));
            }
        });

        agree.setOnClickListener(view -> {
            String date = df.createCurrentDateStringWithTime();
                thisUser.setTermsAccepted(true);
                thisUser.setTermsAcceptedOn(date);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(uid).set(thisUser);
                Intent main = new Intent (TermsAndConditions.this, MainActivity2.class);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
        });
    }
}