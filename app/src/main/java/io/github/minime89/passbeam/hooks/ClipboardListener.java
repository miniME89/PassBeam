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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import io.github.minime89.passbeam.PassBeamService;
import io.github.minime89.passbeam.R;
import io.github.minime89.passbeam.keyboard.DeviceWriter;

public class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String TAG = ClipboardListener.class.getSimpleName();

    private final Context context;
    private final ClipboardManager clipboardManager;

    /**
     * Constructor.
     *
     * @param context          The {@link Context}.
     * @param clipboardManager The {@link ClipboardManager}.
     */
    private ClipboardListener(Context context, ClipboardManager clipboardManager) {
        this.context = context;
        this.clipboardManager = clipboardManager;
    }

    /**
     * Start a clipboard listener which listens on clipboard change events to write the received
     * data to the {@link DeviceWriter} when the device is connected via USB.
     *
     * @param context The {@link Context}.
     * @return Returns the created {@link ClipboardListener} instance.
     */
    public static ClipboardListener start(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipboardListener clipboardListener = new ClipboardListener(context, clipboardManager);
        clipboardManager.addPrimaryClipChangedListener(clipboardListener);

        return clipboardListener;
    }

    /**
     * Write the clipboard to the keyboard device using the {@link DeviceWriter}.
     */
    public void write() {
        ClipData clipData = clipboardManager.getPrimaryClip();
        ClipDescription clipDescription = clipData.getDescription();

        if (clipData.getItemCount() > 0 && clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData.Item item = clipData.getItemAt(0);
            String data = String.valueOf(item.getText());

            DeviceWriter.write(data);
        }
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.v(TAG, String.format("clipboard changed: %s", clipboardManager.getPrimaryClip()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notification = sharedPreferences.getBoolean(context.getString(R.string.settings_notification_key), Boolean.valueOf(context.getString(R.string.settings_notification_defaultValue)));

        UsbListener usbListener = PassBeamService.getInstance().getUsbListener();
        if (usbListener.isConnected() && !notification) {
            write();
        }
    }
}
