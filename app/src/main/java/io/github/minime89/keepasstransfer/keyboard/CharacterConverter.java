package io.github.minime89.keepasstransfer.keyboard;


import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import io.github.minime89.keepasstransfer.Utils;

public class CharacterConverter {
    private static final String TAG = CharacterConverter.class.getSimpleName();

    private static CharacterConverter instance;
    private KeycodeMapper keycodeMapper;

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
        if (keycodeMapper == null || !keycodeMapper.isLoaded()) {
            throw new CharacterConverterException("no character mapping loaded");
        }

        Collection<Pair<Keycode, Keysym>> founds = keycodeMapper.find(character);
        if (founds.size() == 0) {
            throw new CharacterConverterException(String.format("couldn't find character mapping for character '%c'", character));
        }

        Log.i(TAG, String.format("found %d character mappings: %s", founds.size(), Arrays.toString(founds.toArray())));

        //select one character mapping: select one with the least modifier keys
        Iterator<Pair<Keycode, Keysym>> iterator = founds.iterator();
        Pair<Keycode, Keysym> selected = iterator.next();
        while (iterator.hasNext()) {
            Pair<Keycode, Keysym> next = iterator.next();
            Keycode keycode = next.first;
            Keysym keysym = next.second;

            int oneBitCountSelected = Integer.bitCount(selected.second.getModifiers());
            int oneBitCount = Integer.bitCount(keysym.getModifiers());

            if ((oneBitCount < oneBitCountSelected) || (oneBitCount == oneBitCountSelected && keysym.getModifiers() < selected.second.getModifiers())) {
                selected = new Pair<>(keycode, keysym);
            }
        }

        Log.i(TAG, String.format("selected mapping: %s", selected));

        byte[] bytes = new byte[16];
        bytes[0] = (byte) selected.second.getModifiers();
        bytes[2] = (byte) selected.first.getScancode().getScancodeValue();

        Log.i(TAG, String.format("converted character '%c' into keyboard data '%s'", character, Utils.bytesToHex(bytes, Utils.HexFormat.SPACING)));

        return bytes;
    }

    public KeycodeMapper getKeycodeMapper() {
        return keycodeMapper;
    }

    public void setKeycodeMapper(KeycodeMapper keycodeMapper) {
        this.keycodeMapper = keycodeMapper;
    }
}
