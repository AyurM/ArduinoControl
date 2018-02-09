package ru.ayurmar.arduinocontrol.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.ayurmar.arduinocontrol.R;

public class RenameDeviceDialog extends DialogFragment {
    public static final String NEW_DEVICE_NAME_INDEX = "NEW_DEVICE_NAME";
    private static final String sCurrentDeviceName = "CURRENT_DEVICE_NAME";

    private EditText mDeviceNameText;
    private TextView mEmptyNameTextView;

    public static RenameDeviceDialog newInstance(String currentName){
        Bundle args = new Bundle();
        args.putString(sCurrentDeviceName, currentName);
        RenameDeviceDialog fragment = new RenameDeviceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_rename_device, null);

        mDeviceNameText = v.findViewById(R.id.dialog_rename_device_name_edit_text);
        Button cancelButton = v.findViewById(R.id.rename_device_cancel_button);
        Button okButton = v.findViewById(R.id.rename_device_ok_button);
        mEmptyNameTextView = v.findViewById(R.id.rename_device_empty_name_error_text);

        String currentName = getArguments().getString(sCurrentDeviceName, "");
        mDeviceNameText.setText(currentName);
        mDeviceNameText.setSelection(currentName.length());

        okButton.setOnClickListener(view -> {
            String deviceName = mDeviceNameText.getText().toString();
            if(deviceName.isEmpty()){
                mEmptyNameTextView.setVisibility(View.VISIBLE);
                return;
            } else if(deviceName.equals(currentName)){
                dismiss();
                return;
            }
            sendResult(deviceName);
            dismiss();
        });
        cancelButton.setOnClickListener(view -> dismiss());

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }

    private void sendResult(String deviceName) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(NEW_DEVICE_NAME_INDEX, deviceName);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
