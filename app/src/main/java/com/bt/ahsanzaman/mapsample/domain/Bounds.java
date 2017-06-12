package com.bt.ahsanzaman.mapsample.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ahsan Zaman on 12-06-2017.
 */

public class Bounds {

    @SerializedName("northeast")
    private PlaceItem northEast;

    @SerializedName("southwest")
    private PlaceItem southWest;

    public PlaceItem getNorthEast() {
        return northEast;
    }

    public void setNorthEast(PlaceItem northEast) {
        this.northEast = northEast;
    }

    public PlaceItem getSouthWest() {
        return southWest;
    }

    public void setSouthWest(PlaceItem southWest) {
        this.southWest = southWest;
    }
}
