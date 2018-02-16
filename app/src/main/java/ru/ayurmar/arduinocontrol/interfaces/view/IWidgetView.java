package ru.ayurmar.arduinocontrol.interfaces.view;


import java.util.ArrayList;
import java.util.List;

import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.fragments.AboutDeviceDialog;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;

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

    void onLogoutClick();

    void showChangeDeviceDialog(ArrayList<String> deviceSnList, ArrayList<String> deviceNamesList);

    void showEditWidgetDialog(FarhomeWidget widget);

    void showSendSmsDialog(String message, String phoneNumber);

    void updateWidget(FarhomeWidget widget);

    void addWidget(FarhomeWidget widget);

    void deleteWidget(FarhomeWidget widget);

    void updateDeviceUI(FarhomeDevice device);

    void onChangeDeviceClick();

    void showWidgetList(List<FarhomeWidget> widgets);

    int getDeviceCount();

    List<FarhomeWidget> getWidgetList();
}
