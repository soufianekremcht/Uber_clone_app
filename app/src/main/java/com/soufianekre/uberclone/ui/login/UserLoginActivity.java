package com.soufianekre.uberclone.ui.login;

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
import com.soufianekre.uberclone.MyApp;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.data.app_preferences.PrefConst;
import com.soufianekre.uberclone.ui.customer.CustomerMapActivity;
import com.soufianekre.uberclone.ui.driver.DriverMapActivity;
import com.soufianekre.uberclone.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.soufianekre.uberclone.helpers.FirebaseConstant.CUSTOMERS_PATH;
import static com.soufianekre.uberclone.helpers.FirebaseConstant.DRIVERS_PATH;

public class UserLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @BindView(R.id.login_user_email)
    EditText loginUserEmail;
    @BindView(R.id.login_user_password)
    EditText loginUserPassword;
    @BindView(R.id.login_user_radio_group)
    RadioGroup loginUserRadioGroup;

    @BindView(R.id.user_login_btn)
    Button userLoginBtn;
    private Class activityToMoveTo;
    private Context  mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        loginUserRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.login_costumer_radio_btn:
                    MyApp.AppPref().set(PrefConst.USER_STATE,CUSTOMERS_PATH);
                    break;
                case R.id.login_driver_radio_btn:
                    MyApp.AppPref().set(PrefConst.USER_STATE,DRIVERS_PATH);
                    break;
            }
        });


        userLoginBtn.setOnClickListener(view -> {
            firebaseAuthListener = firebaseAuth -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    String userState =   MyApp.AppPref().getString(PrefConst.USER_STATE,CUSTOMERS_PATH);
                    activityToMoveTo = userState.equals(CUSTOMERS_PATH) ? CustomerMapActivity.class
                            :DriverMapActivity.class;

                    Intent intent = new Intent(mContext,activityToMoveTo);
                    startActivity(intent);
                    finish();
                }
            };


            final String email = loginUserEmail.getText().toString();
            final String password = loginUserPassword.getText().toString();

            mAuth.addAuthStateListener(firebaseAuthListener);
            if (email.length()<1|| password.length()<1){
                loginUserEmail.setError("You should Type Some thing");
            }else{
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                        UserLoginActivity.this, task -> {
                    if(!task.isSuccessful()){
                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mContext, "IT WORKING", Toast.LENGTH_SHORT).show();
//                        String user_id = mAuth.getCurrentUser().getUid();
//                        DatabaseReference current_user_db = FirebaseDatabase.getInstance()
//                                .getReference(USERS_PATH)
//                                .child(activityToMoveTo == CustomerMapActivity.class ? "Customers":"Drivers")
//                                .child(user_id);
//
//                        current_user_db.setValue(mAuth.getCurrentUser().getEmail());


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
