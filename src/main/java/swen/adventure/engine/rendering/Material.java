package swen.adventure.engine.rendering;

import org.lwjgl.BufferUtils;
import swen.adventure.engine.rendering.maths.Vector3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 27/09/15.
 */
public class Material {

    //Format:

//    struct Material {
//        //Packed into a single vec4
//        Vector3 ambientColour;
//        float ambientEnabled; //where ~0 is false and ~1 is true.
//
//        //Packed into a single vec4
//        Vector3 diffuseColour;
//        float alpha;
//
//        //Packed into a single vec4
//        Vector3 specularColour;
//        float specularity;
//
//        boolean useAmbientMap;
//        boolean useDiffuseMap;
//        boolean useSpecularColourMap;
//        boolean useSpecularityMap;
//    }

    public static final int NumFloats = 4 * 3; //3 vec4s.
    public static final int NumBooleans = 4;
    public static final int BufferSizeInBytes = 4 * NumFloats + 4 * NumBooleans; //4 bytes per float and per boolean.

    public static Material DefaultMaterial = new Material();

    private static final Sampler ambientMapSampler = new Sampler(TextureUnit.AmbientColourUnit);
    private static final Sampler diffuseMapSampler = new Sampler(TextureUnit.DiffuseColourUnit);
    private static final Sampler specularColourMapSampler = new Sampler(TextureUnit.SpecularColourUnit);
    private static final Sampler specularityMapSampler = new Sampler(TextureUnit.SpecularityUnit);

    private Vector3 _ambientColour;
    private Vector3 _diffuseColour;
    private Vector3 _specularColour;
    private float _transparency;
    private float _specularity;
    private boolean _useAmbient = true;

    private Optional<Texture> _diffuseMap = Optional.empty();
    private Optional<Texture> _ambientMap = Optional.empty();
    private Optional<Texture> _specularColourMap = Optional.empty();
    private Optional<Texture> _specularityMap = Optional.empty();

    private ByteBuffer _bufferRepresentation = null;

    public Material(Vector3 ambientColour, Vector3 diffuseColour, Vector3 specularColour, float transparency, float specularity) {
        _ambientColour = ambientColour;
        _diffuseColour = diffuseColour;
        _specularColour = specularColour;
        _transparency = transparency;
        _specularity = specularity;
    }

    public Material() {
        this(Vector3.zero, Vector3.one, Vector3.one, 0.f, 0.5f);
    }

    public void setUseAmbient(boolean useAmbient) {
        _bufferRepresentation = null;
        _useAmbient = useAmbient;
    }

    /** The ambient colour for the material. Without illumination it will 'glow' with this colour. */
    public Vector3 ambientColour() {
        return _ambientColour;
    }

    /** Set the ambient colour for the material. Without illumination it will 'glow' with this colour. */
    public void setAmbientColour(final Vector3 ambientColour) {
        _bufferRepresentation = null;
        _ambientColour = ambientColour;
    }

    /** The colour of the material's diffuse reflection – this is the main factor for the material's appearance. */
    public Vector3 diffuseColour() {
        return _diffuseColour;
    }

    /** Set the colour of the material's diffuse reflection – this is the main factor for the material's appearance. */
    public void setDiffuseColour(final Vector3 diffuseColour) {
        _bufferRepresentation = null;
        _diffuseColour = diffuseColour;
    }

    /** The colour of specular reflections. For most materials, this will be white.
     *  However, metals have a specular colour closer to their diffuse colour, and there are certain other exceptions. */
    public Vector3 specularColour() {
        return _specularColour;
    }

    /** Set the colour of specular reflections. For most materials, this will be white.
     *  However, metals have a specular colour closer to their diffuse colour, and there are certain other exceptions. */
    public void setSpecularColour(final Vector3 specularColour) {
        _bufferRepresentation = null;
        _specularColour = specularColour;
    }


    /** Transparency is a value in the range [0, 1], and directly controls the output alpha of the material. */
    public float transparency() {
        return _transparency;
    }

