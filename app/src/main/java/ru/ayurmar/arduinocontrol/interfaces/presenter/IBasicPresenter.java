package ru.ayurmar.arduinocontrol.interfaces.presenter;


import ru.ayurmar.arduinocontrol.interfaces.view.IBasicView;

public interface IBasicPresenter<V extends IBasicView> {

    void onAttach(V mainView);

    void onDetach();
}