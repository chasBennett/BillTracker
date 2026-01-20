package com.example.billstracker.popup_classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.billstracker.R;
import com.example.billstracker.custom_objects.Partner;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

@SuppressLint("RestrictedApi")
public class Notify {

    public static View.OnClickListener listener;

    public static void setListener(View.OnClickListener listener) {
        Notify.listener = listener;
    }

    public static void createPopup(Activity activity, String message, View anchor) {

        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG);
        View customSnackView = View.inflate(activity, R.layout.error_view, null);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        TextView errorMessage = customSnackView.findViewById(R.id.error_message);
        errorMessage.setText(message);
        snackbarLayout.addView(customSnackView, 0);
        if (anchor != null) {
            snackbar.setAnchorView(anchor);
            snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        } else {
            snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        }
        snackbar.show();
    }

    public static void createPartnerRequestNotification(Activity activity, Partner partner, View anchor, Intent intent) {

        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG);
        View customSnackView = View.inflate(activity, R.layout.error_view, null);
        customSnackView.setClickable(true);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        TextView errorMessage = customSnackView.findViewById(R.id.error_message);
        snackbar.setDuration(5000);
        errorMessage.setText(String.format("%s%s%s", activity.getString(R.string.you_have_a_sharing_request_from), partner.getPartnerName(), activity.getString(R.string.click_here_to_view_it)));
        snackbarLayout.addView(customSnackView, 0);
        if (anchor != null) {
            snackbar.setAnchorView(anchor);
        }
        snackbar.show();
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        customSnackView.setOnClickListener(v -> activity.startActivity(intent));
    }

    public static void createDialogPopup(Dialog dialog, String message, View anchor) {

        final Snackbar snackbar = Snackbar.make(dialog.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG);
        View customSnackView = View.inflate(dialog.getContext(), R.layout.error_view, null);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        TextView errorMessage = customSnackView.findViewById(R.id.error_message);
        errorMessage.setText(message);
        snackbarLayout.addView(customSnackView, 0);
        if (anchor != null) {
            snackbar.setAnchorView(anchor);
        }
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.show();
    }

    public static Button createButtonPopup(Activity activity, String message, String buttonText, View anchor) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG);
        View customSnackView = View.inflate(activity, R.layout.snackbar_with_button, null);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        TextView errorMessage = customSnackView.findViewById(R.id.error_message);
        errorMessage.setText(message);
        Button snackButton = customSnackView.findViewById(R.id.snackButton);
        snackButton.setText(buttonText);
        snackbarLayout.addView(customSnackView, 0);
        if (anchor != null) {
            snackbar.setAnchorView(anchor);
        }
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.show();
        return snackButton;
    }
}
