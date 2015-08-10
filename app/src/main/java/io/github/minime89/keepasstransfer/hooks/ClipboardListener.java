package io.github.minime89.keepasstransfer.hooks;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import io.github.minime89.keepasstransfer.keyboard.KeyboardDeviceWriter;

public class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String TAG = ClipboardListener.class.getSimpleName();

    private Context context;
    private ClipboardManager clipboardManager;

    public ClipboardListener(Context context) {
        this.context = context;
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onPrimaryClipChanged() {
        ClipData clipData = clipboardManager.getPrimaryClip();

        Log.i(TAG, "**********  onPrimaryClipChanged");
        Log.i(TAG, clipData.toString());

        KeyboardDeviceWriter.write(context, clipData.toString());
    }
}
