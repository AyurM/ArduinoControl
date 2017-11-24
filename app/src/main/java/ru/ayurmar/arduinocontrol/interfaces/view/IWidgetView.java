package ru.ayurmar.arduinocontrol.interfaces.view;


import java.util.List;

import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;

public interface IWidgetView extends IBasicView {

    void showDeviceOnlineStatus(boolean isOnline);

    void showNoItemsUI(boolean isEmpty);

    void showLoadingUI(boolean isLoading);

    void showAddWidgetDialog();

    void showConfirmDeleteDialog(int position);

    void showEditWidgetDialog(IWidget widget);

    void showSendSmsDialog(String message, String phoneNumber);

    void showWidgetList(List<IWidget> widgets);

    void updateWidgetValue(int position);

    List<IWidget> getWidgetList();
}
