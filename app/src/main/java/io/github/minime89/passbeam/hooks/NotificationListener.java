/*
 * Copyright (C) 2015 Marcel Lehwald
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    private static final String ACTION_NOTIFICATION = PassBeamApplication.class.getPackage() + ".ACTION_NOTIFICATION";
    private static final int NOTIFICATION_ID = 42;

    private final Context context;

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
