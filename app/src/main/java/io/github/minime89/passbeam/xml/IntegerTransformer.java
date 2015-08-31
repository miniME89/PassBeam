package io.github.minime89.passbeam.xml;

import org.simpleframework.xml.transform.Transform;

public class IntegerTransformer implements Transform<Integer> {
    public Integer read(String value) {
        return Integer.decode(value);
    }

    public String write(Integer value) {
        return String.valueOf(value);
    }
}
