package ru.ayurmar.arduinocontrol.interfaces.model;

import io.reactivex.Scheduler;

public interface IScheduler {

    Scheduler io();

    Scheduler main();

    Scheduler computation();
}
