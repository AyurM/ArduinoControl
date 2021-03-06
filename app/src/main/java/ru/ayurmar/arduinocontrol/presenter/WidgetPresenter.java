package ru.ayurmar.arduinocontrol.presenter;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.PreferencesActivity;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.Utils;
import ru.ayurmar.arduinocontrol.interfaces.model.IUserDevicesObserver;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidgetsObserver;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.fragments.AboutDeviceDialog;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.model.SwitchWidget;
import ru.ayurmar.arduinocontrol.model.WidgetGroup;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V>, IWidgetsObserver,
        IUserDevicesObserver{
    
    private final Context mContext;
    private IWidgetView mView;

    @Inject
    public WidgetPresenter(IRepository repository, CompositeDisposable disposable,
                           IScheduler scheduler, Context context){
        super(repository, disposable, scheduler);
        this.mContext = context;
    }

    @Override
    public void onAttach(V view, int widgetCategory){
        super.onAttach(view);
        mView = view;
        getRepository().addWidgetsObserver(this);
        getRepository().addUserDevicesObserver(this);
        start();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        getRepository().reset();
    }

    @Override
    public void update(WidgetGroup widgetGroup){
        if(mView != null){
            mView.showWidgetList(widgetGroup.getWidgets());
        }
    }

    @Override
    public void update(RxFirebaseChildEvent<? extends FarhomeWidget> event){
        if(mView == null){
            return;
        }
        switch (event.getEventType()){
            case ADDED:
                mView.addWidget(event.getValue());
                break;
            case CHANGED:
                mView.updateWidget(event.getValue());
                break;
            case MOVED:
                break;
            case REMOVED:
                mView.deleteWidget(event.getValue());
                break;
        }
    }

    @Override
    public void updateWidgetLoadingState(boolean isLoading){
        if(mView != null){
            if(isLoading){
                mView.showLoadingUI(R.string.ui_loading_widgets_text);
            } else {
                mView.showLoadingUI(false);
            }
        }
    }

    @Override
    public void update(FarhomeDevice device){
        if(mView != null){
            mView.updateDeviceUI(device);
        }
    }

    @Override
    public void handleDeviceError(int errorMessage){
        if(mView != null){
            mView.showMessage(errorMessage);
        }
    }

    @Override
    public void updateDeviceLoadingState(boolean isLoading){
        if(mView != null){
            if(isLoading){
                mView.showLoadingUI(R.string.ui_loading_device_text);
            } else {
                mView.showLoadingUI(false);
            }
        }
    }

    @Override
    public void resetFirebaseHelper(){
        getRepository().reset();
    }


    @Override
    public void onAboutDeviceClick(){
        FarhomeDevice currentDevice = getRepository().getCurrentDevice();
        if(currentDevice != null){
            AboutDeviceDialog deviceDialog = AboutDeviceDialog.newInstance(
                    currentDevice.getName(),
                    currentDevice.getModel(),
                    currentDevice.getId()
            );
            if(mView != null){
                mView.showAboutDeviceDialog(deviceDialog);
            }
        }
    }

//    @Override
//    public void onAddWidgetClick(){
//        mView.showAddWidgetDialog();
//    }

    @Override
    public void onAddDeviceClick(){
        mView.showAddDeviceDialog();
    }

    @Override
    public void onRetryToConnectClick(){
        start();
    }

    @Override
    public void onEditWidgetClick(FarhomeWidget widget){
//        mView.showEditWidgetDialog(widget);
    }

    @Override
    public void onChangeDeviceClick(){
        List<FarhomeDevice> devices = getRepository().getUserDevices();
        ArrayList<String> deviceIds = new ArrayList<>();
        ArrayList<String> deviceNames = new ArrayList<>();
        for(FarhomeDevice device : devices){
            deviceIds.add(device.getId());
            deviceNames.add(device.getName());
        }
        mView.showChangeDeviceDialog(deviceIds, deviceNames);
    }

    @Override
    public void changeDevice(String deviceId){
        if(deviceId.equals(getRepository().getCurrentDevice().getId())){
            return;
        }
        getRepository().saveStringPreference(FirebaseAuth.getInstance()
                .getCurrentUser().getUid() + "deviceSn", deviceId);
        getRepository().changeDevice(deviceId);
    }

    @Override
    public void bindDeviceToUser(String deviceSn, String deviceName){
        getRepository().bindDeviceToUser(deviceSn, deviceName);
    }

    @Override
    public void renameCurrentDevice(String newName){
        getRepository().renameCurrentDevice(newName);
    }

    @Override
    public int getDeviceCount(){
        return getRepository().getUserDevices().size();
    }

//    @Override
//    public void deleteWidget(int position){
//        if (position == -1) {
//            return;
//        }
//        getDisposable().add(getRepository()
//                .deleteWidget(mView.getWidgetList().get(position))
//                .subscribeOn(getScheduler().computation())
//                .observeOn(getScheduler().main())
//                .doOnError(throwable -> mView
//                        .showMessage(R.string.message_database_error_text))
//                .subscribe(() -> loadWidgetListFromDb()));
//    }

    @Override
    public void onSendSmsClick(FarhomeWidget widget){
        String message = widget.getName() + " " + widget.getValue(); //заменить на команды для GSM-модуля
        getDisposable().add(getRepository()
                .getStringPreference(PreferencesActivity.KEY_PREF_PHONE_NUMBER)
                .subscribeOn(getScheduler().io())
                .observeOn(getScheduler().main())
                .subscribe(phoneNumber -> {
                    if(phoneNumber == null || phoneNumber.isEmpty()){
                        mView.showMessage(R.string.message_no_phone_number_text);
                    } else {
                        if(isCorrectPhoneNumber(phoneNumber)){
                            mView.showSendSmsDialog(message, phoneNumber);
                        } else {
                            mView.showMessage(R.string.message_wrong_number_format_error_text);
                        }
                    }
                },
                        throwable -> mView
                                .showMessage(R.string.message_error_phone_number_text))
        );
    }

    @Override
    public void onWidgetValueClick(FarhomeWidget widget){
        if(widget instanceof SwitchWidget){
            float newValue = widget.getValue() == 0.0f ? 1.0f : 0.0f;
            getRepository().updateWidgetValue(widget, newValue);
        }
    }

    @Override
    public List<FarhomeWidget> getAllWidgets(){
        return getRepository().getAllWidgets().getWidgets();
    }

//    @Override
//    public void onDeviceStatusClick(){
//        if(Utils.isOnline(mContext)){
//            getDisposable().add(getRepository().isDeviceOnline()
//                    .subscribeOn(getScheduler().io())
//                    .timeout(TIMEOUT_DURATION_S, TimeUnit.SECONDS)
//                    .observeOn(getScheduler().main())
//                    .subscribe(response -> {
//                                String responseString = response.string();
//                                mIsDeviceOnline = Boolean.parseBoolean(responseString);
//                                mView.showDeviceOnlineStatus(mIsDeviceOnline);
//                                mView.showLongMessage(mIsDeviceOnline ?
//                                        R.string.message_device_online_text : R.string.message_device_offline_text);
//                            },
//                            throwable -> {
//                                if(throwable instanceof TimeoutException){
//                                    mView
//                                            .showLongMessage(R.string.message_timeout_error_text);
//                                } else {
//                                    mView.showMessage(throwable.getMessage());
//                                }
//                            }));
//        } else {
//            mView.showLongMessage(R.string.message_no_connection_use_sms_text);
//        }
//    }

//    @Override
//    public boolean isDeviceOnline(){
//        return mIsDeviceOnline;
//    }

    private void start(){
        if (!Utils.isOnline(mContext)) {
            if(mView != null){
                mView.showNoConnectionUI(false);
            }
        } else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null){
                if(mView != null){
                    mView.showNoConnectionUI(true);
                }
                getRepository().loadUserDevices(firebaseUser.getUid());
            }
        }
    }

    private boolean isCorrectPhoneNumber(String phoneNumber){
        return ((phoneNumber.startsWith("+7") && phoneNumber.length() == 12) ||
                (phoneNumber.startsWith("8") && phoneNumber.length() == 11));
    }
}