package ru.ayurmar.arduinocontrol.model;


public class FarhomeDevice {
    private String mUser;
    private String mModel;

    public FarhomeDevice(){
    }

    public FarhomeDevice(String user, String model){
        this.mUser = user;
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
}