package ru.ayurmar.arduinocontrol.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ayurmar.arduinocontrol.AddWidgetActivity;
import ru.ayurmar.arduinocontrol.MainActivity;
import ru.ayurmar.arduinocontrol.MainApp;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.Utils;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.model.AlarmWidget;
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;
import ru.ayurmar.arduinocontrol.model.InfoWidget;
import ru.ayurmar.arduinocontrol.model.SwitchWidget;


public class WidgetFragment extends BasicFragment implements IWidgetView {

    private static final int sAddWidgetRequestCode = 0;
    private static final int sConfirmDeleteCode = 1;
    private static final int sChangeDeviceCode = 2;
    private static final int sAddDeviceCode = 3;
    private static final int sRenameDeviceCode = 4;
//    private static final String sConfirmDeleteTag = "CONFIRM_DELETE_DIALOG";
    private static final String sChangeDeviceTag = "CHANGE_DEVICE_DIALOG";
    private static final String sAddDeviceTag = "ADD_DEVICE_DIALOG";
    private static final String sRenameDeviceTag = "RENAME_DEVICE_DIALOG";
    private static final String sAboutDeviceTag = "ABOUT_DEVICE_DIALOG";

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mLoadingInfoTextView;
    private TextView mNoConnectionTextView;
    private LinearLayout mNoItemsLayout;
    private Button mRetryConnectionButton;
    private int mWidgetCategory;
//    private Menu mMenu;

    @Inject
    IWidgetPresenter<IWidgetView> mPresenter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mWidgetCategory = 0;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_widget, container, false);

        mNoItemsLayout = view.findViewById(R.id.widget_no_items_layout);
        mProgressBar = view.findViewById(R.id.widget_progress_bar);
        mLoadingInfoTextView = view.findViewById(R.id.widget_loading_info_text_view);
        mNoConnectionTextView = view.findViewById(R.id.widget_no_connection_text_view);
        mRecyclerView = view.findViewById(R.id.widget_recycler_view);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
//                LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        tabLayout.getTabAt(0).getIcon()
                .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent),
                        PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mWidgetCategory = tab.getPosition();
                int tabIconColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                showWidgetList(mPresenter.getAllWidgets());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(getContext(), android.R.color.white);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Button addDeviceButton = view.findViewById(R.id.widget_add_device_button);
        mRetryConnectionButton = view.findViewById(R.id.widget_retry_connection_button);
        addDeviceButton.setOnClickListener(view1 -> mPresenter.onAddDeviceClick());
        mRetryConnectionButton.setOnClickListener(view1 -> mPresenter.onRetryToConnectClick());

        mPresenter.onAttach(this, mWidgetCategory);
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        MainApp.getInstance().getFragmentComponent().inject(this);
    }

    @Override
    public void onDestroy(){
        mPresenter.onDetach();
        super.onDestroy();
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        mMenu = menu;
//        inflater.inflate(R.menu.menu_main_widget, menu);
//        MenuItem addWidgetItem = mMenu.findItem(R.id.menu_item_add_widget);
//        addWidgetItem.setVisible(false);   //hide "Add Widget" menu item
//        showDeviceOnlineStatus(false);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_item_device_status:
//                mPresenter.onDeviceStatusClick();
//                return true;
//            case R.id.menu_item_add_widget:
//                mPresenter.onAddWidgetClick();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
//            if(requestCode == sAddWidgetRequestCode){
//                mPresenter.loadWidgetListFromDb();
//            } else
            if(requestCode == sConfirmDeleteCode){
//                mPresenter.deleteWidget(data
//                        .getIntExtra(DeleteConfirmationFragment.EXTRA_POSITION, -1));
            } else if(requestCode == sChangeDeviceCode){
                String deviceSn = data.getStringExtra(ChangeDeviceDialog.SELECTED_DEVICE_INDEX);
                mPresenter.changeDevice(deviceSn);
            } else if(requestCode == sAddDeviceCode){
                String deviseSn = data.getStringExtra(AddDeviceDialog.ADDED_DEVICE_SN_INDEX);
                String deviceName = data.getStringExtra(AddDeviceDialog.ADDED_DEVICE_NAME_INDEX);
                if(deviceName.isEmpty()){
                    deviceName = getString(R.string.placeholder_device_no_name_text);
                }
                mPresenter.bindDeviceToUser(deviseSn, deviceName);
            } else if(requestCode == sRenameDeviceCode){
                String newName = data.getStringExtra(RenameDeviceDialog.NEW_DEVICE_NAME_INDEX);
                mPresenter.renameCurrentDevice(newName);
            }
        }
    }

    @Override
    public void onLogoutClick(){
        mPresenter.resetFirebaseHelper();
    }

