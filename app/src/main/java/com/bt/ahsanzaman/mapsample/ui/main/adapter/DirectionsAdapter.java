package com.bt.ahsanzaman.mapsample.ui.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.domain.Steps;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ahsan Zaman on 11-06-2017.
 */

public class DirectionsAdapter extends RecyclerView.Adapter<DirectionsHolder> {

    private final ArrayList<Steps> mSteps;
    private int mPosition;

    public DirectionsAdapter(ArrayList<Steps> steps) {
        mSteps = steps;
        mPosition = -1;
    }

    @Override
    public DirectionsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.direction_item, parent, false);
        return new DirectionsHolder(view);
    }

    @Override
    public void onBindViewHolder(DirectionsHolder holder, int position) {
        Steps step = mSteps.get(position);
        if(step!=null) {
            holder.mDirectionsText.setText(Html.fromHtml(step.getInstructions()), TextView.BufferType.NORMAL);
            holder.mDirectionsSNo.setText(position+1+".");
        }
    }

    @Override
    public int getItemCount() {
        return mSteps==null?0:mSteps.size();
    }

    public void setItems(ArrayList<Steps> steps){
        mSteps.clear();
        if(steps!=null){
            mSteps.addAll(steps);
        }
    }

    public void setItems(List<Steps> steps, int position) {
        if(mPosition !=position){
            mSteps.clear();
            if(steps!=null){
                mSteps.addAll(steps);
            }
        }
        notifyDataSetChanged();
    }
}

class DirectionsHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.directions_text)
    TextView mDirectionsText;
    @BindView(R.id.directions_s_no)
    TextView mDirectionsSNo;

    public DirectionsHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
