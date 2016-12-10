package dev.petercp.raspicontroller.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.classes.UsageStatistics;
import dev.petercp.raspicontroller.interfaces.OnRoomListResponseListener;
import dev.petercp.raspicontroller.interfaces.OnSingleRoomResponseListener;
import dev.petercp.raspicontroller.interfaces.OnSingleWidgetResponseListener;
import dev.petercp.raspicontroller.interfaces.OnUsageStatisticsResponseListener;
import dev.petercp.raspicontroller.interfaces.OnWidgetListResponseListener;
import dev.petercp.raspicontroller.classes.BaseWidget;

/**
 * Manages widget interface with server. It can statically create widget and fragment instances
 * from a given type. When instantiated, it requests JSON data from a server and translates it to
 * widget instances, then calls the appropriate response listener with the returned widgets as a
 * parameter.
 */
public class NetworkHelper {

    private Context context;
    private DatabaseHelper databaseHelper;
    private AsyncHttpClient client;

    public NetworkHelper(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        client = new AsyncHttpClient();
        client.setConnectTimeout(4000);
    }

    /**
     * Safely joins a relative path with the base server_url string resource.
     * @param    resId    String resource identifier to fetch from application context.
     * @param    args     Format arguments passed to {@link Context#getString(int, Object...)}.
     * @return   String   Full url with path.
     */
    private String getAbsoluteUrl(int resId, Object... args) {
//        String baseUrl = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
//                        .getString(context.getString(R.string.pref_server_url), ""),
        String baseUrl = ConfigManager.getServerUrl(context),
                path = context.getString(resId, args);

        if (!baseUrl.endsWith("/"))
            baseUrl += "/";
        if (path.startsWith("/"))
            path = path.substring(1);

        return baseUrl + path;
    }


    /**
     * Widgets
     */

