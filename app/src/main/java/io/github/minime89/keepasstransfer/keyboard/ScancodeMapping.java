package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import io.github.minime89.keepasstransfer.KeePassTransfer;

public class ScancodeMapping {
    private static final String TAG = ScancodeMapping.class.getSimpleName();
    private static final String SCAN_MAPPING_FILEPATH = "scancode_mapping.cfg";

    private Collection<ScancodeMap> mappings;

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

    ScancodeMapping() {

    }

    public ScancodeMap find(int keycode) {
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

    public void load() {
        Log.i(TAG, "load scancode mappings");

        mappings = new ArrayList<>();

        BufferedReader reader = null;
        try {
            InputStream is = KeePassTransfer.getContext().getAssets().open(SCAN_MAPPING_FILEPATH);
            reader = new BufferedReader(new InputStreamReader(is));

            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#")) {
                    String[] split = line.split(",");
                    if (split.length == 2) {
                        int keycode = Integer.decode(split[0]);
                        int scancode = Integer.decode(split[1]);

                        ScancodeMap scancodeMap = new ScancodeMap(keycode, scancode);
                        mappings.add(scancodeMap);

                        Log.i(TAG, "scancode to keycode mapping: " + scancodeMap);
                    }
                }

                line = reader.readLine();
            }

            Log.i(TAG, "loaded " + mappings.size() + " scancode mappings");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public Collection<ScancodeMap> getMappings() {
        return mappings;
    }
}
