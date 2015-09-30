package swen.adventure.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import swen.adventure.Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL33.*;

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

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, this.width, this.height, 0, GL_RGB, GL_UNSIGNED_BYTE, this.textureData);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0); //TODO generate mip-maps for the textures dynamically and bind them here.

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureRef;
    }

    public void bindToTextureUnit(TextureUnit textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit.glUnit);
        glBindTexture(GL_TEXTURE_2D, this.glTextureRef);
    }

    public static void unbindTexture(TextureUnit textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit.glUnit);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static Texture loadTextureWithName(String fileName) {

        Texture texture = _textureCache.get(fileName);

        if (texture == null) {

            String resourcePath = Utilities.pathForResource(fileName, null);

            IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer numPixelComponentsBuffer = BufferUtils.createIntBuffer(1);

            try {
                byte[] encodedImageData = Files.readAllBytes(new File(resourcePath).toPath()); //Read using Java APIs to work around STBImage limitations with Unicode paths on Windows.
                ByteBuffer encodedImageDataBuffer = BufferUtils.createByteBuffer(encodedImageData.length);
                encodedImageDataBuffer.put(encodedImageData);
                encodedImageDataBuffer.flip();

                STBImage.stbi_set_flip_vertically_on_load(1);

                ByteBuffer image = STBImage.stbi_load_from_memory(encodedImageDataBuffer, widthBuffer, heightBuffer, numPixelComponentsBuffer, 0);

                if (image == null) {
                    throw new RuntimeException("Error loading image with name " + fileName + ": " + STBImage.stbi_failure_reason());
                }

                texture = new Texture(image, widthBuffer.get(), heightBuffer.get(), numPixelComponentsBuffer.get());
                _textureCache.put(fileName, texture);

            } catch (IOException e) {
                throw new RuntimeException("Error loading image with name " + fileName + ": " + e);
            }
        }

        return texture;
    }
}
