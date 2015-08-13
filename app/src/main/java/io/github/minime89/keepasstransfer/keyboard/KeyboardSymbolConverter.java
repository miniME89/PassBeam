package io.github.minime89.keepasstransfer.keyboard;


import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import io.github.minime89.keepasstransfer.Utils;

public class KeyboardSymbolConverter {
    private static final String TAG = KeyboardSymbolConverter.class.getSimpleName();

    private KeycodeMapper keycodeMapper;

    public class CharacterConverterException extends Exception {
        CharacterConverterException() {
            super();
        }

        CharacterConverterException(String message) {
            super(message);
        }
    }

    public KeyboardSymbolConverter() {

    }

    public Collection<byte[]> convert(String string) throws CharacterConverterException {
        Log.d(TAG, String.format("convert string '%s'", string));

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
        Log.d(TAG, "===================================================================================================");
        Log.d(TAG, String.format("convert character '%c (\\u%04x)'", character, (int) character));

        //find character mapping
        if (keycodeMapper == null || !keycodeMapper.isLoaded()) {
            throw new CharacterConverterException("no character mapping loaded");
        }

        Collection<Pair<Keycode, Symbol>> founds = keycodeMapper.find(character);
        if (founds.size() == 0) {
            throw new CharacterConverterException(String.format("couldn't find character mapping for character '%c'", character));
        }

        Log.d(TAG, String.format("found %d character mappings:", founds.size()));


        //select one character mapping: select one with the least modifier keys
        Iterator<Pair<Keycode, Symbol>> iterator = founds.iterator();
        Pair<Keycode, Symbol> selected = iterator.next();
        while (iterator.hasNext()) {
            Pair<Keycode, Symbol> next = iterator.next();
            Keycode keycode = next.first;
            Symbol symbol = next.second;
            Keystate keystate = symbol.getKeystate();

            Log.d(TAG, String.format("{keycode=%s, symbol=%s}", keycode, symbol));

            int oneBitCountSelected = Integer.bitCount(selected.second.getKeystate().getModifiers());
            int oneBitCount = Integer.bitCount(keystate.getModifiers());

            if ((oneBitCount < oneBitCountSelected) || (oneBitCount == oneBitCountSelected && keystate.getModifiers() < selected.second.getKeystate().getModifiers())) {
                selected = new Pair<>(keycode, symbol);
            }
        }

        Log.d(TAG, String.format("selected mapping: {keycode=%s, symbol=%s}", selected.first, selected.second));

        byte[] bytes = new byte[16];
        bytes[0] = (byte) selected.second.getKeystate().getModifiers();
        bytes[2] = (byte) selected.first.getScancode().getScancodeValue();

        Log.d(TAG, String.format("converted character '%c' into keyboard data '%s'", character, Utils.bytesToHex(bytes, Utils.HexFormat.SPACING)));

        return bytes;
    }

    public void load(String keycodeId, String scancodeId, String keysymId) throws ScancodeMapper.ScancodeMapperException, KeysymMapper.KeysymMapperException, KeycodeMapper.KeycodeMapperException {
        Log.i(TAG, "load keyboard symbol converter");

        keycodeMapper = new KeycodeMapper(keycodeId);
        keycodeMapper.load(scancodeId, keysymId);
    }

    public void load(String keycodeId) throws ScancodeMapper.ScancodeMapperException, KeysymMapper.KeysymMapperException, KeycodeMapper.KeycodeMapperException {
        load(keycodeId, ScancodeMapper.DEFAULT_ID, KeycodeMapper.DEFAULT_ID);
    }

    public KeycodeMapper getKeycodeMapper() {
        return keycodeMapper;
    }

    public void setKeycodeMapper(KeycodeMapper keycodeMapper) {
        this.keycodeMapper = keycodeMapper;
    }
}
