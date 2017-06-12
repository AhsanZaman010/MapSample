package com.bt.ahsanzaman.mapsample.ui.main.presenter;

import android.graphics.Color;
import android.util.Log;

import com.bt.ahsanzaman.mapsample.utils.NetworkUtils;
import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.domain.DirectionResults;
import com.bt.ahsanzaman.mapsample.domain.Location;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;
import com.bt.ahsanzaman.mapsample.domain.Route;
import com.bt.ahsanzaman.mapsample.domain.RouteDecode;
import com.bt.ahsanzaman.mapsample.domain.Steps;
import com.bt.ahsanzaman.mapsample.service.LocationsService;
import com.bt.ahsanzaman.mapsample.ui.main.view.CustomMapFragment;
import com.bt.ahsanzaman.mapsample.ui.main.view.IMapActivityView;
import com.bt.ahsanzaman.mapsample.ui.main.view.IMapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static com.bt.ahsanzaman.mapsample.ui.main.view.MapActivity.FROM_REQUEST_CODE;
import static com.bt.ahsanzaman.mapsample.ui.main.view.MapActivity.TO_REQUEST_CODE;

/**
 * Created by Ahsan Zaman on 04-06-2017.
 */

public class MapPresenter {

    private IMapView mMapView;
    private final BehaviorSubject<PlaceItem> mSubject;
    private final CompositeDisposable mCompositeDisposable;
    private final LocationsService mLocationsService;
    private final PriorityQueue<Integer> mColorQueue;
    private final IMapActivityView mActivityView;
    private PlaceItem mFromPlace;
    private PlaceItem mToPlace;
    private DirectionResults mDirectionsResult;

    public MapPresenter(final IMapView mapView, IMapActivityView mapActivityView) {
        mMapView = mapView;
        mActivityView = mapActivityView;
        mSubject = BehaviorSubject.create();
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(getSubject(mapView).subscribe());
        mLocationsService = new LocationsService();
        mColorQueue = new PriorityQueue<>();
        mColorQueue.add(Color.BLUE);
        mColorQueue.add(Color.RED);
        mColorQueue.add(Color.YELLOW);
    }

