package com.soufianekre.uberclone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.soufianekre.uberclone.R;


public class SplashScreenActivity extends AppCompatActivity {
    Runnable r;
    Handler h;

    private ImageView appIconImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        appIconImageView = findViewById(R.id.splash_screen_image_view);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_anim);
        appIconImageView.setAnimation(animation);

        r = () -> openMainActivity();
        h = new Handler();
        h.postDelayed(r,2000);
    }

    private void openMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }





    @Override
    protected void onStop() {
        super.onStop();
        if (h != null){
            h.removeCallbacks(r);
            h.removeMessages(0);
        }
    }
}
