/*
 * Copyright (C) 2015 Marcel Lehwald
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.minime89.passbeam.keyboard;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import io.github.minime89.passbeam.FileManager;
import io.github.minime89.passbeam.Utils;

/**
 * The Converter is used for converting characters or strings into keyboard events, when written
 * to the appropriate device, produce that character or string.
 * <p/>
 * In order to encode characters or strings using {@link Converter#convert(char)} or {@link Converter#convert(String)},
 * the keycode, keysym and scancode tables need to be loaded using {@link Converter#load(String)} or {@link Converter#load(String, String, String)}.
 */
public class Converter {
    private static final String TAG = Converter.class.getSimpleName();

    /**
     * The keycodes used in the encoding process.
     */
    private Keycodes keycodes;

    /**
     * The keysyms used in the encoding process.
     */
    private Keysyms keysyms;

    /**
     * The scancodes used in the encoding process.
     */
    private Scancodes scancodes;

    /**
     * Exception for {@link Converter}. Thrown in case of a failed character or string conversion in
     * {@link Converter#convert(char)} and {@link Converter#convert(String)}.
     */
    public class ConverterException extends Exception {
        ConverterException() {
            super();
        }

        ConverterException(String message) {
            super(message);
        }
    }

    /**
     * Constructor.
     */
    public Converter() {

    }

    /**
     * Encode a string into a collection of byte sequences which represent a series of keyboard
     * events, when written to the appropriate device, produce that string. Read <a href="https://github.com/pelya/android-keyboard-gadget#how-it-works">android-keyboard-gadget</a>
     * or the USB HID specification for more details on the encoding.
     *
     * @param string The string to encode.
     * @return Returns a collection of encoded keyboard events of the input string. Each byte
     * sequence in the collection represents the keyboard event for the character at that position
     * in the input string.
     * @throws ConverterException When parts of the input string couldn't be encoded.
     */
    public Collection<byte[]> convert(String string) throws ConverterException {
        Log.v(TAG, String.format("convert string '%s'", string));

        Collection<byte[]> collection = new ArrayList<>();

        char[] characters = string.toCharArray();
        for (char character : characters) {
            byte[] bytes = convert(character);
            collection.add(bytes);
        }

        return collection;
    }

