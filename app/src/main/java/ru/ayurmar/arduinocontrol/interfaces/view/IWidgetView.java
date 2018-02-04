package ru.ayurmar.arduinocontrol.interfaces.view;


import java.util.List;

import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;

public interface IWidgetView extends IBasicView {

    void showDeviceOnlineStatus(boolean isOnline);

    void showLoadingUI(boolean isLoading);

    void showAddWidgetDialog();

    void showChangeDeviceDialog(List<String> deviceList);

    void showEditWidgetDialog(FarhomeWidget widget);

    void showSendSmsDialog(String message, String phoneNumber);

//    void showWidgetList(List<IWidget> widgets);

    void updateWidgetValue(int position);

    void updateDeviceUI(FarhomeDevice device);

    void onChangeDeviceClick();

    void showWidgetList(List<FarhomeWidget> widgets);

    List<FarhomeWidget> getWidgetList();
}
