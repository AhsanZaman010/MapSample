package com.bt.ahsanzaman.mapsample.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ahsan Zaman on 09-06-2017.
 */
public class OverviewPolyLine {

    @SerializedName("points")
    public String points;

    public String getPoints() {
        return points;
    }
}
