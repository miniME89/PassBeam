package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import io.github.minime89.keepasstransfer.KeePassTransfer;
import io.github.minime89.keepasstransfer.Utils;

public class CharacterMapping {
    private static final String TAG = CharacterMapping.class.getSimpleName();

    private Collection<CharacterMap> mappings;

    public class CharacterMap {
        public static final int MODIFIER_NONE = 0x00;
        public static final int MODIFIER_LEFT_CTRL = 0x01;
        public static final int MODIFIER_RIGHT_CTRL = 0x10;
        public static final int MODIFIER_LEFT_SHIFT = 0x02;
        public static final int MODIFIER_RIGHT_SHIFT = 0x20;
        public static final int MODIFIER_LEFT_ALT = 0x04;
        public static final int MODIFIER_RIGHT_ALT = 0x40;
        public static final int MODIFIER_LEFT_META = 0x08;
        public static final int MODIFIER_RIGHT_META = 0x80;

        private int keycode;
        private int modifiers;
        private char character;

        CharacterMap(int keycode, char character) {
            this.keycode = keycode;
            this.modifiers = MODIFIER_NONE;
            this.character = character;
        }

        CharacterMap(int keycode, int modifiers, char character) {
            this.keycode = keycode;
            this.modifiers = modifiers;
            this.character = character;
        }

        public int getKeycode() {
            return keycode;
        }

        public void setKeycode(int keycode) {
            this.keycode = keycode;
        }

        public int getModifiers() {
            return modifiers;
        }

        public void setModifiers(int modifiers) {
            this.modifiers = modifiers;
        }

        public void setModifier(int modifier) {
            modifiers |= modifier;
        }

        public void unsetModifier(int modifier) {
            modifiers &= ~modifier;
        }

        public boolean isModifier(int modifier) {
            return (modifiers & modifier) > 0;
        }

        public char getCharacter() {
            return character;
        }

        public void setCharacter(char character) {
            this.character = character;
        }

        @Override
        public String toString() {
            return String.format("{keycode=%d, modifiers=%s, character=%c (\\u%04x)}", keycode, Utils.byteToHex((byte) modifiers, Utils.HexFormat.CONDENSED), character, (int) character);
        }
    }

    CharacterMapping() {

    }

    public CharacterMap find(char character) {
        if (!isLoaded()) {
            return null;
        }

        for (CharacterMap characterMap : mappings) {
            if (characterMap.getCharacter() == character) {
                return characterMap;
            }
        }

        return null;
    }

    public Collection<CharacterMap> findAll(char character) {
        Collection<CharacterMap> characterMaps = new ArrayList<>();

        if (!isLoaded()) {
            return characterMaps;
        }

        for (CharacterMap characterMap : mappings) {
            if (characterMap.getCharacter() == character) {
                characterMaps.add(characterMap);
            }
        }

        return characterMaps;
    }

    public void load(String id) {
        Log.i(TAG, "load character mappings '" + id + "'");

        mappings = new ArrayList<>();

        ScancodeMapping scancodeMapping = new ScancodeMapping();
        scancodeMapping.load();

        BufferedReader reader = null;
        try {
            String filename = "layouts/" + id + ".cfg";
            InputStream is = KeePassTransfer.getContext().getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(is));

            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#")) {
                    String[] split = line.split(",");
                    int keycode = Integer.decode(split[0]);

                    if (scancodeMapping.contains(keycode)) {
                        //iterate over possible characters for keycode
                        for (int i = 1; i < split.length; i++) {
                            int unicode = Integer.decode(split[i]);

                            if (unicode <= 0xFFFF && unicode != 0x0000) {
                                CharacterMap characterMap = new CharacterMap(keycode, (char) unicode);

                                //see "Keymap table" on https://wiki.archlinux.org/index.php/Xmodmap
                                if (i == 2) {
                                    characterMap.setModifier(CharacterMap.MODIFIER_LEFT_SHIFT);
                                } else if (i == 3) {
                                    characterMap.setModifier(CharacterMap.MODIFIER_LEFT_ALT);
                                } else if (i == 4) {
                                    characterMap.setModifier(CharacterMap.MODIFIER_LEFT_ALT);
                                    characterMap.setModifier(CharacterMap.MODIFIER_LEFT_SHIFT);
                                } else if (i == 5) {
                                    characterMap.setModifier(CharacterMap.MODIFIER_RIGHT_ALT);
                                } else if (i == 6) {
                                    characterMap.setModifier(CharacterMap.MODIFIER_RIGHT_ALT);
                                    characterMap.setModifier(CharacterMap.MODIFIER_LEFT_SHIFT);
                                }

                                mappings.add(characterMap);

                                Log.i(TAG, "character to keycode mappings: " + characterMap.toString());
                            }
                        }
                    }
                }

                line = reader.readLine();
            }

            Log.i(TAG, "loaded " + mappings.size() + " character mappings");
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

    public Collection<CharacterMap> getMappings() {
        return mappings;
    }
}
