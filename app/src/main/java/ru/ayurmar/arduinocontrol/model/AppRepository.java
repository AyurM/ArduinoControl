package ru.ayurmar.arduinocontrol.model;

import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import ru.ayurmar.arduinocontrol.interfaces.model.BlynkApi;
import ru.ayurmar.arduinocontrol.interfaces.model.IDbHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;


public class AppRepository implements IRepository {

    private static final String sAuthToken = "9bcbd25ea1db4c30ad34f32fb686d768";
    private final BlynkApi mBlynkApi;
    private final IDbHelper mDbHelper;
    private final IPrefHelper mPrefHelper;

    public AppRepository(BlynkApi blynkApi, IDbHelper dbHelper, IPrefHelper prefHelper){
        this.mBlynkApi = blynkApi;
        this.mDbHelper = dbHelper;
        this.mPrefHelper = prefHelper;
    }

    @Override
    public Completable addWidget(IWidget widget){
        return mDbHelper.addWidget(widget);
    }

    @Override
    public Completable addWidgetList(List<IWidget> widgets){
        return mDbHelper.addWidgetList(widgets);
    }

    @Override
    public Completable deleteWidget(IWidget widget){
        return mDbHelper.deleteWidget(widget);
    }

    @Override
    public Single<List<IWidget>> loadWidgetList(){
        return mDbHelper.loadWidgetList();
    }

    @Override
    public Single<IWidget> loadWidget(UUID widgetID){
        return mDbHelper.loadWidget(widgetID);
    }

    @Override
    public Completable updateWidget(IWidget widget){
        return mDbHelper.updateWidget(widget);
    }

    @Override
    public Single<ResponseBody> sendValueFromWidget(IWidget widget){
        return mBlynkApi.setValue(sAuthToken, widget.getPin(),
                widget.getValue().equals(BlynkWidget.ON) ? "0" : "1");
    }

    @Override
    public Single<ResponseBody> requestValueForWidget(IWidget widget){
        return mBlynkApi.getValue(sAuthToken, widget.getPin());
    }

    @Override
    public Single<ResponseBody> isDeviceOnline(){
        return mBlynkApi.isDeviceOnline(sAuthToken);
    }

    @Override
    public Single<String> getStringPreference(String key){
        return mPrefHelper.getStringPreference(key);
    }
}