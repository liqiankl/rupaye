package com.beulah.saver.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.beulah.saver.R;
import com.beulah.saver.adapter.ButtonGridViewAdapter;
import com.beulah.saver.adapter.EditMoneyRemarkFragmentAdapter;
import com.beulah.saver.adapter.TagChooseFragmentAdapter;
import com.beulah.saver.fragment.RupayeFragmentManager;
import com.beulah.saver.fragment.TagChooseFragment;
import com.beulah.saver.model.RupayeRecord;
import com.beulah.saver.model.RecordManager;
import com.beulah.saver.ui.RupayeScrollableViewPager;
import com.beulah.saver.ui.RupayeUnscrollableViewPager;
import com.beulah.saver.ui.MyGridView;
import com.beulah.saver.util.RupayeUtil;

import net.steamcrafted.materialiconlib.MaterialIconView;

public class EditRecordActivity extends AppCompatActivity
        implements TagChooseFragment.OnTagItemSelectedListener {

    private Context mContext;
    private boolean IS_CHANGED = false;
    private boolean FIRST_EDIT = true;
    private int position = -1;

    private ViewPager tagViewPager;
    private TagChooseFragmentAdapter tagAdapter;

    private RupayeScrollableViewPager editViewPager;
    private EditMoneyRemarkFragmentAdapter editAdapter;

    private MyGridView myGridView;
    private ButtonGridViewAdapter myGridViewAdapter;

    private final int NO_TAG_TOAST = 0;
    private final int NO_MONEY_TOAST = 1;
    private final int SAVE_SUCCESSFULLY_TOAST = 4;
    private final int SAVE_FAILED_TOAST = 5;

    private SuperToast superToast;

    private MaterialIconView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_record);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        mContext = this;

        superToast = new SuperToast(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            position = extras.getInt("POSITION");
            RupayeUtil.editRecordPosition = RecordManager.SELECTED_RECORDS.size() - 1 - position;
        } else {
            RupayeUtil.editRecordPosition = -1;
        }


        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(mContext, R.color.statusBarColor));
        } else{
            // do something for phones running an SDK before lollipop
        }

