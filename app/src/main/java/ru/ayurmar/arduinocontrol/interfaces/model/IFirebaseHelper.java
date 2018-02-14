package ru.ayurmar.arduinocontrol.interfaces.model;

import java.util.List;

import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.WidgetGroup;

public interface IFirebaseHelper {

    void addUserDevicesObserver(IUserDevicesObserver observer);

    void removeUserDevicesObserver(IUserDevicesObserver observer);

    void addWidgetsObserver(IWidgetsObserver observer);

    void removeWidgetsObserver(IWidgetsObserver observer);

    void notifyDeviceObservers();

    void notifyWidgetObservers();

    void loadUserDevices();

    List<FarhomeDevice> getUserDevices();

    FarhomeDevice getCurrentDevice();

    WidgetGroup getAllWidgets();

    void renameCurrentDevice(String name);

    void bindDeviceToUser(String deviceSn, String deviceName);

    void changeDevice(String deviceId);

    void reset();
}
