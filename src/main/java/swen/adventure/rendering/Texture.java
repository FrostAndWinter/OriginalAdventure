package swen.adventure.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import swen.adventure.Utilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 29/09/15.
 */
public class Texture {

    private static Map<String, Texture> _textureCache = new HashMap<>();

    public final ByteBuffer textureData;
    public final int width;
    public final int height;
    public final int numPixelComponents;

    public Texture(final ByteBuffer textureData, final int width, final int height, final int numPixelComponents) {
        this.textureData = textureData;
        this.width = width;
        this.height = height;
        this.numPixelComponents = numPixelComponents;
    }

    public static Texture loadTextureWithName(String fileName) {
        String resourcePath = Utilities.pathForResource(fileName, null);

        IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer numPixelComponentsBuffer = BufferUtils.createIntBuffer(1);

        ByteBuffer image = STBImage.stbi_load(resourcePath, widthBuffer, heightBuffer, numPixelComponentsBuffer, 0);

        if (image == null) {
            throw new RuntimeException("Error loading image with name " + fileName);
        }
        return new Texture(image, widthBuffer.get(), heightBuffer.get(), numPixelComponentsBuffer.get());
    }
}
