package ru.ayurmar.arduinocontrol.model;

import java.util.List;

import io.reactivex.Single;
import ru.ayurmar.arduinocontrol.interfaces.model.IFirebaseHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IUserDevicesObserver;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidgetsObserver;


public class AppRepository implements IRepository {

    private final IPrefHelper mPrefHelper;
    private final IFirebaseHelper mFirebaseHelper;

    public AppRepository(IPrefHelper prefHelper, IFirebaseHelper firebaseHelper){
        this.mPrefHelper = prefHelper;
        this.mFirebaseHelper = firebaseHelper;
    }

    @Override
    public Single<String> getStringPreference(String key){
        return mPrefHelper.getStringPreference(key);
    }

    @Override
    public void saveStringPreference(String key, String value){
        mPrefHelper.saveStringPreference(key, value);
    }

    @Override
    public void addUserDevicesObserver(IUserDevicesObserver observer){
        mFirebaseHelper.addUserDevicesObserver(observer);
    }

    @Override
    public void removeUserDevicesObserver(IUserDevicesObserver observer){
        mFirebaseHelper.removeUserDevicesObserver(observer);
    }

    @Override
    public void addWidgetsObserver(IWidgetsObserver observer){
        mFirebaseHelper.addWidgetsObserver(observer);
    }

    @Override
    public void removeWidgetsObserver(IWidgetsObserver observer){
        mFirebaseHelper.removeWidgetsObserver(observer);
    }

    @Override
    public void notifyDeviceObservers(){
        mFirebaseHelper.notifyDeviceObservers();
    }

    @Override
    public void notifyWidgetObservers(){
        mFirebaseHelper.notifyWidgetObservers();
    }

    @Override
    public void loadUserDevices(){
        mFirebaseHelper.loadUserDevices();
    }

    @Override
    public FarhomeDevice getCurrentDevice(){
        return mFirebaseHelper.getCurrentDevice();
    }

    @Override
    public WidgetGroup getAllWidgets(){
        return mFirebaseHelper.getAllWidgets();
    }

    @Override
    public List<FarhomeDevice> getUserDevices(){
        return mFirebaseHelper.getUserDevices();
    }

    @Override
    public void renameCurrentDevice(String name){
        mFirebaseHelper.renameCurrentDevice(name);
    }

    @Override
    public void changeDevice(String deviceId){
        mFirebaseHelper.changeDevice(deviceId);
    }

    @Override
    public void bindDeviceToUser(String deviceSn, String deviceName){
        mFirebaseHelper.bindDeviceToUser(deviceSn, deviceName);
    }

    @Override
    public void reset(){
        mFirebaseHelper.reset();
    }
}