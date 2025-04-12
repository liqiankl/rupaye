package com.beulah.saver.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.beulah.saver.R;
import com.beulah.saver.activity.RupayeApplication;
import com.beulah.saver.model.RecordManager;
import com.beulah.saver.model.SettingManager;
import com.beulah.saver.util.RupayeUtil;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by 伟平 on 2015/10/27.
 */

public class EditMoneyFragment extends Fragment {

    private int fragmentPosition;
    private int tagId = -1;

    public MaterialEditText editView;

    public ImageView tagImage;
    public TextView tagName;

    private View mView;

    Activity activity;

    static public EditMoneyFragment newInstance(int position, int type) {
        EditMoneyFragment fragment = new EditMoneyFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("type", type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.edit_money_fragment, container, false);

        if (getArguments().getInt("type") == RupayeFragmentManager.MAIN_ACTIVITY_FRAGMENT) {
            RupayeFragmentManager.mainActivityEditMoneyFragment = this;
        } else if (getArguments().getInt("type") == RupayeFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT) {
            RupayeFragmentManager.editRecordActivityEditMoneyFragment = this;
        }

        fragmentPosition = getArguments().getInt("position");
        editView = (MaterialEditText)mView.findViewById(R.id.money);
        tagImage = (ImageView)mView.findViewById(R.id.tag_image);
        tagName = (TextView)mView.findViewById(R.id.tag_name);
        tagName.setTypeface(RupayeUtil.typefaceLatoLight);

        editView.setTypeface(RupayeUtil.typefaceLatoHairline);
        editView.setText("0");
        editView.requestFocus();
        editView.setHelperText(" ");
        editView.setKeyListener(null);
        editView.setOnClickListener(null);
        editView.setOnTouchListener(null);

        boolean shouldChange
                = SettingManager.getInstance().getIsMonthLimit()
                && SettingManager.getInstance().getIsColorRemind()
                && RecordManager.getCurrentMonthExpense()
                >= SettingManager.getInstance().getMonthWarning();

        setEditColor(shouldChange);

        if (getArguments().getInt("type") == RupayeFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT
                && RupayeUtil.editRecordPosition != -1) {
            RupayeFragmentManager.editRecordActivityEditMoneyFragment
                    .setTagImage(RupayeUtil.GetTagIcon(
                            (int)RecordManager.SELECTED_RECORDS.get(RupayeUtil.editRecordPosition).getTag()));
            RupayeFragmentManager.editRecordActivityEditMoneyFragment
                    .setTagName(RupayeUtil.GetTagName(
                            (int)RecordManager.SELECTED_RECORDS.get(RupayeUtil.editRecordPosition).getTag()));
            RupayeFragmentManager.editRecordActivityEditMoneyFragment
                    .setTagId(RecordManager.SELECTED_RECORDS.get(RupayeUtil.editRecordPosition).getTag());
            RupayeFragmentManager.editRecordActivityEditMoneyFragment
                    .setNumberText(String.format("%.0f", RecordManager.SELECTED_RECORDS.get(RupayeUtil.editRecordPosition).getMoney()));
        }

        return mView;
    }

    public interface OnTagItemSelectedListener {
        void onTagItemPicked(int position);
    }

    public void updateTags() {

    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public void setTag(int p) {
        tagId = RecordManager.TAGS.get(p).getId();
        tagName.setText(RupayeUtil.GetTagName(RecordManager.TAGS.get(p).getId()));
        tagImage.setImageResource(RupayeUtil.GetTagIcon(RecordManager.TAGS.get(p).getId()));
    }

    public String getNumberText() {
        return editView.getText().toString();
    }

    public void setNumberText(String string) {
        editView.setText(string);
    }

    public String getHelpText() {
        return editView.getHelperText();
    }

    public void setHelpText(String string) {
        editView.setHelperText(string);
    }

    public void editRequestFocus() {
        editView.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                RupayeApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    public void setEditColor(boolean shouldChange) {
        if (shouldChange) {
            editView.setTextColor(SettingManager.getInstance().getRemindColor());
            editView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            editView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
        } else {
            editView.setTextColor(RupayeUtil.getInstance().MY_BLUE);
            editView.setPrimaryColor(RupayeUtil.getInstance().MY_BLUE);
            editView.setHelperTextColor(RupayeUtil.getInstance().MY_BLUE);
        }
    }

    public void setTagName(String name) {
        tagName.setText(name);
    }

    public void setTagImage(int resource) {
        tagImage.setImageResource(resource);
    }

    public void getTagPosition(int[] position) {
        tagImage.getLocationOnScreen(position);
        position[0] += tagImage.getWidth() / 2;
        position[1] += tagImage.getHeight() / 2;
    }

}
