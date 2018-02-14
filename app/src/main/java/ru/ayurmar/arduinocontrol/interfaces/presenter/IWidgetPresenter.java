package ru.ayurmar.arduinocontrol.interfaces.presenter;


import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.model.FarhomeOldWidget;

public interface IWidgetPresenter<V extends IWidgetView> extends IBasicPresenter<V>{

    void loadDevice(String deviceSn);

    void loadWidgets(String deviceSn);

    void loadUserDevices();

    void onChangeDeviceClick();

    void onAboutDeviceClick();

    void renameCurrentDevice(String newName);

    void updateWidgetInDb(FarhomeOldWidget widget);

    void onAddWidgetClick();

    void onAddDeviceClick();

    void bindDeviceToUser(String deviceSn, String deviceName);

    void onEditWidgetClick(FarhomeOldWidget widget);

    void deleteWidget(int position);

    void onSendSmsClick(FarhomeOldWidget widget);

    void onWidgetValueClick(int position);

    void onDeviceStatusClick();

    int getDeviceCount();

//    boolean isDeviceOnline();
}