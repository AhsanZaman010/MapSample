package com.bt.ahsanzaman.mapsample.domain;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ahsan Zaman on 09-06-2017.
 */

public class DirectionResults {
    @SerializedName("routes")
    private List<Route> routes;

    private LatLngBounds latLngBounds;

    private List<PolylineOptions> polylineOptionsList;

    public List<Route> getRoutes() {
        return routes;
    }

    public List<PolylineOptions> getPolylineOptionsList() {
        return polylineOptionsList;
    }

    public void setPolylineOptionsList(List<PolylineOptions> polylineOptionsList) {
        this.polylineOptionsList = polylineOptionsList;
    }

    public LatLngBounds getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }
}
