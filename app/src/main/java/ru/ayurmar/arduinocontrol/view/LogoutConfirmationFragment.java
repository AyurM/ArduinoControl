package ru.ayurmar.arduinocontrol.view;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ru.ayurmar.arduinocontrol.R;

public class LogoutConfirmationFragment extends DialogFragment {

    public interface LogoutDialogListener{

        void onLogoutPositiveClick();

        void onLogoutNegativeClick();
    }

    LogoutDialogListener mListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (activity instanceof LogoutConfirmationFragment.LogoutDialogListener) {
            mListener = (LogoutConfirmationFragment.LogoutDialogListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement LogoutDialogListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.ui_logout_confirmation_title)
                .setNegativeButton(android.R.string.cancel,
                        (dialogInterface, id) -> mListener.onLogoutNegativeClick())
                .setPositiveButton(android.R.string.ok,
                        (dialogInterface, id) -> mListener.onLogoutPositiveClick())
                .create();
    }
}
