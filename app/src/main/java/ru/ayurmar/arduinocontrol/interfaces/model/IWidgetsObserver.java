package ru.ayurmar.arduinocontrol.interfaces.model;


import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.model.WidgetGroup;

public interface IWidgetsObserver {

    void update(FarhomeWidget widget);

    void update(WidgetGroup widgetGroup);

    void updateWidgetLoadingState(boolean isLoading);
}