    /** Transparency controls the output alpha of the material, where a transparency of 0 is fully opaque and a transparency of 1 is fully transparent. */
    public void setTransparency(final float transparency) {
        _bufferRepresentation = null;
        _transparency = transparency;
    }


    /** A value for the gaussian specularity of the material in the range [0, 1], where 0 is perfectly smooth and 1 is very rough. */
    public float specularity() {
        return _specularity;
    }


    /** Set the gaussian specularity of the material in the range [0, 1], where 0 is perfectly smooth and 1 is very rough. */
    public void setSpecularity(final float specularity) {
        _bufferRepresentation = null;
        _specularity = specularity;
    }

    public void setDiffuseMap(final Texture diffuseMap) {
        _bufferRepresentation = null;
        _diffuseMap = Optional.of(diffuseMap);
    }

    public void setAmbientMap(final Texture ambientMap) {
        _bufferRepresentation = null;
        _ambientMap = Optional.of(ambientMap);
    }

    public void setSpecularColourMap(final Texture specularColourMap) {
        _bufferRepresentation = null;
        _specularColourMap = Optional.of(specularColourMap);
    }

    public void setSpecularityMap(final Texture specularityMap) {
        _bufferRepresentation = null; //TODO instead of regenerating the buffer after every change, we should probably just modify the data directly to represent the change.
        _specularityMap = Optional.of(specularityMap);
    }

    public ByteBuffer toBuffer() {

        if (_bufferRepresentation == null) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(BufferSizeInBytes);
            FloatBuffer floatBuffer = buffer.asFloatBuffer();

            floatBuffer.put(_ambientColour.v);
            floatBuffer.put(_useAmbient ? 1.f : 0.f);

            floatBuffer.put(_diffuseColour.v);
            floatBuffer.put(1.f - _transparency); //convert transparency to alpha

            floatBuffer.put(_specularColour.v);
            floatBuffer.put(_specularity);

            buffer.position(NumFloats * 4);
            buffer.putInt(_ambientMap.isPresent() ? 1 : 0);
            buffer.putInt(_diffuseMap.isPresent() ? 1 : 0);
            buffer.putInt(_specularColourMap.isPresent() ? 1 : 0);
            buffer.putInt(_specularityMap.isPresent() ? 1 : 0);

            buffer.flip();
            _bufferRepresentation = buffer;
        }

        return _bufferRepresentation; //FIXME be careful that the buffer doesn't get its position/mark etc. changed.
    }

    public static void bindSamplers() {
        Material.ambientMapSampler.bindToTextureUnit();
        Material.diffuseMapSampler.bindToTextureUnit();
        Material.specularColourMapSampler.bindToTextureUnit();
        Material.specularityMapSampler.bindToTextureUnit();
    }

    public static void unbindSamplers() {
        Material.ambientMapSampler.unbindSampler();
        Material.diffuseMapSampler.unbindSampler();
        Material.specularColourMapSampler.unbindSampler();
        Material.specularityMapSampler.unbindSampler();
    }

    public void bindTextures() {
        _diffuseMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.DiffuseColourUnit));
        _ambientMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.AmbientColourUnit));
        _specularColourMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.SpecularColourUnit));
        _specularityMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.SpecularityUnit));
    }

    public static void unbindTextures() {
        Texture.unbindTexture(TextureUnit.AmbientColourUnit);
        Texture.unbindTexture(TextureUnit.DiffuseColourUnit);
        Texture.unbindTexture(TextureUnit.SpecularityUnit);
        Texture.unbindTexture(TextureUnit.SpecularColourUnit);
    }

    /**
     * Converts a Phong specular value in the range [0, 1000] to a gaussian specular value in the range [0, 1]
     * @param phongSpecular The Phong specular value.
     * @return The gaussian specular value.
     */
    public static float phongSpecularToGaussian(float phongSpecular) {
        return 1.f/phongSpecular;
    }



}
