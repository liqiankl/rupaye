package com.beulah.saver.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beulah.saver.R;
import com.beulah.saver.fragment.RecordCheckDialogFragment;
import com.beulah.saver.model.Rupaye;
import com.beulah.saver.model.RupayeRecord;
import com.beulah.saver.model.RecordManager;
import com.beulah.saver.model.SettingManager;
import com.beulah.saver.model.Tag;
import com.beulah.saver.util.RupayeUtil;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Created by 伟平 on 2015/10/20.
 */

// Todo optimize this

public class TagViewRecyclerViewAdapter
        extends RecyclerView.Adapter<TagViewRecyclerViewAdapter.viewHolder> {

    private Context mContext;

    private List<List<RupayeRecord>> contents;
    private List<Integer> type;
    private List<Double> SumList;
    private List<Map<Integer, Double>> AllTagExpanse;
    private int[] DayExpanseSum;
    private int[] MonthExpanseSum;
    private int[] SelectedPosition;
    private float Sum;
    private int year;
    private int month;
    private int startYear;
    private int startMonth;
    private int endYear;
    private int endMonth;

    private String dialogTitle;

    private int chartType;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    static final Integer SHOW_IN_YEAR = 0;
    static final Integer SHOW_IN_MONTH = 1;

    static final int PIE = 0;
    static final int SUM_HISTOGRAM = 1;
    static final int HISTOGRAM = 2;

    private int fragmentPosition;
    private int fragmentTagId;

    private boolean IS_EMPTY = false;

    public TagViewRecyclerViewAdapter(List<RupayeRecord> RupayeRecords, Context context, int position) {

        mContext = context;
        fragmentPosition = position;
        if (position == 0) {
            chartType = PIE;
        } else if (position == 1) {
            chartType = SUM_HISTOGRAM;
        } else {
            chartType = HISTOGRAM;
        }

        IS_EMPTY = RupayeRecords.isEmpty();

        Sum = 0;

        if (!IS_EMPTY) {

            Collections.sort(RupayeRecords, new Comparator<RupayeRecord>() {
                @Override
                public int compare(RupayeRecord lhs, RupayeRecord rhs) {
                    return rhs.getCalendar().compareTo(lhs.getCalendar());
                }
            });
            contents = new ArrayList<>();
            SumList = new ArrayList<>();
            type = new ArrayList<>();
            year = RupayeRecords.get(0).getCalendar().get(Calendar.YEAR);
            month = RupayeRecords.get(0).getCalendar().get(Calendar.MONTH) + 1;
            endYear = year;
            endMonth = month;
            int yearPosition = 0;
            double monthSum = 0;
            double yearSum = 0;
            List<RupayeRecord> yearSet = new ArrayList<>();
            List<RupayeRecord> monthSet = new ArrayList<>();

            for (RupayeRecord RupayeRecord : RupayeRecords) {
                Sum += RupayeRecord.getMoney();
                if (RupayeRecord.getCalendar().get(Calendar.YEAR) == year) {
                    yearSet.add(RupayeRecord);
                    yearSum += RupayeRecord.getMoney();
                    if (RupayeRecord.getCalendar().get(Calendar.MONTH) == month - 1) {
                        monthSet.add(RupayeRecord);
                        monthSum += RupayeRecord.getMoney();
                    } else {
                        contents.add(monthSet);
                        SumList.add(monthSum);
                        monthSum = RupayeRecord.getMoney();
                        type.add(SHOW_IN_MONTH);
                        monthSet = new ArrayList<>();
                        monthSet.add(RupayeRecord);
                        month = RupayeRecord.getCalendar().get(Calendar.MONTH) + 1;
                    }
                } else {
                    contents.add(monthSet);
                    SumList.add(monthSum);
                    monthSum = RupayeRecord.getMoney();
                    type.add(SHOW_IN_MONTH);
                    monthSet = new ArrayList<>();
                    monthSet.add(RupayeRecord);
                    month = RupayeRecord.getCalendar().get(Calendar.MONTH) + 1;

                    contents.add(yearPosition, yearSet);
                    SumList.add(yearPosition, yearSum);
                    yearSum = RupayeRecord.getMoney();
                    type.add(yearPosition, SHOW_IN_YEAR);
                    yearPosition = contents.size();
                    yearSet = new ArrayList<>();
                    yearSet.add(RupayeRecord);
                    year = RupayeRecord.getCalendar().get(Calendar.YEAR);
                    monthSet = new ArrayList<>();
                    monthSet.add(RupayeRecord);
                    month = RupayeRecord.getCalendar().get(Calendar.MONTH) + 1;
                }
            }
            contents.add(monthSet);
            SumList.add(monthSum);
            type.add(SHOW_IN_MONTH);
            contents.add(yearPosition, yearSet);
            SumList.add(yearPosition, yearSum);
            type.add(yearPosition, SHOW_IN_YEAR);

            startYear = year;
            startMonth = month;

            if (chartType == PIE) {
                AllTagExpanse = new ArrayList<>();
                for (int i = 0; i < contents.size(); i++) {

                    Map<Integer, Double> tagExpanse = new TreeMap<>();

                    for (Tag tag : RecordManager.TAGS) {
                        tagExpanse.put(tag.getId(), Double.valueOf(0));
                    }

                    for (RupayeRecord RupayeRecord : contents.get(i)) {
                        double d = tagExpanse.get(RupayeRecord.getTag());
                        d += RupayeRecord.getMoney();
                        tagExpanse.put(RupayeRecord.getTag(), d);
                    }

                    tagExpanse = RupayeUtil.SortTreeMapByValues(tagExpanse);

                    AllTagExpanse.add(tagExpanse);
                }
            }

            if (chartType == SUM_HISTOGRAM) {
                DayExpanseSum = new int[(endYear - startYear + 1) * 372];
                for (RupayeRecord RupayeRecord : RupayeRecords) {
                    DayExpanseSum[(RupayeRecord.getCalendar().get(Calendar.YEAR) - startYear) * 372 +
                            RupayeRecord.getCalendar().get(Calendar.MONTH) * 31 +
                            RupayeRecord.getCalendar().get(Calendar.DAY_OF_MONTH) - 1] += (int) RupayeRecord.getMoney();
                }
            }

            MonthExpanseSum = new int[(endYear - startYear + 1) * 12];
            for (RupayeRecord RupayeRecord : RupayeRecords) {
                MonthExpanseSum[(RupayeRecord.getCalendar().get(Calendar.YEAR) - startYear) * 12 +
                        RupayeRecord.getCalendar().get(Calendar.MONTH)] += (int) RupayeRecord.getMoney();
            }

            SelectedPosition = new int[contents.size() + 1];
            for (int i = 0; i < SelectedPosition.length; i++) {
                SelectedPosition[i] = 0;
            }

            fragmentTagId = contents.get(0).get(0).getTag();
            if (fragmentPosition == 0) {
                fragmentTagId = -2;
            }
            if (fragmentPosition == 1) {
                fragmentTagId = -1;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_HEADER;
            default:
                return TYPE_CELL;
        }
    }

    @Override
    public int getItemCount() {
        if (IS_EMPTY) return 1;
        return contents.size() + 1;
    }

    @Override
    public TagViewRecyclerViewAdapter.viewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case TYPE_HEADER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.tag_list_view_head, parent, false);
                return new viewHolder(view) {
                };
            }
            case TYPE_CELL: {
                switch (chartType) {
                    case PIE:
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.tag_list_view_pie_body, parent, false);
                        return new viewHolder(view) {
                        };
                    case HISTOGRAM:
                        view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.tag_list_view_histogram_body, parent, false);
                        return new viewHolder(view) {
                        };
                    case SUM_HISTOGRAM:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.tag_list_view_histogram_body, parent, false);
                        return new viewHolder(view) {
                        };
                }
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final viewHolder holder, final int position) {

        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                if (IS_EMPTY) {
                    holder.sum.setText(mContext.getResources().getString(R.string.tag_empty));
                    holder.sum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    holder.from.setVisibility(View.INVISIBLE);
                    holder.to.setVisibility(View.INVISIBLE);
                } else {
                    holder.from.setText(
                            mContext.getResources().getString(R.string.from) + " " +
                                    startYear + " " + RupayeUtil.GetMonthShort(startMonth));
                    holder.sum.setText(RupayeUtil.GetInMoney((int)Sum));
                    holder.to.setText(
                            mContext.getResources().getString(R.string.to) + " " +
                                    endYear + " " + RupayeUtil.GetMonthShort(endMonth));
                    holder.to.setTypeface(RupayeUtil.GetTypeface());
                    holder.from.setTypeface(RupayeUtil.GetTypeface());
                }
                holder.sum.setTypeface(RupayeUtil.typefaceLatoLight);
                break;
            case TYPE_CELL:
                int year = contents.get(position - 1).get(0).getCalendar().get(Calendar.YEAR);
                int month = contents.get(position - 1).get(0).getCalendar().get(Calendar.MONTH) + 1;
                PieChartData pieChartData;
                List<SubcolumnValue> subcolumnValues;
                final List<Column> columns;
                ColumnChartData columnChartData;
                final List<SliceValue> sliceValues;
                holder.date.setTypeface(RupayeUtil.GetTypeface());
                holder.expanse.setTypeface(RupayeUtil.GetTypeface());
                switch (chartType) {
                    case PIE:
                        sliceValues = new ArrayList<>();
                        for (Map.Entry<Integer, Double> entry :
                                AllTagExpanse.get(position - 1).entrySet()) {
                            if (entry.getValue() >= 1) {
                                SliceValue sliceValue = new SliceValue(
                                        (float)(double)entry.getValue(),
                                        mContext.getResources().
                                                getColor(RupayeUtil.GetTagColorResource(entry.getKey())));
                                sliceValue.setLabel(String.valueOf(entry.getKey()));
                                sliceValues.add(sliceValue);
                            }
                        }

                        pieChartData = new PieChartData(sliceValues);
                        pieChartData.setHasLabels(false);
                        pieChartData.setHasLabelsOnlyForSelected(false);
                        pieChartData.setHasLabelsOutside(false);
                        pieChartData.setHasCenterCircle(SettingManager.getInstance().getIsHollow());

                        holder.pie.setPieChartData(pieChartData);
                        holder.pie.setOnValueTouchListener(new PieValueTouchListener(position - 1));
                        holder.pie.setChartRotationEnabled(false);

                        if (type.get(position - 1).equals(SHOW_IN_MONTH)) {
                            holder.date.setText(year + " " + RupayeUtil.GetMonthShort(month));
                        } else {
                            holder.date.setText(year + " ");
                        }

                        holder.expanse.setText(RupayeUtil.GetInMoney((int) (double) SumList.get(position - 1)));

                        holder.iconRight.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] + 1) % sliceValues.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.pie.selectValue(selectedValue);
                            }
                        });

                        holder.iconLeft.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SelectedPosition[position]
                                        = (SelectedPosition[position] - 1 + sliceValues.size()) % sliceValues.size();
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.pie.selectValue(selectedValue);
                            }
                        });

                        break;
                    case SUM_HISTOGRAM:
                        columns = new ArrayList<>();
                        if (type.get(position - 1).equals(SHOW_IN_YEAR)) {
                            int numColumns = 12;
                            for (int i = 0; i < numColumns; i++) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue(
                                        MonthExpanseSum[(year - startYear) * 12 + i],
                                        RupayeUtil.GetRandomColor());
                                value.setLabel(RupayeUtil.MONTHS_SHORT[month] + " " + year);
                                subcolumnValues.add(value);
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < numColumns; i++) {
                                axisValueList.add(new AxisValue(i)
                                        .setLabel(RupayeUtil.GetMonthShort(i + 1)));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(
                                    new ValueTouchListener(position - 1));

                            holder.date.setText(year + "");
                            holder.expanse.setText(RupayeUtil.GetInMoney((int)(double)SumList.get(position - 1)));
                        }
                        if (type.get(position - 1).equals(SHOW_IN_MONTH)) {
                            Calendar tempCal = new GregorianCalendar(year, month - 1, 1);
                            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

                            int numColumns = daysInMonth;

                            for (int i = 0; i < numColumns; ++i) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue((float)
                                        DayExpanseSum[(year - startYear) * 372
                                                + (month - 1) * 31 + i],
                                        RupayeUtil.GetRandomColor());
                                value.setLabel(RupayeUtil.MONTHS_SHORT[month] + " " + (i + 1) + " " + year);
                                subcolumnValues.add(value);
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < daysInMonth; i++) {
                                axisValueList.add(new AxisValue(i).setLabel(i + 1 + ""));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(new ValueTouchListener(position - 1));

                            holder.date.setText(year + " " + RupayeUtil.GetMonthShort(month));
                            holder.expanse.setText(RupayeUtil.GetInMoney((int)(double)SumList.get(position - 1)));
                        }

                        holder.iconRight.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                do {
                                    SelectedPosition[position]
                                            = (SelectedPosition[position] + 1) % columns.size();
                                } while (holder.chart.getChartData().getColumns()
                                         .get(SelectedPosition[position])
                                         .getValues().size() == 0 ||
                                         holder.chart.getChartData().getColumns()
                                         .get(SelectedPosition[position])
                                         .getValues().get(0).getValue() == 0);
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.COLUMN);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        holder.iconLeft.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                do {
                                    SelectedPosition[position]
                                            = (SelectedPosition[position] - 1 + columns.size())
                                            % columns.size();
                                } while (holder.chart.getChartData().getColumns()
                                         .get(SelectedPosition[position])
                                         .getValues().size() == 0 ||
                                         holder.chart.getChartData().getColumns()
                                                 .get(SelectedPosition[position])
                                         .getValues().get(0).getValue() == 0);
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.COLUMN);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        break;
                    case HISTOGRAM:
                        columns = new ArrayList<>();
                        if (type.get(position - 1).equals(SHOW_IN_YEAR)) {
                            int numColumns = 12;
                            for (int i = 0; i < numColumns; i++) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue(
                                        MonthExpanseSum[(year - startYear) * 12 + i],
                                        RupayeUtil.GetRandomColor());
                                value.setLabel(RupayeUtil.MONTHS_SHORT[month] + " " + year);
                                subcolumnValues.add(value);
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < numColumns; i++) {
                                axisValueList.add(new AxisValue(i)
                                        .setLabel(RupayeUtil.GetMonthShort(i + 1)));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(
                                    new ValueTouchListener(position - 1));

                            holder.date.setText(year + "");
                            holder.expanse.setText(RupayeUtil.GetInMoney((int)(double)SumList.get(position - 1)));

                        }
                        if (type.get(position - 1).equals(SHOW_IN_MONTH)) {
                            Calendar tempCal = Calendar.getInstance();
                            tempCal.set(year, month - 1, 1);
                            tempCal.add(Calendar.SECOND, 0);
                            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                            int p = contents.get(position - 1).size() - 1;
                            int numColumns = daysInMonth;

                            for (int i = 0; i < numColumns; ++i) {
                                subcolumnValues = new ArrayList<>();
                                SubcolumnValue value = new SubcolumnValue(0,
                                        RupayeUtil.GetRandomColor());
                                subcolumnValues.add(value);
                                while (p >= 0
                                        && contents.get(position - 1).get(p).getCalendar().
                                        get(Calendar.DAY_OF_MONTH) == i + 1) {
                                    subcolumnValues.get(0).setValue(
                                            subcolumnValues.get(0).getValue() +
                                                    (float) contents.get(position - 1).get(p).getMoney());
                                    subcolumnValues.get(0).setLabel(p + "");
                                    p--;
                                }
                                Column column = new Column(subcolumnValues);
                                column.setHasLabels(false);
                                column.setHasLabelsOnlyForSelected(false);
                                columns.add(column);
                            }

                            columnChartData = new ColumnChartData(columns);

                            Axis axisX = new Axis();
                            List<AxisValue> axisValueList = new ArrayList<>();
                            for (int i = 0; i < daysInMonth; i++) {
                                axisValueList.add(new AxisValue(i).setLabel(i + 1 + ""));
                            }
                            axisX.setValues(axisValueList);
                            Axis axisY = new Axis().setHasLines(true);
                            columnChartData.setAxisXBottom(axisX);
                            columnChartData.setAxisYLeft(axisY);
                            columnChartData.setStacked(true);

                            holder.chart.setColumnChartData(columnChartData);
                            holder.chart.setZoomEnabled(false);
                            holder.chart.setOnValueTouchListener(new ValueTouchListener(position - 1));

                            holder.date.setText(year + " " + RupayeUtil.GetMonthShort(month));
                            holder.expanse.setText(RupayeUtil.GetInMoney((int)(double)SumList.get(position - 1)));
                        }

                        holder.iconRight.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                do {
                                    SelectedPosition[position]
                                            = (SelectedPosition[position] + 1) % columns.size();
                                } while (holder.chart.getChartData().getColumns()
                                         .get(SelectedPosition[position])
                                         .getValues().size() == 0 ||
                                         holder.chart.getChartData().getColumns()
                                                 .get(SelectedPosition[position])
                                         .getValues().get(0).getValue() == 0);
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        holder.iconLeft.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                do {
                                    SelectedPosition[position]
                                            = (SelectedPosition[position] - 1 + columns.size())
                                            % columns.size();
                                } while (holder.chart.getChartData().getColumns()
                                        .get(SelectedPosition[position])
                                         .getValues().size() == 0 ||
                                        holder.chart.getChartData().getColumns()
                                         .get(SelectedPosition[position])
                                         .getValues().get(0).getValue() == 0);
                                SelectedValue selectedValue =
                                        new SelectedValue(
                                                SelectedPosition[position],
                                                0,
                                                SelectedValue.SelectedValueType.NONE);
                                holder.chart.selectValue(selectedValue);
                            }
                        });

                        break;
                }
        }
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        @Optional
        @InjectView(R.id.from)
        TextView from;
        @Optional
        @InjectView(R.id.sum)
        TextView sum;
        @Optional
        @InjectView(R.id.to)
        TextView to;
        @Optional
        @InjectView(R.id.chart_pie)
        PieChartView pie;
        @Optional
        @InjectView(R.id.chart)
        ColumnChartView chart;
        @Optional
        @InjectView(R.id.date)
        TextView date;
        @Optional
        @InjectView(R.id.expanse)
        TextView expanse;
        @Optional
        @InjectView(R.id.icon_left)
        MaterialIconView iconLeft;
        @Optional
        @InjectView(R.id.icon_right)
        MaterialIconView iconRight;

        viewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        private int position;

        public ValueTouchListener(int position) {
            this.position = position;
        }

        @Override
        public void onValueSelected(final int columnIndex, int subColumnIndex, SubcolumnValue value) {
            Snackbar snackbar =
                    Snackbar.with(mContext)
                            .type(SnackbarType.MULTI_LINE)
                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                            .position(Snackbar.SnackbarPosition.BOTTOM)
                            .margin(15, 15)
                            .backgroundDrawable(RupayeUtil.GetSnackBarBackground(fragmentTagId))
                            .textColor(Color.WHITE)
                            .textTypeface(RupayeUtil.GetTypeface())
                            .actionLabel(mContext.getResources().getString(R.string.check))
                            .actionLabelTypeface(RupayeUtil.typefaceLatoLight)
                            .actionColor(Color.WHITE);
            if (fragmentPosition == SUM_HISTOGRAM) {
                if (type.get(position).equals(SHOW_IN_MONTH)) {
                    String text = "";
                    String timeString = contents.get(position).get(0).getCalendarString();
                    int month = contents.get(position).get(0).getCalendar().get(Calendar.MONTH) + 1;
                    timeString = " " + RupayeUtil.GetMonthShort(month)
                            + " " + (columnIndex + 1) + " "
                            + timeString.substring(timeString.length() - 4, timeString.length());
                    if ("zh".equals(RupayeUtil.GetLanguage())) {
                        text = mContext.getResources().getString(R.string.on) + timeString + "\n" +
                                RupayeUtil.GetSpendString((int) value.getValue());
                        dialogTitle = mContext.getResources().getString(R.string.on) + timeString +
                                RupayeUtil.GetSpendString((int) value.getValue());
                    } else {
                        text = RupayeUtil.GetSpendString((int) value.getValue()) + "\n" +
                                mContext.getResources().getString(R.string.on) + timeString;
                        dialogTitle = RupayeUtil.GetSpendString((int) value.getValue()) +
                                mContext.getResources().getString(R.string.on) + timeString;
                    }
                    snackbar.text(text);
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<RupayeRecord> shownRupayeRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (RupayeRecord RupayeRecord : contents.get(position)) {
                                if (RupayeRecord.getCalendar().get(Calendar.DAY_OF_MONTH) == columnIndex + 1) {
                                    shownRupayeRecords.add(RupayeRecord);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialogFragment(
                                            mContext, shownRupayeRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
                if (type.get(position).equals(SHOW_IN_YEAR)) {
                    String text;
                    String timeString = " " +
                            contents.get(position).get(0).getCalendar().get(Calendar.YEAR);
                    timeString = " " + RupayeUtil.GetMonthShort(columnIndex + 1) + " "
                            + timeString.substring(timeString.length() - 4, timeString.length());
                    if ("zh".equals(RupayeUtil.GetLanguage())) {
                        text = mContext.getResources().getString(R.string.in) + timeString + "\n" +
                                RupayeUtil.GetSpendString((int) value.getValue());
                        dialogTitle = mContext.getResources().getString(R.string.in) + timeString +
                                RupayeUtil.GetSpendString((int) value.getValue());
                    } else {
                        text = RupayeUtil.GetSpendString((int) value.getValue()) + "\n" +
                                mContext.getResources().getString(R.string.in) + timeString;
                        dialogTitle = RupayeUtil.GetSpendString((int) value.getValue()) +
                                mContext.getResources().getString(R.string.in) + timeString;
                    }
                    snackbar.text(text);
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<RupayeRecord> shownRupayeRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (RupayeRecord RupayeRecord : contents.get(position)) {
                                if (RupayeRecord.getCalendar().get(Calendar.MONTH) == columnIndex) {
                                    shownRupayeRecords.add(RupayeRecord);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialogFragment(
                                            mContext, shownRupayeRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
            } else {
                if (type.get(position).equals(SHOW_IN_MONTH)) {
                    String text = "";
                    String timeString = contents.get(position).get(0).getCalendarString();
                    timeString = timeString.substring(6, timeString.length());
                    int month = contents.get(position).get(0).getCalendar().get(Calendar.MONTH) + 1;
                    timeString = " " + RupayeUtil.GetMonthShort(month)
                            + " " + (columnIndex + 1) + " "
                            + timeString.substring(timeString.length() - 4, timeString.length());
                    if ("zh".equals(RupayeUtil.GetLanguage())) {
                        text = mContext.getResources().getString(R.string.on) + timeString +
                                RupayeUtil.GetSpendString((int) value.getValue()) + "\n" +
                                "于" + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                        dialogTitle = mContext.getResources().getString(R.string.on) + timeString +
                                RupayeUtil.GetSpendString((int) value.getValue()) + "\n" +
                                "于" + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                    } else {
                        text = RupayeUtil.GetSpendString((int) value.getValue()) +
                                mContext.getResources().getString(R.string.on) + timeString + "\n"
                                + "in " + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                        dialogTitle = RupayeUtil.GetSpendString((int) value.getValue()) +
                                mContext.getResources().getString(R.string.on) + timeString + "\n"
                                + "in " + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                    }
                    snackbar.text(text);
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<RupayeRecord> shownRupayeRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (RupayeRecord RupayeRecord : contents.get(position)) {
                                if (RupayeRecord.getCalendar().
                                        get(Calendar.DAY_OF_MONTH) == columnIndex + 1) {
                                    shownRupayeRecords.add(RupayeRecord);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialogFragment(
                                            mContext, shownRupayeRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
                if (type.get(position).equals(SHOW_IN_YEAR)) {
                    String text;
                    String timeString = "" +
                            contents.get(position).get(0).getCalendar().get(Calendar.YEAR);
                    timeString = " " + RupayeUtil.GetMonthShort(columnIndex + 1) + " "
                            + timeString.substring(timeString.length() - 4, timeString.length());
                    if ("zh".equals(RupayeUtil.GetLanguage())) {
                        text = mContext.getResources().getString(R.string.in) + timeString +
                                RupayeUtil.GetSpendString((int) value.getValue()) + "\n" +
                                "于" + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                        dialogTitle = mContext.getResources().getString(R.string.in) + timeString +
                                RupayeUtil.GetSpendString((int) value.getValue()) + "\n" +
                                "于" + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                    } else {
                        text = RupayeUtil.GetSpendString((int) value.getValue()) +
                                mContext.getResources().getString(R.string.in) + timeString + "\n"
                                + "in " + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                        dialogTitle = RupayeUtil.GetSpendString((int) value.getValue()) +
                                mContext.getResources().getString(R.string.in) + timeString + "\n"
                                + "in " + RupayeUtil.GetTagName(contents.get(position).get(0).getTag());
                    }
                    snackbar.text(text);
                    snackbar.actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            List<RupayeRecord> shownRupayeRecords = new ArrayList<>();
                            boolean isSamed = false;
                            for (RupayeRecord RupayeRecord : contents.get(position)) {
                                if (RupayeRecord.getCalendar().get(Calendar.MONTH) == columnIndex) {
                                    shownRupayeRecords.add(RupayeRecord);
                                    isSamed = true;
                                } else {
                                    if (isSamed) {
                                        break;
                                    }
                                }
                            }
                            ((FragmentActivity)mContext).getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(new RecordCheckDialogFragment(
                                            mContext, shownRupayeRecords, dialogTitle), "MyDialog")
                                    .commit();
                        }
                    });
                    SnackbarManager.show(snackbar);
                }
            }
        }

        @Override
        public void onValueDeselected() {

        }

    }

    private class PieValueTouchListener implements PieChartOnValueSelectListener {

        private int position;

        public PieValueTouchListener(int position) {
            this.position = position;
        }

        @Override
        public void onValueSelected(int i, SliceValue sliceValue) {
            String text = "";
            String timeString = contents.get(position).get(0).getCalendarString();
            int month = contents.get(position).get(0).getCalendar().get(Calendar.MONTH) + 1;
            timeString = timeString.substring(6, timeString.length());
            if (type.get(position).equals(SHOW_IN_YEAR)) {
                timeString = timeString.substring(timeString.length() - 4, timeString.length());
            } else {
                timeString = RupayeUtil.GetMonthShort(month) + " " +
                        timeString.substring(timeString.length() - 4, timeString.length());
            }
            final int tagId = Integer.valueOf(String.valueOf(sliceValue.getLabelAsChars()));
            Double percent = sliceValue.getValue() / SumList.get(position) * 100;
            if ("zh".equals(RupayeUtil.GetLanguage())) {
                text = RupayeUtil.GetSpendString((int) sliceValue.getValue()) +
                        RupayeUtil.GetPercentString(percent) + "\n" +
                        "于" + RupayeUtil.GetTagName(tagId);
                    dialogTitle = mContext.getResources().getString(R.string.in) + timeString +
                            RupayeUtil.GetSpendString((int) sliceValue.getValue()) + "\n" +
                            "于" + RupayeUtil.GetTagName(tagId);

            } else {
                text = RupayeUtil.GetSpendString((int) sliceValue.getValue()) +
                        RupayeUtil.GetPercentString(percent) + "\n" +
                        "in " + RupayeUtil.GetTagName(RecordManager.TAGS.get(tagId).getId());
                dialogTitle = RupayeUtil.GetSpendString((int) sliceValue.getValue()) +
                        mContext.getResources().getString(R.string.in) + timeString + "\n" +
                        "in " + RupayeUtil.GetTagName(RecordManager.TAGS.get(tagId).getId());
            }
            Snackbar snackbar =
                    Snackbar
                            .with(mContext)
                            .type(SnackbarType.MULTI_LINE)
                            .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                            .position(Snackbar.SnackbarPosition.BOTTOM)
                            .margin(15, 15)
                            .backgroundDrawable(RupayeUtil.GetSnackBarBackground(fragmentTagId))
                            .text(text)
                            .textTypeface(RupayeUtil.GetTypeface())
                            .textColor(Color.WHITE)
                            .actionLabelTypeface(RupayeUtil.typefaceLatoLight)
                            .actionLabel(mContext.getResources().getString(R.string.check))
                            .actionColor(Color.WHITE)
                            .actionListener(new ActionClickListener() {
                                @Override
                                public void onActionClicked(Snackbar snackbar) {
                                    List<RupayeRecord> shownRupayeRecords = new ArrayList<>();
                                    for (RupayeRecord record : contents.get(position)) {
                                        if (record.getTag() == tagId) {
                                            shownRupayeRecords.add(record);
                                        }
                                    }
                                    ((FragmentActivity) mContext).getSupportFragmentManager()
                                            .beginTransaction()
                                            .add(new RecordCheckDialogFragment(
                                                    mContext, shownRupayeRecords, dialogTitle), "MyDialog")
                                            .commit();
                                }
                            });
            SnackbarManager.show(snackbar);
        }

        @Override
        public void onValueDeselected() {

        }
    }

}
