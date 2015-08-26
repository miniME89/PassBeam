package io.github.minime89.keepasstransfer.hooks;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import io.github.minime89.keepasstransfer.keyboard.DeviceWriter;

public class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String TAG = ClipboardListener.class.getSimpleName();

    private ClipboardManager clipboardManager;
    private UsbListener usbListener;

    /**
     * Constructor.
     *
     * @param clipboardManager The {@link ClipboardManager}.
     * @param usbListener      The {@link UsbListener}.
     */
    private ClipboardListener(ClipboardManager clipboardManager, UsbListener usbListener) {
        this.clipboardManager = clipboardManager;
        this.usbListener = usbListener;
    }

    /**
     * Start a clipboard listener which listens on clipboard change events to write the received
     * data to the {@link DeviceWriter} when the device is connected via USB.
     *
     * @param context     The {@link Context}.
     * @param usbListener The {@link UsbListener}.
     * @return Returns the created {@link ClipboardListener} instance.
     */
    public static ClipboardListener start(Context context, UsbListener usbListener) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipboardListener clipboardListener = new ClipboardListener(clipboardManager, usbListener);
        clipboardManager.addPrimaryClipChangedListener(clipboardListener);

        return clipboardListener;
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.v(TAG, String.format("clipboard changed: %s", clipboardManager.getPrimaryClip()));

        if (usbListener.isConnected()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            ClipDescription clipDescription = clipData.getDescription();

            if (clipData.getItemCount() > 0 && clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                ClipData.Item item = clipData.getItemAt(0);
                String data = String.valueOf(item.getText());

                DeviceWriter.write(data);
            }
        }
    }
}
