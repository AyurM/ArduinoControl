package ru.ayurmar.arduinocontrol.model;


public class FarhomeWidget {
    private String mName;
    private String mValue;
    private long mTimestamp;

    public FarhomeWidget(){
    }

    public FarhomeWidget(String name, String value, long timestamp) {
        mName = name;
        mValue = value;
        mTimestamp = timestamp;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }
}
