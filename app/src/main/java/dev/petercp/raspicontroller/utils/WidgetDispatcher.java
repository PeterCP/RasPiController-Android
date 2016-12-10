package dev.petercp.raspicontroller.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.petercp.raspicontroller.classes.BaseWidget;
import dev.petercp.raspicontroller.classes.SliderWidget;
import dev.petercp.raspicontroller.classes.ToggleWidget;

/**
 * Factory to create widget and fragment instances from a given type string.
 */
public class WidgetDispatcher {

    /**
     * Maps used as registry for type and widget class relation.
     */
    private static Map<String, Class<? extends BaseWidget>> WIDGET_MAP = new HashMap<>();

    /**
     * Adds a widget type to the registry, along with its associated widget.
     * It should preferably be called from a static block inside the widget class.
     * @param   type        Widget type string defined in the widget class {@link String TYPE}
     *                      static field.
     * @param   widgetCls   Widget class.
     */
    private static void registerWidgetType(String type, Class<? extends BaseWidget> widgetCls) {
        WIDGET_MAP.put(type, widgetCls);
    }

    /**
     * Returns a list of widget type names.
     * @return   {@link List<String>} of widget type names.
     */
    public static List<String> getWidgetTypes() {
        return new ArrayList<>(WIDGET_MAP.keySet());
    }

    /**
     * Statically register widget types.
     */
    static {
        registerWidgetType(SliderWidget.TYPE, SliderWidget.class);
        registerWidgetType(ToggleWidget.TYPE, ToggleWidget.class);
    }

    /**
     * Reads the {@link String type} value from the given {@link JSONObject} and creates a new
     * instance of the given widget type.
     * @param    json   JSON object to parse.
     * @return          Widget instance created from the given JSON object parameter.
     *                  Return value is cast as a {@link BaseWidget}.
     * @throws JSONException
     */
    public static BaseWidget makeWidgetFromJson(JSONObject json) throws JSONException {
        try {
            Class<? extends BaseWidget> cls = WIDGET_MAP.get(json.getString("type"));
            Constructor<? extends BaseWidget> constructor = cls.getDeclaredConstructor(JSONObject.class);
            return constructor.newInstance(json);
        } catch (NoSuchMethodException | IllegalAccessException |
                InvocationTargetException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Constructor is private so that the class is never instantiated.
     */
    private WidgetDispatcher() {}
}
