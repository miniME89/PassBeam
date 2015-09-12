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
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Keysym {
    /**
     *
     */
    private final Integer value;

    /**
     *
     */
    private final String name;

    /**
     *
     */
    private final Unicode unicode;

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
    public static class KeysymBuildException extends Exception {
        KeysymBuildException() {
            super();
        }

        KeysymBuildException(String message) {
            super(message);
        }

        KeysymBuildException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Reference to a keysym. The reference contains only the {@link Keysym#value} of a keysym
     * which can be used to resolve the actual reference to the {@link Keysym} instance.
     */
    @Root(name = "keysym")
    public static class Ref {
        private final Integer value;

        public Ref(@Attribute(name = "value", required = true) Integer value) {
            this.value = value;
        }

        public Ref(Keysym keysym) {
            this.value = keysym.getValue();
        }

        @Attribute(name = "value", required = true)
        public Integer getValue() {
            return value;
        }

        public boolean equals(Keysym.Ref keysymRef) {
            return keysymRef.getValue().equals(value);
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

    public Keysym(@Attribute(name = "value", required = true) Integer value,
                  @Attribute(name = "name", required = false) String name,
                  @Element(name = "unicode", required = false) Unicode unicode) {
        this.value = value;
        this.name = name;
        this.unicode = unicode;
    }

    /**
     * Build the keysym.
     *
     * @param converter The converter used for possible keycode, keysym and scancode lookups.
     * @throws KeysymBuildException When the keysym couldn't be build.
     */
    public void build(Converter converter) throws KeysymBuildException {
        //nothing to do

        valid = true;
    }

    public boolean isPrintable() {
        return unicode != null;
    }

    @Attribute(name = "value", required = true)
    public Integer getValue() {
        return value;
    }

    @Attribute(name = "name", required = false)
    public String getName() {
        return name;
    }

    @Element(name = "unicode", required = false)
    public Unicode getUnicode() {
        return unicode;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(char character) {
        return isPrintable() && unicode.getCharacter() == character;
    }

    public boolean equals(Keysym keysym) {
        return keysym != null && keysym.getValue().equals(value);
    }

    public boolean equals(Keysym.Ref keysymRef) {
        return keysymRef != null && keysymRef.getValue().equals(value);
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        if (!cycle) {
            cycle = true;
            try {
                obj.put("value", value);
                obj.put("name", name);
                obj.put("unicode", (unicode != null) ? unicode.dump() : null);
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
