package com.bt.ahsanzaman.mapsample.domain;

import com.google.gson.annotations.SerializedName;

public class Steps {
    private Location start_location;
    private Location end_location;
    private OverviewPolyLine polyline;

    @SerializedName("html_instructions")
    private String instructions;

    public Location getStart_location() {
        return start_location;
    }

    public Location getEnd_location() {
        return end_location;
    }

    public OverviewPolyLine getPolyline() {
        return polyline;
    }


    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}

