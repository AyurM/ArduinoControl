package ru.ayurmar.arduinocontrol.presenter;

/**
 * TODO:
 * - обновление UI при привязке нового устройства
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import ru.ayurmar.arduinocontrol.PreferencesActivity;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.Utils;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.BlynkWidget;
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V> {

    public static final String USERS_ROOT = "users";
    public static final String WIDGETS_ROOT = "widgets";
    public static final String DEVICES_ROOT = "devices";
    private static final int TIMEOUT_DURATION_S = 10;
    
    private Context mContext;
    private boolean mIsDeviceOnline;
    private String mDeviceSn = "";
    private FarhomeDevice mFarhomeDevice;
    private List<FarhomeWidget> mWidgetList = new ArrayList<>();
    private List<String> mAvailableDevices = new ArrayList<>();
    private List<String> mAvailableDevicesNames = new ArrayList<>();
    private DatabaseReference mUserDevicesRef;
    private DatabaseReference mCurrentDeviceRef;
    private DatabaseReference mWidgetsRef;

    @Inject
    public WidgetPresenter(IRepository repository, CompositeDisposable disposable,
                           IScheduler scheduler, Context context){
        super(repository, disposable, scheduler);
        this.mContext = context;
    }

    @Override
    public void onAttach(V view){
        super.onAttach(view);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            mUserDevicesRef = FirebaseDatabase.getInstance()
                    .getReference(USERS_ROOT + "/" + firebaseUser.getUid() +
                            "/" + DEVICES_ROOT);
            getRepository().getStringPreference(firebaseUser.getUid() + "deviceSn")
                    .subscribeOn(getScheduler().computation())
                    .observeOn(getScheduler().main())
                    .subscribe(deviceSn -> {
                        mDeviceSn = deviceSn;
                        loadUserDevices();
                    });
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
    }

    @Override
    public void loadUserDevices() {
        Log.d("MAIN_ACTIVITY", "loadUserDevices()");
        if (!Utils.isOnline(mContext)) {
            getView().showLongMessage(R.string.message_no_connection_text);
        }

        if (mUserDevicesRef != null) {
            getView().showLoadingUI(R.string.ui_loading_user_devices_text);
            mAvailableDevices.clear();
            mAvailableDevicesNames.clear();
            mUserDevicesRef.keepSynced(true);
            mUserDevicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) {
                        getView().showLoadingUI(false);
                        getView().showWidgetList(mWidgetList);
                        return;
                    }
                    Log.d("MAIN_ACTIVITY", "loadUserDevices(): " + dataSnapshot.toString());
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot child : children) {
                        mAvailableDevices.add(child.getKey());
                        mAvailableDevicesNames.add(child.getValue().toString());
                    }
                    if(mDeviceSn.isEmpty()){
                        mDeviceSn = mAvailableDevices.get(0);
                    }
                    loadDevice(mDeviceSn);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    getView().showLoadingUI(false);
                    getView().showMessage(R.string.message_database_error_text);
                }
            });
        }
    }

    @Override
    public void loadDevice(String deviceSn){
        Log.d("MAIN_ACTIVITY", "loadDevice()");
        if(!Utils.isOnline(mContext)){
            getView().showLongMessage(R.string.message_no_connection_text);
        }

        if(!deviceSn.isEmpty()){
            getRepository().saveStringPreference(FirebaseAuth.getInstance()
                    .getCurrentUser().getUid() + "deviceSn", deviceSn);
            mCurrentDeviceRef = FirebaseDatabase.getInstance()
                    .getReference(DEVICES_ROOT + "/" + deviceSn);
            mCurrentDeviceRef.keepSynced(true);
            getView().showLoadingUI(R.string.ui_loading_device_text);
            mCurrentDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() == 0){
                        getView().showLoadingUI(false);
                        getView().showLongMessage(R.string.message_device_not_found_text);
                        getView().showWidgetList(mWidgetList);
                        return;
                    }
                    Log.d("MAIN_ACTIVITY", "loadDevice()" + dataSnapshot.toString());
                    mFarhomeDevice = dataSnapshot.getValue(FarhomeDevice.class);
                    getView().updateDeviceUI(mFarhomeDevice);
                    loadWidgets(deviceSn);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    getView().showLoadingUI(false);
                    getView().showMessage(R.string.message_database_error_text);
                }
            });
        }
    }

    @Override
    public void loadWidgets(String deviceSn){
        Log.d("MAIN_ACTIVITY", "loadWidgets()");
        if(!Utils.isOnline(mContext)){
            getView().showLongMessage(R.string.message_no_connection_text);
        }
        if(!deviceSn.isEmpty()){
            if(!deviceSn.equals(mDeviceSn)){
                mDeviceSn = deviceSn;
            }
            mWidgetsRef = FirebaseDatabase.getInstance()
                    .getReference(WIDGETS_ROOT + "/" + deviceSn);
            mWidgetsRef.keepSynced(true);
            mWidgetList.clear();
            getView().showLoadingUI(R.string.ui_loading_widgets_text);
            mWidgetsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() == 0){
                        getView().showLoadingUI(false);
                        getView().showLongMessage(R.string.message_no_widgets_found_text);
                        return;
                    }
                    Log.d("MAIN_ACTIVITY", "loadWidgets()" + dataSnapshot.toString());
                    for(DataSnapshot widget : dataSnapshot.getChildren()){
                        FarhomeWidget farhomeWidget = widget.getValue(FarhomeWidget.class);
                        mWidgetList.add(farhomeWidget);
                    }
                    Log.d("MAIN_ACTIVITY", "Parsed " + mWidgetList.size() + " widgets!");
                    getView().showLoadingUI(false);
                    getView().showWidgetList(mWidgetList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    getView().showLoadingUI(false);
                    getView().showMessage(R.string.message_database_error_text);
                }
            });
        }
    }

    @Override
    public void updateWidgetInDb(FarhomeWidget widget){
//        getDisposable().add(getRepository().updateWidget(widget)
//                .subscribeOn(getScheduler().computation())
//                .observeOn(getScheduler().main())
//                .doOnError(throwable -> getView()
//                        .showMessage(R.string.message_database_error_text))
//                .subscribe());
    }

    @Override
    public void onAddWidgetClick(){
        getView().showAddWidgetDialog();
    }

    @Override
    public void onAddDeviceClick(){
        getView().showAddDeviceDialog();
    }

    @Override
    public void onEditWidgetClick(FarhomeWidget widget){
        getView().showEditWidgetDialog(widget);
    }

    @Override
    public void onChangeDeviceClick(){
        getView().showChangeDeviceDialog(mAvailableDevices, mAvailableDevicesNames);
    }

    @Override
    public void bindDeviceToUser(String deviceSn, String deviceName){
        DatabaseReference deviceRootRef = FirebaseDatabase.getInstance()
                .getReference(DEVICES_ROOT);
        deviceRootRef.child(deviceSn).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FarhomeDevice farhomeDevice = dataSnapshot.getValue(FarhomeDevice.class);
                if(farhomeDevice != null){
                    if(farhomeDevice.getUser() != null){
                        Log.d("MAIN_ACTIVITY", farhomeDevice.getUser());
                        Log.d("MAIN_ACTIVITY", "This device is not yours, beech!");
                        return;
                    }
                    Log.d("MAIN_ACTIVITY", dataSnapshot.toString());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null){
                        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                                .getReference();
                        Map<String, Object> deviceUpdates = new HashMap<>();
                        deviceUpdates.put(USERS_ROOT + "/" + user.getUid() + "/" +
                        DEVICES_ROOT + "/" + deviceSn, deviceName);
                        deviceUpdates.put(DEVICES_ROOT + "/" + deviceSn + "/user",
                                user.getUid());
                        deviceUpdates.put(DEVICES_ROOT + "/" + deviceSn + "/name",
                                deviceName);
                        dbRef.updateChildren(deviceUpdates);
                    }
                } else{
                    getView().showLongMessage(R.string.message_sn_not_exist);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void deleteWidget(int position){
//        if (position == -1) {
//            return;
//        }
//        getDisposable().add(getRepository()
//                .deleteWidget(getView().getWidgetList().get(position))
//                .subscribeOn(getScheduler().computation())
//                .observeOn(getScheduler().main())
//                .doOnError(throwable -> getView()
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
//        FarhomeWidget widget = getView().getWidgetList().get(position);
//        WidgetType widgetType = widget.getWidgetType();
//        if(widgetType == WidgetType.ALARM_SENSOR){
//            return;
//        }
//        if(Utils.isOnline(mContext)){
//            widget.setValueLoading(true);
//            getView().updateWidgetValue(position);  //включить анимацию загрузки значения
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
//                                getView().updateWidgetValue(position);},
//                            throwable -> {
//                                widget.setValueLoading(false);
//                                getView().updateWidgetValue(position);
//                                if(throwable instanceof TimeoutException){
//                                    getView()
//                                            .showLongMessage(R.string.message_timeout_error_text);
//                                } else {
//                                    getView().showMessage(throwable.getMessage());
//                                }
//                            }));
//        } else {
//            getView().showLongMessage(R.string.message_no_connection_use_sms_text);
//        }
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
            getView().showLongMessage(R.string.message_no_connection_use_sms_text);
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
}