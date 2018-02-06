package ru.ayurmar.arduinocontrol.interfaces.presenter;


import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;

public interface IWidgetPresenter<V extends IWidgetView> extends IBasicPresenter<V>{

    void loadDevice(String deviceSn);

    void loadWidgets(String deviceSn);

    void loadUserDevices();

    void onChangeDeviceClick();

    void updateWidgetInDb(FarhomeWidget widget);

    void onAddWidgetClick();

    void onAddDeviceClick();

    void bindDeviceToUser(String deviceSn, String deviceName);

    void onEditWidgetClick(FarhomeWidget widget);

    void deleteWidget(int position);

    void onSendSmsClick(FarhomeWidget widget);

    void onWidgetValueClick(int position);

    void onDeviceStatusClick();

    boolean isDeviceOnline();
}