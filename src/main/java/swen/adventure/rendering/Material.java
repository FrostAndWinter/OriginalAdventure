package swen.adventure.rendering;

import org.lwjgl.BufferUtils;
import swen.adventure.rendering.maths.Vector3;

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
//    }

    public static final int NumFloats = 4 * 3; //3 vec4s.
    public static final int BufferSizeInBytes = 4 * NumFloats; //4 bytes per float

    public static Material DefaultMaterial = new Material(Vector3.zero, new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.5f);

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

    public Material(Vector3 ambientColour, Vector3 diffuseColour, Vector3 specularColour, float transparency, float specularity) {
        _ambientColour = ambientColour;
        _diffuseColour = diffuseColour;
        _specularColour = specularColour;
        _transparency = transparency;
        _specularity = specularity;
    }

    public void setUseAmbient(boolean useAmbient) {
        _useAmbient = useAmbient;
    }

    /** The ambient colour for the material. Without illumination it will 'glow' with this colour. */
    public Vector3 ambientColour() {
        return _ambientColour;
    }

    /** Set the ambient colour for the material. Without illumination it will 'glow' with this colour. */
    public void setAmbientColour(final Vector3 ambientColour) {
        _ambientColour = ambientColour;
    }

    /** The colour of the material's diffuse reflection – this is the main factor for the material's appearance. */
    public Vector3 diffuseColour() {
        return _diffuseColour;
    }

    /** Set the colour of the material's diffuse reflection – this is the main factor for the material's appearance. */
    public void setDiffuseColour(final Vector3 diffuseColour) {
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
        _specularColour = specularColour;
    }


    /** Transparency is a value in the range [0, 1], and directly controls the output alpha of the material. */
    public float transparency() {
        return _transparency;
    }

    /** Transparency controls the output alpha of the material, where a transparency of 0 is fully opaque and a transparency of 1 is fully transparent. */
    public void setTransparency(final float transparency) {
        _transparency = transparency;
    }


    /** A value for the gaussian specularity of the material in the range [0, 1], where 0 is perfectly smooth and 1 is very rough. */
    public float specularity() {
        return _specularity;
    }


    /** Set the gaussian specularity of the material in the range [0, 1], where 0 is perfectly smooth and 1 is very rough. */
    public void setSpecularity(final float specularity) {
        _specularity = specularity;
    }

    public void setDiffuseMap(final Texture diffuseMap) {
        _diffuseMap = Optional.of(diffuseMap);
    }

    public void setAmbientMap(final Texture ambientMap) {
        _ambientMap = Optional.of(ambientMap);
    }

    public void setSpecularColourMap(final Texture specularColourMap) {
        _specularColourMap = Optional.of(specularColourMap);
    }

    public void setSpecularityMap(final Texture specularityMap) {
        _specularityMap = Optional.of(specularityMap);
    }

    public FloatBuffer toFloatBuffer() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(NumFloats);

        buffer.put(_ambientColour.v);
        buffer.put(_useAmbient ? 1.f : 0.f);

        buffer.put(_diffuseColour.v);
        buffer.put(1.f - _transparency); //convert transparency to alpha

        buffer.put(_specularColour.v);
        buffer.put(_specularity);

        buffer.flip();
        return buffer;
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
