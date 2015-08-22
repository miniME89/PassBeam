package io.github.minime89.keepasstransfer.keyboard;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Root(strict = false)
public class Keycode {
    /**
     *
     */
    private final Integer value;

    /**
     *
     */
    public final Collection<Keysym.Ref> keysymRefs;

    /**
     *
     */
    private Scancode scancode;

    /**
     *
     */
    private Collection<Symbol> symbols;

    /**
     *
     */
    private boolean valid = false;

    /**
     *
     */
    public static class KeycodeBuildException extends Exception {
        KeycodeBuildException() {
            super();
        }

        KeycodeBuildException(String message) {
            super(message);
        }

        KeycodeBuildException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Reference to a keycode. The reference contains only the {@link Keycode#value} of a keycode which can be used to resolve the actual reference to the {@link Keycode} instance.
     */
    @Root(name = "keycode")
    public static class Ref {
        private final Integer value;

        public Ref(@Attribute(name = "value", required = true) Integer value) {
            this.value = value;
        }

        public Ref(Keycode keycode) {
            this.value = keycode.getValue();
        }

        @Attribute(name = "value", required = true)
        public Integer getValue() {
            return value;
        }

        public boolean equals(Keycode.Ref keycodeRef) {
            return keycodeRef.getValue() == value;
        }

        @Override
        public String toString() {
            return String.format("%s%s{value=%d}", Keycode.class.getSimpleName(), getClass().getSimpleName(), value);
        }
    }

    public Keycode(@Attribute(name = "value", required = true) Integer value,
                   @ElementList(inline = true, required = false) Collection<Keysym.Ref> keysymRefs) {
        this.value = value;
        this.keysymRefs = Collections.unmodifiableCollection(keysymRefs);
    }

    public void build(Keysyms keysyms, Scancodes scancodes) throws KeycodeBuildException {
        valid = false;
        scancode = null;
        symbols = null;

        //resolve scancode
        Keycode.Ref ref = new Keycode.Ref(this);
        scancode = scancodes.find(ref);
        if (scancode == null) {
            throw new KeycodeBuildException(String.format("couldn't resolve keycode reference [%s] to a scancode", ref));
        }

        //resolve symbols
        symbols = new ArrayList<>();
        int col = 0;
        for (Keysym.Ref keysymRef : keysymRefs) {
            col++;
            Keysym keysym = keysyms.find(keysymRef);
            if (keysym != null) {
                int modifiers = 0x00;
                //TODO determine modifier keys from keyboard layout. Hardcoded keys should work for most keyboard layouts for now.
                if (col == 2) {
                    modifiers = Keystate.MODIFIER_LEFT_SHIFT;
                } else if (col == 3) {
                    modifiers = Keystate.MODIFIER_RIGHT_ALT;
                } else if (col == 4) {
                    modifiers = Keystate.MODIFIER_RIGHT_ALT | Keystate.MODIFIER_LEFT_SHIFT;
                }

                Keystate keystate = new Keystate(modifiers);
                symbols.add(new Symbol(this, keysym, keystate));
            }
        }

        valid = true;
    }

    public Collection<Symbol> find(char character) {
        Collection<Symbol> found = new ArrayList<>();

        if (symbols != null) {
            for (Symbol symbol : symbols) {
                if (symbol.getKeysym().equals(character)) {
                    found.add(symbol);
                }
            }
        }

        return found;
    }

    @Attribute(name = "value", required = true)
    public Integer getValue() {
        return value;
    }

    @ElementList(inline = true, required = false)
    public Collection<Keysym.Ref> getKeysymRefs() {
        return keysymRefs;
    }

    public Scancode getScancode() {
        return scancode;
    }

    public Collection<Symbol> getSymbols() {
        return symbols;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(Keycode keycode) {
        return keycode != null && keycode.getValue() == value;
    }

    public boolean equals(Keycode.Ref keycodeRef) {
        return keycodeRef != null && keycodeRef.getValue() == value;
    }

    @Override
    public String toString() {
        return String.format("%s{value: %d, keysymRefs: %s}", getClass().getSimpleName(), value, (keysymRefs != null) ? Arrays.toString(keysymRefs.toArray()) : "null");
    }
}
