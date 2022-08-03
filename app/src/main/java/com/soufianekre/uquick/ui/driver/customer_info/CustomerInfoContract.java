package com.soufianekre.uquick.ui.driver.customer_info;

import com.soufianekre.uquick.ui.base.BaseMvpPresenter;
import com.soufianekre.uquick.ui.base.BaseMvpView;
import com.soufianekre.uquick.ui.customer.map.CustomerMapContract;

interface CustomerInfoContract {

    interface View extends BaseMvpView{
    }

    interface Presenter<V extends CustomerMapContract.View> extends BaseMvpPresenter<V> {

    }
}
