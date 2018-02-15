package ru.ayurmar.arduinocontrol.model;

/*
    TODO:
   - добавление нового устройства
 */

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.ayurmar.arduinocontrol.interfaces.model.IFirebaseHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.interfaces.model.IUserDevicesObserver;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidgetsObserver;

public class FirebaseHelper implements IFirebaseHelper {

    private List<FarhomeDevice> mUserDevices = new ArrayList<>();
    private List<IUserDevicesObserver> mDevicesObservers = new ArrayList<>();
    private List<IWidgetsObserver> mWidgetsObservers = new ArrayList<>();
    private WidgetGroup mWidgetGroup = new WidgetGroup();
    private final IScheduler mScheduler;
    private final CompositeDisposable mDisposable;
    private long mDeviceCount;
    private int mCurrentDeviceIndex;
    private String mLastDeviceId = "";
    private DatabaseReference mAlarmWidgetsRef;
    private DatabaseReference mInfoWidgetsRef;
    private DatabaseReference mSwitchWidgetsRef;

    public FirebaseHelper(IScheduler scheduler, CompositeDisposable disposable){
        this.mDisposable = disposable;
        this.mScheduler = scheduler;
    }

    @Override
    public void addUserDevicesObserver(IUserDevicesObserver observer){
        mDevicesObservers.add(observer);
    }

    @Override
    public void removeUserDevicesObserver(IUserDevicesObserver observer){
        mDevicesObservers.remove(observer);
    }

    @Override
    public void addWidgetsObserver(IWidgetsObserver observer){
        mWidgetsObservers.add(observer);
    }

    @Override
    public void removeWidgetsObserver(IWidgetsObserver observer){
        mWidgetsObservers.remove(observer);
    }

    @Override
    public void notifyDeviceObservers(){
        for(int i = 0; i < mDevicesObservers.size(); i++){
            mDevicesObservers.get(i).update(mUserDevices.get(mCurrentDeviceIndex));
        }
    }

    private void notifyDeviceObserversLoading(boolean isLoading){
        for(int i = 0; i < mDevicesObservers.size(); i++){
            mDevicesObservers.get(i).updateDeviceLoadingState(isLoading);
        }
    }

    @Override
    public void notifyWidgetObservers(){
        for(int i = 0; i < mWidgetsObservers.size(); i++){
            mWidgetsObservers.get(i).update(mWidgetGroup);
        }
    }

    @Override
    public void notifyWidgetObservers(RxFirebaseChildEvent<? extends FarhomeWidget> event){
        for(int i = 0; i < mWidgetsObservers.size(); i++){
            mWidgetsObservers.get(i).update(event);
        }
    }


    private void notifyWidgetObserversLoading(boolean isLoading){
        for(int i = 0; i < mWidgetsObservers.size(); i++){
            mWidgetsObservers.get(i).updateWidgetLoadingState(isLoading);
        }
    }

    @Override
    public List<FarhomeDevice> getUserDevices() {
        return mUserDevices;
    }

    @Override
    public FarhomeDevice getCurrentDevice(){
        if(mUserDevices.isEmpty()){
            return null;
        }
        return mUserDevices.get(mCurrentDeviceIndex);
    }

    @Override
    public WidgetGroup getAllWidgets(){
        return mWidgetGroup;
    }

