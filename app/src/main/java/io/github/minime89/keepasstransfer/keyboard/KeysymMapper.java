package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.minime89.keepasstransfer.FileManager;

public class KeysymMapper {
    private static final String TAG = KeysymMapper.class.getSimpleName();

    public static final String DEFAULT_ID = "default";

    private String id;
    private Map<Integer, Keysym> mappings;

    public class KeysymMapperException extends Exception {
        KeysymMapperException() {
            super();
        }

        KeysymMapperException(String message) {
            super(message);
        }

        KeysymMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public KeysymMapper(String id) throws KeysymMapperException {
        this.id = id;
    }

    public Collection<Keysym> all() {
        return mappings.values();
    }

    public Keysym find(int keysymValue) {
        if (!isLoaded()) {
            return null;
        }

        return mappings.get(keysymValue);
    }

    public boolean contains(int keysymValue) {
        return find(keysymValue) != null;
    }

    public void load() throws KeysymMapperException {
        Log.i(TAG, String.format("load keysym mappings '%s'", id));

        //load keysym mappings file
        String data = null;
        try {
            data = FileManager.getInstance().loadKeysymMapping(id);
        } catch (IOException e) {
            throw new KeysymMapperException("couldn't load keysym mappings file", e);
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
            if (split.length == 2 || split.length == 4) {
                Keysym keysym;

                String keysymName = split[0];
                int keysymValue = Integer.decode(split[1]);

                if (split.length == 4) {
                    String unicodeName = split[3];
                    char unicodeValue = (char) ((int) Integer.decode(split[2]));

                    keysym = new Keysym(keysymName, keysymValue, unicodeName, unicodeValue);
                } else {
                    keysym = new Keysym(keysymName, keysymValue);
                }

                mappings.put(keysym.getKeysymValue(), keysym);
            }
        }
        scanner.close();

        Log.i(TAG, String.format("loaded %d keysym mappings", mappings.size()));
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public String getId() {
        return id;
    }
}
