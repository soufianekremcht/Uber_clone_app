package com.soufianekre.uquick.ui.driver.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.helpers.PermissionHelper;
import com.soufianekre.uquick.ui.base.BaseActivity;
import com.soufianekre.uquick.ui.driver.profile.DriverProfileActivity;
import com.soufianekre.uquick.ui.main.MainActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DriverMapActivity extends BaseActivity implements DriverMapMvp.View {

    //@Inject
    DriverMapPresenter<DriverMapMvp.View> mPresenter;

    @BindView(R.id.driver_map_toolbar)
    Toolbar driverMapToolbar;
    @BindView(R.id.driver_available_switch)
    SwitchCompat driverIsAvailableSwitch;
    @BindView(R.id.driver_map_drawer)
    DrawerLayout driverMapDrawer;
    @BindView(R.id.driver_map_nav_view)
    NavigationView driverMapNavView;
    @BindView(R.id.driver_map_location_btn)
    FloatingActionButton driverMapLocationBtn;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private SupportMapFragment mapFragment;
    private boolean isLoggingOut = false;

    private Polyline greyPolyLine;
    private Polyline bluePolyLine;


    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap mMap;
    private Marker mPickupMarker;
    private Marker mDestinationMarker;
    private Location mDriverLastLocation;
    private LatLng driverLastLocationLatLng;

    private int checkForLocationCount = 0;


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    if (mDriverLastLocation == null ||
                            mDriverLastLocation.getLatitude() != location.getLatitude()) {
                        driverLastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mDriverLastLocation = location;
                        /*if (checkForLocationCount<2){
                            moveToDriverLocation();
                            checkForLocationCount++;
                        }*/
                    }
                }
            }
            //Test if the driver available or Busy
            if (mDriverLastLocation != null)
                mPresenter.checkIfDriverAvailableNow(driverLastLocationLatLng, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        ButterKnife.bind(this);
        mPresenter = new DriverMapPresenter<>();
        mPresenter.onAttach(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionHelper.requestLocationPermission(this);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driver_map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setupUi();



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (driverIsAvailableSwitch.isChecked()){
            mPresenter.findCustomers();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mPresenter.removeAllListeners();
        mPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (PermissionHelper.isLocationPermissionGranted(this)) {
            if (PermissionHelper.isLocationEnabled(this)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient
                        .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                mMap.setMyLocationEnabled(true);
            } else {
                showMessage("Enable The Location in your Phone");
            }

        } else {
            showMessage("Location Permission Needed");
        }


    }


    @Override
    public void addPickupMarker(LatLng latLng) {
        if (mPickupMarker != null) mPickupMarker.remove();
        mPickupMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("pickup location"));
    }

    @Override
    public void addDestinationMarker(LatLng latLng) {
        if (mDestinationMarker != null) mDestinationMarker.remove();
        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Destination location"));
    }

    @Override
    public void showPath(List<LatLng> startEndLatLngList) {
        LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
        for (LatLng latLng : startEndLatLngList) {
            bounds_builder.include(latLng);
        }
        LatLngBounds bounds = bounds_builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2));
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GRAY);
        polylineOptions.width(5f);
        polylineOptions.addAll(startEndLatLngList);
        greyPolyLine = mMap.addPolyline(polylineOptions);

    }

    @Override
    public void startMap() {
        if (PermissionHelper.isLocationPermissionGranted(this)) {
            if (PermissionHelper.isLocationEnabled(this)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                PermissionHelper.showGPSNotEnabledDialog(this);
            }

        }
    }

    @Override
    public FusedLocationProviderClient getFusedLocationProviderClient() {
        return fusedLocationProviderClient;
    }

    @Override
    public LatLng getDriverLastLocationLatLng() {
        return driverLastLocationLatLng;
    }

    @Override
    public void resetMap() {
        if (greyPolyLine != null) greyPolyLine.remove();
        if (mPickupMarker != null) mPickupMarker.remove();
        if (mDestinationMarker != null) mDestinationMarker.remove();
        mPresenter.removeAssignedCustomerRefListeners();
    }

    @Override
    public void removeLocationUpdatesListener() {
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void driverLogout(){
        isLoggingOut = true;
        mPresenter.disconnectDriver();

        //String current_user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference driverRef = FirebaseDatabase.getInstance()
//                .getReference()
//                .child(USERS_PATH)
//                .child(DRIVERS_PATH)
//                .child(current_user_uid);
//        driverRef.removeValue();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void moveToDriverLocation(){
        if (driverLastLocationLatLng != null) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLastLocationLatLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(driverLastLocationLatLng));
        }
    }


    private void setupUi(){
        setSupportActionBar(driverMapToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        driverMapLocationBtn.setOnClickListener(v -> moveToDriverLocation());

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,driverMapDrawer,driverMapToolbar,R.string.open_drawer,R.string.close_drawer);

        actionBarDrawerToggle.syncState();
        driverMapDrawer.addDrawerListener(actionBarDrawerToggle);
        driverMapNavView.setNavigationItemSelectedListener(this);


        driverIsAvailableSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked){
                showMessage("Now You now Available");
                mPresenter.connectDriver();


            }else{
                showMessage("Now You now Busy");
                mPresenter.disconnectDriver();
            }
        });

        // change the location btn position
        View locationButton = ((View) mapFragment.getView().findViewById(
                Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 180, 180, 0);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.drawer_menu_profile:
                Intent settingsIntent = new Intent(DriverMapActivity.this,DriverProfileActivity.class);
                startActivity(settingsIntent);
                closeDrawer();
                return true;
            case R.id.drawer_menu_trip_history:
                return true;
            case R.id.drawer_menu_settings:
                return true;
            case R.id.drawer_menu_logout:
                driverLogout();
                closeDrawer();
                return true;

        }
        return false;
    }

    private void closeDrawer(){
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            driverMapDrawer.closeDrawer(GravityCompat.START);
        },250);
    }




    private void getRoutingToMarker(List<LatLng> pickupLatLng){
        LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
        for (LatLng latLng : pickupLatLng){
            bounds_builder.include(latLng);
        }
        LatLngBounds bounds = bounds_builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,2));
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GRAY);
        polylineOptions.width(5f);
        polylineOptions.addAll(pickupLatLng);
        greyPolyLine = mMap.addPolyline(polylineOptions);

//        PolylineOptions bluePolylineOptions = new PolylineOptions();
//        polylineOptions.color(Color.GRAY);
//        polylineOptions.width(5f);
//        bluePolyLine = mMap.addPolyline(bluePolylineOptions);

//
//        val builder = LatLngBounds.Builder()
//        for (latLng in latLngList) {
//            builder.include(latLng)
//        }
//        val bounds = builder.build()
//        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))
//        val polylineOptions = PolylineOptions()
//        polylineOptions.color(Color.GRAY)
//        polylineOptions.width(5f)
//        polylineOptions.addAll(latLngList)
//        greyPolyLine = googleMap.addPolyline(polylineOptions)
//
//        val blackPolylineOptions = PolylineOptions()
//        blackPolylineOptions.width(5f)
//        blackPolylineOptions.color(Color.BLACK)
//        blackPolyline = googleMap.addPolyline(blackPolylineOptions)
//
//        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
//        originMarker?.setAnchor(0.5f, 0.5f)
//        mDestinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
//        mDestinationMarker?.setAnchor(0.5f, 0.5f)



    }
}