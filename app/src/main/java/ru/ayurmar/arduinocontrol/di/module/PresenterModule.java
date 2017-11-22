package ru.ayurmar.arduinocontrol.di.module;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.interfaces.view.IAddEditWidgetView;
import ru.ayurmar.arduinocontrol.presenter.AddEditWidgetPresenter;
import ru.ayurmar.arduinocontrol.presenter.WidgetPresenter;
import ru.ayurmar.arduinocontrol.di.FragmentScope;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IAddEditWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;

@Module
public class PresenterModule {

    @Provides
    @FragmentScope
    IWidgetPresenter<IWidgetView> provideWidgetPresenter(IRepository repository,
                                                         CompositeDisposable disposable,
                                                         IScheduler scheduler,
                                                         Context context){
        return new WidgetPresenter<>(repository, disposable, scheduler, context);
    }

    @Provides
    @FragmentScope
    IAddEditWidgetPresenter<IAddEditWidgetView> provideAddWidgetPresenter(IRepository repository,
                                                                          CompositeDisposable disposable,
                                                                          IScheduler scheduler){
        return new AddEditWidgetPresenter<>(repository, disposable, scheduler);
    }
}