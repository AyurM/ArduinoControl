package ru.ayurmar.arduinocontrol.fragments;

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

public class AddDeviceDialog extends DialogFragment {
    public static final String ADDED_DEVICE_SN_INDEX = "ADDED_DEVICE_SN";
    public static final String ADDED_DEVICE_NAME_INDEX = "ADDED_DEVICE_NAME";

    private EditText mSerialNumberText;
    private EditText mDeviceNameText;
    private TextView mEmptySnTextView;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_add_device, null);

        mSerialNumberText = v.findViewById(R.id.dialog_add_device_sn_edit_text);
        mDeviceNameText = v.findViewById(R.id.dialog_add_device_name_edit_text);
        Button cancelButton = v.findViewById(R.id.add_device_cancel_button);
        Button okButton = v.findViewById(R.id.add_device_ok_button);
        mEmptySnTextView = v.findViewById(R.id.add_device_empty_sn_error_text);

        okButton.setOnClickListener(view -> {
            String deviceSn = mSerialNumberText.getText().toString();
            String deviceName =  mDeviceNameText.getText().toString();
            if(deviceSn.isEmpty()){
                mEmptySnTextView.setVisibility(View.VISIBLE);
                return;
            }
            sendResult(deviceSn, deviceName);
            dismiss();
        });
        cancelButton.setOnClickListener(view -> dismiss());

        Dialog dialog = new AlertDialog.Builder( getActivity())
                .setView(v)
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.FarHomeDialogAnimation;
        return dialog;
    }

    private void sendResult(String deviceSn, String deviceName) {
        if(getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(ADDED_DEVICE_SN_INDEX, deviceSn);
        intent.putExtra(ADDED_DEVICE_NAME_INDEX, deviceName);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
