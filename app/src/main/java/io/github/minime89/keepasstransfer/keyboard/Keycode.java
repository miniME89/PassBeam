package io.github.minime89.keepasstransfer.keyboard;

import java.util.Arrays;
import java.util.Collection;

public class Keycode {
    private int keycodeValue;
    private Scancode scancode;
    private Collection<Keysym> keysyms;

    public Keycode(int keycodeValue, Scancode scancode, Collection<Keysym> keysyms) {
        this.keycodeValue = keycodeValue;
        this.scancode = scancode;
        this.keysyms = keysyms;
    }

    public int getKeycodeValue() {
        return keycodeValue;
    }

    public void setKeycodeValue(int keycodeValue) {
        this.keycodeValue = keycodeValue;
    }

    public Collection<Keysym> getKeysyms() {
        return keysyms;
    }

    public void setKeysyms(Collection<Keysym> keysyms) {
        this.keysyms = keysyms;
    }

    public Scancode getScancode() {
        return scancode;
    }

    public void setScancode(Scancode scancode) {
        this.scancode = scancode;
    }

    @Override
    public String toString() {
        return String.format("{keycodeValue=%d, scancode=%s, keysyms=%s}", keycodeValue, scancode, Arrays.toString(keysyms.toArray()));
    }
}
