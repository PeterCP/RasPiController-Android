package dev.petercp.raspicontroller.classes;

import android.os.Parcel;
import android.support.v4.app.Fragment;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import dev.petercp.raspicontroller.fragments.cards.SliderCardFragment;
import dev.petercp.raspicontroller.utils.WidgetDispatcher;

/**
 * Widget with a numeric value slider.
 */
public class SliderWidget extends BaseWidget {

    public static final Creator<SliderWidget> CREATOR = new Creator<SliderWidget>() {
        @Override
        public SliderWidget createFromParcel(Parcel in) {
            return new SliderWidget(in);
        }

        @Override
        public SliderWidget[] newArray(int i) {
            return new SliderWidget[0];
        }
    };

    public static final String TYPE = "slider";



    private int value;

    private SliderWidget(Parcel in) {
        super(in);
    }

    /**
     * Used by {@link WidgetDispatcher}.
     */
    @SuppressWarnings("unused")
    public SliderWidget(JSONObject in) throws JSONException {
        super(in);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Fragment makeFragment() {
        return SliderCardFragment.newInstance(this);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        value = in.readInt();
    }

    @Override
    protected void readFromJson(JSONObject in) throws JSONException {
        value = in.getInt("value");
    }

    @Override
    protected void writeToParcel(Parcel out) {
        out.writeInt(value);
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
