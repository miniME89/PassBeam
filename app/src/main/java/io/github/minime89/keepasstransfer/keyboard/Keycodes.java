package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import io.github.minime89.keepasstransfer.FileManager;

@Root(strict = false)
public class Keycodes {
    private static final String TAG = Keycodes.class.getSimpleName();
    public static final String DEFAULT_ID = "us";

    /**
     *
     */
    @Element(name = "layout", required = true)
    private Layout layout;

    /**
     *
     */
    @ElementList(name = "keycodes", inline = true, required = true)
    private Collection<Keycode> keycodes;

    /**
     * Load keycodes with the specified keycodes ID.
     *
     * @param keycodesId The keycodes ID.
     * @return Returns loaded keycodes.
     * @throws FileManager.FileManagerException When keycodes couldn't be loaded.
     */
    public static Keycodes load(String keycodesId) throws FileManager.FileManagerException {
        FileManager fileManager = new FileManager();

        return fileManager.loadKeycodes(keycodesId);
    }

    private Keycodes() {

    }

    public void build(Keysyms keysyms, Scancodes scancodes) {
        for (Keycode keycode : keycodes) {
            try {
                keycode.build(keysyms, scancodes);
            } catch (Keycode.KeycodeBuildException e) {
                Log.w(TAG, e.getMessage());
            }
        }
    }

    public Collection<Keycode> all() {
        return keycodes;
    }

    public Keycode find(Keycode.Ref keycodeRef) {
        for (Keycode keycode : keycodes) {
            if (keycode.equals(keycodeRef)) {
                return keycode;
            }
        }

        return null;
    }

    public Collection<Symbol> find(char character) {
        Collection<Symbol> found = new ArrayList<>();
        for (Keycode keycode : keycodes) {
            Collection<Symbol> symbols = keycode.find(character);
            found.addAll(symbols);
        }

        return found;
    }

    public Layout getLayout() {
        return layout;
    }

    @Override
    public String toString() {
        return String.format("%s{keycodes: %s}", getClass().getSimpleName(), (keycodes != null) ? Arrays.toString(keycodes.toArray()) : "null");
    }
}
