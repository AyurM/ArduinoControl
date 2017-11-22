package ru.ayurmar.arduinocontrol.di.module;


import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import ru.ayurmar.arduinocontrol.db.SQLiteDbHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.BlynkApi;
import ru.ayurmar.arduinocontrol.interfaces.model.IDbHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
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
    BlynkApi provideBlynkApi(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://blynk-cloud.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(BlynkApi.class);
    }

    @Provides
    @Singleton
    IRepository provideAppRepository(BlynkApi tmdbApi, IDbHelper dbHelper){
        return new AppRepository(tmdbApi, dbHelper);
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
