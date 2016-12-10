package dev.petercp.raspicontroller.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseWidget implements Parcelable {

    protected String id;
    protected String name;

    protected BaseWidget(Parcel in) {
        String type = in.readString();
        if (!type.equals(getType()))
            throw new ClassCastException("Widget of type '" + type + "' cannot be cast to " +
                    getClass().getName());

        id = in.readString();
        name = in.readString();
        readFromParcel(in);
    }

    public BaseWidget(JSONObject in) throws JSONException {
        String type = in.getString("type");
        if (!type.equals(getType()))
            throw new ClassCastException("Widget of type '" + type + "' cannot be cast to " +
                    getClass().getName());

        id = in.getString("id");
        name = in.getString("name");
        readFromJson(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel out, int flags) {
        out.writeString(getType());
        out.writeString(id);
        out.writeString(name);
        writeToParcel(out);
    }

    public final JSONObject toJson() throws JSONException {
        JSONObject out = new JSONObject();
        out.put("type", getType());
        out.put("id", id);
        out.put("name", name);
        writeToJson(out);
        return out;
    }

    public final RequestParams toRequestParams() {
        RequestParams out = new RequestParams();
        out.put("id", id);
        out.put("name", name);
        out.put("type", getType());
        writeToRequestParams(out);
        return out;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract String getType();

    public abstract Fragment makeFragment();

    protected abstract void writeToJson(JSONObject out) throws JSONException;

    protected abstract void writeToParcel(Parcel out);

    protected abstract void writeToRequestParams(RequestParams out);

    protected abstract void readFromJson(JSONObject in) throws JSONException;

    protected abstract void readFromParcel(Parcel in);

    @Override
    public String toString() {
        return name;
    }
}
