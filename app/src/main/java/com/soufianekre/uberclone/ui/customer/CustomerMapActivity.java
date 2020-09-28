package com.soufianekre.uberclone.ui.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.helpers.PermissionHelper;
import com.soufianekre.uberclone.ui.base.BaseActivity;
import com.soufianekre.uberclone.ui.customer.profile.CustomerProfileActivity;
import com.soufianekre.uberclone.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CustomerMapActivity extends BaseActivity implements CustomerMapContract.View {


    private static final int PICKUP_REQUEST_CODE = 102;
    private static final int DROP_REQUEST_CODE = 103;
    private static final String REQUEST_UBER_DIALOG_TAG = "request_uber_dialog";


    //widgets
    @BindView(R.id.customer_map_toolbar)
    Toolbar customerMapToolbar;
    @BindView(R.id.customer_request_uber_btn)
    Button customerRequestUberBtn;

    // requestUberSheet
    @BindView(R.id.bs_request_uber_choose_destination_btn)
    Button bsChooseDestinationBtn;
    @BindView(R.id.bs_request_uber_cancel_destination_btn)
    Button bsCancelDestinationBtn;
    @BindView(R.id.bs_request_uber_btn)
    Button bsRequestUberBtn;

    //driverInfoSheet
    @BindView(R.id.bs_driver_info_name)
    TextView bsDriverInfoName;
    @BindView(R.id.bs_driver_info_phone)
    TextView getBsDriverInfoPhone;
    @BindView(R.id.bs_driver_info_image_view)
    ImageView bsDriverInfoImg;

    @BindView(R.id.customer_map_location_fab)
    FloatingActionButton customerMapLocationFab;

    @BindView(R.id.drawer_customer_map)
    DrawerLayout drawerCustomerLayout;
    @BindView(R.id.nav_view_customer)
    NavigationView navViewCustomer;

    private SupportMapFragment mapFragment;

    //presenter
    CustomerMapContract.Presenter<CustomerMapContract.View> mPresenter;

    private boolean isLogging = true;
    private boolean isRequesting = false;


    //Polyline
    private Polyline routePolyLine;

    // Google Maps
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location mCustomerLastLocation;

    private LatLng customerLastLocationLatLng;
    private LatLng customerDestinationLatLng;


    // markers
    private Marker pickupMarker;
    private BitmapDescriptor pickupMarkerIcon;

    private Marker mDriverMarker;
    private BitmapDescriptor driverMarkerIcon;

    private Marker mDestinationMarker;
    private BitmapDescriptor destinationMarkerIcon;


    private BottomSheetBehavior requestUberSheetBehavior;
    private BottomSheetBehavior driverInfoSheetBehavior;


    private int checkForLocationCount = 0;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                customerLastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (getApplicationContext() != null) {
                    mCustomerLastLocation = location;
                }
                if (checkForLocationCount < 2) {
                    moveToCustomerLocation();
                    checkForLocationCount++;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        ButterKnife.bind(this);
        mPresenter = new CustomerMapPresenter<>();
        mPresenter.onAttach(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.customer_map_fragment);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PermissionHelper.requestLocationPermission(this);

        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setupUi();

    }

    private void setupUi () {
        customerMapToolbar.setTitle("Customer DashBoard");
        setSupportActionBar(customerMapToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);


        customerMapLocationFab.setOnClickListener(v -> moveToCustomerLastLocation());
        setupRequestUberBottomSheet();
        setupDriverInfoBottomSheet();
        driverMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.car);
        destinationMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.finish);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_customer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.customer_map_menu_profile:
                Intent settingsIntent = new Intent(CustomerMapActivity.this, CustomerProfileActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.customer_map_menu_logout:
                customerLogout();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        hideRequestUberBottomSheet();
        customerRequestUberBtn.setText("Request Uber");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRequesting && isLogging)
            mPresenter.endRide();
    }

    @Override
    protected void onDestroy() {
        customerLogout();
        removeLocationUpdateListener();

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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else {
                PermissionHelper.showGPSNotEnabledDialog(this);

            }
        }
    }

    @Override
    public void animateCamera(LatLng latLng) {
        if(latLng != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }else{
            mMap.animateCamera(CameraUpdateFactory.zoomBy(15));
        }

    }

    @Override
    public void showPath(List<LatLng> latLngList){
        LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngList){
            bounds_builder.include(latLng);
        }
        LatLngBounds bounds = bounds_builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,2));
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GRAY);
        polylineOptions.width(5f);
        polylineOptions.addAll(latLngList);
        routePolyLine = mMap.addPolyline(polylineOptions);

        // show path animations
