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
    public BasicPresenter(IRepository repository, CompositeDisposable disposable,
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

    protected V getBasicView() {
        return mBasicView;
    }

    protected IRepository getRepository() {
        return mRepository;
    }

    protected CompositeDisposable getDisposable(){
        return mDisposable;
    }

    protected IScheduler getScheduler(){
        return mScheduler;
    }
}
