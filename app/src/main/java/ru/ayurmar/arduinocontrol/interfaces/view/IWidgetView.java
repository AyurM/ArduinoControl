package ru.ayurmar.arduinocontrol.interfaces.view;


import java.util.List;

import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeOldWidget;
import ru.ayurmar.arduinocontrol.fragments.AboutDeviceDialog;

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

    void showEditWidgetDialog(FarhomeOldWidget widget);

    void showSendSmsDialog(String message, String phoneNumber);

    void updateWidget(FarhomeOldWidget widget);

    void updateDeviceUI(FarhomeDevice device);

    void onChangeDeviceClick();

    void showWidgetList(List<FarhomeOldWidget> widgets);

    int getDeviceCount();

    List<FarhomeOldWidget> getWidgetList();
}
