package io.github.minime89.passbeam.hooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.minime89.passbeam.PassBeamService;
import io.github.minime89.passbeam.R;

public class UsbListener extends BroadcastReceiver {
    private static final String TAG = UsbListener.class.getSimpleName();
    private boolean connected;

    /**
     * Constructor.
     */
    private UsbListener() {
        connected = false;
    }

    /**
     * Start a USB listener which listens on broadcasts that indicate whether the device was
     * connected via USB.
     *
     * @param context The {@link Context}.
     * @return Returns the created {@link UsbListener} instance.
     */
    public static UsbListener start(Context context) {
        UsbListener usbListener = new UsbListener();
        context.registerReceiver(usbListener, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        return usbListener;
    }

    /**
     * Update the viability of the notification.
     *
     * @param context The {@link Context}.
     */
    public void update(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notification = sharedPreferences.getBoolean(context.getString(R.string.settings_notification_key), Boolean.valueOf(context.getString(R.string.settings_notification_defaultValue)));

        NotificationListener notificationListener = PassBeamService.getInstance().getNotificationListener();
        if (connected && notification) {
            notificationListener.show(context);
        } else {
            notificationListener.hide(context);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connectedOld = connected;
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        connected = plugged == BatteryManager.BATTERY_PLUGGED_USB;

        if (connected != connectedOld) {
            Log.v(TAG, String.format("USB connection changed: %b", connected));

            update(context);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
