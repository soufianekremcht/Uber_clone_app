package com.soufianekre.uberclone.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.utils.AppUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CustomerProfileActivity extends AppCompatActivity {
    public final static String CUSTOMER_NAME = "name";
    public final static String CUSTOMER_PHONE = "phone";
    public final static String CUSTOMER_PROFILE_IMAGE_URL = "profile_image_url";
    private static final int GALLERY_REQUEST_CODE = 1;

    @BindView(R.id.customer_profile_toolbar)
    Toolbar customerProfileToolbar;
    @BindView(R.id.customer_profile_image_view)
    ImageView customerProfileImageView;
    @BindView(R.id.customer_profile_name_field)
    EditText customerNameEditText;
    @BindView(R.id.customer_profile_phone_field)
    EditText customerPhoneNumEditText;

    private FirebaseAuth mAuth;
    // database ref
    private DatabaseReference mCustomerDbRef;

    // valueEventListeners
    private ValueEventListener mCustomerRefListener;
    private String userId;
    // Optimize this
    private String customerName;
    private String customerPhoneNum;

    private Uri resultUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);
        ButterKnife.bind(this);

        setupUi();

        mAuth = FirebaseAuth.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCustomerDbRef = FirebaseDatabase.getInstance()
                .getReference(CustomerMapActivity.USERS_PATH).child("Customers").child(userId);
        getUserInfo();



    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
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
        setSupportActionBar(customerProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        customerProfileImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,GALLERY_REQUEST_CODE);
        });

    }

    private void saveUserInfo(){
        customerName = customerNameEditText.getText().toString();
        customerPhoneNum = customerPhoneNumEditText.getText().toString();
        Map<String,Object> infoMap = new HashMap<>();
        infoMap.put(CUSTOMER_NAME,customerName);
        infoMap.put(CUSTOMER_PHONE,customerPhoneNum);
        mCustomerDbRef.updateChildren(infoMap);

        // error in Saving Images in firebase;
        if(resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference()
                    .child("profile_images")
                    .child(userId);
            Bitmap bitmap = null;

            try {
                if (Build.VERSION.SDK_INT < 28){
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                }else{
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

            uploadTask
                    .addOnFailureListener(e -> AppUtils.showToast(this,e.getMessage()))
                    .addOnSuccessListener(taskSnapshot -> {

                    });




            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                Uri downloadUrl = task.getResult();
                Map newImage = new HashMap();
                newImage.put(CUSTOMER_PROFILE_IMAGE_URL, downloadUrl.toString());
                mCustomerDbRef.updateChildren(newImage);
            });
            AppUtils.showToast(this,"The profile has been saved");



        }else{
            finish();
        }
    }

    private void getUserInfo(){
       mCustomerRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> values = (HashMap<String,Object>) dataSnapshot.getValue();
                    Object
                            name = values.get(CUSTOMER_NAME),
                            phone = values.get(CUSTOMER_PHONE),
                            profileImageUrl = values.get(CUSTOMER_PROFILE_IMAGE_URL);

                    if (name!=null)
                        customerNameEditText.setText(name.toString());
                    if (phone!=null)
                        customerPhoneNumEditText.setText(phone.toString());
                    if (profileImageUrl!= null) {
                        Glide.with(getApplicationContext())
                                .load(profileImageUrl)
                                .into(customerProfileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mCustomerDbRef.addValueEventListener(mCustomerRefListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_REQUEST_CODE){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            customerProfileImageView.setImageURI(resultUri);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeAllListeners();
    }

    private void removeAllListeners() {
        if(mCustomerRefListener != null)
            mCustomerDbRef.removeEventListener(mCustomerRefListener);
    }
}
