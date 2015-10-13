package swen.adventure.engine.rendering;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 30/09/15.
 *
 * A texture unit is one of the possible units that a texture and sampler can be bound to in an OpenGL context.
 * This enum makes things a little nicer than passing integer constants around.
 */
public enum TextureUnit {
    AmbientColourUnit(0),
    DiffuseColourUnit(1),
    SpecularColourUnit(2),
    SpecularityUnit(3),
    NormalMapUnit(4),

    //Units for deferred shading.
    PositionUnit(5),
    VertexNormalUnit(6),
    FinalUnit(7);

    public final int glUnit;

    TextureUnit(int glUnit) {
        this.glUnit = glUnit;
    }

    public static List<TextureUnit> deferredShadingTextureUnits() {
        return Arrays.asList(PositionUnit, VertexNormalUnit, DiffuseColourUnit, SpecularColourUnit);
    }
}