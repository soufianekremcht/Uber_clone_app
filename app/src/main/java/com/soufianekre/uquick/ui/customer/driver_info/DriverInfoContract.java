package com.soufianekre.uquick.ui.customer.driver_info;

import com.soufianekre.uquick.ui.base.BaseMvpPresenter;
import com.soufianekre.uquick.ui.base.BaseMvpView;

interface DriverInfoContract {
    interface View extends BaseMvpView{
    }

    interface Presenter<V extends DriverInfoContract.View> extends BaseMvpPresenter<V> {


    }
}
