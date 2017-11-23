package ru.ayurmar.arduinocontrol.interfaces.model;

import io.reactivex.Single;
import okhttp3.ResponseBody;

public interface IRepository extends IDbHelper, IPrefHelper {

    Single<ResponseBody> sendValueFromWidget(IWidget widget);

    Single<ResponseBody> requestValueForWidget(IWidget widget);

    Single<ResponseBody> isDeviceOnline();

    void setAuthToken(String authToken);
}