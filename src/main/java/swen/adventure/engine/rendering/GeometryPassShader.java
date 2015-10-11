package swen.adventure.engine.rendering;

import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.rendering.shaders.MaterialShader;

import java.nio.ByteBuffer;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 */
public class GeometryPassShader extends ShaderProgram implements MaterialShader {
    public GeometryPassShader(final String vertexShaderText, final String fragmentShaderText) {
        super(vertexShaderText, fragmentShaderText);
    }

    @Override
    public void setMaterial(final ByteBuffer materialData) {

    }

    @Override
    public void setTextureRepeat(final Vector3 scale) {

    }
}
