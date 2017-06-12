package com.bt.ahsanzaman.mapsample.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ahsan Zaman on 09-06-2017.
 */
public class Route {
    @SerializedName("overview_polyline")
    private OverviewPolyLine overviewPolyLine;

    private List<Legs> legs;

    @SerializedName("bounds")
    private Bounds bounds;

    public OverviewPolyLine getOverviewPolyLine() {
        return overviewPolyLine;
    }

    public List<Legs> getLegs() {
        return legs;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }
}
