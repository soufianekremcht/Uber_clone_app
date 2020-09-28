package com.soufianekre.uberclone.data.app_preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public interface PreferencesHelper {

    <T> void set(@NonNull String key, @Nullable T value);
    String getString(@NonNull String key,String def);

    int getInt(@NonNull String key,int def);
    long getLong(@NonNull String key);
    boolean getBoolean(@NonNull String key,boolean def);
    float getFloat(@NonNull String key);
    boolean isExist(@NonNull String key);
    void clearKey(@NonNull String key);
    Map<String, ?> getAll();

}
