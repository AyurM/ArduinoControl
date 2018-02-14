package ru.ayurmar.arduinocontrol.model;


public class SwitchWidget extends FarhomeWidget {

    @Override
    public void setValue(float value){
        if(value == 0.0f){
            super.setValue(value);
        } else {
            super.setValue(1.0f);
        }
    }
}
