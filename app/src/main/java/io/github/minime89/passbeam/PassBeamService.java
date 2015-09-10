package io.github.minime89.passbeam;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.minime89.passbeam.hooks.ClipboardListener;
import io.github.minime89.passbeam.hooks.NotificationListener;
import io.github.minime89.passbeam.hooks.UsbListener;
import io.github.minime89.passbeam.keyboard.DeviceWriter;
import io.github.minime89.passbeam.keyboard.Keycodes;

public class PassBeamService extends Service {
    private static final String TAG = PassBeamService.class.getSimpleName();

    /**
     *
     */
    private UsbListener usbListener;

    /**
     *
     */
    private ClipboardListener clipboardListener;

    /**
     *
     */
    private NotificationListener notificationListener;

    /**
     *
     */
    private static PassBeamService instance;

    /**
     *
     */
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(getString(R.string.settings_keyboard_layout_key))) {
                updateKeyboardLayout();
            } else if (s.equals(getString(R.string.settings_notification_key))) {
                updateNotification();
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
            }

            return null;
        }
    }

    /**
     *
     */
    public PassBeamService() {

    }

    /**
     * Get the {@link PassBeamService} instance.
     *
     * @return Returns the @link PassBeamService} instance.
     */
    public static PassBeamService getInstance() {
        return instance;
    }

    /**
     * Start the application service which runs in background to initiate a write request to the
     * keyboard device when the device is connected via USB and something was copied to the
     * clipboard.
     *
     * @param context The {@link Context}.
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, PassBeamService.class);
        context.startService(intent);
    }

    /**
     *
     */
    private void updateKeyboardLayout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String keycodesId = sharedPreferences.getString(getString(R.string.settings_keyboard_layout_key), Keycodes.DEFAULT_ID);
        LoadLayoutTask loadLayoutTask = new LoadLayoutTask();
        loadLayoutTask.execute(keycodesId);
    }

    /**
     *
     */
    private void updateNotification() {
        if (usbListener != null) {
            usbListener.update(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, String.format("start %s", getClass().getSimpleName()));

        instance = this;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        Context context = getApplicationContext();
        usbListener = UsbListener.start(context);
        notificationListener = NotificationListener.start(context);
        clipboardListener = ClipboardListener.start(context);

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

    public UsbListener getUsbListener() {
        return usbListener;
    }

    public NotificationListener getNotificationListener() {
        return notificationListener;
    }

    public ClipboardListener getClipboardListener() {
        return clipboardListener;
    }
}
