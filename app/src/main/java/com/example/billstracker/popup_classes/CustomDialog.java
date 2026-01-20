package com.example.billstracker.popup_classes;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.view.WindowCompat;

import com.example.billstracker.R;
import com.example.billstracker.tools.TextTools;
import com.example.billstracker.tools.Tools;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class CustomDialog extends Dialog {

    public static View.OnClickListener positiveButtonListener;
    public static View.OnClickListener neutralButtonListener;
    public static View.OnClickListener negativeButtonListener;
    public static View.OnClickListener perimeterListener;
    ViewGroup main;
    View dialog;
    LinearLayout parent;
    TextView dialogTitle;
    TextView dialogMessage;
    TextView positiveButton;
    TextView negativeButton;
    TextView neutralButton;
    TextInputEditText editText;
    TextInputLayout editTextParent;
    ScrollView scroll;
    LinearLayout spinnerParent;
    ImageView spinnerIcon;
    TextView spinnerHint;
    Spinner spinner;
    public CustomDialog(Activity activity, String title, String message, String positiveButtonText, String negativeButtonText, String neutralButtonText) {
        super(activity);

        setViews(activity);
        dialogTitle.setText(Objects.requireNonNullElse(title, ""));
        dialogMessage.setText(Objects.requireNonNullElse(message, ""));
        positiveButton.setText(Objects.requireNonNullElse(positiveButtonText, activity.getString(R.string.yes)));
        negativeButton.setText(Objects.requireNonNullElse(negativeButtonText, activity.getString(R.string.cancel)));
        if (neutralButton != null) {
            if (neutralButtonText != null) {
                neutralButton.setText(neutralButtonText);
                neutralButton.setVisibility(View.VISIBLE);
            } else {
                neutralButton.setVisibility(View.GONE);
            }
        }
        positiveButton.setOnClickListener(v -> {
            if (editText != null) {
                TextTools.closeSoftInput(editText);
            }
            positiveButtonListener.onClick(v);
        });
        neutralButton.setOnClickListener(v -> {
            if (editText != null) {
                TextTools.closeSoftInput(editText);
            }
            neutralButtonListener.onClick(v);
        });
        if (negativeButton != null) {
            if (negativeButtonText != null) {
                negativeButton.setVisibility(View.VISIBLE);
                negativeButton.setOnClickListener(v -> {
                    if (editText != null) {
                        TextTools.closeSoftInput(editText);
                    }
                    main.removeView(dialog);
                    if (negativeButtonListener != null) {
                        negativeButtonListener.onClick(v);
                    }
                });
            } else {
                negativeButton.setVisibility(View.GONE);
            }
        }
        parent.setOnClickListener(v -> {
            dismissDialog();
            if (perimeterListener != null) {
                perimeterListener.onClick(v);
            }
        });
        main.addView(dialog);
        dialog.setElevation(10);
        scroll.animate().translationY(0).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        main.bringChildToFront(dialog);
        dialog.bringToFront();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

    }

    public void setPositiveButtonListener(View.OnClickListener listener) {
        CustomDialog.positiveButtonListener = listener;
    }

    public void setNeutralButtonListener(View.OnClickListener listener) {
        CustomDialog.neutralButtonListener = listener;
    }

    public void setNegativeButtonListener(View.OnClickListener listener) {
        CustomDialog.negativeButtonListener = listener;
    }

    public void setPerimeterListener(View.OnClickListener listener) {
        CustomDialog.perimeterListener = listener;
    }

    public void isMoneyInput(boolean isMoney) {
        if (isMoney && editText != null) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    public Dialog getDialog() {
        return this;
    }

    public void setEditText(String hint, String defaultValue, Drawable startIconDrawable) {

        editTextParent = dialog.findViewById(R.id.dialogEditTextParent);
        editText = dialog.findViewById(R.id.dialogEditText);
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

    public void setViews(Activity activity) {
        main = activity.findViewById(android.R.id.content);
        dialog = View.inflate(activity, R.layout.custom_dialog, null);
        scroll = dialog.findViewById(R.id.dialogScroll);
        parent = dialog.findViewById(R.id.dialog_parent);
        dialogTitle = dialog.findViewById(R.id.dialogTitle);
        dialogMessage = dialog.findViewById(R.id.dialogMessage);
        positiveButton = dialog.findViewById(R.id.positiveButton);
        negativeButton = dialog.findViewById(R.id.negativeButton);
        neutralButton = dialog.findViewById(R.id.neutralButton);
        dialog.bringToFront();
        Tools.setupUI(activity, dialog);
    }

    public void setTextWatcher(TextWatcher watcher) {
        if (editText != null) {
            String string = "";
            if (editText.getText() != null && !editText.getText().toString().isEmpty()) {
                string = editText.getText().toString();
            }
            editText.addTextChangedListener(watcher);
            editText.setText(string);
        }
    }

    public String getInput() {
        if (editText.getText() != null) {
            return editText.getText().toString();
        } else {
            return "";
        }
    }

    public EditText getEditText() {
        if (editText != null) {
            return editText;
        } else {
            return null;
        }
    }

    public int getSpinnerSelection() {
        if (spinner != null) {
            return spinner.getSelectedItemPosition();
        }
        return 0;
    }

    public void setSpinner(ArrayAdapter<String> spinnerAdapter, String hint, int defaultSelection, Drawable icon) {
        if (dialog != null) {
            spinnerParent = dialog.findViewById(R.id.dialogSpinnerParent);
            spinnerParent.setVisibility(View.VISIBLE);
            spinnerIcon = dialog.findViewById(R.id.dialogSpinnerIcon);
            spinnerHint = dialog.findViewById(R.id.dialogSpinnerHint);
            spinner = dialog.findViewById(R.id.dialogSpinner);
            spinner.setAdapter(spinnerAdapter);
            if (hint != null) {
                spinnerHint.setText(hint);
            }
            spinner.setSelection(defaultSelection);
            if (icon != null) {
                spinnerIcon.setImageDrawable(icon);
            } else {
                spinnerIcon.setVisibility(View.GONE);
            }
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            ViewGroup parent = (ViewGroup) dialog.getParent();
            if (editText != null) {
                TextTools.closeSoftInput(editText);
            }
            if (parent != null) {
                parent.removeView(dialog);
            }
        }
    }
}
