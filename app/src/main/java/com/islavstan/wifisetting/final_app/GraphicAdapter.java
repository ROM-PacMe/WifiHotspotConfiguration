package com.islavstan.wifisetting.final_app;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.pavlospt.CircleView;
import com.islavstan.wifisetting.R;
import com.islavstan.wifisetting.adapter.WifiDaysRecAdapter;
import com.islavstan.wifisetting.model.Day;

import java.util.List;

public class GraphicAdapter extends RecyclerView.Adapter<GraphicAdapter.CustomViewHolder> {
    private List<GraphicModel> graphicModelList;

    public GraphicAdapter(List<GraphicModel> graphicModelList) {
        this.graphicModelList = graphicModelList;
    }


    @Override
    public GraphicAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.graphic_item, parent, false);

        return new GraphicAdapter.CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GraphicAdapter.CustomViewHolder holder, int position) {
        GraphicModel model = graphicModelList.get(position);
      /*  if(position == 0){
            holder.leftView.setVisibility(View.INVISIBLE);
        }else  holder.leftView.setVisibility(View.VISIBLE);*/

       if(model.getStatus()==1){
           holder.leftView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.colorPrimary));
           holder.rightView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.colorPrimary));
           holder.circleView.setFillColor(holder.leftView.getContext().getResources().getColor(R.color.colorPrimary));
           holder.circleView.setStrokeColor(holder.leftView.getContext().getResources().getColor(R.color.colorPrimary));
           holder.circleView.setTitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));
           holder.circleView.setSubtitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));

       }
        if(model.getStatus()==2){
            holder.leftView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.red));
            holder.rightView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.red));
            holder.circleView.setFillColor(holder.leftView.getContext().getResources().getColor(R.color.red));
            holder.circleView.setStrokeColor(holder.leftView.getContext().getResources().getColor(R.color.red));
            holder.circleView.setTitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));
            holder.circleView.setSubtitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));

        }

        if(model.getStatus()==3){
            holder.leftView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.orange));
            holder.rightView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.orange));
            holder.circleView.setFillColor(holder.leftView.getContext().getResources().getColor(R.color.orange));
            holder.circleView.setStrokeColor(holder.leftView.getContext().getResources().getColor(R.color.orange));
            holder.circleView.setTitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));
            holder.circleView.setSubtitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));

        }

        if(model.getStatus()==0){
            holder.leftView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.gray));
            holder.rightView.setBackgroundColor(holder.leftView.getContext().getResources().getColor(R.color.gray));
            holder.circleView.setFillColor(holder.leftView.getContext().getResources().getColor(R.color.gray));
            holder.circleView.setStrokeColor(holder.leftView.getContext().getResources().getColor(R.color.gray));
            holder.circleView.setTitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));
            holder.circleView.setSubtitleColor(holder.leftView.getContext().getResources().getColor(R.color.white));

        }


        holder.circleView.setTitleText(model.getNumber() + "");
       // holder.circleView.setSubtitleText(model.getDate());


    }


    @Override
    public int getItemCount() {
        return graphicModelList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        CircleView circleView;
        View leftView;
        View rightView;


        public CustomViewHolder(View itemView) {
            super(itemView);
            circleView = (CircleView) itemView.findViewById(R.id.main_item);
            rightView = (View) itemView.findViewById(R.id.right_item);
            leftView = (View) itemView.findViewById(R.id.left_item);


        }
    }
}
