package com.bt.ahsanzaman.mapsample.ui.main.view;

import com.bt.ahsanzaman.mapsample.base.BaseView;
import com.bt.ahsanzaman.mapsample.domain.Steps;

import java.util.List;

/**
 * Created by Ahsan Zaman on 11-06-2017.
 */

public interface IMapActivityView extends BaseView {
    void setFromText(String placeName);

    void setToText(String placeName);

    void showInstructions();

    void hideInstructions();

    void onRouteClicked(List<Steps> steps, int position);
}
