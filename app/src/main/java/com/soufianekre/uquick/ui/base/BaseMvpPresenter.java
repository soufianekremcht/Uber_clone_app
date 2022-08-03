package com.soufianekre.uquick.ui.base;


public interface BaseMvpPresenter<V extends BaseMvpView> {
    void onAttach(V mvpView);
    void onDetach();

}