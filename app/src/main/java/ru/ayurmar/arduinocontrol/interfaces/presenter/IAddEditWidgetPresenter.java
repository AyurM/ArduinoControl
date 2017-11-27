package ru.ayurmar.arduinocontrol.interfaces.presenter;


import ru.ayurmar.arduinocontrol.interfaces.view.IAddEditWidgetView;

public interface IAddEditWidgetPresenter<V extends IAddEditWidgetView>
        extends IBasicPresenter<V> {

    void onCancelClick();

    void onOkClick(boolean isEditMode, boolean isDevMode);

    void loadWidgetToEdit(String widgetId);
}