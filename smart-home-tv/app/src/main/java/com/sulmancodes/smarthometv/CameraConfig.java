package com.sulmancodes.smarthometv;

import android.content.Context;
import android.content.SharedPreferences;

final class CameraConfig {
    private static final String PREFS = "smart_home_settings";

    private CameraConfig() { }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String cameraName(Context c) { return prefs(c).getString("camera_name", "Living room camera"); }
    static void saveCameraName(Context c, String value) {
        prefs(c).edit().putString("camera_name", value == null ? "" : value.trim()).apply();
    }
    static String cameraUrl(Context c) { return SecretStore.get(c, "camera_url"); }
    static void saveCameraUrl(Context c, String value) { SecretStore.put(c, "camera_url", value == null ? "" : value.trim()); }
    static boolean cameraEnabled(Context c) { return prefs(c).getBoolean("camera_enabled", true); }
    static void setCameraEnabled(Context c, boolean value) { prefs(c).edit().putBoolean("camera_enabled", value).apply(); }

    static String cricketApiKey(Context c) { return SecretStore.get(c, "cricket_api_key"); }
    static void saveCricketApiKey(Context c, String value) { SecretStore.put(c, "cricket_api_key", value == null ? "" : value.trim()); }
    static boolean cricketEnabled(Context c) { return prefs(c).getBoolean("cricket_enabled", true); }
    static void setCricketEnabled(Context c, boolean value) { prefs(c).edit().putBoolean("cricket_enabled", value).apply(); }
    static int cricketRefreshMinutes(Context c) { return Math.max(15, prefs(c).getInt("cricket_refresh_minutes", 15)); }
    static void setCricketRefreshMinutes(Context c, int value) {
        prefs(c).edit().putInt("cricket_refresh_minutes", Math.max(15, Math.min(60, value))).apply();
    }

    static int screensaverMinutes(Context c) { return prefs(c).getInt("screensaver_minutes", 10); }
    static void setScreensaverMinutes(Context c, int value) {
        prefs(c).edit().putInt("screensaver_minutes", Math.max(0, Math.min(60, value))).apply();
    }
    static String clockMode(Context c) { return prefs(c).getString("clock_mode", "system"); }
    static void setClockMode(Context c, String value) { prefs(c).edit().putString("clock_mode", value).apply(); }

    static String mediaVisionPackage(Context c) { return prefs(c).getString("media_vision_package", ""); }
    static void setMediaVisionPackage(Context c, String value) {
        prefs(c).edit().putString("media_vision_package", value == null ? "" : value).apply();
    }
    static String route(Context c, String key) { return prefs(c).getString(key, ""); }
    static void setRoute(Context c, String key, String value) {
        prefs(c).edit().putString(key, value == null ? "" : value.trim()).apply();
    }
}
