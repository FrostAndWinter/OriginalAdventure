/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 package swen.adventure.engine.rendering.shaders.deferredrendering;

import swen.adventure.engine.rendering.TextureUnit;
import swen.adventure.engine.rendering.shaders.PerObjectMaterialShader;

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
    private final int _depthRangeUniformRef;

    public LightPassShader(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);

        _screenSizeUniformRef = glGetUniformLocation(this.glProgramRef(), "screenSizeUniform");
        _halfSizeNearPlaneUniformRef = glGetUniformLocation(this.glProgramRef(), "halfSizeNearPlaneUniform");
        _modelToClipMatrixUniformRef = glGetUniformLocation(this.glProgramRef(), "modelToClipMatrixUniform");
        _depthRangeUniformRef = glGetUniformLocation(this.glProgramRef(), "depthRangeUniform");

        final int vertexNormalsSamplerRef = glGetUniformLocation(this.glProgramRef(), "cameraSpaceNormalSampler");
        final int diffuseColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "diffuseColourSampler");
        final int specularColourSamplerRef = glGetUniformLocation(this.glProgramRef(), "specularColourSampler");
        final int depthSamplerRef = glGetUniformLocation(this.glProgramRef(), "depthSampler");

        this.useProgram();
        glUniform1i(diffuseColourSamplerRef, TextureUnit.DiffuseColourUnit.glUnit);
        glUniform1i(vertexNormalsSamplerRef, TextureUnit.VertexNormalUnit.glUnit);
        glUniform1i(specularColourSamplerRef, TextureUnit.SpecularColourUnit.glUnit);
        glUniform1i(depthSamplerRef, TextureUnit.DepthTextureUnit.glUnit);
        this.endUseProgram();
    }

    public void setScreenSize(float width, float height) {
        glUniform2f(_screenSizeUniformRef, width, height);
    }

    public void setHalfSizeNearPlane(float zNear, float aspect, float tanHalfFOV) {
        float y = tanHalfFOV * zNear;
        float x = y * aspect;
        glUniform2f(_halfSizeNearPlaneUniformRef, x, y);
    }

    public void setDepthRange(float zNear, float zFar) {
        glUniform2f(_depthRangeUniformRef, zNear, zFar);
    }
}