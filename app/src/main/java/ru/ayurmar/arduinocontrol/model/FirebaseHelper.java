package ru.ayurmar.arduinocontrol.model;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.ayurmar.arduinocontrol.interfaces.model.IFirebaseHelper;
import ru.ayurmar.arduinocontrol.interfaces.model.IScheduler;
import ru.ayurmar.arduinocontrol.interfaces.model.IUserDevicesObserver;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidgetsObserver;

public class FirebaseHelper implements IFirebaseHelper {

    private List<FarhomeDevice> mUserDevices = new ArrayList<>();
    private WidgetGroup mWidgetGroup = new WidgetGroup();
    private List<IUserDevicesObserver> mDevicesObservers = new ArrayList<>();
    private List<IWidgetsObserver> mWidgetsObservers = new ArrayList<>();
    private final IScheduler mScheduler;
    private final CompositeDisposable mDisposable;
    private long mDeviceCount;
    private int mCurrentDeviceIndex;

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
    public void notifyUserDevicesObservers(){
        for(int i = 0; i < mDevicesObservers.size(); i++){
            mDevicesObservers.get(i).update(mUserDevices);
        }
    }

    @Override
    public void notifyWidgetsObservers(){
        for(int i = 0; i < mWidgetsObservers.size(); i++){
            mWidgetsObservers.get(i).update(mWidgetGroup);
        }
    }

    @Override
    public List<FarhomeDevice> getUserDevices() {
        return mUserDevices;
    }

    @Override
    public FarhomeDevice getCurrentDevice(){
        return mUserDevices.get(mCurrentDeviceIndex);
    }

    @Override
    public WidgetGroup getAllWidgets(){
        return mWidgetGroup;
    }

    @Override
    public void loadUserDevices(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference userDevicesRef = FirebaseDatabase.getInstance()
                    .getReference(DatabasePaths.USERS + "/" + firebaseUser.getUid()
                            + "/" + DatabasePaths.DEVICES);
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
                            if(mUserDevices.contains(device)){
                                return;
                            }
                            mUserDevices.add(device);
                            Log.d("FARHOME", device.getName());
                            if(mUserDevices.size() == mDeviceCount){
                                Log.d("FARHOME",
                            "Загружено " + mUserDevices.size() + " устройств!");
                                loadWidgets(mUserDevices.get(mCurrentDeviceIndex).getId());
                            }
                        },
                        throwable -> Log.d("FARHOME", "Ошибка при загрузке" +
                                "устройств!"));
    }

    private void loadWidgets(String deviceSn){
        if(deviceSn == null || deviceSn.isEmpty()){
            return;
        }

        DatabaseReference alarmRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                    DatabasePaths.CATEGORY_ALARM);
        DatabaseReference infoRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_INFO);
        DatabaseReference switchRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_SWITCH);

        RxFirebaseDatabase.observeSingleValueEvent(alarmRef,
                DataSnapshotMapper.listOf(AlarmWidget.class))
                .subscribeOn(mScheduler.io())
                .subscribe(alarmWidgets -> {
                    mWidgetGroup.setAlarmWidgets(alarmWidgets);
                    Log.d("FARHOME", "Найдено " + mWidgetGroup.getAlarmWidgetsCount() +
                            " тревожных датчиков!");
                });

        RxFirebaseDatabase.observeSingleValueEvent(infoRef,
                DataSnapshotMapper.listOf(InfoWidget.class))
                .subscribeOn(mScheduler.io())
                .subscribe(infoWidgets -> {
                    mWidgetGroup.setInfoWidgets(infoWidgets);
                    Log.d("FARHOME", "Найдено " + mWidgetGroup.getInfoWidgetsCount() +
                            " информационных датчиков!");
                });

        RxFirebaseDatabase.observeSingleValueEvent(switchRef,
                DataSnapshotMapper.listOf(SwitchWidget.class))
                .subscribeOn(mScheduler.io())
                .subscribe(switchWidgets -> {
                    mWidgetGroup.setSwitchWidgets(switchWidgets);
                    Log.d("FARHOME", "Найдено " + mWidgetGroup.getSwitchWidgetsCount() +
                            " управляющих датчиков!");
                });
    }

    private void addAlarmWidgetsListener(String deviceSn){
        DatabaseReference alarmRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_ALARM);
        Disposable alarmDisposable = RxFirebaseDatabase.observeChildEvent(alarmRef,
                AlarmWidget.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(alarmEvent -> {
                    switch (alarmEvent.getEventType()){
                        case ADDED:
                            mWidgetGroup.getAlarmWidgets().add(alarmEvent.getValue());
                            break;
                        case CHANGED:
                            mWidgetGroup.updateWidget(alarmEvent.getValue());
                            break;
                        case MOVED:
                            mWidgetGroup.updateWidget(alarmEvent.getValue());
                            break;
                        case REMOVED:
                            mWidgetGroup.getAlarmWidgets().remove(alarmEvent.getValue());
                            break;
                    }
                },
                        throwable -> Log.e("FARHOME",
                                "Ошибка в событии тревожных датчиков!"));
        mDisposable.add(alarmDisposable);
    }

    private void addInfoWidgetsListener(String deviceSn){
        DatabaseReference infoRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_INFO);
        Disposable infoDisposable = RxFirebaseDatabase.observeChildEvent(infoRef,
                InfoWidget.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(infoEvent -> {
                            switch (infoEvent.getEventType()){
                                case ADDED:
                                    mWidgetGroup.getInfoWidgets().add(infoEvent.getValue());
                                    break;
                                case CHANGED:
                                    mWidgetGroup.updateWidget(infoEvent.getValue());
                                    break;
                                case MOVED:
                                    mWidgetGroup.updateWidget(infoEvent.getValue());
                                    break;
                                case REMOVED:
                                    mWidgetGroup.getInfoWidgets().remove(infoEvent.getValue());
                                    break;
                            }
                        },
                        throwable -> Log.e("FARHOME",
                                "Ошибка в событии информационных датчиков!"));
        mDisposable.add(infoDisposable);
    }

    private void addSwitchWidgetsListener(String deviceSn){
        DatabaseReference switchRef = FirebaseDatabase.getInstance()
                .getReference(DatabasePaths.WIDGETS + "/" + deviceSn + "/" +
                        DatabasePaths.CATEGORY_SWITCH);
        Disposable switchDisposable = RxFirebaseDatabase.observeChildEvent(switchRef,
                SwitchWidget.class)
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.main())
                .subscribe(switchEvent -> {
                            switch (switchEvent.getEventType()){
                                case ADDED:
                                    mWidgetGroup.getSwitchWidgets().add(switchEvent.getValue());
                                    break;
                                case CHANGED:
                                    mWidgetGroup.updateWidget(switchEvent.getValue());
                                    break;
                                case MOVED:
                                    mWidgetGroup.updateWidget(switchEvent.getValue());
                                    break;
                                case REMOVED:
                                    mWidgetGroup.getSwitchWidgets().remove(switchEvent.getValue());
                                    break;
                            }
                        },
                        throwable -> Log.e("FARHOME",
                                "Ошибка в событии управляющих датчиков!"));
        mDisposable.add(switchDisposable);
    }
}