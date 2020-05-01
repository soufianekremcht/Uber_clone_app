package com.soufianekre.uberclone.fragments;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.activities.DriverMapActivity;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.soufianekre.uberclone.activities.DriverProfileActivity.DRIVER_NAME;
import static com.soufianekre.uberclone.activities.DriverProfileActivity.DRIVER_PHONE;
import static com.soufianekre.uberclone.activities.DriverProfileActivity.DRIVER_PROFILE_IMAGE_URL;

public class DriverInfoBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String DRIVER_FOUND_ID = "driver_found_id";
    DatabaseReference assignedDriver;
    @BindView(R.id.driver_info_name_field)
    EditText driverInfoNameField;
    @BindView(R.id.driver_info_phone_field)
    EditText driverInfoPhoneField;
    @BindView(R.id.driver_info_image_view)
    ImageView driverInfoImageView;

    private String dbRefDriverId =  "";

    public DriverInfoBottomSheetFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkBundle();
        return inflater.inflate(R.layout.bottom_sheet_driver_info,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);

        assignedDriver = FirebaseDatabase.getInstance()
                .getReference(DriverMapActivity.USERS_PATH)
                .child("Drivers")
                .child(dbRefDriverId);
        getDriverInfo();
    }

    private void checkBundle(){
        dbRefDriverId = getArguments().getString(DRIVER_FOUND_ID);

    }
    private void getDriverInfo(){

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> values = (HashMap<String,Object>) dataSnapshot.getValue();
                    Object
                            name = values.get(DRIVER_NAME),
                            phone = values.get(DRIVER_PHONE),
                            profileImageUrl = values.get(DRIVER_PROFILE_IMAGE_URL);

                    if (name!=null)
                        driverInfoNameField.setText(name.toString());
                    if (phone!=null)
                        driverInfoPhoneField.setText(phone.toString());
                    if (profileImageUrl!= null) {
                        Glide.with(getActivity())
                                .load(profileImageUrl)
                                .into(driverInfoImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        assignedDriver.addValueEventListener(eventListener);
    }
}
