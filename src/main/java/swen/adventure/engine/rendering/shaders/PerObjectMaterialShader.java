package swen.adventure.engine.rendering.shaders;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.ShaderProgram;
import swen.adventure.engine.rendering.TextureUnit;
import swen.adventure.engine.rendering.maths.Matrix3;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.Light;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 26/09/15.
 *
 * PerObjectMaterialShader describes a shader that implements materials (set via a uniform),
 * gaussian specular shading, diffuse shading, and textures for specularity, specular colour,
 * diffuse colour, and ambient colour.
 * It also provides functionality to repeat a texture in the U or V direction.
 * Up to 32 light sources are supported.
 */
public class PerObjectMaterialShader extends ShaderProgram implements MaterialShader {
    private final int _modelToCameraMatrixUniformRef;
    private final int _cameraToClipMatrixUniformRef;
    private final int _normalModelToCameraMatrixUniformRef;

    private int _lightUniformBufferRef;
    private int _materialUniformBufferRef;
    private final int _textureRepeatUniformBufferRef;

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

    public PerObjectMaterialShader(String vertexShaderText, String fragmentShaderText) {
        super(vertexShaderText, fragmentShaderText);
        //Retrieve the uniforms
        _modelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToCameraMatrixUniform");
        _cameraToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "cameraToClipMatrixUniform");
        _normalModelToCameraMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "normalModelToCameraMatrixUniform");
        _textureRepeatUniformBufferRef = glGetUniformLocation(this.glProgramRef(), "textureRepeatUniform");

        final int ambientColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "ambientColourSampler");
        final int diffuseColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "diffuseColourSampler");
        final int specularColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "specularColourSampler");
        final int specularitySamplerRef = glGetUniformLocation(this.glProgramRef(), "specularitySampler");

        //Bind the sampler references to texture unit indices
        this.useProgram();
        glUniform1i(ambientColourSamplerRef, TextureUnit.AmbientColourUnit.glUnit);
        glUniform1i(diffuseColourSamplerRef, TextureUnit.DiffuseColourUnit.glUnit);
        glUniform1i(specularColourSamplerRef, TextureUnit.SpecularColourUnit.glUnit);
        glUniform1i(specularitySamplerRef, TextureUnit.SpecularityUnit.glUnit);
        this.endUseProgram();

        //Setup the uniform buffers
        int lightBlock = glGetUniformBlockIndex(this.glProgramRef(), "Light");
        int materialBlock = glGetUniformBlockIndex(this.glProgramRef(), "Material");

        if (lightBlock != -1) {
            int lightBlockIndex = ShaderProgram.nextUniformBlockIndex();

            glUniformBlockBinding(this.glProgramRef(), lightBlock, lightBlockIndex);
            _lightUniformBufferRef = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
            glBufferData(GL_UNIFORM_BUFFER, Light.BufferSizeInBytes, GL_DYNAMIC_DRAW);

            //Bind the static buffer
            glBindBufferRange(GL_UNIFORM_BUFFER, lightBlockIndex, _lightUniformBufferRef, 0, Light.BufferSizeInBytes);
        }

        if (materialBlock != -1) {
            int materialBlockIndex = ShaderProgram.nextUniformBlockIndex();
            glUniformBlockBinding(this.glProgramRef(), materialBlock, materialBlockIndex);
            _materialUniformBufferRef = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, _materialUniformBufferRef);
            glBufferData(GL_UNIFORM_BUFFER, Material.BufferSizeInBytes, GL_DYNAMIC_DRAW);

            //Bind the static buffer
            glBindBufferRange(GL_UNIFORM_BUFFER, materialBlockIndex, _materialUniformBufferRef, 0, Material.BufferSizeInBytes);
        }

        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public PerObjectMaterialShader() {
        this(vertexShaderText(), fragmentShaderText());
    }

    public void setLightData(ByteBuffer lightData) {
        glBindBuffer(GL_UNIFORM_BUFFER, _lightUniformBufferRef);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, Light.BufferSizeInBytes, lightData);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setMaterial(ByteBuffer materialData) {
        glBindBuffer(GL_UNIFORM_BUFFER, _materialUniformBufferRef);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, materialData);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public void setTextureRepeat(Vector3 textureRepeat) {
        glUniform2f(_textureRepeatUniformBufferRef, textureRepeat.x, textureRepeat.y);
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

}
