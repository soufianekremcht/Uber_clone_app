package com.soufianekre.uquick.ui.main;

import com.soufianekre.uquick.ui.base.BaseMvpPresenter;
import com.soufianekre.uquick.ui.base.BaseMvpView;

interface MainContract {
    interface View extends BaseMvpView {
    }

    interface Presenter<V extends View> extends BaseMvpPresenter<V> {

    }
}
