package ru.ayurmar.arduinocontrol.di.module;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.db.SQLiteDbHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IDbHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.model.AppPrefHelper;
import ru.ayurmar.arduinocontrol.model.AppRepository;
import ru.ayurmar.arduinocontrol.model.AppScheduler;

@Module
public class WidgetModule {

    @Provides
    @Singleton
    IDbHelper provideDbHelper(Context context){
        return new SQLiteDbHelper(context);
    }

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
    IRepository provideAppRepository(IDbHelper dbHelper,
                                     IPrefHelper prefHelper){
        return new AppRepository(dbHelper, prefHelper);
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
