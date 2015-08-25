package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.minime89.keepasstransfer.hooks.ClipboardListener;
import io.github.minime89.keepasstransfer.keyboard.Converter;
import io.github.minime89.keepasstransfer.keyboard.Keycodes;

/**
 * The KeePassTransfer class will be initiated at application start. Any global state which is
 * needed across the whole application will be accessible through the single instance.
 */
public class KeePassTransfer extends Application {
    private static final String TAG = KeePassTransfer.class.getSimpleName();
    private static KeePassTransfer instance;

    /**
     * The application context.
     */
    private Context context;

    /**
     * The shared preferences
     */
    private SharedPreferences sharedPreferences;

    /**
     * The keyboard symbol converter.
     */
    private Converter converter;

    /**
     *
     */
    private ClipboardListener clipboardListener;

    /**
     *
     */
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(getString(R.string.settings_keyboard_layout_key))) {
                updateKeyboardLayout();
            }
        }
    };

    /**
     * Get the {@link KeePassTransfer} instance.
     *
     * @return Returns the @link KeePassTransfer} instance.
     */
    public static KeePassTransfer getInstance() {
        return instance;
    }

    /**
     *
     */
    private void updateKeyboardLayout() {
        String keycodesId = sharedPreferences.getString(getString(R.string.settings_keyboard_layout_key), Keycodes.DEFAULT_ID);

        Log.v(TAG, String.format("load keyboard layout '%s'", keycodesId));

        try {
            converter.load(keycodesId);
        } catch (FileManager.FileManagerException e) {
            Log.e(TAG, "couldn't load keyboard layout: " + e.getMessage()); //TODO handle
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        context = getApplicationContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        converter = new Converter();

        clipboardListener = new ClipboardListener();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(clipboardListener);

        updateKeyboardLayout();
    }

    public Context getContext() {
        return context;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public Converter getConverter() {
        return converter;
    }

    public ClipboardListener getClipboardListener() {
        return clipboardListener;
    }
}
