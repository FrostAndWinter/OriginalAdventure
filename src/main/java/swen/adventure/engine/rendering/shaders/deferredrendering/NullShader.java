/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.shaders.PositionShader;

import java.io.IOException;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 12/10/15.
 *
 * NullShader transforms the input vertices and outputs nothing.
 */
public class NullShader extends PositionShader {

    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("deferred-shading", "PointLightPass", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String fragmentShaderText = "#version 330\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "}";

    public NullShader() {
        super(vertexShaderText(), fragmentShaderText);
    }
}