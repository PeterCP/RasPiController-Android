package dev.petercp.raspicontroller.classes;

import android.os.Parcel;
import android.support.v4.app.Fragment;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import dev.petercp.raspicontroller.fragments.cards.ToggleCardFragment;
import dev.petercp.raspicontroller.utils.WidgetDispatcher;

/**
 * Widget with a simple true|false toggle button.
 */
public class ToggleWidget extends BaseWidget {

    public static final Creator<ToggleWidget> CREATOR = new Creator<ToggleWidget>() {
        @Override
        public ToggleWidget createFromParcel(Parcel in) {
            return new ToggleWidget(in);
        }

        @Override
        public ToggleWidget[] newArray(int i) {
            return new ToggleWidget[0];
        }
    };

    public static final String TYPE = "toggle";



    private boolean value;

    private ToggleWidget(Parcel in) {
        super(in);
    }

    /**
     * Used by {@link WidgetDispatcher}.
     */
    @SuppressWarnings("unused")
    public ToggleWidget(JSONObject in) throws JSONException {
        super(in);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Fragment makeFragment() {
        return ToggleCardFragment.newInstance(this);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        value = in.readByte() != 0;
    }

    @Override
    protected void readFromJson(JSONObject in) throws JSONException {
        value = in.getBoolean("value");
    }

    @Override
    protected void writeToParcel(Parcel out) {
        out.writeByte((byte) (value ? 1 : 0));
    }

    @Override
    protected void writeToJson(JSONObject out) throws JSONException {
        out.put("value", value);
    }

    @Override
    protected void writeToRequestParams(RequestParams out) {
        out.put("value", value);
    }
}
