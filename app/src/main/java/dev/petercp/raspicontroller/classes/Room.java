package dev.petercp.raspicontroller.classes;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.petercp.raspicontroller.utils.WidgetDispatcher;

public class Room implements Parcelable {

    public static final Parcelable.Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    private String id;
    private String name;

    private List<BaseWidget> widgets = new ArrayList<>();

    private Room(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Room(JSONObject json) throws JSONException {
        id = json.getString("id");
        name = json.getString("name");
        if (json.has("widgets")) {
            JSONArray array = json.getJSONArray("widgets");
            for (int i = 0; i < array.length(); i++) {
                widgets.add(WidgetDispatcher.makeWidgetFromJson(array.getJSONObject(i)));
            }
        }
    }

    public Room(Cursor cursor) {
        id = cursor.getString(1);
        name = cursor.getString(2);
    }

    public List<BaseWidget> getWidgets() {
        return widgets;
    }

    public void setWidgets(List<BaseWidget> widgets) {
        this.widgets = widgets;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Room) {
            return id.equals(((Room) obj).id);
        } else {
            return super.equals(obj);
        }
    }
}
