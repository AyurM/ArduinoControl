package ru.ayurmar.arduinocontrol.interfaces.model;

import ru.ayurmar.arduinocontrol.model.FarhomeDevice;

public interface IUserDevicesObserver {

    void update(FarhomeDevice device);

    void handleDeviceError(int errorMessage);

    void updateDeviceLoadingState(boolean isLoading);
}
