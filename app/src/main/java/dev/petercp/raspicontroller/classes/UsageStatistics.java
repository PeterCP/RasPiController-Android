package dev.petercp.raspicontroller.classes;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.petercp.raspicontroller.utils.DateTimeUtils;
import dev.petercp.raspicontroller.utils.WidgetDispatcher;

public class UsageStatistics {

    public class UsageEntry {
        private float usage;
        private DateTime date;

        private UsageEntry(JSONObject json) throws JSONException {
            date = DateTimeUtils.fromString(json.getString("date"));
            usage = (float) json.getDouble("usage");
        }

        public DateTime getDate() {
            return date;
        }

        public float getUsage() {
            return usage;
        }
    }

    private BaseWidget widget;
    private DateTime from, to;
    private List<UsageEntry> usages;

    public UsageStatistics(JSONObject json) throws JSONException {
        usages = new ArrayList<>();
        widget = WidgetDispatcher.makeWidgetFromJson(json.getJSONObject("widget"));
        from = DateTimeUtils.fromString(json.getString("from"));
        to = DateTimeUtils.fromString(json.getString("to"));
        JSONArray array = json.getJSONArray("usages");
        for (int i = 0; i < array.length(); i++)
            usages.add(new UsageEntry(array.getJSONObject(i)));
    }

    public BaseWidget getWidget() {
        return widget;
    }

    public List<UsageEntry> getUsages() {
        return usages;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }
}