//        PolylineOptions blackPolyOption = new PolylineOptions();
//        polylineOptions.color(Color.BLACK);
//        polylineOptions.width(5f);
//        blackPolyLine = mMap.addPolyline(blackPolyOption);

//        //Marker originMarker = addOriginDestinationMarkerAndGet(latLngList.get(0));
//        originMarker.setAnchor(0.5f, 0.5f);
//        if (mDestinationMarker != null) mDestinationMarker.remove();
//        mDestinationMarker = addOriginDestinationMarkerAndGet(latLngList.get(latLngList.size() - 1));
//        mDestinationMarker.setAnchor(0.5f, 0.5f);

//        ValueAnimator polylineAnimator = MapAnimationUtils.polylineAnimator();
//        polylineAnimator.addUpdateListener(animation -> {
//            int percentValue = (int) animation.getAnimatedValue();
//            int index = (int) (routePolyLine.getPoints().size() * (percentValue/100.0f));
//            blackPolyLine.setPoints(routePolyLine.getPoints().subList(0,index));
//        });
//        polylineAnimator.start();
    }

    @Override
    public void addPickUpMarker(LatLng pickupLocationLatLng) {
        if (pickupMarker != null) pickupMarker.remove();
        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocationLatLng).title("Pickup Here"));

    }

    @Override
    public void addDriverLocationMarker(LatLng driverLocationLatLng) {
        if (mDriverMarker != null) mDriverMarker.remove();

        mDriverMarker = mMap.addMarker(new MarkerOptions()
                .icon(driverMarkerIcon)
                .position(driverLocationLatLng)
                .title("your driver"));
    }

    @Override
    public LatLng getCustomerDestinationLatLng() {
        return customerDestinationLatLng;
    }


    @Override
    public void setRequestUberBtnText(String text) {
        customerRequestUberBtn.setText(text);
    }

    @Override
    public Location getCustomerLastLocation() {
        return mCustomerLastLocation;
    }

    @Override
    public void showDestination(){
        showMessage("You can now choose your Destination");
        mMap.setOnMapClickListener(latLng -> {
            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(latLng.latitude + " : " + latLng.longitude)
                    .icon(destinationMarkerIcon);

            if (mDestinationMarker != null) mDestinationMarker.remove();
            if (routePolyLine != null) routePolyLine.remove();

            animateCamera(latLng);
            mDestinationMarker = mMap.addMarker(markerOptions);
            customerDestinationLatLng = latLng;
            List<LatLng> locationLatLangList = new ArrayList<>();
            locationLatLangList.add(customerLastLocationLatLng);
            locationLatLangList.add(customerDestinationLatLng);
            mPresenter.getDirections(locationLatLangList);
        });
    }

    @Override
    public void cancelDestination(){
        if (mDestinationMarker != null) mDestinationMarker.remove();
        if (routePolyLine != null) routePolyLine.remove();
        customerDestinationLatLng = null;
        mMap.setOnMapClickListener(null);
        mPresenter.endRide();
    }

    @Override
    public void resetMap() {
        if (routePolyLine != null)
            routePolyLine.remove();
        if (mDriverMarker != null)
            mDriverMarker.remove();
        if (pickupMarker != null)
            pickupMarker.remove();
        if (mDestinationMarker != null)
            mDestinationMarker.remove();
    }


