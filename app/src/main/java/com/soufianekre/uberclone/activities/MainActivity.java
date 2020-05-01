package com.soufianekre.uberclone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.soufianekre.uberclone.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.login_page_btn)
    Button loginPageBtn;
    @BindView(R.id.sign_up_page_btn)
    Button SignUPPageBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        loginPageBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        });
        SignUPPageBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,UserRegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
