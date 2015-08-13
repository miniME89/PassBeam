package io.github.minime89.keepasstransfer.keyboard;

import java.util.Arrays;
import java.util.Collection;

public class Keycode {
    private int keycodeValue;
    private Scancode scancode;
    private Collection<Symbol> symbols;

    public Keycode(int keycodeValue, Scancode scancode, Collection<Symbol> symbols) {
        this.keycodeValue = keycodeValue;
        this.scancode = scancode;
        this.symbols = symbols;
    }

    public int getKeycodeValue() {
        return keycodeValue;
    }

    public void setKeycodeValue(int keycodeValue) {
        this.keycodeValue = keycodeValue;
    }

    public Collection<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(Collection<Symbol> symbols) {
        this.symbols = symbols;
    }

    public Scancode getScancode() {
        return scancode;
    }

    public void setScancode(Scancode scancode) {
        this.scancode = scancode;
    }

    @Override
    public String toString() {
        return String.format("{keycodeValue=%d, scancode=%s, symbols=%s}", keycodeValue, scancode, Arrays.toString(symbols.toArray()));
    }
}
