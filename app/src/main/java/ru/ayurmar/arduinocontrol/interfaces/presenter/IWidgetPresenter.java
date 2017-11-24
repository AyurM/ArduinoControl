package ru.ayurmar.arduinocontrol.interfaces.presenter;


import java.util.List;

import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;

public interface IWidgetPresenter<V extends IWidgetView> extends IBasicPresenter<V>{

    void loadWidgetListFromDb();

    void saveWidgetListToDb(List<IWidget> widgets);

    void updateWidgetInDb(IWidget widget);

    void onAddWidgetClick();

    void onEditWidgetClick(IWidget widget);

    void deleteWidget(int position);

    void onSendSmsClick(IWidget widget);

    void onWidgetValueClick(int position);

    void onDeviceStatusClick();

    boolean isDeviceOnline();
}