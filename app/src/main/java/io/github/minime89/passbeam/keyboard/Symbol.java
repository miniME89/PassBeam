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

import org.json.JSONException;
import org.json.JSONObject;

public class Symbol {
    private final Keycode keycode;
    private final Keysym keysym;
    private final Keystate keystate;

    /**
     *
     */
    private boolean cycle = false;

    public Symbol(Keycode keycode, Keysym keysym, Keystate keystate) {
        this.keycode = keycode;
        this.keysym = keysym;
        this.keystate = keystate;
    }

    public Keycode getKeycode() {
        return keycode;
    }

    public Keysym getKeysym() {
        return keysym;
    }

    public Keystate getKeystate() {
        return keystate;
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        if (!cycle) {
            cycle = true;
            try {
                obj.put("keycode", (keycode != null) ? keycode.dump() : null);
                obj.put("keysym", (keysym != null) ? keysym.dump() : null);
                obj.put("keystate", (keystate != null) ? keystate.dump() : null);
            } finally {
                cycle = false;
            }
        }

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
