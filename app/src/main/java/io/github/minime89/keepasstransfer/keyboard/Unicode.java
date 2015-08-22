package io.github.minime89.keepasstransfer.keyboard;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Unicode {
    /**
     *
     */
    private Integer value;

    /**
     *
     */
    private String name;

    public Unicode(@Attribute(name = "value", required = true) String name,
                   @Attribute(name = "name", required = false) int value) { //TODO should be required!
        this.name = name;
        this.value = value;
    }

    @Attribute(name = "value", required = true)
    public int getValue() {
        return value;
    }

    @Attribute(name = "name", required = false) //TODO should be required!
    public String getName() {
        return name;
    }

    public char getCharacter() {
        return (char) value.intValue();
    }

    @Override
    public String toString() {
        return String.format("%s{value: %d, name: %s, character: %c}", getClass().getSimpleName(), value, name, getCharacter());
    }
}
