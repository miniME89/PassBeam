package io.github.minime89.keepasstransfer.hooks;

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
    public static final int STATE_CREATED = 1;
    public static final int STATE_DESTROYED = 2;
    private static int state = STATE_DESTROYED;
    private static Collection<NotificationStateListener> notificationStateListeners = new ArrayList<>();

    public interface NotificationStateListener {
        void change(int state);
    }

    public NotificationListener() {
    }

    /**
     * @param listener
     */
    public static void addNotificationStateListener(NotificationStateListener listener) {
        notificationStateListeners.add(listener);
    }

    /**
     * @param listener
     */
    public static void removeNotificationStateListener(NotificationStateListener listener) {
        notificationStateListeners.remove(listener);
    }

    /**
     *
     */
    private static void notifyNotificationStateListeners() {
        for (NotificationStateListener listener : notificationStateListeners) {
            listener.change(state);
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

        state = STATE_CREATED;
        notifyNotificationStateListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        state = STATE_DESTROYED;
        notifyNotificationStateListeners();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "**********  onNotificationPosted");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "********** onNOtificationRemoved");
    }
}
