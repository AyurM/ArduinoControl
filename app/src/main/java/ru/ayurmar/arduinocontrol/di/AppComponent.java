package ru.ayurmar.arduinocontrol.di;


import javax.inject.Singleton;

import dagger.Component;
import ru.ayurmar.arduinocontrol.di.module.AppModule;
import ru.ayurmar.arduinocontrol.di.module.WidgetModule;

@Component(modules = {AppModule.class, WidgetModule.class})
@Singleton
public interface AppComponent {

    FragmentComponent getFragmentComponent();
}