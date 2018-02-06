package ru.ayurmar.arduinocontrol.model;

import android.content.SharedPreferences;

import javax.inject.Inject;

import io.reactivex.Single;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;


public class AppPrefHelper implements IPrefHelper {
    private final SharedPreferences mPreferences;

    @Inject
    public AppPrefHelper(SharedPreferences preferences){
        this.mPreferences = preferences;
    }

    @Override
    public Single<String> getStringPreference(String key){
        return Single.fromCallable(() -> mPreferences.getString(key, ""));
    }

    @Override
    public void saveStringPreference(String key, String value){
        mPreferences.edit().putString(key, value).apply();
    }
}
