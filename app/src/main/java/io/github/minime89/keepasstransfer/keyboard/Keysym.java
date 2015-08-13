package io.github.minime89.keepasstransfer.keyboard;

import io.github.minime89.keepasstransfer.Utils;

public class Keysym {
    public static final int MODIFIER_NONE = 0x00;
    public static final int MODIFIER_LEFT_CTRL = 0x01;
    public static final int MODIFIER_RIGHT_CTRL = 0x10;
    public static final int MODIFIER_LEFT_SHIFT = 0x02;
    public static final int MODIFIER_RIGHT_SHIFT = 0x20;
    public static final int MODIFIER_LEFT_ALT = 0x04;
    public static final int MODIFIER_RIGHT_ALT = 0x40;
    public static final int MODIFIER_LEFT_META = 0x08;
    public static final int MODIFIER_RIGHT_META = 0x80;

    private String keysymName;
    private int keysymValue;
    private String unicodeName;
    private char unicodeValue;
    private int modifiers;

    public Keysym(String keysymName, int keysymValue, String unicodeName, char unicodeValue) {
        this.keysymName = keysymName;
        this.keysymValue = keysymValue;
        this.unicodeName = unicodeName;
        this.unicodeValue = unicodeValue;
    }

    public Keysym(String keysymName, int keysymValue) {
        this.keysymName = keysymName;
        this.keysymValue = keysymValue;
    }

    public String getKeysymName() {
        return keysymName;
    }

    public void setKeysymName(String keysymName) {
        this.keysymName = keysymName;
    }

    public int getKeysymValue() {
        return keysymValue;
    }

    public void setKeysymValue(int keysymValue) {
        this.keysymValue = keysymValue;
    }

    public String getUnicodeName() {
        return unicodeName;
    }

    public void setUnicodeName(String unicodeName) {
        this.unicodeName = unicodeName;
    }

    public char getUnicodeValue() {
        return unicodeValue;
    }

    public void setUnicodeValue(char unicodeValue) {
        this.unicodeValue = unicodeValue;
    }

    public boolean isPrintable() {
        return unicodeValue > 0;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return String.format("{keysymName=%s, keysymValue=%d, unicodeName=%s, unicodeValue=%c, modifiers=%s}", keysymName, keysymValue, unicodeName, unicodeValue, Utils.byteToHex((byte) modifiers, Utils.HexFormat.SPACING));
    }
}
