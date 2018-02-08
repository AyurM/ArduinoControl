package ru.ayurmar.arduinocontrol.interfaces.model;


import io.reactivex.Single;

public interface IPrefHelper {

    Single<String> getStringPreference(String key);

    void saveStringPreference(String key, String value);
}