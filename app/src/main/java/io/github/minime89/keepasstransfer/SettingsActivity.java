package io.github.minime89.keepasstransfer;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDelegate;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.Collection;

import io.github.minime89.keepasstransfer.hooks.NotificationListener;
import io.github.minime89.keepasstransfer.keyboard.KeyboardDeviceWriter;
import io.github.minime89.keepasstransfer.keyboard.MappingManager;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private AppCompatDelegate appCompatDelegate;
    private Preference notificationsStatusPreference;

    private NotificationListener.LifecycleListener lifecycleListener = new NotificationListener.LifecycleListener() {
        @Override
        public void change(int state) {
            updateNotificationsStatus();
        }
    };

    private void setupNotificationsStatus() {
        notificationsStatusPreference = findPreference(getString(R.string.settings_notifications_status_key));

        NotificationListener.addLifecycleListener(lifecycleListener);

        updateNotificationsStatus();
    }

    private void updateNotificationsStatus() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (notificationsStatusPreference != null && preferenceScreen != null) {
            //notifications access is allowed
            if (NotificationListener.isNotificationAccessEnabled()) {
                preferenceScreen.removePreference(notificationsStatusPreference);
            }
            //notifications access is disallowed
            else {
                Spannable text = new SpannableString(notificationsStatusPreference.getTitle());
                text.setSpan(new ForegroundColorSpan(Color.RED), 0, text.length(), 0);
                notificationsStatusPreference.setTitle(text);

                preferenceScreen.addPreference(notificationsStatusPreference);
            }
        }
    }

    private void setupKeyboardLayout() {
        ListPreference keyboardLayoutPreference = (ListPreference) findPreference(getString(R.string.settings_keyboard_layout_key));

        Collection<String> characterMappings = MappingManager.getInstance().listCharacterMappings();
        CharSequence[] entries = characterMappings.toArray(new CharSequence[characterMappings.size()]);

        keyboardLayoutPreference.setEntries(entries);
        keyboardLayoutPreference.setEntryValues(entries);
    }

    private void setupKeyboardLayoutTest() {
        Preference keyboardLayoutTestPreference = findPreference(getString(R.string.settings_keyboard_layout_test_key));
        keyboardLayoutTestPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View currentView = SettingsActivity.this.getCurrentFocus();

                if (currentView != null) {
                    InputMethodManager im = (InputMethodManager) SettingsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(SettingsActivity.this.getCurrentFocus(), InputMethodManager.SHOW_FORCED);

                    currentView.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View view, int i, KeyEvent keyEvent) {
                            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                                String str = String.valueOf((char) keyEvent.getUnicodeChar());
                                Log.i(TAG, String.format("unicode: %d / char: %c / string: %s / event: %s", keyEvent.getUnicodeChar(), (char) keyEvent.getUnicodeChar(), str, keyEvent.toString()));
                                KeyboardDeviceWriter.write(str);
                            }

                            return true;
                        }
                    });
                }

                return true;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getAppCompatDelegate().installViewFactory();
        getAppCompatDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

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