    /**
     * Encode a character into a byte sequence which represent a keyboard event, when  written to
     * the appropriate device, produce that character. Read <a href="https://github.com/pelya/android-keyboard-gadget#how-it-works">android-keyboard-gadget</a>
     * or the USB HID specification for more details on the encoding.
     *
     * @param character The character to encode.
     * @return Returns an encoded keyboard event of the input character.
     * @throws ConverterException When parts of the input string couldn't be encoded.
     */
    public synchronized byte[] convert(char character) throws ConverterException {
        Log.v(TAG, String.format("convert character '%c (\\u%04x)'", character, (int) character));

        if (keycodes == null) {
            throw new ConverterException("no keycodes loaded");
        }

        Collection<Symbol> founds = keycodes.find(character);
        if (founds.size() == 0) {
            throw new ConverterException(String.format("couldn't find symbols for character '%c'", character));
        }

        Log.v(TAG, String.format("found %d symbols", founds.size()));

        //select one symbol: select one with the least modifier keys
        Iterator<Symbol> iterator = founds.iterator();
        Symbol selected = founds.iterator().next();
        while (iterator.hasNext()) {
            Symbol symbol = iterator.next();
            Keycode keycode = symbol.getKeycode();
            Keysym keysym = symbol.getKeysym();
            Keystate keystate = symbol.getKeystate();

            Log.v(TAG, String.format("symbol: %s", symbol));

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

        Log.v(TAG, String.format("converted character '%c' into keyboard data '%s'", character, Utils.bytesToHex(bytes, Utils.HexFormat.SPACING)));

        return bytes;
    }

    /**
     * Load the keycode, keysym and scancode tables used for encoding characters and strings into
     * keyboard events using {@link #convert(char)} and {@link #convert(String)}.
     * The keycodes represent the specific keyboard layout and should be chosen according to the
     * desired target keyboard layout for encoding. The keysym and scancode tables are usually
     * fixed, but alternatives can be provided and loaded for future extensions.
     *
     * @param keycodesId  The keycodes ID which will be used for loading the associated keycodes
     *                    file. The ID is directly mapped to the keycodes filename in the keycodes
     *                    directory.
     * @param keysymsId   The keysyms ID which will be used for loading the associated keysyms
     *                    file. The ID is directly mapped to the keysyms filename in the keysyms
     *                    directory.
     * @param scancodesId The scancodes ID which will be used for loading the associated scancodes
     *                    file. The ID is directly mapped to the scancodes filename in the scancodes
     *                    directory.
     * @throws FileManager.FileManagerException
     */
    public synchronized void load(String keycodesId, String keysymsId, String scancodesId) throws FileManager.FileManagerException {
        Log.v(TAG, String.format("load keyboard converter {keycodeId=%s, keysymId=%s, scancodeId=%s}", keycodesId, keysymsId, scancodesId));

        //TODO better handling of failed loading -> set all instances to null on error
        scancodes = Scancodes.load(scancodesId);
        keysyms = Keysyms.load(scancodesId);
        keycodes = Keycodes.load(keycodesId);

        scancodes.build(keycodes, keysyms);
        keysyms.build(keycodes, scancodes);
        keycodes.build(keysyms, scancodes);
    }

    /**
     * Load the keycode, keysym and scancode tables used for encoding characters and strings into
     * keyboard events using {@link #convert(char)} and {@link #convert(String)}.
     * The keycodes represent the specific keyboard layout and should be chosen according to the
     * desired target keyboard layout for encoding. Default tables for keysym and scancode tables
     * will be used.
     *
     * @param keycodesId The keycodes ID which will be used for loading the associated keycodes
     *                   file. The ID is directly mapped to the keycodes filename in the keycodes
     *                   directory.
     * @throws FileManager.FileManagerException
     */
    public void load(String keycodesId) throws FileManager.FileManagerException {
        load(keycodesId, Keysyms.DEFAULT_ID, Scancodes.DEFAULT_ID);
    }

    /**
     * Load the keycode, keysym and scancode tables used for encoding characters and strings into
     * keyboard events using {@link #convert(char)} and {@link #convert(String)}.
     * The keycodes represent the specific keyboard layout and will be loaded according to the
     * provided layout parameter. Default tables for keysym and scancode tables will be used.
     *
     * @param layout The layout which will be used for loading the associated keycodes file.
     * @throws FileManager.FileManagerException
     */
    public void load(Layout layout) throws FileManager.FileManagerException {
        load(layout.getId(), Keysyms.DEFAULT_ID, Scancodes.DEFAULT_ID);
    }

    /**
     * Load the keycode, keysym and scancode tables used for encoding characters and strings into
     * keyboard events using {@link #convert(char)} and {@link #convert(String)}.
     * The keycodes represent the specific keyboard layout and will be loaded according to the
     * provided layout parameter. The keysym and scancode tables are usually fixed, but alternatives
     * can be provided and loaded for future extensions.
     *
     * @param layout      The layout which will be used for loading the associated keycodes file.
     * @param keysymsId   The keysyms ID which will be used for loading the associated keysyms
     *                    file. The ID is directly mapped to the keysyms filename in the keysyms
     *                    directory.
     * @param scancodesId The scancodes ID which will be used for loading the associated scancodes
     *                    file. The ID is directly mapped to the scancodes filename in the scancodes
     *                    directory.
     * @throws FileManager.FileManagerException
     */
    public void load(Layout layout, String keysymsId, String scancodesId) throws FileManager.FileManagerException {
        load(layout.getId(), keysymsId, scancodesId);
    }

    public synchronized Keycodes getKeycodes() {
        return keycodes;
    }

    public synchronized Keysyms getKeysyms() {
        return keysyms;
    }

    public synchronized Scancodes getScancodes() {
        return scancodes;
    }
}
