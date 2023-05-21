package com.example.billstracker;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class LoadIcon {

    boolean darkMode;

    public void loadIcon (Context context, com.google.android.material.imageview.ShapeableImageView view, String category, String path) {

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        darkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        ArrayList<Integer> icons = new ArrayList<>(Arrays.asList(R.drawable.auto, R.drawable.credit_card, R.drawable.entertainment, R.drawable.insurance,
                R.drawable.invoice, R.drawable.mortgage, R.drawable.personal_loan, R.drawable.utilities));

        File file = new File(path);
        view.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.neutralGray, context.getTheme())));
        if (darkMode) {
            view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme()));
            view.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.tiles, context.getTheme())));
        }
        else {
            view.setBackground(null);
        }

        if (!path.contains("custom") || !file.exists()) {

            view.setContentPadding(30,30,30,30);
            view.setPadding(0,0,0,0);
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Glide.with(context).load(icons.get(Integer.parseInt(category))).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerInside().into(view);
        }
        else {
            view.setImageTintList(null);
            view.setContentPadding(0,0,0,0);
            view.setPadding(40,40,40,40);
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Glide.with(context).load(path).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).circleCrop().into(view);
        }
    }
    public void loadDefault (Context context, int image, com.google.android.material.imageview.ShapeableImageView view) {

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        darkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        view.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.neutralGray, context.getTheme())));
        if (darkMode) {
            view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme()));
            view.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.tiles, context.getTheme())));
        }
        else {
            view.setBackground(null);
        }
        view.setContentPadding(40,40,40,40);
        view.setPadding(0,0,0,0);
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Glide.with(context).load(image).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerInside().into(view);
    }
    public void loadImageFromDatabase (Context context, com.google.android.material.imageview.ShapeableImageView view, String path) {

        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        darkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        view.setImageTintList(null);

        view.setContentPadding(0,0,0,0);
        view.setPadding(40,40,40,40);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(view).load(path).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).circleCrop().into(view);

        if (darkMode) {
            view.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.circle, context.getTheme()));
        }
        else {
            view.setBackground(null);
        }
    }
}