    @Override
    public void loadUserDevices(String lastDeviceId){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mLastDeviceId = lastDeviceId;
            DatabaseReference userDevicesRef = FirebaseDatabase.getInstance()
                    .getReference(DatabasePaths.USERS + "/" + firebaseUser.getUid()
                            + "/" + DatabasePaths.DEVICES);
            notifyDeviceObserversLoading(true);
            RxFirebaseDatabase.observeSingleValueEvent(userDevicesRef,
                    dataSnapshot -> {
                        mDeviceCount = dataSnapshot.getChildrenCount();
                        return getSerialNumbers(dataSnapshot);
                            })
                .flattenAsObservable(snList -> snList)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(deviceSn -> loadUserDevice(deviceSn));
        }
    }

    @Override
    public void renameCurrentDevice(String name){
        if(name.equals(getCurrentDevice().getName())){
            return;
        }
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Map<String, Object> deviceUpdates = new HashMap<>();
            deviceUpdates.put(DatabasePaths.USERS + "/" + user.getUid() + "/" +
                    DatabasePaths.DEVICES + "/" + getCurrentDevice().getId(), name);
            deviceUpdates.put(DatabasePaths.DEVICES + "/" + getCurrentDevice().getId()
                            + "/name", name);
            RxFirebaseDatabase.updateChildren(dbRef, deviceUpdates)
                    .subscribeOn(mScheduler.io())
                    .observeOn(mScheduler.main())
                    .subscribe(() -> {
                        getCurrentDevice().setName(name);
                        notifyDeviceObservers();
                    },
                            throwable -> Log.e("FARHOME", "Ошибка при" +
                                    "переименовании устройства!"));
        }
    }

    @Override
    public void bindDeviceToUser(String deviceSn, String deviceName){

    }

    @Override
    public void changeDevice(String deviceId){
        for(int i = 0; i < mUserDevices.size(); i++){
            if(deviceId.equals(mUserDevices.get(i).getId())){
                mCurrentDeviceIndex = i;
                break;
            }
        }
        notifyDeviceObservers();
        mDisposable.dispose();  //убрать слушатели от предыдущего устройства
        loadWidgets(deviceId);
    }

    @Override
    public void reset(){
        mUserDevices.clear();
        mCurrentDeviceIndex = 0;
        mDeviceCount = 0;
        mDevicesObservers.clear();
        mWidgetsObservers.clear();
        mDisposable.dispose();
    }

    private static List<String> getSerialNumbers(DataSnapshot dataSnapshot){
        List<String> snList = new ArrayList<>();
        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
        for (DataSnapshot child : children) {
            snList.add(child.getKey());
            Log.d("FARHOME", child.getKey());
        }
        return snList;
    }

    private void loadUserDevice(String deviceSn){
        DatabaseReference deviceRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.DEVICES + "/" + deviceSn);
        RxFirebaseDatabase.observeSingleValueEvent(deviceRef, FarhomeDevice.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(device -> {
                            if(!mUserDevices.contains(device)){
                                mUserDevices.add(device);
                            }
                            Log.d("FARHOME", device.getName());
                            if(mUserDevices.size() == mDeviceCount){
                                Log.d("FARHOME",
                            "Загружено " + mUserDevices.size() + " устройств!");
                                updateCurrentDeviceIndex();
                                notifyDeviceObserversLoading(false);
                                notifyDeviceObservers();
                                loadWidgets(mUserDevices.get(mCurrentDeviceIndex).getId());
                            }
                        },
                        throwable -> {
                                Log.d("FARHOME", "Ошибка при загрузке" +
                                    "устройств!");
                                notifyDeviceObserversLoading(false);
                        });
    }

    private void loadWidgets(String deviceSn){
        if(deviceSn == null || deviceSn.isEmpty()){
            return;
        }
        mAlarmWidgetsRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_ALARM);

        mInfoWidgetsRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_INFO);

        mSwitchWidgetsRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_SWITCH);
        mWidgetGroup.clear();
        notifyWidgetObserversLoading(true);
        loadAlarmWidgets();
        loadInfoWidgets();
        loadSwitchWidgets();
    }

    private void loadAlarmWidgets(){
        RxFirebaseDatabase.observeSingleValueEvent(mAlarmWidgetsRef,
                DataSnapshotMapper.listOf(AlarmWidget.class))
                .subscribeOn(mScheduler.io())
                .doOnSuccess(widgets -> addAlarmWidgetsListener())
                .subscribe(alarmWidgets -> {
                    mWidgetGroup.setAlarmWidgets(alarmWidgets);
                    Log.d("FARHOME", "Найдено " + mWidgetGroup.getAlarmWidgetsCount() +
                            " тревожных датчиков!");
                    notifyWidgetObserversLoading(false);
                    notifyWidgetObservers();
                });
    }

    private void loadInfoWidgets(){
        RxFirebaseDatabase.observeSingleValueEvent(mInfoWidgetsRef,
                DataSnapshotMapper.listOf(InfoWidget.class))
                .subscribeOn(mScheduler.io())
                .doOnSuccess(widgets -> addInfoWidgetsListener())
                .subscribe(infoWidgets -> {
                    mWidgetGroup.setInfoWidgets(infoWidgets);
                    Log.d("FARHOME", "Найдено " + mWidgetGroup.getInfoWidgetsCount() +
                            " информационных датчиков!");
                    notifyWidgetObserversLoading(false);
                    notifyWidgetObservers();
                });
    }

    private void loadSwitchWidgets(){
        RxFirebaseDatabase.observeSingleValueEvent(mSwitchWidgetsRef,
                DataSnapshotMapper.listOf(SwitchWidget.class))
                .subscribeOn(mScheduler.io())
                .doOnSuccess(widgets -> addSwitchWidgetsListener())
                .subscribe(switchWidgets -> {
                    mWidgetGroup.setSwitchWidgets(switchWidgets);
                    Log.d("FARHOME", "Найдено " + mWidgetGroup.getSwitchWidgetsCount() +
                            " управляющих датчиков!");
                    notifyWidgetObserversLoading(false);
                    notifyWidgetObservers();
                });
    }

    private void addAlarmWidgetsListener(){
        Disposable alarmDisposable = RxFirebaseDatabase.observeChildEvent(mAlarmWidgetsRef,
                AlarmWidget.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(alarmEvent -> {
                    AlarmWidget widget = alarmEvent.getValue();
                    switch (alarmEvent.getEventType()){
                        case ADDED:
                            if(!mWidgetGroup.getAlarmWidgets().contains(widget)){
                                mWidgetGroup.getAlarmWidgets().add(alarmEvent.getValue());
                                notifyWidgetObservers(alarmEvent);
                            }
                            return;
                        case CHANGED:
                            mWidgetGroup.updateWidget(widget);
                            break;
                        case MOVED:
                            mWidgetGroup.updateWidget(widget);
                            break;
                        case REMOVED:
                            mWidgetGroup.getAlarmWidgets().remove(widget);
                            break;
                    }
                    notifyWidgetObservers(alarmEvent);
                },
                        throwable -> Log.e("FARHOME",
                                "Ошибка в событии тревожных датчиков!"));
        mDisposable.add(alarmDisposable);
    }

    private void addInfoWidgetsListener(){
        Disposable infoDisposable = RxFirebaseDatabase.observeChildEvent(mInfoWidgetsRef,
                InfoWidget.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(infoEvent -> {
                            InfoWidget widget = infoEvent.getValue();
                            switch (infoEvent.getEventType()){
                                case ADDED:
                                    if(!mWidgetGroup.getInfoWidgets().contains(widget)){
                                        mWidgetGroup.getInfoWidgets().add(widget);
                                        notifyWidgetObservers(infoEvent);
                                    }
                                    return;
                                case CHANGED:
                                    mWidgetGroup.updateWidget(widget);
                                    break;
                                case MOVED:
                                    mWidgetGroup.updateWidget(widget);
                                    break;
                                case REMOVED:
                                    mWidgetGroup.getInfoWidgets().remove(widget);
                                    break;
                            }
                            notifyWidgetObservers(infoEvent);
                        },
                        throwable -> Log.e("FARHOME",
                                "Ошибка в событии информационных датчиков!"));
        mDisposable.add(infoDisposable);
    }

    private void addSwitchWidgetsListener(){
        Disposable switchDisposable = RxFirebaseDatabase.observeChildEvent(mSwitchWidgetsRef,
                SwitchWidget.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(switchEvent -> {
                            SwitchWidget widget = switchEvent.getValue();
                            switch (switchEvent.getEventType()){
                                case ADDED:
                                    if(!mWidgetGroup.getSwitchWidgets().contains(widget)){
                                        mWidgetGroup.getSwitchWidgets().add(widget);
                                        notifyWidgetObservers(switchEvent);
                                    }
                                    return;
                                case CHANGED:
                                    mWidgetGroup.updateWidget(widget);
                                    break;
                                case MOVED:
                                    mWidgetGroup.updateWidget(widget);
                                    break;
                                case REMOVED:
                                    mWidgetGroup.getSwitchWidgets().remove(widget);
                                    break;
                            }
                            notifyWidgetObservers(switchEvent);
                        },
                        throwable -> Log.e("FARHOME",
                                "Ошибка в событии управляющих датчиков!"));
        mDisposable.add(switchDisposable);
    }

    private void updateCurrentDeviceIndex(){
        if(mLastDeviceId.isEmpty()){
            mCurrentDeviceIndex = 0;
            return;
        }
        for(int i = 0; i < mUserDevices.size(); i++){
            if(mLastDeviceId.equals(mUserDevices.get(i).getId())){
                mCurrentDeviceIndex = i;
                break;
            }
        }
    }
}