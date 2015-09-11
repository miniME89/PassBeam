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
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Unicode {
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
    private boolean cycle = false;

    public Unicode(@Attribute(name = "value", required = true) Integer value,
                   @Attribute(name = "name", required = false) String name) { //TODO should be required!
        this.name = name;
        this.value = value;
    }

    @Attribute(name = "value", required = true)
    public Integer getValue() {
        return value;
    }

    @Attribute(name = "name", required = false) //TODO should be required!
    public String getName() {
        return name;
    }

    public char getCharacter() {
        return (char) value.intValue();
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        if (!cycle) {
            cycle = true;
            try {
                obj.put("value", value);
                obj.put("name", name);
                obj.put("character", getCharacter());
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