// edit viewpager///////////////////////////////////////////////////////////////////////////////////
        editViewPager = (RupayeScrollableViewPager)findViewById(R.id.edit_pager);
        editViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);

        editAdapter = new EditMoneyRemarkFragmentAdapter(
                getSupportFragmentManager(), RupayeFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT);

        editViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    RupayeFragmentManager.editRecordActivityEditRemarkFragment.editRequestFocus();
                } else {
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment.editRequestFocus();
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        editViewPager.setAdapter(editAdapter);
        
// tag viewpager////////////////////////////////////////////////////////////////////////////////////
        tagViewPager = (ViewPager)findViewById(R.id.viewpager);
        tagViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (RecordManager.TAGS.size() % 8 == 0)
            tagAdapter = new TagChooseFragmentAdapter(getSupportFragmentManager(), RecordManager.TAGS.size() / 8);
        else
            tagAdapter = new TagChooseFragmentAdapter(getSupportFragmentManager(), RecordManager.TAGS.size() / 8 + 1);

        tagViewPager.setAdapter(tagAdapter);
        
        myGridView = (MyGridView)findViewById(R.id.gridview);
        myGridViewAdapter = new ButtonGridViewAdapter(this);
        myGridView.setAdapter(myGridViewAdapter);

        myGridView.setOnItemClickListener(gridViewClickListener);
        myGridView.setOnItemLongClickListener(gridViewLongClickListener);

        myGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        myGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        View lastChild = myGridView.getChildAt(myGridView.getChildCount() - 1);
                        myGridView.setLayoutParams(
                                new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.FILL_PARENT, lastChild.getBottom()));
                    }
                });

        back = (MaterialIconView)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("IS_CHANGED", IS_CHANGED);
        intent.putExtra("POSITION", position);
        setResult(RESULT_OK, intent);

        RupayeUtil.editRecordPosition = -1;

        super.finish();
    }

    private AdapterView.OnItemClickListener gridViewClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            buttonClickOperation(false, position);
        }
    };

    private AdapterView.OnItemLongClickListener gridViewLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            buttonClickOperation(true, position);
            return true;
        }
    };

    private void buttonClickOperation(boolean longClick, int position) {
        if (IS_CHANGED) {
            return;
        }
        if (RupayeFragmentManager.editRecordActivityEditMoneyFragment.getNumberText().toString().equals("0")
                && !RupayeUtil.ClickButtonCommit(position)) {
            if (RupayeUtil.ClickButtonDelete(position)
                    || RupayeUtil.ClickButtonIsZero(position)) {

            } else {
                RupayeFragmentManager.editRecordActivityEditMoneyFragment.setNumberText(RupayeUtil.BUTTONS[position]);
            }
        } else {
            if (RupayeUtil.ClickButtonDelete(position)) {
                if (longClick) {
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment.setNumberText("0");
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment.setHelpText(" ");
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment.setHelpText(
                            RupayeUtil.FLOATINGLABELS[RupayeFragmentManager.editRecordActivityEditMoneyFragment
                                    .getNumberText().toString().length()]);
                } else {
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment.setNumberText(
                            RupayeFragmentManager.editRecordActivityEditMoneyFragment.getNumberText().toString()
                            .substring(0, RupayeFragmentManager.editRecordActivityEditMoneyFragment
                                    .getNumberText().toString().length() - 1));
                    if (RupayeFragmentManager.editRecordActivityEditMoneyFragment.getNumberText().toString().length() == 0) {
                        RupayeFragmentManager.editRecordActivityEditMoneyFragment.setNumberText("0");
                        RupayeFragmentManager.editRecordActivityEditMoneyFragment.setHelpText(" ");
                    }
                }
            } else if (RupayeUtil.ClickButtonCommit(position)) {
                commit();
            } else {
                if (FIRST_EDIT) {
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment.setNumberText(RupayeUtil.BUTTONS[position]);
                    FIRST_EDIT = false;
                } else {
                    RupayeFragmentManager.editRecordActivityEditMoneyFragment
                            .setNumberText(RupayeFragmentManager.editRecordActivityEditMoneyFragment
                                    .getNumberText().toString() + RupayeUtil.BUTTONS[position]);
                }
            }
        }
        RupayeFragmentManager.editRecordActivityEditMoneyFragment.setHelpText(RupayeUtil.FLOATINGLABELS[
                RupayeFragmentManager.editRecordActivityEditMoneyFragment.getNumberText().toString().length()]);
    }

    private void commit() {
        if (RupayeFragmentManager.editRecordActivityEditMoneyFragment.getTagId() == -1) {
            showToast(NO_TAG_TOAST);
        } else if (RupayeFragmentManager.editRecordActivityEditMoneyFragment.getNumberText().toString().equals("0")) {
            showToast(NO_MONEY_TOAST);
        } else  {
            RupayeRecord RupayeRecord = new RupayeRecord();
            RupayeRecord.set(RecordManager.SELECTED_RECORDS.get(RecordManager.getInstance(mContext).SELECTED_RECORDS.size() - 1 - position));
            RupayeRecord.setMoney(Float.valueOf(RupayeFragmentManager.editRecordActivityEditMoneyFragment.getNumberText().toString()));
            RupayeRecord.setTag(RupayeFragmentManager.editRecordActivityEditMoneyFragment.getTagId());
            RupayeRecord.setRemark(RupayeFragmentManager.editRecordActivityEditRemarkFragment.getRemark());
            long updateId = RecordManager.updateRecord(RupayeRecord);
            if (updateId == -1) {
                if (!superToast.isShowing()) {
                    showToast(SAVE_FAILED_TOAST);
                }
            } else {
                IS_CHANGED = true;
                RecordManager.SELECTED_RECORDS.set(RecordManager.getInstance(mContext).SELECTED_RECORDS.size() - 1 - position, RupayeRecord);
                for (int i = RecordManager.getInstance(mContext).RECORDS.size() - 1; i >= 0; i--) {
                    if (RupayeRecord.getId() == RecordManager.RECORDS.get(i).getId()) {
                        RecordManager.RECORDS.set(i, RupayeRecord);
                        break;
                    }
                }
                onBackPressed();
            }
        }
    }

    private void showToast(int toastType) {
        SuperToast.cancelAllSuperToasts();
        SuperActivityToast.cancelAllSuperActivityToasts();

        superToast.setAnimations(RupayeUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);

        switch (toastType) {
            case NO_MONEY_TOAST:

                superToast.setText(mContext.getResources().getString(R.string.toast_no_money));
                superToast.setBackground(SuperToast.Background.BLUE);
                superToast.getTextView().setTypeface(RupayeUtil.typefaceLatoLight);

                break;
            case SAVE_SUCCESSFULLY_TOAST:

                superToast.setText(
                        mContext.getResources().getString(R.string.toast_save_successfully));
                superToast.setBackground(SuperToast.Background.GREEN);
                superToast.getTextView().setTypeface(RupayeUtil.typefaceLatoLight);

                break;
            case SAVE_FAILED_TOAST:

                superToast.setText(mContext.getResources().getString(R.string.toast_save_failed));
                superToast.setBackground(SuperToast.Background.RED);
                superToast.getTextView().setTypeface(RupayeUtil.typefaceLatoLight);

                break;
            default:

                break;
        }

        superToast.show();
    }

    @Override
    public void onTagItemPicked(int position) {
        RupayeFragmentManager.editRecordActivityEditMoneyFragment.setTag(tagViewPager.getCurrentItem() * 8 + position + 2);
    }

    @Override
    public void onAnimationStart(int id) {

    }

    private float x1, x2, y1, y2;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = ev.getX();
                y1 = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                x2 = ev.getX();
                y2 = ev.getY();
                if (editViewPager.getCurrentItem() == 0
                        && RupayeUtil.isPointInsideView(x2, y2, editViewPager)
                        && RupayeUtil.GetScreenWidth(mContext) - x2 <= 60) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < RupayeFragmentManager.tagChooseFragments.size(); i++) {
            if (RupayeFragmentManager.tagChooseFragments.get(i) != null) {
                RupayeFragmentManager.tagChooseFragments.get(i).onDestroy();
                RupayeFragmentManager.tagChooseFragments.set(i, null);
            }
        }
        if (RupayeFragmentManager.editRecordActivityEditMoneyFragment != null) {
            RupayeFragmentManager.editRecordActivityEditMoneyFragment.onDestroy();
            RupayeFragmentManager.editRecordActivityEditMoneyFragment = null;
        }
        if (RupayeFragmentManager.editRecordActivityEditRemarkFragment != null) {
            RupayeFragmentManager.editRecordActivityEditRemarkFragment.onDestroy();
            RupayeFragmentManager.editRecordActivityEditRemarkFragment = null;
        }
        super.onDestroy();
    }
}
