package com.soufianekre.uquick.ui.customer.map;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


import static com.soufianekre.uquick.helpers.FirebaseConstant.CUSTOMERS_REQUESTS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.CUSTOMER_REQUEST_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.CUSTOMER_RIDE_ID_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.DRIVERS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.DRIVER_AVAILABLE_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.DRIVER_LOCATION_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.USERS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.destinationLatKey;
import static com.soufianekre.uquick.helpers.FirebaseConstant.destinationLngKey;
import static com.soufianekre.uquick.ui.driver.profile.DriverProfileActivity.DRIVER_NAME;
import static com.soufianekre.uquick.ui.driver.profile.DriverProfileActivity.DRIVER_PHONE;
import static com.soufianekre.uquick.ui.driver.profile.DriverProfileActivity.DRIVER_PROFILE_IMAGE_URL;


public class CustomerMapPresenter<V extends CustomerMapContract.View>
        extends BasePresenter<V>
        implements CustomerMapContract.Presenter<V> {



    public static final String CLOSEST_DRIVER_MESSAGE = "get_Closest_driver";
    public static final String REQUEST_BTN_TEXT = "request_btn_text";


    //private GeoApiContext mGeoApiContext = null;


    private Handler mainThread = new Handler(Looper.getMainLooper());
    private double mapSearchRadius = 1;
    private boolean driverFound = false;

    private String driverFoundUID;

    private String requestCarService;

    private GeoQuery geoQuery;
    private GeoQueryEventListener geoQueryEventListener;

    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private LatLng driverLocationLatLng;

    private DatabaseReference assignedDriver;
    private ValueEventListener assignedDriverValueListener;




    public CustomerMapPresenter() {
    }

    @Override
    public void requestUber() {
        Location customerLastLocation = getMvpView().getCustomerLastLocation();
        if (getMvpView().getCustomerDestinationLatLng() != null){
            LatLng pickupLatLng = new LatLng(customerLastLocation.getLatitude(), customerLastLocation.getLongitude());
            getMvpView().animateCamera(pickupLatLng);
            getMvpView().setRequestingUber(true);

            // Put The Request in FireBase DB;
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference().child(CUSTOMERS_REQUESTS_PATH);

            GeoFire geoFire = new GeoFire(ref);



            geoFire.setLocation(userId, new GeoLocation(customerLastLocation.getLatitude(),customerLastLocation.getLongitude()),
                    (key, error) -> {
                        if (error != null)
                            getMvpView().onError("Error : "+ error.getMessage());
                        else
                            getMvpView().onError("\"You Request is Working  ...\"");
                    });

            getMvpView().addPickUpMarker(pickupLatLng);
            getMvpView().hideRequestUberBottomSheet();
            Executors.newSingleThreadExecutor().execute(() ->
                    getClosestDriver(pickupLatLng, getMvpView().getCustomerDestinationLatLng())
            );

        }else{
            getMvpView().onError(R.string.toast_check_destination_request_uber);
        }

    }



    @Override
    public void getClosestDriver(LatLng pickUpLocationLatLng,LatLng customerDestinationLatLng) {

        DatabaseReference driverLocation =
                FirebaseDatabase.getInstance().getReference()
                        .child(DRIVER_AVAILABLE_PATH);

        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(
                new GeoLocation(pickUpLocationLatLng.latitude, pickUpLocationLatLng.longitude)
                ,mapSearchRadius);

        // to avoid bugs
        geoQuery.removeAllListeners();
        geoQueryEventListener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFoundUID = key;
                    getMvpView().onError("Get Closest Driver", "Looking for any Driver Available....");
                    DatabaseReference driverRef = FirebaseDatabase
                            .getInstance().getReference()
                            .child(USERS_PATH)
                            .child(DRIVERS_PATH)
                            .child(driverFoundUID);
                    driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                driverFound = true;
                                DatabaseReference driverRef = FirebaseDatabase
                                        .getInstance().getReference()
                                        .child(USERS_PATH)
                                        .child(DRIVERS_PATH)
                                        .child(driverFoundUID)
                                        .child(CUSTOMER_REQUEST_PATH);

                                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                HashMap<String, Object> map = new HashMap<>();
                                map.put(CUSTOMER_RIDE_ID_PATH, customerId);
                                map.put(destinationLatKey, customerDestinationLatLng.latitude);
                                map.put(destinationLngKey, customerDestinationLatLng.longitude);

                                driverRef.updateChildren(map);
                                getMvpView().onError("Driver has been found");
                                mainThread.post(() -> {
                                    getDriverLocation(pickUpLocationLatLng);
                                });

                            } else {
                                mainThread.post(() -> {
                                    getMvpView().onError("Get Closest Driver ", "dataSnapShot is null during the search For Drivers");
                                    getMvpView().showMessage("Some Error Happens When Searching , dataSnapShot"
                                            + dataSnapshot.exists());
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            getMvpView().onError("Get closest Driver", databaseError.getMessage());

                        }
                    });
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
                if (!driverFound && mapSearchRadius < 20) {
                    mapSearchRadius++;
                    getClosestDriver(pickUpLocationLatLng, customerDestinationLatLng);
                } else {
                    getMvpView().onError("Get Closest Driver","We Can 't Find Any Available Driver In The This Area");

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        };
        geoQuery.addGeoQueryEventListener(geoQueryEventListener);
    }

    @Override
    public void getDriverLocation(LatLng pickupLocationLatLng) {

        driverLocationRef = FirebaseDatabase.getInstance().getReference()
                .child(DRIVER_AVAILABLE_PATH)
                .child(driverFoundUID)
                .child(DRIVER_LOCATION_PATH);

        driverLocationRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    List<LatLng> driverAndPickUpLatLngList = new ArrayList<>();
                    double driverLocationLat = 0;
                    double driverLocationLng = 0;

                    if (map.get(0)!= null && map.get(1) != null) {
                        driverLocationLat = Double.parseDouble(map.get(0).toString());
                        driverLocationLng = Double.parseDouble(map.get(1).toString());

                    }

                    driverLocationLatLng = new LatLng(driverLocationLat, driverLocationLng);
                    driverAndPickUpLatLngList.add(driverLocationLatLng);
                    driverAndPickUpLatLngList.add(pickupLocationLatLng);

                    getMvpView().addDriverLocationMarker(driverLocationLatLng);

                    // calculate The distance
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocationLatLng.latitude);
                    loc1.setLongitude(pickupLocationLatLng.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLocationLatLng.latitude);
                    loc2.setLongitude(driverLocationLatLng.longitude);
                    int distance = (int) loc1.distanceTo(loc2);
                    getMvpView().onError("Get Driver Location","Distance Between Driver and Customer :" + distance);

                    getDirections(driverAndPickUpLatLngList);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                getMvpView().onError(databaseError.getMessage());
            }
        };

        driverLocationRef.addListenerForSingleValueEvent(driverLocationRefListener);

    }



    @Override
    public void getDriverInfo(String foundedDriverId) {
        assignedDriver = FirebaseDatabase.getInstance()
                .getReference()
                .child(USERS_PATH)
                .child(DRIVERS_PATH)
                .child(foundedDriverId);
        assignedDriverValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> values = (HashMap<String,Object>) dataSnapshot.getValue();
                    Object
                            name = values.get(DRIVER_NAME),
                            phone = values.get(DRIVER_PHONE),
                            profileImageUrl = values.get(DRIVER_PROFILE_IMAGE_URL);

                    getMvpView().setDriverInfo(name.toString(),phone.toString(),profileImageUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        assignedDriver.addValueEventListener(assignedDriverValueListener);
    }

    @Override
    public void getDirections(List<LatLng> startEndLatLngs){

        getMvpView().showMessage("You Did'nt Activate Billing Account For your Directions Api");
        getMvpView().showPath(startEndLatLngs);



        /** TODO: Direction Api (Required Billing Account)
         The purpose of The Code below is to Show Precise
        Path between Two Locations using The Car Roads
         **/
        /*

        if(mGeoApiContext == null){
            mGeoApiContext = getMvpView().getGeoApiContext();
        }

        List<LatLng> directionsLatLngList = new ArrayList<>();
        com.google.maps.model.LatLng startLatLng= new
                com.google.maps.model
                        .LatLng(startEndLatLngs.get(0).latitude,startEndLatLngs.get(0).longitude);
        com.google.maps.model.LatLng endLatLng = new
                com.google.maps.model
                        .LatLng(startEndLatLngs.get(1).latitude,startEndLatLngs.get(1).longitude);


        DirectionsApiRequest directionsApiRequest = new DirectionsApiRequest(mGeoApiContext);
        directionsApiRequest.mode(TravelMode.DRIVING);
        directionsApiRequest.origin(startLatLng);
        directionsApiRequest.destination(endLatLng);
        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                DirectionsRoute[] routes = result.routes;
                getMvpView().showMessage("Routes is Empty " + routes.length);
                if (routes.length != 0){
                    Log.e("Routes","Routes get Size : " + routes.length);
                    for (DirectionsRoute route : routes) {
                        List<com.google.maps.model.LatLng> path = route.overviewPolyline.decodePath();
                        for (int i = 0; i < path.size(); i++) {
                            LatLng directionLatLng = new LatLng(path.get(i).lat, path.get(i).lng);
                            directionsLatLngList.add(directionLatLng);

                        }

                    }
                    mainThread.post(() -> {
                        getMvpView().showPath(directionsLatLngList);
                    });
                }else{
                    mainThread.post(() -> {
                        getMvpView().showMessage("Routes is Empty " + routes.length);
                    });
                }
            }



            @Override
            public void onFailure(Throwable e) {
                Log.e("DirectionRequest",e.getLocalizedMessage());
                mainThread.post(() ->{
                    getMvpView().showMessage("You Did'nt Activate Billing Account For your Directions Api");
                    getMvpView().showPath(startEndLatLngs);
                        });

            }
        });
*/
    }

    @Override
    public void endRide() {
        mapSearchRadius = 0.1;
        getMvpView().setRequestingUber(false);
        if (geoQuery != null) {
            if (geoQueryEventListener != null)
                geoQuery.removeGeoQueryEventListener(geoQueryEventListener);
            geoQuery.removeAllListeners();
        }
        removeDriverLocationRefListener();
        removeAssignedDriverRefListener();

        if (driverFoundUID != null) {
            // remove the request from database in drivers row
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                    .child(USERS_PATH)
                    .child(DRIVERS_PATH)
                    .child(driverFoundUID)
                    .child(CUSTOMER_REQUEST_PATH);
            driverRef.removeValue();
            driverFoundUID = null;
        }
        driverFound = false;
        // Remove The request from the fireBase DB
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            String current_user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference()
                    .child(CUSTOMERS_REQUESTS_PATH)
                    .child(current_user_uid);
            customerRequestRef.removeValue();
        }
        getMvpView().resetMap();
    }



    void removeAssignedDriverRefListener(){
        if (assignedDriverValueListener != null)
            assignedDriver.removeEventListener(assignedDriverValueListener);
    }
    void removeDriverLocationRefListener(){

        if (driverLocationRefListener != null)
            driverLocationRef.removeEventListener(driverLocationRefListener);

    }


}
