package ru.ayurmar.arduinocontrol.interfaces.view;


public interface IAddEditWidgetView extends IBasicView {

    String getWidgetName();

    String getWidgetPin();

//    WidgetType getWidgetType();

    void closeDialog(boolean isWidgetListChanged);
}