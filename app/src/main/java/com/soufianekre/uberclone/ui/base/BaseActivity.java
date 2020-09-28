package com.soufianekre.uberclone.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity implements BaseMvpView {


    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, BaseActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActivityComponent().inject(this);;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onError(int resId) {
        Log.e("Activity Error",getString(resId));
    }

    @Override
    public void onError(String message) {
        Log.e("Base Activity Error",message);
    }

    @Override
    public void onError(String tag, String message) {
        Log.e(tag,message);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int resId) {
        Toast.makeText(this,resId,Toast.LENGTH_SHORT).show();
    }
}