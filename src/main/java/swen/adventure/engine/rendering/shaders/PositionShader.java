/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package swen.adventure.engine.rendering.shaders;

import swen.adventure.engine.rendering.ShaderProgram;
import swen.adventure.engine.rendering.maths.Matrix4;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 12/10/15.
 */
public class PositionShader extends ShaderProgram {

    private final int _modelToCameraMatrixUniformRef;
    private final int _cameraToClipMatrixUniformRef;

    public PositionShader(final String vertexShaderText, final String fragmentShaderText) {
        super(vertexShaderText, fragmentShaderText);

        _modelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToCameraMatrixUniform");
        _cameraToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "cameraToClipMatrixUniform");
    }

    public void setModelToCameraMatrix(Matrix4 matrix) {
        glUniformMatrix4fv(_modelToCameraMatrixUniformRef, false, matrix.toFloatBuffer());
    }

    public void setCameraToClipMatrix(Matrix4 matrix) {
        glUniformMatrix4fv(_cameraToClipMatrixUniformRef, false, matrix.toFloatBuffer());
    }

}