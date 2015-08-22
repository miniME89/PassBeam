package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.minime89.keepasstransfer.keyboard.DeviceWriter;
import io.github.minime89.keepasstransfer.keyboard.Keycodes;

public class KeePassTransfer extends Application {
    private static final String TAG = KeePassTransfer.class.getSimpleName();
    private static Context context;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(getString(R.string.settings_keyboard_layout_key))) {
                updateKeyboardLayout();
            }
        }
    };

    private void updateKeyboardLayout() {
        String keycodeId = sharedPreferences.getString(getString(R.string.settings_keyboard_layout_key), Keycodes.DEFAULT_ID);

        Log.i(TAG, String.format("load keyboard layout '%s'", keycodeId));

        try {
            DeviceWriter.getConverter().load(keycodeId);
        } catch (FileManager.FileManagerException e) {
            Log.e(TAG, "couldn't load keyboard layout: " + e.getMessage()); //TODO handle
            e.printStackTrace();
        }
    }

    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        try {
            Keycodes.load("de");
        } catch (FileManager.FileManagerException e) {
            e.printStackTrace();
        }

        updateKeyboardLayout();
    }

    public static Context getContext() {
        return context;
    }
}
