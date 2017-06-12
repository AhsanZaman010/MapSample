package com.bt.ahsanzaman.mapsample.ui.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;

import java.util.ArrayList;

/**
 * Created by Accolite- on 2/2/2016.
 */
public class LocationsPlaceAdapter extends ArrayAdapter<PlaceItem> implements Filterable {

    // declaring our ArrayList of item

    private ArrayList<PlaceItem> mResultList;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public LocationsPlaceAdapter(Context context, int textViewResourceId, ArrayList<PlaceItem> objects) {
        super(context, textViewResourceId, objects);
        this.mResultList = objects;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View view = convertView;
        view=convertView;
        // assign the view we are converting to a local variable


        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.place_list_item, null);
        }
            TextView placeTitle = (TextView) view.findViewById(R.id.placeTitle);
            TextView placeDescription = (TextView) view.findViewById(R.id.placeDescription);
            PlaceItem placeItem = mResultList.get(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.placeImage);
            imageView.setImageResource(R.drawable.prediction_default_black);
            placeTitle.setText(placeItem.getPlaceName());
            placeDescription.setText(placeItem.getPlaceAddress());

        return view;

    }

    public void updateList(ArrayList<PlaceItem> placesList)
    {
        mResultList.clear();
        mResultList.addAll(placesList);
    }
    @Override
    public int getCount() {
        if(mResultList==null)
            return 0;
        return mResultList.size();
    }

    @Override
    public PlaceItem getItem(int position) {
        return mResultList.get(position);
    }

}
