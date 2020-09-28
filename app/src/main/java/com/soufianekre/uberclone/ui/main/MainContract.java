package com.soufianekre.uberclone.ui.main;

import com.soufianekre.uberclone.ui.base.BaseMvpPresenter;
import com.soufianekre.uberclone.ui.base.BaseMvpView;

interface MainContract {
    interface View extends BaseMvpView {
    }

    interface Presenter<V extends View> extends BaseMvpPresenter<V> {

    }
}
