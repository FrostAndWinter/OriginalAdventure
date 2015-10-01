package swen.adventure.engine.rendering.shaders;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.ShaderProgram;
import swen.adventure.engine.rendering.maths.Matrix4;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 1/10/15.
 */
public class PickerShader extends ShaderProgram {
    private final int _modelToClipMatrixUniformRef;
    private final int _colourUniformRef;

    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("PickerShader", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("PickerShader", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PickerShader() {
        super(PickerShader.vertexShaderText(), PickerShader.fragmentShaderText());

        _modelToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToClipMatrixUniform");
        _colourUniformRef = glGetUniformLocation(this.glProgramRef(), "colourUniform");
    }

    public void setModelToClipMatrix(Matrix4 modelToClipMatrix) {
        glUniformMatrix4fv(_modelToClipMatrixUniformRef, false, modelToClipMatrix.toFloatBuffer());
    }

    public void setID(int id) {
        //First  bits go into r
        //Second 11 bits go into g
        //Third 10 bits go into b

        int r = (id >>> 16) & 0xFF;
        int g = (id >>> 8) & 0xFF;
        int b = id & 0xFF;

        glUniform3f(_colourUniformRef, r / 255.f, g / 255.f, b / 255.f);
    }

    public static int colourToID(byte r, byte g, byte b) {

        return ((r << 16) | (g << 8) | b) & 0xFFFFFF;
    }
}
