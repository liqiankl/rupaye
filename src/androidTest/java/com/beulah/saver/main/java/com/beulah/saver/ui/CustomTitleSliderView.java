package com.beulah.saver.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.beulah.saver.R;
import com.beulah.saver.activity.RupayeApplication;
import com.beulah.saver.fragment.RupayeFragmentManager;
import com.beulah.saver.util.RupayeUtil;

/**
 * Created by Weiping on 2016/1/23.
 */
public class CustomTitleSliderView extends BaseSliderView {
    private static Typeface font = null;
    private String content;
    private int type;
    private TextView title;

    public CustomTitleSliderView(String content, int type) {
        super(RupayeApplication.getAppContext());
        this.content = content;
        this.type = type;
        if (type == RupayeFragmentManager.NUMBER_SLIDER) {
            RupayeFragmentManager.numberCustomTitleSliderView = this;
        } else if (type == RupayeFragmentManager.EXPENSE_SLIDER) {
            RupayeFragmentManager.expenseCustomTitleSliderView = this;
        }
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.custom_title_slider_view,null);

        LinearLayout description = (LinearLayout)v.findViewById(R.id.description_layout);
        description.setVisibility(View.GONE);

        title = (TextView)v.findViewById(R.id.title);
        title.setText(content);
        title.setTypeface(RupayeUtil.typefaceLatoLight);

        ImageView target = (ImageView)v.findViewById(R.id.daimajia_slider_image);
        bindEventAndShow(v, target);
        return v;
    }

    public void setTitle(String string) {
        title.setText(string);
    }
}
