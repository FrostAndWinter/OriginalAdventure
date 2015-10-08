package swen.adventure.engine.rendering.shaders;

import swen.adventure.engine.rendering.maths.Vector3;

import java.nio.ByteBuffer;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 30/09/15.
 * A MaterialShader is a shader that can have material parameters given to it.
 * This comprises of defining a material and defining a scale for the textures on that material.
 */
public interface MaterialShader {
    void setMaterial(ByteBuffer materialData);
    void setTextureRepeat(Vector3 scale);
}
