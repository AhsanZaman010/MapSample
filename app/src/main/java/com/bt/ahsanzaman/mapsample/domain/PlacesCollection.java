package com.bt.ahsanzaman.mapsample.domain;

import java.util.ArrayList;

/**
 * Created by Ahsan Zaman on 12-06-2017.
 */

public class PlacesCollection {

    public PlacesCollection(int responseCode) {
        this.responseCode = responseCode;
    }

    public PlacesCollection() {
    }

    public PlacesCollection(ArrayList<PlaceItem> places) {
        this.places = places;
    }

    private ArrayList<PlaceItem> places;

    private int responseCode;

    private String message;

    public ArrayList<PlaceItem> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<PlaceItem> places) {
        this.places = places;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


}
