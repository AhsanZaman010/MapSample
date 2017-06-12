package com.bt.ahsanzaman.mapsample.ui.main.view;

import com.bt.ahsanzaman.mapsample.base.BaseView;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by Ahsan Zaman on 05-06-2017.
 */

public interface IMapView extends BaseView {
    void refreshPolylines();

    void updateMap(int fromRequestCode, LatLng latLng, int i);

    void updateMap(List<PolylineOptions> polylineOptionsList);

    void showError(String s);

    void animateToPlace(PlaceItem placeItem);

    void animateBounds(LatLngBounds latLngBounds);

    void changeLines(int position);

    void enableMyLocation();
}
