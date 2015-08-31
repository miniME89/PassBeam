package io.github.minime89.passbeam.xml;

import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

public class TransformMatcher implements Matcher {
    @Override
    public Transform match(Class type) throws Exception {
        if (type.equals(Integer.class)) {
            return new IntegerTransformer();
        }

        return null;
    }
}
