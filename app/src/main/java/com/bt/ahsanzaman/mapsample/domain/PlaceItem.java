package com.bt.ahsanzaman.mapsample.domain;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Accolite- on 2/2/2016.
 */
public class PlaceItem implements Serializable {
    private String placeID;
    private String placeName;

    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;
    private String placeAddress;
    private int placeType;

    public PlaceItem()
    {

    }

    public PlaceItem(String placeID, String placeName, double latitude, double longitude, String placeAddress, int placeType) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.placeType = placeType;
        this.placeID=placeID;
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public int getPlaceType() {
        return placeType;
    }

    public void setPlaceType(int placeType) {
        this.placeType = placeType;
    }

    public boolean isLocationSet()
    {
        if(latitude==0||longitude==0)
            return false;
        return true;
    }
    public void setGoogleLocation(LatLng latLng)
    {
        latitude=latLng.latitude;
        longitude=latLng.longitude;
    }

    public boolean containsLocation() {
        return latitude!=0 && longitude!=0;
    }

    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
