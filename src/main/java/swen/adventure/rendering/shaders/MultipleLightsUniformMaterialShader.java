package swen.adventure.rendering.shaders;

import swen.adventure.Utilities;
import swen.adventure.rendering.ShaderProgram;
import swen.adventure.rendering.maths.Matrix3;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.rendering.maths.Vector4;
import swen.adventure.scenegraph.Light;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 26/09/15.
 */
public class MultipleLightsUniformMaterialShader extends ShaderProgram {
    private final int _modelToCameraMatrixUniformRef;
    private final int _cameraToClipMatrixUniformRef;
    private final int _normalModelToCameraMatrixUniformRef;
    private final int _colourUniformRef;

    private final int _maxIntensityUniformRef;
    private final int _specularityUniformRef;

    private static final int LightBlockIndex = 0;

    private final int _lightUniformBufferRef;

    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("VertexShaderConstSpecular", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("DiffuseSpecularMultipleLights", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MultipleLightsUniformMaterialShader() {
        super(vertexShaderText(), fragmentShaderText());

        //Retrieve the uniforms
        _modelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToCameraMatrixUniform");
        _colourUniformRef = glGetUniformLocation(this.glProgramRef(), "colour");
        _cameraToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "cameraToClipMatrixUniform");
        _normalModelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "normalModelToCameraMatrixUniform");

        _maxIntensityUniformRef = glGetUniformLocation(this.glProgramRef(), "maxIntensity");
        _specularityUniformRef = glGetUniformLocation(this.glProgramRef(), "specularity");

        //Setup the uniform buffer
        int lightBlock = glGetUniformBlockIndex(this.glProgramRef(), "Light");

        glUniformBlockBinding(this.glProgramRef(), lightBlock, LightBlockIndex);

        _lightUniformBufferRef = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
        glBufferData(GL_UNIFORM_BUFFER, Light.BufferSizeInBytes, GL_DYNAMIC_DRAW);

        //Bind the static buffer
        glBindBufferRange(GL_UNIFORM_BUFFER, LightBlockIndex, _lightUniformBufferRef, 0, Light.BufferSizeInBytes);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setLightData(ByteBuffer lightData) {
        glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, Light.BufferSizeInBytes, lightData);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setModelToCameraMatrix(Matrix4 matrix) {
        glUniformMatrix4fv(_modelToCameraMatrixUniformRef, false, matrix.toFloatBuffer());
    }

    public void setCameraToClipMatrix(Matrix4 matrix) {
        glUniformMatrix4fv(_cameraToClipMatrixUniformRef, false, matrix.toFloatBuffer());
    }

    public void setNormalModelToCameraMatrix(Matrix3 matrix) {
        glUniformMatrix3fv(_normalModelToCameraMatrixUniformRef, false, matrix.toFloatBuffer());
    }

    public void setColour(Vector4 colour) {
        glUniform4fv(_colourUniformRef, colour.toFloatBuffer());
    }

    public void setMaxIntensity(float maxIntensity) {
        glUniform1f(_maxIntensityUniformRef, maxIntensity);
    }

    public void setSpecularity(float specularity) {
        glUniform1f(_specularityUniformRef, specularity);
    }
}
