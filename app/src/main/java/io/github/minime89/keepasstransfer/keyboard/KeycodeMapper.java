package io.github.minime89.keepasstransfer.keyboard;

import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import io.github.minime89.keepasstransfer.FileManager;

public class KeycodeMapper {
    private static final String TAG = KeycodeMapper.class.getSimpleName();

    public static final String DEFAULT_ID = "default";

    private String id;
    private Map<Integer, Keycode> mappings;
    private ScancodeMapper scancodeMapper;
    private KeysymMapper keysymMapper;

    public class KeycodeMapperException extends Exception {
        KeycodeMapperException() {
            super();
        }

        KeycodeMapperException(String message) {
            super(message);
        }

        KeycodeMapperException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public KeycodeMapper(String id) throws KeycodeMapperException {
        this.id = id;
    }

    public Collection<Keycode> all() {
        return mappings.values();
    }

    public Collection<Pair<Keycode, Symbol>> find(char character) {
        Collection<Pair<Keycode, Symbol>> entries = new ArrayList<>();

        if (!isLoaded()) {
            return entries;
        }

        for (Map.Entry<Integer, Keycode> entry : mappings.entrySet()) {
            Keycode keycode = entry.getValue();
            Collection<Symbol> symbols = keycode.getSymbols();
            for (Symbol symbol : symbols) {
                if (symbol.getKeysym().getUnicodeValue() == character) {
                    entries.add(new Pair<>(keycode, symbol));
                }
            }
        }

        return entries;
    }

    public void load(String scancodeId, String keysymId) throws KeycodeMapperException, ScancodeMapper.ScancodeMapperException, KeysymMapper.KeysymMapperException {
        Log.i(TAG, String.format("load keycode mappings '%s'", id));

        //load scancode mapper
        ScancodeMapper scancodeMapper = new ScancodeMapper(scancodeId);
        scancodeMapper.load();

        //load keysym mapper
        KeysymMapper keysymMapper = new KeysymMapper(keysymId);
        keysymMapper.load();

        //load keycode mappings file
        String data = null;
        try {
            data = FileManager.getInstance().loadKeycodeMapping(id);
        } catch (IOException e) {
            throw new KeycodeMapperException("couldn't load keycode mappings file", e);
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
            if (split.length > 1) {
                int keycodeValue = Integer.decode(split[0]);

                Scancode scancode = scancodeMapper.find(keycodeValue);
                if (scancode == null) { //FIXME this should not happen if the scancode table is complete
                    Log.w(TAG, String.format("couldn't find scancode for keycodeValue '%d'", keycodeValue));
                } else {
                    Collection<Symbol> symbols = new ArrayList<>();

                    //iterate over possible characters for keycodeValue
                    for (int i = 1; i < split.length; i++) {
                        int keysymValue = Integer.decode(split[i]);
                        Keysym keysym = keysymMapper.find(keysymValue);
                        if (keysym == null) { //FIXME this should not happen if the keysym table is complete
                            Log.w(TAG, String.format("couldn't find keysym for keysymValue '%d'", keysymValue));
                        } else {
                            int modifiers = 0x00;
                            //FIXME determine modifier keys from keyboard layout. Hardcoded keys should work for most keyboard layouts for now.
                            if (i == 2) {
                                modifiers = Keystate.MODIFIER_LEFT_SHIFT;
                            } else if (i == 3) {
                                modifiers = Keystate.MODIFIER_RIGHT_ALT;
                            } else if (i == 4) {
                                modifiers = Keystate.MODIFIER_RIGHT_ALT | Keystate.MODIFIER_LEFT_SHIFT;
                            }

                            Keystate keystate = new Keystate(modifiers);
                            Symbol symbol = new Symbol(keysym, keystate);

                            symbols.add(symbol);
                        }
                    }

                    Keycode keycode = new Keycode(keycodeValue, scancode, symbols);
                    mappings.put(keycode.getKeycodeValue(), keycode);
                }
            }
        }

        Log.i(TAG, String.format("loaded %d keycode mappings", mappings.size()));
    }

    public void load() throws KeycodeMapperException, ScancodeMapper.ScancodeMapperException, KeysymMapper.KeysymMapperException {
        load(ScancodeMapper.DEFAULT_ID, KeysymMapper.DEFAULT_ID);
    }

    public boolean isLoaded() {
        return mappings != null;
    }

    public String getId() {
        return id;
    }

    public ScancodeMapper getScancodeMapper() {
        return scancodeMapper;
    }

    public KeysymMapper getKeysymMapper() {
        return keysymMapper;
    }
}
