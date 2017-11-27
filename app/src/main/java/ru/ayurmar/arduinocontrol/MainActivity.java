package ru.ayurmar.arduinocontrol;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import ru.ayurmar.arduinocontrol.view.InfoFragment;
import ru.ayurmar.arduinocontrol.view.WidgetFragment;

public class MainActivity extends AppCompatActivity
        implements InfoFragment.InfoDialogListener {

    private static final String sInfoDialogTag = "INFO_DIALOG_TAG";
    public static final String DEV_MODE = "IS_DEV_MODE";
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsDevMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbarAndDrawer();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.main_fragment_container);
        if (fragment == null) {
            fragment = new WidgetFragment();
            fm.beginTransaction()
                    .add(R.id.main_fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (mDrawerLayout.isDrawerOpen(mNavigationView)){
            mDrawerLayout.closeDrawers();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFinishDialog(boolean isDevMode){
        mIsDevMode = isDevMode;
        FragmentManager fm = getSupportFragmentManager();
        WidgetFragment fragment = (WidgetFragment) fm.findFragmentById(R.id.main_fragment_container);
        if(fragment != null){
            fragment.updateWidgetList();
        }
    }

    public boolean isDevMode(){
        return mIsDevMode;
    }

    private void setupToolbarAndDrawer(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDrawerLayout = findViewById(R.id.main_layout_drawer);
        mNavigationView = findViewById(R.id.navigation_drawer);
        setupDrawerContent(mNavigationView);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open,
                R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            selectDrawerItem(menuItem);
            return true;
        });
    }

    private void selectDrawerItem(MenuItem menuItem){
        switch(menuItem.getItemId()){
            case R.id.menu_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                intent.putExtra(DEV_MODE, mIsDevMode);
                startActivity(intent);
                break;
            case R.id.menu_about:
                InfoFragment fragment = new InfoFragment();
                fragment.show(getSupportFragmentManager(), sInfoDialogTag);
                break;
            default:
        }
//        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
    }
}