package io.github.minime89.passbeam.keyboard;

public class Keystate {
    public static final int MODIFIER_NONE = 0x00;
    public static final int MODIFIER_LEFT_CTRL = 0x01;
    public static final int MODIFIER_RIGHT_CTRL = 0x10;
    public static final int MODIFIER_LEFT_SHIFT = 0x02;
    public static final int MODIFIER_RIGHT_SHIFT = 0x20;
    public static final int MODIFIER_LEFT_ALT = 0x04;
    public static final int MODIFIER_RIGHT_ALT = 0x40;
    public static final int MODIFIER_LEFT_META = 0x08;
    public static final int MODIFIER_RIGHT_META = 0x80;

    private int modifiers;

    public Keystate(int modifiers) {
        this.modifiers = modifiers;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
}
