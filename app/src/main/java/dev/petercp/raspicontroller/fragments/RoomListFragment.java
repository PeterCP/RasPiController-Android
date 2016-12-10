package dev.petercp.raspicontroller.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.activities.ChartActivity;
import dev.petercp.raspicontroller.activities.SettingsActivity;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.interfaces.OnRoomListResponseListener;
import dev.petercp.raspicontroller.interfaces.OnSingleRoomResponseListener;
import dev.petercp.raspicontroller.utils.NetworkHelper;

public class RoomListFragment extends ManagedFragment {

    @SuppressWarnings("FieldCanBeLocal")
    private View rootView;
    private ListView roomsListView;
    private ArrayAdapter<Room> roomsAdapter;

    private NetworkHelper networkHelper;

    public RoomListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateManagerActivityUI();

        rootView = inflater.inflate(R.layout.fragment_room_list, container, false);

        roomsListView = (ListView) rootView.findViewById(R.id.rooms_list);
        roomsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Room room = (Room) roomsListView.getItemAtPosition(i);
                getManagerActivity().setActiveFragment(WidgetListFragment.newInstance(room));
            }
        });

        roomsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_selectable_list_item,
                new ArrayList<Room>()
        );

        roomsListView.setAdapter(roomsAdapter);

        registerForContextMenu(roomsListView);

        updateRoomList();

        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.room_context, menu);
        menu.setHeaderTitle(R.string.room_options);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Room room = roomsAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.action_usage:
                ChartActivity.startNew(getContext(), room);
                return true;
            case R.id.action_delete:
                networkHelper.deleteRoom(room, new OnSingleRoomResponseListener() {
                    @Override
                    public void onSingleRoomResponse(@Nullable Room r) {
                        roomsAdapter.remove(room);
                    }
                });
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_main, menu);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.title_activity_rooms);
    }

    @Override
    public Integer getNavigationDrawerItemId() {
        return R.id.nav_rooms;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivityForResult(intent, SettingsActivity.CODE);
                return true;
            case R.id.action_refresh:
                updateRoomList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == SettingsActivity.CODE)
            updateRoomList();
    }

    @Override
    public boolean hasFAB() {
        return true;
    }

    @Override
    public void onFABClicked(View view) {
        AddRoomDialogFragment fragment = new AddRoomDialogFragment();
        fragment.show(getActivity().getSupportFragmentManager(), "addRoom");
    }

    public void updateRoomList() {
        networkHelper.getRoomIndex(new OnRoomListResponseListener() {
            @Override
            public void onRoomListResponse(List<Room> rooms) {
                setRoomList(rooms);
            }
        });
    }

    private void setRoomList(List<Room> rooms) {
        if (getContext() != null) {
            roomsAdapter.clear();
            roomsAdapter.addAll(rooms);
            roomsAdapter.notifyDataSetChanged();
        }
    }

}
