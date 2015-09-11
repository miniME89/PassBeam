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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.xml.ElementList;

import java.util.Collection;
import java.util.Collections;

import io.github.minime89.passbeam.FileManager;

public class Layouts {
    private static final String TAG = Keycodes.class.getSimpleName();

    /**
     *
     */
    private final Collection<Layout> layouts;

    private Layouts(Collection<Layout> layouts) {
        this.layouts = Collections.unmodifiableCollection(layouts);
    }

    /**
     * Load layouts.
     *
     * @return Returns loaded layouts.
     * @throws FileManager.FileManagerException When layouts couldn't be loaded.
     */
    public static Layouts load() throws FileManager.FileManagerException {
        FileManager fileManager = new FileManager();
        Collection<Layout> layouts = fileManager.loadLayouts();

        return new Layouts(layouts);
    }

    /**
     * Find the layout with the given ID.
     *
     * @param id The layout ID.
     * @return Returns the layout.
     */
    public Layout find(String id) {
        for (Layout layout : layouts) {
            if (layout.getId().equals(id)) {
                return layout;
            }
        }

        return null;
    }

    @ElementList(name = "keycodes", inline = true, required = true)
    public Collection<Layout> getLayouts() {
        return layouts;
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray layoutsArr = new JSONArray();
        for (Layout layout : layouts) {
            layoutsArr.put(layout.dump());
        }
        obj.put("layouts", layoutsArr);

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
