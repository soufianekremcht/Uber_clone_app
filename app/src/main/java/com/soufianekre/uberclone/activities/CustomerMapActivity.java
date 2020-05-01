package com.soufianekre.uberclone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.fragments.DriverInfoBottomSheetFragment;
import com.soufianekre.uberclone.utils.AppUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING;
import static com.soufianekre.uberclone.fragments.DriverInfoBottomSheetFragment.DRIVER_FOUND_ID;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback{

    public static final String USERS_PATH = "Users";
    public static final String DRIVER_AVAILABLE_PATH = "DriversAvailable";
    public static final String DRIVER_WORKING_PATH = "DriversWorking";
    public static final String CUSTOMER_REQUEST_PATH = "CustomerRequest";

    @BindView(R.id.customer_map_toolbar)
    Toolbar customerMapToolbar;
    @BindView(R.id.customer_request_uber_btn)
    Button customerRequestUberBtn;
    @BindView(R.id.customer_map_drawer)
    DrawerLayout customerMapDrawer;
    @BindView(R.id.customer_map_nav_view)
    NavigationView customerMapNavView;
    @BindView(R.id.customer_map_location_fab)
    FloatingActionButton customerMapLocationFab;
    //@BindView(R.id.customer_map_fab_menu)
    //SpeedDialView customerFabMenu;


    SpeedDialActionItem requestUberFab;
    SpeedDialActionItem speedDialActionItem2;
    //SpeedDialActionItem speedDialActionItem3;

    // bottom_sheet
    private Button bsRequestUberBtn;
    private Button bsRequestUberCancelDestination;
    private Button bsRequestUberChooseDestination;
    private LinearLayout bsRequestUberLayout;
    private RadioGroup bsRequestUberRadioGroup;


    private BottomSheetBehavior requestUberBottomSheetBehavior;


    private GoogleMap mMap;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location mCustomerLastLocation;
    private LatLng pickupLocation;
    private LatLng customerDestinationLatLng;


    private double mapSearchRadius = 1;
    private boolean isRequesting = false;
    private boolean driverFound = false;
    private boolean isChoosingDestination = false;
    private String driverFoundID;
    private String requestCarService;

    private GeoQuery geoQuery;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private Marker pickupMarker;
    private Marker mDriverMarker;
    private Marker destinationMarker;



    private  ActionBarDrawerToggle customerMapDrawerToggle;



    private LatLng customerLastLocationLatLng;

    private int checkForLocationCount = 0;

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()){
                customerLastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (getApplicationContext() != null ) {
                    mCustomerLastLocation = location;
                }
                if(checkForLocationCount <2){
                    moveToCustomerLocation();
                    checkForLocationCount++;
                }
            }
        }
    };

    private void moveToCustomerLocation(){
        if (customerLastLocationLatLng!=null)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(customerLastLocationLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.customer_map);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestLocationPermission();
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        customerDestinationLatLng = new LatLng(0.0,0.0);

        setupUi();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customer_map_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (customerMapDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()){
            case R.id.customer_map_menu_profile:
                Intent settingsIntent = new Intent(CustomerMapActivity.this, CustomerProfileActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.customer_map_menu_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        hideBottomSheet();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRequesting)
            endRide();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }



    private void setupUi () {
        setSupportActionBar(customerMapToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // nav view and drawer
        customerMapDrawerToggle = new ActionBarDrawerToggle(
                this,customerMapDrawer,R.string.open_drawer,R.string.close_drawer);
        customerMapDrawerToggle.syncState();
        customerMapDrawer.addDrawerListener(customerMapDrawerToggle);
        customerMapNavView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.drawer_menu_profile:{
                    return true;
                }
                case R.id.drawer_menu_settings:{
                    return true;
                }
                case R.id.drawer_menu_trip_history:{
                    return true;
                }
            }
            return false;
        });
        // fab setup

        /*requestUberFab = new SpeedDialActionItem.Builder(R.id.fab_action1
                ,R.drawable.ic_pin_drop_white_24dp)
                .setLabel("Request Uber ").create();
        speedDialActionItem2 = new SpeedDialActionItem
                .Builder(R.id.fab_action2,R.drawable.ic_home_white_24dp)
                .setLabel("Choose Location ")
                .create();

        customerFabMenu.addActionItem(requestUberFab,0);
        customerFabMenu.addActionItem(speedDialActionItem2,1);

         */
        // setup Bottom sheet

        bsRequestUberBtn = (Button) findViewById(R.id.bs_request_uber_btn);
        bsRequestUberLayout = (LinearLayout) findViewById(R.id.bottom_sheet);
        bsRequestUberRadioGroup = (RadioGroup) findViewById(R.id.bs_request_uber_radio_group);
        bsRequestUberChooseDestination =  (Button) findViewById(R.id.bs_request_uber_choose_destination_btn);
        bsRequestUberCancelDestination =  (Button) findViewById(R.id.bs_request_uber_cancel_destination_btn);
        bsRequestUberBtn.setOnClickListener(view -> {
                requestDriver();
        });
        // Bottom Sheet Behavior


        requestUberBottomSheetBehavior = BottomSheetBehavior.from(bsRequestUberLayout);


        requestUberBottomSheetBehavior.addBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case STATE_HIDDEN:
                    case STATE_DRAGGING:
                    case STATE_SETTLING:
                        break;
                    case STATE_EXPANDED:{
                        int selectedService = bsRequestUberRadioGroup.getCheckedRadioButtonId();
                        RadioButton radioButton = findViewById(selectedService);
                        if (radioButton != null)
                            requestCarService = radioButton.getText().toString();
                    }
                    case STATE_COLLAPSED: {
                    }
                    break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + newState);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        customerRequestUberBtn.setOnClickListener(view -> {
            if (isRequesting) {
                endRide();
                customerRequestUberBtn.setText("Request Uber");
            }else{
                showBottomSheet();
            }

        });
        bsRequestUberChooseDestination.setOnClickListener(view -> {
            chooseDestination();
        });

        bsRequestUberCancelDestination.setOnClickListener(view -> {
            cancelDestination();
        });
    }

    /**
     * manually opening / closing bottom sheet on button click
     */
    public void showBottomSheet() {
        if (requestUberBottomSheetBehavior.getState() != STATE_EXPANDED) {
            requestUberBottomSheetBehavior.setState(STATE_EXPANDED);
        } else {
            requestUberBottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }
    private void hideBottomSheet(){
        if (requestUberBottomSheetBehavior.getState() == STATE_EXPANDED) {
            requestUberBottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    private void chooseDestination(){
        if (isChoosingDestination) {
            isChoosingDestination = false;
            bsRequestUberChooseDestination.setText("Choose The destination");
            bsRequestUberCancelDestination.setVisibility(View.GONE);
            mMap.setOnMapClickListener(null);
        } else {
            AppUtils.showToast(this,"You can now choose your Destination");
            bsRequestUberCancelDestination.setVisibility(View.VISIBLE);
            bsRequestUberChooseDestination.setText(getString(R.string.destination_confirm));
            isChoosingDestination = true;
            mMap.setOnMapClickListener(latLng -> {
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                // Clears the previously touched position
                if (destinationMarker != null)
                    destinationMarker.remove();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                destinationMarker = mMap.addMarker(markerOptions);
                customerDestinationLatLng = latLng;
            });
        }

    }
    private void cancelDestination(){
        if (destinationMarker != null)
            destinationMarker.remove();
        bsRequestUberCancelDestination.setVisibility(View.GONE);
    }

    private void requestDriver(){
        if (destinationMarker != null){
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            isRequesting = true;
            // Put The Request in FireBase DB;
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference().child(CUSTOMER_REQUEST_PATH);

            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userId,
                    new GeoLocation(mCustomerLastLocation.getLatitude(), mCustomerLastLocation.getLongitude()),
                    (key, error) -> {
                        if (error != null)
                            AppUtils.showToast(getApplicationContext(),
                                    "driver request failed");

                        else
                            AppUtils.showToast(getApplicationContext(),
                                    "driver request is working ..");
                    });

            pickupLocation = new LatLng(mCustomerLastLocation.getLatitude(), mCustomerLastLocation.getLongitude());
            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));
            customerRequestUberBtn.setText("Cancel The Request");


            Executors.newSingleThreadExecutor().execute(()->getClosestDriver());
            hideBottomSheet();
        }else{
            AppUtils.showToast(CustomerMapActivity.this
                    ,getResources().getString(R.string.toast_check_destination_request_uber));
        }
    }

    private void getClosestDriver() {
        DatabaseReference driverLocation =
                FirebaseDatabase.getInstance().getReference()
                        .child(DRIVER_AVAILABLE_PATH);

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(
                new GeoLocation(pickupLocation.latitude, pickupLocation.longitude)
                ,mapSearchRadius);
        // to avoid bugs
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && isRequesting) {
                    driverFoundID = key;
                    DatabaseReference driverRef = FirebaseDatabase
                            .getInstance().getReference()
                            .child(USERS_PATH)
                            .child("Drivers")
                            .child(driverFoundID);

                    driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             if (dataSnapshot.exists()) {
                                 customerRequestUberBtn.setText("Looking for any Driver Available....");
                                 driverFound = true;
                                 DatabaseReference driverRef = FirebaseDatabase
                                         .getInstance().getReference()
                                         .child(USERS_PATH)
                                         .child("Drivers")
                                         .child(driverFoundID)
                                         .child(CUSTOMER_REQUEST_PATH);
                                 String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                 HashMap<String, Object> map = new HashMap<>();
                                 map.put("customerRideId", customerId);
                                 map.put(DriverMapActivity.destinationLatKey,customerDestinationLatLng.latitude);
                                 map.put(DriverMapActivity.destinationLngKey, customerDestinationLatLng.longitude);
                                 AppUtils.showToast(getApplicationContext(),"A Driver Founded");
                                 driverRef.updateChildren(map);

                                 getDriverLocation();

                             }else{
                                 AppUtils.showToast(getApplicationContext(),"this shit wont work why ?");
                             }
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError databaseError) {

                         }
                     }
                    );
                }
            }
            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    mapSearchRadius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private void getDriverLocation () {
        driverLocationRef = FirebaseDatabase.getInstance().getReference()
                .child(DRIVER_AVAILABLE_PATH)
                .child(driverFoundID)
                .child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double driverLocationLat = 0;
                    double driverLocationLng = 0;

                    if (map.get(0)  != null && map.get(1) != null) {
                        driverLocationLat = Double.parseDouble(map.get(0).toString());
                        driverLocationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLocationLatLng = new LatLng(driverLocationLat, driverLocationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocationLatLng)
                            .title("your driver"));

                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLocationLatLng.latitude);
                    loc2.setLongitude(driverLocationLatLng.longitude);

                    int distance = (int) loc1.distanceTo(loc2);
                    customerRequestUberBtn.setText(String.format("Driver Found : %s m ", distance));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        showBottomSheetDialogFragment();

    }

    public void showBottomSheetDialogFragment() {
        DriverInfoBottomSheetFragment bottomSheetFragment = new DriverInfoBottomSheetFragment();
        Bundle bundle = new Bundle();
        if (driverFoundID != null){
            bundle.putString(DRIVER_FOUND_ID,driverFoundID);
            bottomSheetFragment.setArguments(bundle);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
        }


    }

    private void requestLocationPermission() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                AppUtils.showToast(getApplicationContext(), "Location Permission Granted");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).onSameThread()
                .check();
    }

    private void endRide () {
        mapSearchRadius = 0.1;
        isRequesting = false;
        if (geoQuery != null)
            geoQuery.removeAllListeners();


        if (mDriverMarker != null)
            mDriverMarker.remove();
        if (driverLocationRefListener != null)
            driverLocationRef.removeEventListener(driverLocationRefListener);
        if (pickupMarker != null)
            pickupMarker.remove();
        if (driverFoundID != null) {
            // remove the request from database in drivers row
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                    .child(USERS_PATH)
                    .child("Drivers")
                    .child(driverFoundID)
                    .child(CUSTOMER_REQUEST_PATH);
            driverRef.removeValue();
            driverFoundID = null;
        }
        driverFound = false;
        // Remove The request from the fireBase DB
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(CUSTOMER_REQUEST_PATH)
                .child(userId);
        GeoFire refGeoFire = new GeoFire(ref);
        //refGeoFire.removeLocation(userId, null);
        ref.removeValue();
    }
}
