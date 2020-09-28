package com.soufianekre.uberclone.ui.customer;

import android.location.Location;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.soufianekre.uberclone.ui.base.BaseMvpPresenter;
import com.soufianekre.uberclone.ui.base.BaseMvpView;

import java.util.List;

public interface CustomerMapContract {


    interface View extends BaseMvpView , OnMapReadyCallback {
        Location getCustomerLastLocation();
        void animateCamera(LatLng locationLatLng);

        void showDestination();
        void cancelDestination();
        void showPath(List<LatLng> latLngList);
        void setDriverInfo(String toString, String toString1, Object profileImageUrl);
        void addPickUpMarker(LatLng pickUpLatLng);
        void addDriverLocationMarker(LatLng driverLocationLatLng);


        void setRequestUberBtnText(String text);
        void hideRequestUberBottomSheet();
        void setRequestingUber(boolean state);
        void resetMap();
        void removeLocationUpdateListener();

        //GeoApiContext getGeoApiContext();
        LatLng getCustomerDestinationLatLng();
    }

    interface Presenter<V extends View> extends BaseMvpPresenter<V> {
        void chooseDestination();
        void cancelDestination();
        void requestUber();
        void getDriverLocation(LatLng pickUpLatLng);
        void getClosestDriver(LatLng pickUpLatLng,LatLng customerDestinationLatLng);
        void getDirections(List<LatLng> startEndLatLngs);
        void endRide();
        void getDriverInfo(String foundedDriverId);


    }
}
