package swen.adventure.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import swen.adventure.Utilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL21.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 29/09/15.
 */
public class Texture {

    private static Map<String, Texture> _textureCache = new HashMap<>();

    public final ByteBuffer textureData;
    public final int width;
    public final int height;
    public final int numPixelComponents;

    public final int glTextureRef;

    public Texture(final ByteBuffer textureData, final int width, final int height, final int numPixelComponents) {
        this.textureData = textureData;
        this.width = width;
        this.height = height;
        this.numPixelComponents = numPixelComponents;
        this.glTextureRef = this.initOpenGL();
    }

    /**
     * @return The GL Texture reference for this texture.
     */
    private int initOpenGL() {
        int textureRef = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureRef);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8, this.width, this.height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, this.textureData);

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureRef;
    }

    public static Texture loadTextureWithName(String fileName) {

        Texture texture = _textureCache.get(fileName);

        if (texture == null) {

            String resourcePath = Utilities.pathForResource(fileName, null);

            IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer numPixelComponentsBuffer = BufferUtils.createIntBuffer(1);

            ByteBuffer image = STBImage.stbi_load(resourcePath, widthBuffer, heightBuffer, numPixelComponentsBuffer, 0);

            if (image == null) {
                throw new RuntimeException("Error loading image with name " + fileName);
            }
            texture = new Texture(image, widthBuffer.get(), heightBuffer.get(), numPixelComponentsBuffer.get());
            _textureCache.put(fileName, texture);
        }

        return texture;
    }
}
