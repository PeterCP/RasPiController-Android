package dev.petercp.raspicontroller.interfaces;

import java.util.List;

import dev.petercp.raspicontroller.classes.Room;

public interface OnRoomListResponseListener {
    void onRoomListResponse(List<Room> rooms);
}
