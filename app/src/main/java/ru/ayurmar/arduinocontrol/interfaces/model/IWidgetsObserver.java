package ru.ayurmar.arduinocontrol.interfaces.model;


import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.model.WidgetGroup;

public interface IWidgetsObserver {

    void update(WidgetGroup widgetGroup);

    void update(RxFirebaseChildEvent<? extends FarhomeWidget> event);

    void updateWidgetLoadingState(boolean isLoading);
}
