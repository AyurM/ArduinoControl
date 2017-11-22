package ru.ayurmar.arduinocontrol.model;


import java.util.Date;
import java.util.UUID;

import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;

public class BlynkWidget implements IWidget {

    private static final String ON = "ВКЛ";
    private static final String OFF = "ВЫКЛ";
    public static final String UNDEFINED = "--";

    private String mName;
    private String mPin;
    private String mValue;
    private WidgetType mWidgetType;
    private Date mLastUpdateTime;
    private UUID mId;

    public BlynkWidget(String name, String pin, String value, WidgetType type){
        this.mName = name;
        this.mPin = pin;
        this.mWidgetType = type;
        this.mId = UUID.randomUUID();
        this.mLastUpdateTime = new Date();
        setValue(value);
    }

    public BlynkWidget(String name, String pin, String value, WidgetType type,
                       String id, long date){
        this.mName = name;
        this.mPin = pin;
        this.mWidgetType = type;
        this.mId = UUID.fromString(id);
        this.mLastUpdateTime = new Date(date);
        setValue(value);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPin() {
        return mPin;
    }

    public void setPin(String pin) {
        mPin = pin;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        if(value == null){
            mValue = UNDEFINED;
            return;
        }
        if(value.equals(ON) || value.equals(OFF) || value.equals(UNDEFINED)){
            mValue = value;
            return;
        }
        if(mWidgetType == WidgetType.BUTTON){
            try{
                if(Float.parseFloat(value) == 0.0f){
                    value = OFF;
                } else {
                    value = ON;
                }
            } catch (NumberFormatException e){
                value = UNDEFINED;
            }
        }
        mValue = value;
    }

    @Override
    public WidgetType getWidgetType() {
        return mWidgetType;
    }

    @Override
    public void setWidgetType(WidgetType widgetType) {
        mWidgetType = widgetType;
    }

    @Override
    public Date getLastUpdateTime() {
        return mLastUpdateTime;
    }

    @Override
    public void setLastUpdateTime(Date lastUpdateTime) {
        mLastUpdateTime = lastUpdateTime;
    }

    @Override
    public UUID getId() {
        return mId;
    }
}