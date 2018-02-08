package ru.ayurmar.arduinocontrol.model;

import java.util.HashMap;
import java.util.Map;

public class FarhomeUser {
    private String mName;
    private Map<String, Boolean> mDevices;

    public FarhomeUser(){
    }

    public FarhomeUser(String name){
        this.mName = name;
        this.mDevices = new HashMap<>();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Map<String, Boolean> getDevices() {
        return mDevices;
    }

    public void setDevices(Map<String, Boolean> devices) {
        mDevices = devices;
    }
}
