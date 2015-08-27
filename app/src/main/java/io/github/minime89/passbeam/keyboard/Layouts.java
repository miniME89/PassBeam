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

    private Layouts(Collection<Layout> layouts) {
        this.layouts = Collections.unmodifiableCollection(layouts);
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
            return "ERROR";
        }
    }
}
