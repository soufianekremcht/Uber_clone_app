package com.soufianekre.uberclone.ui.driver;


import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.soufianekre.uberclone.ui.base.BasePresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.soufianekre.uberclone.helpers.FirebaseConstant.CUSTOMER_REQUEST_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.CUSTOMER_RIDE_ID_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.DRIVERS_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.DRIVER_AVAILABLE_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.DRIVER_WORKING_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.USERS_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.destinationLatKey;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.destinationLngKey;


public class DriverMapPresenter<V extends DriverMapMvp.View> extends BasePresenter<V> implements DriverMapMvp.Presenter<V> {

    private static final String TAG = "DriverMapPresenter";




    // ValueEventListeners
    private ValueEventListener customerPickUpRefListener;
    private ValueEventListener assignedCustomerRequestRefListener;

    private FirebaseDatabase mFireBaseDBInstance = FirebaseDatabase.getInstance();

    // database References
    private DatabaseReference refDriverAvailable = mFireBaseDBInstance.getReference(DRIVER_AVAILABLE_PATH);
    private DatabaseReference refDriverWorking = mFireBaseDBInstance.getReference(DRIVER_WORKING_PATH);

    private DatabaseReference assignedCustomerPickupRef;
    private DatabaseReference assignedCustomerRequestRef;

    private GeoFire geoFireDriverAvailable = new GeoFire(refDriverAvailable);
    private GeoFire geoFireDriverBusy = new GeoFire(refDriverWorking);

    private Handler mainThread = new Handler(Looper.getMainLooper());

    private boolean CustomerFound;



    @Override
    public void findCustomers(){
        Executors.newSingleThreadExecutor().execute(() ->{
            String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String assignedCustomerRequestRefKey = null;
            mainThread.post(() -> getMvpView().onError("Driver Map activity","Starting : assignedCustomerRef :"
                    + assignedCustomerRequestRefKey ));
            getAssignedCustomer();
        });
    }


    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        assignedCustomerRequestRef = mFireBaseDBInstance.getReference()
                .child(USERS_PATH)
                .child(DRIVERS_PATH)
                .child(driverId)
                .child(CUSTOMER_REQUEST_PATH);


        LatLng driverLastLocationLatLng = getMvpView().getDriverLastLocationLatLng();

        assignedCustomerRequestRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String customerId = dataSnapshot.child(CUSTOMER_RIDE_ID_PATH).getValue().toString();
                    checkIfDriverAvailableNow(driverLastLocationLatLng,customerId);
                    getAssignedCustomerPickupLocation(driverLastLocationLatLng,customerId);
                    getAssignedCustomerDestination(driverLastLocationLatLng);
                } else {
                    getMvpView().onError("get Assigned Customer"
                            ,"assigned Customer dataSnapshots is " + dataSnapshot.exists());
                    mainThread.post(() ->{
                        getMvpView().showMessage("Why Assigned CustomerRequest is not working !!!");
                    });
                    endRide();
                    // Looking For Customers
                    findCustomers();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                getMvpView().onError("Get Assigned Customer","Error : " + databaseError.getMessage());
            }
        };

        assignedCustomerRequestRef.addValueEventListener(assignedCustomerRequestRefListener);
    }


    private void getAssignedCustomerPickupLocation(LatLng driverLastLocationLatLng,String customerId){
        assignedCustomerPickupRef = mFireBaseDBInstance
                .getReference()
                .child(CUSTOMER_REQUEST_PATH)
                .child(customerId)
                .child("l");

        getMvpView().onError("Driver Map Presenter","assignedCustomerRef :" + assignedCustomerRequestRef );
        customerPickUpRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue() != null){
                    List<Object> locationHashMap = (List<Object>) dataSnapshot.getValue();
                    double customerPickUpLocationLat = 0;
                    double customerPickUpLocationLng = 0;

                    if(locationHashMap.get(0) != null && locationHashMap.get(1) != null){
                        customerPickUpLocationLat = Double.parseDouble(locationHashMap.get(0).toString());
                        customerPickUpLocationLng = Double.parseDouble(locationHashMap.get(1).toString());
                    }
                    LatLng pickUpLatLng = new LatLng(customerPickUpLocationLat,customerPickUpLocationLng);
                    // set Pick Up Marker



                    List<LatLng> pickUpLatLngList = new ArrayList<>();
                    pickUpLatLngList.add(pickUpLatLng);
                    pickUpLatLngList.add(driverLastLocationLatLng);
                    //show Path in The Map
                    mainThread.post(() ->{
                        getMvpView().addPickupMarker(pickUpLatLng);
                        getDirections(pickUpLatLngList);
                    });


                }else{
                    mainThread.post(() ->getMvpView().showMessage("Pick up wont work oh shit Here We go Again!!!"));
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
            }
        };
        assignedCustomerPickupRef.addValueEventListener(customerPickUpRefListener);
    }

    private void getAssignedCustomerDestination(LatLng driverLastLocationLatLng){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        assignedCustomerRequestRef = FirebaseDatabase.getInstance()
                .getReference()
                .child(USERS_PATH)
                .child(DRIVERS_PATH)
                .child(driverId)
                .child(CUSTOMER_REQUEST_PATH);

        assignedCustomerRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Map<String, Object> hashMap = (Map<String, Object>) dataSnapshot.getValue();
                    double destinationLat = 0.0;
                    double destinationLng = 0.0;

                    if(hashMap.get(destinationLngKey)!=null){
                        destinationLat = Double.parseDouble(hashMap.get(destinationLatKey).toString());
                        destinationLng = Double.parseDouble(hashMap.get(destinationLngKey).toString());
                    }
                    LatLng customerDestinationLatLng = new LatLng(destinationLat, destinationLng);
                    mainThread.post(() -> getMvpView().addDestinationMarker(customerDestinationLatLng));


                } else {
                    mainThread.post(() ->{
                        getMvpView().showMessage("What is this ,the destination fucks with me");
                        getMvpView().addPickupMarker(driverLastLocationLatLng);
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void getDirections(List<LatLng> startEndLatLngList) {
        getMvpView().showPath(startEndLatLngList);
    }

    @Override
    public void connectDriver() {
        getMvpView().startMap();
    }

    @Override
    public void disconnectDriver() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DRIVER_AVAILABLE_PATH);
        //ref.removeValue();
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, (key, error) -> { });
    }

    @Override
    public void endRide() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = mFireBaseDBInstance
                .getReference()
                .child(USERS_PATH)
                .child(DRIVERS_PATH)
                .child(userId)
                .child(CUSTOMER_REQUEST_PATH);
        driverRef.removeValue();
        getMvpView().resetMap();

        if (customerPickUpRefListener != null)
            assignedCustomerPickupRef.removeEventListener(customerPickUpRefListener);
    }


    @Override
    public void removeAllListeners() {
        removeAssignedCustomerRefListeners();
        getMvpView().removeLocationUpdatesListener();
    }
    @Override
    public void removeAssignedCustomerRefListeners() {
        if (assignedCustomerRequestRefListener != null)
            assignedCustomerRequestRef.removeEventListener(assignedCustomerRequestRefListener);
        if (customerPickUpRefListener != null)
            assignedCustomerPickupRef.removeEventListener(customerPickUpRefListener);
    }


    @Override
    public void checkIfDriverAvailableNow(LatLng driverLocationLatLng, String customerId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (customerId == null || customerId.equals("")) {
            geoFireDriverBusy.removeLocation(userId, (key, error) -> { });
            geoFireDriverAvailable.setLocation(userId, new GeoLocation(driverLocationLatLng.latitude,
                    driverLocationLatLng.longitude), (key, error) -> { });
        } else{
            geoFireDriverAvailable.removeLocation(userId, (key, error) -> { });
            geoFireDriverBusy.setLocation(userId, new GeoLocation(
                    driverLocationLatLng.latitude,
                    driverLocationLatLng.longitude
            ), (key, error) -> { });
        }
    }




}