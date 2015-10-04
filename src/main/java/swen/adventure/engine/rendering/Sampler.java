package swen.adventure.engine.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
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

        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glSamplerParameteri(this.glSamplerRef, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glSamplerParameterf(this.glSamplerRef, GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f);
    }

    public void bindToTextureUnit() {
        glBindSampler(this.textureUnit.glUnit, this.glSamplerRef);
    }

    public void unbindSampler() {
        glBindSampler(this.textureUnit.glUnit, 0);
    }
}
