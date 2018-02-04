package ru.ayurmar.arduinocontrol.view;

/**
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

    private static final String sDeviceListIndex = "DEVICE_LIST_INDEX";
    public static final String SELECTED_DEVICE_INDEX = "SELECTED_DEVICE_INDEX";

    public static ChangeDeviceDialog newInstance(ArrayList<String> deviceList){
        Bundle args = new Bundle();
        args.putStringArrayList(sDeviceListIndex, deviceList);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getTargetFragment().getActivity(),
                android.R.layout.simple_list_item_1,
                deviceList == null ? new ArrayList<>() : deviceList);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            sendResult(i);
            dismiss();
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.ui_menu_select_device_text)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void sendResult(int position) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(SELECTED_DEVICE_INDEX, position);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
