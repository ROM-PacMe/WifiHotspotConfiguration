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
        holder.time.setText(getCorrectTime(Long.valueOf(day.getTime())));
        holder.number.setText(day.getNumber() + ".");
    }



    private String getCorrectTime(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;

        seconds = seconds % 60;
        return String.format("%02d:%02d", hours, minutes);


    }


    @Override
    public int getItemCount() {
        return daysList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView time;
        TextView number;

        public CustomViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.time);
            number = (TextView) itemView.findViewById(R.id.number);


        }
    }
}
