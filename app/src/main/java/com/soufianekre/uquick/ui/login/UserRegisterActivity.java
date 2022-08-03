package com.soufianekre.uquick.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soufianekre.uquick.MyApp;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.data.app_preferences.PrefConst;
import com.soufianekre.uquick.ui.customer.map.CustomerMapActivity;
import com.soufianekre.uquick.ui.driver.map.DriverMapActivity;
import com.soufianekre.uquick.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.soufianekre.uquick.helpers.FirebaseConstant.CUSTOMERS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.DRIVERS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.USERS_PATH;

public class UserRegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @BindView(R.id.sign_up_user_name)
    EditText signUpUserName;
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
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        userType = MyApp.AppPref().getString(PrefConst.USER_STATE,CUSTOMERS_PATH);

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
                    userType = DRIVERS_PATH;
                    MyApp.AppPref().set(PrefConst.USER_STATE,userType);


                    break;
                case R.id.login_costumer_radio_btn:
                    userType = CUSTOMERS_PATH;
                    MyApp.AppPref().set(PrefConst.USER_STATE,userType);

                    break;

            }
        });

        userRegisterBtn.setOnClickListener(view -> {
            final String userName = signUpUserName.getText().toString();
            final String userEmail = signUpUserEmail.getText().toString();
            final String userPassword = signUpUserPassword.getText().toString();
            final String password_repeated = signUpUserPasswordRepeat.getText().toString();


            if (!password_repeated.equals(userPassword)){
                signUpUserPasswordRepeat.setError("The Password doesn't match, Check again.");
            }else{
                mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(UserRegisterActivity.this, task -> {
                    if(task.isSuccessful()){
                        Toasty.success(mContext,"Your Registration is complete").show();
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = FirebaseDatabase.getInstance()
                                .getReference(USERS_PATH)
                                .child(userType)
                                .child(user_id);
                        current_user_db.child("name").setValue(userName);
                        current_user_db.child("email").setValue(userEmail);
                        // move to another window
                        userType = MyApp.AppPref().getString(PrefConst.USER_STATE,CUSTOMERS_PATH);
                        activityToMoveTo = userType.equals(getString(R.string.customers)) ?
                                CustomerMapActivity.class: DriverMapActivity.class;
                        Intent intent = new Intent(mContext,activityToMoveTo);
                        startActivity(intent);
                        finish();
                    }else{
                        Toasty.error(mContext,task.getException().getMessage()).show();
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