//    @Override
//    public void showDeviceOnlineStatus(boolean isOnline){
//        mMenu.findItem(R.id.menu_item_device_status)
//                .setIcon(isOnline ? R.drawable.ic_device_online :
//                                    R.drawable.ic_device_offline);
//    }

    @Override
    public void showNoConnectionUI(boolean isConnected){
        mNoConnectionTextView.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        mRetryConnectionButton.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        mNoItemsLayout.setVisibility(isConnected ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showLoadingUI(boolean isLoading){
        mNoItemsLayout.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        mRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mLoadingInfoTextView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showLoadingUI(int loadingInfoId){
        showLoadingUI(true);
        mLoadingInfoTextView.setText(getString(loadingInfoId));
    }

    private void showNoItemsUI(boolean isEmpty){
        mNoItemsLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showWidgetList(List<FarhomeWidget> widgets){
        List<FarhomeWidget> adapterWidgets = new ArrayList<>();
        switch (mWidgetCategory){
            case 1:
                for(FarhomeWidget widget : widgets){
                    if(widget instanceof SwitchWidget){
                        adapterWidgets.add(widget);
                    }
                }
                break;
            case 2:
                for(FarhomeWidget widget : widgets){
                    if(widget instanceof InfoWidget){
                        adapterWidgets.add(widget);
                    }
                }
                break;
            case 3:
                for(FarhomeWidget widget : widgets){
                    if(widget instanceof AlarmWidget){
                        adapterWidgets.add(widget);
                    }
                }
                break;
            default:
                adapterWidgets = widgets;
                break;
        }
        mRecyclerView.setAdapter(new WidgetAdapter(adapterWidgets));
        showNoItemsUI(widgets.isEmpty());
    }

    @Override
    public void updateDeviceUI(FarhomeDevice device){
        if(device != null && device.getName() != null){
            MainActivity activity = (MainActivity) getActivity();
            if(activity != null){
                activity.changeToolbarTitle(device.getName());
            }
        }
    }

    @Override
    public void onChangeDeviceClick(){
        mPresenter.onChangeDeviceClick();
    }

    @Override
    public void onAboutDeviceClick(){
        mPresenter.onAboutDeviceClick();
    }

    @Override
    public List<FarhomeWidget> getWidgetList(){
        return ((WidgetAdapter) mRecyclerView.getAdapter()).getItemsList();
    }

    @Override
    public void updateWidget(FarhomeWidget widget){
        List<FarhomeWidget> adapterList = getWidgetList();
        for(int i = 0; i < adapterList.size(); i++){
            FarhomeWidget oldWidget = adapterList.get(i);
            if(oldWidget.getDbkey().equals(widget.getDbkey())){
                oldWidget.setName(widget.getName());
                oldWidget.setTimestamp(widget.getTimestamp());
                if(oldWidget instanceof AlarmWidget){
                    ((AlarmWidget) oldWidget).setValue(widget.getValue());
                } else if(oldWidget instanceof InfoWidget){
                    ((InfoWidget) oldWidget).setValue(widget.getValue());
                } else if(oldWidget instanceof SwitchWidget){
                    ((SwitchWidget) oldWidget).setValue(widget.getValue());
                }
                mRecyclerView.getAdapter().notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void addWidget(FarhomeWidget widget){
        List<FarhomeWidget> adapterList = getWidgetList();
        adapterList.add(widget);
        mRecyclerView.getAdapter().notifyItemInserted(adapterList.size() - 1);
        showNoItemsUI(adapterList.isEmpty());
    }

    @Override
    public void deleteWidget(FarhomeWidget widget){
        List<FarhomeWidget> adapterList = getWidgetList();
        for(int i = 0; i < adapterList.size(); i++){
            FarhomeWidget oldWidget = adapterList.get(i);
            if(oldWidget.getDbkey().equals(widget.getDbkey())){
                adapterList.remove(i);
                mRecyclerView.getAdapter().notifyItemRemoved(i);
                showNoItemsUI(adapterList.isEmpty());
                break;
            }
        }
    }

    @Override
    public void showAddWidgetDialog(){
        Intent intent = new Intent(getContext(), AddWidgetActivity.class);
        intent.putExtra(AddWidgetActivity.IS_EDIT_MODE, false);
        intent.putExtra(AddWidgetActivity.IS_DEV_MODE, false);
        intent.putExtra(AddWidgetActivity.WIDGET_ID, "");
        startActivityForResult(intent, sAddWidgetRequestCode);
    }

    @Override
    public void showAddDeviceDialog(){
        AddDeviceDialog addDeviceDialog = new AddDeviceDialog();
        addDeviceDialog.setTargetFragment(WidgetFragment.this, sAddDeviceCode);
        FragmentActivity activity = getActivity();
        if(activity != null){
            addDeviceDialog.show(getActivity().getSupportFragmentManager(), sAddDeviceTag);
        }
    }

    @Override
    public void showRenameDeviceDialog(String currentName){
        RenameDeviceDialog renameDeviceDialog = RenameDeviceDialog.newInstance(currentName);
        renameDeviceDialog.setTargetFragment(WidgetFragment.this, sRenameDeviceCode);
        FragmentActivity activity = getActivity();
        if(activity != null){
            renameDeviceDialog.show(getActivity().getSupportFragmentManager(), sRenameDeviceTag);
        }
    }

//    @Override
//    public void showEditWidgetDialog(FarhomeWidget widget){
//        Intent intent = new Intent(getContext(), AddWidgetActivity.class);
//        intent.putExtra(AddWidgetActivity.IS_EDIT_MODE, true);
//        intent.putExtra(AddWidgetActivity.IS_DEV_MODE, mIsDevMode);
//        intent.putExtra(AddWidgetActivity.WIDGET_ID, widget.getId().toString());
//        startActivityForResult(intent, sAddWidgetRequestCode);
//    }

    @Override
    public void showChangeDeviceDialog(ArrayList<String> deviceSnList, ArrayList<String> deviceNamesList){
        if(deviceSnList.isEmpty()){
            showMessage(R.string.ui_no_devices_found_text);
        }
        ChangeDeviceDialog deviceDialog = ChangeDeviceDialog
                .newInstance(deviceSnList, deviceNamesList);
        deviceDialog.setTargetFragment(WidgetFragment.this, sChangeDeviceCode);
        FragmentActivity activity = getActivity();
        if(activity != null){
            deviceDialog.show(getActivity().getSupportFragmentManager(), sChangeDeviceTag);
        }
    }

    @Override
    public void showAboutDeviceDialog(AboutDeviceDialog dialog){
        FragmentActivity activity = getActivity();
        if(activity != null){
            dialog.show(getActivity().getSupportFragmentManager(), sAboutDeviceTag);
        }
    }

    @Override
    public void showSendSmsDialog(String message, String phoneNumber){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("sms_body", message);
        smsIntent.putExtra("address"  , phoneNumber);
        try{
            startActivity(smsIntent);
        } catch (ActivityNotFoundException e){
            showMessage(R.string.message_cant_send_sms_text);
        }
    }

    @Override
    public int getDeviceCount(){
        return mPresenter.getDeviceCount();
    }

//    private void showConfirmDeleteDialog(int position){
//        DeleteConfirmationFragment fragment = DeleteConfirmationFragment.newInstance(position);
//        fragment.setTargetFragment(WidgetFragment.this, sConfirmDeleteCode);
//        FragmentActivity activity = getActivity();
//        if(activity != null){
//            fragment.show(getActivity().getSupportFragmentManager(), sConfirmDeleteTag);
//        }
//    }

    private class WidgetHolder extends RecyclerView.ViewHolder{
        private FarhomeWidget mWidget;
        private final TextView mTextViewName;
        private final TextView mTextViewValue;
        private final TextView mTextViewDate;
        private final ImageView mIconImageView;
//        private ImageButton mButtonEdit;
//        private ImageButton mButtonSms;
//        private ImageButton mButtonDelete;

        WidgetHolder(View itemView){
            super(itemView);
            mTextViewName = itemView.findViewById(R.id.widget_item_text_view_name);
            mTextViewValue = itemView.findViewById(R.id.widget_item_text_view_value);
            mTextViewDate = itemView.findViewById(R.id.widget_item_text_view_last_update_time);
            mIconImageView = itemView.findViewById(R.id.widget_item_icon_image_view);
//            mButtonEdit = itemView.findViewById(R.id.widget_item_button_edit);
//            mButtonSms = itemView.findViewById(R.id.widget_item_button_sms);
//            mButtonDelete = itemView.findViewById(R.id.widget_item_button_delete);
//            mButtonDelete.setVisibility(View.GONE);
        }

        void bindWidget(FarhomeWidget widget) {
            mWidget = widget;
            toggleValueLoadingUI(false);

            if(mWidget.getName() == null){
                mTextViewName.setText(R.string.placeholder_device_no_name_text);
            } else {
                if(mWidget.getName().length() > 12){
                    mTextViewName.setTextSize(20);
                } else {
                    mTextViewName.setTextSize(24);
                }
                mTextViewName.setText(mWidget.getName());
            }

            if(mWidget instanceof AlarmWidget){
                bindAlarmWidget();
            } else if(mWidget instanceof SwitchWidget){
                bindSwitchWidget();
            } else if(mWidget instanceof InfoWidget){
                bindInfoWidget();
            }

            mTextViewDate.setText(Utils.formatDate(new Date(mWidget.getTimestamp()),
                    getContext()));

//            mButtonEdit.setOnClickListener(view -> mPresenter.onEditWidgetClick(mWidget));
//            mButtonSms.setOnClickListener(view -> mPresenter.onSendSmsClick(mWidget));
//            mButtonDelete.setOnClickListener(view -> showConfirmDeleteDialog(mPosition));
        }

        private void bindAlarmWidget(){
            mIconImageView.setVisibility(View.VISIBLE);
            mTextViewValue.setVisibility(View.GONE);
            mIconImageView.setImageDrawable(mWidget.getValue() == 0.0f ?
                    getResources().getDrawable(R.drawable.ok_icon) :
                    getResources().getDrawable(R.drawable.alert_icon));
            mIconImageView.setOnClickListener(view -> {
                toggleValueLoadingUI(true);
                mPresenter.onWidgetValueClick(mWidget);
            });
        }

        private void bindSwitchWidget(){
            mIconImageView.setVisibility(View.VISIBLE);
            mTextViewValue.setVisibility(View.GONE);
            mIconImageView.setImageDrawable(mWidget.getValue() == 0.0f ?
                    getResources().getDrawable(R.drawable.power_off) :
                    getResources().getDrawable(R.drawable.power_on));
            mIconImageView.setOnClickListener(view -> {
                toggleValueLoadingUI(true);
                mPresenter.onWidgetValueClick(mWidget);
            });
        }

        private void bindInfoWidget(){
            if(String.valueOf(mWidget.getValue()).length() > 4){
                mTextViewValue.setTextSize(42);
            } else {
                mTextViewValue.setTextSize(48);
            }
            mTextViewValue.setText(String.valueOf(mWidget.getValue()));
        }

        private void toggleValueLoadingUI(boolean isLoading){
            if(mWidget instanceof SwitchWidget){
                mIconImageView.setAlpha(isLoading ? 0.1f : 1f);
            } else {
                mTextViewValue.setAlpha(isLoading ? 0.1f : 1f);
            }
        }
    }

    private class WidgetAdapter extends RecyclerView.Adapter<WidgetHolder>{
        private final List<FarhomeWidget> mWidgets;

        WidgetAdapter(List<FarhomeWidget> list){
            this.mWidgets = list;
        }

        @Override
        public WidgetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.item_small_widget, parent, false);
            return new WidgetHolder(view);
        }

        @Override
        public void onBindViewHolder(WidgetHolder holder, int position) {
            holder.bindWidget(mWidgets.get(position));
        }

        @Override
        public int getItemCount() {
            return mWidgets.size();
        }

        private List<FarhomeWidget> getItemsList(){
            return mWidgets;
        }
    }
}