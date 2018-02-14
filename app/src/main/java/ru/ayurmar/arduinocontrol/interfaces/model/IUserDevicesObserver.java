package ru.ayurmar.arduinocontrol.interfaces.model;


import java.util.List;

import ru.ayurmar.arduinocontrol.model.FarhomeDevice;

public interface IUserDevicesObserver {

    void update(List<FarhomeDevice> devices);

    void update(FarhomeDevice device);
}
