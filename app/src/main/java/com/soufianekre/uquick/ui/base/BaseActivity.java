package com.soufianekre.uquick.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;

public class BaseActivity extends AppCompatActivity implements BaseMvpView {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onError(int resId) {
        onError(getString(resId));
    }

    @Override
    public void onError(String message) {
        Toasty.error(this,message,Toast.LENGTH_SHORT).show();
        Log.e("Base Activity Error",message);
    }

    @Override
    public void onError(String tag, String message) {
        Log.e(tag,message);
    }

    @Override
    public void showMessage(String message) {
        Toasty.info(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getString(resId));
    }
}