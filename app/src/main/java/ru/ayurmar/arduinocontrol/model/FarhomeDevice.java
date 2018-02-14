package ru.ayurmar.arduinocontrol.model;


public class FarhomeDevice {
    private String mUser;
    private String mName;
    private String mModel;
    private String mId;

    public FarhomeDevice(){
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

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof FarhomeDevice)){
            return false;
        }
        return mId.equals(((FarhomeDevice) obj).getId());
    }
}