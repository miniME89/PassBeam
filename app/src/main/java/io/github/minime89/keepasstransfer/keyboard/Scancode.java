package io.github.minime89.keepasstransfer.keyboard;

public class Scancode {
    private int keycodeValue;
    private int scancodeValue;

    public Scancode(int keycodeValue, int scancodeValue) {
        this.keycodeValue = keycodeValue;
        this.scancodeValue = scancodeValue;
    }

    public int getKeycodeValue() {
        return keycodeValue;
    }

    public void setKeycodeValue(int keycodeValue) {
        this.keycodeValue = keycodeValue;
    }

    public int getScancodeValue() {
        return scancodeValue;
    }

    public void setScancodeValue(int scancodeValue) {
        this.scancodeValue = scancodeValue;
    }

    @Override
    public String toString() {
        return String.format("{keycodeValue=%d, scancodeValue=%d}", keycodeValue, scancodeValue);
    }
}
