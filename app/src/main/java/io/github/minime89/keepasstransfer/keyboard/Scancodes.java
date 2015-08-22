package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Arrays;
import java.util.Collection;

import io.github.minime89.keepasstransfer.FileManager;

@Root(strict = false)
public class Scancodes {
    private static final String TAG = Scancodes.class.getSimpleName();
    public static final String DEFAULT_ID = "default";

    @ElementList(name = "scancodes", inline = true, required = true)
    private Collection<Scancode> scancodes;

    /**
     * Load scancodes with the specified scancodes ID.
     *
     * @param scancodesId The scancodes ID.
     * @return Returns loaded scancodes.
     * @throws FileManager.FileManagerException When scancodes couldn't be loaded.
     */
    public static Scancodes load(String scancodesId) throws FileManager.FileManagerException {
        return FileManager.getInstance().loadScancodes(scancodesId);
    }

    private Scancodes() {

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

    public Collection<Scancode> all() {
        return scancodes;
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

    @Override
    public String toString() {
        return String.format("%s{scancodes: %s}", getClass().getSimpleName(), (scancodes != null) ? Arrays.toString(scancodes.toArray()) : "null");
    }
}
