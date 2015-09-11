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
