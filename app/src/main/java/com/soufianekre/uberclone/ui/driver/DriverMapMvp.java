package com.soufianekre.uberclone.ui.driver;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.soufianekre.uberclone.ui.base.BaseMvpPresenter;
import com.soufianekre.uberclone.ui.base.BaseMvpView;

import java.util.List;

public interface DriverMapMvp{

    interface Presenter<V extends DriverMapMvp.View> extends BaseMvpPresenter<V> {
        void connectDriver();
        void disconnectDriver();
        void findCustomers();
        void getDirections(List<LatLng> startEndLatLngList);
        void checkIfDriverAvailableNow(LatLng driverLocationLatLng, String customerId);

        void removeAllListeners();
        void removeAssignedCustomerRefListeners();
        void endRide();



    }

    interface View extends BaseMvpView, OnMapReadyCallback,
            NavigationView.OnNavigationItemSelectedListener{
        void addPickupMarker(LatLng latLng);
        void addDestinationMarker(LatLng latLng);
        void showPath(List<LatLng> startEndLatLngList);
        void startMap();
        void resetMap();

        void removeLocationUpdatesListener();
        FusedLocationProviderClient getFusedLocationProviderClient();
        LatLng getDriverLastLocationLatLng();
        void driverLogOut();
    }

}
