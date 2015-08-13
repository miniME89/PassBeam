package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.minime89.keepasstransfer.FileManager;

public class KeycodeMapper {
    private static final String TAG = KeycodeMapper.class.getSimpleName();

    private String id;
    private Map<Integer, Keycode> mappings;
    private ScancodeMapper scancodeMapper;
    private KeysymMapper keysymMapper;

    public class KeycodeMapperException extends Exception {
        KeycodeMapperException() {
            super();
        }

        KeycodeMapperException(String message) {
            super(message);
        }

        KeycodeMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public KeycodeMapper(String id, ScancodeMapper scancodeMapper, KeysymMapper keysymMapper) throws KeycodeMapperException {
        this.id = id;
        this.scancodeMapper = scancodeMapper;
        this.keysymMapper = keysymMapper;

        load();
    }

    public Collection<Keycode> all() {
        return mappings.values();
    }

    public Collection<Pair<Keycode, Keysym>> find(char character) {
        Collection<Pair<Keycode, Keysym>> entries = new ArrayList<>();

        if (!isLoaded()) {
            return entries;
        }

        for (Map.Entry<Integer, Keycode> entry : mappings.entrySet()) {
            Keycode keycode = entry.getValue();
            Collection<Keysym> keysyms = keycode.getKeysyms();
            for (Keysym keysym : keysyms) {
                if (keysym.getUnicodeValue() == character) {
                    entries.add(new Pair<>(keycode, keysym));
                }
            }
        }

        return entries;
    }

    private void load() throws KeycodeMapperException {
        Log.i(TAG, String.format("load keycodeValue mappings '%s'", id));

        //load keycodeValue mappings file
        String data = null;
        try {
            data = FileManager.getInstance().loadKeycodeMapping(id);
        } catch (IOException e) {
            throw new KeycodeMapperException("couldn't load keycodeValue mappings file", e);
        }

        //verify that a valid scancode mapping is loaded
        if (scancodeMapper == null || !scancodeMapper.isLoaded()) {
            throw new KeycodeMapperException("no valid scancode mapping loaded");
        }

        //verify that a valid keysym mapping is loaded
        if (keysymMapper == null || !keysymMapper.isLoaded()) {
            throw new KeycodeMapperException("no valid keycode mapping loaded");
        }

        mappings = new HashMap<>();

        //read data line by line
        Scanner scanner = new Scanner(data);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            //remove comments
            int commentStartIndex = line.indexOf('#');
            if (commentStartIndex >= 0) {
                line = line.substring(0, commentStartIndex);
            }

            //split line by separator
            String[] split = line.split(",");
            if (split.length > 1) {
                int keycodeValue = Integer.decode(split[0]);

                Scancode scancode = scancodeMapper.find(keycodeValue);
                if (scancode == null) { //FIXME this should not happen if the scancode table is complete
                    Log.w(TAG, String.format("couldn't find scancode for keycodeValue '%d'", keycodeValue));
                } else {
                    Collection<Keysym> keysyms = new ArrayList<>();

                    //iterate over possible characters for keycodeValue
                    for (int i = 1; i < split.length; i++) {
                        int keysymValue = Integer.decode(split[i]);
                        Keysym keysym = keysymMapper.find(keysymValue);
                        if (keysym == null) { //FIXME this should not happen if the keysym table is complete
                            Log.w(TAG, String.format("couldn't find keysym for keysymValue '%d'", keysymValue));
                        } else {
                            if (i == 2) {
                                keysym.setModifiers(Keysym.MODIFIER_LEFT_SHIFT);
                            } else if (i == 3) {
                                keysym.setModifiers(Keysym.MODIFIER_RIGHT_ALT);
                            } else if (i == 4) {
                                keysym.setModifiers(Keysym.MODIFIER_RIGHT_ALT | Keysym.MODIFIER_LEFT_SHIFT);
                            }

                            keysyms.add(keysym);
                        }
                    }

                    Keycode keycode = new Keycode(keycodeValue, scancode, keysyms);
                    mappings.put(keycode.getKeycodeValue(), keycode);
                }
            }
        }

        Log.i(TAG, String.format("loaded %d keycode mappings", mappings.size()));
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public String getId() {
        return id;
    }

    public ScancodeMapper getScancodeMapper() {
        return scancodeMapper;
    }

    public void setScancodeMapper(ScancodeMapper scancodeMapper) {
        this.scancodeMapper = scancodeMapper;
    }

    public KeysymMapper getKeysymMapper() {
        return keysymMapper;
    }

    public void setKeysymMapper(KeysymMapper keysymMapper) {
        this.keysymMapper = keysymMapper;
    }
}
