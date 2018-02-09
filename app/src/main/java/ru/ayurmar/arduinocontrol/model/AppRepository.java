package ru.ayurmar.arduinocontrol.model;

import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ayurmar.arduinocontrol.interfaces.model.IDbHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IPrefHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;


public class AppRepository implements IRepository {

    private final IDbHelper mDbHelper;
    private final IPrefHelper mPrefHelper;

    public AppRepository(IDbHelper dbHelper, IPrefHelper prefHelper){
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
    public Single<String> getStringPreference(String key){
        return mPrefHelper.getStringPreference(key);
    }

    @Override
    public void saveStringPreference(String key, String value){
        mPrefHelper.saveStringPreference(key, value);
    }
}