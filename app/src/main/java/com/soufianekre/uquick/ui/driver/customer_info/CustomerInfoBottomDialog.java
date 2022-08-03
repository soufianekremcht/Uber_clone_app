package com.soufianekre.uquick.ui.driver.customer_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.ui.base.BaseBottomSheetFragment;

import butterknife.ButterKnife;

class CustomerInfoBottomDialog extends BaseBottomSheetFragment {

    public static final String DRIVER_FOUND_ID = "driver_found_id";
    DatabaseReference assignedDriver;

    private String dbRefDriverId =  "";

    private ValueEventListener assignedDriverValueListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkBundle();
        return inflater.inflate(R.layout.sheet_driver_info,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);


    }

    private void checkBundle(){
        dbRefDriverId = getArguments().getString(DRIVER_FOUND_ID);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAllListeners();
    }

    void removeAllListeners(){
        if (assignedDriverValueListener != null)
            assignedDriver.removeEventListener(assignedDriverValueListener);
    }
}
