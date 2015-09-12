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
package io.github.minime89.passbeam.keyboard;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import io.github.minime89.passbeam.FileManager;

@Root(strict = false)
public class Keycodes {
    private static final String TAG = Keycodes.class.getSimpleName();
    public static final String DEFAULT_ID = "us";

    /**
     *
     */
    private final Collection<Keycode> keycodes;

    /**
     * Load the keycodes with the specified keycodes ID.
     *
     * @param keycodesId The keycodes ID.
     * @return Returns loaded keycodes.
     * @throws FileManager.FileManagerException When keycodes couldn't be loaded.
     */
    public static Keycodes load(String keycodesId) throws FileManager.FileManagerException {
        FileManager fileManager = new FileManager();

        return fileManager.loadKeycodes(keycodesId);
    }

    public Keycodes(@ElementList(name = "keycodes", inline = true, required = true) Collection<Keycode> keycodes) {
        this.keycodes = Collections.unmodifiableCollection(keycodes);
    }

    /**
     * Build all keycodes. This will call {@link Keycode#build(Converter)} for every keycode in
     * {@link #keycodes}. Any failed keycode build attempts will be ignored.
     *
     * @param converter The converter used for possible keycode, keysym and scancode lookups.
     */
    public void build(Converter converter) {
        for (Keycode keycode : keycodes) {
            try {
                keycode.build(converter);
            } catch (Keycode.KeycodeBuildException e) {
                Log.v(TAG, e.getMessage());
            }
        }
    }

    public Keycode find(Keycode.Ref keycodeRef) {
        for (Keycode keycode : keycodes) {
            if (keycode.equals(keycodeRef)) {
                return keycode;
            }
        }

        return null;
    }

    public Collection<Symbol> find(char character) {
        Collection<Symbol> found = new ArrayList<>();
        for (Keycode keycode : keycodes) {
            Collection<Symbol> symbols = keycode.find(character);
            found.addAll(symbols);
        }

        return found;
    }

    public Collection<Symbol> findPrintable() {
        Collection<Symbol> symbols = new ArrayList<>();
        for (Keycode keycode : keycodes) {
            Collection<Symbol> keycodeSymbols = keycode.getSymbols();
            if (keycodeSymbols != null) {
                for (Symbol keycodeSymbol : keycodeSymbols) {
                    Keysym keysym = keycodeSymbol.getKeysym();
                    if (keysym.isPrintable()) {
                        symbols.add(keycodeSymbol);
                    }
                }
            }
        }

        return symbols;
    }

    @ElementList(name = "keycodes", inline = true, required = true)
    public Collection<Keycode> getKeycodes() {
        return keycodes;
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray keycodesArr = new JSONArray();
        for (Keycode keycode : keycodes) {
            keycodesArr.put(keycode.dump());
        }
        obj.put("keycodes", keycodesArr);

        return obj;
    }

    @Override
    public String toString() {
        try {
            return dump().toString();
        } catch (JSONException e) {
            return super.toString();
        }
    }
}
