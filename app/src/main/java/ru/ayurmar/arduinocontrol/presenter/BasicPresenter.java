package ru.ayurmar.arduinocontrol.presenter;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IBasicPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IBasicView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;


public class BasicPresenter<V extends IBasicView> implements IBasicPresenter<V> {

    private V mBasicView;
    private final IRepository mRepository;
    private final CompositeDisposable mDisposable;
    private final IScheduler mScheduler;

    @Inject
    BasicPresenter(IRepository repository, CompositeDisposable disposable,
                          IScheduler scheduler){
        this.mRepository = repository;
        this.mDisposable = disposable;
        this.mScheduler = scheduler;
    }

    @Override
    public void onAttach(V mainView) {
        mBasicView = mainView;
    }

    @Override
    public void onDetach() {
        mDisposable.clear();
        mBasicView = null;
    }

    V getView() {
        return mBasicView;
    }

    IRepository getRepository() {
        return mRepository;
    }

    CompositeDisposable getDisposable(){
        return mDisposable;
    }

    IScheduler getScheduler(){
        return mScheduler;
    }
}
