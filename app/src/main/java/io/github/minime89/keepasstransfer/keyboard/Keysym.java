package io.github.minime89.keepasstransfer.keyboard;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Keysym {
    /**
     *
     */
    private final Integer value;

    /**
     *
     */
    private final String name;

    /**
     *
     */
    private final Unicode unicode;

    /**
     *
     */
    private boolean valid = false;

    /**
     *
     */
    public static class KeysymBuildException extends Exception {
        KeysymBuildException() {
            super();
        }

        KeysymBuildException(String message) {
            super(message);
        }

        KeysymBuildException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Reference to a keysym. The reference contains only the {@link Keysym#value} of a keysym which can be used to resolve the actual reference to the {@link Keysym} instance.
     */
    @Root(name = "keysym")
    public static class Ref {
        private final Integer value;

        public Ref(@Attribute(name = "value", required = true) Integer value) {
            this.value = value;
        }

        public Ref(Keysym keysym) {
            this.value = keysym.getValue();
        }

        @Attribute(name = "value", required = true)
        public Integer getValue() {
            return value;
        }

        public boolean equals(Keysym.Ref keysymRef) {
            return keysymRef.getValue() == value;
        }

        @Override
        public String toString() {
            return String.format("%s%s{value=%d}", Keysym.class.getSimpleName(), getClass().getSimpleName(), value);
        }
    }

    public Keysym(@Attribute(name = "value", required = true) Integer value,
                  @Attribute(name = "name", required = false) String name,
                  @Element(name = "unicode", required = false) Unicode unicode) {
        this.value = value;
        this.name = name;
        this.unicode = unicode;
    }

    public void build(Keycodes keycodes, Scancodes scancodes) throws KeysymBuildException {
        //nothing to do

        valid = true;
    }

    public boolean isPrintable() {
        return unicode != null;
    }

    @Attribute(name = "value", required = true)
    public Integer getValue() {
        return value;
    }

    @Attribute(name = "name", required = false)
    public String getName() {
        return name;
    }

    @Element(name = "unicode", required = false)
    public Unicode getUnicode() {
        return unicode;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(char character) {
        return isPrintable() && unicode.getCharacter() == character;
    }

    public boolean equals(Keysym keysym) {
        return keysym != null && keysym.getValue() == value;
    }

    public boolean equals(Keysym.Ref keysymRef) {
        return keysymRef != null && keysymRef.getValue() == value;
    }

    @Override
    public String toString() {
        return String.format("%s{value: %d, name: %s, unicode: %s}", getClass().getSimpleName(), value, name, unicode);
    }
}
