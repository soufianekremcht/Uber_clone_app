package com.soufianekre.uberclone.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.ui.base.BaseActivity;
import com.soufianekre.uberclone.ui.customer.CustomerMapActivity;
import com.soufianekre.uberclone.ui.login.UserLoginActivity;
import com.soufianekre.uberclone.ui.login.UserRegisterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements MainContract.View {

    //@Inject
    MainPresenter<MainContract.View> mPresenter;

    @BindView(R.id.login_page_btn)
    Button loginPageBtn;
    @BindView(R.id.sign_up_page_btn)
    Button SignUPPageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPresenter = new MainPresenter<>();
        //getActivityComponent().inject(this);
        ButterKnife.bind(this);
        mPresenter.onAttach(MainActivity.this);

        loginPageBtn.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                Intent intent = new Intent(this, CustomerMapActivity.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
                startActivity(intent);
                finish();
            }

        });
        SignUPPageBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserRegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }


    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

}