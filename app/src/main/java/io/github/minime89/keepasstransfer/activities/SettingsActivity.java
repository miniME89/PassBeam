package io.github.minime89.keepasstransfer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import io.github.minime89.keepasstransfer.KeePassTransfer;
import io.github.minime89.keepasstransfer.R;
import io.github.minime89.keepasstransfer.hooks.NotificationListener;
import io.github.minime89.keepasstransfer.keyboard.Converter;
import io.github.minime89.keepasstransfer.keyboard.DeviceWriter;
import io.github.minime89.keepasstransfer.keyboard.Keycode;
import io.github.minime89.keepasstransfer.keyboard.Keycodes;
import io.github.minime89.keepasstransfer.keyboard.Keysym;
import io.github.minime89.keepasstransfer.keyboard.Symbol;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private AppCompatDelegate appCompatDelegate;
    private Preference notifications;

    private NotificationListener.NotificationStateListener notificationStateListener = new NotificationListener.NotificationStateListener() {
        @Override
        public void change(int state) {
            updateNotificationsStatus();
        }
    };

    private void updateNotificationsStatus() {
        if (notifications != null) {
            //notifications access is allowed
            if (NotificationListener.isNotificationAccessEnabled()) {
                notifications.setWidgetLayoutResource(0);
            }
            //notifications access is disallowed
            else {
                notifications.setWidgetLayoutResource(R.layout.icon_fragment);
            }

            //FIXME update UI
            PreferenceScreen screen = getPreferenceScreen();
            PreferenceCategory preferenceCategory = (PreferenceCategory) screen.findPreference(getString(R.string.settings_group_notifications_key));
            preferenceCategory.removePreference(notifications);
            preferenceCategory.addPreference(notifications);
        }
    }

    private void setupNotificationsStatus() {
        notifications = findPreference(getString(R.string.settings_notification_access_key));
        NotificationListener.addNotificationStateListener(notificationStateListener);
        updateNotificationsStatus();
    }

    private void setupKeyboardLayout() {
        Preference keyboardLayoutPreference = findPreference(getString(R.string.settings_keyboard_layout_key));
        keyboardLayoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, KeyboardLayoutActivity.class);
                startActivity(intent);

                return true;
            }
        });
    }

    private void setupKeyboardLayoutTest() {
        Preference keyboardLayoutTestPreference = findPreference(getString(R.string.settings_keyboard_layout_test_key));
        keyboardLayoutTestPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Converter keyboardDeviceWriter = KeePassTransfer.getInstance().getConverter();
                if (keyboardDeviceWriter != null) {
                    Keycodes keycodes = keyboardDeviceWriter.getKeycodes();
                    if (keycodes != null) {
                        Collection<Keycode> keycodesAll = keycodes.all();

                        StringBuilder strBuilder = new StringBuilder();
                        for (Keycode keycode : keycodesAll) {
                            Collection<Symbol> symbols = keycode.getSymbols();
                            if (symbols != null) {
                                for (Symbol symbol : symbols) {
                                    Keysym keysym = symbol.getKeysym();
                                    if (keysym.isPrintable()) {
                                        strBuilder.append(keysym.getUnicode().getCharacter());
                                    }
                                }
                            }
                        }

                        String str = strBuilder.toString();
                        Log.v(TAG, String.format("write %d printable unicode characters for the selected keyboard layout: %s", str.length(), str));
                        DeviceWriter.write(str);
                    }
                }

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getAppCompatDelegate().installViewFactory();
        getAppCompatDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = KeePassTransfer.getInstance().getSharedPreferences();

        //first time startup?
        if (!sharedPreferences.getBoolean("setup", false)) {
            Intent intent = new Intent(this, SetupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_activity);

        setupNotificationsStatus();
        setupKeyboardLayout();
        setupKeyboardLayoutTest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getAppCompatDelegate().onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getAppCompatDelegate().onPostCreate(savedInstanceState);
    }

    @Override
    public MenuInflater getMenuInflater() {
        return getAppCompatDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getAppCompatDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getAppCompatDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getAppCompatDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getAppCompatDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getAppCompatDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getAppCompatDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getAppCompatDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getAppCompatDelegate().onStop();
    }

    public void invalidateOptionsMenu() {
        getAppCompatDelegate().invalidateOptionsMenu();
    }

    private AppCompatDelegate getAppCompatDelegate() {
        if (appCompatDelegate == null) {
            appCompatDelegate = AppCompatDelegate.create(this, null);
        }

        return appCompatDelegate;
    }
}
