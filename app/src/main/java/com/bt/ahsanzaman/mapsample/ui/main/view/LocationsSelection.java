package com.bt.ahsanzaman.mapsample.ui.main.view;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.base.BaseActivity;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;
import com.bt.ahsanzaman.mapsample.domain.PlacesCollection;
import com.bt.ahsanzaman.mapsample.ui.main.adapter.LocationsPlaceAdapter;
import com.bt.ahsanzaman.mapsample.ui.main.presenter.LocationsSelectionPresenter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.bt.ahsanzaman.mapsample.ui.main.presenter.LocationsSelectionPresenter.NO_INTERNET;

public class LocationsSelection extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ILocationsSelectionView {

    public static final int REQUEST_CHECK_SETTINGS = 101;
    private static final int SECONDARY_PROGRESS = 1;
    public static final int PRIMARY_PROGRESS = 0;
    public static final int LOCATION_REQUEST = 100;
    private LocationsPlaceAdapter locationsPlaceAdapter;
    private static final int FROM_REQUEST_CODE = 1;
    private static final int TO_REQUEST_CODE = 2;

    @BindView(R.id.useCurrentLocation)
    View useCurrentLocation;
    @BindView(R.id.progressLayout)
    RelativeLayout progressLayout;
    @BindView(R.id.placesLayout)
    LinearLayout placesLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.placeSuggestionsListView)
    ListView placeSuggestionsListView;
    @BindView(R.id.inputSearchLocation)
    EditText inputSearchLocation;
    @BindView(R.id.progressBarMain)
    View progressBarMain;
    @BindView(R.id.container)
    View mParentView;


    private LocationsSelectionPresenter mPresenter;
    private CompositeDisposable mCompositeDisposable;
    private int mRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_selection);
        ButterKnife.bind(this);
        mCompositeDisposable = new CompositeDisposable();
        mPresenter = new LocationsSelectionPresenter(this,
                new GoogleApiClient.Builder(this)
                        .addApi(Places.GEO_DATA_API)
                        .enableAutoManage(this, 0, this)
                        .addConnectionCallbacks(this)
                        .addApi(LocationServices.API)
                        .build(), this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int requestCode = getIntent().getExtras().getInt("requestCode", 0);
            mRequestCode = requestCode;
            if (requestCode == FROM_REQUEST_CODE)
                inputSearchLocation.setHint("Search from location");
            if (requestCode == TO_REQUEST_CODE)
                inputSearchLocation.setHint("Search to location");
        }
        progressLayout.setVisibility(View.INVISIBLE);
        locationsPlaceAdapter = new LocationsPlaceAdapter(this, R.layout.place_list_item, new ArrayList<PlaceItem>());
        placeSuggestionsListView.setAdapter(locationsPlaceAdapter);
    }

    @OnItemClick(R.id.placeSuggestionsListView)
    void itemClicked(int position) {
        mPresenter.onPlaceSelected(locationsPlaceAdapter.getItem(position));
    }

    @OnClick(R.id.useCurrentLocation)
    void useCurrentLocation() {
        mPresenter.getCurrentLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Disposable disposable = Observable
                .create(new ObservableOnSubscribe<CharSequence>() {
                    @Override
                    public void subscribe(final ObservableEmitter<CharSequence> emitter) throws Exception {
                        inputSearchLocation.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                showLoading(SECONDARY_PROGRESS);
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                emitter.onNext(s);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }
                })
                .observeOn(Schedulers.io())
                .debounce(500, TimeUnit.MILLISECONDS).map(new Function<CharSequence, String>() {
                    @Override
                    public String apply(CharSequence charSequence) throws Exception {
                        return charSequence.toString();
                    }
                })
                .throttleLast(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        return s;
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<PlacesCollection>>() {
                    @Override
                    public ObservableSource<PlacesCollection> apply(String s) throws Exception {
                        if(s == null || s.length()<3)
                            return Observable.just(new PlacesCollection(new ArrayList<PlaceItem>()));
                        try {
                            PlacesCollection places = mPresenter.getList(s);
                            return Observable.just(places);
                        } catch (Exception e){
                            return Observable.error(e);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideLoading(SECONDARY_PROGRESS);
                        if (throwable instanceof NetworkErrorException) {
                            showError(getString(R.string.error) + getString(R.string.space) +getString(R.string.check_internet));
                        } else if (throwable instanceof InterruptedException) {
                            showError(getString(R.string.error));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<PlacesCollection>() {
                    @Override
                    public void accept(PlacesCollection places) throws Exception {
                        hideLoading(SECONDARY_PROGRESS);
                        if(places==null){
                            showError(getString(R.string.error));
                        } else if(places.getResponseCode() == NO_INTERNET ){
                            showError(getString(R.string.error) + getString(R.string.space) +getString(R.string.check_internet));
                        } else if(places.getResponseCode() != 0 || places.getPlaces()==null){
                            showError(getString(R.string.error));
                        } else {
                            updatePlaces(places.getPlaces());
                        }
                    }
                })
                .subscribe();
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    //startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    //settingsrequest();//keep asking if imp or do whatever
                    break;
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeDisposable.dispose();
    }

    @Override
    public void showError(String error) {
        Snackbar.make(mParentView, error,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void returnPlace(PlaceItem placeItem) {
        Intent intent = new Intent();
        intent.putExtra("resultPlace", placeItem);
        setResult(mRequestCode, intent);
        finish();
    }

    @Override
    public void updatePlaces(ArrayList<PlaceItem> placeItems) {
        locationsPlaceAdapter.updateList(placeItems);
        locationsPlaceAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading(int mode) {
        switch (mode){
            case PRIMARY_PROGRESS:
                progressBarMain.setVisibility(View.VISIBLE);
                break;
            case SECONDARY_PROGRESS:
                progressLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void hideLoading(int mode) {
        switch (mode){
            case PRIMARY_PROGRESS:
                progressBarMain.setVisibility(View.GONE);
                break;
            case SECONDARY_PROGRESS:
                progressLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mPresenter.getCurrentLocation();
            }
        }
    }
}
