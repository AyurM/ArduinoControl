package ru.ayurmar.arduinocontrol.interfaces.model;


import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BlynkApi {

    @GET("{auth_token}/get/{pin}")
    Single<ResponseBody> getValue(@Path("auth_token") String auth_token,
                                  @Path("pin") String pin);

    @GET("{auth_token}/update/{pin}")
    Single<ResponseBody> setValue(@Path("auth_token") String auth_token,
                                  @Path("pin") String pin, @Query("value") String value);

    @GET("{auth_token}/isHardwareConnected")
    Single<ResponseBody> isDeviceOnline(@Path("auth_token") String auth_token);
}