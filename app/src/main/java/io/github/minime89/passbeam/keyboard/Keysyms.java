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
public class Keysyms {
    private static final String TAG = Keysyms.class.getSimpleName();
    public static final String DEFAULT_ID = "default";

    /**
     *
     */
    private final Collection<Keysym> keysyms;

    /**
     * Load keysyms with the specified keysyms ID.
     *
     * @param keysymsId The keysyms ID.
     * @return Returns loaded keysyms.
     * @throws FileManager.FileManagerException When keysyms couldn't be loaded.
     */
    public static Keysyms load(String keysymsId) throws FileManager.FileManagerException {
        FileManager fileManager = new FileManager();

        return fileManager.loadKeysyms(keysymsId);
    }

    private Keysyms(@ElementList(name = "keysyms", inline = true, required = true) Collection<Keysym> keysyms) {
        this.keysyms = Collections.unmodifiableCollection(keysyms);
    }

    public void build(Keycodes keycodes, Scancodes scancodes) {
        for (Keysym keysym : keysyms) {
            try {
                keysym.build(keycodes, scancodes);
            } catch (Keysym.KeysymBuildException e) {
                Log.w(TAG, e.getMessage());
            }
        }
    }

    public Keysym find(Keysym.Ref keysymRef) {
        for (Keysym keysym : keysyms) {
            if (keysym.equals(keysymRef)) {
                return keysym;
            }
        }

        return null;
    }

    @ElementList(name = "keysyms", inline = true, required = true)
    public Collection<Keysym> getKeysyms() {
        return keysyms;
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray keysymsArr = new JSONArray();
        for (Keysym keysym : keysyms) {
            keysymsArr.put(keysym.dump());
        }
        obj.put("keysyms", keysymsArr);

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
