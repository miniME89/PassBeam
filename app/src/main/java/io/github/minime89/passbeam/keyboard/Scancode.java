package io.github.minime89.passbeam.keyboard;

import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Scancode {
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
    private final Keycode.Ref keycodeRef;

    /**
     *
     */
    private Keycode keycode;

    /**
     *
     */
    private boolean valid = false;

    /**
     *
     */
    private boolean cycle = false;

    /**
     *
     */
    public static class ScancodeBuildException extends Exception {
        ScancodeBuildException() {
            super();
        }

        ScancodeBuildException(String message) {
            super(message);
        }

        ScancodeBuildException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Reference to a scancode. The reference contains only the {@link Scancode#value} of a scancode which can be used to resolve the actual reference to the {@link Scancode} instance.
     */
    @Root(name = "scancode")
    public static class Ref {
        private final Integer value;

        public Ref(@Attribute(name = "value", required = true) Integer value) {
            this.value = value;
        }

        public Ref(Scancode scancode) {
            this.value = scancode.getValue();
        }

        @Attribute(name = "value", required = true)
        public Integer getValue() {
            return value;
        }

        public boolean equals(Scancode.Ref scancodeRef) {
            return scancodeRef.getValue().equals(value);
        }

        public JSONObject dump() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("value", value);

            return obj;
        }

        @Override
        public String toString() {
            try {
                return dump().toString();
            } catch (JSONException e) {
                return super.toString();
            }
        }
    }

    public Scancode(@Attribute(name = "value", required = true) Integer value,
                    @Attribute(name = "name", required = false) String name,
                    @Element(name = "keycode", required = false) Keycode.Ref keycodeRef) {
        this.value = value;
        this.name = name;
        this.keycodeRef = keycodeRef;
    }

    public void build(Keycodes keycodes, Keysyms keysyms) throws ScancodeBuildException {
        valid = false;
        keycode = null;

        //resolve keycode
        keycode = keycodes.find(keycodeRef);

        if (keycode == null) {
            throw new ScancodeBuildException(String.format("couldn't resolve keycode reference [%s] to a keycode", keycodeRef));
        }

        valid = true;
    }

    @Attribute(name = "value", required = true)
    public Integer getValue() {
        return value;
    }

    @Attribute(name = "name", required = false)
    public String getName() {
        return name;
    }

    @Element(name = "keycode", required = false)
    public Keycode.Ref getKeycodeRef() {
        return keycodeRef;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean equals(Scancode scancode) {
        return scancode != null && scancode.getValue().equals(value);
    }

    public boolean equals(Scancode.Ref scancodeRef) {
        return scancodeRef != null && scancodeRef.getValue().equals(value);
    }

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        if (!cycle) {
            cycle = true;
            try {
                obj.put("value", value);
                obj.put("name", name);
                obj.put("keycodeRef", (keycodeRef != null) ? keycodeRef.dump() : null);
                obj.put("keycode", (keycode != null) ? keycode.dump() : null);
            } finally {
                cycle = false;
            }
        }

        return obj;
    }

    @Override
    public String toString() {
        try {
            return dump().toString();
        } catch (JSONException e) {
            return super.toString();
        }
    }
}
