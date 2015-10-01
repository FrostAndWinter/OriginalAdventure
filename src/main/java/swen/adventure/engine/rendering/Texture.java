package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import swen.adventure.engine.Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL21.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 29/09/15.
 */
public class Texture {

    private static Map<String, Texture> _textureCache = new HashMap<>();

    public final ByteBuffer textureData;
    private final List<ByteBuffer> _mipMappedData = new ArrayList<>();
    public final int width;
    public final int height;
    public final int numPixelComponents;

    public final int glTextureRef;

    public Texture(final ByteBuffer textureData, final int width, final int height, final int numPixelComponents) {
        this.textureData = textureData;
        this.width = width;
        this.height = height;
        this.numPixelComponents = numPixelComponents;

        int bytesPerPixel = textureData.limit()/(width * height);
        for (int w = width, h = height; w > 1 || h > 1; ) {
            w /= 2; h /= 2;

            ByteBuffer outputBuffer = BufferUtils.createByteBuffer(w * h * bytesPerPixel);
            int success = STBImageResize.stbir_resize_uint8_srgb(textureData , width , height , 0,
                    outputBuffer, w, h, 0,
                    numPixelComponents, numPixelComponents == 4 ? 3 : 0, 0);

            if (success == 0) {
                System.err.printf("Error generating mip-map with dimensions %d, %d.\n", w, h);
            }
            _mipMappedData.add(outputBuffer);
        }

        this.glTextureRef = this.initOpenGL();
    }

    /**
     * @return The GL Texture reference for this texture.
     */
    private int initOpenGL() {
        int textureRef = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureRef);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, this.textureData);

        int mipmapLevel = 0;
        for(; mipmapLevel < _mipMappedData.size(); mipmapLevel++) {

            glTexImage2D(GL_TEXTURE_2D, mipmapLevel + 1, GL_SRGB8_ALPHA8, this.width / (2 << mipmapLevel), this.height / (2 << mipmapLevel), 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, _mipMappedData.get(mipmapLevel));
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmapLevel);

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

                ByteBuffer image = STBImage.stbi_load_from_memory(encodedImageDataBuffer, widthBuffer, heightBuffer, numPixelComponentsBuffer, 4);

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
