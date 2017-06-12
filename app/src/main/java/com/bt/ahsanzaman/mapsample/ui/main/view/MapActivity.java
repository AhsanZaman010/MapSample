package com.bt.ahsanzaman.mapsample.ui.main.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.bt.ahsanzaman.mapsample.ui.main.adapter.DirectionsAdapter;
import com.bt.ahsanzaman.mapsample.ui.main.adapter.DividerItemDecoration;
import com.bt.ahsanzaman.mapsample.ui.main.presenter.MapPresenter;
import com.bt.ahsanzaman.mapsample.R;
import com.bt.ahsanzaman.mapsample.domain.PlaceItem;
import com.bt.ahsanzaman.mapsample.domain.Steps;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MapActivity extends AppCompatActivity implements IMapActivityView {

    private MapPresenter mPresenter;
    public static final int FROM_REQUEST_CODE = 1;
    private static final int RESULT_CODE_CURRENT_LOCATION_OK = 23;
    public static final int TO_REQUEST_CODE = 2;


    @BindView(R.id.fromLocationName)
    TextView fromTextView;
    @BindView(R.id.toLocationName)
    TextView toTextView;
    @BindView(R.id.progressBarMain)
    View mProgressBarMain;
    @BindView(R.id.my_appbar_container)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.floating_button)
    FloatingActionButton mFloatingButton;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mDirectionsToolbar;
    private DirectionsAdapter mAdapter;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        CustomMapFragment fragment = CustomMapFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();
        mPresenter = new MapPresenter(fragment, this);
        hideInstructions();
        mCompositeDisposable = new CompositeDisposable();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
        //params.setBehavior(new FABBehavior());
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

            }
        });
        mAdapter = new DirectionsAdapter(new ArrayList<Steps>());
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(rvLayoutManager);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(this,
                R.drawable.recycler_divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if(verticalOffset==0){
                            e.onNext(1);
                        } else {
                            e.onNext(0);
                        }
                    }
                });
            }
        })
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                })
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer==1) {
                            mFloatingButton.setImageResource(R.drawable.ic_keyboard_arrow_up_white_48dp);
                        } else {
                            mFloatingButton.setImageResource(R.drawable.ic_keyboard_arrow_down_white_48dp);
                        }
                    }
                })
                .subscribe();
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompositeDisposable.dispose();
    }

    public GoogleApiClient getClient() {
        return null;
    }

    public MapPresenter updateFragment(CustomMapFragment customMapFragment) {
        if(mPresenter!=null){
            mPresenter.setMapView(customMapFragment);
        }
        return mPresenter;
    }

    @Override
    public void setFromText(String placeName) {
        fromTextView.setText(placeName);
    }

    @Override
    public void setToText(String placeName) {
        toTextView.setText(placeName);
    }

    @OnClick(R.id.fromLocationName)
    void fromClick() {
        Intent intent = new Intent(this, LocationsSelection.class);
        intent.putExtra("requestCode", FROM_REQUEST_CODE);
        startActivityForResult(intent, FROM_REQUEST_CODE);
    }

    @OnClick(R.id.toLocationName)
    void toClick() {
        Intent intent = new Intent(this, LocationsSelection.class);
        intent.putExtra("requestCode", TO_REQUEST_CODE);
        startActivityForResult(intent, TO_REQUEST_CODE);
    }

    @Override
    public void showLoading(int mode) {
        mProgressBarMain.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading(int mode) {
        mProgressBarMain.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            PlaceItem placeItem = (PlaceItem) data.getSerializableExtra("resultPlace");
            mPresenter.updatePlace(requestCode, resultCode, placeItem);
        }
    }

    @OnClick(R.id.floating_button)
    void onFloatClicked(){
        if (mAppBarLayout.getTop() < 0) {
            mAppBarLayout.setExpanded(true);
            mFloatingButton.setImageResource(R.drawable.ic_keyboard_arrow_up_white_48dp);
        }
        else {
            mAppBarLayout.setExpanded(false);
            mFloatingButton.setImageResource(R.drawable.ic_keyboard_arrow_down_white_48dp);
        }
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }



    @Override
    public void showInstructions(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mFloatingButton.show();
        mDirectionsToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInstructions(){
        mFloatingButton.hide();
        mRecyclerView.setVisibility(View.GONE);
        mDirectionsToolbar.setVisibility(View.GONE);
    }

    @Override
    public void onRouteClicked(List<Steps> steps, int position) {
        mAdapter.setItems(steps, position);
    }

    @OnClick(R.id.toolbar)
    void toolbarClick(){
        onFloatClicked();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 124) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mPresenter.permissionGranted();

            }
        }
    }
}
