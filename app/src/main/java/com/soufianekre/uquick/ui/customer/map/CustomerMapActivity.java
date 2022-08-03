package com.soufianekre.uquick.ui.customer.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.helpers.PermissionHelper;
import com.soufianekre.uquick.ui.base.BaseActivity;
import com.soufianekre.uquick.ui.customer.profile.CustomerProfileActivity;
import com.soufianekre.uquick.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.soufianekre.uquick.helpers.FirebaseConstant.CUSTOMERS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.USERS_PATH;


public class CustomerMapActivity extends BaseActivity implements CustomerMapContract.View {


    private static final int PICKUP_REQUEST_CODE = 102;
    private static final int DROP_REQUEST_CODE = 103;
    private static final String REQUEST_UBER_DIALOG_TAG = "request_uber_dialog";



    //widgets
    @BindView(R.id.customer_map_toolbar)
    Toolbar customerMapToolbar;

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

    @BindView(R.id.customer_request_ride_fab)
    ExtendedFloatingActionButton customerRequestRideFab;

    @BindView(R.id.drawer_customer_map)
    DrawerLayout customerDrawerLayout;
    @BindView(R.id.nav_view_customer)
    NavigationView navViewCustomer;

    private SupportMapFragment mapFragment;

    //presenter
    CustomerMapContract.Presenter<CustomerMapContract.View> mPresenter;

    private boolean isLogging = true;
    private boolean isRequestingUber = false;


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
            getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Drawer setup
        ActionBarDrawerToggle drawerToggle;
        drawerToggle = new ActionBarDrawerToggle(this, customerDrawerLayout,customerMapToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerToggle.syncState();
        customerDrawerLayout.addDrawerListener(drawerToggle);
        navViewCustomer.setNavigationItemSelectedListener(this);
        // fab btn
        setRequestRideFab(false);
        customerRequestRideFab.setOnClickListener(v -> {
            if (isRequestingUber){
                setRequestRideFab(false);
                mPresenter.endRide();
                showMessage("Ride Request is Canceled");

            }else{
                showRequestUberBottomSheet();
            }
        });

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
    public void onBackPressed() {
        //super.onBackPressed();

        hideRequestUberBottomSheet();
        //setRequestRideFab(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.e("Customer Activity","On Resume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e("Customer Activity","On Pause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e("Customer Activity","On Stop");
        super.onStop();
        if (isRequestingUber)
            mPresenter.endRide();
    }

    @Override
    protected void onDestroy() {
        Log.e("Customer Activity","On Destroy");
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
            isRequestingUber = true;
            mPresenter.getDirections(locationLatLangList);
        });
    }

    @Override
    public void cancelDestination(){
        if (mDestinationMarker != null) mDestinationMarker.remove();
        if (routePolyLine != null) routePolyLine.remove();
        customerDestinationLatLng = null;
        mMap.setOnMapClickListener(null);
        isRequestingUber = false;
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
        isRequestingUber = state;
    }




    private void moveToCustomerLocation(){
        if (customerLastLocationLatLng!=null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(customerLastLocationLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }





    private void setupRequestUberBottomSheet(){
        LinearLayout bottom_sheet;
        bottom_sheet = findViewById(R.id.btmsheet_request_uber_layout);
        requestUberSheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        // callback for do something
        requestUberSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        customerRequestRideFab.show();
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
            showDestination();
            bsChooseDestinationBtn.setVisibility(View.GONE);
            bsCancelDestinationBtn.setVisibility(View.VISIBLE);
        });
        bsCancelDestinationBtn.setOnClickListener(v ->{
            cancelDestination();
            bsChooseDestinationBtn.setVisibility(View.VISIBLE);
            bsCancelDestinationBtn.setVisibility(View.GONE);
        });

        bsRequestUberBtn.setOnClickListener(v ->{
            setRequestRideFab(isRequestingUber);
            customerRequestRideFab.show();
            mPresenter.requestUber();

        });
        // click event for show-dismiss bottom sheet
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
                        customerRequestRideFab.show();
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        // Do Something
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        // Do Something
                        showMessage("CustomerInfoBS is collapsed");
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


    public void showRequestUberBottomSheet(){
        if (requestUberSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            requestUberSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            hideRequestUberBottomSheet();
        }
    }

    @Override
    public void hideRequestUberBottomSheet() {
        if (requestUberSheetBehavior.getState() == requestUberSheetBehavior.STATE_EXPANDED){
            requestUberSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            //setRequestRideFab(false);
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
    @Override
    public void setRequestRideFab(boolean isRequesting){
        if (isRequesting){
            customerRequestRideFab.setText("Cancel");
            customerRequestRideFab.setIcon(
                    ResourcesCompat.getDrawable(getResources(),R.drawable.ic_close_white,null));
        }else{
            customerRequestRideFab.setText("Request");
            customerRequestRideFab.setIcon(
                    ResourcesCompat.getDrawable(getResources(),R.drawable.ic_request_ride_white,null));
        }
    }


    private void customerLogout() {

        if (isRequestingUber) {
            showMessage("You Should Cancel any Operation Before Logging out");
        }else{
            isLogging = false;
            String current_user_uid = "";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                current_user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference()
                        .child(USERS_PATH)
                        .child(CUSTOMERS_PATH)
                        .child(current_user_uid);
                customerRef.removeValue();
            }
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.drawer_menu_profile:
                Intent settingsIntent = new Intent(CustomerMapActivity.this, CustomerProfileActivity.class);
                startActivity(settingsIntent);
                closeDrawer();
                return true;
            case R.id.drawer_menu_trip_history:
                return true;
            case R.id.drawer_menu_settings:
                return true;
            case R.id.drawer_menu_logout:
                customerLogout();
                closeDrawer();
                return true;

        }

        return false;
    }

    private void closeDrawer(){
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            customerDrawerLayout.closeDrawer(GravityCompat.START);
        },250);
    }


    // Required Google Maps Place Api and A billing Account
    /*@Override
    public GeoApiContext getGeoApiContext() {
        return  new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();
    }*/

}
