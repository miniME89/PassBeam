package io.github.minime89.keepasstransfer.binding;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public class IntegerMatcher implements Matcher {
    @Override
    public Transform match(Class type) throws Exception {
        if (type.equals(Integer.class)) {
            return new IntegerConverter();
        }
        return null;
    }
}
