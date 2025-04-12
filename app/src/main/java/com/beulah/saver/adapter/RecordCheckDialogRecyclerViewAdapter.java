package com.beulah.saver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.beulah.saver.R;
import com.beulah.saver.model.RupayeRecord;
import com.beulah.saver.model.RecordManager;
import com.beulah.saver.util.RupayeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by 伟平 on 2015/11/1.
 */
public class RecordCheckDialogRecyclerViewAdapter extends RecyclerView.Adapter<RecordCheckDialogRecyclerViewAdapter.viewHolder> {

    private OnItemClickListener onItemClickListener;

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List<RupayeRecord> RupayeRecords;

    public RecordCheckDialogRecyclerViewAdapter(Context context, List<RupayeRecord> list) {
        RupayeRecords = new ArrayList<>();
        RupayeRecords = list;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public RecordCheckDialogRecyclerViewAdapter(Context context, List<RupayeRecord> list, OnItemClickListener onItemClickListener) {
        RupayeRecords = new ArrayList<>();
        RupayeRecords = list;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new viewHolder(mLayoutInflater.inflate(R.layout.record_check_item, parent, false));
    }

    @Override
    public void onBindViewHolder(viewHolder holder, final int position) {
        holder.imageView.setImageResource(
                RupayeUtil.GetTagIcon(RupayeRecords.get(position).getTag()));
        holder.date.setText(RupayeRecords.get(position).getCalendarString());
        holder.date.setTypeface(RupayeUtil.typefaceLatoLight);
        holder.money.setTypeface(RupayeUtil.typefaceLatoLight);
        holder.money.setText(String.valueOf((int) RupayeRecords.get(position).getMoney()));
        holder.money.setTextColor(
                RupayeUtil.GetTagColorResource(RecordManager.TAGS.get(RupayeRecords.get(position).getTag()).getId()));
        holder.index.setText((position + 1) + "");
        holder.index.setTypeface(RupayeUtil.typefaceLatoLight);
        holder.remark.setText(RupayeRecords.get(position).getRemark());
        holder.remark.setTypeface(RupayeUtil.typefaceLatoLight);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (RupayeRecords == null) {
            return 0;
        }
        return RupayeRecords.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.image_view)
        ImageView imageView;
        @InjectView(R.id.date)
        TextView date;
        @InjectView(R.id.remark)
        TextView remark;
        @InjectView(R.id.money)
        TextView money;
        @InjectView(R.id.index)
        TextView index;
        @InjectView(R.id.material_ripple_layout)
        MaterialRippleLayout layout;

        viewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

        @Override
        public void onClick(View v) {
//            onItemClickListener.onItemClick(v, getPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }
}