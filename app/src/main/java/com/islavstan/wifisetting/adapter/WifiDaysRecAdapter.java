package com.islavstan.wifisetting.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.islavstan.wifisetting.R;
import com.islavstan.wifisetting.model.Date;
import com.islavstan.wifisetting.model.Day;

import java.util.List;

public class WifiDaysRecAdapter extends RecyclerView.Adapter<WifiDaysRecAdapter.CustomViewHolder> {
    private List<Day> daysList;

    public WifiDaysRecAdapter(List<Day> daysList) {
        this.daysList = daysList;
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi_date_item, parent, false);

        return new WifiDaysRecAdapter.CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Day day = daysList.get(position);
        holder.date.setText(day.getDate());
        holder.time.setText(day.getTime());
    }

    @Override
    public int getItemCount() {
        return daysList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView time;

        public CustomViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);


        }
    }
}
