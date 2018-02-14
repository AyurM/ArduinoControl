package ru.ayurmar.arduinocontrol.presenter;

/*
TODO:
- Отсылка команд в Firebase DB
 */

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import ru.ayurmar.arduinocontrol.model.DatabasePaths;
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.fragments.AboutDeviceDialog;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.model.WidgetGroup;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V>, IWidgetsObserver,
        IUserDevicesObserver{

    private static final String sLogTag = "FARHOME";
    
    private Context mContext;
    private IWidgetView mView;
    private String mDeviceSn = "";

    @Inject
    public WidgetPresenter(IRepository repository, CompositeDisposable disposable,
                           IScheduler scheduler, Context context){
        super(repository, disposable, scheduler);
        this.mContext = context;
    }

    @Override
    public void onAttach(V view){
        super.onAttach(view);
        mView = view;
        getRepository().addWidgetsObserver(this);
        getRepository().addUserDevicesObserver(this);
        if (!Utils.isOnline(mContext)) {
            if(mView != null){
                mView.showNoConnectionUI(false);
            }
            return;
        }
        getRepository().loadUserDevices();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        getRepository().removeWidgetsObserver(this);
        getRepository().removeUserDevicesObserver(this);
        Log.d(sLogTag, "WidgetPresenter is detached!");
    }

    @Override
    public void update(WidgetGroup widgetGroup){
        if(mView != null){
            mView.showWidgetList(widgetGroup.getWidgets());
        }
    }

    @Override
    public void update(FarhomeWidget widget){
        if(mView != null){
            mView.updateWidget(widget);
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
    public void update(List<FarhomeDevice> devices){
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

    @Override
    public void updateWidgetInDb(FarhomeWidget widget){
//        getDisposable().add(getRepository().updateWidget(widget)
//                .subscribeOn(getScheduler().computation())
//                .observeOn(getScheduler().main())
//                .doOnError(throwable -> mView
//                        .showMessage(R.string.message_database_error_text))
//                .subscribe());
    }

    @Override
    public void onAddWidgetClick(){
        mView.showAddWidgetDialog();
    }

    @Override
    public void onAddDeviceClick(){
        mView.showAddDeviceDialog();
    }

    @Override
    public void onEditWidgetClick(FarhomeWidget widget){
        mView.showEditWidgetDialog(widget);
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
        getRepository().changeDevice(deviceId);
    }

    @Override
    public void bindDeviceToUser(String deviceSn, String deviceName){
        DatabaseReference deviceRootRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.DEVICES);
        deviceRootRef.child(deviceSn).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FarhomeDevice farhomeDevice = dataSnapshot.getValue(FarhomeDevice.class);
                if(farhomeDevice != null){
                    //устройство найдено, но принадлежит другому пользователю
                    if(farhomeDevice.getUser() != null){
                        if(mView != null){
                            mView.showLongMessage(R.string.message_device_already_has_owner);
                        }
                        return;
                    }
                    Log.d(sLogTag, dataSnapshot.toString());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null){
                        //закрепить устройство за данным юзером
                        mDeviceSn = deviceSn;
                        getRepository().saveStringPreference(user.getUid() +
                                "deviceSn", mDeviceSn);
                        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                .getReference();
                        Map<String, Object> deviceUpdates = new HashMap<>();
                        deviceUpdates.put(DatabasePaths.USERS + "/" + user.getUid() + "/" +
                        DatabasePaths.DEVICES + "/" + deviceSn, deviceName);
                        deviceUpdates.put(DatabasePaths.DEVICES + "/" + deviceSn + "/user",
                                user.getUid());
                        deviceUpdates.put(DatabasePaths.DEVICES + "/" + deviceSn + "/name",
                                deviceName);
                        dbRef.updateChildren(deviceUpdates);
                    }
                } else{
                    if(mView != null){
                        mView.showLongMessage(R.string.message_sn_not_exist);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void renameCurrentDevice(String newName){
        getRepository().renameCurrentDevice(newName);
    }

    @Override
    public int getDeviceCount(){
        return getRepository().getUserDevices().size();
    }

    @Override
    public void deleteWidget(int position){
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
    }

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
    public void onWidgetValueClick(int position){
//        FarhomeWidget widget = mView.getWidgetList().get(position);
//        WidgetType widgetType = widget.getWidgetType();
//        if(widgetType == WidgetType.ALARM_SENSOR){
//            return;
//        }
//        if(Utils.isOnline(mContext)){
//            widget.setValueLoading(true);
//            mView.updateWidgetValue(position);  //включить анимацию загрузки значения
//            Single<ResponseBody> blynkRequest;
//            if(widgetType == WidgetType.DISPLAY){
//                blynkRequest = getRepository().requestValueForWidget(widget);
//            } else {
//                blynkRequest = getRepository().sendValueFromWidget(widget);
//            }
//            getDisposable().add(blynkRequest
//                    .subscribeOn(getScheduler().io())
//                    .timeout(TIMEOUT_DURATION_S, TimeUnit.SECONDS)
//                    .observeOn(getScheduler().main())
//                    .doAfterSuccess(response -> updateWidgetInDb(widget))
//                    .subscribe(response -> {
//                                String responseString = response.string();
//                                if(widgetType == WidgetType.DISPLAY){
//                                    handleDisplayRequestResponse(responseString, widget);
//                                } else {
//                                    handleButtonSendResponse(responseString, widget);
//                                }
//                                widget.setLastUpdateTime(new Date());
//                                widget.setValueLoading(false);
//                                mView.updateWidgetValue(position);},
//                            throwable -> {
//                                widget.setValueLoading(false);
//                                mView.updateWidgetValue(position);
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
    }

    @Override
    public void onDeviceStatusClick(){
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
    }

//    @Override
//    public boolean isDeviceOnline(){
//        return mIsDeviceOnline;
//    }

    private boolean isCorrectPhoneNumber(String phoneNumber){
        return ((phoneNumber.startsWith("+7") && phoneNumber.length() == 12) ||
                (phoneNumber.startsWith("8") && phoneNumber.length() == 11));
    }
}