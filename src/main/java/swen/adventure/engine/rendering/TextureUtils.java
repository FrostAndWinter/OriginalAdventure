package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 */
public class TextureUtils {

    private static double getPixel(ByteBuffer pixels, boolean wrap, int x, int y, int width, int height, int componentsPerPixel) {

        if (x < 0) x = wrap ? (x + width) : 0;
        if (y < 0) y = wrap ? (y + height) : 0;
        if (x >= width) x = wrap ? (x - width) : (width - 1);
        if (y >= height) y = wrap ? (y - height) : (height - 1);
        int idx = x + y * width;
        return (pixels.get(idx * componentsPerPixel + 0) + pixels.get(idx * componentsPerPixel + 1) + pixels.get(idx * componentsPerPixel + 2)) / (256.0 * 3.0);
    }

    /**
     * Generates a normal map from a height map.
     * Adapted from http://gamedev.stackexchange.com/questions/80940/how-to-create-normal-map-from-bump-map-in-runtime#81934
     * @param heightMap The height-map image.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A RGB(A) format image containing the new normal map.
     */
    public static ByteBuffer generateNormalMap(ByteBuffer heightMap, int width, int height, int componentsPerPixel, double extrusion, boolean wrap) {
        int x,y;

        ByteBuffer outputImage = BufferUtils.createByteBuffer(heightMap.limit());

        for(y = 0; y < height; y++) {
            for(x = 0; x < width; x++) {
                double up = getPixel(heightMap, wrap, x, y - 1, width, height, componentsPerPixel);
                double down = getPixel(heightMap, wrap, x, y + 1, width, height, componentsPerPixel);
                double left = getPixel(heightMap, wrap, x - 1, y, width, height, componentsPerPixel);
                double right = getPixel(heightMap, wrap, x + 1, y, width, height, componentsPerPixel);
                double upleft = getPixel(heightMap, wrap, x - 1, y - 1, width, height, componentsPerPixel);
                double upright = getPixel(heightMap, wrap, x + 1, y - 1, width, height, componentsPerPixel);
                double downleft = getPixel(heightMap, wrap, x - 1, y + 1, width, height, componentsPerPixel);
                double downright = getPixel(heightMap, wrap, x + 1, y + 1, width, height, componentsPerPixel);

                double vert = (down - up) * 2.0 + downright + downleft - upright - upleft;
                double horiz = (right - left) * 2.0 + upright + downright - upleft - downleft;
                double depth = 1.0 / extrusion;
                double scale = 127.0 / Math.sqrt(vert * vert + horiz * horiz + depth*depth);

                byte r = (byte)(128 - horiz * scale);
                byte g = (byte)(128 + vert * scale);
                byte b = (byte)(128 + depth * scale);

                int idx = x + y * width;
                outputImage.put(idx * componentsPerPixel + 0, r);
                outputImage.put(idx * componentsPerPixel + 1, g);
                outputImage.put(idx * componentsPerPixel + 2, b);

                if (componentsPerPixel == 4) { outputImage.put(idx * 4 + 3, (byte)255); };
            }
        }

        outputImage.position(heightMap.limit());
        outputImage.flip();
        return outputImage;
    }
}
