package io.github.minime89.keepasstransfer.keyboard;

public class Keysym {
    private String keysymName;
    private int keysymValue;
    private String unicodeName;
    private char unicodeValue;

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

    @Override
    public String toString() {
        return String.format("{keysymName=%s, keysymValue=%d, unicodeName=%s, unicodeValue=%c}", keysymName, keysymValue, unicodeName, unicodeValue);
    }
}
