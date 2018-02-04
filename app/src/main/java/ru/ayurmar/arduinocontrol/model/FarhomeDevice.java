package ru.ayurmar.arduinocontrol.model;


public class FarhomeDevice {
    private String mUser;
    private String mName;
    private String mModel;

    public FarhomeDevice(){
    }

    public FarhomeDevice(String user, String name, String model){
        this.mUser = user;
        this.mName = name;
        this.mModel = model;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}