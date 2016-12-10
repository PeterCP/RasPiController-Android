package dev.petercp.raspicontroller.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.activities.AddWidgetActivity;
import dev.petercp.raspicontroller.activities.SettingsActivity;
import dev.petercp.raspicontroller.classes.BaseWidget;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.interfaces.OnWidgetListResponseListener;
import dev.petercp.raspicontroller.utils.NetworkHelper;

import static android.app.Activity.RESULT_OK;

public class WidgetListFragment extends ManagedFragment {

    private static final String ARG_ROOM = "room";

    public static WidgetListFragment newInstance(Room room) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ROOM, room);
        WidgetListFragment fragment = new WidgetListFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @SuppressWarnings("FieldCanBeLocal")
    private View rootView;
    private List<Fragment> currentCards;
    private Room room;
    private NetworkHelper networkHelper;

    public WidgetListFragment() {}

    /**
     * Base Fragment methods.
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            room = getArguments().getParcelable(ARG_ROOM);
        } else {
            room = null;
        }

        setHasOptionsMenu(true);

        currentCards = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        networkHelper = new NetworkHelper(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        networkHelper = null;
    }

    /**
     * View and menu methods.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        updateManagerActivityUI();

        rootView = inflater.inflate(R.layout.fragment_widget_list, container, false);

        if (room != null) {
            getManagerActivity().setAppbarTitle(room.getName() + " Widgets");
        }

        updateWidgetList();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_main, menu);
    }

    @Override
    public String getTitle() {
        String base = getContext().getString(R.string.title_activity_widgets);
        if (room == null)
            return base;
        else
            return base + " in " + room.getName();
    }

    /**
     * ManagedFragment methods.
     */

    @Override
    public Integer getNavigationDrawerItemId() {
        if (room == null)
            return R.id.nav_widgets;
        else
            return R.id.nav_rooms;
    }

    @Override
    public boolean hasFAB() {
        return true;
    }

    /**
     * Listener methods.
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivityForResult(intent, SettingsActivity.CODE);
                return true;
            case R.id.action_refresh:
                updateWidgetList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (
                requestCode == SettingsActivity.CODE || requestCode == AddWidgetActivity.CODE))
            updateWidgetList();
    }

    @Override
    public void onFABClicked(View view) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), AddWidgetActivity.class);
            intent.putExtra("room", room);
            startActivityForResult(intent, AddWidgetActivity.CODE);
        }
    }

    /**
     * Extra methods.
     */

    private void updateWidgetList() {
        OnWidgetListResponseListener listener = new OnWidgetListResponseListener() {
            @Override
            public void onWidgetListResponse(List<BaseWidget> widgets) {
                setWidgetList(widgets);
            }
        };
        if (room == null)
            networkHelper.getWidgetIndex(listener);
        else
            networkHelper.getWidgetsInRoom(room, listener);
    }

    private void setWidgetList(List<BaseWidget> widgets) {
        if (isRunning()) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            for (Fragment fragment : currentCards)
                if (fragment != null)
                    transaction.remove(fragment);
            transaction.commit();

            currentCards.clear();
            Fragment fragment;
            transaction = manager.beginTransaction();
            for (BaseWidget widget : widgets) {
                fragment = widget.makeFragment();
                currentCards.add(fragment);
                transaction.add(R.id.card_container, fragment);
            }
            transaction.commit();
        }
    }
}
