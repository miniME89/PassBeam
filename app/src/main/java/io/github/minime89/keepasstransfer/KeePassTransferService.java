package io.github.minime89.keepasstransfer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.minime89.keepasstransfer.hooks.ClipboardListener;
import io.github.minime89.keepasstransfer.hooks.UsbListener;
import io.github.minime89.keepasstransfer.keyboard.DeviceWriter;
import io.github.minime89.keepasstransfer.keyboard.Keycodes;

public class KeePassTransferService extends Service {
    private static final String TAG = KeePassTransferService.class.getSimpleName();

    /**
     *
     */
    private ClipboardListener clipboardListener;

    /**
     *
     */
    private UsbListener usbListener;

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
     *
     */
    private class LoadLayoutTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String keycodesId = params[0];
            Log.v(TAG, String.format("load keyboard layout '%s'", keycodesId));

            try {
                DeviceWriter.getConverter().load(keycodesId);
            } catch (FileManager.FileManagerException e) {
                Log.e(TAG, "couldn't load keyboard layout: " + e.getMessage()); //TODO handle
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     *
     */
    public KeePassTransferService() {

    }

    /**
     * Start the application service which runs in background to initiate a write request to the
     * keyboard device when the device is connected via USB and something was copied to the
     * clipboard.
     *
     * @param context The {@link Context}.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, KeePassTransferService.class);
        context.startService(intent);
    }

    /**
     *
     */
    private void updateKeyboardLayout() {
        SharedPreferences sharedPreferences = KeePassTransferApplication.getInstance().getSharedPreferences();
        String keycodesId = sharedPreferences.getString(getString(R.string.settings_keyboard_layout_key), Keycodes.DEFAULT_ID);
        LoadLayoutTask loadLayoutTask = new LoadLayoutTask();
        loadLayoutTask.execute(keycodesId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, String.format("start %s", getClass().getSimpleName()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        Context context = getApplicationContext();
        usbListener = UsbListener.start(context);
        clipboardListener = ClipboardListener.start(context, usbListener);

        updateKeyboardLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v(TAG, String.format("stop %s", getClass().getSimpleName()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
