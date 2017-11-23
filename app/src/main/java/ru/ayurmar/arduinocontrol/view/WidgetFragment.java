package ru.ayurmar.arduinocontrol.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import ru.ayurmar.arduinocontrol.AddWidgetActivity;
import ru.ayurmar.arduinocontrol.MainApp;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.Utils;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;


public class WidgetFragment extends BasicFragment implements IWidgetView {

    private static final int sAddWidgetRequestCode = 0;
    private RecyclerView mRecyclerView;
    private TextView mTextViewNoItems;
    private ProgressBar mProgressBar;
    private Menu mMenu;

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

        mTextViewNoItems = view.findViewById(R.id.widget_text_view_no_items);
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
        mPresenter.loadWidgetListFromDb();
        mPresenter.checkDeviceOnlineStatus();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.menu_main_widget, menu);
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
        if (resultCode == Activity.RESULT_OK && requestCode == sAddWidgetRequestCode) {
            mPresenter.loadWidgetListFromDb();
        }
    }

    @Override
    public void showDeviceOnlineStatus(boolean isOnline){
        mMenu.findItem(R.id.menu_item_device_status)
                .setIcon(isOnline ? R.drawable.ic_device_online :
                                    R.drawable.ic_device_offline);
    }

    @Override
    public void showNoItemsUI(boolean isEmpty){
        mTextViewNoItems.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showLoadingUI(boolean isLoading){
        mTextViewNoItems.setAlpha(isLoading ? 0.1f : 1f);
        mRecyclerView.setAlpha(isLoading ? 0.1f : 1f);
        mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showWidgetList(List<IWidget> widgets){
        mRecyclerView.setAdapter(new WidgetAdapter(widgets));
        showNoItemsUI(widgets.isEmpty());
    }

    @Override
    public List<IWidget> getWidgetList(){
        return ((WidgetAdapter) mRecyclerView.getAdapter()).getItemsList();
    }

    @Override
    public void updateWidgetValue(int position){
        mRecyclerView.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void showAddWidgetDialog(){
        Intent intent = new Intent(getContext(), AddWidgetActivity.class);
        intent.putExtra(AddWidgetActivity.IS_EDIT_MODE, false);
        intent.putExtra(AddWidgetActivity.WIDGET_ID, "");
        startActivityForResult(intent, sAddWidgetRequestCode);
    }

    @Override
    public void showEditWidgetDialog(IWidget widget){
        Intent intent = new Intent(getContext(), AddWidgetActivity.class);
        intent.putExtra(AddWidgetActivity.IS_EDIT_MODE, true);
        intent.putExtra(AddWidgetActivity.WIDGET_ID, widget.getId().toString());
        startActivityForResult(intent, sAddWidgetRequestCode);
    }

    @Override
    public void showSendSmsDialog(String message, String phoneNumber){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("sms_body", message);
        smsIntent.putExtra("address"  , phoneNumber);
        startActivity(smsIntent);
    }

    private class WidgetHolder extends RecyclerView.ViewHolder{
        private IWidget mWidget;
        private int mPosition = -1;
        private TextView mTextViewName;
        private TextView mTextViewValue;
        private TextView mTextViewDate;
        private ImageButton mButtonEdit;
        private ImageButton mButtonSms;
        private ImageButton mButtonDelete;

        WidgetHolder(View itemView){
            super(itemView);
            mTextViewName = itemView.findViewById(R.id.widget_text_view_name);
            mTextViewValue = itemView.findViewById(R.id.widget_text_view_value);
            mTextViewDate = itemView.findViewById(R.id.widget_text_view_last_update_time);
            mButtonEdit = itemView.findViewById(R.id.widget_button_edit);
            mButtonSms = itemView.findViewById(R.id.widget_button_sms);
            mButtonDelete = itemView.findViewById(R.id.widget_button_delete);

            Typeface font = Typeface.createFromAsset(getActivity().getAssets(),
                    "fonts/OpenSans-Regular.ttf");
            mTextViewName.setTypeface(font);
            mTextViewValue.setTypeface(font);
            mTextViewDate.setTypeface(font);
        }

        void bindWidget(IWidget widget, int position) {
            mWidget = widget;
            mPosition = position;
            mTextViewName.setText(mWidget.getName());
            if(mWidget.getValue().length() > 3){
                mTextViewValue.setTextSize(42);
            } else {
                mTextViewValue.setTextSize(48);
            }
            mTextViewValue.setText(mWidget.getValue());
            mTextViewDate.setText(Utils.formatDate(mWidget.getLastUpdateTime(), getContext()));

            mTextViewValue.setOnClickListener(view -> mPresenter.onWidgetValueClick(mPosition));
            mButtonEdit.setOnClickListener(view -> mPresenter.onEditWidgetClick(mWidget));
            mButtonSms.setOnClickListener(view -> mPresenter.onSendSmsClick(mWidget));
            mButtonDelete.setOnClickListener(view -> mPresenter.onDeleteWidgetClick(mWidget));
        }
    }

    private class WidgetAdapter extends RecyclerView.Adapter<WidgetHolder>{
        private List<IWidget> mWidgets;

        WidgetAdapter(List<IWidget> list){
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

        private List<IWidget> getItemsList(){
            return mWidgets;
        }
    }
}