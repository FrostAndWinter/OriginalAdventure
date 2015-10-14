package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import swen.adventure.Settings;
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
 *
 * A Texture is a multi-dimensional array of pixel data that is usually used to represent images.
 * This class is intended to make using image files with OpenGL as simple as binding and unbinding the texture from a texture unit.
 */
public final class Texture {

    private static Map<String, Texture> _textureCache = new HashMap<>();
    private static Map<String, Texture> _normalsCache = new HashMap<>();

    public final ByteBuffer textureData;
    private final List<ByteBuffer> _mipMappedData = new ArrayList<>();

    public final int width;
    public final int height;
    public final int numPixelComponents;
    public final boolean useSRGB;

    private final int glTextureRef;

    private Texture(final ByteBuffer textureData, final int width, final int height, final int numPixelComponents, boolean useSRGB) {
        this.textureData = textureData;
        this.width = width;
        this.height = height;
        this.numPixelComponents = numPixelComponents;

        this.useSRGB = useSRGB;

        int bytesPerPixel = textureData.limit()/(width * height);
        for (int w = width, h = height; w > 1 || h > 1; ) { //Generate mip-maps using STBImageResize.
            w /= 2; h /= 2;

            ByteBuffer outputBuffer = BufferUtils.createByteBuffer(w * h * bytesPerPixel);
            int success = STBImageResize.stbir_resize_uint8_generic(textureData, width, height, 0,
                    outputBuffer, w, h, 0,
                    numPixelComponents,
                    this.numPixelComponents == 4 ? 3 : STBImageResize.STBIR_ALPHA_CHANNEL_NONE, 0,
                    STBImageResize.STBIR_EDGE_CLAMP,
                    STBImageResize.STBIR_FILTER_DEFAULT,
                    useSRGB ? STBImageResize.STBIR_COLORSPACE_SRGB : STBImageResize.STBIR_COLORSPACE_LINEAR);

            if (success == 0) {
                System.err.printf("Error generating mip-map with dimensions %d, %d.\n", w, h);
            }

            _mipMappedData.add(outputBuffer);
        }

        this.glTextureRef = Utilities.isHeadlessMode ? 0 : this.initOpenGL();
    }

    /**
     * @return The GL Texture reference for this texture.
     */
    private int initOpenGL() {
        int textureRef = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureRef);

        glTexImage2D(GL_TEXTURE_2D, 0, this.internalFormat(), this.width, this.height, 0, this.textureFormat(), GL_UNSIGNED_BYTE, this.textureData);

        int mipmapLevel = 0;
        for(; mipmapLevel < _mipMappedData.size(); mipmapLevel++) {

            glTexImage2D(GL_TEXTURE_2D, mipmapLevel + 1, this.internalFormat(), this.width / (2 << mipmapLevel), this.height / (2 << mipmapLevel), 0,
                    this.textureFormat(), GL_UNSIGNED_BYTE, _mipMappedData.get(mipmapLevel));
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipmapLevel);

        glBindTexture(GL_TEXTURE_2D, 0);

        return textureRef;
    }

    private int textureFormat() {
        return this.numPixelComponents == 3 ? GL_RGB : GL_RGBA;
    }

    private int internalFormat() {
        if (this.useSRGB) {
            return this.numPixelComponents == 3 ? GL_SRGB8 : GL_SRGB8_ALPHA8;
        } else {
            return this.numPixelComponents == 3 ? GL_RGB8 : GL_RGBA8;
        }
    }

    /**
     * Binds the texture to a given texture unit.
     * @param textureUnit The texture unit to bind to.
     */
    public void bindToTextureUnit(TextureUnit textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit.glUnit);
        glBindTexture(GL_TEXTURE_2D, this.glTextureRef);
    }

    /**
     * Unbinds the currently bound texture from the specified texture unit.
     * @param textureUnit The texture unit to unbind the texture from.
     */
    public static void unbindTexture(TextureUnit textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit.glUnit);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static ByteBuffer loadImageWithName(String directory, String fileName, IntBuffer widthBuffer, IntBuffer heightBuffer, IntBuffer numPixelComponentsBuffer) {
        String resourcePath = Utilities.pathForResource(directory, fileName, null);

        try {
            byte[] encodedImageData = Files.readAllBytes(new File(resourcePath).toPath()); //Read using Java APIs to work around STBImage limitations with Unicode paths on Windows.
            ByteBuffer encodedImageDataBuffer = BufferUtils.createByteBuffer(encodedImageData.length);
            encodedImageDataBuffer.put(encodedImageData);
            encodedImageDataBuffer.flip();

            STBImage.stbi_set_flip_vertically_on_load(1);

            ByteBuffer image = STBImage.stbi_load_from_memory(encodedImageDataBuffer, widthBuffer, heightBuffer, numPixelComponentsBuffer, Settings.IntelGraphicsWorkaround ? 4 : 0);

            if (image == null) {
                throw new RuntimeException("Error loading image with name " + fileName + ": " + STBImage.stbi_failure_reason());
            }
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Error loading image with name " + fileName + ": " + e);
        }
    }

    /**
     * Loads a height map from an image file at the specified location.
     * It then converts it to a normal map and returns the resulting texture.
     * @param directory The directory to look for the image file in.
     * @param fileName The name of the image file.
     * @return A normal map texture, consisting of the base image and mip-mapped images.
     */
    public static Texture loadHeightMapWithName(String directory, String fileName) {

        Texture texture = _normalsCache.get(directory + fileName);
        if (texture == null) {

            IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer numPixelComponentsBuffer = BufferUtils.createIntBuffer(1);
            ByteBuffer heightMap = Texture.loadImageWithName(directory, fileName, widthBuffer, heightBuffer, numPixelComponentsBuffer);

            ByteBuffer normalMap = TextureUtils.generateNormalMap(heightMap, widthBuffer.get(0), heightBuffer.get(0), numPixelComponentsBuffer.get(0), 2.0, false);

            texture = new Texture(normalMap, widthBuffer.get(), heightBuffer.get(), numPixelComponentsBuffer.get(), false);

            _normalsCache.put(directory + fileName, texture);

        }

        return texture;
    }

    /**
     * Loads an image from the file at the specified location.
     * @param directory The directory to look for the image file in.
     * @param fileName The name of the image file.
     * @param useSRGB Whether or not the image is in the sRGB colour space.
     * @return A texture, consisting of the base image and mip-mapped images.
     */
    public static Texture loadTextureWithName(String directory, String fileName, boolean useSRGB) {

        Texture texture = _textureCache.get(directory + fileName);

        if (texture == null) {
            IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
            IntBuffer numPixelComponentsBuffer = BufferUtils.createIntBuffer(1);
            ByteBuffer image = Texture.loadImageWithName(directory, fileName, widthBuffer, heightBuffer, numPixelComponentsBuffer);

                texture = new Texture(image, widthBuffer.get(), heightBuffer.get(), numPixelComponentsBuffer.get(), useSRGB);
                _textureCache.put(directory + fileName, texture);
        }

        return texture;
    }
}
