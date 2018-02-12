package ru.ayurmar.arduinocontrol.fragments;

/*
 * Диалоговое окно смены устройства для текущего пользователя
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.ayurmar.arduinocontrol.R;


public class ChangeDeviceDialog extends DialogFragment {

    private static final String sDeviceListIndex = "DEVICE_SN_LIST_INDEX";
    private static final String sDeviceNamesIndex = "DEVICE_NAMES_LIST_INDEX";
    public static final String SELECTED_DEVICE_INDEX = "SELECTED_DEVICE_SN";

    public static ChangeDeviceDialog newInstance(ArrayList<String> deviceSnList,
                                                 ArrayList<String> deviceNamesList){
        Bundle args = new Bundle();
        args.putStringArrayList(sDeviceListIndex, deviceSnList);
        args.putStringArrayList(sDeviceNamesIndex, deviceNamesList);
        ChangeDeviceDialog fragment = new ChangeDeviceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_change_device, null);
        ListView listView = v.findViewById(R.id.change_device_list_view);
        Button cancelButton = v.findViewById(R.id.change_device_cancel_button);

        ArrayList<String> deviceList = getArguments().getStringArrayList(sDeviceListIndex);
        ArrayList<String> deviceNamesList = getArguments().getStringArrayList(sDeviceNamesIndex);

        listView.setAdapter(new ChangeDeviceAdapter(getTargetFragment().getActivity(),
                deviceNamesList));
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if(deviceList != null){
                sendResult(deviceList.get(i));
            }
            dismiss();
        });

        cancelButton.setOnClickListener(view -> dismiss());

        Dialog dialog = new AlertDialog.Builder( getActivity())
                .setView(v)
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.FarHomeDialogAnimation;
        return dialog;
    }

    private void sendResult(String deviceSn) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(SELECTED_DEVICE_INDEX, deviceSn);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    private class ChangeDeviceAdapter extends BaseAdapter{
        Context mContext;
        ArrayList<String> mDeviceList;
        private LayoutInflater mInflater = null;

        ChangeDeviceAdapter(Context context, ArrayList<String> deviceList){
            this.mContext = context;
            this.mDeviceList = deviceList;
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount(){
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position){
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = mInflater.inflate(R.layout.item_change_device, null);
            TextView text = v.findViewById(R.id.item_change_device_name);
            text.setText(mDeviceList.get(position));
            return v;
        }
    }
}
