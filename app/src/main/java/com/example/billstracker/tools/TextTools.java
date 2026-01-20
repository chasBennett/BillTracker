package com.example.billstracker.tools;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.example.billstracker.R;
import com.google.android.material.textfield.TextInputEditText;

public class TextTools {

    static final int textSatisfactory = R.drawable.border_stroke_blue;
    static final int textUnsatisfactory = R.drawable.border_stroke;

    public static boolean isAcceptablePassword(EditText editText, TextView length, TextView upper, TextView lower, TextView num) {

        Context context = editText.getContext();

        String password = editText.getText().toString();

        int minimumLength = 5;

        length.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        upper.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        lower.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        num.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        upper.setTextColor(context.getColor(R.color.lightGrey));
        lower.setTextColor(context.getColor(R.color.lightGrey));
        length.setTextColor(context.getColor(R.color.lightGrey));
        num.setTextColor(context.getColor(R.color.lightGrey));

        if (!password.isEmpty()) {

            boolean leng = false;
            boolean capital = false;
            boolean lowercase = false;
            boolean number = false;

            for (int i = 0; i < password.length(); ++i) {
                if (Character.isUpperCase(password.charAt(i))) {
                    capital = true;
                    upper.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    upper.setTextColor(context.getColor(R.color.blackAndWhite));
                }
                if (Character.isLowerCase(password.charAt(i))) {
                    lowercase = true;
                    lower.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    lower.setTextColor(context.getColor(R.color.blackAndWhite));
                }
                if (Character.isDigit(password.charAt(i))) {
                    number = true;
                    num.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                    num.setTextColor(context.getColor(R.color.blackAndWhite));
                }
            }

            if (password.length() >= minimumLength) {
                leng = true;
                length.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkmarksmall, 0, 0, 0);
                length.setTextColor(context.getColor(R.color.blackAndWhite));
            }

            if (!leng || !capital || !lowercase || !number) {
                editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), textUnsatisfactory, context.getTheme()));
                return false;
            } else {
                editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), textSatisfactory, context.getTheme()));
                return true;
            }
        } else {
            editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), textUnsatisfactory, context.getTheme()));
            return false;
        }

    }

    public static void setValidBorder(EditText editText, boolean isValid) {
        if (editText.hasFocus()) {
            if (isValid) {
                editText.setBackground(AppCompatResources.getDrawable(editText.getContext(), R.drawable.border_stroke_blue));
            } else {
                editText.setBackground(AppCompatResources.getDrawable(editText.getContext(), R.drawable.border_error));
            }
        } else {
            editText.setBackground(AppCompatResources.getDrawable(editText.getContext(), R.drawable.border_stroke));
        }
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                editText.setBackground(AppCompatResources.getDrawable(editText.getContext(), R.drawable.border_stroke));
            }
        });
    }

    public static boolean isValidString(EditText editText, int minimumLength) {

        Context context = editText.getContext();
        String string = editText.getText().toString();
        if (!string.isEmpty()) {
            if (string.length() >= minimumLength) {
                setValidBorder(editText, true);
                return true;
            } else {
                editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), textUnsatisfactory, context.getTheme()));
                return false;
            }
        } else {
            editText.setBackground(ResourcesCompat.getDrawable(context.getResources(), textUnsatisfactory, context.getTheme()));
            return false;
        }
    }

    public static void onEnterSelected(EditText editText, InputManager manager) {
        if (editText != null && editText.getContext() != null) {
            Context context = editText.getContext();
            InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            editText.setOnKeyListener((view, i, keyEvent) -> {
                manager.selectedKeyIsEnter(i == KeyEvent.KEYCODE_ENTER);
                if (i == KeyEvent.KEYCODE_ENTER) {
                    mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                return false;
            });
        }
    }

    static void onEnterSelected(TextInputEditText editText, InputManager manager) {
        if (editText != null && editText.getContext() != null) {
            Context context = editText.getContext();
            InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            editText.setOnKeyListener((view, i, keyEvent) -> {
                manager.selectedKeyIsEnter(i == KeyEvent.KEYCODE_ENTER);
                if (i == KeyEvent.KEYCODE_ENTER) {
                    mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                return false;
            });
        }
    }

    public static void closeSoftInput(EditText editText) {
        if (editText != null && editText.getContext() != null) {
            Context context = editText.getContext();
            InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    static void closeSoftInput(TextInputEditText editText) {
        if (editText != null && editText.getContext() != null) {
            Context context = editText.getContext();
            InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void updateText(TextView view, String newText) {
        Typeface typeface = view.getTypeface();
        AlphaAnimation animation = new AlphaAnimation(1f, 0f);
        animation.setDuration(600);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setText(newText);
                view.setTextColor(ResourcesCompat.getColor(view.getContext().getResources(), R.color.payBill, view.getContext().getTheme()));
                if (typeface.isItalic()) {
                    view.setTypeface(null, Typeface.BOLD_ITALIC);
                } else {
                    view.setTypeface(null, Typeface.BOLD);
                }
                Drawable[] drawables = view.getCompoundDrawables();
                if (drawables[0] == null) {
                    view.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.checkmarksmall, view.getContext().getTheme()), drawables[1], drawables[2], drawables[3]);
                } else if (drawables[2] == null) {
                    view.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], ResourcesCompat.getDrawable(view.getContext().getResources(), R.drawable.checkmarksmall, view.getContext().getTheme()), drawables[3]);
                }
                TextViewCompat.setCompoundDrawableTintList(view, ColorStateList.valueOf(ResourcesCompat.getColor(view.getContext().getResources(), R.color.payBill, view.getContext().getTheme())));
                AlphaAnimation animation1 = new AlphaAnimation(0f, 1f);
                animation1.setDuration(600);
                animation1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.postDelayed(() -> {
                            view.setTextColor(ResourcesCompat.getColor(view.getContext().getResources(), R.color.blackAndWhite, view.getContext().getTheme()));
                            TextViewCompat.setCompoundDrawableTintList(view, ColorStateList.valueOf(ResourcesCompat.getColor(view.getContext().getResources(), R.color.blackAndWhite, view.getContext().getTheme())));
                            view.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
                            view.setTypeface(typeface);
                        }, 1200);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(animation1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    public static void changeMoneyTextValue(TextView textView, double newValue, OnCompleteCallback callback) {
        if (textView != null && textView.getContext() != null && textView.getLayout() != null) {
            Layout layout = textView.getLayout();
            Context context = textView.getContext();
            Typeface typeface = textView.getTypeface();
            double startingValue = FixNumber.makeDouble(textView.getText().toString());
            PopupWindow popupWindow = new PopupWindow(context);
            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(null);
            String adderText;
            TextView adder = new TextView(context);
            int color;

            if (newValue >= startingValue) {
                color = ContextCompat.getColor(context, R.color.payBill);
                adderText = "+ " + FixNumber.addSymbol(newValue - startingValue);
            } else {
                color = ContextCompat.getColor(context, R.color.red);
                adderText = "- " + FixNumber.addSymbol(startingValue - newValue);
            }
            adder.setTextColor(color);
            adder.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(color);
            textView.setTypeface(null, Typeface.BOLD);
            adder.setText(adderText);
            popupWindow.setElevation(40);
            popupWindow.setContentView(adder);
            int offset = 0;
            int lineOfText = layout.getLineForOffset(offset);
            int x = (int) layout.getPrimaryHorizontal(offset);
            int y = (int) (layout.getLineTop(lineOfText) - (textView.getHeight() * 2.3));
            popupWindow.showAsDropDown(textView, x, y);
            adder.animate().translationY(150).setDuration(1200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    popupWindow.dismiss();
                    ValueAnimator animator1 = ValueAnimator.ofFloat((float) startingValue, (float) newValue);
                    animator1.setDuration(1500);

                    animator1.addUpdateListener(valueAnimator -> textView.setText(FixNumber.addSymbol(valueAnimator.getAnimatedValue().toString())));
                    animator1.start();
                    animator1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(@NonNull Animator animator) {
                            textView.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.blackAndWhite, context.getTheme()));
                            textView.setTypeface(typeface);
                            callback.isSuccessful(true);
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animator) {
                            callback.isSuccessful(true);
                        }

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animator) {

                        }
                    });
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {
                    popupWindow.dismiss();
                    textView.setText(FixNumber.addSymbol(newValue));
                    callback.isSuccessful(true);
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {

                }
            });
        }
    }

    public interface InputManager {
        void selectedKeyIsEnter(boolean isEnter);
    }

    public interface OnCompleteCallback {
        void isSuccessful(boolean isSuccessful);
    }
}
