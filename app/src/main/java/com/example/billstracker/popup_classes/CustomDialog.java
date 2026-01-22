package com.example.billstracker.popup_classes;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.billstracker.R;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class CustomDialog extends Dialog {

    /* =======================
       Core views
       ======================= */
    private ScrollView scroll;
    private LinearLayout parent;
    private TextView dialogTitle;
    private TextView dialogMessage;
    private TextView positiveButton;
    private TextView negativeButton;
    private TextView neutralButton;

    @Nullable
    TextInputEditText editText;
    TextInputLayout editTextParent;

    /* PIN UI (optional) */
    @Nullable
    private LinearLayout keypadLayout;

    @Nullable
    private TextView pinDisplay; // optional masked display

    /* =======================
       Optional listeners
       ======================= */
    @Nullable private View.OnClickListener positiveButtonListener;
    @Nullable private View.OnClickListener neutralButtonListener;
    @Nullable private View.OnClickListener negativeButtonListener;
    @Nullable private View.OnClickListener perimeterListener;

    public CustomDialog(
            @NonNull Activity activity,
            @Nullable String title,
            @Nullable String message,
            @Nullable String positiveText,
            @Nullable String negativeText,
            @Nullable String neutralText
    ) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        setCancelable(true);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        bindViews(activity);

        dialogTitle.setText(Objects.requireNonNullElse(title, ""));
        dialogMessage.setText(Objects.requireNonNullElse(message, ""));
        positiveButton.setText(
                Objects.requireNonNullElse(positiveText, activity.getString(R.string.yes))
        );

        if (negativeText != null) {
            negativeButton.setText(negativeText);
            negativeButton.setVisibility(View.VISIBLE);
        } else negativeButton.setVisibility(View.GONE);

        if (neutralText != null) {
            neutralButton.setText(neutralText);
            neutralButton.setVisibility(View.VISIBLE);
        } else neutralButton.setVisibility(View.GONE);

        positiveButton.setOnClickListener(v -> {
            closeKeyboard();
            if (positiveButtonListener != null) positiveButtonListener.onClick(v);
        });

        negativeButton.setOnClickListener(v -> {
            closeKeyboard();
            dismiss();
            if (negativeButtonListener != null) negativeButtonListener.onClick(v);
        });

        neutralButton.setOnClickListener(v -> {
            closeKeyboard();
            if (neutralButtonListener != null) neutralButtonListener.onClick(v);
        });

        parent.setOnClickListener(v -> {
            dismiss();
            if (perimeterListener != null) perimeterListener.onClick(v);
        });

        scroll.animate()
                .translationY(0)
                .setDuration(300)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .start();

        Tools.setupUI(activity, parent);

        /* =======================
           Inject keypad container
           ======================= */
        if (scroll != null && scroll.getChildCount() > 0) {
            LinearLayout contentLayout = (LinearLayout) scroll.getChildAt(0);

            keypadLayout = new LinearLayout(activity);
            keypadLayout.setOrientation(LinearLayout.VERTICAL);
            keypadLayout.setVisibility(View.GONE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 20, 0, 20);
            keypadLayout.setLayoutParams(lp);

            contentLayout.addView(keypadLayout, 2);
        }

        pinDisplay = findViewById(R.id.pinDisplay); // optional
    }

    private void bindViews(Activity activity) {
        scroll = findViewById(R.id.dialogScroll);
        parent = findViewById(R.id.dialog_parent);
        dialogTitle = findViewById(R.id.dialogTitle);
        dialogMessage = findViewById(R.id.dialogMessage);
        positiveButton = findViewById(R.id.positiveButton);
        negativeButton = findViewById(R.id.negativeButton);
        neutralButton = findViewById(R.id.neutralButton);
        editText = findViewById(R.id.dialogEditText);
    }

    private void closeKeyboard() {
        if (editText != null) TextTools.closeSoftInput(editText);
    }

    /* =======================
       Public API (UNCHANGED)
       ======================= */

    public void setPositiveButtonListener(@Nullable View.OnClickListener listener) {
        this.positiveButtonListener = listener;
    }

    public void setNeutralButtonListener(@Nullable View.OnClickListener listener) {
        this.neutralButtonListener = listener;
    }

    public void setNegativeButtonListener(@Nullable View.OnClickListener listener) {
        this.negativeButtonListener = listener;
    }

    public void setPerimeterListener(@Nullable View.OnClickListener listener) {
        this.perimeterListener = listener;
    }

    public void setEditText(String hint, String defaultValue, Drawable startIconDrawable) {

        if (editText != null) {
        editTextParent = findViewById(R.id.dialogEditTextParent);
        editTextParent.setVisibility(View.VISIBLE);
        if (startIconDrawable != null) {
            editTextParent.setStartIconDrawable(startIconDrawable);
        } else {
            editText.setCompoundDrawables(null, null, null, null);
        }
        if (hint != null) {
            editTextParent.setHint(hint);
        }
        if (defaultValue != null) {
            editText.setText(defaultValue);
        }
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }

    public void isMoneyInput(boolean isMoney) {
        if (isMoney && editText != null)
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public void setTextWatcher(@Nullable android.text.TextWatcher watcher) {
        if (editText != null && watcher != null)
            editText.addTextChangedListener(watcher);
    }

    @NonNull
    public String getInput() {
        return editText != null && editText.getText() != null
                ? editText.getText().toString()
                : "";
    }

    @Nullable
    public EditText getEditText() {
        return editText;
    }

    public void setSpinner(
            @NonNull android.widget.ArrayAdapter<String> adapter,
            @Nullable String hint,
            int defaultSelection,
            @Nullable Drawable icon
    ) {
        LinearLayout spinnerParent = findViewById(R.id.dialogSpinnerParent);
        android.widget.Spinner spinner = findViewById(R.id.dialogSpinner);

        if (spinnerParent != null) spinnerParent.setVisibility(View.VISIBLE);
        if (spinner != null) {
            spinner.setAdapter(adapter);
            spinner.setSelection(defaultSelection);
        }
    }

    public int getSpinnerSelection() {
        android.widget.Spinner spinner = findViewById(R.id.dialogSpinner);
        return spinner != null ? spinner.getSelectedItemPosition() : 0;
    }

    @NonNull
    public Dialog getDialog() {
        return this;
    }

    public void dismissDialog() {
        dismiss();
        closeKeyboard();
    }

    /* =======================
   PIN keypad support
   ======================= */
    public void enablePinKeypad() {
        if (editText == null || keypadLayout == null) return;

        // Hide EditText
        editText.setVisibility(View.GONE);

        if (dialogTitle != null) {
            dialogTitle.setVisibility(View.GONE);
        }

        // Show PIN display
        TextView pinDisplay = findViewById(R.id.pinDisplay);
        if (pinDisplay != null) pinDisplay.setVisibility(View.VISIBLE);

        // Show keypad
        keypadLayout.setVisibility(View.VISIBLE);
        keypadLayout.removeAllViews();

        final StringBuilder pinBuilder = new StringBuilder();

        int[][] buttonValues = {
                {1,2,3},
                {4,5,6},
                {7,8,9},
                {-1,0,-2} // -1 empty, -2 delete
        };

        for (int[] rowValues : buttonValues) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setGravity(Gravity.CENTER);

            for (int val : rowValues) {
                Button b = new Button(getContext());
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, 200, 1f); // height fixed
                btnParams.setMargins(5,5,5,5);
                b.setLayoutParams(btnParams);
                b.setTextSize(24);
                b.setTextColor(getContext().getColor(R.color.primary));
                //b.setBackgroundColor(getContext().getColor(android.R.color.transparent));
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                b.setBackgroundResource(outValue.resourceId);


                if (val == -1) b.setVisibility(View.INVISIBLE);
                else if (val == -2) b.setText("⌫");
                else b.setText(String.valueOf(val));

                b.setOnClickListener(view -> {
                    //String current = pinBuilder.toString();
                    if (val >= 0 && pinBuilder.length() < 6) pinBuilder.append(val);
                    else if (val == -2 && pinBuilder.length() > 0) pinBuilder.deleteCharAt(pinBuilder.length()-1);

                    StringBuilder bullets = new StringBuilder();
                    for (int i = 0; i < pinBuilder.length(); i++) bullets.append("●");

                    if (pinDisplay != null) pinDisplay.setText(bullets.toString());
                    editText.setText(pinBuilder.toString());
                });

                row.addView(b);
            }
            keypadLayout.addView(row);
        }
    }

}
