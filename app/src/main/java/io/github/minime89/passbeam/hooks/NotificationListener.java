package io.github.minime89.passbeam.hooks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import io.github.minime89.passbeam.PassBeamApplication;
import io.github.minime89.passbeam.PassBeamService;
import io.github.minime89.passbeam.R;

public class NotificationListener extends BroadcastReceiver {
    private static final String TAG = NotificationListener.class.getSimpleName();
    public static String ACTION_NOTIFICATION = PassBeamApplication.class.getPackage() + ".ACTION_NOTIFICATION";
    public static int NOTIFICATION_ID = 42;

    private Context context;

    public NotificationListener(Context context) {
        this.context = context;
    }

    public static NotificationListener start(Context context) {
        NotificationListener notificationListener = new NotificationListener(context);
        IntentFilter intentFilter = new IntentFilter(ACTION_NOTIFICATION);
        context.registerReceiver(notificationListener, intentFilter);

        return notificationListener;
    }

    /**
     * Show the notification.
     *
     * @param context The {@link Context}.
     */
    public void show(Context context) {
        Intent intent = new Intent(ACTION_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Hide the notification.
     *
     * @param context The {@link Context}.
     */
    public void hide(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "notification clicked");

        ClipboardListener clipboardListener = PassBeamService.getInstance().getClipboardListener();
        clipboardListener.write();
    }
}
