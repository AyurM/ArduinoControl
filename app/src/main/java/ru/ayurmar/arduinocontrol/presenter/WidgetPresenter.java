package ru.ayurmar.arduinocontrol.presenter;


import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.model.WidgetType;

public class WidgetPresenter<V extends IWidgetView>
        extends BasicPresenter<V> implements IWidgetPresenter<V> {

    public static final String WIDGETS_ROOT = "widgets";
    public static final String DEVICES_ROOT = "devices";
    private static final int TIMEOUT_DURATION_S = 10;
    
    private Context mContext;
    private boolean mIsDeviceOnline;
    private String mDeviceSn = "";
    private FarhomeDevice mFarhomeDevice;
    private List<FarhomeWidget> mWidgetList = new ArrayList<>();
    private List<String> mAvailableDevices = new ArrayList<>();

    @Inject
    public WidgetPresenter(IRepository repository, CompositeDisposable disposable,
                           IScheduler scheduler, Context context){
        super(repository, disposable, scheduler);
        this.mContext = context;
    }

    @Override
    public void loadUserDevices() {
        if (!Utils.isOnline(mContext)) {
            getView().showLongMessage(R.string.message_no_connection_text);
//            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            getView().showLoadingUI(true);
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("users/" + firebaseUser.getUid() + "/devices");
            ref.keepSynced(true);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) {
                        getView().showLoadingUI(false);
                        return;
                    }
                    Log.d("MAIN_ACTIVITY", dataSnapshot.toString());
                    Log.d("MAIN_ACTIVITY", "Children count = " + dataSnapshot.getChildrenCount());
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot child : children) {
                        mAvailableDevices.add(child.getKey());
                    }
                    mDeviceSn = mAvailableDevices.get(0);
                    getView().showLoadingUI(false);
                    loadDevice();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    getView().showLoadingUI(false);
                    Log.d("MAIN_ACTIVITY", "Database error!");
                }
            });
        }
    }

    @Override
    public void loadDevice(){
        if(!Utils.isOnline(mContext)){
            getView().showLongMessage(R.string.message_no_connection_text);
//            return;
        }
        if(mFarhomeDevice == null && !mDeviceSn.isEmpty()){
            getView().showLoadingUI(true);
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference(DEVICES_ROOT + "/" + mDeviceSn);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount() == 0){
                        getView().showLoadingUI(false);
                        getView().showLongMessage(R.string.message_device_not_found_text);
                        return;
                    }
                    Log.d("MAIN_ACTIVITY", dataSnapshot.toString());
                    Log.d("MAIN_ACTIVITY", "Devices count = " + dataSnapshot.getChildrenCount());
                    mFarhomeDevice = dataSnapshot.getValue(FarhomeDevice.class);
                    getView().updateDeviceUI(mDeviceSn);
                    getView().showLoadingUI(false);
                    loadWidgets();
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
    public void loadWidgets(){
        if(!Utils.isOnline(mContext)){
            getView().showLongMessage(R.string.message_no_connection_text);
//            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(WIDGETS_ROOT + "/" + mDeviceSn);
        getView().showLoadingUI(true);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0){
                    getView().showLoadingUI(false);
                    getView().showLongMessage(R.string.message_no_widgets_found_text);
                    return;
                }
                Log.d("MAIN_ACTIVITY", dataSnapshot.toString());
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
    public void onEditWidgetClick(FarhomeWidget widget){
        getView().showEditWidgetDialog(widget);
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