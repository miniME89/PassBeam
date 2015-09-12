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

import java.util.Collection;
import java.util.Collections;

import io.github.minime89.passbeam.FileManager;

@Root(strict = false)
public class Scancodes {
    private static final String TAG = Scancodes.class.getSimpleName();
    public static final String DEFAULT_ID = "default";

    private final Collection<Scancode> scancodes;

    /**
     * Load the scancodes with the specified scancodes ID.
     *
     * @param scancodesId The scancodes ID.
     * @return Returns loaded scancodes.
     * @throws FileManager.FileManagerException When scancodes couldn't be loaded.
     */
    public static Scancodes load(String scancodesId) throws FileManager.FileManagerException {
        FileManager fileManager = new FileManager();

        return fileManager.loadScancodes(scancodesId);
    }

    private Scancodes(@ElementList(name = "scancodes", inline = true, required = true) Collection<Scancode> scancodes) {
        this.scancodes = Collections.unmodifiableCollection(scancodes);
    }

    /**
     * Build all scancodes. This will call {@link Scancode#build(Converter)} for every scancode in
     * {@link #scancodes}. Any failed scancode build attempts will be ignored.
     *
     * @param converter The converter used for possible keycode, keysym and scancode lookups.
     */
    public void build(Converter converter) {
        for (Scancode scancode : scancodes) {
            try {
                scancode.build(converter);
            } catch (Scancode.ScancodeBuildException e) {
                Log.v(TAG, e.getMessage());
            }
        }
    }

    public Scancode find(Scancode.Ref scancodeRef) {
        for (Scancode scancode : scancodes) {
            if (scancode.equals(scancodeRef)) {
                return scancode;
            }
        }

        return null;
    }

    public Scancode find(Keycode.Ref keycodeRef) {
        for (Scancode scancode : scancodes) {
            if (scancode.getKeycodeRef() != null && scancode.getKeycodeRef().equals(keycodeRef)) {
                return scancode;
            }
        }

        return null;
    }

    @ElementList(name = "scancodes", inline = true, required = true)
    public Collection<Scancode> getScancodes() {
        return scancodes;
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray scancodesArr = new JSONArray();
        for (Scancode scancode : scancodes) {
            scancodesArr.put(scancode.dump());
        }
        obj.put("scancodes", scancodesArr);

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