    private Observable getSubject(final IMapView mapView) {
        return mSubject
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mapView.showError("Something went wrong");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<PlaceItem>() {
                    @Override
                    public void accept(PlaceItem placeItem) throws Exception {
                        if(areLocationsSelected()) {
                            mapView.animateBounds(LatLngBounds
                                    .builder()
                                    .include(mFromPlace.getLatLng())
                                    .include(mToPlace.getLatLng())
                                    .build());
                            getDirections();
                        } else {
                            mapView.updateMap(placeItem.getPlaceType(), new LatLng(placeItem.getLatitude(), placeItem.getLongitude()), placeItem.getPlaceType());
                            mapView.animateToPlace(placeItem);
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public void getDirections() {
        if(NetworkUtils.isInternetOn(((CustomMapFragment) mMapView).getContext())) {
            mMapView.showLoading(0);
            mLocationsService.getAPI().getResults(mFromPlace.getLatitude() + "," + mFromPlace.getLongitude(), mToPlace.getLatitude() + "," + mToPlace.getLongitude())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(new Function<DirectionResults, DirectionResults>() {
                        @Override
                        public DirectionResults apply(DirectionResults directionResults) throws Exception {
                            ArrayList<ArrayList<LatLng>> routes = new ArrayList<ArrayList<LatLng>>();
                            if (directionResults.getRoutes().size() > 0) {
                                for (Route routeA : directionResults.getRoutes()) {
                                    ArrayList<LatLng> routelist = new ArrayList<LatLng>();
                                    ArrayList<LatLng> decodelist;
                                    Log.i("zacharia", "Legs length : " + routeA.getLegs().size());
                                    if (routeA.getLegs().size() > 0) {
                                        List<Steps> steps = routeA.getLegs().get(0).getSteps();
                                        Log.i("zacharia", "Steps size :" + steps.size());
                                        Steps step;
                                        Location location;
                                        String polyline;
                                        for (int i = 0; i < steps.size(); i++) {
                                            step = steps.get(i);
                                            location = step.getStart_location();
                                            routelist.add(new LatLng(location.getLat(), location.getLng()));
                                            Log.i("zacharia", "Start Location :" + location.getLat() + ", " + location.getLng());
                                            polyline = step.getPolyline().getPoints();
                                            decodelist = RouteDecode.decodePoly(polyline);
                                            routelist.addAll(decodelist);
                                            location = step.getEnd_location();
                                            routelist.add(new LatLng(location.getLat(), location.getLng()));
                                            Log.i("zacharia", "End Location :" + location.getLat() + ", " + location.getLng());
                                        }
                                    }
                                    routes.add(routelist);
                                }
                            }
                            List<PolylineOptions> polylineOptionsList = new ArrayList<PolylineOptions>();
                            for(ArrayList<LatLng> routelist : routes) {
                                if (routelist.size() > 0) {
                                    int color = mColorQueue.poll();
                                    mColorQueue.add(color);
                                    PolylineOptions rectLine = new PolylineOptions().width(10).color(
                                            color);
                                    for (int i = 0; i < routelist.size(); i++) {
                                        rectLine.add(routelist.get(i));
                                    }
                                    polylineOptionsList.add(rectLine);
                                    // Adding route on the map
                                }
                            }
                            directionResults.setPolylineOptionsList(polylineOptionsList);
                            if(mDirectionsResult!=null && mDirectionsResult.getRoutes()!=null && mDirectionsResult.getRoutes().size()>0){
                                LatLngBounds latLngBounds = null;
                                for(Route route : mDirectionsResult.getRoutes()){
                                    if(latLngBounds == null) {
                                        latLngBounds = new LatLngBounds( route.getBounds().getSouthWest().getLatLng(), route.getBounds().getNorthEast().getLatLng());
                                    }
                                    latLngBounds = latLngBounds.including(route.getBounds().getNorthEast().getLatLng()).including(route.getBounds().getSouthWest().getLatLng());
                                }
                                if(latLngBounds!=null){
                                    mDirectionsResult.setLatLngBounds(latLngBounds);
                                }
                            }
                            return directionResults;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(new Consumer<DirectionResults>() {
                        @Override
                        public void accept(DirectionResults directionResults) throws Exception {
                            mDirectionsResult = directionResults;
                            mMapView.updateMap(directionResults.getPolylineOptionsList());
                            mMapView.animateBounds(mDirectionsResult.getLatLngBounds());
                            mMapView.updateMap(TO_REQUEST_CODE, mToPlace.getLatLng(), 17);
                            mMapView.updateMap(FROM_REQUEST_CODE, mFromPlace.getLatLng(), 17);
                            mMapView.hideLoading(0);
                            onRouteClicked(0);
                        }
                    })
                    .subscribe();
        } else {
            mMapView.showError(((CustomMapFragment) mMapView).getString(R.string.no_internet_connection));
        }
    }

    public void updatePlace(int requestCode, int resultCode, PlaceItem placeItem) {
        if (placeItem == null) {
            return;
        }
        if(areLocationsSelected()){
            mMapView.refreshPolylines();
        }
        placeItem.setPlaceType(requestCode);
        if (requestCode == FROM_REQUEST_CODE) {
            Log.e("ResultPlace", placeItem.getLatitude() + "");
            mActivityView.setFromText(placeItem.getPlaceName());
            mFromPlace = placeItem;
            mMapView.updateMap(FROM_REQUEST_CODE, new LatLng(placeItem.getLatitude(), placeItem.getLongitude()), 17);
            mSubject.onNext(placeItem);
        }
        if (requestCode == TO_REQUEST_CODE) {
            Log.e("ResultPlace", placeItem.getLatitude() + "");
            mActivityView.setToText(placeItem.getPlaceName());
            mToPlace = placeItem;
            mMapView.updateMap(TO_REQUEST_CODE, new LatLng(placeItem.getLatitude(), placeItem.getLongitude()), 17);
            mSubject.onNext(placeItem);
        }
        mActivityView.hideInstructions();
    }

    public boolean areLocationsSelected() {
        if(mFromPlace!=null && mToPlace!=null && mFromPlace.containsLocation() && mToPlace.containsLocation()){
            return true;
        }
        return false;
    }

    public void setMapView(CustomMapFragment customMapFragment) {
        mMapView = customMapFragment;
    }

    public void onRouteClicked(int position) {
        if(mDirectionsResult!=null && mDirectionsResult.getRoutes()!=null && mDirectionsResult.getRoutes().size()>position) {
            mActivityView.showInstructions();
            mActivityView.onRouteClicked(mDirectionsResult.getRoutes().get(position).getLegs().get(0).getSteps(), position);
            mMapView.changeLines(position);
        }
    }

    public void permissionGranted() {
        mMapView.enableMyLocation();
    }
}
