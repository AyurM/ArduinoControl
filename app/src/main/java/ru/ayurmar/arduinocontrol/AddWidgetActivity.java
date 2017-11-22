package ru.ayurmar.arduinocontrol;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import ru.ayurmar.arduinocontrol.view.AddEditWidgetFragment;

public class AddWidgetActivity extends AppCompatActivity {

    public static final String IS_EDIT_MODE = "IS_EDIT_MODE_ADD_WIDGET_ACTIVITY";
    public static final String WIDGET_ID = "WIDGET_ID_ADD_WIDGET_ACTIVITY";

    private boolean mIsEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_widget);

        Toolbar toolbar = findViewById(R.id.add_widget_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mIsEditMode = getIntent().getBooleanExtra(IS_EDIT_MODE, false);
        getSupportActionBar().setTitle(mIsEditMode ?
                R.string.ui_menu_edit_widget_text : R.string.ui_menu_add_widget_text);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.add_widget_fragment_container);
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.add_widget_fragment_container, fragment)
                    .commit();
        }
    }

    private Fragment createFragment(){
        String widget_id = getIntent().getStringExtra(WIDGET_ID);
        return AddEditWidgetFragment.newInstance(mIsEditMode, widget_id);
    }
}