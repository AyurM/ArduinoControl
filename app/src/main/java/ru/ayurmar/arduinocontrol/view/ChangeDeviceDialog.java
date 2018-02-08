package ru.ayurmar.arduinocontrol.view;

/*
 * Диалоговое окно смены устройства для текущего пользователя
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

        ArrayList<String> deviceList = getArguments().getStringArrayList(sDeviceListIndex);
        ArrayList<String> deviceNamesList = getArguments().getStringArrayList(sDeviceNamesIndex);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getTargetFragment().getActivity(),
                android.R.layout.simple_list_item_1,
                deviceNamesList == null ? new ArrayList<>() : deviceNamesList);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            if(deviceList != null){
                sendResult(deviceList.get(i));
            }
            dismiss();
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
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
}
