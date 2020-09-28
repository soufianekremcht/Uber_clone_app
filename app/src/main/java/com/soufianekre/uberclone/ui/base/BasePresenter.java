package com.soufianekre.uberclone.ui.base;



public class BasePresenter<V extends BaseMvpView> implements BaseMvpPresenter<V> {

    private static final String TAG = "BasePresenter";

//    private final DataManager mDataManager = new AppDataManager();
//    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();


    private V mvpView;

    public BasePresenter() {
    }

    @Override
    public void onAttach(V mvpView) {
        this.mvpView = mvpView;
    }

    @Override
    public void onDetach() {
//        mCompositeDisposable.dispose();
        mvpView = null;
    }

    public boolean isViewAttached() {
        return mvpView != null;
    }

    public V getMvpView() {
        return mvpView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }


//    public DataManager getDataManager() {
//        return mDataManager;
//    }
//
//
//    public CompositeDisposable getCompositeDisposable() {
//        return mCompositeDisposable;
//    }

    public static class MvpViewNotAttachedException extends RuntimeException {
        MvpViewNotAttachedException() {
            super("Please call Presenter.onAttach(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }




}
