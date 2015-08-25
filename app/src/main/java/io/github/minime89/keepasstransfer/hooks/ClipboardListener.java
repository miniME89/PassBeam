package io.github.minime89.keepasstransfer.hooks;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;

import io.github.minime89.keepasstransfer.KeePassTransfer;
import io.github.minime89.keepasstransfer.keyboard.DeviceWriter;

public class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String TAG = ClipboardListener.class.getSimpleName();

    private ClipboardManager clipboardManager;

    public ClipboardListener() {
        Context context = KeePassTransfer.getInstance().getContext();
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onPrimaryClipChanged() {
        if (NotificationListener.isNotificationPosted()) {
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
