/*
 * Copyright (C) 2015 Marcel Lehwald
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.minime89.passbeam.keyboard;

import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Layout {
    /**
     *
     */
    private final String id;

    /**
     *
     */
    private final String layoutName;

    /**
     *
     */
    private final String layoutDescription;

    /**
     *
     */
    private final String variantName;

    /**
     *
     */
    private final String variantDescription;

    /**
     * Constructor.
     */
    public Layout(@Element(name = "layoutName", required = false) String layoutName,
                  @Element(name = "layoutDescription", required = false) String layoutDescription,
                  @Element(name = "variantName", required = false) String variantName,
                  @Element(name = "variantDescription", required = false) String variantDescription) {
        this.layoutName = layoutName;
        this.layoutDescription = layoutDescription;
        this.variantName = variantName;
        this.variantDescription = variantDescription;
        this.id = buildId();
    }

    /**
     * Build a unique identifier from the layout name and variant name. This identifier needs to
     * match the filename of the keycodes file. The identifier will be used for finding the
     * file associated with that layout and will be build as following:
     * <ul>
     * <li>If only the layout name was provided, the identifier will match "&lt;layout&gt;"</li>
     * <li>If layout name and variant name was provided, the identifier will match "&lt;layout&gt;-&lt;variant&gt;"</li>
     * </ul>
     *
     * @return Returns the identifier.
     */
    private String buildId() {
        String id = "";
        if (layoutName != null && !layoutName.isEmpty()) {
            id = layoutName;

            if (variantName != null && !variantName.isEmpty()) {
                id = id + "-" + variantName;
            }
        }

        return id;
    }

    public String getId() {
        return id;
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

    public JSONObject dump() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("layoutName", layoutName);
        obj.put("layoutDescription", layoutDescription);
        obj.put("variantName", variantName);
        obj.put("variantDescription", variantDescription);

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
