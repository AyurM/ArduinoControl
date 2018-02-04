package ru.ayurmar.arduinocontrol;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import ru.ayurmar.arduinocontrol.di.AppComponent;
import ru.ayurmar.arduinocontrol.di.DaggerAppComponent;
import ru.ayurmar.arduinocontrol.di.module.AppModule;
import ru.ayurmar.arduinocontrol.di.FragmentComponent;
import ru.ayurmar.arduinocontrol.di.module.WidgetModule;


public class MainApp extends Application {

    protected static MainApp sInstance;

    private AppComponent mAppComponent;
    private FragmentComponent mFragmentComponent;

    @Override
    public void onCreate(){
        super.onCreate();
        sInstance = this;

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .widgetModule(new WidgetModule())
                .build();
    }

    public static MainApp getInstance(){
        return sInstance;
    }

    public FragmentComponent getFragmentComponent(){
        if(mFragmentComponent == null){
            mFragmentComponent = mAppComponent.getFragmentComponent();
        }

        return mFragmentComponent;
    }

    public void clearFragmentComponent(){
        mFragmentComponent = null;
    }
}