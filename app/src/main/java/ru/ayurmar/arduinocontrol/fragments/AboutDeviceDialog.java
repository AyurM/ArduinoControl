package ru.ayurmar.arduinocontrol.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.ayurmar.arduinocontrol.R;


public class AboutDeviceDialog extends DialogFragment {
    private static final String sDeviceName = "DEVICE_NAME";
    private static final String sDeviceModel = "DEVICE_MODEL";
    private static final String sDeviceSn = "DEVICE_SN";

    public static AboutDeviceDialog newInstance(String name, String model,
                                                String sn){
        Bundle args = new Bundle();
        args.putString(sDeviceName, name);
        args.putString(sDeviceModel, model);
        args.putString(sDeviceSn, sn);
        AboutDeviceDialog fragment = new AboutDeviceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_about_device, null);
        TextView nameTextView = v.findViewById(R.id.about_device_name_value);
        TextView modelTextView = v.findViewById(R.id.about_device_model_value);
        TextView snTextView = v.findViewById(R.id.about_device_sn_value);

        Button okButton = v.findViewById(R.id.about_device_ok_button);
        okButton.setOnClickListener(view -> dismiss());

        Bundle args = getArguments();
        if(args != null){
            nameTextView.setText(args.getString(sDeviceName, ""));
            modelTextView.setText(args.getString(sDeviceModel, ""));
            snTextView.setText(args.getString(sDeviceSn, ""));
        }
        Dialog dialog = new AlertDialog.Builder( getActivity())
                .setView(v)
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.FarHomeDialogAnimation;
        return dialog;
    }
}