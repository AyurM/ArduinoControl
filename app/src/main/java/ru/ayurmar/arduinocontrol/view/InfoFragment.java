package ru.ayurmar.arduinocontrol.view;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ru.ayurmar.arduinocontrol.R;

public class InfoFragment extends DialogFragment{

    private TextView mTextViewVersion;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_info, null);
        mTextViewVersion = v.findViewById(R.id.info_version_text_view);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.ui_about_text)
                .setNeutralButton(android.R.string.ok,
                        ((dialogInterface, i) -> dismiss()))
                .create();
    }
}
