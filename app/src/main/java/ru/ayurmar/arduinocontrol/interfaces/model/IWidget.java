package ru.ayurmar.arduinocontrol.interfaces.model;

import java.util.Date;
import java.util.UUID;

import ru.ayurmar.arduinocontrol.model.WidgetType;

public interface IWidget {

    String getName();

    void setName(String name);

    String getPin();

    void setPin(String pin);

    WidgetType getWidgetType();

    void setWidgetType(WidgetType widgetType);

    String getValue();

    void setValue(String value);

    Date getLastUpdateTime();

    void setLastUpdateTime(Date date);

    UUID getId();
}