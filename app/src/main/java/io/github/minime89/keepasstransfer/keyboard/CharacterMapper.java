package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import io.github.minime89.keepasstransfer.Utils;

public class CharacterMapper {
    private static final String TAG = CharacterMapper.class.getSimpleName();

    private String id;
    private Collection<CharacterMap> mappings;
    private ScancodeMapper scancodeMapper;

    public class CharacterMapperException extends Exception {
        CharacterMapperException() {
            super();
        }

        CharacterMapperException(String message) {
            super(message);
        }

        CharacterMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

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

    public CharacterMapper(String id) {
        this.id = id;
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

    public void load() throws CharacterMapperException {
        Log.i(TAG, "load character mappings '" + id + "'");

        //load character mappings file
        String data = null;
        try {
            data = MappingFileManager.getInstance().loadCharacterMapping(id);
        } catch (IOException e) {
            throw new CharacterMapperException("couldn't load character mappings file", e);
        }

        //verify that a valid scancode mappings is loaded
        if (scancodeMapper == null || !scancodeMapper.isLoaded()) {
            throw new CharacterMapperException("no valid scancode mappings is loaded");
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
                int keycode = Integer.decode(split[0]);

                //ignore characters which do not have a scancode mappings
                if (scancodeMapper.contains(keycode)) {
                    //iterate over possible characters for keycode
                    for (int i = 1; i < split.length; i++) {
                        int unicode = Integer.decode(split[i]);

                        if (unicode <= 0xFFFF && unicode != 0x0000) {
                            //create character mappings
                            CharacterMap characterMap = new CharacterMap(keycode, (char) unicode);

                            //define modifier keys based on the column position. See "Keymap table" on https://wiki.archlinux.org/index.php/Xmodmap
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

                            //add character mappings
                            mappings.add(characterMap);

                            Log.i(TAG, "character to keycode mapping: " + characterMap.toString());
                        }
                    }
                }
            }
        }

        Log.i(TAG, "loaded " + mappings.size() + " character mappings");
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public String getId() {
        return id;
    }

    public Collection<CharacterMap> getMappings() {
        return mappings;
    }

    public ScancodeMapper getScancodeMapper() {
        return scancodeMapper;
    }

    public void setScancodeMapper(ScancodeMapper scancodeMapper) {
        this.scancodeMapper = scancodeMapper;
    }
}
