package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.Context;

public class KeePassTransfer extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
