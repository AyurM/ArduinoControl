package ru.ayurmar.arduinocontrol.presenter;


import android.content.Context;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import ru.ayurmar.arduinocontrol.PreferencesActivity;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.Utils;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.BlynkWidget;
import ru.ayurmar.arduinocontrol.model.WidgetType;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V> {

    private static final int TIMEOUT_DURATION_S = 10;
    
    private Context mContext;
    private boolean mIsDeviceOnline;

    @Inject
    public WidgetPresenter(IRepository repository, CompositeDisposable disposable,
                           IScheduler scheduler, Context context){
        super(repository, disposable, scheduler);
        this.mContext = context;
        getDisposable().add(getRepository()
                .getStringPreference(PreferencesActivity.KEY_PREF_AUTH_TOKEN)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doAfterSuccess(token -> onDeviceStatusClick())
                .subscribe(token -> getRepository().setAuthToken(token),
                        throwable -> getView()
                                .showMessage(R.string.message_error_auth_token_text)));
    }

    @Override
    public void loadWidgetListFromDb(){
        getView().showLoadingUI(true);
        getDisposable().add(getRepository().loadWidgetList()
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .subscribe(widgets -> {
                    getView().showLoadingUI(false);
                    getView().showWidgetList(widgets);
                        },
                        throwable -> {
                    getView().showLoadingUI(false);
                    getView().showMessage(R.string.message_database_error_text);
                        }));
    }

    @Override
    public void saveWidgetListToDb(List<IWidget> widgets){
        getDisposable().add(getRepository().addWidgetList(widgets)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doOnError(throwable -> getView()
                        .showMessage(R.string.message_database_error_text))
                .subscribe());
    }

    @Override
    public void updateWidgetInDb(IWidget widget){
        getDisposable().add(getRepository().updateWidget(widget)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doOnError(throwable -> getView()
                        .showMessage(R.string.message_database_error_text))
                .subscribe());
    }

    @Override
    public void onAddWidgetClick(){
        getView().showAddWidgetDialog();
    }

    @Override
    public void onEditWidgetClick(IWidget widget){
        getView().showEditWidgetDialog(widget);
    }

    @Override
    public void onDeleteWidgetClick(IWidget widget){
        getDisposable().add(getRepository().deleteWidget(widget)
                .subscribeOn(getScheduler().computation())
                .observeOn(getScheduler().main())
                .doOnError(throwable -> getView()
                        .showMessage(R.string.message_database_error_text))
                .subscribe(() -> loadWidgetListFromDb()));
    }

    @Override
    public void onSendSmsClick(IWidget widget){
        String message = widget.getName() + " " + widget.getValue(); //заменить на команды для GSM-модуля
        getDisposable().add(getRepository()
                .getStringPreference(PreferencesActivity.KEY_PREF_PHONE_NUMBER)
                .subscribeOn(getScheduler().io())
                .observeOn(getScheduler().main())
                .subscribe(phoneNumber -> {
                    if(phoneNumber == null){
                        getView().showMessage(R.string.message_no_phone_number_text);
                    } else {
                        if(isCorrectPhoneNumber(phoneNumber)){
                            getView().showSendSmsDialog(message, phoneNumber);
                        } else {
                            getView()
                                    .showMessage(R.string.message_wrong_number_format_error_text);
                        }
                    }
                },
                        throwable -> getView()
                                .showMessage(R.string.message_error_phone_number_text))
        );
    }

    @Override
    public void onWidgetValueClick(int position){
        IWidget widget = getView().getWidgetList().get(position);
        WidgetType widgetType = widget.getWidgetType();
        if(widgetType == WidgetType.ALARM_SENSOR){
            return;
        }
        if(Utils.isOnline(mContext)){
            widget.setValueLoading(true);
            getView().updateWidgetValue(position);  //включить анимацию загрузки значения
            Single<ResponseBody> blynkRequest;
            if(widgetType == WidgetType.DISPLAY){
                blynkRequest = getRepository().requestValueForWidget(widget);
            } else {
                blynkRequest = getRepository().sendValueFromWidget(widget);
            }
            getDisposable().add(blynkRequest
                    .subscribeOn(getScheduler().io())
                    .timeout(TIMEOUT_DURATION_S, TimeUnit.SECONDS)
                    .observeOn(getScheduler().main())
                    .doOnSuccess(response -> updateWidgetInDb(widget))
                    .subscribe(response -> {
                                String responseString = response.string();
                                if(widgetType == WidgetType.DISPLAY){
                                    handleDisplayRequestResponse(responseString, widget);
                                } else {
                                    handleButtonSendResponse(responseString, widget);
                                }
                                widget.setLastUpdateTime(new Date());
                                widget.setValueLoading(false);
                                getView().updateWidgetValue(position);},
                            throwable -> {
                                widget.setValueLoading(false);
                                getView().updateWidgetValue(position);
                                if(throwable instanceof TimeoutException){
                                    getView()
                                            .showLongMessage(R.string.message_timeout_error_text);
                                } else {
                                    getView().showMessage(throwable.getMessage());
                                }
                            }));
        } else {
            getView().showLongMessage(R.string.message_no_connection_text);
        }
    }

    @Override
    public void onDeviceStatusClick(){
        if(Utils.isOnline(mContext)){
            getDisposable().add(getRepository().isDeviceOnline()
                    .subscribeOn(getScheduler().io())
                    .timeout(TIMEOUT_DURATION_S, TimeUnit.SECONDS)
                    .observeOn(getScheduler().main())
                    .subscribe(response -> {
                                String responseString = response.string();
                                mIsDeviceOnline = Boolean.parseBoolean(responseString);
                                getView().showDeviceOnlineStatus(mIsDeviceOnline);
                                getView().showLongMessage(mIsDeviceOnline ?
                                        R.string.message_device_online_text : R.string.message_device_offline_text);
                            },
                            throwable -> {
                                if(throwable instanceof TimeoutException){
                                    getView()
                                            .showLongMessage(R.string.message_timeout_error_text);
                                } else {
                                    getView().showMessage(throwable.getMessage());
                                }
                            }));
        } else {
            getView().showLongMessage(R.string.message_no_connection_text);
        }
    }

    @Override
    public boolean isDeviceOnline(){
        return mIsDeviceOnline;
    }

    private boolean isCorrectPhoneNumber(String phoneNumber){
        return ((phoneNumber.startsWith("+7") && phoneNumber.length() == 12) ||
                (phoneNumber.startsWith("8") && phoneNumber.length() == 11));
    }

    private void handleDisplayRequestResponse(String responseString,
                                                   IWidget widget){
        if(responseString.startsWith("[\"")){
            responseString = responseString
                    .substring(2, responseString.length() - 2);
        } else {
            responseString = BlynkWidget.UNDEFINED;
        }
        widget.setValue(responseString);
    }

    private void handleButtonSendResponse(String responseString,
                                          IWidget widget){
        if(responseString.isEmpty()){
            widget.setValue(widget.getValue().equals(BlynkWidget.ON) ?
                    BlynkWidget.OFF : BlynkWidget.ON);
        } else {
            widget.setValue(BlynkWidget.UNDEFINED);
        }
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
//        getView().showMessage("Test widgets added");
//    }
}