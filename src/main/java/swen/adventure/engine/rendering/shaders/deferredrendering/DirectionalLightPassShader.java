package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.ShaderProgram;
import swen.adventure.engine.rendering.shaders.PerObjectMaterialShader;

import java.io.IOException;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 12/10/15.
 */
public class DirectionalLightPassShader extends LightPassShader {
    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "DirectionalLightPass", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "DirectionalLightPass", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DirectionalLightPassShader() {
        super(vertexShaderText(), fragmentShaderText());
    }
}
