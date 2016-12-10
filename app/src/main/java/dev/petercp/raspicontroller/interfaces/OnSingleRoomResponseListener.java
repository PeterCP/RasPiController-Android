package dev.petercp.raspicontroller.interfaces;

import android.support.annotation.Nullable;

import dev.petercp.raspicontroller.classes.Room;

public interface OnSingleRoomResponseListener {
    void onSingleRoomResponse(@Nullable Room room);
}
