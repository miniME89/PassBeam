package io.github.minime89.keepasstransfer.hooks;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import io.github.minime89.keepasstransfer.KeePassTransfer;
import io.github.minime89.keepasstransfer.R;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = NotificationListener.class.getSimpleName();
    public static final int STATE_CREATED = 1;
    public static final int STATE_DESTROYED = 2;
    private static int state = STATE_DESTROYED;
    private static Collection<NotificationStateListener> notificationStateListeners;
    private static Collection<String> notifications;
    private static Collection<String> applications;

    /**
     *
     */
    public interface NotificationStateListener {
        void change(int state);
    }

    /**
     * Constructor.
     */
    public NotificationListener() {

    }

    /**
     * Add a notifications state listener which will be called when the state of the notifications
     * listener has changed. The state change is either the creation or the destruction of the
     * notifications listener.
     * listener
     *
     * @param listener The listener.
     * @see NotificationListener#removeNotificationStateListener
     */
    public static void addNotificationStateListener(NotificationStateListener listener) {
        notificationStateListeners.add(listener);
    }

    /**
     * Remove a notifications state listener.
     *
     * @param listener The listener.
     * @see NotificationListener#addNotificationStateListener
     */
    public static void removeNotificationStateListener(NotificationStateListener listener) {
        notificationStateListeners.remove(listener);
    }

    /**
     * Notify all registered listeners about the current state of the notifications listener.
     *
     * @see NotificationListener#addNotificationStateListener
     * @see NotificationListener#removeNotificationStateListener
     */
    private static void notifyNotificationStateListeners() {
        for (NotificationStateListener listener : notificationStateListeners) {
            listener.change(state);
        }
    }

    /**
     * Check if the application is allowed to receive notifications events.
     *
     * @return Returns true if the application is allowed to receive notifications events.
     */
    public static boolean isNotificationAccessEnabled() {
        Context context = KeePassTransfer.getInstance().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationStateListeners = new ArrayList<>();
        notifications = new ArrayList<>();
        applications = Arrays.asList(getResources().getStringArray(R.array.config_applications));

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
        String key = sbn.getPackageName() + "|" + sbn.getId() + "|" + sbn.getTag();
        if (applications != null && applications.contains(sbn.getPackageName())) {
            Log.v(TAG, String.format("posted notification by registered application '%s'", sbn.getPackageName()));
            notifications.add(key);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String key = sbn.getPackageName() + "|" + sbn.getId() + "|" + sbn.getTag();
        if (notifications != null && notifications.contains(key)) {
            Log.v(TAG, String.format("removed notification by registered application '%s'", sbn.getPackageName()));
            notifications.remove(key);
        }
    }

    /**
     * Check whether a notification of a certain application is visible. The list of applications
     * for which the viability will be registered, is defined in config.xml.
     *
     * @return Returns true when a notification of a certain application is visible.
     */
    public static boolean isNotificationPosted() {
        return notifications != null && notifications.size() > 0;
    }
}
