package com.soufianekre.uberclone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.utils.AppUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserRegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @BindView(R.id.sign_up_user_email)
    EditText signUpUserEmail;
    @BindView(R.id.sign_up_user_password)
    EditText signUpUserPassword;
    @BindView(R.id.sign_up_user_password_repeat)
    EditText signUpUserPasswordRepeat;

    @BindView(R.id.sign_up_user_radio_group)
    RadioGroup signUpUserRadioGroup;

    @BindView(R.id.user_register_btn)
    Button userRegisterBtn;

    private String userType;
    private Class activityToMoveTo = CustomerMapActivity.class;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        userType = "Customers";

        firebaseAuthListener = firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                Intent intent = new Intent(mContext,activityToMoveTo);
                startActivity(intent);
                finish();
            }
        };

        signUpUserRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.login_driver_radio_btn:
                    activityToMoveTo =  DriverMapActivity.class;
                    userType = "Drivers";

                    break;
                case R.id.login_costumer_radio_btn:
                    activityToMoveTo = CustomerMapActivity.class;
                    userType = "Customers";
                    break;

            }
        });

        userRegisterBtn.setOnClickListener(view -> {
            firebaseAuthListener = firebaseAuth -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(mContext,activityToMoveTo);
                    startActivity(intent);
                    finish();
                }
            };

            final String userEmail = signUpUserEmail.getText().toString();
            final String userPassword = signUpUserPassword.getText().toString();
            final String password_repeated = signUpUserPasswordRepeat.getText().toString();


            if (!password_repeated.equals(userPassword)){
                signUpUserPasswordRepeat.setError("The Password doesn't match, Check again.");
            }else{
                mAuth.addAuthStateListener(firebaseAuthListener);
                mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(UserRegisterActivity.this, task -> {
                    if(!task.isSuccessful()){
                        AppUtils.showToast(UserRegisterActivity.this,task.getException().getMessage());
                    }else{
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = FirebaseDatabase.getInstance()
                                .getReference(DriverMapActivity.USERS_PATH)
                                .child(userType)
                                .child(user_id);
                        current_user_db.setValue(userEmail);
                    }
                });
            }


        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuthListener != null)
            mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null)
        mAuth.removeAuthStateListener(firebaseAuthListener);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMain();
    }

    private void goToMain () {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
