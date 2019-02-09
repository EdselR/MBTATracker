package com.edsel.mbtatracker;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class stopAdapter extends RecyclerView.Adapter<stopAdapter.ViewHolder>{

    Context context;
    List<mbtaStops> stopsList;


    public stopAdapter(List<mbtaStops> stopsList) {
        this.stopsList = stopsList;

    }

    @Override
    public stopAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stops_row, parent, false);

        context = parent.getContext();

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(stopAdapter.ViewHolder holder, int pos) {


        holder.name.setText("" + stopsList.get(pos).getName());
        holder.name.setBackgroundColor(Color.parseColor(stopsList.get(pos).getColour()));
        holder.direction.setText("Direction: " + stopsList.get(pos).getPlatform());
        holder.estimatedArrival.setText("Current Arrival: " + stopsList.get(pos).getEtaTime());
        holder.nextArrival.setText("Next Arrival: " + stopsList.get(pos).getNextTime());

        final int index = pos;


        holder.itemView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                mbtaStops currentStop = stopsList.get(index);
                Intent map = new Intent(context, stopInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("stop", currentStop);
                map.putExtras(bundle);
                context.startActivity(map);

            }});

    }

    @Override
    public int getItemCount() {
        if(stopsList == null){
            return 0;
        }
        else{
            return stopsList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView direction;
        public TextView estimatedArrival;
        public TextView nextArrival;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.stopName);
            direction = itemView.findViewById(R.id.stopDirection);
            estimatedArrival = itemView.findViewById(R.id.estimatedTime);
            nextArrival = itemView.findViewById(R.id.nextArrival);

        }
    }
}
