package ru.ayurmar.arduinocontrol.interfaces.view;


import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.WidgetType;

public interface IAddEditWidgetView extends IBasicView {

    String getWidgetName();

    String getWidgetPin();

    WidgetType getWidgetType();

    void fillEditForm(IWidget widget);

    void closeDialog(boolean isWidgetListChanged);
}