package ru.ayurmar.arduinocontrol.interfaces.view;


public interface IBasicView {

    void showMessage(int stringId);

    void showMessage(String message);

    void showLongMessage(int stringId);
}