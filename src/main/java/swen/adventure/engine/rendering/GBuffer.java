package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 11/10/15.
 */
public class GBuffer {

    enum TextureType {
        Position(0),
        Diffuse(1),
        Normal(2),
        TextureCoordinate(3);

        public final int glIndex;

        TextureType(final int glIndex) {
            this.glIndex = glIndex;
        }
    }

    private final int _frameBufferObject;
    private final int[] _glTextures = new int[TextureType.values().length];
    private final int _depthTexture;

    public GBuffer(int width, int height) {

        //this.setSize(width, height);

        // Create the FBO
        _frameBufferObject = glGenFramebuffers();
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, _frameBufferObject);

        IntBuffer textureBuffer = BufferUtils.createIntBuffer(_glTextures.length);
        // Create the gbuffer textures
        glGenTextures(textureBuffer);
        textureBuffer.get(_glTextures);
        _depthTexture = glGenTextures();

        for (int i = 0 ; i < _glTextures.length ; i++) {
            glBindTexture(GL_TEXTURE_2D, _glTextures[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_RGB, GL_FLOAT, 0);
            glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, _glTextures[i], 0);
        }

        // depth
        glBindTexture(GL_TEXTURE_2D, _depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT,
                0);
        glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, _depthTexture, 0);

        int[] drawBuffers = { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3 };
        IntBuffer drawBuffersBuffer = BufferUtils.createIntBuffer(drawBuffers.length);
        drawBuffersBuffer.put(drawBuffers);
        drawBuffersBuffer.flip();
        glDrawBuffers(drawBuffersBuffer);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("FB error, status: 0x%x\n", Status);
        }

        // restore default FBO
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    public void bindForWriting() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, _frameBufferObject);
    }

    public void bindForReading() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, _frameBufferObject);
    }

    void setReadBuffer(TextureType textureType)
    {
        glReadBuffer(GL_COLOR_ATTACHMENT0 + textureType.glIndex);
    }
}
