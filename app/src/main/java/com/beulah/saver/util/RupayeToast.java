package com.beulah.saver.util;

import android.graphics.Color;

import com.github.johnpersano.supertoasts.SuperToast;
import com.beulah.saver.activity.RupayeApplication;

/**
 * Created by Weiping on 2015/11/30.
 */
public class RupayeToast {
    private static RupayeToast ourInstance = new RupayeToast();

    public static RupayeToast getInstance() {
        return ourInstance;
    }

    private RupayeToast() {
    }

    public void showToast(int text, int color) {
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(RupayeApplication.getAppContext());
        superToast.setAnimations(RupayeUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(RupayeApplication.getAppContext().getResources().getString(text));
        superToast.setBackground(color);
        superToast.getTextView().setTypeface(RupayeUtil.typefaceLatoLight);
        superToast.show();
    }

    public void showToast(String text, int color) {
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(RupayeApplication.getAppContext());
        superToast.setAnimations(RupayeUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(color);
        superToast.getTextView().setTypeface(RupayeUtil.typefaceLatoLight);
        superToast.show();
    }
}
