package io.github.minime89.keepasstransfer.hooks;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import io.github.minime89.keepasstransfer.KeePassTransfer;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = NotificationListener.class.getSimpleName();
    private static final int LIFECYCLE_CREATED = 1;
    private static final int LIFECYCLE_DESTROYED = 2;
    private static int lifecycleState = LIFECYCLE_DESTROYED;
    private static Collection<LifecycleListener> lifecycleListeners = new ArrayList<>();

    public interface LifecycleListener {
        void change(int state);
    }

    public NotificationListener() {
    }

    /**
     *
     * @param listener
     */
    public static void addLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }

    /**
     *
     * @param listener
     */
    public static void removeLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.remove(listener);
    }

    /**
     *
     */
    private static void notifyLifecycleListeners() {
        for (LifecycleListener listener : lifecycleListeners) {
            listener.change(lifecycleState);
        }
    }

    /**
     * Check if the application is allowed to receive notification events.
     *
     * @return Returns true if the application is allowed to receive notification events.
     */
    public static boolean isNotificationAccessEnabled() {
        Context context = KeePassTransfer.getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        lifecycleState = LIFECYCLE_CREATED;
        notifyLifecycleListeners();

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardListener());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        lifecycleState = LIFECYCLE_DESTROYED;
        notifyLifecycleListeners();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "**********  onNotificationPosted");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "********** onNOtificationRemoved");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
    }
}
