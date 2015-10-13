package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.Utilities;
import swen.adventure.engine.rendering.TextureUnit;
import swen.adventure.engine.rendering.maths.Matrix4;
import swen.adventure.engine.rendering.shaders.PerObjectMaterialShader;

import java.io.IOException;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 *
 * A LightPassShader is used within light passes in deferred shading.
 */
public class LightPassShader extends PerObjectMaterialShader {

    private final int _screenSizeUniformRef;
    private final int _halfSizeNearPlaneUniformRef;
    private final int _modelToClipMatrixUniformRef;

    public LightPassShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);

        _screenSizeUniformRef = glGetUniformLocation(this.glProgramRef(), "screenSizeUniform");
        _halfSizeNearPlaneUniformRef = glGetUniformLocation(this.glProgramRef(), "halfSizeNearPlane");
        _modelToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToClipMatrixUniform");

        final int positionsSamplerRef = glGetUniformLocation(this.glProgramRef(), "cameraSpacePositionSampler");
        final int vertexNormalsSamplerRef = glGetUniformLocation(this.glProgramRef(), "cameraSpaceNormalSampler");
        final int diffuseColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "diffuseColourSampler");
        final int specularColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "specularColourSampler");
        final int depthSamplerRef = glGetUniformLocation(this.glProgramRef(), "depthSampler");

        this.useProgram();
        glUniform1i(positionsSamplerRef, TextureUnit.PositionUnit.glUnit);
        glUniform1i(diffuseColourSamplerRef, TextureUnit.DiffuseColourUnit.glUnit);
        glUniform1i(vertexNormalsSamplerRef, TextureUnit.VertexNormalUnit.glUnit);
        glUniform1i(specularColourSamplerRef, TextureUnit.SpecularColourUnit.glUnit);
        glUniform1i(depthSamplerRef, TextureUnit.DepthTextureUnit.glUnit);
        this.endUseProgram();
    }

    public void setScreenSize(float width, float height) {
        glUniform2f(_screenSizeUniformRef, width, height);
    }

    public void setHalfSizeNearPlane(float height, float aspect, float fieldOfViewY) {
        float x = height * aspect;
        float y = (float)Math.tan(fieldOfViewY / 2.f);
        glUniform2f(_halfSizeNearPlaneUniformRef, x, y);
    }

    public void setModelToClipMatrix(Matrix4 matrix) {
        glUniformMatrix4fv(_modelToClipMatrixUniformRef, false, matrix.toFloatBuffer());
    }
}
