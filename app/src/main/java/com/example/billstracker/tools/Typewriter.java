package com.example.billstracker.tools;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class Typewriter {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private CharSequence mText;
    private TextView view;
    private int mIndex;
    private long mDelay = 500; //Default 500ms delay

    public void animateText(CharSequence text, TextView view1) {
        mText = text;
        mIndex = 0;
        this.view = view1;

        if (view.getText().length() == 0) {
            mHandler.removeCallbacks(characterAdder);
            mHandler.postDelayed(characterAdder, mDelay);
        }
    }    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if (view != null) {
                view.setText(mText.subSequence(0, mIndex++));
                if (mIndex <= mText.length()) {
                    mHandler.postDelayed(characterAdder, mDelay);
                }
            }
        }
    };

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }


}
