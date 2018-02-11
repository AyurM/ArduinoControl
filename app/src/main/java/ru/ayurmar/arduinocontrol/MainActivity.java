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
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import ru.ayurmar.arduinocontrol.interfaces.view.IWidgetView;
import ru.ayurmar.arduinocontrol.view.InfoFragment;
import ru.ayurmar.arduinocontrol.view.LogoutConfirmationFragment;
import ru.ayurmar.arduinocontrol.view.WidgetFragment;

public class MainActivity extends AppCompatActivity
        implements LogoutConfirmationFragment.LogoutDialogListener,
        PopupMenu.OnMenuItemClickListener{

    private static final String sInfoDialogTag = "INFO_DIALOG_TAG";
    private static final String sConfirmLogoutTag = "CONFIRM_LOGOUT_DIALOG";
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mToolbarTitle;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

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
    public void onLogoutPositiveClick(){
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item){
        FragmentManager fm = getSupportFragmentManager();
        IWidgetView widgetView = (IWidgetView) fm.findFragmentById(R.id.main_fragment_container);
        //всплывающее меню при нажатии на название в тулбаре
        switch (item.getItemId()){
            case R.id.menu_rename_device:
                if(widgetView != null){
                    widgetView.showRenameDeviceDialog(mToolbarTitle.getText().toString());
                }
                return true;
            case R.id.menu_popup_add_device:
                if(widgetView != null){
                    widgetView.showAddDeviceDialog();
                }
                return true;
            case R.id.menu_popup_change_device:
                if(widgetView != null){
                    widgetView.onChangeDeviceClick();
                }
                return true;
            case R.id.menu_popup_about_device:
                if(widgetView != null){
                    widgetView.onAboutDeviceClick();
                }
                return true;
            default:
                return true;
        }
    }

    public void changeToolbarTitle(String title){
        mToolbarTitle.setText(title);
    }

    private void setupToolbarAndDrawer(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbarTitle = toolbar.findViewById(R.id.toolbar_title);

        //всплывающее меню при нажатии на название в тулбаре
        mToolbarTitle.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.device_popup_menu);

            FragmentManager fm = getSupportFragmentManager();
            IWidgetView widgetView = (IWidgetView) fm.findFragmentById(R.id.main_fragment_container);
            int deviceCount = 0;
            //настройка видимости пунктов всплывающего меню
            if(widgetView != null){
                deviceCount = widgetView.getDeviceCount();
            }
            if(deviceCount == 0){
                popupMenu.getMenu().findItem(R.id.menu_rename_device).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_popup_about_device).setVisible(false);
            }
            if(deviceCount <= 1){
                popupMenu.getMenu().findItem(R.id.menu_popup_change_device).setVisible(false);
            }

            popupMenu.show();
        });

        mDrawerLayout = findViewById(R.id.main_layout_drawer);
        mNavigationView = findViewById(R.id.navigation_drawer);

        //отобразить аккаунт пользователя в навигационной панели
        View navHeader = mNavigationView.getHeaderView(0);
        TextView userNameTextView = navHeader.findViewById(R.id.drawer_header_user_email_text_view);
        userNameTextView.setText(mAuth.getCurrentUser() == null ?
                "" : mAuth.getCurrentUser().getEmail());
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
                startActivity(intent);
                break;
            case R.id.menu_about:
                InfoFragment infoFragment = new InfoFragment();
                infoFragment.show(getSupportFragmentManager(), sInfoDialogTag);
                break;
            case R.id.menu_change_device:
                FragmentManager fm = getSupportFragmentManager();
                IWidgetView widgetView = (IWidgetView) fm.findFragmentById(R.id.main_fragment_container);
                widgetView.onChangeDeviceClick();
                break;
            case R.id.menu_logout:
                LogoutConfirmationFragment logoutFragment = new LogoutConfirmationFragment();
                logoutFragment.show(getSupportFragmentManager(), sConfirmLogoutTag);
                break;
            default:
        }
//        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
    }
}