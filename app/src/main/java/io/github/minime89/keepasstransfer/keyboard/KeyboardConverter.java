package io.github.minime89.keepasstransfer.keyboard;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import io.github.minime89.keepasstransfer.Utils;

public class KeyboardConverter {
    private static final String TAG = KeyboardConverter.class.getSimpleName();

    private ScancodeMapping scancodeMapping;
    private CharacterMapping characterMapping;

    public class KeyboardConverterException extends Exception {
        KeyboardConverterException() {
            super();
        }

        KeyboardConverterException(String message) {
            super(message);
        }
    }

    public KeyboardConverter() {
        scancodeMapping = new ScancodeMapping();
        characterMapping = new CharacterMapping();

        scancodeMapping.load();
        characterMapping.load("de");
    }

    public Collection<byte[]> convert(String string) throws Exception {
        Log.i(TAG, String.format("convert string '%s'", string));

        Collection<byte[]> collection = new ArrayList<>();

        char[] characters = string.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            char character = characters[i];
            byte[] bytes = convert(character);
            collection.add(bytes);
        }

        return collection;
    }

    public byte[] convert(char character) throws KeyboardConverterException {
        Log.w(TAG, String.format("convert character '%c (\\u%04x)'", character, (int) character));

        //find character mapping
        Collection<CharacterMapping.CharacterMap> characterMaps = characterMapping.findAll(character);
        if (characterMaps.size() == 0) {
            throw new KeyboardConverterException("couldn't find character mapping for character '" + character + "'");
        }

        Log.i(TAG, "found character mappings: " + Arrays.toString(characterMaps.toArray()));

        //select one character mapping: select one with the least modifier keys
        CharacterMapping.CharacterMap characterMap = characterMaps.iterator().next();
        Iterator<CharacterMapping.CharacterMap> iterator = characterMaps.iterator();
        while (iterator.hasNext()) {
            CharacterMapping.CharacterMap next = iterator.next();
            int oneBitCountCurrent = Integer.bitCount(characterMap.getModifiers());
            int oneBitCount = Integer.bitCount(next.getModifiers());

            if (oneBitCount < oneBitCountCurrent) {
                characterMap = next;
            } else if (oneBitCount == oneBitCountCurrent && next.getModifiers() < characterMap.getModifiers()) {
                characterMap = next;
            }
        }

        Log.i(TAG, "selected character mapping: " + characterMap);

        //find scancode mapping
        int keycode = characterMap.getKeycode();
        ScancodeMapping.ScancodeMap scancodeMap = scancodeMapping.find(keycode);
        if (scancodeMap == null) {
            throw new KeyboardConverterException("couldn't find scancode mapping for keycode '" + keycode + "'");
        }

        Log.i(TAG, "found scancode mapping: " + scancodeMap);

        byte[] bytes = new byte[16];
        bytes[0] = (byte) characterMap.getModifiers();
        bytes[2] = (byte) scancodeMap.getScancode();

        Log.i(TAG, "converted character '" + character + "' into keyboard data '" + Utils.bytesToHex(bytes, Utils.HexFormat.SPACING) + "'");

        return bytes;
    }
}
