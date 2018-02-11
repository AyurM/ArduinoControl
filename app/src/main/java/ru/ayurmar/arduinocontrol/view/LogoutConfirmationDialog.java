package ru.ayurmar.arduinocontrol.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import ru.ayurmar.arduinocontrol.R;

public class LogoutConfirmationDialog extends DialogFragment {

    public interface LogoutDialogListener{

        void onLogoutPositiveClick();
    }

    LogoutDialogListener mListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (activity instanceof LogoutConfirmationDialog.LogoutDialogListener) {
            mListener = (LogoutConfirmationDialog.LogoutDialogListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement LogoutDialogListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_logout_confirm, null);

        Button cancelButton = v.findViewById(R.id.logout_confirm_cancel_button);
        Button okButton = v.findViewById(R.id.logout_confirm_ok_button);

        cancelButton.setOnClickListener(view -> dismiss());
        okButton.setOnClickListener(view -> mListener.onLogoutPositiveClick());

        Dialog dialog = new AlertDialog.Builder( getActivity())
                .setView(v)
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.FarHomeDialogAnimation;
        return dialog;
    }
}
