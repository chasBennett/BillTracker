package com.example.billstracker.popup_classes;

import static com.example.billstracker.activities.MainActivity2.skipInstruction1;
import static com.example.billstracker.activities.MainActivity2.skipInstruction2;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.billstracker.R;
import com.example.billstracker.activities.AddBiller;
import com.example.billstracker.activities.CreateBudget;
import com.example.billstracker.tools.Prefs;
import com.example.billstracker.tools.Repository;
import com.example.billstracker.tools.Tools;

public class InstructionPopup {

    final Activity activity;
    ViewGroup main;
    View dialog;
    LinearLayout addBiller, addBudget, instructions;
    TextView skip, skip2, dontAsk, dontAsk2;
    Button addBill, addBud, getStarted;
    View parent;

    public InstructionPopup(Activity activity) {
        this.activity = activity;
        setViews();

        if (skipInstruction1 && skipInstruction2) {
            dismissDialog();
        } else {

            if (Repository.getInstance().getUser(activity) != null) {
                if (Repository.getInstance().getBills().isEmpty() && !skipInstruction1) {
                    setView(1);
                } else if (Repository.getInstance().getUser(activity).getBudgets().isEmpty() && !skipInstruction2) {
                    setView(2);
                } else if (!skipInstruction2) {
                    setView(3);
                }
            }
            skip.setOnClickListener(view -> {
                setView(2);
                skipInstruction1 = true;
            });
            skip2.setOnClickListener(view -> {
                dismissDialog();
                skipInstruction2 = true;
            });
            dontAsk.setOnClickListener(view -> {
                Prefs.setTrainingDone(activity, true);
                dismissDialog();
            });
            dontAsk2.setOnClickListener(view -> {
                Prefs.setTrainingDone(activity, true);
                dismissDialog();
            });
            addBill.setOnClickListener(view -> {
                activity.startActivity(new Intent(activity, AddBiller.class));
                dismissDialog();
            });
            addBud.setOnClickListener(view -> {
                activity.startActivity(new Intent(activity, CreateBudget.class));
                dismissDialog();
            });
            getStarted.setOnClickListener(view -> {
                if (!Repository.getInstance().getBills().isEmpty() && !Repository.getInstance().getUser(activity).getBudgets().isEmpty()) {
                    Prefs.setTrainingDone(activity, true);
                }
                dismissDialog();
                skipInstruction1 = true;
                skipInstruction2 = true;
            });

            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            parent.setOnClickListener(v -> dismissDialog());

            main.addView(dialog);
            dialog.setElevation(10);
            main.bringChildToFront(dialog);
        }
    }

    public void setView(int choice) {

        addBiller.setVisibility(View.GONE);
        addBudget.setVisibility(View.GONE);
        instructions.setVisibility(View.GONE);
        switch (choice) {
            case 1:
                addBiller.setVisibility(View.VISIBLE);
                break;
            case 2:
                addBudget.setVisibility(View.VISIBLE);
                break;
            case 3:
                instructions.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setViews() {

        if (dialog == null) {
            main = activity.findViewById(android.R.id.content);
            dialog = View.inflate(activity, R.layout.app_introduction, null);
            parent = dialog.findViewById(R.id.parentView);
            addBiller = dialog.findViewById(R.id.addFirstBiller);
            addBudget = dialog.findViewById(R.id.addFirstBudget);
            instructions = dialog.findViewById(R.id.instructions);
            skip = dialog.findViewById(R.id.skip);
            skip2 = dialog.findViewById(R.id.skip2);
            dontAsk = dialog.findViewById(R.id.dontAsk);
            dontAsk2 = dialog.findViewById(R.id.dontAsk2);
            addBill = dialog.findViewById(R.id.btnAddBiller2);
            addBud = dialog.findViewById(R.id.btnCreateBudget);
            getStarted = dialog.findViewById(R.id.btnGetStarted);
            Tools.setupUI(activity, dialog);
        }

    }

    public void dismissDialog() {
        if (main != null && dialog != null) {
            main.removeView(dialog);
        }
    }
}
