package io.github.minime89.keepasstransfer.keyboard;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Layout {
    private final String layoutName;
    private final String layoutDescription;
    private final String variantName;
    private final String variantDescription;
    private final String filename;

    public Layout(@Element(name = "layoutName", required = false) String layoutName,
                  @Element(name = "layoutDescription", required = false) String layoutDescription,
                  @Element(name = "variantName", required = false) String variantName,
                  @Element(name = "variantDescription", required = false) String variantDescription,
                  @Element(name = "filename", required = false) String filename) {
        this.layoutName = layoutName;
        this.layoutDescription = layoutDescription;
        this.variantName = variantName;
        this.variantDescription = variantDescription;
        this.filename = filename;
    }

    @Element(name = "layoutName", required = false)
    public String getLayoutName() {
        return layoutName;
    }

    @Element(name = "layoutDescription", required = false)
    public String getLayoutDescription() {
        return layoutDescription;
    }

    @Element(name = "variantName", required = false)
    public String getVariantName() {
        return variantName;
    }

    @Element(name = "variantDescription", required = false)
    public String getVariantDescription() {
        return variantDescription;
    }

    @Element(name = "filename", required = false)
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return String.format("%s{layoutName: %s, layoutDescription: %s, variantName: %s, variantDescription: %s, filename: %s}", getClass().getSimpleName(), layoutName, layoutDescription, variantName, variantDescription, filename);
    }
}
