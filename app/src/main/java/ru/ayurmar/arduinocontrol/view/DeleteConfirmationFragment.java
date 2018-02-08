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

import ru.ayurmar.arduinocontrol.R;

public class DeleteConfirmationFragment extends DialogFragment {

    private static final String ARG_POSITION = "WIDGET_POSITION";
    public static final String EXTRA_POSITION = "EXTRA_WIDGET_POSITION";

    public static DeleteConfirmationFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        DeleteConfirmationFragment fragment = new DeleteConfirmationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_delete_confirmation, null);

        int widgetPosition = getArguments().getInt(ARG_POSITION);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.ui_delete_confirmation_title)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(android.R.string.ok,
                        ((dialogInterface, i) -> sendResult(widgetPosition)))
                .create();
    }

    private void sendResult(int position) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION, position);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}