package ru.ayurmar.arduinocontrol.interfaces.view;


import java.util.List;

import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.view.AboutDeviceDialog;

public interface IWidgetView extends IBasicView {

    void showDeviceOnlineStatus(boolean isOnline);

    void showLoadingUI(boolean isLoading);

    void showLoadingUI(int loadingInfo);

    void showNoConnectionUI(boolean isConnected);

    void showAddWidgetDialog();

    void showAddDeviceDialog();

    void showRenameDeviceDialog(String currentName);

    void showAboutDeviceDialog(AboutDeviceDialog dialog);

    void onAboutDeviceClick();

    void showChangeDeviceDialog(List<String> deviceSnList, List<String> deviceNamesList);

    void showEditWidgetDialog(FarhomeWidget widget);

    void showSendSmsDialog(String message, String phoneNumber);

    void updateWidget(FarhomeWidget widget);

    void updateDeviceUI(FarhomeDevice device);

    void onChangeDeviceClick();

    void showWidgetList(List<FarhomeWidget> widgets);

    List<FarhomeWidget> getWidgetList();
}
