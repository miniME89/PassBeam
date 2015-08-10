package io.github.minime89.keepasstransfer.keyboard;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import io.github.minime89.keepasstransfer.Utils;

public class CharacterConverter {
    private static final String TAG = CharacterConverter.class.getSimpleName();

    private static CharacterConverter instance;
    private ScancodeMapper scancodeMapper;
    private CharacterMapper characterMapper;

    public class CharacterConverterException extends Exception {
        CharacterConverterException() {
            super();
        }

        CharacterConverterException(String message) {
            super(message);
        }
    }

    public static CharacterConverter getInstance() {
        if (instance == null) {
            instance = new CharacterConverter();
        }

        return instance;
    }

    private CharacterConverter() {

    }

    public Collection<byte[]> convert(String string) throws CharacterConverterException {
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

    public byte[] convert(char character) throws CharacterConverterException {
        Log.w(TAG, String.format("convert character '%c (\\u%04x)'", character, (int) character));

        //find character mapping
        if (characterMapper == null || !characterMapper.isLoaded()) {
            throw new CharacterConverterException("no character mapping loaded");
        }

        Collection<CharacterMapper.CharacterMap> characterMaps = characterMapper.findAll(character);
        if (characterMaps.size() == 0) {
            throw new CharacterConverterException("couldn't find character mapping for character '" + character + "'");
        }

        Log.i(TAG, "found character mappings: " + Arrays.toString(characterMaps.toArray()));

        //select one character mapping: select one with the least modifier keys
        CharacterMapper.CharacterMap characterMap = characterMaps.iterator().next();
        Iterator<CharacterMapper.CharacterMap> iterator = characterMaps.iterator();
        while (iterator.hasNext()) {
            CharacterMapper.CharacterMap next = iterator.next();
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
        if (scancodeMapper == null || !scancodeMapper.isLoaded()) {
            throw new CharacterConverterException("no scancode mapping loaded");
        }

        int keycode = characterMap.getKeycode();
        ScancodeMapper.ScancodeMap scancodeMap = scancodeMapper.find(keycode);
        if (scancodeMap == null) {
            throw new CharacterConverterException("couldn't find scancode mapping for keycode '" + keycode + "'");
        }

        Log.i(TAG, "found scancode mapping: " + scancodeMap);

        byte[] bytes = new byte[16];
        bytes[0] = (byte) characterMap.getModifiers();
        bytes[2] = (byte) scancodeMap.getScancode();

        Log.i(TAG, "converted character '" + character + "' into keyboard data '" + Utils.bytesToHex(bytes, Utils.HexFormat.SPACING) + "'");

        return bytes;
    }

    public void load() throws ScancodeMapper.ScancodeMapperException, CharacterMapper.CharacterMapperException {
        scancodeMapper.load();
        characterMapper.load();
    }

    public ScancodeMapper getScancodeMapper() {
        return scancodeMapper;
    }

    public void setScancodeMapper(ScancodeMapper scancodeMapper) {
        this.scancodeMapper = scancodeMapper;

        if (characterMapper != null) {
            characterMapper.setScancodeMapper(scancodeMapper);
        }
    }

    public CharacterMapper getCharacterMapper() {
        return characterMapper;
    }

    public void setCharacterMapper(CharacterMapper characterMapper) {
        this.characterMapper = characterMapper;

        if (characterMapper != null) {
            characterMapper.setScancodeMapper(scancodeMapper);
        }
    }
}
