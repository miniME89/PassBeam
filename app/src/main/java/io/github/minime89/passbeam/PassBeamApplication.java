package io.github.minime89.passbeam;

import android.app.Application;
import android.content.Context;

public class PassBeamApplication extends Application {
    private static final String TAG = PassBeamApplication.class.getSimpleName();
    private static PassBeamApplication instance;

    /**
     * The application context.
     */
    private Context context;

    /**
     * Get the {@link PassBeamApplication} instance.
     *
     * @return Returns the @link PassBeamApplication} instance.
     */
    public static PassBeamApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        context = getApplicationContext();

        PassBeamService.start(context);
    }

    public Context getContext() {
        return context;
    }
}
