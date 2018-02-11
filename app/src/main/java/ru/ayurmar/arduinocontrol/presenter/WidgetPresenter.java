package ru.ayurmar.arduinocontrol.presenter;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IRepository;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.view.AboutDeviceDialog;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V> {

    public static final String USERS_ROOT = "users";
    public static final String WIDGETS_ROOT = "widgets";
    public static final String DEVICES_ROOT = "devices";
    private static final String sLogTag = "FARHOME";
    
    private Context mContext;
    private IWidgetView mView;
    private String mDeviceSn = "";
    private FarhomeDevice mFarhomeDevice;
    private List<FarhomeWidget> mWidgetList = new ArrayList<>();
    private List<String> mAvailableDevices = new ArrayList<>();
    private List<String> mAvailableDevicesNames = new ArrayList<>();
    private DatabaseReference mUserDevicesRef;
    private DatabaseReference mWidgetsRef;
    private ChildEventListener mUserDevicesListener;
    private ChildEventListener mWidgetsListener;

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
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null){
            mUserDevicesRef = FirebaseDatabase.getInstance()
                    .getReference(USERS_ROOT + "/" + firebaseUser.getUid()
                            + "/" + DEVICES_ROOT);
            getRepository().getStringPreference(firebaseUser.getUid() + "deviceSn")
                    .subscribeOn(getScheduler().computation())
                    .observeOn(getScheduler().main())
                    .subscribe(deviceSn -> {
                        mDeviceSn = deviceSn;
                        Log.d(sLogTag, "mDeviceSn = " + mDeviceSn);
                        loadUserDevices();
                    });
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        removeFirebaseListeners();
        Log.d(sLogTag, "WidgetPresenter is detached!");
    }

    @Override
    public void loadUserDevices() {
        Log.d(sLogTag, "loadUserDevices()");
        if (!Utils.isOnline(mContext)) {
            if(mView != null){
                mView.showNoConnectionUI(false);
            }
            return;
        }
        if(mView != null){
            mView.showNoConnectionUI(true);
        }
        if (mUserDevicesRef != null) {
            if(mView != null){
                mView.showLoadingUI(R.string.ui_loading_user_devices_text);
            }
            mAvailableDevices.clear();
            mAvailableDevicesNames.clear();
            mUserDevicesRef.keepSynced(true);
            mUserDevicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) {
                        if(mView != null){
                            mView.showLoadingUI(false);
                            mView.showWidgetList(mWidgetList);
                        }
                        addDevicesListener();
                        return;
                    }
                    Log.d(sLogTag, "loadUserDevices(): " + dataSnapshot.toString());
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot child : children) {
                        mAvailableDevices.add(child.getKey());
                        mAvailableDevicesNames.add(child.getValue().toString());
                    }
                    if(mDeviceSn.isEmpty()){
                        mDeviceSn = mAvailableDevices.get(0);
                    }
                    loadDevice(mDeviceSn);
                    addDevicesListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(mView != null){
                        mView.showLoadingUI(false);
                        mView.showMessage(R.string.message_database_error_text);
                    }
                }
            });
        }
    }

    @Override
    public void loadDevice(String deviceSn){
        Log.d(sLogTag, "loadDevice()");
        if(!Utils.isOnline(mContext)){
            mView.showLongMessage(R.string.message_no_connection_text);
        }

        if(!deviceSn.isEmpty()){
            getRepository().saveStringPreference(FirebaseAuth.getInstance()
                    .getCurrentUser().getUid() + "deviceSn", deviceSn);
            DatabaseReference currentDeviceRef = FirebaseDatabase.getInstance()
                    .getReference(DEVICES_ROOT + "/" + deviceSn);
//            mCurrentDeviceRef.keepSynced(true);
            if(mView != null){
                mView.showLoadingUI(R.string.ui_loading_device_text);
            }
            currentDeviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() == 0){
                        if(mView != null){
                            mView.showLoadingUI(false);
                            mView.showLongMessage(R.string.message_device_not_found_text);
                            mView.showWidgetList(mWidgetList);
                        }
                        return;
                    }
                    Log.d(sLogTag, "loadDevice()" + dataSnapshot.toString());
                    mFarhomeDevice = dataSnapshot.getValue(FarhomeDevice.class);
                    if(mView != null){
                        mView.updateDeviceUI(mFarhomeDevice);
                    }
                    loadWidgets(deviceSn);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(mView != null){
                        mView.showLoadingUI(false);
                        mView.showMessage(R.string.message_database_error_text);
                    }
                }
            });
        }
    }

    @Override
    public void loadWidgets(String deviceSn){
        Log.d(sLogTag, "loadWidgets()");
        if(!Utils.isOnline(mContext)){
            mView.showLongMessage(R.string.message_no_connection_text);
        }
        if(!deviceSn.isEmpty()){
            if(!deviceSn.equals(mDeviceSn)){
                mDeviceSn = deviceSn;
            }
            mWidgetsRef = FirebaseDatabase.getInstance()
                    .getReference(WIDGETS_ROOT + "/" + deviceSn);
            mWidgetsRef.keepSynced(true);
            mWidgetList.clear();
            if(mView != null){
                mView.showLoadingUI(R.string.ui_loading_widgets_text);
            }
            mWidgetsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() == 0){
                        if(mView != null){
                            mView.showLoadingUI(false);
                            mView.showLongMessage(R.string.message_no_widgets_found_text);
                        }
                        return;
                    }
                    Log.d(sLogTag, "loadWidgets()" + dataSnapshot.toString());
                    for(DataSnapshot widget : dataSnapshot.getChildren()){
                        FarhomeWidget farhomeWidget = widget.getValue(FarhomeWidget.class);
                        mWidgetList.add(farhomeWidget);
                    }
                    Log.d(sLogTag, "Parsed " + mWidgetList.size() + " widgets!");
                    if(mView != null){
                        mView.showLoadingUI(false);
                        mView.showWidgetList(mWidgetList);
                    }
                    addWidgetsListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(mView != null){
                        mView.showLoadingUI(false);
                        mView.showMessage(R.string.message_database_error_text);
                    }
                }
            });
        }
    }

    @Override
    public void onAboutDeviceClick(){
        if(mFarhomeDevice != null){
            AboutDeviceDialog deviceDialog = AboutDeviceDialog.newInstance(
                    mFarhomeDevice.getName(),
                    mFarhomeDevice.getModel(),
                    mDeviceSn
            );
            if(mView != null){
                mView.showAboutDeviceDialog(deviceDialog);
            }
        }
    }

    private void addDevicesListener(){
        Log.d(sLogTag, "addDevicesListener()");
        mUserDevicesListener = mUserDevicesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(sLogTag, "key = " + dataSnapshot.getKey() +
                        "; value = " + dataSnapshot.getValue());
                String deviceSn = dataSnapshot.getKey();
                if(!mAvailableDevices.contains(deviceSn)){
                    mAvailableDevices.add(deviceSn);
                    mAvailableDevicesNames.add((String) dataSnapshot.getValue());
                    if(mDeviceSn.isEmpty()){
                        mDeviceSn = deviceSn;
                        loadDevice(deviceSn);
                    } else if(deviceSn.equals(mDeviceSn)){
                        loadDevice(deviceSn);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(sLogTag, "onChildChanged: " + s +
                        "; " + dataSnapshot.toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(sLogTag, "onChildRemoved: " + dataSnapshot.toString());
                mAvailableDevices.remove(dataSnapshot.getKey());
                mAvailableDevicesNames.remove(dataSnapshot.getValue());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(sLogTag, "onChildMoved: " + s +
                        "; " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(mView != null){
                    mView.showMessage(R.string.message_database_error_text);
                }
            }
        });

    }

    private void addWidgetsListener(){
        Log.d(sLogTag, "addWidgetsListener()");
        mWidgetsListener = mWidgetsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(sLogTag, "Widgets onChildChanged: " + s +
                        "; " + dataSnapshot.toString());
                FarhomeWidget changedWidget = dataSnapshot.getValue(FarhomeWidget.class);
                if(mView != null){
                    mView.updateWidget(changedWidget);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(mView != null){
                    mView.showMessage(R.string.message_database_error_text);
                }
            }
        });

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
        mView.showChangeDeviceDialog(mAvailableDevices, mAvailableDevicesNames);
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
                        deviceUpdates.put(USERS_ROOT + "/" + user.getUid() + "/" +
                        DEVICES_ROOT + "/" + deviceSn, deviceName);
                        deviceUpdates.put(DEVICES_ROOT + "/" + deviceSn + "/user",
                                user.getUid());
                        deviceUpdates.put(DEVICES_ROOT + "/" + deviceSn + "/name",
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
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Map<String, Object> deviceUpdates = new HashMap<>();
            deviceUpdates.put(USERS_ROOT + "/" + user.getUid() + "/" +
                    DEVICES_ROOT + "/" + mDeviceSn, newName);
            deviceUpdates.put(DEVICES_ROOT + "/" + mDeviceSn + "/name",
                    newName);
            dbRef.updateChildren(deviceUpdates);
        }
        mFarhomeDevice.setName(newName);
        int deviceIndex = mAvailableDevices.indexOf(mDeviceSn);
        if(deviceIndex != -1){
            mAvailableDevicesNames.set(deviceIndex, newName);
        }
        mView.updateDeviceUI(mFarhomeDevice);
    }

    @Override
    public int getDeviceCount(){
        return mAvailableDevices.size();
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
                    if(phoneNumber == null){
                        mView.showMessage(R.string.message_no_phone_number_text);
                    } else {
                        if(isCorrectPhoneNumber(phoneNumber)){
                            mView.showSendSmsDialog(message, phoneNumber);
                        } else {
                            mView
                                    .showMessage(R.string.message_wrong_number_format_error_text);
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

    private void removeFirebaseListeners(){
        if(mUserDevicesRef != null && mUserDevicesListener != null){
            mUserDevicesRef.removeEventListener(mUserDevicesListener);
        }
        if(mWidgetsRef != null && mWidgetsListener != null){
            mWidgetsRef.removeEventListener(mWidgetsListener);
        }
    }

//    private void handleDisplayRequestResponse(String responseString,
//                                                   IWidget widget){
//        if(responseString.startsWith("[\"")){
//            responseString = responseString
//                    .substring(2, responseString.length() - 2);
//        } else {
//            responseString = BlynkWidget.UNDEFINED;
//        }
//        widget.setValue(responseString);
//    }
//
//    private void handleButtonSendResponse(String responseString,
//                                          IWidget widget){
//        if(responseString.isEmpty()){
//            widget.setValue(widget.getValue().equals(BlynkWidget.ON) ?
//                    BlynkWidget.OFF : BlynkWidget.ON);
//        } else {
//            widget.setValue(BlynkWidget.UNDEFINED);
//        }
//    }
}