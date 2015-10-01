package swen.adventure.engine.rendering.shaders;

import java.nio.ByteBuffer;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 30/09/15.
 */
public interface MaterialShader {
    void setMaterial(ByteBuffer materialData);
}
