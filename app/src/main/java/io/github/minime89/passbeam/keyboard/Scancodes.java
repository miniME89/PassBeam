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
     * Load scancodes with the specified scancodes ID.
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

    public void build(Keycodes keycodes, Keysyms keysyms) {
        for (Scancode scancode : scancodes) {
            try {
                scancode.build(keycodes, keysyms);
            } catch (Scancode.ScancodeBuildException e) {
                Log.w(TAG, e.getMessage());
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
