package com.soufianekre.uberclone;

import android.app.Application;

import com.soufianekre.uberclone.data.app_preferences.AppPreferencesHelper;
import com.soufianekre.uberclone.data.app_preferences.PreferencesHelper;
import com.soufianekre.uberclone.helpers.AppConst;


public class MyApp extends Application {
    private static PreferencesHelper appPreferenceHelper;
    @Override
    public void onCreate() {
        super.onCreate();
        //Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        appPreferenceHelper = new AppPreferencesHelper(this, AppConst.PREF_NAME);
    }


    public static PreferencesHelper AppPref() {
        return appPreferenceHelper;
    }
}
