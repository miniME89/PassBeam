package io.github.minime89.keepasstransfer.hooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        connected = plugged == BatteryManager.BATTERY_PLUGGED_USB;

        Log.v(TAG, String.format("USB connection changed: %b", connected));
    }

    public boolean isConnected() {
        return connected;
    }
}
