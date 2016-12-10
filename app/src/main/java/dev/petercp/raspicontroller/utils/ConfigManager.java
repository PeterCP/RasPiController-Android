package dev.petercp.raspicontroller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class ConfigManager {

    public static void setServerUrl(Context context, String serverUrl) {
        SharedPreferences.Editor editor = context.getSharedPreferences("app_preferences",
                Context.MODE_PRIVATE).edit();
        editor.putString("server_url", serverUrl);
        editor.apply();
    }

    public static String getServerUrl(Context context) {
        return context.getSharedPreferences("app_preferences",
                Context.MODE_PRIVATE).getString("server_url", "");
    }

    public static void setLocale(Context context, String locale) {
        SharedPreferences.Editor editor = context.getSharedPreferences("app_preferences",
                Context.MODE_PRIVATE).edit();
        editor.putString("app_locale", locale);
        editor.apply();
    }

    public static String getLocale(Context context) {
        return context.getSharedPreferences("app_preferences",
                Context.MODE_PRIVATE).getString("app_locale", "");
    }

    public static void updateContextLocale(Context context) {
        String localeString = context.getSharedPreferences("app_preferences",
                Context.MODE_PRIVATE).getString("app_locale", "");

        Locale locale;
        if (localeString.isEmpty())
            locale = Locale.getDefault();
        else
            locale = new Locale(localeString);

        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, null);
    }
}
