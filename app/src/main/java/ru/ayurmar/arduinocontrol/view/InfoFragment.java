package ru.ayurmar.arduinocontrol.view;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ru.ayurmar.arduinocontrol.R;

public class InfoFragment extends DialogFragment{

    private static final int sClicksToUnlockDevMode = 10;
    private int mUnlockDevModeClicks = 0;
    private InfoDialogListener mListener;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (activity instanceof InfoDialogListener) {
            mListener = (InfoDialogListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement InfoDialogListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InfoDialogListener) {
            mListener = (InfoDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InfoDialogListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_info, null);
        TextView textViewVersion = v.findViewById(R.id.info_version_text_view);
        //show hidden "Developer Settings" category
        textViewVersion.setOnClickListener(view -> {
            mUnlockDevModeClicks++;
            if(mUnlockDevModeClicks == sClicksToUnlockDevMode){
                Toast.makeText(getActivity(),
                        R.string.ui_pref_developer_settings_available, Toast.LENGTH_SHORT).show();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.ui_about_text)
                .setNeutralButton(android.R.string.ok,
                        ((dialogInterface, i) -> {
                            mListener.onFinishDialog(mUnlockDevModeClicks >= sClicksToUnlockDevMode);
                            dismiss();
                        }))
                .create();
    }

    public interface InfoDialogListener{
        void onFinishDialog(boolean isDevModeActivated);
    }
}
