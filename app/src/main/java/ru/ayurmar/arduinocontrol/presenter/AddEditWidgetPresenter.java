package ru.ayurmar.arduinocontrol.presenter;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IAddEditWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IAddEditWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.WidgetType;


public class AddEditWidgetPresenter<V extends IAddEditWidgetView>
        extends BasicPresenter<V> implements IAddEditWidgetPresenter<V> {

    private IWidget mWidget;

    @Inject
    public AddEditWidgetPresenter(IRepository repository, CompositeDisposable disposable,
                                  IScheduler scheduler){
        super(repository, disposable, scheduler);
    }

    @Override
    public void onCancelClick(){
        getView().closeDialog(false);
    }

    @Override
    public void onOkClick(boolean isEditMode, boolean isDevMode){
        String name = getView().getWidgetName();
        String pin = getView().getWidgetPin();
        WidgetType type = getView().getWidgetType();
        if(isEditMode && !isDevMode){
            if(name == null){
                getView().showMessage(R.string.message_add_edit_empty_name_text);
                return;
            }
        } else {
            if(name == null || pin == null || type == null){
                getView().showMessage(R.string.message_add_edit_error_text);
                return;
            }
        }

        Completable addEditResult;

        if(isEditMode){
            mWidget.setName(name);
            if(isDevMode){
                mWidget.setPin(pin);
                mWidget.setWidgetType(type);
            }
//            addEditResult = getRepository().updateWidget(mWidget);
        } else {
//            mWidget = new BlynkWidget(name, pin, BlynkWidget.UNDEFINED, type);
//            addEditResult = getRepository().addWidget(mWidget);
        }

//        getDisposable().add(addEditResult
//                .subscribeOn(getScheduler().computation())
//                .observeOn(getScheduler().main())
//                .doOnError(throwable -> getView()
//                        .showMessage(R.string.message_database_error_text))
//                .subscribe(() -> getView().closeDialog(true))
//        );
    }

    @Override
    public void loadWidgetToEdit(String widgetId){
//        getDisposable().add(getRepository().loadWidget(UUID.fromString(widgetId))
//                .subscribeOn(getScheduler().computation())
//                .observeOn(getScheduler().main())
//                .subscribe(widget -> {
//                    mWidget = widget;
//                    getView().fillEditForm(mWidget);
//                },
//                        throwable -> getView()
//                                .showMessage(R.string.message_database_error_text))
//        );
    }
}