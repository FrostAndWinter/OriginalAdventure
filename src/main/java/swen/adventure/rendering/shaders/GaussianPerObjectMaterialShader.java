package swen.adventure.rendering.shaders;

import swen.adventure.Utilities;
import swen.adventure.rendering.Material;
import swen.adventure.rendering.ShaderProgram;
import swen.adventure.rendering.maths.Matrix3;
import swen.adventure.rendering.maths.Matrix4;
import swen.adventure.scenegraph.Light;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 26/09/15.
 */
public class GaussianPerObjectMaterialShader extends ShaderProgram {
    private final int _modelToCameraMatrixUniformRef;
    private final int _cameraToClipMatrixUniformRef;
    private final int _normalModelToCameraMatrixUniformRef;

    private final int _maxIntensityUniformRef;
    private final int _gammaUniformRef;

    private static final int LightBlockIndex = 0;
    private static final int MaterialBlockIndex = 1;

    private final int _lightUniformBufferRef;
    private final int _materialUniformBufferRef;

    private static String vertexShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("VertexShaderMaterial", "vert"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String fragmentShaderText() {
        try {
            return Utilities.readFile(Utilities.pathForResource("GaussianMaterials", "frag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GaussianPerObjectMaterialShader() {
        super(vertexShaderText(), fragmentShaderText());

        //Retrieve the uniforms
        _modelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToCameraMatrixUniform");
        _cameraToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "cameraToClipMatrixUniform");
        _normalModelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "normalModelToCameraMatrixUniform");

        _maxIntensityUniformRef = glGetUniformLocation(this.glProgramRef(), "maxIntensity");
        _gammaUniformRef = glGetUniformLocation(this.glProgramRef(), "gamma");

        //Setup the uniform buffers
        int lightBlock = glGetUniformBlockIndex(this.glProgramRef(), "Light");
        int materialBlock = glGetUniformBlockIndex(this.glProgramRef(), "Material");

        glUniformBlockBinding(this.glProgramRef(), lightBlock, LightBlockIndex);
        glUniformBlockBinding(this.glProgramRef(), materialBlock, MaterialBlockIndex);

        _lightUniformBufferRef = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
        glBufferData(GL_UNIFORM_BUFFER, Light.BufferSizeInBytes, GL_DYNAMIC_DRAW);

        //Bind the static buffer
        glBindBufferRange(GL_UNIFORM_BUFFER, LightBlockIndex, _lightUniformBufferRef, 0, Light.BufferSizeInBytes);

        _materialUniformBufferRef = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, _materialUniformBufferRef);
        glBufferData(GL_UNIFORM_BUFFER, Material.BufferSizeInBytes, GL_DYNAMIC_DRAW);

        //Bind the static buffer
        glBindBufferRange(GL_UNIFORM_BUFFER, MaterialBlockIndex, _materialUniformBufferRef, 0, Material.BufferSizeInBytes);

        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setLightData(ByteBuffer lightData) {
        glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, Light.BufferSizeInBytes, lightData);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setMaterial(FloatBuffer materialData) {
        glBindBuffer(GL_UNIFORM_BUFFER, _materialUniformBufferRef);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, materialData);
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

    public void setMaxIntensity(float maxIntensity) {
        glUniform1f(_maxIntensityUniformRef, maxIntensity);
    }

    public void setGamma(float gamma) {
        glUniform1f(_gammaUniformRef, gamma);
    }
}