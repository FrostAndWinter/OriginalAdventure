package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 *
 * The GBuffer contains the different framebuffers used for deferred rendering.
 */
class GBuffer {

    private final int _frameBufferObject;
    private final int[] _glTextures = new int[TextureUnit.deferredShadingTextureUnits().size()];
    private final int _depthTexture;
    private final int _finalTexture;

    private final int _finalBufferAttachment;

    private static int gBufferFormatForTextureUnit(TextureUnit textureUnit) {
        switch (textureUnit) {
            case DiffuseColourUnit:
                return GL_RGB8;
            case SpecularColourUnit:
                return GL_RGBA8_SNORM;
            case VertexNormalUnit:
                return GL_R11F_G11F_B10F; //This is a positive-only format, so we need to modify the values in the shader.
        }

        throw new RuntimeException(textureUnit + " is not supported as a GBuffer format.");
    }

    public GBuffer(int pixelWidth, int pixelHeight) {

        // Create the FBO
        _frameBufferObject = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, _frameBufferObject);

        IntBuffer textureBuffer = BufferUtils.createIntBuffer(_glTextures.length);
        // Create the gbuffer textures
        glGenTextures(textureBuffer);
        textureBuffer.get(_glTextures);

        _depthTexture = glGenTextures();
        _finalTexture = glGenTextures();

        int i = 0;
        for (TextureUnit textureUnit : TextureUnit.deferredShadingTextureUnits()) {

            glBindTexture(GL_TEXTURE_2D, _glTextures[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GBuffer.gBufferFormatForTextureUnit(textureUnit), pixelWidth, pixelHeight, 0, GL_RGBA, GL_FLOAT, (ByteBuffer)null);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, _glTextures[i], 0);

            i++;
        }

        // depth
        glBindTexture(GL_TEXTURE_2D, _depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH32F_STENCIL8, pixelWidth, pixelHeight, 0, GL_DEPTH_STENCIL, GL_FLOAT_32_UNSIGNED_INT_24_8_REV, (ByteBuffer)null);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, _depthTexture, 0);

        // final
        glBindTexture(GL_TEXTURE_2D, _finalTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, pixelWidth, pixelHeight, 0, GL_RGBA, GL_FLOAT, (ByteBuffer)null);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, _finalTexture, 0);

        _finalBufferAttachment = GL_COLOR_ATTACHMENT0 + i;

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.err.printf("FB error, status: 0x%x\n", status);
        }

        // restore default FBO
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    public void startFrame() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, _frameBufferObject);
        glDrawBuffer(_finalBufferAttachment);
        glClear(GL_COLOR_BUFFER_BIT);
    }


    public void bindForGeometryPass() {

        int[] drawBuffers = { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, _finalBufferAttachment }; //Normals, diffuse, specular, ambient.
        IntBuffer drawBuffersBuffer = BufferUtils.createIntBuffer(drawBuffers.length);
        drawBuffersBuffer.put(drawBuffers);
        drawBuffersBuffer.flip();
        glDrawBuffers(drawBuffersBuffer);
    }


    public void bindForStencilPass() {
        // must disable the draw buffers
        glDrawBuffer(GL_NONE);
    }

    public void bindForLightPass()
    {
        glDrawBuffer(_finalBufferAttachment);

        int i = 0;
        for (TextureUnit textureUnit : TextureUnit.deferredShadingTextureUnits()) {
            glActiveTexture(GL_TEXTURE0 + textureUnit.glUnit);
            glBindTexture(GL_TEXTURE_2D, _glTextures[i]);
            i++;
        }

        glActiveTexture(GL_TEXTURE0 + TextureUnit.DepthTextureUnit.glUnit);
        glBindTexture(GL_TEXTURE_2D, _depthTexture);
    }

    public void bindForFinalPass() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, _frameBufferObject);
        glReadBuffer(_finalBufferAttachment);
    }

    public void destroy() {
        if (_frameBufferObject != 0) {
            glDeleteFramebuffers(_frameBufferObject);
        }

        if (_glTextures[0] != 0) {
            IntBuffer textureBuffer = BufferUtils.createIntBuffer(_glTextures.length);
            textureBuffer.put(_glTextures);
            textureBuffer.flip();
            glDeleteTextures(textureBuffer);
        }

        if (_depthTexture != 0) {
            glDeleteTextures(_depthTexture);
        }

        if (_finalTexture != 0) {
            glDeleteTextures(_finalTexture);
        }
    }
}
