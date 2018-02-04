package ru.ayurmar.arduinocontrol.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import ru.ayurmar.arduinocontrol.model.FarhomeDevice;
import ru.ayurmar.arduinocontrol.model.FarhomeWidget;


public class WidgetFragment extends BasicFragment implements IWidgetView {

    private static final int sAddWidgetRequestCode = 0;
    private static final int sConfirmDeleteCode = 1;
    private static final int sChangeDeviceCode = 2;
    private static final String sConfirmDeleteTag = "CONFIRM_DELETE_DIALOG";
    private static final String sChangeDeviceTag = "CHANGE_DEVICE_DIALOG";

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private LinearLayout mNoItemsLayout;
    private Menu mMenu;
    private boolean mIsDevMode;

    @Inject
    IWidgetPresenter<IWidgetView> mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_widget, container, false);

        mNoItemsLayout = view.findViewById(R.id.widget_no_items_layout);
        mProgressBar = view.findViewById(R.id.widget_progress_bar);
        mRecyclerView = view.findViewById(R.id.widget_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPresenter.onAttach(this);
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

    @Override
    public void onResume(){
        super.onResume();
        mPresenter.loadUserDevices();
//        mPresenter.loadDevice();
//        mPresenter.loadWidgets(mDeviceSn);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.menu_main_widget, menu);
        MenuItem addWidgetItem = mMenu.findItem(R.id.menu_item_add_widget);
        addWidgetItem.setVisible(mIsDevMode);   //hide "Add Widget" menu item
        showDeviceOnlineStatus(mPresenter.isDeviceOnline());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_device_status:
                mPresenter.onDeviceStatusClick();
                return true;
            case R.id.menu_item_add_widget:
                mPresenter.onAddWidgetClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == sAddWidgetRequestCode){
//                mPresenter.loadWidgetListFromDb();
            } else if(requestCode == sConfirmDeleteCode){
                mPresenter.deleteWidget(data
                        .getIntExtra(DeleteConfirmationFragment.EXTRA_POSITION, -1));
            } else if(requestCode == sChangeDeviceCode){
                showMessage("Device " +
                        data.getIntExtra(ChangeDeviceDialog.SELECTED_DEVICE_INDEX, -1)
                        + " is selected!");
            }
        }
    }

    @Override
    public void showDeviceOnlineStatus(boolean isOnline){
        mMenu.findItem(R.id.menu_item_device_status)
                .setIcon(isOnline ? R.drawable.ic_device_online :
                                    R.drawable.ic_device_offline);
    }

    @Override
    public void showLoadingUI(boolean isLoading){
        mNoItemsLayout.setAlpha(isLoading ? 0.1f : 1f);
        mRecyclerView.setAlpha(isLoading ? 0.1f : 1f);
        mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateDeviceUI(FarhomeDevice device){
        if(device != null && device.getName() != null){
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(device.getName());
        }
    }

    @Override
    public void showWidgetList(List<FarhomeWidget> widgets){
        mRecyclerView.setAdapter(new WidgetAdapter(widgets));
        showNoItemsUI(widgets.isEmpty());
    }

    @Override
    public void onChangeDeviceClick(){
        mPresenter.onChangeDeviceClick();
    }

    @Override
    public List<FarhomeWidget> getWidgetList(){
        return ((WidgetAdapter) mRecyclerView.getAdapter()).getItemsList();
    }

    @Override
    public void updateWidgetValue(int position){
        mRecyclerView.getAdapter().notifyItemChanged(position);
    }

    public void updateWidgetList(){
        mIsDevMode = ((MainActivity) getActivity()).isDevMode();
        getActivity().invalidateOptionsMenu();
//        mPresenter.loadWidgetListFromDb();
    }

    @Override
    public void showAddWidgetDialog(){
        Intent intent = new Intent(getContext(), AddWidgetActivity.class);
        intent.putExtra(AddWidgetActivity.IS_EDIT_MODE, false);
        intent.putExtra(AddWidgetActivity.IS_DEV_MODE, mIsDevMode);
        intent.putExtra(AddWidgetActivity.WIDGET_ID, "");
        startActivityForResult(intent, sAddWidgetRequestCode);
    }

    @Override
    public void showEditWidgetDialog(FarhomeWidget widget){
//        Intent intent = new Intent(getContext(), AddWidgetActivity.class);
//        intent.putExtra(AddWidgetActivity.IS_EDIT_MODE, true);
//        intent.putExtra(AddWidgetActivity.IS_DEV_MODE, mIsDevMode);
//        intent.putExtra(AddWidgetActivity.WIDGET_ID, widget.getId().toString());
//        startActivityForResult(intent, sAddWidgetRequestCode);
    }

    @Override
    public void showChangeDeviceDialog(List<String> deviceList){
        if(deviceList.isEmpty()){
            showMessage(R.string.ui_no_devices_found_text);
        }
        ChangeDeviceDialog deviceDialog = ChangeDeviceDialog
                .newInstance((ArrayList<String>) deviceList);
        deviceDialog.setTargetFragment(WidgetFragment.this, sChangeDeviceCode);
        deviceDialog.show(getActivity().getSupportFragmentManager(), sChangeDeviceTag);
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

    private void showConfirmDeleteDialog(int position){
        DeleteConfirmationFragment fragment = DeleteConfirmationFragment.newInstance(position);
        fragment.setTargetFragment(WidgetFragment.this, sConfirmDeleteCode);
        fragment.show(getActivity().getSupportFragmentManager(), sConfirmDeleteTag);
    }

    private void showNoItemsUI(boolean isEmpty){
        mNoItemsLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private class WidgetHolder extends RecyclerView.ViewHolder{
        private FarhomeWidget mWidget;
        private int mPosition = -1;
        private TextView mTextViewName;
        private TextView mTextViewValue;
        private TextView mTextViewDate;
        private ImageButton mButtonEdit;
        private ImageButton mButtonSms;
        private ImageButton mButtonDelete;

        WidgetHolder(View itemView){
            super(itemView);
            mTextViewName = itemView.findViewById(R.id.widget_item_text_view_name);
            mTextViewValue = itemView.findViewById(R.id.widget_item_text_view_value);
            mTextViewDate = itemView.findViewById(R.id.widget_item_text_view_last_update_time);
            mButtonEdit = itemView.findViewById(R.id.widget_item_button_edit);
            mButtonSms = itemView.findViewById(R.id.widget_item_button_sms);
            mButtonDelete = itemView.findViewById(R.id.widget_item_button_delete);
            mButtonDelete.setVisibility(mIsDevMode ? View.VISIBLE : View.GONE);

            Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/OpenSans-Regular.ttf");
            mTextViewName.setTypeface(font);
            mTextViewValue.setTypeface(font);
            mTextViewDate.setTypeface(font);
        }

        void bindWidget(FarhomeWidget widget, int position) {
            mWidget = widget;
            mPosition = position;
            mTextViewName.setText(mWidget.getName());
            if(mWidget.getValue().length() > 3){
                mTextViewValue.setTextSize(42);
            } else {
                mTextViewValue.setTextSize(48);
            }
            mTextViewValue.setText(mWidget.getValue());
            mTextViewDate.setText(Utils.formatDate(new Date(mWidget.getTimestamp()),
                    getContext()));

            mTextViewValue.setOnClickListener(view -> mPresenter.onWidgetValueClick(mPosition));
            mButtonEdit.setOnClickListener(view -> mPresenter.onEditWidgetClick(mWidget));
            mButtonSms.setOnClickListener(view -> mPresenter.onSendSmsClick(mWidget));
            mButtonDelete.setOnClickListener(view -> showConfirmDeleteDialog(mPosition));
        }

//        private void toggleValueLoadingUI(boolean isLoading){
//            mTextViewValue.setAlpha(isLoading ? 0.1f : 1f);
//        }
    }

    private class WidgetAdapter extends RecyclerView.Adapter<WidgetHolder>{
        private List<FarhomeWidget> mWidgets;

        WidgetAdapter(List<FarhomeWidget> list){
            this.mWidgets = list;
        }

        @Override
        public WidgetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.widget_item, parent, false);
            return new WidgetHolder(view);
        }

        @Override
        public void onBindViewHolder(WidgetHolder holder, int position) {
            holder.bindWidget(mWidgets.get(position), position);
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