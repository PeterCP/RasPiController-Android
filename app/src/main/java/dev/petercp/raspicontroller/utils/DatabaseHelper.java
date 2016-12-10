package dev.petercp.raspicontroller.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.exceptions.SQLQueryException;
import dev.petercp.raspicontroller.classes.BaseWidget;

@SuppressWarnings("WeakerAccess")
public class DatabaseHelper extends SQLiteOpenHelper {

    private static class RoomsTable {
        static final String
                TABLE_NAME = "rooms",

                ID = "id",
                ROOM_ID = "room_id",
                NAME = "name",

                CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                        ID + " INTEGER PRIMARY KEY, " +
                        ROOM_ID + " TEXT NOT NULL, " +
                        NAME + " TEXT NOT NULL" +
                ")",

                DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        static final String[] PROJECTION = { full(ID), full(ROOM_ID), full(NAME) };

        static String full(String column) {
            return TABLE_NAME + "." + column;
        }
    }

    private static class WidgetsTable {
        static final String
                TABLE_NAME = "widget_cache",

                ID = "id",
                WIDGET_ID = "widget_id",
                DATA_JSON = "data_json",

                CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                        ID + " INTEGER PRIMARY KEY, " +
                        WIDGET_ID + " TEXT NOT NULL UNIQUE, " +
                        DATA_JSON + " TEXT NOT NULL" +
                ")",

                DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        static final String[] PROJECTION = { full(ID), full(WIDGET_ID), full(DATA_JSON) };

        static String full(String column) {
            return TABLE_NAME + "." + column;
        }
    }

    private static class WidgetsRoomsTable {
        static final String
                TABLE_NAME = "widgets_rooms",

                WIDGET_ID = "widget_id",
                ROOM_ID = "room_id",

                CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                        WIDGET_ID + " TEXT PRIMARY KEY REFERENCES " + WidgetsTable.TABLE_NAME +
                        "(" + WidgetsTable.WIDGET_ID + ") ON DELETE CASCADE, " +
                        ROOM_ID + " TEXT NOT NULL REFERENCES " + RoomsTable.TABLE_NAME +
                        "(" + RoomsTable.ROOM_ID + ") ON DELETE CASCADE" +
                ")",

                DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @SuppressWarnings("unused")
        static final String[] PROJECTION = { full(WIDGET_ID), full(ROOM_ID) };

        static String full(String column) {
            return TABLE_NAME + "." + column;
        }
    }

