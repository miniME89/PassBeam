package io.github.minime89.keepasstransfer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class KeePassTransferApplication extends Application {
    private static final String TAG = KeePassTransferApplication.class.getSimpleName();
    private static KeePassTransferApplication instance;

    /**
     * The application context.
     */
    private Context context;

    /**
     * The shared preferences
     */
    private SharedPreferences sharedPreferences;

    /**
     * Get the {@link KeePassTransferApplication} instance.
     *
     * @return Returns the @link KeePassTransferApplication} instance.
     */
    public static KeePassTransferApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        context = getApplicationContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        KeePassTransferService.start(context);
    }

    public Context getContext() {
        return context;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
