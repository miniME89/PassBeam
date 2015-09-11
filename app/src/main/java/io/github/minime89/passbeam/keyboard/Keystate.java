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

public class Keystate {
    public static final int MODIFIER_NONE = 0x00;
    public static final int MODIFIER_LEFT_CTRL = 0x01;
    public static final int MODIFIER_RIGHT_CTRL = 0x10;
    public static final int MODIFIER_LEFT_SHIFT = 0x02;
    public static final int MODIFIER_RIGHT_SHIFT = 0x20;
    public static final int MODIFIER_LEFT_ALT = 0x04;
    public static final int MODIFIER_RIGHT_ALT = 0x40;
    public static final int MODIFIER_LEFT_META = 0x08;
    public static final int MODIFIER_RIGHT_META = 0x80;

    /**
     *
     */
    private final int modifiers;

    /**
     *
     */
    private boolean cycle = false;

    /**
     * Constructor.
     *
     * @param modifiers
     */
    public Keystate(int modifiers) {
        this.modifiers = modifiers;
    }

    public int getModifiers() {
        return modifiers;
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("modifiers", modifiers);

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
