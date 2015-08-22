package io.github.minime89.keepasstransfer.binding;

import org.simpleframework.xml.transform.Transform;

public class IntegerConverter implements Transform<Integer> {
    public Integer read(String value) {
        return Integer.decode(value);
    }

    public String write(Integer value)  {
        return String.valueOf(value);
    }
}
