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
            return "ERROR";
        }
    }
}
