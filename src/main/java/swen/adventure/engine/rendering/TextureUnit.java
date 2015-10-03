package swen.adventure.engine.rendering;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 30/09/15.
 */
public enum TextureUnit {
    AmbientColourUnit(0),
    DiffuseColourUnit(1),
    SpecularColourUnit(2),
    SpecularityUnit(3),
    NormalMapUnit(4);

    public final int glUnit;

    TextureUnit(int glUnit) {
        this.glUnit = glUnit;
    }
}