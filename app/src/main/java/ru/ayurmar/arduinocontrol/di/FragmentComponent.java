package ru.ayurmar.arduinocontrol.di;

import dagger.Subcomponent;
import ru.ayurmar.arduinocontrol.fragments.AddEditWidgetFragment;
import ru.ayurmar.arduinocontrol.fragments.WidgetFragment;
import ru.ayurmar.arduinocontrol.di.module.PresenterModule;

@Subcomponent(modules = {PresenterModule.class})
@FragmentScope
public interface FragmentComponent {

    void inject(WidgetFragment fragment);

    void inject(AddEditWidgetFragment fragment);
}