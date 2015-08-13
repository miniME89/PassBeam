package io.github.minime89.keepasstransfer;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDelegate;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import io.github.minime89.keepasstransfer.hooks.ClipboardListener;
import io.github.minime89.keepasstransfer.hooks.NotificationListener;
import io.github.minime89.keepasstransfer.keyboard.KeyboardSymbolConverter;
import io.github.minime89.keepasstransfer.keyboard.KeyboardDeviceWriter;
import io.github.minime89.keepasstransfer.keyboard.Keycode;
import io.github.minime89.keepasstransfer.keyboard.KeycodeMapper;
import io.github.minime89.keepasstransfer.keyboard.Keysym;
import io.github.minime89.keepasstransfer.keyboard.KeysymMapper;
import io.github.minime89.keepasstransfer.keyboard.ScancodeMapper;
import io.github.minime89.keepasstransfer.keyboard.Symbol;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private AppCompatDelegate appCompatDelegate;
    private Preference notificationsStatusPreference;
    private SharedPreferences sharedPreferences;

    private NotificationListener.LifecycleListener lifecycleListener = new NotificationListener.LifecycleListener() {
        @Override
        public void change(int state) {
        updateNotificationsStatus();
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.settings_keyboard_layout_key))) {
            updateKeyboardLayout();
        }
        }
    };

    private void updateKeyboardLayout() {
        String keycodeId = sharedPreferences.getString(getString(R.string.settings_keyboard_layout_key), KeycodeMapper.DEFAULT_ID);

        try {
            KeyboardDeviceWriter.getKeyboardSymbolConverter().load(keycodeId);
        } catch (ScancodeMapper.ScancodeMapperException e) {
            e.printStackTrace();
        } catch (KeysymMapper.KeysymMapperException e) {
            e.printStackTrace();
        } catch (KeycodeMapper.KeycodeMapperException e) {
            e.printStackTrace();
        }
    }

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

        Collection<String> characterMappings = FileManager.getInstance().listKeycodeMappings();
        CharSequence[] entries = characterMappings.toArray(new CharSequence[characterMappings.size()]);

        keyboardLayoutPreference.setEntries(entries);
        keyboardLayoutPreference.setEntryValues(entries);
    }

    private void setupKeyboardLayoutTest() {
        Preference keyboardLayoutTestPreference = findPreference(getString(R.string.settings_keyboard_layout_test_key));
        keyboardLayoutTestPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                KeyboardSymbolConverter keyboardDeviceWriter = KeyboardDeviceWriter.getKeyboardSymbolConverter();
                if (keyboardDeviceWriter != null) {
                    KeycodeMapper keycodeMapper = keyboardDeviceWriter.getKeycodeMapper();
                    if (keycodeMapper != null) {
                        Collection<Keycode> keycodes = keycodeMapper.all();

                        StringBuilder strBuilder = new StringBuilder();
                        for (Keycode keycode : keycodes) {
                            Collection<Symbol> symbols = keycode.getSymbols();
                            for (Symbol symbol : symbols) {
                                Keysym keysym = symbol.getKeysym();
                                if (keysym.isPrintable()) {
                                    strBuilder.append(keysym.getUnicodeValue());
                                }
                            }
                        }

                        String str = strBuilder.toString();
                        Log.i(TAG, String.format("write %d printable unicode characters for the selected keyboard layout: %s", str.length(), str));
                        KeyboardDeviceWriter.write(str);
                    }
                }

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getAppCompatDelegate().installViewFactory();
        getAppCompatDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_activity);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setupNotificationsStatus();
        setupKeyboardLayout();
        setupKeyboardLayoutTest();

        updateKeyboardLayout();

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardListener());
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
