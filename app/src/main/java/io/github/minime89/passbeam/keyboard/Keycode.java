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
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Root(strict = false)
public class Keycode {
    /**
     *
     */
    private final Integer value;

    /**
     *
     */
    private final Collection<Keysym.Ref> keysymRefs;

    /**
     *
     */
    private Scancode scancode;

    /**
     *
     */
    private Collection<Symbol> symbols;

    /**
     *
     */
    private boolean valid = false;

    /**
     *
     */
    private boolean cycle = false;

    /**
     *
     */
    public static class KeycodeBuildException extends Exception {
        KeycodeBuildException() {
            super();
        }

        KeycodeBuildException(String message) {
            super(message);
        }

        KeycodeBuildException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Reference to a keycode. The reference contains only the {@link Keycode#value} of a keycode
     * which can be used to resolve the actual reference to the {@link Keycode} instance.
     */
    @Root(name = "keycode")
    public static class Ref {
        private final Integer value;

        public Ref(@Attribute(name = "value", required = true) Integer value) {
            this.value = value;
        }

        public Ref(Keycode keycode) {
            this.value = keycode.getValue();
        }

        @Attribute(name = "value", required = true)
        public Integer getValue() {
            return value;
        }

        public boolean equals(Keycode.Ref keycodeRef) {
            return keycodeRef.getValue().equals(value);
        }

        public JSONObject dump() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("value", value);

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

    public Keycode(@Attribute(name = "value", required = true) Integer value,
                   @ElementList(inline = true, required = false) Collection<Keysym.Ref> keysymRefs) {
        this.value = value;
        this.keysymRefs = Collections.unmodifiableCollection(keysymRefs);
    }

    /**
     * Build the keycode. This will lookup the scancode instance associated with the provided
     * keycode {@link #value} and lookup all keysyms associated with the provided {@link #keysymRefs}.
     *
     * @param converter The converter used for possible keycode, keysym and scancode lookups.
     * @throws KeycodeBuildException When the keycode couldn't be build.
     */
    public void build(Converter converter) throws KeycodeBuildException {
        valid = false;
        scancode = null;
        symbols = null;

        Scancodes scancodes = converter.getScancodes();
        if (scancodes == null) {
            throw new KeycodeBuildException("couldn't get scancodes from convert instance");
        }

        //resolve scancode
        Keycode.Ref ref = new Keycode.Ref(this);
        scancode = scancodes.find(ref);
        if (scancode == null) {
            throw new KeycodeBuildException(String.format("couldn't resolve keycode reference [%s] to a scancode", ref));
        }

        Keysyms keysyms = converter.getKeysyms();
        if (keysyms == null) {
            throw new KeycodeBuildException("couldn't get keysyms from convert instance");
        }

        //resolve symbols
        symbols = new ArrayList<>();
        int col = 0;
        for (Keysym.Ref keysymRef : keysymRefs) {
            col++;
            Keysym keysym = keysyms.find(keysymRef);
            if (keysym == null) {
                Log.v("keysyms", String.format("couldn't resolve keysym reference [%s] to a keysym", keysymRef)); //TODO remove
            } else {
                int modifiers = 0x00;
                //TODO determine modifier keys from keyboard layout. Hardcoded keys should work for most keyboard layouts for now.
                if (col == 2) {
                    modifiers = Keystate.MODIFIER_LEFT_SHIFT;
                } else if (col == 3) {
                    modifiers = Keystate.MODIFIER_RIGHT_ALT;
                } else if (col == 4) {
                    modifiers = Keystate.MODIFIER_RIGHT_ALT | Keystate.MODIFIER_LEFT_SHIFT;
                }

                Keystate keystate = new Keystate(modifiers);
                symbols.add(new Symbol(this, keysym, keystate));
            }
        }

        valid = true;
    }

    public Collection<Symbol> find(char character) {
        Collection<Symbol> found = new ArrayList<>();

        if (symbols != null) {
            for (Symbol symbol : symbols) {
                if (symbol.getKeysym().equals(character)) {
                    found.add(symbol);
                }
            }
        }

        return found;
    }

    @Attribute(name = "value", required = true)
    public Integer getValue() {
        return value;
    }

    @ElementList(inline = true, required = false)
    public Collection<Keysym.Ref> getKeysymRefs() {
        return keysymRefs;
    }

    public Scancode getScancode() {
        return scancode;
    }

    public Collection<Symbol> getSymbols() {
        return symbols;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(Keycode keycode) {
        return keycode != null && keycode.getValue().equals(value);
    }

    public boolean equals(Keycode.Ref keycodeRef) {
        return keycodeRef != null && keycodeRef.getValue().equals(value);
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        if (!cycle) {
            cycle = true;
            try {
                obj.put("value", value);
                obj.put("scancode", (scancode != null) ? scancode.dump() : null);

                JSONArray scancodesArr = new JSONArray();
                for (Keysym.Ref keysymRef : keysymRefs) {
                    scancodesArr.put(keysymRef.dump());
                }
                obj.put("keysymRefs", scancodesArr);

                JSONArray symbolsArr = new JSONArray();
                if (symbols != null) {
                    for (Symbol symbol : symbols) {
                        symbolsArr.put(symbol.dump());
                    }
                }
                obj.put("symbols", symbolsArr);
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
