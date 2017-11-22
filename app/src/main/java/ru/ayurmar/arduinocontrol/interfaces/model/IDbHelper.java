package ru.ayurmar.arduinocontrol.interfaces.model;


import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface IDbHelper {

    Completable addWidget(IWidget widget);

    Completable addWidgetList(List<IWidget> widgets);

    Completable deleteWidget(IWidget widget);

    Single<List<IWidget>> loadWidgetList();

    Single<IWidget> loadWidget(UUID widgetID);

    Completable updateWidget(IWidget widget);
}
