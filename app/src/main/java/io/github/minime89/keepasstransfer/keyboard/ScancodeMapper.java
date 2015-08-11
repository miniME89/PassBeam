package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class ScancodeMapper {
    private static final String TAG = ScancodeMapper.class.getSimpleName();

    private String id;
    private Collection<ScancodeMap> mappings;

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

    public class ScancodeMap {
        private int keycode;
        private int scancode;

        ScancodeMap(int keycode, int scancode) {
            this.keycode = keycode;
            this.scancode = scancode;
        }

        public int getKeycode() {
            return keycode;
        }

        public void setKeycode(int keycode) {
            this.keycode = keycode;
        }

        public int getScancode() {
            return scancode;
        }

        public void setScancode(int scancode) {
            this.scancode = scancode;
        }

        @Override
        public String toString() {
            return String.format("{keycode=%d, scancode=%d}", keycode, scancode);
        }
    }

    public ScancodeMapper(String id) {
        this.id = id;
    }

    public ScancodeMap find(int keycode) {
        if (!isLoaded()) {
            return null;
        }

        for (ScancodeMap scancodeMap : mappings) {
            if (scancodeMap.getKeycode() == keycode) {
                return scancodeMap;
            }
        }

        return null;
    }

    public boolean contains(int keycode) {
        return find(keycode) != null;
    }

    public void load() throws ScancodeMapperException {
        Log.i(TAG, "load scancode mappings");

        //load scancode mappings file
        String data = null;
        try {
            data = MappingManager.getInstance().loadScancodeMapping(id);
        } catch (IOException e) {
            throw new ScancodeMapperException("couldn't load scancode mappings file", e);
        }

        mappings = new ArrayList<>();

        //read data line by line
        Scanner scanner = new Scanner(data);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            //ignore lines which start with # character for comments
            if (!line.startsWith("#")) {
                //split line by separator
                String[] split = line.split(",");
                if (split.length == 2) {
                    int keycode = Integer.decode(split[0]);
                    int scancode = Integer.decode(split[1]);

                    //create and add scancode mappings
                    ScancodeMap scancodeMap = new ScancodeMap(keycode, scancode);
                    mappings.add(scancodeMap);

                    Log.i(TAG, "scancode to keycode mapping: " + scancodeMap);
                }
            }
        }
        scanner.close();

        Log.i(TAG, "loaded " + mappings.size() + " scancode mappings");
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public Collection<ScancodeMap> getMappings() {
        return mappings;
    }
}