    private static final String DB_NAME = "RasPiController.db";
    private static final int DB_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RoomsTable.CREATE);
        db.execSQL(WidgetsTable.CREATE);
        db.execSQL(WidgetsRoomsTable.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WidgetsRoomsTable.DROP);
        db.execSQL(WidgetsTable.DROP);
        db.execSQL(RoomsTable.DROP);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



    /**
     * Widget methods.
     */

    @SuppressWarnings("unused")
    private BaseWidget getWidgetByRowId(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(WidgetsTable.TABLE_NAME, WidgetsTable.PROJECTION,
                "id = ?", new String[] { String.valueOf(id) }, null, null, null);

        if (cursor.moveToFirst() && cursor.getCount() == 1) {
            try {
                JSONObject json = new JSONObject(cursor.getString(2));
                cursor.close();
                return WidgetDispatcher.makeWidgetFromJson(json);
            } catch (JSONException ex) {
                cursor.close();
                throw new RuntimeException(ex);
            }
        } else {
            cursor.close();
            throw new SQLQueryException("An error occurred while querying " +
                    WidgetsTable.TABLE_NAME + " with constraint 'id = " + id + "'");
        }
    }

    public BaseWidget getWidget(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(WidgetsTable.TABLE_NAME, WidgetsTable.PROJECTION,
                "widget_id = ?", new String[] { id }, null, null, null);

        if (cursor.moveToFirst() && cursor.getCount() == 1) {
            try {
                JSONObject json = new JSONObject(cursor.getString(2));
                cursor.close();
                return WidgetDispatcher.makeWidgetFromJson(json);
            } catch (JSONException ex) {
                cursor.close();
                throw new RuntimeException(ex);
            }
        } else {
            cursor.close();
            throw new SQLQueryException("An error occurred while querying " +
                    WidgetsTable.TABLE_NAME + " with constraint 'widget_id = " + id + "'");
        }
    }

    public List<BaseWidget> getAllWidgets() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(WidgetsTable.TABLE_NAME, WidgetsTable.PROJECTION,
                null, null, null, null, null);

        List<BaseWidget> widgets = new ArrayList<>();

        if (cursor.moveToFirst()) {
            JSONObject json;
            do {
                try {
                    json = new JSONObject(cursor.getString(2));
                    widgets.add(WidgetDispatcher.makeWidgetFromJson(json));
                } catch (JSONException ex) {
                    throw new java.lang.RuntimeException(ex);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return widgets;
    }

    public boolean storeAllWidgets(List<BaseWidget> widgets) {
        boolean result = true;
        getWritableDatabase().delete(WidgetsTable.TABLE_NAME, null, null);
        for (BaseWidget widget : widgets) {
            if (!storeWidget(widget))
                result = false;
        }
        return result;
    }

    public boolean storeWidget(BaseWidget widget) {
        try {
            getWidget(widget.getId());
            return updateWidget(widget);
        } catch (SQLQueryException exception) {
            return insertWidget(widget);
        }
    }

    private boolean insertWidget(BaseWidget widget) {
        try {
            ContentValues values = new ContentValues();
            values.put(WidgetsTable.WIDGET_ID, widget.getId());
            values.put(WidgetsTable.DATA_JSON, widget.toJson().toString());
            long id = getWritableDatabase().insertOrThrow(WidgetsTable.TABLE_NAME, null, values);
            return id != -1;
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean updateWidget(BaseWidget widget) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        try {
            values.put(WidgetsTable.DATA_JSON, widget.toJson().toString());
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }

        int rowsAffected = db.update(
                WidgetsTable.TABLE_NAME,
                values,
                "widget_id = ?",
                new String[] { widget.getId() }
        );

        return rowsAffected > 0;
    }

    public boolean deleteWidget(BaseWidget widget) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(WidgetsTable.TABLE_NAME, "widget_id = ?",
                new String[] { widget.getId() });
        return rowsAffected > 0;
    }



    /**
     * Room methods.
     */

    public List<Room> getAllRooms() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(RoomsTable.TABLE_NAME, RoomsTable.PROJECTION,
                null, null, null, null, null);

        List<Room> rooms = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                rooms.add(new Room(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return rooms;
    }

    public Room getRoom(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(RoomsTable.TABLE_NAME, RoomsTable.PROJECTION,
                "room_id = ?", new String[] { id }, null, null, null);

        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            Room room = new Room(cursor);
            cursor.close();
            room.setWidgets(getWidgetsInRoom(room));
            return room;
        } else {
            cursor.close();
            throw new SQLQueryException("An error occurred while querying " +
                    RoomsTable.TABLE_NAME + " with constraint 'room_id = " + id + "'.");
        }
    }

    public boolean storeAllRooms(List<Room> rooms) {
        boolean result = true;
        getWritableDatabase().delete(RoomsTable.TABLE_NAME, null, null);
        for (Room room : rooms) {
            result = result && storeRoom(room);
        }
        return result;
    }

    public boolean storeRoom(Room room) {
        boolean result = true;
        try {
            getRoom(room.getId());
            if (!updateRoom(room))
                result = false;
        } catch (SQLQueryException ex) {
            if (!insertRoom(room))
                result = false;
        }

        for (BaseWidget widget : room.getWidgets()) {
            if (!storeWidget(widget))
                result = false;
            if (!storeWidgetRoomRelationship(widget, room))
                result = false;
        }

        return result;
    }

    public boolean insertRoom(Room room) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RoomsTable.ROOM_ID, room.getId());
        values.put(RoomsTable.NAME, room.getName());
        long id = db.insertOrThrow(RoomsTable.TABLE_NAME, null, values);
        return id != -1;
    }

    public boolean updateRoom(Room room) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RoomsTable.NAME, room.getName());
        int rowsAffected = db.update(
                RoomsTable.TABLE_NAME,
                values,
                "room_id = ?",
                new String[] { room.getId() }
        );
        return rowsAffected > 0;
    }

    public boolean deleteRoom(Room room) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(
                RoomsTable.TABLE_NAME,
                "room_id = ?",
                new String[] { room.getId() }
        );
        return rowsAffected > 0;
    }


    /**
     * Room and widget relationship methods.
     */

    public List<BaseWidget> getWidgetsInRoom(Room room) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                WidgetsTable.TABLE_NAME + " INNER JOIN " + WidgetsRoomsTable.TABLE_NAME +
                        " ON " + WidgetsTable.full(WidgetsTable.WIDGET_ID) +
                        " = " + WidgetsRoomsTable.full(WidgetsRoomsTable.WIDGET_ID),
                WidgetsTable.PROJECTION,
                WidgetsRoomsTable.full(WidgetsRoomsTable.ROOM_ID) + " = ?",
                new String[] { room.getId() },
                null, null, null
        );

        List<BaseWidget> widgets = new ArrayList<>();

        if (cursor.moveToFirst()) {
            JSONObject json;
            do {
                try {
                    json = new JSONObject(cursor.getString(2));
                    widgets.add(WidgetDispatcher.makeWidgetFromJson(json));
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return widgets;
    }

    public Room getRoomForWidget(BaseWidget widget) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                RoomsTable.TABLE_NAME + " INNER JOIN " + WidgetsRoomsTable.TABLE_NAME +
                        " ON " + RoomsTable.full(RoomsTable.ROOM_ID) +
                        " = " + WidgetsRoomsTable.full(WidgetsRoomsTable.ROOM_ID),
                RoomsTable.PROJECTION,
                WidgetsRoomsTable.full(WidgetsRoomsTable.WIDGET_ID) + " = ?",
                new String[] { widget.getId() },
                null, null, null
        );

        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            Room room = new Room(cursor);
            cursor.close();
            return room;
        } else {
            cursor.close();
            throw new SQLQueryException("No room found for widget " + widget.getId() + ".");
        }
    }

    public boolean storeWidgetRoomRelationship(BaseWidget widget, Room room) {
        try {
            getRoomForWidget(widget);
            return updateWidgetRoomRelationship(widget, room);
        } catch (SQLQueryException ex) {
            return insertWidgetRoomRelationship(widget, room);
        }
    }

    private boolean insertWidgetRoomRelationship(BaseWidget widget, Room room) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WidgetsRoomsTable.WIDGET_ID, widget.getId());
        values.put(WidgetsRoomsTable.ROOM_ID, room.getId());

        db.insertOrThrow(WidgetsRoomsTable.TABLE_NAME, null, values);
        return true;
    }

    private boolean updateWidgetRoomRelationship(BaseWidget widget, Room room) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WidgetsRoomsTable.ROOM_ID, room.getId());
        int rowsAffected = db.update(WidgetsRoomsTable.TABLE_NAME, values,
                "widget_id = ?", new String[]{ widget.getId() });
        return rowsAffected > 0;
    }

    public boolean removeWidgetRoomRelationship(BaseWidget widget) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(
                WidgetsRoomsTable.TABLE_NAME,
                WidgetsRoomsTable.WIDGET_ID + " = ?",
                new String[] { widget.getId() }
        );
        return rowsAffected > 0;
    }
}