//    @Override
//    public void showDriverInfoBottomSheet(String foundedDriverId) {
//        mPresenter.getDriverInfo(foundedDriverId);
//        if (driverInfoSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
//            driverInfoSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//        }
//    }


    @Override
    public void setRequestingUber(boolean state) {
        isRequesting = state;
    }




    private void moveToCustomerLocation(){
        if (customerLastLocationLatLng!=null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(customerLastLocationLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }





    private void setupRequestUberBottomSheet(){
        LinearLayout bottom_sheet;
        bottom_sheet = findViewById(R.id.bottom_sheet_request_uber_layout);
        requestUberSheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        // callback for do something
        requestUberSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        setRequestUberBtnText("Close Sheet");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (isRequesting){
                            setRequestUberBtnText("Cancel Request ");
                        }else{
                            setRequestUberBtnText("Request Uber");
                        }
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;

                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        bsChooseDestinationBtn.setOnClickListener(v -> {
            mPresenter.chooseDestination();
            bsChooseDestinationBtn.setVisibility(View.GONE);
            bsCancelDestinationBtn.setVisibility(View.VISIBLE);
        });
        bsCancelDestinationBtn.setOnClickListener(v ->{
            mPresenter.cancelDestination();
            bsChooseDestinationBtn.setVisibility(View.VISIBLE);
            bsCancelDestinationBtn.setVisibility(View.GONE);
        });

        bsRequestUberBtn.setOnClickListener(v ->{
            mPresenter.requestUber();
            isRequesting=true;
        });
        // click event for show-dismiss bottom sheet
        customerRequestUberBtn.setOnClickListener(view -> {
            if (isRequesting){
                mPresenter.endRide();
                setRequestUberBtnText("Request Uber");
            }else{
                showHideRequestUberBottomSheet();
            }
        });
        requestUberSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }

    public void setupDriverInfoBottomSheet(){
        LinearLayout driver_info_bottom_sheet;
        driver_info_bottom_sheet = findViewById(R.id.bottom_sheet_driver_info_layout);
        driverInfoSheetBehavior = BottomSheetBehavior.from(driver_info_bottom_sheet);
        // callback for do something
        driverInfoSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_SETTLING:
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        // Do Something
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        // Do Something
                        showMessage("driverInfoBS is collapsed");
                    }
                    break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
        driverInfoSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    }


    public void showHideRequestUberBottomSheet(){
        if (requestUberSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            requestUberSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            setRequestUberBtnText("Close Options");
        } else {
            hideRequestUberBottomSheet();
        }
    }

    @Override
    public void hideRequestUberBottomSheet() {
        if (requestUberSheetBehavior.getState() == requestUberSheetBehavior.STATE_EXPANDED){
            requestUberSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if(!isRequesting)
                setRequestUberBtnText("Show Uber Request Options");
        }
    }


    @Override
    public void setDriverInfo(String name,String phone,Object profileImageUrl){
        if (name!=null)
            bsDriverInfoName.setText(name);
        if (phone!=null)
            getBsDriverInfoPhone.setText(phone);
        if (profileImageUrl!= null) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .into(bsDriverInfoImg);
        }
    }

    @Override
    public void removeLocationUpdateListener() {
        if (mLocationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }


    private void moveToCustomerLastLocation(){
        if (customerLastLocationLatLng != null) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLastLocationLatLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(customerLastLocationLatLng));
        }

    }

    private void customerLogout(){
        isLogging = false;
        String current_user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference()
//                .child(USERS_PATH)
//                .child(CUSTOMERS_PATH)
//                .child(current_user_uid);
//        customerRef.removeValue();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    // Required Google Maps Place Api and A billing Account
    /*@Override
    public GeoApiContext getGeoApiContext() {
        return  new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();
    }*/

}
