package ru.ayurmar.arduinocontrol.presenter;


import android.content.Context;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.Utils;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.BlynkWidget;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V> {
    
    private Context mContext;
    private boolean mIsDeviceOnline;

    @Inject
    public WidgetPresenter(IRepository repository, CompositeDisposable disposable,
                           IScheduler scheduler, Context context){
        super(repository, disposable, scheduler);
        this.mContext = context;
    }

    @Override
    public void loadWidgetListFromDb(){
        getBasicView().showLoadingUI(true);
        getDisposable().add(getRepository().loadWidgetList()
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .subscribe(widgets -> {
                    getBasicView().showLoadingUI(false);
                    getBasicView().showWidgetList(widgets);
                        },
                        throwable -> {
                    getBasicView().showLoadingUI(false);
                    getBasicView().showMessage(R.string.message_database_error_text);
                        }));
    }

    @Override
    public void saveWidgetListToDb(List<IWidget> widgets){
        getDisposable().add(getRepository().addWidgetList(widgets)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doOnError(throwable -> getBasicView()
                        .showMessage(R.string.message_database_error_text))
                .subscribe());
    }

    @Override
    public void updateWidgetInDb(IWidget widget){
        getDisposable().add(getRepository().updateWidget(widget)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doOnError(throwable -> getBasicView()
                        .showMessage(R.string.message_database_error_text))
                .subscribe());
    }

    @Override
    public void onAddWidgetClick(){
        getBasicView().showAddWidgetDialog();
    }

    @Override
    public void onEditWidgetClick(IWidget widget){
        getBasicView().showEditWidgetDialog(widget);
    }

    @Override
    public void onDeleteWidgetClick(IWidget widget){
        getDisposable().add(getRepository().deleteWidget(widget)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doOnError(throwable -> getBasicView()
                        .showMessage(R.string.message_database_error_text))
                .subscribe(() -> loadWidgetListFromDb()));
    }

    @Override
    public void onSendSmsClick(IWidget widget){
        String message = widget.getName() + " " + widget.getValue();
        String phoneNumber = "+79140508579";
        getBasicView().showSendSmsDialog(message, phoneNumber);
    }

    @Override
    public void checkDeviceOnlineStatus(){
        if(Utils.isOnline(mContext)){
            getDisposable().add(getRepository().isDeviceOnline()
                    .subscribeOn(getScheduler().io())
                    .observeOn(getScheduler().main())
                    .subscribe(response -> {
                                String responseString = response.string();
                                mIsDeviceOnline = Boolean.parseBoolean(responseString);
                                getBasicView().showDeviceOnlineStatus(mIsDeviceOnline);
                            },
                            throwable -> getBasicView().showMessage(throwable.getMessage() )));
        } else {
            getBasicView().showLongMessage(R.string.message_no_connection_text);
        }
    }

    @Override
    public void onWidgetValueClick(int position){
        if(Utils.isOnline(mContext)){
            IWidget widget = getBasicView().getWidgetList().get(position);
            getDisposable().add(getRepository().requestValueForWidget(widget)
                    .subscribeOn(getScheduler().io())
                    .observeOn(getScheduler().main())
                    .doOnSuccess(response -> updateWidgetInDb(widget))
                    .subscribe(response -> {
                                String responseString = response.string();
                                if(responseString.startsWith("[\"")){
                                    responseString = responseString
                                            .substring(2, responseString.length() - 2);
                                } else {
                                    responseString = BlynkWidget.UNDEFINED;
                                }
                                widget.setValue(responseString);
                                widget.setLastUpdateTime(new Date());
                                getBasicView().updateWidgetValue(position);},
                            throwable -> getBasicView().showMessage(throwable.getMessage() )));
        } else {
            getBasicView().showLongMessage(R.string.message_no_connection_text);
        }
    }

    @Override
    public void onDeviceStatusClick(){
        getBasicView().showLongMessage(mIsDeviceOnline ?
                R.string.message_device_online_text : R.string.message_device_offline_text);
    }

//    private void addTestWidgets(){
//        BlynkWidget widget1 = new BlynkWidget("Температура", "D5", "32",
//                WidgetType.DISPLAY);
//        BlynkWidget widget2 = new BlynkWidget("Свет (Кухня)", "D2", "1",
//                WidgetType.BUTTON);
//        List<IWidget> widgets = new ArrayList<>();
//        widgets.add(widget1);
//        widgets.add(widget2);
//        saveWidgetListToDb(widgets);
//        getBasicView().showMessage("Test widgets added");
//    }
}