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

    public static final int NumFloats = 4 * 3; //3 vec4s.
    public static final int BufferSizeInBytes = 4 * NumFloats + 4; //4 bytes per float and one integer mask containing the boolean values.

    public static Material DefaultMaterial = new Material();


    // Samplers are lazily loaded in bindSamplers
    // This is because some computers do not support glGenSamplers, if this is called statically
    // it makes the Material class fail to load and crashes in other parts of the program
    private static Sampler ambientMapSampler;
    private static Sampler diffuseMapSampler;
    private static Sampler specularColourMapSampler;
    private static Sampler specularityMapSampler;
    private static Sampler normalMapSampler;

    private Vector3 _ambientColour;
    private Vector3 _diffuseColour;
    private Vector3 _specularColour;
    private float _opacity;
    private float _specularity;
    private boolean _useAmbient = true;

    private Optional<Texture> _diffuseMap = Optional.empty();
    private Optional<Texture> _ambientMap = Optional.empty();
    private Optional<Texture> _specularColourMap = Optional.empty();
    private Optional<Texture> _specularityMap = Optional.empty();
    private Optional<Texture> _normalMap = Optional.empty();

    private ByteBuffer _bufferRepresentation = null;

    public Material(Vector3 ambientColour, Vector3 diffuseColour, Vector3 specularColour, float opacity, float specularity) {
        _ambientColour = ambientColour;
        _diffuseColour = diffuseColour;
        _specularColour = specularColour;
        _opacity = opacity;
        _specularity = specularity;
    }

    public Material() {
        this(Vector3.zero, Vector3.one, Vector3.one, 1.f, 0.5f);
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


    /** Opacity is a value in the range [0, 1], and directly controls the output alpha of the material. */
    public float transparency() {
        return _opacity;
    }

    /** Opacity controls the output alpha of the material, where a opacity of 1 is fully opaque and a opacity of 0 is fully transparent. */
    public void setOpacity(final float opacity) {
        _bufferRepresentation = null;
        _opacity = opacity;
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

    /**
     * @param diffuseMap A map that defines the diffuse colour of the material at each texture coordinate.
     */
    public void setDiffuseMap(final Texture diffuseMap) {
        _bufferRepresentation = null;
        _diffuseMap = Optional.of(diffuseMap);
    }

    /**
     * @param ambientMap A map that defines the ambient colour of the material at each texture coordinate.
     */
    public void setAmbientMap(final Texture ambientMap) {
        _bufferRepresentation = null;
        _ambientMap = Optional.of(ambientMap);
    }

    /**
     * @param specularColourMap A map that defines the specular colour of the material at each texture coordinate.
     */
    public void setSpecularColourMap(final Texture specularColourMap) {
        _bufferRepresentation = null;
        _specularColourMap = Optional.of(specularColourMap);
    }

    /**
     * @param specularityMap A map that defines the specularity of the material at each texture coordinate.
     */
    public void setSpecularityMap(final Texture specularityMap) {
        _bufferRepresentation = null; //TODO instead of regenerating the buffer after every change, we should probably just modify the data directly to represent the change.
        _specularityMap = Optional.of(specularityMap);
    }

    public void setNormalMap(final Texture normalMap) {
        _bufferRepresentation = null;
        _normalMap = Optional.of(normalMap);
    }

    private int packMapFlags() {
        int result = 0;
        if (_ambientMap.isPresent()) { result |= 1; }
        if (_diffuseMap.isPresent()) { result |= 1 << 1; }
        if (_specularColourMap.isPresent()) { result |= 1 << 2; }
        if (_specularityMap.isPresent()) { result |= 1 << 3; }
        if (_normalMap.isPresent()) { result |= 1 << 4; }
        return result;
    }

    /**
     * @return This material's attributes, packed into a ByteBuffer.
     * The format is as follows:
     * struct Material {
     * //Packed into a single vec4
     * Vector3 ambientColour;
     * float ambientEnabled; //where ~0 is false and ~1 is true.
     *
     * //Packed into a single vec4
     * Vector3 diffuseColour;
     * float alpha;
     *
     * //Packed into a single vec4
     * Vector3 specularColour;
     * float specularity;

     * boolean useAmbientMap; //packed in an integer as 1 << 0
     * boolean useDiffuseMap; //packed in an integer as 1 << 1
     * boolean useSpecularColourMap; //packed in an integer as 1 << 2
     * boolean useSpecularityMap; //packed in an integer as 1 << 3
     * boolean useNormalMap; //packed in an integer as 1 << 4
     */
    public ByteBuffer toBuffer() {

        if (_bufferRepresentation == null) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(BufferSizeInBytes);
            FloatBuffer floatBuffer = buffer.asFloatBuffer();

            floatBuffer.put(_ambientColour.v);
            floatBuffer.put(_useAmbient ? 1.f : 0.f);

            floatBuffer.put(_diffuseColour.v);
            floatBuffer.put(_opacity);

            floatBuffer.put(_specularColour.v);
            floatBuffer.put(_specularity);

            buffer.position(NumFloats * 4);

            buffer.putInt(this.packMapFlags());

            buffer.flip();
            _bufferRepresentation = buffer;
        }

        return _bufferRepresentation; //FIXME be careful that the buffer doesn't get its position/mark etc. changed.
    }

    /**
     * Binds all the samplers for Material to their respective texture units.
     */
    public static void bindSamplers() {
        try {
            if (ambientMapSampler == null) {
                ambientMapSampler = new Sampler(TextureUnit.AmbientColourUnit);
            }
            Material.ambientMapSampler.bindToTextureUnit();
            if (diffuseMapSampler == null) {
                diffuseMapSampler = new Sampler(TextureUnit.DiffuseColourUnit);
            }
            Material.diffuseMapSampler.bindToTextureUnit();
            if (specularColourMapSampler == null) {
                specularColourMapSampler = new Sampler(TextureUnit.SpecularColourUnit);
            }
            Material.specularColourMapSampler.bindToTextureUnit();
            if (specularityMapSampler == null) {
                specularityMapSampler = new Sampler(TextureUnit.SpecularityUnit);
            }
            Material.specularityMapSampler.bindToTextureUnit();
            if (normalMapSampler == null) {
                normalMapSampler = new Sampler(TextureUnit.NormalMapUnit);
            }
            Material.normalMapSampler.bindToTextureUnit();
        } catch (IllegalStateException ignored) {

        }
    }

    /**
     * Unbinds all the samplers for Material from their respective texture units.
     */
    public static void unbindSamplers() {
        if (ambientMapSampler != null) {
            Material.ambientMapSampler.unbindSampler();
        }
        if (diffuseMapSampler != null) {
            Material.diffuseMapSampler.unbindSampler();
        }
        if (specularColourMapSampler != null) {
            Material.specularColourMapSampler.unbindSampler();
        }
        if (specularityMapSampler != null) {
            Material.specularityMapSampler.unbindSampler();
        }
        if (normalMapSampler != null) {
            Material.normalMapSampler.unbindSampler();
        }
    }

    /**
     * Binds all the textures that are present for this material to their respective texture units.
     */
    public void bindTextures() {
        _diffuseMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.DiffuseColourUnit));
        _ambientMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.AmbientColourUnit));
        _specularColourMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.SpecularColourUnit));
        _specularityMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.SpecularityUnit));
        _normalMap.ifPresent(texture -> texture.bindToTextureUnit(TextureUnit.NormalMapUnit));
    }

    /**
     * Unbinds all textures from their respective texture units.
     */
    public static void unbindTextures() {
        Texture.unbindTexture(TextureUnit.AmbientColourUnit);
        Texture.unbindTexture(TextureUnit.DiffuseColourUnit);
        Texture.unbindTexture(TextureUnit.SpecularityUnit);
        Texture.unbindTexture(TextureUnit.SpecularColourUnit);
        Texture.unbindTexture(TextureUnit.NormalMapUnit);
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
