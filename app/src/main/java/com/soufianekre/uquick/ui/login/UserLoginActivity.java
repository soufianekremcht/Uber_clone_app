package com.soufianekre.uquick.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.soufianekre.uquick.MyApp;
import com.soufianekre.uquick.R;
import com.soufianekre.uquick.data.app_preferences.PrefConst;
import com.soufianekre.uquick.helpers.InputHelper;
import com.soufianekre.uquick.ui.customer.map.CustomerMapActivity;
import com.soufianekre.uquick.ui.driver.map.DriverMapActivity;
import com.soufianekre.uquick.ui.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.soufianekre.uquick.helpers.FirebaseConstant.CUSTOMERS_PATH;
import static com.soufianekre.uquick.helpers.FirebaseConstant.DRIVERS_PATH;

public class UserLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @BindView(R.id.login_user_email_field)
    TextInputEditText loginUserEmail;

    @BindView(R.id.login_user_password_field)
    TextInputEditText loginUserPassword;

    @BindView(R.id.login_user_radio_group)
    RadioGroup loginUserRadioGroup;

    @BindView(R.id.login_forget_password_btn)
    Button loginForgetPasswordBtn;

    @BindView(R.id.user_login_btn)
    Button userLoginBtn;

    private Class<? extends AppCompatActivity> activityToMoveTo;
    private Context  mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        MyApp.AppPref().set(PrefConst.USER_STATE,CUSTOMERS_PATH);

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
            final String email = loginUserEmail.getText().toString();
            final String password = loginUserPassword.getText().toString();

            if (InputHelper.isEmpty(loginUserEmail) || InputHelper.isEmpty(loginUserPassword)){
                loginUserEmail.setError("You should Type Some thing");
            }else
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                        UserLoginActivity.this, task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(mContext, "IT WORKING", Toast.LENGTH_SHORT).show();
                        String userState =   MyApp.AppPref().getString(PrefConst.USER_STATE,CUSTOMERS_PATH);
                        activityToMoveTo = userState.equals(CUSTOMERS_PATH) ? CustomerMapActivity.class
                                : DriverMapActivity.class;
                        Intent intent = new Intent(mContext,activityToMoveTo);
                        startActivity(intent);
                        finish();

                    }else{
                        Toasty.error(mContext, task.getException().getMessage()).show();
                    }
                });
        });

        loginForgetPasswordBtn.setOnClickListener(v -> {
            mAuth.sendPasswordResetEmail(loginUserEmail.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toasty.success(mContext, "Check Your Email").show();
                        } else {
                            Toasty.error(mContext, task.getException().getMessage()).show();
                        }

                    });
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
