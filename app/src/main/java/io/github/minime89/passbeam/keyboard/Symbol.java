package io.github.minime89.passbeam.keyboard;

public class Symbol {
    private final Keycode keycode;
    private final Keysym keysym;
    private final Keystate keystate;

    public Symbol(Keycode keycode, Keysym keysym, Keystate keystate) {
        this.keycode = keycode;
        this.keysym = keysym;
        this.keystate = keystate;
    }

    public Keycode getKeycode() {
        return keycode;
    }

    public Keysym getKeysym() {
        return keysym;
    }

    public Keystate getKeystate() {
        return keystate;
    }

    @Override
    public String toString() {
        return String.format("{keysym=%s, keystate=%s}", keysym, keystate);
    }
}
