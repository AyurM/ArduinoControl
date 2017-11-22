package ru.ayurmar.arduinocontrol.model;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;

public class AppScheduler implements IScheduler {

    @Override
    public Scheduler io(){
        return Schedulers.io();
    }

    @Override
    public Scheduler main(){
        return AndroidSchedulers.mainThread();
    }

    @Override
    public Scheduler computation(){
        return Schedulers.computation();
    }
}