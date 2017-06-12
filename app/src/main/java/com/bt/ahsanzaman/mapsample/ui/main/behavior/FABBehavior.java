package com.bt.ahsanzaman.mapsample.ui.main.behavior;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Created by Ahsan Zaman on 11-06-2017.
 */

public class FABBehavior extends FloatingActionButton.Behavior {

    public FABBehavior() {
        super();
        setAutoHideEnabled(false);
    }

    public FABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAutoHideEnabled(false);
    }

}
