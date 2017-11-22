package ru.ayurmar.arduinocontrol.view;

import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import ru.ayurmar.arduinocontrol.interfaces.view.IBasicView;


public class BasicFragment extends Fragment implements IBasicView {

    @Override
    public void showMessage(int stringId){
        Toast toast = Toast.makeText(getActivity(), stringId, Toast.LENGTH_SHORT);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if(textView != null){
            textView.setGravity(Gravity.CENTER);
        }
        toast.show();
    }

    @Override
    public void showMessage(String message){
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if(textView != null){
            textView.setGravity(Gravity.CENTER);
        }
        toast.show();
    }

    @Override
    public void showLongMessage(int stringId){
        Toast toast = Toast.makeText(getActivity(), stringId, Toast.LENGTH_LONG);
        TextView textView = toast.getView().findViewById(android.R.id.message);
        if(textView != null){
            textView.setGravity(Gravity.CENTER);
        }
        toast.show();
    }
}
