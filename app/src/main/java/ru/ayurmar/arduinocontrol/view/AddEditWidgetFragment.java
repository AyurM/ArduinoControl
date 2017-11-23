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


import javax.inject.Inject;

import ru.ayurmar.arduinocontrol.MainApp;
import ru.ayurmar.arduinocontrol.R;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.interfaces.presenter.IAddEditWidgetPresenter;
import ru.ayurmar.arduinocontrol.interfaces.view.IAddEditWidgetView;
import ru.ayurmar.arduinocontrol.model.WidgetType;

public class AddEditWidgetFragment extends BasicFragment implements IAddEditWidgetView {

    private static final String sIsEditMode = "IS_EDIT_MODE";
    private static final String sWidgetId = "WIDGET_ID";

    private EditText mEditTextName;
    private EditText mEditTextPin;
    private Spinner mSpinnerType;
    private Button mButtonCancel;
    private Button mButtonOk;

    private WidgetType mSelectedType = null;
    private boolean mIsEditMode;
    private String mWidgetId;

    @Inject
    IAddEditWidgetPresenter<IAddEditWidgetView> mPresenter;

    public static AddEditWidgetFragment newInstance(boolean isEditMode,
                                                    String widgetId) {
        Bundle args = new Bundle();
        args.putBoolean(sIsEditMode, isEditMode);
        args.putString(sWidgetId, widgetId);
        AddEditWidgetFragment fragment = new AddEditWidgetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsEditMode = getArguments().getBoolean(sIsEditMode);
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
        mSpinnerType = view.findViewById(R.id.add_widget_type_spinner);
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

        mButtonCancel = view.findViewById(R.id.add_widget_cancel_button);
        mButtonOk = view.findViewById(R.id.add_widget_ok_button);

        mButtonCancel.setOnClickListener(view1 -> mPresenter.onCancelClick());
        mButtonOk.setOnClickListener(view1 -> mPresenter.onOkClick(mIsEditMode));
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