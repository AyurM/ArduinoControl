package ru.ayurmar.arduinocontrol.view;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import ru.ayurmar.arduinocontrol.MainApp;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IAddEditWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IAddEditWidgetView;
import ru.ayurmar.arduinocontrol.model.WidgetType;

public class AddEditWidgetFragment extends BasicFragment implements IAddEditWidgetView {

    private static final String sIsEditMode = "IS_EDIT_MODE";
    private static final String sIsDevMode = "IS_DEV_MODE_EDIT_FRAGMENT";
    private static final String sWidgetId = "WIDGET_ID";

    private EditText mEditTextName;
    private EditText mEditTextPin;
    private Spinner mSpinnerType;

    private WidgetType mSelectedType = null;
    private boolean mIsEditMode;
    private boolean mIsDevMode;
    private String mWidgetId;

    @Inject
    IAddEditWidgetPresenter<IAddEditWidgetView> mPresenter;

    public static AddEditWidgetFragment newInstance(boolean isEditMode,
                                                    boolean isDevMode,
                                                    String widgetId) {
        Bundle args = new Bundle();
        args.putBoolean(sIsEditMode, isEditMode);
        args.putBoolean(sIsDevMode, isDevMode);
        args.putString(sWidgetId, widgetId);
        AddEditWidgetFragment fragment = new AddEditWidgetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsEditMode = getArguments().getBoolean(sIsEditMode);
        mIsDevMode = getArguments().getBoolean(sIsDevMode);
        if(mIsEditMode){
            mWidgetId = getArguments().getString(sWidgetId);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_widget, container, false);
        mEditTextName = view.findViewById(R.id.add_widget_name_edit_text);
        mEditTextPin = view.findViewById(R.id.add_widget_pin_edit_text);
        TextView textViewPin = view.findViewById(R.id.add_widget_pin_text_view);
        mSpinnerType = view.findViewById(R.id.add_widget_type_spinner);
        TextView textViewType = view.findViewById(R.id.add_widget_type_text_view);

        mEditTextPin.setVisibility(mIsDevMode ? View.VISIBLE : View.GONE);
        mSpinnerType.setVisibility(mIsDevMode ? View.VISIBLE : View.GONE);
        textViewPin.setVisibility(mIsDevMode ? View.VISIBLE : View.GONE);
        textViewType.setVisibility(mIsDevMode ? View.VISIBLE : View.GONE);

        fillTypeSpinner();

        if(mIsEditMode){
            mPresenter.loadWidgetToEdit(mWidgetId);
        }

        mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        mSelectedType = WidgetType.BUTTON;
                        break;
                    case 1:
                        mSelectedType = WidgetType.DISPLAY;
                        break;
                    default:
                        mSelectedType = WidgetType.ALARM_SENSOR;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSelectedType = WidgetType.BUTTON;
            }
        });

        Button buttonCancel = view.findViewById(R.id.add_widget_cancel_button);
        Button buttonOk = view.findViewById(R.id.add_widget_ok_button);

        buttonCancel.setOnClickListener(view1 -> mPresenter.onCancelClick());
        buttonOk.setOnClickListener(view1 -> mPresenter.onOkClick(mIsEditMode, mIsDevMode));
        mPresenter.onAttach(this);
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        MainApp.getInstance().getFragmentComponent().inject(this);
    }

    @Override
    public void onDestroy(){
        mPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public void fillEditForm(IWidget widget){
        mEditTextName.setText(widget.getName());
        mEditTextPin.setText(widget.getPin());
        mSpinnerType.setSelection(widget.getWidgetType().ordinal());
    }

    @Override
    public String getWidgetName(){
        if(mEditTextName.getText().toString().isEmpty()){
            return null;
        } else {
            return mEditTextName.getText().toString();
        }
    }

    @Override
    public String getWidgetPin(){
        if(mEditTextPin.getText().toString().isEmpty()){
            return null;
        } else {
            return mEditTextPin.getText().toString();
        }
    }

    @Override
    public WidgetType getWidgetType(){
        return mSelectedType;
    }

    @Override
    public void closeDialog(boolean isWidgetListChanged){
        if(isWidgetListChanged){
            getActivity().setResult(Activity.RESULT_OK, null);
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED, null);
        }
        getActivity().finish();
    }

    private void fillTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.widget_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerType.setAdapter(adapter);
    }
}