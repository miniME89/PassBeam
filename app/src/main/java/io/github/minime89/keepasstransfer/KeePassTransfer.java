package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.Context;

import io.github.minime89.keepasstransfer.keyboard.KeyboardSymbolConverter;
import io.github.minime89.keepasstransfer.keyboard.KeycodeMapper;
import io.github.minime89.keepasstransfer.keyboard.KeysymMapper;
import io.github.minime89.keepasstransfer.keyboard.ScancodeMapper;

public class KeePassTransfer extends Application {
    private static final String TAG = KeePassTransfer.class.getSimpleName();

    private static Context context;

    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        FileManager.getInstance().install();
    }

    public static Context getContext() {
        return context;
    }
}
