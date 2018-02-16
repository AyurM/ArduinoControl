package ru.ayurmar.arduinocontrol.model;

public abstract class FarhomeWidget {

    private String mName;
    private String mDbkey;
    private long mTimestamp;
    private float mValue;

    public FarhomeWidget(){
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDbkey() {
        return mDbkey;
    }

    public void setDbkey(String dbkey) {
        mDbkey = dbkey;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public float getValue() {
        return mValue;
    }

    protected void setValue(float value){
        mValue = value;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof FarhomeWidget)){
            return false;
        }
        return mDbkey.equals(((FarhomeWidget) obj).getDbkey());
    }
}
