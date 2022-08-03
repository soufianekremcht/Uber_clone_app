package com.soufianekre.uquick.ui.customer.map;

import android.location.Location;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.soufianekre.uquick.ui.base.BaseMvpPresenter;
import com.soufianekre.uquick.ui.base.BaseMvpView;

import java.util.List;

public interface CustomerMapContract {


    interface View extends BaseMvpView , OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
        Location getCustomerLastLocation();
        void animateCamera(LatLng locationLatLng);

        void showDestination();
        void cancelDestination();
        void showPath(List<LatLng> latLngList);
        void setDriverInfo(String toString, String toString1, Object profileImageUrl);
        void addPickUpMarker(LatLng pickUpLatLng);
        void addDriverLocationMarker(LatLng driverLocationLatLng);

        void hideRequestUberBottomSheet();
        void setRequestingUber(boolean state);
        void resetMap();
        void removeLocationUpdateListener();
        void setRequestRideFab(boolean isRequesting);

        //GeoApiContext getGeoApiContext();
        LatLng getCustomerDestinationLatLng();
    }

    interface Presenter<V extends View> extends BaseMvpPresenter<V> {
        void requestUber();
        void getDriverLocation(LatLng pickUpLatLng);
        void getClosestDriver(LatLng pickUpLatLng,LatLng customerDestinationLatLng);
        void getDirections(List<LatLng> startEndLatLngs);
        void endRide();
        void getDriverInfo(String foundedDriverId);


    }
}
