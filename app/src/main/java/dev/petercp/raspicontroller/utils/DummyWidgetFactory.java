package dev.petercp.raspicontroller.utils;

import android.support.v4.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.petercp.raspicontroller.classes.BaseWidget;

/**
 * Makes random widgets and fragments from default values.
 */
@SuppressWarnings("all")
public class DummyWidgetFactory {

    private static int DEFAULT_LIST_LENGTH = 5;
    private static Random RANDOM = new Random();
    private static JSONObject[] BLUEPRINTS; // Default widget values.
    static {
        try {
            BLUEPRINTS = new JSONObject[]{
                    new JSONObject().put("type", "raw").put("id", "id")
                            .put("name", "Name").put("value", "Value"),
                    new JSONObject().put("type", "slider").put("id", "id")
                            .put("name", "Name").put("value", RANDOM.nextInt(101)),
                    new JSONObject().put("type", "toggle").put("id", "id")
                            .put("name", "Name").put("value", false),
            };
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static JSONObject randomJson() {
        return BLUEPRINTS[RANDOM.nextInt(BLUEPRINTS.length)];
    }

    public static BaseWidget makeDummyWidget() {
        try {
            return WidgetDispatcher.makeWidgetFromJson(randomJson());
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<BaseWidget> makeDummyWidgetList() {
        return makeDummyWidgetList(DEFAULT_LIST_LENGTH);
    }

    public static List<BaseWidget> makeDummyWidgetList(int amount) {
        List<BaseWidget> widgets = new ArrayList<>();
        for (int i = 0; i < amount; i++)
            widgets.add(makeDummyWidget());
        return widgets;
    }

    public static Fragment makeDummyFragment() {
        return makeDummyWidget().makeFragment();
    }

    public static List<Fragment> makeDummyFragmentList() {
        return makeDummyFragmentList(DEFAULT_LIST_LENGTH);
    }

    public static List<Fragment> makeDummyFragmentList(int amount) {
        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < amount; i++)
            fragments.add(makeDummyFragment());
        return fragments;
    }
}
