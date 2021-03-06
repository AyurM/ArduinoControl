package ru.ayurmar.arduinocontrol.di.module;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.interfaces.model.IFirebaseHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.model.AppPrefHelper;
import ru.ayurmar.arduinocontrol.model.AppRepository;
import ru.ayurmar.arduinocontrol.model.AppScheduler;
import ru.ayurmar.arduinocontrol.model.FirebaseHelper;

@Module
public class WidgetModule {


    @Provides
    @Singleton
    SharedPreferences provideSharedPref(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Singleton
    IPrefHelper providePrefHelper(SharedPreferences preferences){
        return new AppPrefHelper(preferences);
    }

    @Provides
    @Singleton
    IFirebaseHelper provideFirebaseHelper(IScheduler scheduler,
                                          CompositeDisposable disposable){
        return new FirebaseHelper(scheduler, disposable);
    }

    @Provides
    @Singleton
    IRepository provideAppRepository(IPrefHelper prefHelper, IFirebaseHelper firebaseHelper,
                                     IScheduler scheduler){
        return new AppRepository(prefHelper, firebaseHelper, scheduler);
    }

    @Provides
    CompositeDisposable provideDisposable(){
        return new CompositeDisposable();
    }

    @Provides
    IScheduler provideScheduler(){
        return new AppScheduler();
    }
}
