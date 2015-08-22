package io.github.minime89.keepasstransfer.keyboard;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import io.github.minime89.keepasstransfer.FileManager;
import io.github.minime89.keepasstransfer.Utils;

public class Converter {
    private static final String TAG = Converter.class.getSimpleName();

    private Keycodes keycodes;
    private Keysyms keysyms;
    private Scancodes scancodes;

    public class CharacterConverterException extends Exception {
        CharacterConverterException() {
            super();
        }

        CharacterConverterException(String message) {
            super(message);
        }
    }

    public Converter() {

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
        if (keycodes == null) {
            throw new CharacterConverterException("no character mapping loaded");
        }

        Collection<Symbol> founds = keycodes.find(character);
        if (founds.size() == 0) {
            throw new CharacterConverterException(String.format("couldn't find symbol mapping for character '%c'", character));
        }

        Log.d(TAG, String.format("found %d symbol mappings", founds.size()));

        //select one character mapping: select one with the least modifier keys
        Iterator<Symbol> iterator = founds.iterator();
        Symbol selected = founds.iterator().next();
        while (iterator.hasNext()) {
            Symbol symbol = iterator.next();
            Keycode keycode = symbol.getKeycode();
            Keysym keysym = symbol.getKeysym();
            Keystate keystate = symbol.getKeystate();

            Log.d(TAG, String.format("symbol: %s", symbol));

            Keystate selectedKeystate = selected.getKeystate();
            int selectedOneBitCount = Integer.bitCount(selectedKeystate.getModifiers());
            int oneBitCount = Integer.bitCount(keystate.getModifiers());

            if ((oneBitCount < selectedOneBitCount) || (oneBitCount == selectedOneBitCount && keystate.getModifiers() < selectedKeystate.getModifiers())) {
                selected = symbol;
            }
        }

        byte[] bytes = new byte[16];
        bytes[0] = (byte) selected.getKeystate().getModifiers();
        bytes[2] = selected.getKeycode().getScancode().getValue().byteValue();

        Log.d(TAG, String.format("converted character '%c' into keyboard data '%s'", character, Utils.bytesToHex(bytes, Utils.HexFormat.SPACING)));

        return bytes;
    }

    public void load(String keycodesId, String keysymsId, String scancodesId) throws FileManager.FileManagerException {
        Log.i(TAG, String.format("load keyboard symbol converter {keycodeId=%s, keysymId=%s, scancodeId=%s}", keycodesId, keysymsId, scancodesId));

        scancodes = Scancodes.load(scancodesId);
        keysyms = Keysyms.load(scancodesId);
        keycodes = Keycodes.load(keycodesId);

        scancodes.build(keycodes, keysyms);
        keysyms.build(keycodes, scancodes);
        keycodes.build(keysyms, scancodes);
    }

    public void load(String keycodeId) throws FileManager.FileManagerException {
        load(keycodeId, Keysyms.DEFAULT_ID, Scancodes.DEFAULT_ID);
    }

    public Keycodes getKeycodes() {
        return keycodes;
    }

    public Keysyms getKeysyms() {
        return keysyms;
    }

    public Scancodes getScancodes() {
        return scancodes;
    }
}
