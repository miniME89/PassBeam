package io.github.minime89.keepasstransfer.keyboard;

public class Symbol {
    private Keysym keysym;
    private Keystate keystate;

    public Symbol(Keysym keysym, Keystate keystate) {
        this.keysym = keysym;
        this.keystate = keystate;
    }

    public Keysym getKeysym() {
        return keysym;
    }

    public void setKeysym(Keysym keysym) {
        this.keysym = keysym;
    }

    public Keystate getKeystate() {
        return keystate;
    }

    public void setKeystate(Keystate keystate) {
        this.keystate = keystate;
    }
}
