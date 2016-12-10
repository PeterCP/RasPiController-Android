package dev.petercp.raspicontroller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.classes.BaseWidget;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.fragments.AddRoomDialogFragment;
import dev.petercp.raspicontroller.fragments.ManagedFragment;
import dev.petercp.raspicontroller.fragments.RoomListFragment;
import dev.petercp.raspicontroller.fragments.WidgetListFragment;
import dev.petercp.raspicontroller.interfaces.OnSingleWidgetResponseListener;
import dev.petercp.raspicontroller.interfaces.WidgetFragmentListener;
import dev.petercp.raspicontroller.utils.ConfigManager;
import dev.petercp.raspicontroller.utils.NetworkHelper;

public class ManagerActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        WidgetFragmentListener,
        AddRoomDialogFragment.OnRoomCreatedListener {


    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private NetworkHelper networkHelper;

    /**
     * Activity methods.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager.updateContextLocale(this);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_main);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationView = (NavigationView) findViewById(R.id.nav_drawer);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFABClicked(view);
            }
        });

        networkHelper = new NetworkHelper(this);

        setActiveFragment(new WidgetListFragment(), false);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ManagedFragment) {
            setTitle(((ManagedFragment) fragment).getTitle());
        }
    }

    /**
     * Extra methods.
     */

    public void setAppbarTitle(String title) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    public void setIsFABVisible(boolean isFABVisible) {
        if (isFABVisible) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    public void setActiveNavigationItem(@IdRes int id) {
        navigationView.setCheckedItem(id);
    }

    private void setActiveFragment(ManagedFragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setActiveFragment(ManagedFragment fragment) {
        setActiveFragment(fragment, true);
    }

    public ManagedFragment getActiveFragment() {
        return (ManagedFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    /**
     * Listener methods.
     */

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_widgets:
                setActiveFragment(new WidgetListFragment());
                break;
            case R.id.nav_rooms:
                setActiveFragment(new RoomListFragment());
                break;
//            case R.id.nav_settings:
//                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
//                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onFABClicked(View view) {
        getActiveFragment().onFABClicked(view);
    }

    /**
     * WidgetFragmentListener methods.
     */

    @Override
    public void onWidgetUpdateRequested(BaseWidget in, OnSingleWidgetResponseListener listener) {
        networkHelper.getWidgetById(in.getId(), listener);
    }

    @Override
    public void onWidgetValueUpdated(BaseWidget widget, OnSingleWidgetResponseListener listener) {
        networkHelper.updateWidget(widget, listener);
    }

    @Override
    public void onWidgetDeleteRequested(BaseWidget widget, OnSingleWidgetResponseListener listener) {
        networkHelper.deleteWidget(widget, listener);
    }

    /**
     * AddRoomDialogFragment methods.
     */

    @Override
    public void onRoomCreated(Room room) {
        ManagedFragment fragment = getActiveFragment();
        if (fragment instanceof RoomListFragment)
            ((RoomListFragment) fragment).updateRoomList();
    }
}
