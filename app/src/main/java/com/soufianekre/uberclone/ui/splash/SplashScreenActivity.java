package com.soufianekre.uberclone.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.soufianekre.uberclone.R;
import com.soufianekre.uberclone.ui.base.BaseActivity;
import com.soufianekre.uberclone.ui.main.MainActivity;


public class SplashScreenActivity extends BaseActivity {
    Runnable r;
    Handler h;

    private ImageView appIconImageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        appIconImageView = findViewById(R.id.splash_screen_image_view);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_anim);
        appIconImageView.setAnimation(animation);

        r = this::openMainActivity;
        h = new Handler();
        h.postDelayed(r,1000);
    }

    private void openMainActivity(){
        showMessage("Now , We ARe Talking ...");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (h != null){
            h.removeCallbacks(r);
            h.removeMessages(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
