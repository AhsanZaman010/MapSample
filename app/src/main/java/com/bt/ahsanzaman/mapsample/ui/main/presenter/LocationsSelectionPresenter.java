package com.bt.ahsanzaman.mapsample.ui.main.presenter;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.bt.ahsanzaman.mapsample.domain.PlacesCollection;
import com.bt.ahsanzaman.mapsample.utils.GPSTracker;
import com.bt.ahsanzaman.mapsample.ui.main.view.ILocationsSelectionView;
import com.bt.ahsanzaman.mapsample.ui.main.view.LocationsSelection;
import com.bt.ahsanzaman.mapsample.utils.NetworkUtils;
import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;
import com.bt.ahsanzaman.mapsample.utils.PermissionUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.bt.ahsanzaman.mapsample.ui.main.view.LocationsSelection.LOCATION_REQUEST;
import static com.bt.ahsanzaman.mapsample.ui.main.view.LocationsSelection.PRIMARY_PROGRESS;
import static com.bt.ahsanzaman.mapsample.ui.main.view.LocationsSelection.REQUEST_CHECK_SETTINGS;
import static com.google.android.gms.common.api.CommonStatusCodes.API_NOT_CONNECTED;

/**
 * Created by Ahsan Zaman on 08-06-2017.
 */

public class LocationsSelectionPresenter {

    public static final int SOMETHING_WENT_WRONG = 12;
    public static final int NO_INTERNET = 13;
    private final ILocationsSelectionView mView;
    private final GoogleApiClient mClient;
    private static final LatLngBounds BOUNDS_BANGALORE = new LatLngBounds(
            new LatLng(12.867466, 77.490437), new LatLng(13.050815, 77.689564));
    private final Context mContext;
    private final CompositeDisposable mCompositeDisposable;

    public LocationsSelectionPresenter(ILocationsSelectionView locationsSelection, GoogleApiClient client, Context context) {
        mView = locationsSelection;
        mClient = client;
        mContext = context;
        mCompositeDisposable = new CompositeDisposable();
    }

    public PlacesCollection getList(CharSequence constraint) throws Exception {
        if(NetworkUtils.isInternetOn(mContext)) {
            if (mClient != null) {
                Log.i("", "Executing autocomplete query for: " + constraint);
                PendingResult<AutocompletePredictionBuffer> results =
                        Places.GeoDataApi
                                .getAutocompletePredictions(mClient, constraint.toString(),
                                        BOUNDS_BANGALORE, null);
                // Wait for predictions, set the timeout.
                AutocompletePredictionBuffer autocompletePredictions = results
                        .await(60, TimeUnit.SECONDS);
                final Status status = autocompletePredictions.getStatus();
                if (!status.isSuccess()) {
                    //mView.showError(status.toString());
                    Log.e("Predictions", "Error getting place predictions: " + status
                            .toString());
                    autocompletePredictions.release();
                    return new PlacesCollection(SOMETHING_WENT_WRONG);
                }

                Log.i("Predictions", "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");
                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                ArrayList<PlaceItem> resultList = new ArrayList<PlaceItem>();
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    resultList.add(new PlaceItem(prediction.getPlaceId(), prediction.getPrimaryText(null).toString(), 0, 0,
                            prediction.getFullText(null).toString(), 4));
                }
                // Buffer release
                autocompletePredictions.release();
                return new PlacesCollection(resultList);
            } else {
                Log.e("Places Prediction", "Google API client is not connected.");
                return new PlacesCollection(API_NOT_CONNECTED);
            }
        } else {
            return new PlacesCollection(NO_INTERNET);
        }
    }

    void sendLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    (LocationsSelection) mContext,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    public void getCurrentLocation() {
        boolean isAlreadyGranted = PermissionUtils.isGranted((Activity) mContext, LOCATION_REQUEST,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        if (!isAlreadyGranted) {
            return;
        }
        mView.showLoading(PRIMARY_PROGRESS);
        LocationManager lm = (LocationManager) mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            sendLocationRequest();
        } else {
            GPSTracker gpsTracker = new GPSTracker(mContext, (Activity) mContext);
            PlaceItem placeItem = new PlaceItem();
            placeItem.setGoogleLocation(new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()));
            placeItem.setPlaceName(gpsTracker.getLocationName(mContext));
            //placeItem.setPlaceAddress(placeItem.getPlaceName());
            mView.returnPlace(placeItem);
        }
    }

    public void onPlaceSelected(final PlaceItem item) {
        mView.showLoading(PRIMARY_PROGRESS);
        final PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(mClient, item.getPlaceID());
        Observable.create(new ObservableOnSubscribe<PlaceItem>() {
            @Override
            public void subscribe(final ObservableEmitter<PlaceItem> e) throws Exception {
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            Log.e("dlfslfvm", "Place query did not complete. Error: " +
                                    places.getStatus().toString());
                            e.onError(new NetworkErrorException());
                        }
                        final Place place = places.get(0);
                        LatLng latLng = place.getLatLng();
                        item.setLatitude(latLng.latitude);
                        item.setLongitude(latLng.longitude);
                        if (TextUtils.isEmpty(item.getPlaceName())) {
                            item.setPlaceName(place.getName().toString());
                        }
                        if (TextUtils.isEmpty(item.getPlaceAddress())) {
                            item.setPlaceAddress(place.getAddress().toString());
                        }
                        places.release();
                        e.onNext(item);
                    }
                });
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<PlaceItem>() {
                    @Override
                    public void accept(PlaceItem placeItem) throws Exception {
                        mView.hideLoading(PRIMARY_PROGRESS);;
                        mView.returnPlace(placeItem);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.hideLoading(PRIMARY_PROGRESS);;
                        mView.showError(mContext.getString(R.string.error));
                    }
                })
                .subscribe();
    }
}
