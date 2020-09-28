package com.soufianekre.uberclone.ui.driver_test;

//public class OldDriverMapActivity extends AppCompatActivity
//        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
//    public static final String USERS_PATH = "Users";
//    public static final String DRIVER_AVAILABLE_PATH = "DriversAvailable";
//    public static final String DRIVER_WORKING_PATH = "DriversWorking";
//    public static final String CUSTOMER_REQUEST_PATH = "CustomerRequest";
//
//    public static final String destinationLngKey = "destinationLng";
//    public static final String destinationLatKey = "destinationLat";
//
//    @BindView(R.id.driver_map_toolbar)
//    Toolbar driverMapToolbar;
//    @BindView(R.id.driver_condition_switch)
//    SwitchCompat driverIsWorkingSwitch;
//    @BindView(R.id.driver_map_drawer)
//    DrawerLayout driverMapDrawer;
//    @BindView(R.id.driver_map_nav_view)
//    NavigationView driverMapNavView;
//    @BindView(R.id.driver_map_location_btn)
//    FloatingActionButton driverMapLocationBtn;
//
//    private ActionBarDrawerToggle actionBarDrawerToggle;
//    private SupportMapFragment mapFragment;
//    private String customerId;
//    private boolean isLoggingOut = false;
//
//    private Polyline greyPolyLine;
//    private Polyline bluePolyLine;
//
//    private LatLng customerDestinationLatLng;
//    private LatLng driverLastLocationLatLng;
//    private LocationRequest mLocationRequest;
//    private FusedLocationProviderClient fusedLocationProviderClient;
//
//
//    private FirebaseDatabase mFireBaseDBInstance;
//    // database References
//    private DatabaseReference refDriverAvailable;
//    private DatabaseReference refDriverWorking;
//
//    private DatabaseReference assignedCustomerPickupRef;
//    private DatabaseReference assignedCustomerRequestRef;
//
//
//    //ValueEventListeners
//    private ValueEventListener customerPickUpRefListener;
//    private ValueEventListener assignedCustomerRefListener;
//
//    private GeoFire geoFireDriverAvailable,geoFireDriverWorking;
//
//
//
//
//
//    private GoogleMap mMap;
//    private Marker mPickupMarker;
//    private Marker destinationMarker;
//    private Location mDriverLastLocation;
//
//    private int checkForLocationCount = 0;
//
//
//    private LocationCallback mLocationCallback = new LocationCallback(){
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            for (Location location: locationResult.getLocations()){
//                if (getApplicationContext() != null){
//                    if (mDriverLastLocation == null ||
//                            mDriverLastLocation.getLatitude() != location.getLatitude()) {
//                        driverLastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                        mDriverLastLocation = location;
//                        /*if (checkForLocationCount<2){
//                            moveToDriverLocation();
//                            checkForLocationCount++;
//                        }*/
//                    }
//                }
//            }
//            if (mDriverLastLocation != null)
//                checkIfDriverWorkingOrAvailable(mDriverLastLocation);
//        }
//    };
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_driver_map);
//        ButterKnife.bind(this);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            requestLocationPermission();
//        }
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.driver_map_fragment);
//        if (mapFragment != null) mapFragment.getMapAsync(this);
//
//        customerId = "";
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        mFireBaseDBInstance = FirebaseDatabase.getInstance();
//        refDriverAvailable =  mFireBaseDBInstance.getReference(DRIVER_AVAILABLE_PATH);
//        refDriverWorking = mFireBaseDBInstance.getReference(DRIVER_WORKING_PATH);
//        geoFireDriverAvailable = new GeoFire(refDriverAvailable);
//        geoFireDriverWorking = new GeoFire(refDriverWorking);
//
//        setupUi();
//        getAssignedCustomer();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.driver_map_menu,menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (actionBarDrawerToggle.onOptionsItemSelected(item))
//            return true;
//        switch(item.getItemId()){
//            case R.id.driver_map_menu_logout:
//                driverLogOut();
//                break;
//            case R.id.driver_map_menu_settings:
//                Intent intent = new Intent(OldDriverMapActivity.this, DriverProfileActivity.class);
//                startActivity(intent);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//    }
//
//    @Override
//    protected void onStop() {
//        if (!isLoggingOut){
//            disconnectDriver();
//        }
//        removeAllListeners();
//        super.onStop();
//
//    }
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//
//        if (!isLocationPermissionGranted()) {
//            return;
//        }
//        fusedLocationProviderClient
//                .requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
//
//        mMap.setMyLocationEnabled(true);
//
//    }
//
//    private void moveToDriverLocation(){
//        if (driverLastLocationLatLng != null) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLastLocationLatLng));
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(driverLastLocationLatLng));
//        }
//    }
//
//
//    private void setupUi(){
//
//        setSupportActionBar(driverMapToolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//
//        driverMapLocationBtn.setOnClickListener(v -> moveToDriverLocation());
//
//        actionBarDrawerToggle = new ActionBarDrawerToggle(
//                this,driverMapDrawer,R.string.open_drawer,R.string.close_drawer);
//        actionBarDrawerToggle.syncState();
//        driverMapDrawer.addDrawerListener(actionBarDrawerToggle);
//
//        driverMapNavView.setNavigationItemSelectedListener(this);
//
//        driverIsWorkingSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
//            if (checked){
//                connectDriver();
//            }else{
//                disconnectDriver();
//            }
//        });
//
//        // change the location btn position
//        View locationButton = ((View) mapFragment.getView().findViewById(
//                Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
//        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//        // position on right bottom
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//        rlp.setMargins(0, 180, 180, 0);
//
//    }
//
//
//    private void connectDriver(){
//        if (!isLocationPermissionGranted()) {
//            return;
//        }
//        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
//        mMap.setMyLocationEnabled(true);
//
//    }
//
//    private void disconnectDriver(){
//        if (fusedLocationProviderClient != null){
//            endRide();
//            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
//            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DRIVER_AVAILABLE_PATH);
//
//            GeoFire geoFire = new GeoFire(ref);
//            geoFire.removeLocation(userId, (key, error) -> { });
//        }
//
//    }
//
//    private void driverLogOut(){
//        isLoggingOut = true;
//        disconnectDriver();
//        FirebaseAuth.getInstance().signOut();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
//    }
//    private void endRide(){
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference driverRef = mFireBaseDBInstance
//                .getReference()
//                .child("MyUsers")
//                .child("Drivers")
//                .child(userId)
//                .child(CUSTOMER_REQUEST_PATH);
//        driverRef.removeValue();
//        customerId = "";
//        clearRouteMap();
//        if (mPickupMarker != null)
//            mPickupMarker.remove();
//        if (destinationMarker != null)
//            destinationMarker.remove();
//
//        if (customerPickUpRefListener != null)
//            assignedCustomerPickupRef.removeEventListener(customerPickUpRefListener);
//
//    }
//
//
//    private void checkIfDriverWorkingOrAvailable(Location location){
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        if (customerId.equals("")) {
//            geoFireDriverWorking.removeLocation(userId, (key, error) -> { });
//            geoFireDriverAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> { });
//        } else {
//            geoFireDriverAvailable.removeLocation(userId, (key, error) -> { });
//            geoFireDriverWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> { });
//        }
//    }
//
//
//    private void getAssignedCustomer(){
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        assignedCustomerRequestRef = mFireBaseDBInstance.getReference()
//                .child(USERS_PATH)
//                .child("Drivers")
//                .child(driverId)
//                .child(CUSTOMER_REQUEST_PATH)
//                .child("customerRideId");
//        assignedCustomerRefListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    customerId = dataSnapshot.getValue().toString();
//                    getAssignedCustomerPickupLocation();
//                    getAssignedCustomerDestination();
//
//                }else{
//                    endRide();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {}
//        };
//
//        assignedCustomerRequestRef.addValueEventListener(assignedCustomerRefListener);
//    }
//
//
//    private void getAssignedCustomerPickupLocation(){
//        assignedCustomerPickupRef = mFireBaseDBInstance
//                .getReference()
//                .child(CUSTOMER_REQUEST_PATH)
//                .child(customerId)
//                .child("l");
//        customerPickUpRefListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    List<Object> locationHashMap = (List<Object>) dataSnapshot.getValue();
//                    double pickUpLocationLat = 0;
//                    double pickUpLocationLng = 0;
//                    if(locationHashMap.get(0) != null && locationHashMap.get(1) != null){
//                        pickUpLocationLat = Double.parseDouble(locationHashMap.get(0).toString());
//                        pickUpLocationLng = Double.parseDouble(locationHashMap.get(1).toString());
//                    }
//                    LatLng pickUpLatLng = new LatLng(pickUpLocationLat,pickUpLocationLng);
//                    mPickupMarker = mMap.addMarker(new MarkerOptions()
//                            .position(pickUpLatLng)
//                            .title("pickup location"));
//                    List<LatLng> pickUpLatLngList = new ArrayList<>();
//                    pickUpLatLngList.add(pickUpLatLng);
//                    getRoutingToMarker(pickUpLatLngList);
//
//                }else{
//                    AppUtils.showToast(getApplicationContext(),"Pick up wont work oh shitt!!!");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NotNull DatabaseError databaseError) {
//            }
//        };
//        assignedCustomerPickupRef.addValueEventListener(customerPickUpRefListener);
//    }
//
//    private void getAssignedCustomerDestination(){
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        assignedCustomerRequestRef = FirebaseDatabase.getInstance()
//                .getReference()
//                .child(USERS_PATH)
//                .child("Drivers")
//                .child(driverId)
//                .child(CUSTOMER_REQUEST_PATH);
//
//        assignedCustomerRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    Map<String, Object> hashMap = (Map<String, Object>) dataSnapshot.getValue();
//                    double destinationLat = 0.0;
//                    double destinationLng = 0.0;
//
//                    if(hashMap.get(destinationLngKey)!=null){
//
//                        destinationLat = Double.parseDouble(hashMap.get(destinationLatKey).toString());
//                        destinationLng = Double.parseDouble(hashMap.get(destinationLngKey).toString());
//                    }
//                    customerDestinationLatLng = new LatLng(destinationLat, destinationLng);
//                    destinationMarker = mMap.addMarker(new MarkerOptions()
//                            .position(customerDestinationLatLng)
//                            .title("Destination location"));
//                } else {
//                    AppUtils.showToast(getApplicationContext(),"What is this ,the destination fucks with me");
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }
//    private void removeAllListeners(){
//        if (assignedCustomerRefListener != null)
//            assignedCustomerRequestRef.removeEventListener(assignedCustomerRefListener);
//        if (customerPickUpRefListener != null)
//            assignedCustomerPickupRef.removeEventListener(customerPickUpRefListener);
//        if (mLocationCallback != null)
//            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
//    }
//
//
//    private void getRoutingToMarker(List<LatLng> pickupLatLng){
//        LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
//        for (LatLng latLng : pickupLatLng){
//            bounds_builder.include(latLng);
//        }
//        LatLngBounds bounds = bounds_builder.build();
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,2));
//        PolylineOptions polylineOptions = new PolylineOptions();
//        polylineOptions.color(Color.GRAY);
//        polylineOptions.width(5f);
//        polylineOptions.addAll(pickupLatLng);
//        greyPolyLine = mMap.addPolyline(polylineOptions);
//
////        PolylineOptions bluePolylineOptions = new PolylineOptions();
////        polylineOptions.color(Color.GRAY);
////        polylineOptions.width(5f);
////        bluePolyLine = mMap.addPolyline(bluePolylineOptions);
//
////
////        val builder = LatLngBounds.Builder()
////        for (latLng in latLngList) {
////            builder.include(latLng)
////        }
////        val bounds = builder.build()
////        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))
////        val polylineOptions = PolylineOptions()
////        polylineOptions.color(Color.GRAY)
////        polylineOptions.width(5f)
////        polylineOptions.addAll(latLngList)
////        greyPolyLine = googleMap.addPolyline(polylineOptions)
////
////        val blackPolylineOptions = PolylineOptions()
////        blackPolylineOptions.width(5f)
////        blackPolylineOptions.color(Color.BLACK)
////        blackPolyline = googleMap.addPolyline(blackPolylineOptions)
////
////        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
////        originMarker?.setAnchor(0.5f, 0.5f)
////        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
////        destinationMarker?.setAnchor(0.5f, 0.5f)
//
//        AppUtils.showToast(this,"Rooting should Works");
//
//
//    }
//    private void clearRouteMap() {
//        if (greyPolyLine != null)
//        greyPolyLine.remove();
//    }
//
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.drawer_menu_settings: {
//                break;
//            }
//            case R.id.drawer_menu_profile: {
//
//                break;
//            }
//            case R.id.drawer_menu_trip_history: {
//                break;
//            }
//
//        }
//        driverMapDrawer.closeDrawer(GravityCompat.START);
//        return false;
//    }
//
//
//    private void requestLocationPermission(){
//        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
//            @Override
//            public void onPermissionsChecked(MultiplePermissionsReport report) {
//                Toast.makeText(getApplicationContext(),"Location Permission Granted",Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                token.continuePermissionRequest();
//            }
//        }).onSameThread().check();
//    }
//
//
//
//    private boolean isLocationPermissionGranted(){
//        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED;
//    }
//}
