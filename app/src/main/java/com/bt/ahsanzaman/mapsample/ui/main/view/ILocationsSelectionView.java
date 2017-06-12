package com.bt.ahsanzaman.mapsample.ui.main.view;

import com.bt.ahsanzaman.mapsample.base.BaseView;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;

import java.util.ArrayList;

/**
 * Created by Ahsan Zaman on 08-06-2017.
 */

public interface ILocationsSelectionView extends BaseView {
    void showError(String error);

    void returnPlace(PlaceItem placeItem);

    void updatePlaces(ArrayList<PlaceItem> placeItems);
}
