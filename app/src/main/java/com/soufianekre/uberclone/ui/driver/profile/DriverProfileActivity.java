package com.soufianekre.uberclone.ui.driver.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.ui.base.BaseActivity;
import com.soufianekre.uberclone.utils.AppUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.soufianekre.uberclone.helpers.FirebaseConstant.DRIVERS_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.PROFILE_IMAGES_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.USERS_PATH;

public class DriverProfileActivity extends BaseActivity {
    public final static String DRIVER_NAME = "name";
    public final static String DRIVER_PHONE = "phone";
    public final static String DRIVER_PROFILE_IMAGE_URL = "profile_image_url";
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final String DRIVER_SERVICE = "driver_service" ;
    private Handler mainThread = new Handler(Looper.getMainLooper());

    @BindView(R.id.driver_profile_toolbar)
    Toolbar driverProfileToolbar;
    @BindView(R.id.driver_profile_image_view)
    ImageView driverProfileImageView;
    @BindView(R.id.driver_profile_name_field)
    EditText driverNameEditText;
    @BindView(R.id.driver_profile_phone_field)
    EditText driverPhoneNumEditText;

    @BindView(R.id.profile_skills_text)
    TextView profileSkillsText;
    @BindView(R.id.profile_description_text)
    TextView TextprofileDescriptionText;
    @BindView(R.id.profile_location_text)
    TextView profileLocationText;
    @BindView(R.id.profile_questions_text)
    TextView profileQuestionsText;
    @BindView(R.id.profile_fun_facts_content_text)
    TextView profileFunFactsText;

    @BindView(R.id.profile_skills_edit_btn)
    ImageView profileSkillsEditBtn;
    @BindView(R.id.profile_description_edit_btn)
    ImageView profileDescriptionEditBtn;
    @BindView(R.id.profile_location_edit_btn)
    ImageView profileLocationEditBtn;
    @BindView(R.id.profile_questions_edit_btn)
    ImageView profileQuestionsEditBtn;
    @BindView(R.id.profile_fun_facts_edit_btn)
    ImageView profileFunFactsEditBtn;






    /*
    @BindView(R.id.uber_x_radio_btn)
    AppCompatRadioButton uberXRadioBtn;
    @BindView(R.id.uber_black_radio_btn)
    AppCompatRadioButton uberBlackRadioBtn;
    @BindView(R.id.uber_truck_radio_btn)
    AppCompatRadioButton uberTruckRadioBtn;
    @BindView(R.id.driver_radio_group)
    RadioGroup driverRadioGroup;

     */

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDbRef;
    private String driverId;
    // Optimize this
    private String driverName;
    private String driverPhoneNum;
    private String driverCarService;

    private Uri resultUri;

    // valueEventListener
    private ValueEventListener valueEventListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);
        ButterKnife.bind(this);

        setupUi();
        mAuth = FirebaseAuth.getInstance();
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDriverDbRef = FirebaseDatabase.getInstance()
                .getReference(USERS_PATH)
                .child(DRIVERS_PATH)
                .child(driverId);
        Executors.newSingleThreadExecutor().execute(this::getDriverProfileInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_customer,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_menu_confirm:{
                saveUserInfo();
                finish();
                break;
            }
            case android.R.id.home:{
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUi(){
        setSupportActionBar(driverProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        driverProfileImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,GALLERY_REQUEST_CODE);
        });
    }

    private void saveUserInfo(){
        driverName = driverNameEditText.getText().toString();
        driverPhoneNum = driverPhoneNumEditText.getText().toString();
        //int selectedCar = driverRadioGroup.getCheckedRadioButtonId();
        //AppCompatRadioButton radioButton = findViewById(selectedCar);
        Map<String,Object> driverInfoMap = new HashMap<>();

        if (driverName.length() <3){
            driverNameEditText.setError(getString(R.string.short_name_error));
        }else {
            driverInfoMap.put(DRIVER_NAME, driverName);
            driverInfoMap.put(DRIVER_PHONE, driverPhoneNum);
            mDriverDbRef.updateChildren(driverInfoMap);
            if (resultUri != null) {
                StorageReference filePath = FirebaseStorage
                        .getInstance().getReference()
                        .child(PROFILE_IMAGES_PATH)
                        .child(driverId);
                Bitmap bitmap = null;

                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), resultUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();
                UploadTask uploadTask = filePath.putBytes(data);

                uploadTask.addOnFailureListener(e -> AppUtils.showToast(getApplicationContext(), e.getMessage()));

                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    Uri downloadUrl = task.getResult();
                    Map newImage = new HashMap();
                    newImage.put(DRIVER_PROFILE_IMAGE_URL, downloadUrl.toString());
                    mDriverDbRef.updateChildren(newImage);
                });
            }else{
                showMessage("the result Uri is null for some reason .");
            }
        }
    }

    private void getDriverProfileInfo(){
       valueEventListener = new  ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> values = (HashMap<String,Object>) dataSnapshot.getValue();
                    Object
                            name = values.get(DRIVER_NAME),
                            phone = values.get(DRIVER_PHONE),
                            profileImageUrl = values.get(DRIVER_PROFILE_IMAGE_URL);
                            //carService =values.get(DRIVER_SERVICE);
                    mainThread.post(() ->{
                        if (name!=null)
                            driverNameEditText.setText(name.toString());
                        if (phone!=null)
                            driverPhoneNumEditText.setText(phone.toString());
                        if (profileImageUrl!= null) {
                            Glide.with(getApplicationContext())
                                    .load(profileImageUrl)
                                    .into(driverProfileImageView);
                        }
                          /*
                    if (carService!=null){
                        switch (carService.toString()) {
                            case "UberX":{
                                uberXRadioBtn.setChecked(true);
                                break;
                            }case "UberBlack":{
                                uberBlackRadioBtn.setChecked(true);
                                break;
                            }case "UberTruck":{
                                uberTruckRadioBtn.setChecked(true);
                                break;
                            }
                        }
                    }
                     */
                    });


                }else{
                    mainThread.post(() ->{
                        showMessage("Error Happens In The Profile");
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
       };
       mDriverDbRef.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE){
            resultUri = data.getData();
            driverProfileImageView.setImageURI(resultUri);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeAllListeners();

    }

    private void removeAllListeners(){
        if (valueEventListener != null)
            mDriverDbRef.removeEventListener(valueEventListener);
    }
}
