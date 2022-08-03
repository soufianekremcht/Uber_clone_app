package com.soufianekre.uquick.ui.base;


import androidx.annotation.StringRes;

public interface BaseMvpView {

    void onError(@StringRes int resId);

    void onError(String message);

    void onError(String tag,String message);

    void showMessage(String message);

    void showMessage(@StringRes int resId);

}