    /**
     * Requests a list of all widgets from the server. The response listener accepts a
     * {@link java.util.List} of widgets as it's only argument.
     * @param   listener    Response listener.
     */
    public void getWidgetIndex(final OnWidgetListResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_widgets);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    ArrayList<BaseWidget> widgets = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        BaseWidget widget = WidgetDispatcher.makeWidgetFromJson(response.getJSONObject(i));
                        widgets.add(widget);
                    }
                    String text = context.getString(R.string.message_index_success, widgets.size());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    databaseHelper.storeAllWidgets(widgets);
                    listener.onWidgetListResponse(widgets);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onWidgetListResponse(databaseHelper.getAllWidgets());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onWidgetListResponse(databaseHelper.getAllWidgets());
            }
        });
    }

    /**
     * Requests a widget from the server using its id. The response listener's widget argument
     * may be null.
     * @param   id          Id to request.
     * @param   listener    Response listener.
     */
    public void getWidgetById(final String id, final OnSingleWidgetResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_widgets_single, id);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    BaseWidget widget = WidgetDispatcher.makeWidgetFromJson(response);
                    databaseHelper.storeWidget(widget);
                    String text = context.getString(R.string.message_get_success,
                            widget.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    listener.onSingleWidgetResponse(widget);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleWidgetResponse(databaseHelper.getWidget(id));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleWidgetResponse(databaseHelper.getWidget(id));
            }
        });
    }

    /**
     * Updates a widget on the server and the local cache. The response listener's widget
     * argument may be null.
     * @param   widget     Widget to update.
     * @param   listener   Response listener.
     */
    public void updateWidget(final BaseWidget widget, final OnSingleWidgetResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_widgets_single, widget.getId());
        RequestParams params = widget.toRequestParams();
        client.patch(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    BaseWidget widget = WidgetDispatcher.makeWidgetFromJson(response);
                    databaseHelper.storeWidget(widget);
                    listener.onSingleWidgetResponse(widget);
                    String text = context.getString(R.string.message_update_success, widget.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleWidgetResponse(databaseHelper.getWidget(widget.getId()));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleWidgetResponse(databaseHelper.getWidget(widget.getId()));
            }
        });
    }

    /**
     * Create a widget on the server and add it to the local cache.
     * @param   name       Widget name.
     * @param   type       Widget type.
     * @param   outpin     Widget output pin.
     * @param   room       Room in which the widget is located.
     * @param   listener   Response listener.
     */
    public void createWidget(String name, String type, int outpin, @Nullable Room room,
                             final OnSingleWidgetResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_widgets);
        RequestParams params = new RequestParams();
        params.put("name", name);
        params.put("type", type);
        params.put("outpin", outpin);
        if (room != null) {
            params.put("room", room.getId());
        }
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    BaseWidget widget = WidgetDispatcher.makeWidgetFromJson(response);
                    databaseHelper.storeWidget(widget);
                    if (response.has("room")) {
                        Room room = new Room(response.getJSONObject("room"));
                        databaseHelper.storeRoom(room);
                        databaseHelper.storeWidgetRoomRelationship(widget, room);
                    }
                    String text = context.getString(R.string.message_create_success, widget.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    listener.onSingleWidgetResponse(widget);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                String text = context.getString(R.string.message_http_error, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a widget from the server and local cache.
     * @param   widget     Widget to delete.
     * @param   listener   Response listener.
     */
    public void deleteWidget(final BaseWidget widget, final OnSingleWidgetResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_widgets_single, widget.getId());
        client.delete(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                databaseHelper.deleteWidget(widget);
                listener.onSingleWidgetResponse(widget);
                String text = context.getString(R.string.message_delete_success, widget.getName());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Rooms
     */

    /**
     * Requests a list of all rooms from the server. The response listener accepts a
     * {@link List<Room>} of rooms as it's only argument.
     * @param   listener    Response listener.
     */
    public void getRoomIndex(final OnRoomListResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_rooms);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    List<Room> rooms = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        rooms.add(new Room(response.getJSONObject(i)));
                    }
                    String text = context.getString(R.string.message_index_success, rooms.size());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    databaseHelper.storeAllRooms(rooms);
                    listener.onRoomListResponse(rooms);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onRoomListResponse(databaseHelper.getAllRooms());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onRoomListResponse(databaseHelper.getAllRooms());
            }
        });
    }

    /**
     * Requests a room from the server using its id. The response listener accepts a single
     * {@link Room} object as its only argument.
     * @param   id          Room id to request.
     * @param   listener    Response listener.
     */
    public void getRoomById(final String id, final OnSingleRoomResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_rooms_single, id);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Room room = new Room(response);
                    databaseHelper.storeRoom(room);
                    String text = context.getString(R.string.message_get_success,
                            room.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    listener.onSingleRoomResponse(room);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleRoomResponse(databaseHelper.getRoom(id));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleRoomResponse(databaseHelper.getRoom(id));
            }
        });
    }

    /**
     * Updates a room on the server and the local cache. The response listener accepts a single
     * {@link Room} as its only argument.
     * @param   room       Room to update.
     * @param   listener   Response listener.
     */
    public void updateRoom(final Room room, final OnSingleRoomResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_rooms_single, room.getId());
        RequestParams params = new RequestParams();
        params.put("name", room.getName());
        client.patch(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Room room = new Room(response);
                    databaseHelper.updateRoom(room);
                    listener.onSingleRoomResponse(room);
                    String text = context.getString(R.string.message_update_success, room.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleRoomResponse(databaseHelper.getRoom(room.getId()));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onSingleRoomResponse(databaseHelper.getRoom(room.getId()));
            }
        });
    }

    /**
     * Create a room on the server and add it to the local cache.
     * @param   name       Room name.
     * @param   listener   Response listener.
     */
    public void createRoom(String name, final OnSingleRoomResponseListener listener) {
        RequestParams params = new RequestParams();
        params.put("name", name);
        client.post(getAbsoluteUrl(R.string.path_rooms), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Room room = new Room(response);
                    databaseHelper.storeRoom(room);
                    String text = context.getString(R.string.message_create_success, room.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    listener.onSingleRoomResponse(room);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                String text = context.getString(R.string.message_http_error, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Deletes a room from the server and local cache.
     * @param   room       Room to delete.
     * @param   listener   Response listener.
     */
    public void deleteRoom(final Room room, final OnSingleRoomResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_rooms_single, room.getId());
        client.delete(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                databaseHelper.deleteRoom(room);
                listener.onSingleRoomResponse(room);
                String text = context.getString(R.string.message_delete_success, room.getName());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Extra
     */

    /**
     * Gets all the widgets in a given room from server or local cache.
     * @param   room       Room from which to get the widgets.
     * @param   listener   Response listener.
     */
    public void getWidgetsInRoom(final Room room, final OnWidgetListResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_rooms_single, room.getId());
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Room room = new Room(response);
                    databaseHelper.storeRoom(room);
                    String text = context.getString(R.string.message_get_success,
                            room.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    listener.onWidgetListResponse(room.getWidgets());
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error_fallback, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onWidgetListResponse(databaseHelper.getWidgetsInRoom(room));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error_fallback, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                listener.onWidgetListResponse(databaseHelper.getWidgetsInRoom(room));
            }
        });
    }

    /**
     * Get usage statistics for a given widget.
     *
     * @param   widget     Widget for which to request usage statistics.
     * @param   from       Oldest date to fetch.
     * @param   to         Newest date to fetch.
     * @param   listener   Response listener.
     */
    public void getWidgetUsage(final BaseWidget widget, @Nullable DateTime from, @Nullable DateTime to,
                               final OnUsageStatisticsResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_widgets_usage, widget.getId());
        RequestParams params = new RequestParams();
        params.add("from", DateTimeUtils.toString(from));
        params.add("to", DateTimeUtils.toString(to));
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String text = context.getString(R.string.message_get_success, widget.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    List<UsageStatistics> list = new ArrayList<>();
                    list.add(new UsageStatistics(response));
                    listener.onUsageStatisticsResponse(list);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get usage statistics for a given room.
     * @param   room       Room for which to request usage statistics.
     * @param   from       Oldest date to fetch.
     * @param   to         Newest date to fetch.
     * @param   listener   Response listener.
     */
    public void getRoomUsage(final Room room, @Nullable DateTime from, @Nullable DateTime to,
                             final OnUsageStatisticsResponseListener listener) {
        String url = getAbsoluteUrl(R.string.path_rooms_usage, room.getId());
        RequestParams params = new RequestParams();
        params.add("from", DateTimeUtils.toString(from));
        params.add("to", DateTimeUtils.toString(to));
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    String text = context.getString(R.string.message_get_success, room.getName());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    List<UsageStatistics> list = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++)
                        list.add(new UsageStatistics(response.getJSONObject(i)));
                    listener.onUsageStatisticsResponse(list);
                } catch (JSONException ex) {
                    String text = context.getString(R.string.message_error, ex.getMessage());
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                String text = context.getString(R.string.message_http_error, statusCode);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String text = context.getString(R.string.message_error, throwable.getMessage());
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
