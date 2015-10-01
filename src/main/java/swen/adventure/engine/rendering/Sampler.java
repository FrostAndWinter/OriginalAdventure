package swen.adventure.engine.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 30/09/15.
 * Sampler is a wrapper around an OpenGL sampler object.
 */
public class Sampler {

    public final int glSamplerRef;
    public final TextureUnit textureUnit;

    public Sampler(TextureUnit textureUnit) {
        this.glSamplerRef = glGenSamplers();
        this.textureUnit = textureUnit;

        glBindSampler(textureUnit.glUnit, this.glSamplerRef);

        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void bindToTextureUnit() {
        glBindSampler(this.textureUnit.glUnit, this.glSamplerRef);
    }

    public void unbindSampler() {
        glBindSampler(this.textureUnit.glUnit, 0);
    }
}
