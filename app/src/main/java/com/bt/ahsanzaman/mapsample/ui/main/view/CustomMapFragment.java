package com.bt.ahsanzaman.mapsample.ui.main.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bt.ahsanzaman.mapsample.ui.main.presenter.MapPresenter;
import com.bt.ahsanzaman.mapsample.utils.PermissionUtils;
import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

import static com.bt.ahsanzaman.mapsample.ui.main.view.MapActivity.FROM_REQUEST_CODE;

public class CustomMapFragment extends android.support.v4.app.Fragment implements
        LocationListener, OnMapReadyCallback, IMapView {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static GoogleMap map;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private SupportMapFragment mapFragment;
    @BindView(R.id.map)
    View mapView;
    private ArrayList<Polyline> polylines;
    private Marker toMarker;
    private Marker fromMarker;
    private MapPresenter mPresenter;
    private CompositeDisposable mCompositeDisposable;
    @BindView(R.id.container)
    View mParentView;
    @BindView(R.id.progressBarMain)
    View mProgressBarMain;


    public CustomMapFragment() {

    }

    public static CustomMapFragment newInstance() {
        CustomMapFragment fragment = new CustomMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompositeDisposable = new CompositeDisposable();
        polylines = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_custom_map, container, false);
        ButterKnife.bind(this, view);
        //Getting reference to SupportMapFragment of the activity_main
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MapActivity){
            mPresenter = ((MapActivity)context).updateFragment(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 124) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        map.animateCamera(cameraUpdate);
        if (getContext() == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isAlreadyGranted = PermissionUtils.isGranted(getActivity(), 124,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            if (isAlreadyGranted) {
                locationManager.removeUpdates(this);
            }
        } else {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        map = googleMap;
        if (map != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean isAlreadyGranted = PermissionUtils.isGranted(getActivity(), 124, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
                if (isAlreadyGranted) {
                    map.setMyLocationEnabled(true);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                }
            } else {
                map.setMyLocationEnabled(true);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            }

            // Enable MyLocation Button in the Map

            //        Location location = map.getMyLocation();
            //          LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                @Override
                public void onPolylineClick(Polyline polyline) {
                    if (polylines.contains(polyline)) {
                        mPresenter.onRouteClicked(polylines.indexOf(polyline));
                    }
                }
            });
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 80, 30);
            map.setPadding(0, 0, 50, 300);
        }
    }

    @Override
    public void showLoading(int mode) {
        mProgressBarMain.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading(int mode) {
        mProgressBarMain.setVisibility(View.GONE);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void refreshPolylines() {
        if(map!=null) {
            map.clear();
        }
        if (polylines != null && polylines.size() > 0) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines = null;
        }
    }

    @Override
    public void updateMap(int requestCode, LatLng initialMarkerPosition, int zoom) {
        updateMarkerPlots(initialMarkerPosition, requestCode);
    }

    @Override
    public void updateMap(List<PolylineOptions> polylineOptionsList) {
        if(polylines == null){
            polylines = new ArrayList<>();
        }
        for (PolylineOptions polylineOptions : polylineOptionsList) {
            Polyline polyline = map.addPolyline(polylineOptions);
            polyline.setClickable(true);
            polylines.add(polyline);
        }
    }

    @Override
    public void showError(String s) {
        Snackbar.make(mParentView, s,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void updateMarkerPlots(LatLng point, int requestCode) {
        if (requestCode == FROM_REQUEST_CODE) {
            placeFromMarker(point);
        } else {
            placeToMarker(point);
        }
    }

    @Override
    public void animateToPlace(PlaceItem placeItem){
        if(placeItem!=null){
            LatLng latLng = new LatLng(placeItem.getLatitude(), placeItem.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void animateBounds(LatLngBounds latLngBounds) {
            if(latLngBounds!=null && map!=null){
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200));
            }
    }

    @Override
    public void changeLines(int position) {
        if(polylines!=null){
            for(int i=0; i<polylines.size(); i++){
                Polyline polyline = polylines.get(i);
                if(polyline!=null){
                    if(i == position){
                        polyline.setColor(Color.BLUE);
                    } else {
                        polyline.setColor(Color.GRAY);
                    }
                }
            }
        }
    }

    @Override
    public void enableMyLocation() {
        map.setMyLocationEnabled(true);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    public Marker placeFromMarker(LatLng latLng) {
        if (fromMarker != null) {
            fromMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_black_48dp));
        markerOptions.position(latLng);
        markerOptions.visible(true);
        Log.e("From marker", "Installing new");
        fromMarker = map.addMarker(markerOptions);
        return fromMarker;
    }


    public Marker placeToMarker(LatLng latLng) {
        if (toMarker != null) {
            toMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_drop_black_48dp));
        markerOptions.visible(true);
        toMarker = map.addMarker(markerOptions);
        return toMarker;
    }




}
