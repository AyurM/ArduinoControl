package ru.ayurmar.arduinocontrol.model;

import io.reactivex.Single;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;


public class AppRepository implements IRepository {

    private final IPrefHelper mPrefHelper;

    public AppRepository(IPrefHelper prefHelper){
        this.mPrefHelper = prefHelper;
    }

    @Override
    public Single<String> getStringPreference(String key){
        return mPrefHelper.getStringPreference(key);
    }

    @Override
    public void saveStringPreference(String key, String value){
        mPrefHelper.saveStringPreference(key, value);
    }
}