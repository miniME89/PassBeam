package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.minime89.keepasstransfer.FileManager;

public class ScancodeMapper {
    private static final String TAG = ScancodeMapper.class.getSimpleName();

    public static final String DEFAULT_ID = "default";

    private String id;
    private Map<Integer, Scancode> mappings;

    public class ScancodeMapperException extends Exception {
        ScancodeMapperException() {
            super();
        }

        ScancodeMapperException(String message) {
            super(message);
        }

        ScancodeMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public ScancodeMapper(String id) throws ScancodeMapperException {
        this.id = id;
    }

    public Collection<Scancode> all() {
        return mappings.values();
    }

    public Scancode find(int keycodeValue) {
        if (!isLoaded()) {
            return null;
        }

        return mappings.get(keycodeValue);
    }

    public boolean contains(int keycode) {
        return find(keycode) != null;
    }

    public void load() throws ScancodeMapperException {
        Log.i(TAG, String.format("load scancode mappings '%s'", id));

        //load scancode mappings file
        String data;
        try {
            data = FileManager.getInstance().loadScancodeMapping(id);
        } catch (IOException e) {
            throw new ScancodeMapperException("couldn't load scancode mappings file", e);
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
            if (split.length == 2) {
                int keycodeValue = Integer.decode(split[0]);
                int scancodeValue = Integer.decode(split[1]);

                Scancode scancode = new Scancode(keycodeValue, scancodeValue);
                mappings.put(keycodeValue, scancode);
            }
        }
        scanner.close();

        Log.i(TAG, String.format("loaded %d scancode mappings", mappings.size()));
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public String getId() {
        return id;
    }
}
