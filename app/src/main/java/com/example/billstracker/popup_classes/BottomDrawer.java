package com.example.billstracker.popup_classes;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import com.example.billstracker.R;

public class BottomDrawer {

    public static View.OnClickListener defaultButtonListener;
    public static View.OnClickListener selectImageButtonListener;
    public void setDefaultButtonListener (View.OnClickListener listener) {
        BottomDrawer.defaultButtonListener = listener;
    }
    public void setSelectImageButtonListener (View.OnClickListener listener) {
        BottomDrawer.selectImageButtonListener = listener;
    }

    final ViewGroup viewGroup;
    final View dialog;
    final LinearLayout parent;
    final LinearLayout drawer;
    final LinearLayout defaultButton;
    final LinearLayout selectImageButton;
    final LinearLayout closeDrawer;
    final long drawerSlideDuration = 400;

    public BottomDrawer (Activity activity) {

        viewGroup = activity.findViewById(android.R.id.content);
        dialog = View.inflate(activity, R.layout.bottom_drawer, null);
        parent = dialog.findViewById(R.id.dismissDrawer);
        closeDrawer = dialog.findViewById(R.id.closeDrawer);
        defaultButton = dialog.findViewById(R.id.use_default);
        selectImageButton = dialog.findViewById(R.id.select_image);
        drawer = dialog.findViewById(R.id.drawer);
        dialog.bringToFront();

        closeDrawer.setOnClickListener(v -> dismissDialog());
        parent.setOnClickListener(v -> dismissDialog());
        defaultButton.setOnClickListener(v -> defaultButtonListener.onClick(v));
        selectImageButton.setOnClickListener(v -> selectImageButtonListener.onClick(v));

        viewGroup.addView(dialog);
        viewGroup.bringChildToFront(dialog);
        dialog.setElevation(400);
        drawer.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(drawerSlideDuration).start();
    }
    public void dismissDialog () {
        if (dialog != null) {
            ViewGroup parent = (ViewGroup) dialog.getParent();
            if (parent != null && drawer != null) {
                drawer.animate().translationY(2000).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(drawerSlideDuration).start();
                parent.postDelayed(() -> parent.removeView(dialog), drawerSlideDuration);
            }
        }
    }
}

