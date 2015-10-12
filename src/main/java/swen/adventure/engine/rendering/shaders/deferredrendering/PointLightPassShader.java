package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.TextureUnit;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 12/10/15.
 */
public class PointLightPassShader extends LightPassShader {


    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "PointLightPass", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "PointLightPass", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PointLightPassShader() {
        super(vertexShaderText(), fragmentShaderText());
    }
}
