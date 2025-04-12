package com.beulah.saver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.beulah.saver.R;
import com.beulah.saver.activity.RupayeApplication;
import com.beulah.saver.fragment.ReportViewFragment;
import com.beulah.saver.util.RupayeUtil;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Weiping on 2016/2/2.
 */

public class ReportDayAdapter extends BaseAdapter {

    private ArrayList<double[]> dayExpense;
    private int month;

    public ReportDayAdapter(ArrayList<double[]> dayExpense, int month) {
        this.dayExpense = dayExpense;
        this.month = month;
    }

    @Override
    public int getCount() {
        return min(dayExpense.size() - 1, ReportViewFragment.MAX_DAY_EXPENSE);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_day, null);

        TextView icon = (TextView)convertView.findViewById(R.id.month);
        TextView name = (TextView)convertView.findViewById(R.id.month_name);
        TextView expense = (TextView)convertView.findViewById(R.id.month_expense);
        TextView records = (TextView)convertView.findViewById(R.id.month_sum);

        icon.setTypeface(RupayeUtil.getInstance().typefaceLatoLight);
        name.setTypeface(RupayeUtil.getInstance().typefaceLatoLight);
        expense.setTypeface(RupayeUtil.getInstance().typefaceLatoLight);
        records.setTypeface(RupayeUtil.getInstance().typefaceLatoLight);

        icon.setBackgroundResource(getBackgroundResource());
        icon.setText("" + ((int)dayExpense.get(position + 1)[2]));
        name.setText(RupayeUtil.getInstance().GetCalendarStringDayExpenseSort(RupayeApplication.getAppContext(), (int)dayExpense.get(position + 1)[0], (int)dayExpense.get(position + 1)[1] + 1, (int)dayExpense.get(position + 1)[2]) + RupayeUtil.getInstance().GetPurePercentString(dayExpense.get(position + 1)[4] * 100));
        expense.setText(RupayeUtil.getInstance().GetInMoney((int) dayExpense.get(position + 1)[3]));
        records.setText(RupayeUtil.getInstance().GetInRecords((int) dayExpense.get(position + 1)[5]));

        return convertView;
    }

    private int getBackgroundResource() {
        Random random = new Random();
        switch (random.nextInt(6)) {
            case 0: return R.drawable.bg_month_icon_small_0;
            case 1: return R.drawable.bg_month_icon_small_1;
            case 2: return R.drawable.bg_month_icon_small_2;
            case 3: return R.drawable.bg_month_icon_small_3;
            case 4: return R.drawable.bg_month_icon_small_4;
            case 5: return R.drawable.bg_month_icon_small_5;
            default:return R.drawable.bg_month_icon_small_0;
        }
    }

    private int min(int a, int b) {
        return (a < b ? a : b);
    }

}
