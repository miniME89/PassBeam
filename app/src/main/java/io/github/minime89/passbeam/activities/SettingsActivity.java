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
package io.github.minime89.passbeam.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import io.github.minime89.passbeam.R;
import io.github.minime89.passbeam.keyboard.Converter;
import io.github.minime89.passbeam.keyboard.DeviceWriter;
import io.github.minime89.passbeam.keyboard.Keycodes;
import io.github.minime89.passbeam.keyboard.Keysym;
import io.github.minime89.passbeam.keyboard.Symbol;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private AppCompatDelegate appCompatDelegate;

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
                Converter converter = DeviceWriter.getConverter();
                if (converter != null) {
                    Keycodes keycodes = converter.getKeycodes();
                    if (keycodes != null) {
                        StringBuilder strBuilder = new StringBuilder();
                        Collection<Symbol> symbols = keycodes.findPrintable();
                        for (Symbol symbol : symbols) {
                            Keysym keysym = symbol.getKeysym();
                            strBuilder.append(keysym.getUnicode().getCharacter());
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

    private AppCompatDelegate getAppCompatDelegate() {
        if (appCompatDelegate == null) {
            appCompatDelegate = AppCompatDelegate.create(this, null);
        }

        return appCompatDelegate;
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //first time startup?
        if (!sharedPreferences.getBoolean("setup", false)) {
            Intent intent = new Intent(this, SetupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_activity);

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

    @Override
    public void invalidateOptionsMenu() {
        getAppCompatDelegate().invalidateOptionsMenu();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return false;
    }
}
