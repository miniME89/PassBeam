package io.github.minime89.keepasstransfer.keyboard;

import org.simpleframework.xml.ElementList;

import java.util.Collection;

import io.github.minime89.keepasstransfer.FileManager;

public class Layouts {
    private static final String TAG = Keycodes.class.getSimpleName();

    /**
     *
     */
    @ElementList(name = "keycodes", inline = true, required = true)
    private Collection<Layout> layouts;

    /**
     * Load layouts.
     *
     * @return Returns loaded layouts.
     * @throws FileManager.FileManagerException When layouts couldn't be loaded.
     */
    public static Layouts load() throws FileManager.FileManagerException {
        FileManager fileManager = new FileManager();
        Collection<Layout> layouts = fileManager.loadLayouts();

        Layouts instance = new Layouts();
        instance.layouts = layouts;

        return instance;
    }

    private Layouts() {

    }

    public Collection<Layout> all() {
        return layouts;
    }
}
