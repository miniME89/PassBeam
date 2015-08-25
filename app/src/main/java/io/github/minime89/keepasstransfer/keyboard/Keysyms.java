package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Arrays;
import java.util.Collection;

import io.github.minime89.keepasstransfer.FileManager;

@Root(strict = false)
public class Keysyms {
    private static final String TAG = Keysyms.class.getSimpleName();
    public static final String DEFAULT_ID = "default";

    /**
     *
     */
    @ElementList(name = "keysyms", inline = true, required = true)
    private Collection<Keysym> keysyms;

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

    private Keysyms() {

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

    public Collection<Keysym> all() {
        return keysyms;
    }

    public Keysym find(Keysym.Ref keysymRef) {
        for (Keysym keysym : keysyms) {
            if (keysym.equals(keysymRef)) {
                return keysym;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("%s{keysyms: %s}", getClass().getSimpleName(), (keysyms != null) ? Arrays.toString(keysyms.toArray()) : "null");
    }
}
